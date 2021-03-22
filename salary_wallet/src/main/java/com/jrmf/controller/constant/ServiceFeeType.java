package com.jrmf.controller.constant;

/**
 * 手续费收取方式
 *
 * @author linsong
 * @date 2019/7/24
 */
public enum ServiceFeeType {

    RECHARGE(1, "充值预扣收"),

    ISSUE(2, "下发实时扣收"),

    PERSON(3, "下发实时扣收个人承担");

    private final  int code;
    private final String desc;

    ServiceFeeType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static ServiceFeeType codeOf(int code) {
        for(ServiceFeeType transferType : values()) {
            if(transferType.getCode() == code){
                return transferType;
            }
        }
        return ISSUE;
    }
    public static ServiceFeeType codeOfEnum(int code) {
        for(ServiceFeeType transferType : values()) {
            if(transferType.getCode() == code){
                return transferType;
            }
        }
        return null;
    }
}
