package com.jrmf.controller.constant;

/**
 * @Title: ProxyCountType
 * @Description: 代理商维护成本 0:本级商户和下级代理差额成本统计 1：本级代理商直接成本统计'
 * @create 2019/11/4 15:21
 */
public enum ProxyCountType {
    /**
     * 代理商维护成本 代理商统计范围 0:本级商户 1：本级商户和下级代理商户
     */
    SAMEANDLOWER_CUSTOM_SPREAD(0,"本级商户和下级代理差额成本统计"),

    SAME_CUSTOM_SPREAD(1,"本级代理商直接成本统计");

    private final int code;
    private final String desc;

    ProxyCountType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ProxyCountType codeOf(int code){
        for (ProxyCountType type : values()) {
            if(code == type.code){
                return type;
            }
        }
        return null;
    }

    public static ProxyCountType codeOfDefault(int code){
        for (ProxyCountType type : values()) {
            if(code == type.code){
                return type;
            }
        }
        return SAME_CUSTOM_SPREAD;
    }
}
