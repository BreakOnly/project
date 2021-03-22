package com.jrmf.controller.constant.sms;

public enum SmsTemplateCodeEnum {

  LOGIN("SMS_192530789", "登录短信模板"),
  WARNING_ORDER("SMS_192576826", "落地超时提醒模板"),
  SHOW_FULL_KEY("SMS_192541736", "显示签名秘钥模板"),
  RESET_KEY("SMS_192541752", "重置签名秘钥模板"),
  RECHARGE_CONFIRM("SMS_192571783", "系统充值到账提醒模板"),
  SIGN("SMS_192576919", "签约验证码模板"),
  PAY_SUCCESS_ORDER("", "下发成功短信提醒"),
  YXY_UNSIGN_NOTICE("", "营销云未签约通知"); //通知类短信阿里云需要提供相关资质，预留变量

  private final String code;
  private final String desc;

  SmsTemplateCodeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public String getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }
}
