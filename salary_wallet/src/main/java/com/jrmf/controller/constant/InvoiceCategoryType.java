package com.jrmf.controller.constant;

/**
 * 发票类别
 */
public enum InvoiceCategoryType {


  RECHARGE(1,"余额充值"),
  ISSUE(2,"实发申请");

  private final int code;
  private final String desc;

  private InvoiceCategoryType(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }
}
