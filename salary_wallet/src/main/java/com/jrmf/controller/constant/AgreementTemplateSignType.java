package com.jrmf.controller.constant;

/**
 * @author 种路路
 * @create 2018-11-01 21:08
 * @desc 签约状态
 **/
public enum AgreementTemplateSignType {

    /**
     * 1-本地人工审核签约
     2-调用第三方接口静默签约
     */
    LOCAL_SIGN(1,"本地人工审核签约"),

    THIRD_SIGN(2,"调用第三方接口静默签约");


    private final  int code;
    private final String desc;

    AgreementTemplateSignType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
