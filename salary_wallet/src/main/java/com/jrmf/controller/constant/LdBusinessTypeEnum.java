package com.jrmf.controller.constant;

/**
 * 联动交易业务类型
 *
 * @author linsong
 * @date 2020/11/3
 */
public enum LdBusinessTypeEnum {

  TRANSFERTOB(1, "转账"),

  TRANSFERTOBANDTOC(2, "下发"),

  REVERSAL(3, "冲正"),

  TRANSFERTOBTOC(4, "转账下发"),

  SPLITORDER(5, "拆单下发");

  private final int code;
  private final String desc;

  LdBusinessTypeEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }


  public static LdBusinessTypeEnum codeOf(int code) {
    for (LdBusinessTypeEnum businessType : values()) {
      if (businessType.getCode() == code) {
        return businessType;
      }
    }
    return null;
  }
}
