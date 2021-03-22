package com.jrmf.controller.constant;

public enum TempStatus {
    /**
     * Author Nicholas-Ning
     * Description //TODO 1,"成功" 2,"失败" 3,"已打款" 4,"已删除"
     * Date 20:06 2018/11/30
     * Param 
     * return 
     **/
    SUCCESS(1,"校验成功"),

    FAILURE(2,"校验失败"),

    SUBMITTED(3,"已打款"),

    DELETED(4,"已删除");

    private final  int code;
    private final String desc;

    TempStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TempStatus codeOf(int code) {
        for(TempStatus tempStatus : values()) {
            if(tempStatus.getCode() == code){
                return tempStatus;
            }
        }
        return null;
    }
}
