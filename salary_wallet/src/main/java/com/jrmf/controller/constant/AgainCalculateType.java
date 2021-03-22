package com.jrmf.controller.constant;

/**
 * @author 种路路
 * @create 2019年11月1日16:05:00
 * @desc 计算规则 或者 重新计算规则
 * //
 **/
public enum AgainCalculateType {

    /**
     * 1.全部、2.服务公司、3.代理商、4.商户
     */
    ALL(1,"全部",""),

    COMPANY(2,"服务公司","companyId"),

    CUSTOM_PROXY(3,"代理商","proxyCustomKey"),

    CUSTOM(4,"商户","customKey");

    private final  int code;
    private final String desc;
    private final String customType;

    AgainCalculateType(int code, String desc, String customType) {
        this.code = code;
        this.desc = desc;
        this.customType = customType;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getCustomType() {
        return customType;
    }


    public static AgainCalculateType codeOf(int code) {
        for(AgainCalculateType type : values()) {
            if(type.getCode() == code){
                return type;
            }
        }
        return ALL;
    }

    public static AgainCalculateType codeOfDefault(int code) {
        for(AgainCalculateType type : values()) {
            if(type.getCode() == code){
                return type;
            }
        }
        return null;
    }
    public static AgainCalculateType descOfDefault(String desc) {
        for(AgainCalculateType certType : values()) {
            if(desc.equals(certType.getDesc())){
                return certType;
            }
        }
        return null;
    }
}
