package com.jrmf.controller.constant;

public enum FundModelType {

    NONE(0, "无充值联动"),

    RECHARGE(1, "充值联动"),

    PAY(2, "下发联动"),

    RECHARGEPAY(3, "充值转账和下发过账联动");

    private final int code;
    private final String desc;

    FundModelType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static FundModelType codeOf(int code) {
        for (FundModelType confirmType : values()) {
            if (confirmType.getCode() == code) {
                return confirmType;
            }
        }
        return NONE;
    }

}
