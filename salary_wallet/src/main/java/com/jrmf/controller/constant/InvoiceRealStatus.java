package com.jrmf.controller.constant;

public enum InvoiceRealStatus {

	READY_TYPE(1,"申请待处理"),
	DOING_TYPE(2,"申请已受理"),
	REJECTION_TYPE(3,"申请驳回"),
	SUCCESS_TYPE(4,"成功"),
	DOWN_TYPE(5,"挂起");
    private final int code;
    private final String desc;
    
	private InvoiceRealStatus(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
    public static InvoiceRealStatus codeOf(int code) {
        for(InvoiceRealStatus certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return READY_TYPE;
    }
}
