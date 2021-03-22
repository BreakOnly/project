package com.jrmf.controller.constant;

public enum LinkageTranStatus {

    UNKNOW(0, "未知"),

    SUCCESS(1, "成功"),

    FAILURE(2, "失败"),

    PAYING(3, "处理中");


    private final int code;
    private final String desc;

    LinkageTranStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static LinkageTranStatus codeOf(int code) {
        for (LinkageTranStatus linkageType : values()) {
            if (linkageType.getCode() == code) {
                return linkageType;
            }
        }
        return UNKNOW;
    }

}
