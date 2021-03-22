package com.jrmf.controller.constant;

/**
 * @author 种路路
 * @create 2018-11-01 21:08
 * @desc 签约状态
 **/
public enum UsersAgreementSignType {

    /**
     * 1-创建
     2-签约处理中
     3-签约待审核
     4-签约失败
     5-签约成功
     6-属于多家商户
     */
    SIGN_CREATE(1,"创建"),

    SIGN_PROCESSING(2,"签约处理中"),

    SIGN_PRE_REVIEW(3,"签约待审核"),

    SIGN_FAIL(4,"签约失败"),

    SIGN_SUCCESS(5,"签约成功"),

    SIGN_CHOOSE_CUSTOM(6,"属于多家商户"),

    SIGN_FORBIDDEN(7,"删除签约成功");

    private final  int code;
    private final String desc;

    UsersAgreementSignType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static UsersAgreementSignType codeOf(int code) {
        for(UsersAgreementSignType usersAgreementSignType : values()) {
            if(usersAgreementSignType.getCode() == code){
                return usersAgreementSignType;
            }
        }
        return SIGN_CREATE;
    }

}
