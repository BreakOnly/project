package com.jrmf.domain;

import lombok.Data;

@Data
public class CustomBalanceHistory {

  private Integer id;

  private String customKey;

  private String companyId;

  private Integer tradeType;

  private String tradeAmount;

  private String preTradeBalance;

  private String afterTradeBalance;

  private Integer tradeNumber;

  private String remark;

  private String customName;

  private String companyName;

  private Integer payType;

  private String relateOrderNo;

  private String operator;

  private String createTime;


  public CustomBalanceHistory() {
  }

  public CustomBalanceHistory(String customKey, String companyId, Integer payType,
      String tradeAmount, Integer tradeNumber, Integer tradeType) {
    this.customKey = customKey;
    this.companyId = companyId;
    this.tradeType = tradeType;
    this.tradeAmount = tradeAmount;
    this.tradeNumber = tradeNumber;
    this.payType = payType;
  }

  public CustomBalanceHistory(String customKey, String companyId, Integer payType,
                              String tradeAmount, Integer tradeNumber, Integer tradeType,
                              String relateOrderNo,String operator) {
    this.customKey = customKey;
    this.companyId = companyId;
    this.tradeType = tradeType;
    this.tradeAmount = tradeAmount;
    this.tradeNumber = tradeNumber;
    this.payType = payType;
    this.relateOrderNo = relateOrderNo;
    this.operator = operator;
  }
}