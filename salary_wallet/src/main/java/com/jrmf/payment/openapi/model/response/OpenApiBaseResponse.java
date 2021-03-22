package com.jrmf.payment.openapi.model.response;


import com.alibaba.fastjson.JSON;


public class OpenApiBaseResponse<T> {

	public static final String SUCCESS_CODE = "0000";

	// 状态
	private String code = SUCCESS_CODE;

	// 返回信息
	private String msg;
	
	private String sign;

	// 响应数据
	private T data;
	
	public OpenApiBaseResponse(){}
	
	public OpenApiBaseResponse(String code, String msg) {
		super();
		this.code = code;
		this.msg = msg;
	}
	
	public OpenApiBaseResponse(String code, String msg, T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}
	

	public OpenApiBaseResponse(T data) {
		this.data = data;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	
	public boolean successed(){
		return SUCCESS_CODE.equals(code);
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
