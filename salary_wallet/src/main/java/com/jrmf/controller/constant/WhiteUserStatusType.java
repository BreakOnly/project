package com.jrmf.controller.constant;

public enum WhiteUserStatusType {
	
	
	READY_TYPE(0,"待审核"),
	CONFIRM_TYPE(1,"审核通过"),
	DOWN_TYPE(2,"驳回"),
	INVALID_TYPE(2,"失效");
	
	private final  int code;
    private final String desc;

    private WhiteUserStatusType(int code, String desc) {
    	this.code = code;
    	this.desc = desc;
    }

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
    
    public static WhiteUserStatusType codeOf(int code) {
        for(WhiteUserStatusType transferType : values()) {
            if(transferType.getCode() == code){
                return transferType;
            }
        }
        return READY_TYPE;
    }
    
}
