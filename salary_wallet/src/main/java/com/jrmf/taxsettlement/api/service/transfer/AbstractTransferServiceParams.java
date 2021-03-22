package com.jrmf.taxsettlement.api.service.transfer;

import com.jrmf.taxsettlement.api.gateway.NotNull;

import java.util.Map;

import com.jrmf.taxsettlement.api.gateway.Amount;
import com.jrmf.taxsettlement.api.service.ActionParams;

public abstract class AbstractTransferServiceParams extends ActionParams {
	
	public static final String PROFILT_RATES = "PROFILT_RATES";

	public static final String CALCULATION_RATES = "CALCULATION_RATES";

	public static final String SUPPLEMENT_AMOUNT = "SUPPLEMENT_AMOUNT";

	public static final String SUPPLEMENT_FEE = "SUPPLEMENT_FEE";

	public static final String PROFIT_AMOUNT = "PROFIT_AMOUNT";

	public static final String SUM_FEE = "SUM_FEE";

	public static final String RATE_INTERVAL = "RATE_INTERVAL";

	public static final String ARRIVL_AMOUNT = "ARRIVL_AMOUNT";
	
	@NotNull
	private String transferCorpId;
	
	@NotNull
	private String requestNo;
	
	@NotNull
	@Amount
	private String amount;
	
	@NotNull
	private String name;
	
	private String certificateType;

	@NotNull
	private String certificateNo;
	
	private String remark;
	
	private String batchNo;
	
	private String notifyUrl;

	private Map<String, String> feeInfo;

	private String sourceAmount;
	
	public String getTransferCorpId() {
		return transferCorpId;
	}

	public String getRequestNo() {
		return requestNo;
	}
	
	public String getAmount() {
		return amount;
	}

	public String getRemark() {
		return remark;
	}

	public void setTransferCorpId(String transferCorpId) {
		this.transferCorpId = transferCorpId;
	}

	public void setRequestNo(String requestNo) {
		this.requestNo = requestNo;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getName() {
		return name;
	}

	public String getCertificateType() {
		return certificateType;
	}

	public String getCertificateNo() {
		return certificateNo;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setCertificateType(String certificateType) {
		this.certificateType = certificateType;
	}

	public void setCertificateNo(String certificateNo) {
		this.certificateNo = certificateNo;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public Map<String, String> getFeeInfo() {
		return feeInfo;
	}

	public void setFeeInfo(Map<String, String> feeInfo) {
		this.feeInfo = feeInfo;
	}

	public String getSourceAmount() {
		return sourceAmount;
	}

	public void setSourceAmount(String sourceAmount) {
		this.sourceAmount = sourceAmount;
	}
}
