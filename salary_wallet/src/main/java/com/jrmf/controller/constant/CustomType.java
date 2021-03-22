package com.jrmf.controller.constant;

/**
 * @author 种路路
 * @create 2018-11-01 21:08
 * @desc 身份证类型
 * //
 **/
public enum CustomType {

    /**
     * 1 商户 2 服务公司 3 代理商 4 管理员 5 集团型商户
     */
    CUSTOM(1, "商户"),

    COMPANY(2, "服务公司"),

    PROXY(3, "代理商"),

    ROOT(4, "机构登陆账户"),

    GROUP(5, "集团型商户"),

    PROXYCHILDEN(6, "关联性代理商"),

    PLATFORM(7, "平台");

    private final int code;
    private final String desc;

    CustomType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CustomType codeOf(int code) {
        for (CustomType custom : values()) {
            if (custom.getCode() == code) {
                return custom;
            }
        }
        return null;
    }

}
