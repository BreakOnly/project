package com.jrmf.common;

public class RespResult<T> {

  private String code = "00000";

  private String msg = "成功";

  private T data;

  // 几个常用方法
  public RespResult<T> success() {
    return new RespResult<T>();
  }

  public RespResult<T> success(T data) {
    return new RespResult<T>(data);
  }

  public RespResult<T> error(String code, String message) {
    return new RespResult<T>(code, message);
  }

  public RespResult<T> error(String code, String message, T data) {
    return new RespResult<T>(code, message, data);
  }

  // 几个构造方法
  public RespResult() {
  }

  public RespResult(T data) {
    this.data = data;
  }

  public RespResult(String code, String message) {
    this.code = code;
    this.msg = message;
  }

  public RespResult(String code, String message, T data) {
    this.code = code;
    this.msg = message;
    this.data = data;
  }

  public boolean isSuccess() {
    return "00000".endsWith(code);
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public Object getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}