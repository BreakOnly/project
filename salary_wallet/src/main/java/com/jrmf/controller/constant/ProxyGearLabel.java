package com.jrmf.controller.constant;

/**
 * @Title: ProxyGearLabel
 * @Description: 代理商维护成本 档位金额报税标签 1:小金额 2：大金额
 * @create 2019/11/4 18:16
 */
public enum ProxyGearLabel {
    /**
     * 代理商维护成本 档位金额报税标签 1:小金额 2：大金额
     */
    LITTLE_MONEY(1,"小金额"),

    BIG_MONEY(2,"大金额");

    private final int code;
    private final String desc;

    ProxyGearLabel(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ProxyGearLabel codeOf(int code){
        for (ProxyGearLabel type : values()) {
            if(code == type.code){
                return type;
            }
        }
        return null;
    }

    public static ProxyGearLabel codeOfDefault(int code){
        for (ProxyGearLabel type : values()) {
            if(code == type.code){
                return type;
            }
        }
        return LITTLE_MONEY;
    }
}
