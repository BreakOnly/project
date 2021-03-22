package com.jrmf.taxsettlement.api;

public class APIDockingException extends RuntimeException {

	private String errorCode;

	private String errorMsg;

	private String serialNumber;

	private Object applyNumber;

	public APIDockingException(String errorCode, String errorMsg) {
		super(new StringBuilder(errorCode).append(":").append(errorMsg).toString());
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
	public APIDockingException(String errorCode, String errorMsg,String serialNumber) {
		super(new StringBuilder(errorCode).append(":").append(errorMsg).append(":").append(serialNumber).toString());
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.serialNumber = serialNumber;
	}

	public APIDockingException(String errorCode, String errorMsg,String serialNumber,Object applyNumber) {
		super(new StringBuilder(errorCode).append(":").append(errorMsg).append(":").append(serialNumber).append(":").append(applyNumber).toString());
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.serialNumber = serialNumber;
		this.applyNumber = applyNumber;
	}


	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public Object getApplyNumber() {
		return applyNumber;
	}
}
