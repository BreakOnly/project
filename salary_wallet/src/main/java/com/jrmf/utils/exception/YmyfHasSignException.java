package com.jrmf.utils.exception;

import com.jrmf.utils.RespCode;

public class YmyfHasSignException extends RuntimeException{

    private int state;

    private String respmsg;

    public YmyfHasSignException(String s) {
        super(s);
        state = RespCode.YMYF_NORMAL_EXCEPTION;
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
