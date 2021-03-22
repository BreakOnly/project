package com.jrmf.controller.constant;

/**
 * @Title: ProxyProxyType
 * @Description:  代理商维护成本 代理商统计范围 0:本级商户 1：本级商户和下级代理商户
 * @create 2019/11/4 14:39
 */
public enum ProxyProxyType {
    /**
     * 代理商维护成本 代理商统计范围 0:本级商户 1：本级商户和下级代理商户
     */
    SAME_CUSTOM(0,"本级商户"),

    SAMEANDLOWER_CUSTOM(1,"本级商户和下级代理商户");

    private final int code;
    private final String desc;

    ProxyProxyType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ProxyProxyType codeOf(int code){
        for (ProxyProxyType type : values()) {
            if(code == type.code){
                return type;
            }
        }
        return null;
    }

    public static ProxyProxyType codeOfDefault(int code){
        for (ProxyProxyType type : values()) {
            if(code == type.code){
                return type;
            }
        }
        return SAME_CUSTOM;
    }
}
