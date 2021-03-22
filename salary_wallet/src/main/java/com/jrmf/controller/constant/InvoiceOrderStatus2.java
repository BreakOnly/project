package com.jrmf.controller.constant;

public enum InvoiceOrderStatus2 {

	NO_TYPE(0,"未开票"),
	DOING_TYPE(1,"开票处理中"),
	FINISH_TYPE(2,"开票完成"),
	FAIL_TYPE(3,"开票失败");

    private final int code;
    private final String desc;

	private InvoiceOrderStatus2(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
    public static InvoiceOrderStatus2 codeOf(int code) {
        for(InvoiceOrderStatus2 certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return NO_TYPE;
    }
}
