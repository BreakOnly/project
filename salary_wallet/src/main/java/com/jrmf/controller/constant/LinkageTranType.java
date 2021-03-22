package com.jrmf.controller.constant;

public enum LinkageTranType {

    MAINACCOUNT(0, "商户充值转账"),

    SUBACCOUNT(1, "商户子账户充值转账"),

    PAYACCOUNT(2, "下发联动过账");


    private final int code;
    private final String desc;

    LinkageTranType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static LinkageTranType codeOf(int code) {
        for (LinkageTranType linkageType : values()) {
            if (linkageType.getCode() == code) {
                return linkageType;
            }
        }
        return MAINACCOUNT;
    }

}
