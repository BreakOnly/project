package com.jrmf.controller.constant;

/**
 * @description: <br/>
 * @author: <br/>
 * @create：2020年04⽉30⽇<br/>
 */
public enum SignShareType {

    COMPANYTYPE(1, "服务公司共享"),

    CUSTOMTYPE(2, "商户共享"),

    CUSTOMONETOMANY(3, "商户一对多共享");



    private final int code;
    private final String desc;

    SignShareType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static SignShareType codeOf(int code) {
        for (SignShareType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

    public static SignShareType codeOfEnum(int code) {
        for (SignShareType type : values()) {
            if(type.getCode() == code){
                return type;
            }
        }
        return COMPANYTYPE;
    }
}
