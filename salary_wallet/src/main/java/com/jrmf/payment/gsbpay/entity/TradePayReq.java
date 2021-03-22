package com.jrmf.payment.gsbpay.entity;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class TradePayReq implements Serializable {

  private String notityUrl;
  private String tradeTime;
  private String appId;
  private String outTradeNo;
  private String mchtId;
  private String signType = "MD5";
  private String currency = "CNY";
  private String nonceStr;

  private List<TradeItemReq> bizContent;
}
