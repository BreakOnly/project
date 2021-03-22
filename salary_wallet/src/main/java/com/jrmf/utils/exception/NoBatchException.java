package com.jrmf.utils.exception;

import com.jrmf.utils.RespCode;

public class NoBatchException extends RuntimeException{

    private int state;

    private String respmsg;

    public NoBatchException(String s) {
        super(s);
        state = RespCode.YMYF_NO_BATCH_EXCEPTION;
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
