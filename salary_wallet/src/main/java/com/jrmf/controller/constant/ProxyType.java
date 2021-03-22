package com.jrmf.controller.constant;

/**
 * @author chonglulu
 * @time: 2019年11月4日09:37:24
 * 代理商户类型
 */
public enum ProxyType {
    /**
     * 代理商户类型   1,直接商户  2.间接商户
     */
    DIRECT_CUSTOM(1,"直接商户"),

    INDIRECT_CUSTOM(2,"直接商户");

    private final int code;
    private final String desc;

    ProxyType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ProxyType codeOf(int code){
        for (ProxyType type : values()) {
            if(code == type.code){
                return type;
            }
        }
        return null;
    }

    public static ProxyType codeOfDefault(int code){
        for (ProxyType type : values()) {
            if(code == type.code){
                return type;
            }
        }
        return DIRECT_CUSTOM;
    }
}
