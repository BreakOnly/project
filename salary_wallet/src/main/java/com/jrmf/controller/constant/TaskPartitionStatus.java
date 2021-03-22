package com.jrmf.controller.constant;

public enum TaskPartitionStatus {

    PUBLICTASK(1, "公共展示任务"),

    AUTOTASK(2, "智能匹配"),

    IMPORTTASK(3, "商户导入"),

    USERTASK(4, "用户报名"),

    RESOURCETASK(5, "资源任务池");

    private final int code;
    private final String desc;

    TaskPartitionStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TaskPartitionStatus codeOf(int code) {
        for (TaskPartitionStatus commissionStatus : values()) {
            if (commissionStatus.getCode() == code) {
                return commissionStatus;
            }
        }
        return PUBLICTASK;
    }
}
