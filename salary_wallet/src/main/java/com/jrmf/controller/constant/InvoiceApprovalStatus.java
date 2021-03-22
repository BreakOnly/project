package com.jrmf.controller.constant;

public enum InvoiceApprovalStatus {

	NO_TYPE(0,"未核销"), 
	PART_TYPE(1,"部分核销"),
	FINISH_TYPE(2,"已核销");
	
    private final int code;
    private final String desc;
    
	private InvoiceApprovalStatus(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
    public static InvoiceApprovalStatus codeOf(int code) {
        for(InvoiceApprovalStatus certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return NO_TYPE;
    }
}
