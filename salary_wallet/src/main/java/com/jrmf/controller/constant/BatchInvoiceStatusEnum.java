package com.jrmf.controller.constant;

public enum BatchInvoiceStatusEnum {
    FINISH(1, "已开票"),
    PROCESSING(2, "开票中"),
    FAIL(3, "开票失败"),
    ;

    private final int code;
    private final String desc;

    BatchInvoiceStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
