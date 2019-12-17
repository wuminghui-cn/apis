package com.ty.apis.service;

public interface NetEasyService
{
    String getNewsDetails(String type, String id);

    String getNewsList(String type, String sTime, String eTime, String page);
}