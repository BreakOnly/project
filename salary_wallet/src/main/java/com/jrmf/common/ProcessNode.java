package com.jrmf.common;

/**
 * @author: YJY
 * @date: 2020/10/9
 * @description: 用户提交数据流程
 */
public enum ProcessNode {

  IDENTITY_AUTH_FRONT(1,"身份证正面认证"),
  IDENTITY_AUTH_REVERSE(2,"身份证反面认证"),
  SFC(3,"实名认证"),
  VIDEO_AUTH(4,"活体认证"),
  BUSINESS_LICENSE(5,"提交签名并注册"),
  ENTERPRISE(6,"企业审核"),
  ALL_SUCCESS(7,"企业审核全部完成!等待政府审核");
   int processNode;
   String msg;

  ProcessNode(int processNode, String msg) {
    this.processNode = processNode;
    this.msg = msg;
  }

  public int getProcessNode() {
    return processNode;
  }

  public String getMsg() {
    return msg;
  }
}
