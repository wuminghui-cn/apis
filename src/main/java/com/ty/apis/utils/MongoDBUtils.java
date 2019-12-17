package com.ty.apis.utils;

import com.mongodb.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

public class MongoDBUtils{

    /**
     * MongoClient的实例代表数据库连接池，是线程安全的，可以被多线程共享，客户端在多线程条件下仅维持一个实例即可
     * Mongo是非线程安全的，目前mongodb API中已经建议用MongoClient替代Mongo
     */
    private MongoClient mongoClient = null;

    private static final Logger logger = LogManager.getLogger(MongoDBUtils.class);

    private static final Integer soTimeOut = 30000;
    private static final Integer connectionsPerHost = 500;
    private static final Integer threadsBlock = 30;
    private String host = "localhost";
    private int port = 27017;
    private String database = "163News";


    private MongoDBUtils(){
        if (mongoClient == null){
            mongoClient = new MongoClient(
                    new ServerAddress(host, port),
                    new MongoClientOptions.Builder()
                            .socketTimeout(soTimeOut)
                            .connectionsPerHost(connectionsPerHost)
                            .threadsAllowedToBlockForConnectionMultiplier(
                                    threadsBlock).socketTimeout(20*1000).build());
        }
    }

    /********单例模式声明开始，采用饿汉式方式生成，保证线程安全********************/

    //类初始化时，自行实例化
    private static final MongoDBUtils mongoDBDaoImpl = new MongoDBUtils();

    /**
     *
     * 方法名：getMongoDBDaoImplInstance
     * 创建时间：2014-8-30 下午04:29:26
     * 描述：单例的静态工厂方法
     * @return
     */
    public static MongoDBUtils getMongoDBDaoImplInstance(){
        return mongoDBDaoImpl;
    }


    public MongoDatabase getDb(String dbName) {
        return mongoClient.getDatabase(dbName);
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return mongoClient.getDatabase(database).getCollection(collectionName);
    }

    /**
     * 批量插入文档
     * @param collectionName
     * @param listMap
     */
    public void inSertMany(String collectionName, List<Map<String, Object>> listMap) {

        logger.info("---------->InsertMany");

        MongoDatabase mongoDatabase = mongoClient.getDatabase(database);

        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);

        List<Document> docs = new ArrayList<Document>();
        for (Map<String, Object> map : listMap)
        {
            Document doc = new Document();
            doc.putAll(map);
            docs.add(doc);
        }

        mongoCollection.insertMany(docs);
    }

    /**
     *  根据关键字删除匹配的文档
     * @param collectionName
     * @param deleteArgs   name ---[liming,wanger]
     *                     socre ---[100, 80]
     * @return
     */
    public long delete(String collectionName, Map<String, Object> deleteArgs) {
        long size = 0;
        MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);

        for (String key : deleteArgs.keySet()) {
            Document doc = new Document();

            for (int i = 0; i < ((List<Object>)deleteArgs.get(key)).size(); i++)
            {
                doc.put(key, ((List<Object>)deleteArgs.get(key)).get(i));

                DeleteResult result = null;
                try {
                    result = mongoCollection.deleteMany(doc);
                    size += result.getDeletedCount();
                } catch (MongoWriteException e) {
                    logger.error(e.getMessage());
                }
            }
        }

        return size;
    }

    /**
     *  根据关键字检索返回文档清单
     * @param collectionName
     * @param findArgs  name  -- liming
     *                  score -- 80
     * @return
     */
    public List<Document> find(String collectionName, Map<String, Object> findArgs) {

        MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);

        BasicDBObject query = new BasicDBObject();

        BasicDBList values = new BasicDBList();
        for (String key : findArgs.keySet()) {
            values.add(new BasicDBObject(key, findArgs.get(key)));
        }

        query.put("$or", values);

        MongoCursor<Document> cursor = mongoCollection.find(query).iterator();
        List<Document> docs = new ArrayList<Document>();
        try {
            while (cursor.hasNext()) {
                Document findDoc = cursor.next();
                docs.add(findDoc);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return docs.size() > 0 ? docs : null;
    }

    /**
     *  根据关键字检索判断文档是否存在
     * @param collectionName
     * @param findArgs  name  -- liming
     *                  score -- 80
     * @return
     */
    public boolean isExist(String collectionName, Map<String, Object> findArgs) {

        MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);

        BasicDBObject query = new BasicDBObject();

        BasicDBList values = new BasicDBList();
        for (String key : findArgs.keySet()) {
            values.add(new BasicDBObject(key, findArgs.get(key)));
        }

        query.put("$or", values);

        MongoCursor<Document> cursor = mongoCollection.find(query).iterator();

        return cursor.hasNext() ? true : false;
    }

    /**
     *  根据关键字检索判断文档是否存在
     * @param collectionName
     * @param key
     * @param value
     * @return
     */
    public boolean isExist(String collectionName, String key, Object value) {

        MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);

        BasicDBObject query = new BasicDBObject();
        BasicDBList values = new BasicDBList();
        values.add(new BasicDBObject(key, value));
        query.put("$or", values);

        MongoCursor<Document> cursor = mongoCollection.find(query).iterator();

        return cursor.hasNext() ? true : false;
    }
}
