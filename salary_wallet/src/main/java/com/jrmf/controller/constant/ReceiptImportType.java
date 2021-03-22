package com.jrmf.controller.constant;


public enum ReceiptImportType {


    PINGANBANKONE(1, "平安银行单页单张"),

    PINGANBANKTHREE(2, "平安银行单页三张"),

    MYBANKONE(3, "网商银行单页单张");

    private final int code;
    private final String desc;

    ReceiptImportType(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ReceiptImportType codeOf (int code) {
        for (ReceiptImportType receiptType : values()) {
            if (receiptType.getCode() == code) {
                return receiptType;
            }
        }
        return null;
    }
}
