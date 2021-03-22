package com.jrmf.utils.exception;

import com.jrmf.utils.RespCode;

/**
 * @Title: ChannelRouteException
 * @Description:
 * @create 2020/4/14 17:20
 */
public class ChannelRouteException extends RuntimeException{
    private int state;

    private String respmsg;

    public ChannelRouteException(String s) {
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
