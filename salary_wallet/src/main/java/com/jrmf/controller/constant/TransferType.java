package com.jrmf.controller.constant;

/**
 * Author Nicholas-Ning
 * Description //TODO 批次表状态
 * Date 11:25 2018/11/19
 * Param
 * return
 *
 * @author guoto*/
public enum TransferType {
    /**
     * 1,"收入"  2,"支出"（对于服务公司来讲）
     **/
    SUCCESS(1,"充值"),

    UNKNOWN(0,"未知"),

    FAILURE(2,"发放");


    private final  int code;
    private final String desc;

    TransferType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static TransferType codeOf(int code) {
        for(TransferType transferType : values()) {
            if(transferType.getCode() == code){
                return transferType;
            }
        }
        return UNKNOWN;
    }
}
