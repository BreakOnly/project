package com.jrmf.controller.constant;

public enum ConfirmStatus {

    SUCCESS(1, "已核销"),

    FAILURE(0, "未核销"),

    Paying(20, "支付中"),

    PaySuccess(30, "支付成功"),

    PayFail(40, "支付失败");

    private final int code;
    private final String desc;

    ConfirmStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ConfirmStatus codeOf(int code) {
        for (ConfirmStatus commissionStatus : values()) {
            if (commissionStatus.getCode() == code) {
                return commissionStatus;
            }
        }
        return SUCCESS;
    }
}
