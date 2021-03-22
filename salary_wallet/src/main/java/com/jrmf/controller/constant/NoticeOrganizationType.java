package com.jrmf.controller.constant;

/**
 * @Title: NoticeOrganizationType
 * @Description: 公告通知范围
 * @create 2020/1/16 14:20
 */
public enum NoticeOrganizationType {
    /**
     * 通知范围 1：商户 2：下发公司 3：代理商 4：账户管理员 5：集团型商户 6：自定义
     */
    CUSTOM(1,"商户"),

    COMPANY(2,"下发公司"),

    PROXY(3,"代理商"),

    ROOT(4,"机构登陆账户"),

    GROUP(5,"集团型商户"),

    RESTS(6,"自定义");

    private final int code;
    private final String desc;

    NoticeOrganizationType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static NoticeOrganizationType codeOf(int code){
        for (NoticeOrganizationType type : values()) {
            if(code == type.code){
                return type;
            }
        }
        return null;
    }

    public static NoticeOrganizationType codeOfDefault(int code){
        for (NoticeOrganizationType type : values()) {
            if(code == type.code){
                return type;
            }
        }
        return RESTS;
    }
}
