package com.jrmf.taxsettlement.api;

public class APITransferRecordDO {

	private String merchantId;
	
	private String requestNo;

	private String batchNo;
	
	private String partnerId;
	
	private String transferCorpId;
	
	private String dealNo;
	
	private String notifyUrl;

	private String accountDate;
	
	private String status;
	
	private String retCode;
	
	private String retMsg;
	
	private String updateTime;
	
	private String createTime;

	public String getMerchantId() {
		return merchantId;
	}

	public String getRequestNo() {
		return requestNo;
	}

	public String getTransferCorpId() {
		return transferCorpId;
	}

	public String getDealNo() {
		return dealNo;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public String getStatus() {
		return status;
	}

	public String getRetCode() {
		return retCode;
	}

	public String getRetMsg() {
		return retMsg;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public void setRequestNo(String requestNo) {
		this.requestNo = requestNo;
	}

	public void setTransferCorpId(String transferCorpId) {
		this.transferCorpId = transferCorpId;
	}

	public void setDealNo(String dealNo) {
		this.dealNo = dealNo;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	
	public String getAccountDate() {
		return accountDate;
	}

	public void setAccountDate(String accountDate) {
		this.accountDate = accountDate;
	}	

	public void setStatus(String status) {
		this.status = status;
	}

	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}

	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
}
