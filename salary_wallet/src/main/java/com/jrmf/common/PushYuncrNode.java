package com.jrmf.common;

/**
 * @author: YJY
 * @date: 2021/1/08
 * @description:
 */
public enum PushYuncrNode {

  PUSH_CONTRACT(1,"推送合同"),
  PUSH_SETTLEMENT(2,"推送结算"),
  PUSH_FINAL_STATEMENT(3,"推送结算单")
  ;
  int node;
  String msg;

  PushYuncrNode(int node, String msg) {
    this.node = node;
    this.msg = msg;
  }

  public int getNode() {
    return node;
  }

  public String getMsg() {
    return msg;
  }
}
