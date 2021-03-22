package com.jrmf.payment.openapi.model.request.econtract;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.econtract.RealTimeResult;

public class ContractRealtimeQueryParam implements IBaseParam<RealTimeResult> {

	private String orderId;
	private String extrOrderId;

	@Override
	public String requestURI() {
		return "/econtract/extr/order/rtqry";
	}

	@Override
	public String methodName() {
		return null;
	}

	@Override
	public String version() {
		return null;
	}
	
	/**
	 * @return the orderId
	 */
	public String getOrderId() {
		return orderId;
	}

	/**
	 * @param orderId the orderId to set
	 */
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	/**
	 * @return the extrOrderId
	 */
	public String getExtrOrderId() {
		return extrOrderId;
	}

	/**
	 * @param extrOrderId the extrOrderId to set
	 */
	public void setExtrOrderId(String extrOrderId) {
		this.extrOrderId = extrOrderId;
	}

	@Override
	public Class<?> respDataClass() {
		return RealTimeResult.class;
	}
	
}
