package com.jrmf.controller.constant;

/**
 * 描 述: 回单处理状态<br/>
 * 创 建：2019年10⽉11⽇<br/>
 * 版 本：v1.0.0<br>
 */
public enum ReceiptStatus {

    ALLFAIL(4,"全部失败"),

    PARTFAIL(3,"部分失败"),

    PANDING(0,"待处理"),

    INHAND(2,"处理中"),

    SUCCESS(1,"成功");


    private final int code;
    private final String desc;

    ReceiptStatus(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ReceiptStatus codeOf (int code) {
        for (ReceiptStatus receiptStatus : values()) {
            if (receiptStatus.getCode() == code) {
                return receiptStatus;
            }
        }
        return null;
    }
}
