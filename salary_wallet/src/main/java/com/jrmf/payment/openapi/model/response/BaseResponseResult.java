package com.jrmf.payment.openapi.model.response;

/**
 * Created by ThinkPad on 2017/6/13.
 */
public class BaseResponseResult<T> {
    private String code;
    private String msg;
    private String sign;
    private T data;

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

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
