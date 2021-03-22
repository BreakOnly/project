package com.jrmf.controller.constant;

public enum TaxpayerType {
	
	GENERAL_TYPE(1,"一般纳税人"),
	SMALL_TYPE(2,"小规模纳税人");
	
    private final int code;
    private final String desc;
    
	private TaxpayerType(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
    public static TaxpayerType codeOf(int code) {
        for(TaxpayerType certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return GENERAL_TYPE;
    }
}
