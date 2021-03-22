package com.jrmf.controller.constant;

public enum AccountTransStatus {
	
    success(1,"成功"),

    fail(2,"失败"),
    
    doing(3,"处理中");
    
    private final  int code;
    private final String desc;
    
	private AccountTransStatus(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
    
    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
    
    public static AccountTransStatus codeOf(int code) {
        for(AccountTransStatus transferType : values()) {
            if(transferType.getCode() == code){
                return transferType;
            }
        }
        return doing;
    }

}
