package com.jrmf.controller.constant;

/**
 * @author 种路路
 * @create 2018-11-01 21:08
 * //
 **/
public enum SignStep {

    /**
     * 0-创建
     1-签约协议模板成功
     2-签约协议模板失败
     */
    SIGN_CREATE(0,"创建"),

    SIGN_SUCCESS(1,"签约步骤成功"),

    SIGN_FAIL(2,"签约步骤失败");

    private final  int code;
    private final String desc;

    SignStep(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static SignStep codeOf(int code) {
        for(SignStep certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return SIGN_CREATE;
    }

    public static SignStep codeOfDefault(int code) {
        for(SignStep certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return null;
    }
    public static SignStep descOfDefault(String desc) {
        for(SignStep certType : values()) {
            if(desc.equals(certType.getDesc())){
                return certType;
            }
        }
        return null;
    }
}
