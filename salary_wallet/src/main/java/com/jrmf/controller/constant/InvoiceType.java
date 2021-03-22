package com.jrmf.controller.constant;

public enum InvoiceType {

	GENERAL_TYPE(1,"普通发票"),
	DEDICATED_TYPE(2,"增值税专用发票");
	
    private final int code;
    private final String desc;
    
	private InvoiceType(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
    public static InvoiceType codeOf(int code) {
        for(InvoiceType certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return GENERAL_TYPE;
    }
    public static InvoiceType codeOfReal(int code) {
        for(InvoiceType certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return null;
    }
}
