package com.jrmf.payment.openapi.model.response.invoice;

import com.jrmf.payment.openapi.model.response.IBizResult;

public class InvoiceApplyQryResult implements IBizResult{
	/**
	 * 必填(是) long 说明(交易流水号)
	 */
	private Long trasactionId;
	/**
	 * 必填(是) string(64) 说明(客户订单号)
	 */
	private String outOrderId;
	/**
	 * 必填(否) string(512) 说明(客户请求信息包，原样返回)
	 */
	private String attach;
	/**
	 * 必填(是) Long(14) 说明(开票金额，以分为单位)
	 */
	private Long amount;
	/**
	 * 必填(否) string(20) 说明(发票代码)
	 */
	private String invoiceCode;
	/**
	 * 必填(否) string(20) 说明(发票号码)
	 */
	private String invoiceNo;
	/**
	 * 必填(是) string(20) 说明(交易结果success-成功，fail-失败，dealing-处理中)
	 */
	private String resultCode;
	/**
	 * 必填(否) string(1024) 说明(交易结果描述)
	 */
	private String resultMsg;

	public Long getTrasactionId() {
		return trasactionId;
	}

	public void setTrasactionId(Long trasactionId) {
		this.trasactionId = trasactionId;
	}

	public String getOutOrderId() {
		return outOrderId;
	}

	public void setOutOrderId(String outOrderId) {
		this.outOrderId = outOrderId;
	}

	public String getAttach() {
		return attach;
	}

	public void setAttach(String attach) {
		this.attach = attach;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public String getInvoiceCode() {
		return invoiceCode;
	}

	public void setInvoiceCode(String invoiceCode) {
		this.invoiceCode = invoiceCode;
	}

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

}
