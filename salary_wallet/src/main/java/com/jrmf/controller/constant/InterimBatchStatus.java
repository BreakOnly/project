package com.jrmf.controller.constant;

public enum InterimBatchStatus {

    // 1 全部 校验成功 2 全部校验失败 3 部分失败 4 已打款 5已删除 6待审核 7审核通过 8驳回 9已锁定

    TOBECONTINUED(0, "待确认"),

    SUCCESS(1, "全部校验成功"),

    FAILURE(2, "全部校验失败"),

    PARTIALFAILURE(3, "部分失败"),

    SUBMITTED(4, "已打款"),

    DELETED(5, "已删除"),

    NOTCHECK(6, "待审核"),

    CHECKSUCCESS(7, "审核通过"),

    TUREDOWN(8, "驳回"),

    LOCKIN(9, "已锁定");

    private final int code;
    private final String desc;

    InterimBatchStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static InterimBatchStatus codeOf(int code) {
        for (InterimBatchStatus interimBatchStatus : values()) {
            if (interimBatchStatus.getCode() == code) {
                return interimBatchStatus;
            }
        }
        return null;
    }
}
