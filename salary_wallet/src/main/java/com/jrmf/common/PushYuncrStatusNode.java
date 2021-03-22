package com.jrmf.common;

/**
 * @author: YJY
 * @date: 2021/1/08
 * @description:
 */
public enum PushYuncrStatusNode {


  SUCCESS("1","成功"),
  IN_HAND("2","处理中"),
  FAIL("3","失败")
  ;
  String node;
  String msg;

  PushYuncrStatusNode(String node, String msg) {
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
