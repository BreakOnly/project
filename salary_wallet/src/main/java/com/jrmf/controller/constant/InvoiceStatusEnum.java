package com.jrmf.controller.constant;

public enum InvoiceStatusEnum {

  NOT_INVOICE(0, "未开票"),
  INVOICE_BEING(1, "开票处理中"),
  INVOICE_SUCCESS(2, "开票完成"),
  INVOICE_FAIL(3, "开票失败");

  private final int code;
  private final String desc;

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }

  InvoiceStatusEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static String codeOf(int code) {
    for(InvoiceStatusEnum type : values()) {
      if(type.getCode() == code){
        return type.getDesc();
      }
    }
    return null;
  }
}
