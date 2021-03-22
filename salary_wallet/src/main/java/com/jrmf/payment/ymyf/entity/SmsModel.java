package com.jrmf.payment.ymyf.entity;

import java.io.Serializable;


/***
 *
*
* 描    述：5.6 服务商付款短信确认接口
*
* 创 建 者： @author wl
* 创建时间： 2020/2/7 11:15
* 创建描述：
*
* 修 改 者：
* 修改时间：
* 修改描述：
*
* 审 核 者：
* 审核时间：
* 审核描述：
*
 */
public class SmsModel implements Serializable{
	
	private static final long serialVersionUID = 1L;

	/**
	 * 批次号
	 */
	private String batchNo;

	/**
	 * 请求id
	 */
	private String reqId;

	/**
	 * 短信验证码
	 */
	private String smsCode;


	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getReqId() {
		return reqId;
	}

	public void setReqId(String reqId) {
		this.reqId = reqId;
	}

	public String getSmsCode() {
		return smsCode;
	}

	public void setSmsCode(String smsCode) {
		this.smsCode = smsCode;
	}
}
