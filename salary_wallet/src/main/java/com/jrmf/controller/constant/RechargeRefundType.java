package com.jrmf.controller.constant;

public enum RechargeRefundType {

    UNKNOWN(-1, "未知"),

    ALL(1, "全部退款"),

    PART(2, "部分退款");

    private final int code;
    private final String desc;

    RechargeRefundType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RechargeRefundType codeOf(int code) {
        for (RechargeRefundType confirmType : values()) {
            if (confirmType.getCode() == code) {
                return confirmType;
            }
        }
        return UNKNOWN;
    }

}
