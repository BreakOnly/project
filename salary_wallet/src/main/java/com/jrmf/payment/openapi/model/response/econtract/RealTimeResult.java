package com.jrmf.payment.openapi.model.response.econtract;

import com.jrmf.payment.openapi.model.response.IBizResult;

public class RealTimeResult implements IBizResult {

	private String status;
	private String desc;

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}
