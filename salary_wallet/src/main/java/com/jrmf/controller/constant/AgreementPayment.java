package com.jrmf.controller.constant;

/**
 * @author 种路路
 * @create 2018-11-01 21:08
 * @desc  /**
 *协议限制
 */
public enum AgreementPayment {

    /**
     1-先签约后支付
     2-先支付后签约
     3-不限制
     */
    SIGN_FIRST(1,"先签约后支付"),

    PAY_FIRST(2,"调用第三方接口静默签约"),

    WHATEVER(3,"随意");


    private final  int code;
    private final String desc;

    AgreementPayment(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static void main(String[] args) {
        System.out.println(SIGN_FIRST.code);

    }

}
