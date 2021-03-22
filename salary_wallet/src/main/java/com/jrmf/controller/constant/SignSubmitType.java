package com.jrmf.controller.constant;

/**
 * @author 种路路
 * @create 2019年5月20日21:31:16
 * @desc 签约提交状态
 *
 **/
public enum SignSubmitType {

    /**
     */
    H5(0, "H5"),

    API(1, "API"),

    MIGRATION(2, "迁移签约"),

    BATCH(3, "批次下发共享签约"),

    PLATFORM(4, "平台发起共享签约"),

    IMPORT(5,"后台批量导入签约"),

    WX(6,"微信小程序签约"),

    LINKAGE(7,"商户联动签约"),

    SERVICE_COMPANY(8,"转包服务公司签约"),

    PAYMENT_BEFORE_LINKAGE(9, "支付前联动第三方平台签约");

    private final  int code;
    private final String desc;

    SignSubmitType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static SignSubmitType codeOf(int code) {
        for (SignSubmitType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return SignSubmitType.H5;
    }

}
