package com.ty.apis;

import com.ty.apis.timer.NetEasyClawer;
import com.ty.apis.utils.MongoDBUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.bson.Document;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.*;

@SpringBootApplication
public class ApisApplication extends SpringBootServletInitializer {

    private static String _163NewsUrl = "http://3g.163.com/touch/news/";

    private static final Logger logger = LogManager.getLogger(ApisApplication.class);

    public static void main(String[] args) {
        //testDB();
        SpringApplication.run(ApisApplication.class, args);
    }


    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        logger.info("---------->onStartUp");

        NetEasyClawer netEasyClawer = NetEasyClawer.getInstance();

        // 3min刷新一次新闻
        netEasyClawer.SetParams(_163NewsUrl, 5, 60);
        netEasyClawer.StartTimer();

        super.onStartup(servletContext);
    }

    private static void testDB()
    {
        MongoDBUtils mongoDBDao =  MongoDBUtils.getMongoDBDaoImplInstance();

        boolean isExit = false;
        isExit = mongoDBDao.isExist("test-object", "name", "liming");
        isExit = mongoDBDao.isExist("test-object", "name", "zhangming");
        isExit = mongoDBDao.isExist("test-object", "score", 50);


        List<Map<String, Object>> listMap1 = new LinkedList<>();

        Map<String, Object> m11 = new HashMap<String, Object>();
        m11.put("name", "liming");
        m11.put("score", 100);

        Map<String, Object> m22 = new HashMap<String, Object>();
        m22.put("name", "zhangming");
        m22.put("score", 80);

        Map<String, Object> m33 = new HashMap<String, Object>();
        m33.put("name", "wanger");
        m33.put("score", 60);

        listMap1.add(m11);
        listMap1.add(m22);
        listMap1.add(m33);


        mongoDBDao.inSertMany("test-object", listMap1);

        Map<String, Object> m12 = new HashMap<String, Object>();
        m12.put("name", "liming");
        m12.put("score", 80);
        List<Document> ret = mongoDBDao.find("test-object", m12);

        Map<String, Object> del = new HashMap<>();
        List<Object> value = new ArrayList<>();
        value.add("liming");
        value.add("wanger");
        del.put("name", value);

        List<Object> value1 = new ArrayList<>();
        value1.add(50);
        value1.add(30);
        del.put("score", value1);


//        long ret1 = mongoDBDao.delete("test-object", del);
//
//        System.out.println("delete: " + ret1);

    }
}
