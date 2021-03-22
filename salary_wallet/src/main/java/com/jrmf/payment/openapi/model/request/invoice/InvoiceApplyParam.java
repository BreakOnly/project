package com.jrmf.payment.openapi.model.request.invoice;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.invoice.InvoiceApplyResult;

/**
 * 发票申请请求
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2018年8月6日
 */
public class InvoiceApplyParam implements IBaseParam<InvoiceApplyResult> {

	/**
	 * 必填(是) string(64) 说明(客户订单号)
	 */
	private String outOrderNo;
	
	/**
	 * 必填(否) string(512) 说明(客户信息包，异步回调时原样返回)
	 */
	private String attach;
	
	/**
	 * 必填(是) Long 说明(开票公司id)
	 */
	private Long serviceCompanyId;
	
	
	/**
	 * 必填(是) Long 说明(税号科目id)
	 */
	private Long invoiceSubjectId;
	
	/**
	 * 必填(否) string(64) 说明(发票备注)
	 */
	private String remark;
	
	/**
	 * 必填(是) string(2) 说明(发票类型，PP-普票，ZP-专票)
	 */
	private String invoiceType;
	
	/**
	 * 必填(是) string(64) 说明(客户公司id)
	 */
	private Long customCompanyId;
	
	/**
	 * 必填(是) Long(14) 说明(开票金额，以分为单位)
	 */
	private Long amount;
	
	/**
	 * 必填(是) String(256) 说明(回调地址)
	 */
	private String notifyUrl;
	
	

	public String getOutOrderNo() {
		return outOrderNo;
	}
	

	public void setOutOrderNo(String outOrderNo) {
		this.outOrderNo = outOrderNo;
	}

	public String getAttach() {
		return attach;
	}

	public void setAttach(String attach) {
		this.attach = attach;
	}

	public Long getServiceCompanyId() {
		return serviceCompanyId;
	}

	public void setServiceCompanyId(Long serviceCompanyId) {
		this.serviceCompanyId = serviceCompanyId;
	}

	public Long getInvoiceSubjectId() {
		return invoiceSubjectId;
	}

	public void setInvoiceSubjectId(Long invoiceSubjectId) {
		this.invoiceSubjectId = invoiceSubjectId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}
	

	public Long getCustomCompanyId() {
		return customCompanyId;
	}

	public void setCustomCompanyId(Long customCompanyId) {
		this.customCompanyId = customCompanyId;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	
	@Override
	public String requestURI() {
		return "/invoice/openapi/invoiceApply";
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
		return InvoiceApplyResult.class;
	}

}
