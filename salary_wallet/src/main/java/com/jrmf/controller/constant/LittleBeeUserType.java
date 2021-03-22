package com.jrmf.controller.constant;

public enum LittleBeeUserType {

    REGISTER(1, "主动注册"),

    INPUT(2, "商户推荐");

    private final int code;
    private final String desc;

    LittleBeeUserType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static LittleBeeUserType codeOf(int code) {
        for (LittleBeeUserType commissionStatus : values()) {
            if (commissionStatus.getCode() == code) {
                return commissionStatus;
            }
        }
        return REGISTER;
    }
}
