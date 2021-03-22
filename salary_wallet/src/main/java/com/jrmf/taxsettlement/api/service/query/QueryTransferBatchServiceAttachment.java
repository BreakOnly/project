package com.jrmf.taxsettlement.api.service.query;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

public class QueryTransferBatchServiceAttachment extends ActionAttachment {

	private String batchNo;
	
	private String status;
	
	private String statusDesc;
	
	private int totalRequestCount;
	
	private int acceptRequestCount;
	
	private int unacceptRequestCount;
	
	private int transferDoneRequestCount;
	
	private int transferFailRequestCount;
	
	private String createTime;

	public String getBatchNo() {
		return batchNo;
	}

	public String getStatus() {
		return status;
	}

	public String getStatusDesc() {
		return statusDesc;
	}

	public int getTotalRequestCount() {
		return totalRequestCount;
	}

	public int getAcceptRequestCount() {
		return acceptRequestCount;
	}

	public int getUnacceptRequestCount() {
		return unacceptRequestCount;
	}

	public int getTransferDoneRequestCount() {
		return transferDoneRequestCount;
	}

	public int getTransferFailRequestCount() {
		return transferFailRequestCount;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}

	public void setTotalRequestCount(int totalRequestCount) {
		this.totalRequestCount = totalRequestCount;
	}

	public void setAcceptRequestCount(int acceptRequestCount) {
		this.acceptRequestCount = acceptRequestCount;
	}

	public void setUnacceptRequestCount(int unacceptRequestCount) {
		this.unacceptRequestCount = unacceptRequestCount;
	}

	public void setTransferDoneRequestCount(int transferDoneRequestCount) {
		this.transferDoneRequestCount = transferDoneRequestCount;
	}

	public void setTransferFailRequestCount(int transferFailRequestCount) {
		this.transferFailRequestCount = transferFailRequestCount;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
