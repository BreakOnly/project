package com.jrmf.controller.constant;

public enum EmailSendStatus {
    /**
     * 1 成功  2 失败  3 处理中 4 带处理
     */
    SUCCESS(1,"hs","成功"),

    FAILURE(2,"alipay","失败"),

    RUNNING(3,"wechat","处理中"),

    WAITING(0,"bankcard","带处理");

    private final  int code;
    private final String englishDesc;
    private final String desc;

    EmailSendStatus(int code, String englishDesc, String desc) {
        this.code = code;
        this.englishDesc = englishDesc;
        this.desc = desc;
    }

    public String getEnglishDesc() {
        return englishDesc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static EmailSendStatus codeOf(int code) {
        for(EmailSendStatus status : values()) {
            if(status.getCode() == code){
                return status;
            }
        }
        return WAITING;
    }
}
