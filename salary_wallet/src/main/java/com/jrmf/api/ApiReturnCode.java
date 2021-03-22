package com.jrmf.api;

public enum ApiReturnCode {
    /**
     *
     **/
    SUCCESS("000000","交易成功"),

    FAILURE("000001","交易失败"),

    DEDUCTION_FAILURE("000002","扣款失败"),

    SUBMIT_EXCEPTION("000004","上送异常"),

    NOT_SUCH_ORDER("000003","无该笔订单"),

    REAL_COMPANY_ACCOUNT_INVALID("000004","系统内记账户为失效请联系客户经理");




    private String code;
    private String desc;

    ApiReturnCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static ApiReturnCode codeOf(String code) {
        for(ApiReturnCode apiReturnCode : values()) {
            if(apiReturnCode.getCode() == code){
                return apiReturnCode;
            }
        }
        return null;
    }
}
