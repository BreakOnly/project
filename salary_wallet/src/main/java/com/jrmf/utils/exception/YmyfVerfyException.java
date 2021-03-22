package com.jrmf.utils.exception;

import com.jrmf.utils.RespCode;

public class YmyfVerfyException extends RuntimeException{

    private int state;

    private String respmsg;

    public YmyfVerfyException(String s) {
        super(s);
        state = RespCode.YMYF_VERFY_FAIL;
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
