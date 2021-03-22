package com.jrmf.payment.util;


public enum AygPayBankName {
	
	ALI(2, "alipay"),
	WECHAT(3, "wx"),
	WECHAT_PACK(5, "wxpack"),
	UNKNOW_BANK(0, "unknow");

	private int code;
	private String bankName;

    public static AygPayBankName codeOf(int code) {
        for(AygPayBankName certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return UNKNOW_BANK;
    }
	
	AygPayBankName(int code, String bankName) {
		this.code = code;
		this.bankName = bankName;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

}
