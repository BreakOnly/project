package com.jrmf.payment.util;

import java.io.Serializable;

public final class PaymentReturn<T> implements Serializable {

	private static final long serialVersionUID = -1998926942033225187L;

	private String retCode;

	private String failMessage;
	
	private T attachment;
	
	public PaymentReturn(String retCode, String failMessage, T attachment) {
		super();
		this.retCode = retCode;
		this.failMessage = failMessage;
		this.attachment = attachment;
	}

	public PaymentReturn(String retCode, String failMessage) {
		super();
		this.retCode = retCode;
		this.failMessage = failMessage;
	}

	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}

	public void setFailMessage(String failMessage) {
		this.failMessage = failMessage;
	}

	public String getRetCode() {
		return retCode;
	}

	public String getFailMessage() {
		return failMessage;
	}

	public T getAttachment() {
		return attachment;
	}

	public void setAttachment(T attachment) {
		this.attachment = attachment;
	}

	@Override
	public String toString() {
		return "PaymentReturn [retCode=" + retCode + ", failMessage="
				+ failMessage + ", attachment=" + attachment.toString() + "]";
	}
	
	
	
}
