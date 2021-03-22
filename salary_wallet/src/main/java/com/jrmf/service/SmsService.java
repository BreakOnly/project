package com.jrmf.service;

public interface SmsService {

  boolean sendNoticeSMS(String phone, String content, String signName, String templateCode,
      String templateParam);

}
