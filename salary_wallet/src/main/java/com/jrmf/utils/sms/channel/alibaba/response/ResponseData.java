package com.jrmf.utils.sms.channel.alibaba.response;

import java.io.Serializable;

public class ResponseData implements Serializable {

  private static final long serialVersionUID = 1L;

  private String Message;
  private String RequestId;
  private String BizId;
  private String Code;

  public ResponseData() {

  }


  public String getMessage() {
    return Message;
  }

  public void setMessage(String message) {
    Message = message;
  }

  public String getRequestId() {
    return RequestId;
  }

  public void setRequestId(String requestId) {
    RequestId = requestId;
  }

  public String getBizId() {
    return BizId;
  }

  public void setBizId(String bizId) {
    BizId = bizId;
  }

  public String getCode() {
    return Code;
  }

  public void setCode(String code) {
    Code = code;
  }
}
