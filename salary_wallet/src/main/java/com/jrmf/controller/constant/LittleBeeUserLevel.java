package com.jrmf.controller.constant;

public enum LittleBeeUserLevel {

    LEVELONE(1, "level-1"),

    LEVELTWO(2, "level-2"),

    LEVELTHREE(3, "level-3");

    private final int code;
    private final String desc;

    LittleBeeUserLevel(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static LittleBeeUserLevel codeOf(int code) {
        for (LittleBeeUserLevel commissionStatus : values()) {
            if (commissionStatus.getCode() == code) {
                return commissionStatus;
            }
        }
        return LEVELONE;
    }
}
