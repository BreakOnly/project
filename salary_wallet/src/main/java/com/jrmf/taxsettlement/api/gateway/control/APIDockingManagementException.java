package com.jrmf.taxsettlement.api.gateway.control;

public class APIDockingManagementException extends RuntimeException {

	private String errorCode;
	
	private String errorMsg;
	
	APIDockingManagementException(String errorCode, String errorMsg) {
		super(errorMsg);
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
}
