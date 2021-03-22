package com.jrmf.controller.constant;

/**
 *  批次表获取验证码状态
 */
public enum BatchLockStatus {
    /**
     * 0未获取验证码 1获取验证码中 2获取验证码失败 3获取验证码成功 4确认验证码处理中 5确认验证码失败 6确认验证码成功 7获取验证码异常 8确认验证码异常
     **/
    NOTGET(0, "未获取验证码"),

    GETTING(1, "获取验证码中"),

    GETFAILURE(2, "获取验证码失败"),

    GETSUCCESS(3, "获取验证码成功"),

    CONFIRMING(4, "确认验证码处理中"),

    CONFIRMFAILURE(5, "确认验证码失败"),

    CONFIRMSUCCESS(6, "确认验证码成功"),

    GETERROR(7, "获取验证码异常"),

    CONFIRMERROR(8, "确认验证码异常"),

    CONFIRMUNKNOWNERROR(9, "确认验证码未知系统异常");

    private final  int code;
    private final String desc;

    BatchLockStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static BatchLockStatus codeOf(int code) {
        for(BatchLockStatus logckStatus : values()) {
            if(logckStatus.getCode() == code){
                return logckStatus;
            }
        }
        return null;
    }
}
