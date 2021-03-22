package com.jrmf.controller.constant;

public enum BatchInvoiceStepEnum {
    PUSH_CONTRACT(1, "推送合同"),
    PUSH_SETTLEMENT(2, "推送结算"),
    PUSH_FINAL_STATEMENT(3, "上传结算单"),
    PUSH_INVOICE(4, "上传结算发票")
    ;

    private final int code;
    private final String desc;

    BatchInvoiceStepEnum(int code, String desc) {
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
