package com.jrmf.taxsettlement.api.service;

public enum CommonRetCodes {

	ACTION_DONE("0000", "操作完成"),
	
	ACTION_WAIT_TO_CHECK("0001", "操作审核中"),

	ACTION_FAIL("0002", "操作失败"),

	UNSUPPORTED_ACTION("A001", "不支持操作"), 
	
	SYSTEM_BUSY_NOW("A0ZT", "系统忙"),
	
	INVOCATION_NO_RESULT("A0ZU", "调用不明"),
	
	INVOCATION_TIMEOUT("A0ZV", "调用超时"),
	
	INVAILD_DATA("A0ZW", "数据异常"),
	
	INVAILD_PARAMS("A0ZX", "参数异常"),
	
	UNEXPECT_ERROR("A0ZY", "系统异常"),
	
	UNCATCH_ERROR("A0ZZ", "未捕获异常"),
	
	SEND_SMS_ERROR("A0ZS", "发送短信失败");


	private final String code;

	private final String desc;

	private CommonRetCodes(String code, String desc) {
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
