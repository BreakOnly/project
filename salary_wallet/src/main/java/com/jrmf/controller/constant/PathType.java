package com.jrmf.controller.constant;

public enum PathType {

    BANKPAY(0, "银行银企直联"),

    SELPAY(1, "三方资金存管");


    private final int code;
    private final String desc;

    PathType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static PathType codeOf(int code) {
        for (PathType linkageType : values()) {
            if (linkageType.getCode() == code) {
                return linkageType;
            }
        }
        return BANKPAY;
    }

}
