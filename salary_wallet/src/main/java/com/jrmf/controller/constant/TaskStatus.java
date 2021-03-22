package com.jrmf.controller.constant;

public enum TaskStatus {

    SUCCESS(1, "已完成结算"),

    INPROCESS(2, "进行中"),

    SIGNEDUP(3, "已报名"),

    TOBEEMPLOYED(4, "待录用"),

    EMPLOYED(5, "已录用"),

    TOBEONDUTY(6, "待到岗"),

    ONDUTY(7, "已到岗"),

    TOBEPAY(8, "待支付"),

    TOBECONFIRM(9, "待匹配确认"),

    CANCEL(10, "已取消");

    private final int code;
    private final String desc;

    TaskStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TaskStatus codeOf(int code) {
        for (TaskStatus commissionStatus : values()) {
            if (commissionStatus.getCode() == code) {
                return commissionStatus;
            }
        }
        return SIGNEDUP;
    }
}
