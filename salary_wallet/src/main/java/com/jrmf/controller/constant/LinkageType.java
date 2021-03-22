package com.jrmf.controller.constant;

public enum LinkageType {

    RECHARGENO(0, "充值联动"),

    PAY(1, "下发联动");


    private final int code;
    private final String desc;

    LinkageType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static LinkageType codeOf(int code) {
        for (LinkageType linkageType : values()) {
            if (linkageType.getCode() == code) {
                return linkageType;
            }
        }
        return RECHARGENO;
    }

}
