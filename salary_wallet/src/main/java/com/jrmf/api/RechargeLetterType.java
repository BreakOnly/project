package com.jrmf.api;

public enum RechargeLetterType {
  LOCAL(new Byte("1"), "本地生成"), ESIGN(new Byte("2"), "电子签名");

  private Byte rechargeLetterTypeCode;
  private String rechargeLetterTypeName;

  RechargeLetterType(Byte rechargeLetterTypeCode, String rechargeLetterTypeName) {
    this.rechargeLetterTypeCode = rechargeLetterTypeCode;
    this.rechargeLetterTypeName = rechargeLetterTypeName;
  }

  public Byte getRechargeLetterTypeCode() {
    return rechargeLetterTypeCode;
  }

  public void setRechargeLetterTypeCode(Byte rechargeLetterTypeCode) {
    this.rechargeLetterTypeCode = rechargeLetterTypeCode;
  }

  public String getRechargeLetterTypeName() {
    return rechargeLetterTypeName;
  }

  public void setRechargeLetterTypeName(String rechargeLetterTypeName) {
    this.rechargeLetterTypeName = rechargeLetterTypeName;
  }
}
