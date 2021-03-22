package com.jrmf.controller.constant;

public enum ChannelUserRealNameType {

  /**
   * 0正常实名 1已删除 2二要素验证失败
   */
  SUCCESS(0, "实名认证成功"),

  DELETE(1, "实名认证已删除"),

  FAIL(2, "二要素验证失败");

  private final int code;
  private final String desc;

  ChannelUserRealNameType(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }

  public static ChannelUserRealNameType codeOf(int code) {
    for (ChannelUserRealNameType custom : values()) {
      if (custom.getCode() == code) {
        return custom;
      }
    }
    return null;
  }

}
