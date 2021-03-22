package com.jrmf.controller.constant;

/**
 * 描 述:回单机构凭证类型 <br/>
 * 创 建：2019年10⽉11⽇<br/>
 * 版 本：v1.0.0<br>
 */
public enum ReceiptOrganizationType {
    /**
     * qb_receipt_batch 回单机构凭证类型
     */
    BANK(1,"银行"),

    PARTFAIL(3,"第三方支付机构");

    private final int code;
    private final String desc;

    ReceiptOrganizationType(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ReceiptOrganizationType codeOf (int code) {
        for (ReceiptOrganizationType receiptOrganizationType : values()) {
            if (receiptOrganizationType.getCode() == code) {
                return receiptOrganizationType;
            }
        }
        return null;
    }
}
