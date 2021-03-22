package com.jrmf.controller.constant;

public enum InvoiceOrderStatus {

	NO_TYPE(0,"未开票"), 
	DOING_TYPE(1,"部分开票"),
	SECTION_TYPE(2,"完成开票"),
	FINISH_TYPE(3,"处理中");
	
    private final int code;
    private final String desc;
    
	private InvoiceOrderStatus(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
    public static InvoiceOrderStatus codeOf(int code) {
        for(InvoiceOrderStatus certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return NO_TYPE;
    }
}
