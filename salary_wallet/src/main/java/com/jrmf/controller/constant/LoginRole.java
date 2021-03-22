package com.jrmf.controller.constant;

/**
 * @author 种路路
 * @create 2018年12月27日09:56:01
 * @desc 商户登录角色
 * //
 **/
public enum LoginRole {

    /**
     * 1.管理员账号 2.操作员账号
     */
    ADMIN_ACCOUNT(1,"管理员"),

    UNKNOW(-1,"未知"),

    OPERATE_ACCOUNT(2,"操作员");

    private final  int code;
    private final String desc;

    LoginRole(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static LoginRole codeOf(int code) {
        for(LoginRole loginRole : values()) {
            if(loginRole.getCode() == code){
                return loginRole;
            }
        }
        return ADMIN_ACCOUNT;
    }

    public static LoginRole codeOfDefault(int code) {
        for(LoginRole loginRole : values()) {
            if(loginRole.getCode() == code){
                return loginRole;
            }
        }
        return null;
    }
    public static LoginRole descOfDefault(String desc) {
        for(LoginRole loginRole : values()) {
            if(desc.equals(loginRole.getDesc())){
                return loginRole;
            }
        }
        return UNKNOW;
    }
}
