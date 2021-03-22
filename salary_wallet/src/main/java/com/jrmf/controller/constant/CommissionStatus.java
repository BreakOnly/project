package com.jrmf.controller.constant;

public enum CommissionStatus {
    /**
     * Author Nicholas-Ning
     * Description //TODO 1,"成功" 2,"失败" 3,"已提交，处理中" 4,"已删除"
     * Date 13:52 2018/11/19
     * Param
     * return 
     **/
    SUCCESS(1,"交易成功"),

    FAILURE(2,"交易失败"),

    SUBMITTED(3,"已提交，处理中"),

    DELETED(4,"已删除");

    private final  int code;
    private final String desc;

    CommissionStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CommissionStatus codeOf(int code) {
        for(CommissionStatus commissionStatus : values()) {
            if(commissionStatus.getCode() == code){
                return commissionStatus;
            }
        }
        return SUBMITTED;
    }
}
