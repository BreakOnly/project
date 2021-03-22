package com.jrmf.payment.gsbpay;

public class GsbConfig {

  //测试请求地址
  public static final String REQUEST_URL_TEST="http://39.106.185.66:80";

  //测试请求地址
  public static final String REQUEST_URL_PROD="https://pay.gongsibao.com";

  //付款
  public static final String PAYMENT_PAY="/merchant/order/transfer";

  //付款结果查询
  public static final String PAYMENT_QUERY="/merchant/order/query";

  //付款结果查询
  public static final String BALANCE_QUERY="/merchant/busiAccount/query";

}
