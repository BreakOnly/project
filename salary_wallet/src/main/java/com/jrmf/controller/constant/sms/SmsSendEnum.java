package com.jrmf.controller.constant.sms;

public enum SmsSendEnum {

    SEND(1,"发送"),

    NOT_SEND(0,"不发送");

    private final  int code;
    private final String desc;

	private SmsSendEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
    
    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
    
    public static SmsSendEnum codeOf(int code) {
        for(SmsSendEnum sendEnum : values()) {
            if(sendEnum.getCode() == code){
                return sendEnum;
            }
        }
        return null;
    }
}
