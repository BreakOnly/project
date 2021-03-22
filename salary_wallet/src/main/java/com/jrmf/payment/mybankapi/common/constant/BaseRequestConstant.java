package com.jrmf.payment.mybankapi.common.constant;

public interface BaseRequestConstant {

  String GATEWAYURL = "http://test.tc.mybank.cn/gop/gateway.do";
  String VERSION = "2.1";
  String CHARSET = "UTF-8";
  String SIGN_TYPE = "TWSIGN";

  String SUCCESS = "T";
  String FAIL = "F";

  String TRADE_FINISHED = "TRADE_FINISHED"; //提现成功
  String TRADE_FAILED = "TRADE_FAILED"; //提现失败
  String REFUND_TICKET = "REFUND_TICKET"; //退票成功

  String NOTIFY_TYPE = "remit_sync";

}
