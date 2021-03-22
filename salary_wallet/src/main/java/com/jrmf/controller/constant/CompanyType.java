package com.jrmf.controller.constant;


public enum CompanyType {


    ACTUAL(0, "实际下发"),

    SUBCONTRACT(1, "转包下发"),

    ALIAS(2, "别名下发");

    private final int code;
    private final String desc;

    CompanyType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static CompanyType codeOf(int code) {
        for (CompanyType certType : values()) {
            if (certType.getCode() == code) {
                return certType;
            }
        }
        return ACTUAL;
    }
}
