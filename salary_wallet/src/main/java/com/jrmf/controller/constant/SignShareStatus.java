package com.jrmf.controller.constant;


public enum SignShareStatus {

    SIGN_SUCCESS(1, "用户已签约"),

    SIGN_SHARE_SUCCESS(2, "用户尝试签约共享成功"),

    SIGN_SHARE_FAIL(3, "用户尝试签约共享失败"),

    SIGN_FAIL(4, "签约失败"),

    NO_SIGN_PAY(5, "非先签约后支付,跳过签约校验");


    private final int code;
    private final String desc;

    SignShareStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static SignShareStatus codeOf(int code) {
        for (SignShareStatus type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }
}
