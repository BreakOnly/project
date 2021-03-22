package com.jrmf.utils.exception;

import com.jrmf.utils.RespCode;

/**
 * @author 种路路
 * @create 2018-11-16 15:48
 * @desc 登录异常错误
 **/
public class LoginException extends RuntimeException {
    private int state;

    private String respmsg;

    public LoginException(String s) {
        super(s);
        state = RespCode.error000;
        respmsg = s;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getRespmsg() {
        return respmsg;
    }

    public void setRespmsg(String respmsg) {
        this.respmsg = respmsg;
    }
}
