package com.jrmf.controller.constant;

public enum AccountTransType {

    /**
     * 1,"收入"  2,"支出"（对于服务公司来讲）
     **/
    transOut(1,"出金"),

    transIn(2,"入金");
    
    private final  int code;
    private final String desc;
    
	private AccountTransType(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
    
    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
    
    public static AccountTransType codeOf(int code) {
        for(AccountTransType transferType : values()) {
            if(transferType.getCode() == code){
                return transferType;
            }
        }
        return transOut;
    }
}
