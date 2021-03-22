package com.jrmf.common;

public enum BatchInvoiceStatus {

  NO_INVOICE("0","未开票"),
  IN_INVOICE("1","开票处理中"),
  SUCCESS_INVOICE("2","开票完成"),
  FAIL_INVOICE("3","开票失败")
  ;


  private String node;
  private String msg;

  BatchInvoiceStatus(String node, String msg) {
    this.node = node;
    this.msg = msg;
  }

  public String getNode() {
    return node;
  }

  public String getMsg() {
    return msg;
  }
}
