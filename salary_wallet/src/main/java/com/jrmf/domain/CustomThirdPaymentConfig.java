package com.jrmf.domain;

import lombok.Data;

@Data
public class CustomThirdPaymentConfig {

  private Integer id;

  private Integer configType;

  private String customKey;

  private String pathNo;

  private String thirdMerchid;

  private String privateKey;

  private String publicKey;

  private String apiKey;

  private String parameter1;

  private String parameter2;

  private String parameter3;

  private String createTime;

  private String updateTime;

  private String customName;

  private Integer status;

  private String pathName;

  private Integer pathType;

  private Integer pathKeyType;
}