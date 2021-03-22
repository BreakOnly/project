package com.jrmf.payment.openapi.model.request.econtract;

import com.jrmf.payment.openapi.model.request.IBaseParam;

public class ContractCancelOrderParam implements IBaseParam<Void> {

	private String extrOrderId;
	private String orderId;

	@Override
	public String requestURI() {
		return "/econtract/extr/order/cancelsign";
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

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Override
	public Class<?> respDataClass() {
		return Void.class;
	}
	
	
	
}
