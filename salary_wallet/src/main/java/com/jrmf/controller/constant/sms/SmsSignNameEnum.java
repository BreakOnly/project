package com.jrmf.controller.constant.sms;

public enum SmsSignNameEnum {

  JRMF("智税通");

  private final String name;

  SmsSignNameEnum(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
