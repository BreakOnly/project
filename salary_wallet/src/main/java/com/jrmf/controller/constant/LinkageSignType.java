package com.jrmf.controller.constant;

/**
 * 支付前是否联动第三方平台签约（0：否，1：是）
 */
public enum LinkageSignType {

  NOT(0, "否"),

  YES(1, "是");


  private final int code;
  private final String desc;

  LinkageSignType(int code, String desc) {
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
