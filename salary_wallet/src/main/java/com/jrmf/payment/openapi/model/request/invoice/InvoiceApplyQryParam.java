package com.jrmf.payment.openapi.model.request.invoice;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.invoice.InvoiceApplyQryResult;

/**
 * 发票申请查询
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2018年8月6日
 */
public class InvoiceApplyQryParam implements IBaseParam<InvoiceApplyQryResult>{
	/**
	 * 必填(是) long 说明(交易流水号)
	 */
	private Long trasactionId;

	public Long getTrasactionId() {
		return trasactionId;
	}

	public void setTrasactionId(Long trasactionId) {
		this.trasactionId = trasactionId;
	}

	@Override
	public String requestURI() {
		return "/invoice/openapi/invoiceQry";
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
		return InvoiceApplyQryResult.class;
	}


}
