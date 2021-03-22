package com.jrmf.controller.constant;

public enum LdOrderStatusEnum {

  PANDING(0, "待处理"),

  SUCCESS(1, "交易成功"),

  FAILURE(2, "交易失败"),

  SUBMITTED(3, "处理中");

  private final int code;
  private final String desc;

  LdOrderStatusEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }

  public static LdOrderStatusEnum codeOf(int code) {
    for (LdOrderStatusEnum commissionStatus : values()) {
      if (commissionStatus.getCode() == code) {
        return commissionStatus;
      }
    }
    return null;
  }
}
