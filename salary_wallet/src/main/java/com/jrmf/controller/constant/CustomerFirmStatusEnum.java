package com.jrmf.controller.constant;

public enum CustomerFirmStatusEnum {

  SUCCESS(1,"成功"),

  FAIL(2,"失败"),

  DOING(3,"处理中");

  private int code;
  private String desc;

  CustomerFirmStatusEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }

  public static CustomerFirmStatusEnum codeOf(int code) {
    for(CustomerFirmStatusEnum type : values()) {
      if(type.getCode() == code){
        return type;
      }
    }
    return null;
  }

}
