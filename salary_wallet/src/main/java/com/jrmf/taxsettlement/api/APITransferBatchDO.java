package com.jrmf.taxsettlement.api;

public class APITransferBatchDO {

	private String merchantId;
	
	private String batchNo;
	
	private String partnerId;
	
	private String transferCorpId;
		
	private String notifyUrl;
	
	private String status;
	
	private int totalRequestCount;
	
	private int distilledRequestCount;
	
	private int undistillRequestCount;
	
	private int acceptRequestCount;
	
	private int unacceptRequestCount;
	
	private int transferDoneRequestCount;
	
	private int transferFailRequestCount;
	
	private String updateTime;
	
	private String createTime;

	public String getMerchantId() {
		return merchantId;
	}

	public String getTransferCorpId() {
		return transferCorpId;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public String getStatus() {
		return status;
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

	public void setTransferCorpId(String transferCorpId) {
		this.transferCorpId = transferCorpId;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public int getTotalRequestCount() {
		return totalRequestCount;
	}

	public void setTotalRequestCount(int totalRequestCount) {
		this.totalRequestCount = totalRequestCount;
	}

	public int getAcceptRequestCount() {
		return acceptRequestCount;
	}

	public void setAcceptRequestCount(int acceptRequestCount) {
		this.acceptRequestCount = acceptRequestCount;
	}

	public int getUnacceptRequestCount() {
		return unacceptRequestCount;
	}

	public void setUnacceptRequestCount(int unacceptRequestCount) {
		this.unacceptRequestCount = unacceptRequestCount;
	}

	public int getDistilledRequestCount() {
		return distilledRequestCount;
	}

	public void setDistilledRequestCount(int distilledRequestCount) {
		this.distilledRequestCount = distilledRequestCount;
	}

	public int getUndistillRequestCount() {
		return undistillRequestCount;
	}

	public void setUndistillRequestCount(int undistillRequestCount) {
		this.undistillRequestCount = undistillRequestCount;
	}

	public int getTransferDoneRequestCount() {
		return transferDoneRequestCount;
	}

	public void setTransferDoneRequestCount(int transferDoneRequestCount) {
		this.transferDoneRequestCount = transferDoneRequestCount;
	}

	public int getTransferFailRequestCount() {
		return transferFailRequestCount;
	}

	public void setTransferFailRequestCount(int transferFailRequestCount) {
		this.transferFailRequestCount = transferFailRequestCount;
	}
}
