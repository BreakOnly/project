package com.jrmf.controller.constant;

public enum IsSubAccount {

    NO(0, "非子账号"),

    YES(1, "是子账号");


    private final int code;
    private final String desc;

    IsSubAccount(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static IsSubAccount codeOf(int code) {
        for (IsSubAccount linkageType : values()) {
            if (linkageType.getCode() == code) {
                return linkageType;
            }
        }
        return NO;
    }

}
