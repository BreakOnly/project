package com.jrmf.payment.openapi.model.response.prepare;

import com.jrmf.payment.openapi.model.response.IBizResult;

public class AsynCertificationResult implements IBizResult{

	private String certResult;
	private String certResultMsg;
	
	public String getCertResult() {
		return certResult;
	}
	public void setCertResult(String certResult) {
		this.certResult = certResult;
	}
	public String getCertResultMsg() {
		return certResultMsg;
	}
	public void setCertResultMsg(String certResultMsg) {
		this.certResultMsg = certResultMsg;
	}
	
	
}
