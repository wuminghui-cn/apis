package com.ty.apis.timer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.ty.apis.utils.MongoDBUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetEasyClawer
{
    private String url;
    private Integer initialDelay;
    private Integer peroid;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    //private static RandomAccessFile randomAccessFile;
    private static final Logger logger = LogManager.getLogger(NetEasyClawer.class);

    private static NetEasyClawer netEasyClawer;

    private static int iTimer = 0;
    private NetEasyClawer(){}

    public static NetEasyClawer getInstance()
    {
        if (null == netEasyClawer)
        {
            netEasyClawer = new NetEasyClawer();
        }

        return  netEasyClawer;
    }

    public void SetParams(String url, Integer initialDelay, Integer peroid)
    {
        this.url = url;
        this.initialDelay = initialDelay;
        this.peroid = peroid;
    }

    public void StartTimer()
    {
        logger.info("-------->StartTimer");
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                iTimer++;
                logger.info("-------->Run: " + iTimer);

                parse163Web(url);
            }
        };

        scheduledExecutorService.scheduleAtFixedRate(runnable, initialDelay, peroid, TimeUnit.SECONDS);
    }

    public void StopTimer()
    {
        logger.info("-------->StopTimer");
        scheduledExecutorService.shutdown();
    }

    /**
     *
     * @param url
     */
    private void parse163Web(String url)
    {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements items = doc.getElementsByTag("script");
            String topicData="";

            for (Element item : items)
            {
                String str = item.data();

                if (str.contains("topicData")) {
                    int i = str.indexOf("{");
                    int j = str.lastIndexOf(";");
                    topicData = str.substring(i, j);
                    //logger.info("****163Ret:" + topicData);
                }
            }

            JSONObject jsonObject =  JSON.parseObject(topicData);
            JSONObject j_data = jsonObject.getJSONObject("data");

            Set<String> typeSet= j_data.keySet();
            Iterator<String> itType = typeSet.iterator();

            MongoDBUtils mongoDBUtils =  MongoDBUtils.getMongoDBDaoImplInstance();

            while (itType.hasNext())
            {
                String type = itType.next();

                List<Map<String, Object>> detailsMap = new LinkedList<>();

                JSONArray json = new JSONArray();
                json = (JSONArray) j_data.get(type);

                for (int i = 0; i < json.size(); i++)
                {
                    // 详情信息入表
                    Set<String> keySet = json.getJSONObject(i).keySet();
                    Iterator<String> it = keySet.iterator();

                    Map<String, Object> mDetails = new HashMap<String, Object>();

                    while (it.hasNext())
                    {
                        String key = it.next();
                        Object value = json.getJSONObject(i).get(key);

                        mDetails.put(key, value);
                    }

                    // 查找是否已经存在该记录
                    boolean isExit = mongoDBUtils.isExist("c_details_" + type, "docid", mDetails.get("docid"));
                    if (isExit)
                    {
                        //logger.info("-------------->isExist. docid: " + mDetails.get("docid"));
                    }
                    else
                    {
                        detailsMap.add(mDetails);
                    }
                }

                // 批量插入
                if (detailsMap.size() > 0)
                {
                    mongoDBUtils.inSertMany("c_details_" + type, detailsMap);
                }
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }


}
