package com.jrmf.controller.constant;

public enum ReceiptStatusEnum {

  NOT_RECEIPT(0, "无回单"),
  EXIST_RECEIPT(1, "有回单");

  private final int code;
  private final String desc;

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }

  ReceiptStatusEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static String codeOf(int code) {
    for(ReceiptStatusEnum type : values()) {
      if(type.getCode() == code){
        return type.getDesc();
      }
    }
    return null;
  }
}
