package com.jrmf.controller.constant;

public enum BatchInvoiceStepStatusEnum {
    SUCCESS("1", "成功"),
    PROCESSING("2", "处理中"),
    FAIL("3", "失败"),
    ;

    private final String code;
    private final String desc;

    BatchInvoiceStepStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
