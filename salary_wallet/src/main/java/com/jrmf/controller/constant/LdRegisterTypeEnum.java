package com.jrmf.controller.constant;

/**
 * 联动交易登记类型(不知道有啥用，为原来的联动相关交易增加一个枚举管理)
 *
 * @author linsong
 * @date 2020/11/3
 */
public enum LdRegisterTypeEnum {

  DEFAULT(1, "默认类型"), //原有联动交易都为1

  APIPAYMENT(2, "来源api下发");

  private final int code;
  private final String desc;

  LdRegisterTypeEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }


  public static LdRegisterTypeEnum codeOf(int code) {
    for (LdRegisterTypeEnum registerType : values()) {
      if (registerType.getCode() == code) {
        return registerType;
      }
    }
    return null;
  }
}
