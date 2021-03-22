package com.jrmf.controller.constant;

/**
 * Author Nicholas-Ning
 * Description //TODO 批次表状态
 * Date 11:25 2018/11/19
 * Param
 * return
 **/
public enum HistoryStatus {
    /**
     * 0 待确认 1,"全部成功"  2,"全部失败" 3,"已提交，处理中" 4,"驳回" 5,"部分失败" 6,"挂起" 7,"已开票"
     **/
    TOBECONTINUED(0,"待确认"),

    SUCCESS(1,"全部成功"),

    FAILURE(2,"全部失败"),

    SUBMITTED(3,"已提交，处理中"),

    TUREDOWN(4,"驳回"),

    PARTIALFAILURE(5,"部分失败"),

    HANG(6,"挂起"),

    BILLED(7,"已开票");

    private final  int code;
    private final String desc;

    HistoryStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static HistoryStatus codeOf(int code) {
        for(HistoryStatus historyStatus : values()) {
            if(historyStatus.getCode() == code){
                return historyStatus;
            }
        }
        return null;
    }
}
