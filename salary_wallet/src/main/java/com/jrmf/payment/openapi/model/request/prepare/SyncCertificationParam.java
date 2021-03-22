package com.jrmf.payment.openapi.model.request.prepare;

import com.jrmf.payment.openapi.constants.CertificationType;
import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.prepare.AsynCertificationResult;

/**
 * 异步实名认证请求
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2018年8月6日
 */
public class SyncCertificationParam implements IBaseParam<AsynCertificationResult>{

	private String name;
	private String idcard;
	private String validType;
	private String mobile;
	private String payAccountType;
	private String payAccount;
	private String bankName;
	private String notifyUrl;
	private String requestId;	//  请求唯一标识
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdcard() {
		return idcard;
	}

	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}

	public String getValidType() {
		return validType;
	}

	public void setValidType(CertificationType validType) {
		this.validType = validType.getCode();
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPayAccountType() {
		return payAccountType;
	}

	public void setPayAccountType(String payAccountType) {
		this.payAccountType = payAccountType;
	}

	public String getPayAccount() {
		return payAccount;
	}

	public void setPayAccount(String payAccount) {
		this.payAccount = payAccount;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	/**
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @param requestId the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@Override
	public String requestURI() {
		return "/prepare/sync/certification";
	}

	/**
	 * @return the notifyUrl
	 */
	public String getNotifyUrl() {
		return notifyUrl;
	}

	/**
	 * @param notifyUrl the notifyUrl to set
	 */
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	@Override
	public String methodName() {
		return null;
	}

	@Override
	public String version() {
		return null;
	}

	@Override
	public Class<?> respDataClass() {
		return AsynCertificationResult.class;
	}

}
