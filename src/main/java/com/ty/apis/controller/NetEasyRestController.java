package com.ty.apis.controller;

import com.ty.apis.service.NetEasyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NetEasyRestController {

    @Autowired
    private NetEasyService netEasyService;

    @RequestMapping(value = "/api/newsList", params = {"type", "sTime", "eTime", "page"}, method = RequestMethod.POST)
    public String getNewsList(
            @RequestParam("type") String type,
            @RequestParam("sTime") String sTime,
            @RequestParam("eTime") String eTime,
            @RequestParam("page") String page)
    {
        String ret = netEasyService.getNewsList(type, sTime, eTime, page);
        if (ret.equals(""))
        {
            return "ret is null";
        }
        return ret;
    }

    @RequestMapping(value = "/api/newsDetail", params = {"type", "docid"}, method = RequestMethod.POST)
    public String getNewsDetails(
            @RequestParam("type") String type,
            @RequestParam("docid") String docid)
    {
        String ret = netEasyService.getNewsDetails(type, docid);
        return ret;
    }

}
