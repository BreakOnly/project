package com.jrmf.controller.constant;

public enum LdCommissionBusinessTypeEnum {

  DEFAUTL("00", "默认"),

  B2B2B2C("01", "联动B2B2B2C业务"),

  B2CSPLIT("02", "拆单下发明细");

  private final String code;
  private final String desc;

  LdCommissionBusinessTypeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public String getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }


  public static LdCommissionBusinessTypeEnum codeOf(String code) {
    for (LdCommissionBusinessTypeEnum commissionBusinessType : values()) {
      if (commissionBusinessType.getCode().equals(code)) {
        return commissionBusinessType;
      }
    }
    return null;
  }

}
