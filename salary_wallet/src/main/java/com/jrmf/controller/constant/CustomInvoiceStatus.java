package com.jrmf.controller.constant;

public enum CustomInvoiceStatus {
    /**
     * 1,"启用"  -1,"已删除"
     **/
    IN_USE(1, "启用"),

    UNKNOWN(0, "未知"),

    DELETED(-1, "已删除");

    private final int code;
    private final String desc;

    CustomInvoiceStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CustomInvoiceStatus codeOf(int code) {
        for (CustomInvoiceStatus customInvoiceStatus : values()) {
            if (customInvoiceStatus.getCode() == code) {
                return customInvoiceStatus;
            }
        }
        return UNKNOWN;
    }
}
