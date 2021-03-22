package com.jrmf.controller.constant;

/**
 * 商户余额历史交易状态
 *
 * @author linsong
 * @date 2019/4/8
 */
public enum CustomTransferRecordType {

    UNDERFINED(0, "", "未知类型"),

    SUBACCOUNTINTO(1, "C", "子账号入金"),

    SUBACCOUNTOUT(2, "D", "子账户出金"),

    ADJUSTMENTOUT(3, "D", "存管账户余额调账出金"),

    ADJUSTMENTINTO(4, "C", "存管账户余额调账入金");


    private final int code;
    private final String flag;
    private final String desc;

    CustomTransferRecordType(int code, String flag, String desc) {
        this.code = code;
        this.flag = flag;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getFlag() {
        return flag;
    }

    public static CustomTransferRecordType codeOf(int code) {
        for (CustomTransferRecordType transferType : values()) {
            if (transferType.getCode() == code) {
                return transferType;
            }
        }
        return UNDERFINED;
    }

    public static CustomTransferRecordType codeOfFlag(String flag) {
        for (CustomTransferRecordType transferType : values()) {
            if (transferType.getCode() < ADJUSTMENTOUT.getCode() && transferType.getFlag().equals(flag)) {
                return transferType;
            }
        }
        return UNDERFINED;
    }
}
