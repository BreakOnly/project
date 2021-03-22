package com.jrmf.payment.mybankapi.common.enums;

public enum ResultStatusCode {
  OK("1", "成功"), PERMISSION_DENIED("2", "失败");
  private String errCode;
  private String msg;

  ResultStatusCode(String errCode, String msg) {
    this.setErrCode(errCode);
    this.setMsg(msg);
  }

  public String getErrCode() {
    return errCode;
  }

  public void setErrCode(String errCode) {
    this.errCode = errCode;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

}
