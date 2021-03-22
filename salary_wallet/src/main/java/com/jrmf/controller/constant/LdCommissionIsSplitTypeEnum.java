package com.jrmf.controller.constant;

public enum LdCommissionIsSplitTypeEnum {

  NO(0, "非拆单交易"),

  YES(1, "拆单交易");

  private final int code;
  private final String desc;

  LdCommissionIsSplitTypeEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }


  public static LdCommissionIsSplitTypeEnum codeOf(int code) {
    for (LdCommissionIsSplitTypeEnum commissionBusinessType : values()) {
      if (commissionBusinessType.getCode() == code) {
        return commissionBusinessType;
      }
    }
    return null;
  }

}
