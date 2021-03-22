package com.jrmf.controller.constant;

public enum CustomInvoiceIsDefault {
    /**
     * 1,"默认"  -1,"非默认"
     **/
    IS_DEFAULT(1, "默认"),


    NOT_DEFAULT(-1, "非默认");

    private final int code;
    private final String desc;

    CustomInvoiceIsDefault(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CustomInvoiceIsDefault codeOf(int code) {
        for (CustomInvoiceIsDefault customInvoiceisDefault : values()) {
            if (customInvoiceisDefault.getCode() == code) {
                return customInvoiceisDefault;
            }
        }
        return NOT_DEFAULT;
    }
}
