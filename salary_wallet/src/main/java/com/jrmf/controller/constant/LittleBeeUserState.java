package com.jrmf.controller.constant;

public enum LittleBeeUserState {

    REGISTER(1, "注册成功"),

    REALNAME(0, "实名认证成功");

    private final int code;
    private final String desc;

    LittleBeeUserState(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static LittleBeeUserState codeOf(int code) {
        for (LittleBeeUserState commissionStatus : values()) {
            if (commissionStatus.getCode() == code) {
                return commissionStatus;
            }
        }
        return REALNAME;
    }
}
