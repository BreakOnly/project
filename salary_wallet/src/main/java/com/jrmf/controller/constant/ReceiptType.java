package com.jrmf.controller.constant;

/**
 * 描 述: 回单处理方式<br/>
 * 创 建：2019年10⽉11⽇<br/>
 * 版 本：v1.0.0<br>
 */
public enum ReceiptType {

    /**
     * qb_receipt_batch 回单处理方式
     */
    BANK(1,"手工处理"),

    PARTFAIL(0,"自动处理");

    private final int code;
    private final String desc;

    ReceiptType(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ReceiptType codeOf (int code) {
        for (ReceiptType receiptType : values()) {
            if (receiptType.getCode() == code) {
                return receiptType;
            }
        }
        return null;
    }
}
