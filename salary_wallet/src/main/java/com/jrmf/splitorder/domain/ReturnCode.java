package com.jrmf.splitorder.domain;

public enum ReturnCode {

    REQUEST_SUCCESS(0000, "访问成功"),

    FAIL(0001, "访问失败"),

    SUCCESS(0002, "执行成功"),

    PARAM_ERROR(0003, "参数异常"),

    TEMPLATE_ERROR(0004, "导入模板异常"),

    DATA_OVERFLOW(0005, "导入数据超限"),

    DUPLICATEIDNUMBER(0006, "身份证号重复");


    private Integer state;
    private String msg;

    ReturnCode(Integer state, String msg) {
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

    public static ReturnCode codeOf(Integer code) {
        for (ReturnCode returnCode : values()) {
            if (returnCode.getState().equals(code)) {
                return returnCode;
            }
        }
        return FAIL;
    }
}
