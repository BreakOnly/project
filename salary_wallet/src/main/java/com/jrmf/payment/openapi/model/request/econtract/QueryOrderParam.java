package com.jrmf.payment.openapi.model.request.econtract;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.econtract.QueryOrderResult;

public class QueryOrderParam implements IBaseParam<QueryOrderResult>{

	private String orderId;
	private String extrOrderId;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getExtrOrderId() {
		return extrOrderId;
	}
	
	public void setExtrOrderId(String extrOrderId) {
		this.extrOrderId = extrOrderId;
	}

	@Override
	public String requestURI() {
		return "/econtract/extr/order/qry";
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
		return QueryOrderResult.class;
	}
	
}
