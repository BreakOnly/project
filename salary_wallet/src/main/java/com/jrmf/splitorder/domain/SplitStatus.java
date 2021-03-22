package com.jrmf.splitorder.domain;

public enum SplitStatus {
    SUCCESS(1, "可以拆单"),

    FAIL(-1, "不可以拆单"),

    AMOUNT_NOT(2, "金额不能为空");


    private Integer state;
    private String msg;

    SplitStatus(Integer state, String msg) {
        this.state = state;
        this.msg = msg;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static SplitStatus codeOf(Integer code) {
        for (SplitStatus splitStatus : values()) {
            if (splitStatus.getState().equals(code)) {
                return splitStatus;
            }
        }
        return FAIL;
    }
}
