package com.jrmf.controller.constant;

/**
 * Author Nicholas-Ning
 * Description //TODO 批次表状态
 * Date 11:25 2018/11/19
 * Param
 * return
 **/
public enum RechargeStatusType {
    /**
     * 1,"成功"  2,"失败" 4,"驳回"
     **/
    SUCCESS(1, "成功"),

    CONFIRMING(0, "待确认"),

    UNKNOWN(-1, "未知"),

    FAILURE(2, "驳回"),

    REFUND(3, "已退款完成"),

    NORECHARGE(8, "未转账"),

    RECHARGEING(9, "充值转账中"),

    RECHARGESUCCESS(10, "充值转账成功"),

    RECHARGEFAILURE(11, "充值转账失败");


    private final int code;
    private final String desc;

    RechargeStatusType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RechargeStatusType codeOf(int code) {
        for (RechargeStatusType rechargeStatusType : values()) {
            if (rechargeStatusType.getCode() == code) {
                return rechargeStatusType;
            }
        }
        return UNKNOWN;
    }
}
