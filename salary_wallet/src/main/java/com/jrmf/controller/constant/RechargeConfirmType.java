package com.jrmf.controller.constant;

public enum RechargeConfirmType {

    AUTO(1, "自动确认"),

    MANUAL(2, "人工确认");

    private final int code;
    private final String desc;

    RechargeConfirmType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RechargeConfirmType codeOf(int code) {
        for (RechargeConfirmType confirmType : values()) {
            if (confirmType.getCode() == code) {
                return confirmType;
            }
        }
        return MANUAL;
    }

}
