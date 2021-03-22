package com.jrmf.controller.constant;

public enum InvoiceStatus {

	READY_TYPE(0,"待确认"),
	CONFIRM_TYPE(1,"确认"),
	DOWN_TYPE(2,"驳回");
	
    private final int code;
    private final String desc;
    
	private InvoiceStatus(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
    public static InvoiceStatus codeOf(int code) {
        for(InvoiceStatus certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return READY_TYPE;
    }
}
