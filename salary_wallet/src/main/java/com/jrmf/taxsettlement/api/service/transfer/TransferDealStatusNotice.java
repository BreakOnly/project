package com.jrmf.taxsettlement.api.service.transfer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TransferDealStatusNotice implements Serializable {

	private int level = 0;
	
	private String notifyUrl;
	
	private boolean batchNotice;
	
	private Map<String, Object> noticeData;
	
	public TransferDealStatusNotice(String notifyUrl, Map<String, Object> noticeData) {
		this.notifyUrl = notifyUrl;
		this.noticeData = noticeData;
		this.setBatchNotice(false);
	}
	
	int increaseAndGetLevel() {
		return ++level;
	}

	String getNotifyUrl() {
		return notifyUrl;
	}

	Map<String, Object> getNoticeData() {
		return new HashMap<String, Object>(noticeData);
	}

	public boolean isBatchNotice() {
		return batchNotice;
	}

	public void setBatchNotice(boolean batchNotice) {
		this.batchNotice = batchNotice;
	}
}
