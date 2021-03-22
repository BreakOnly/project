package com.jrmf.common;

/**
 * @author: YJY
 * @date: 2020/12/15 11:24
 * @description:
 */
public enum YuncrFailNode {

  ID_FRONT( "身份证正面文件节点失败","id_front"),
  ID_BACK("身份证反面文件节点失败","id_back" ),
  ID_AUTH("实名认证失败","id_auth" ),
  VEDIO("开户视频文件节点失败","vedio"),
  HAND_WRITTEN("手写签名文件节点失败","hand_written" ),
  OTHER("其他","other" );

  private String failMsg;
  private String type;

  public static String getByType(String type) {
    for (YuncrFailNode failNode : YuncrFailNode.values()) {
      if (failNode.getType().equals(type)) {
        return failNode.getFailMsg();
      }
    }
    return null;
  }

  YuncrFailNode(String failMsg, String type) {
    this.failMsg = failMsg;
    this.type = type;
  }

  public String getFailMsg() {
    return failMsg;
  }

  public String getType() {
    return type;
  }
}
