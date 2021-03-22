package com.jrmf.utils.dto;

public class BaseParam implements IObject {

	private static final String DEFAULT_SIGN_TYPE = "RSA";
	private String extrSystemId;
	private String nonce;
	private String timestamp;
	private String signType = DEFAULT_SIGN_TYPE;
	private String sign;
	private String requestId;	//  请求唯一标识
	private String notifyUrl;	//	异步回调URL

	/**
	 * @return the extrSystemId
	 */
	public String getExtrSystemId() {
		return extrSystemId;
	}

	/**
	 * @param extrSystemId the extrSystemId to set
	 */
	public void setExtrSystemId(String extrSystemId) {
		this.extrSystemId = extrSystemId;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}
	
	/**
	 * @return the requesId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @param requesId the requesId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
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

}
