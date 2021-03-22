package com.jrmf.taxsettlement.api.service;

import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ActionResult<T> implements Serializable {

	private String retCode;

	private String retMsg;

	private String serialNumber;

	private T retData;

	public ActionResult() {
		this.retCode = CommonRetCodes.ACTION_DONE.getCode();
	}

	public ActionResult(T attachment) {
		this.retCode = CommonRetCodes.ACTION_DONE.getCode();
		this.retData = attachment;
	}
	public ActionResult(T attachment, String serialNumber) {
		log.info("个体工商户注册api返回数据:data:"+attachment.toString()+"serialNumber:"+serialNumber);
		this.retCode = CommonRetCodes.ACTION_DONE.getCode();
		this.retData = attachment;
		this.serialNumber = serialNumber;
	}

	public ActionResult(String failCode, String failMessage) {
		this.retCode = failCode;
		this.retMsg = failMessage;
	}

	public ActionResult(String failCode, String failMessage,T attachment, String serialNumber) {
		log.info("个体工商户注册api返回数据:data:"+attachment.toString()+"serialNumber:"+serialNumber);
		this.retCode = failCode;
		this.retData = attachment;
		this.retMsg = failMessage;
		this.serialNumber = serialNumber;
	}

	public boolean isOk() {
		return CommonRetCodes.ACTION_DONE.getCode().equals(this.retCode);
	}

	public String getRetCode() {
		return retCode;
	}

	public String getRetMsg() {
		return retMsg;
	}

	public T getRetData() {
		return this.retData;
	}

	public String getSerialNumber() {
		return serialNumber;
	}
}
