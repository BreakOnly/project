package com.jrmf.controller.constant;

/**
 * 商户余额历史交易状态
 *
 * @author linsong
 * @date 2019/4/8
 */
public enum TradeType {

    RECHARGE(1, "充值"),

    WEBPAYMENT(2, "web下发"),

    ADDBALANCE(3, "增额调账"),

    SUBBALANCE(4, "减额调账"),

    RECHARGEREFUND(5, "充值流水退款"),

    SERVICEFEE(6, "下发服务费扣收"),

    RECHARGESERVICEFEE(7, "充值服务费扣收"),

    RECHARGEINTO(8, "充值调账入金可用余额"),

    YXYSUBBALANCE(9, "营销云确认报告扣款"),

    APIPAYMENT(10, "api下发"),

    PAYMENTREFUND(11, "下发失败退款"),

    SWITCH_IN(12,"切入完税公司"),

    SWITCH_OUT(13,"切出完税公司");

    private final int code;
    private final String desc;

    TradeType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static TradeType codeOf(int code) {
        for (TradeType transferType : values()) {
            if (transferType.getCode() == code) {
                return transferType;
            }
        }
        return null;
    }
}
