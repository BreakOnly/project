package com.jrmf.payment.openapi.model.request.invoice;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.invoice.QueryInvoiceBindResult;

/**
 * 查询发票绑定信息
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2018年8月6日
 */
public class QueryInvoiceBindParam implements IBaseParam<QueryInvoiceBindResult>{

	@Override
	public String requestURI() {
		return "/invoice/openapi/bindInfo";
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
		return QueryInvoiceBindResult.class;
	}

}
