package com.jrmf.controller.constant;

public enum PathKeyTypeEnum {

  PLAYFORM(0, "平台共用秘钥"),

  COMPANY(1, "服务公司共用秘钥"),

  CUSTOM(2, "商户独立秘钥");

  private final int code;
  private final String desc;

  PathKeyTypeEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }


  public static PathKeyTypeEnum codeOf(int code) {
    for (PathKeyTypeEnum typeEnum : values()) {
      if (typeEnum.getCode() == code) {
        return typeEnum;
      }
    }
    return null;
  }

}
