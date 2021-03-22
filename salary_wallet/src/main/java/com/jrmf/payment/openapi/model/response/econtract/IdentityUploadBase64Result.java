/**
 * 
 */
package com.jrmf.payment.openapi.model.response.econtract;

import com.jrmf.payment.openapi.model.response.IBizResult;

/**
 * @author Napoleon.Chen
 * @date 2018年12月14日
 */
public class IdentityUploadBase64Result implements IBizResult {

	private String sign;
    private String resultCode;
    private String resultMessage;

	/**
	 * @return the sign
	 */
	public String getSign() {
		return sign;
	}

	/**
	 * @param sign the sign to set
	 */
	public void setSign(String sign) {
		this.sign = sign;
	}

	/**
	 * @return the resultCode
	 */
	public String getResultCode() {
		return resultCode;
	}

	/**
	 * @param resultCode the resultCode to set
	 */
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	/**
	 * @return the resultMessage
	 */
	public String getResultMessage() {
		return resultMessage;
	}

	/**
	 * @param resultMessage the resultMessage to set
	 */
	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}
	
}
