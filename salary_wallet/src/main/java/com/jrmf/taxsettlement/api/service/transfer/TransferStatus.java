package com.jrmf.taxsettlement.api.service.transfer;

public enum TransferStatus {

	ACCEPTED("A0", "受理成功"),
	
	FAIL_TO_ACCEPT("A1", "受理失败"),
	
	TRANSFERING("B0", "下发打款中"),
	
	TRANSFER_DONE("C1", "下发打款成功"),
	
	TRANSFER_FAILED("C2", "下发打款失败"),
	
	TRANSFER_ROLLBACK("C3", "成功后被冲正");
	
	private String code;
	
	private String desc;
	
	private TransferStatus(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDesc() {
		return desc;
	}
}
