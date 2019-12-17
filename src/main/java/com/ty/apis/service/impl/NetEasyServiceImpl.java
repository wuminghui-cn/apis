package com.ty.apis.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.ty.apis.utils.MongoDBUtils;
import org.bson.Document;
import com.ty.apis.service.NetEasyService;
import org.springframework.stereotype.Service;

@Service
public class NetEasyServiceImpl implements NetEasyService {
    /**
     * 根据类型、ID获取新闻的详情
     * @param type  分类
     * @param docid ID
     * @return  json
     */
    @Override
    public String getNewsDetails(String type, String docid)
    {
        MongoDBUtils mongoDBUtils =  MongoDBUtils.getMongoDBDaoImplInstance();

        BasicDBObject query = new BasicDBObject();
        query.put("docid", docid);

        MongoCursor<Document> cursor = mongoDBUtils.getCollection("c_details_" + type).find(query).skip(0).iterator();

        String json = new String();

        while (cursor.hasNext())
        {
            json = cursor.next().toJson();
        }

        return json;
    }

    /**
     *  根据类型、时间获取新闻列表
     * @param type  类型
     * @param sTime 开始时间
     * @param eTime 结束时间
     * @param page
     * @return
     */
    @Override
    public String getNewsList(String type, String sTime, String eTime, String page)
    {
        MongoDBUtils mongoDBUtils =  MongoDBUtils.getMongoDBDaoImplInstance();

        //  String sTime = "2019-12-09";
        //  String eTime = "2019-12-10";

        BasicDBObject query = new BasicDBObject();
        query.put("ptime", new BasicDBObject(
                "$gte", sTime + " 00:00:00")
                .append("$lte", eTime + " 23:59:59")
        );

        MongoCursor<Document> cursor = mongoDBUtils.getCollection("c_details_" + type).find(query).skip(0).iterator();


        String ret = new String();

        JSONArray jsRet = new JSONArray();

        while (cursor.hasNext())
        {
            String js = cursor.next().toJson();

            JSONObject jsonObject =  JSON.parseObject(js);
            String docid = (String)jsonObject.get("docid");
            String ptime = (String)jsonObject.get("ptime");
            String title = (String)jsonObject.get("title");
            String source = (String)jsonObject.get("source");
            String link = (String)jsonObject.get("link");

            JSONObject jsonImg = new JSONObject();

            JSONArray jsonPicInfo = jsonObject.getJSONArray("picInfo");
            if (null != jsonPicInfo) {
                for (int i = 0; i < jsonPicInfo.size(); i++) {
                    JSONObject picInfo = jsonPicInfo.getJSONObject(i);
                    if (null != picInfo) {
                        jsonImg.put("url", picInfo.get("url"));
                    }
                }
            }

            JSONObject jsOne = new JSONObject();
            jsOne.put("docid", docid);
            jsOne.put("ptime", ptime);
            jsOne.put("title", title);
            jsOne.put("source", source);
            jsOne.put("imgList", jsonImg);
            jsOne.put("link", link);

            jsRet.add(jsOne);
        }

        return jsRet.toString();
    }
}
