package com.jrmf.utils.exception;

import com.jrmf.utils.RespCode;

/**
 * @author 种路路
 * @create 2018-11-16 18:03
 * @desc
 **/
public class SessionDestroyedException extends RuntimeException {
    private int state;

    private String respmsg;

    public SessionDestroyedException(String s) {
        super(s);
        state = RespCode.SESSION_DESTROYED;
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
