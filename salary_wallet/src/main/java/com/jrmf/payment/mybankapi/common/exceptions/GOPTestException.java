package com.jrmf.payment.mybankapi.common.exceptions;


public class GOPTestException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private String memo;

  public GOPTestException(Throwable e) {
    this(ReturnCode.SYSTEM_ERROR, e);
  }

  public GOPTestException(ReturnCode returnCode) {
    super(returnCode.getMessage());
    this.returnCode = returnCode;
  }

  public GOPTestException(ReturnCode returnCode, String message) {
    super(message, null);
    this.returnCode = returnCode;
  }

  public GOPTestException(ReturnCode returnCode, Throwable e) {
    super(returnCode.getMessage(), e);
    this.returnCode = returnCode;
  }

  public GOPTestException(ReturnCode returnCode, String message, Throwable e) {
    super(message, e);
    this.returnCode = returnCode;
  }

  final ReturnCode returnCode;

  public ReturnCode getReturnCode() {
    return returnCode;
  }

  public String getMemo() {
    return memo;
  }

  public void setMemo(String memo) {
    this.memo = memo;
  }

}
