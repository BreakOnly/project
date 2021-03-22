package com.jrmf.controller.constant;

public enum RechargeLetterStatusType {
    SUCCESS(new Byte("2"), "成功"),
    FAILURE(new Byte("3"), "失败"),
    PROCESSING(new Byte("1"),"未上传"),
    NO_NEED(new Byte("0"),"不需要上传");



    private final Byte code;
    private final String desc;

    RechargeLetterStatusType(Byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RechargeLetterStatusType codeOf(Byte code) {
        for (RechargeLetterStatusType rechargeStatusType : values()) {
            if (rechargeStatusType.getCode().equals(code)) {
                return rechargeStatusType;
            }
        }
        return null;
    }
}
