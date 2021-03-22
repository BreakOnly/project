package com.jrmf.common;

/**
 * @author: YJY
 * @date: 2020/12/18 15:42
 * @description:
 */

public enum  NodeMessage {

  WAIT_USER_UPLOAD(1,"待用户提交"),
  WAIT_ENTERPRISE_AUTH(2,"待企业审核"),
  WAIT_GOVERNMENT_AUTH(3,"政府审核中"),
  AUTH_SUCCESS(4,"审核成功"),
  AUTH_FAIL(5,"审核失败");

  int node;
  String msg;

  NodeMessage(int node, String msg) {
    this.node = node;
    this.msg = msg;
  }

  public int getNode() {
    return node;
  }

  public void setNode(int node) {
    this.node = node;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }
}
