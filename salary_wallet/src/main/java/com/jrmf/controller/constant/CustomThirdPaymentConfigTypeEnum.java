package com.jrmf.controller.constant;

public enum CustomThirdPaymentConfigTypeEnum {

  CUSTOM_KEY(0, "商户平台方通道秘钥配置"),

  CUSTOM_MERCHANTID(1, "商户平台方appid配置"),

  CUSTOM_KEY_AND_MERCHANTID(2, "商户平台方通道秘钥及appid配置"),

  REALCOMPANY_KEY(3, "转包公司平台方通道配置");

  private final int code;
  private final String desc;

  CustomThirdPaymentConfigTypeEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }


  public static CustomThirdPaymentConfigTypeEnum codeOf(int code) {
    for (CustomThirdPaymentConfigTypeEnum typeEnum : values()) {
      if (typeEnum.getCode() == code) {
        return typeEnum;
      }
    }
    return null;
  }

}
