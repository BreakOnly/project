package com.jrmf.controller.constant;

public enum SortType {

  DESCENDING(0, "按时间降序"),

  ASCENDING(1, "按时间升序");

  private final int code;
  private final String desc;

  SortType(int code, String desc) {
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
