package com.jrmf.taxsettlement.api.gateway.batch;

public enum TransferBatchStatus {

	BATCH_ACCEPTED("A0", "受理完成"),
	
	BATCH_TRANSFERING("B0", "批次下发打款中"),
	
	BATCH_DONE("C0", "批次处理结束");
	
	private String code;
	
	private String desc;
	
	private TransferBatchStatus(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDesc() {
		return desc;
	}

	public static TransferBatchStatus codeOf(String code) {
		for(TransferBatchStatus status : values()) {
			if(status.code.equals(code))
				return status;
		}
		return null;
	}
}
