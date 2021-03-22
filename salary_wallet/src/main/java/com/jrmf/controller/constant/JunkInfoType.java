package com.jrmf.controller.constant;

public enum JunkInfoType {

    RATE(1, "初始化档位信息无档位的信息");

    private final int code;
    private final String desc;

    JunkInfoType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static JunkInfoType codeOf(int code) {
        for (JunkInfoType custom : values()) {
            if (custom.getCode() == code) {
                return custom;
            }
        }
        return null;
    }

}
