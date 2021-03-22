package com.jrmf.controller.constant;

/**
 * 充值类型
 *
 * @author linsong
 * @date 2019/7/24
 */
public enum RechargeType {

    AMOUNT(1, "余额充值"),

    SERVICEAMOUNT(2, "补服务费");

    private final int code;
    private final String desc;

    RechargeType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RechargeType codeOf(int code) {
        for (RechargeType rechargeStatusType : values()) {
            if (rechargeStatusType.getCode() == code) {
                return rechargeStatusType;
            }
        }
        return AMOUNT;
    }
    public static RechargeType codeOfEnum(int code) {
        for (RechargeType rechargeStatusType : values()) {
            if (rechargeStatusType.getCode() == code) {
                return rechargeStatusType;
            }
        }
        return null;
    }
}
