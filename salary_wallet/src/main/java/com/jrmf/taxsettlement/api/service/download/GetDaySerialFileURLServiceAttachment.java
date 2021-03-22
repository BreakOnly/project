package com.jrmf.taxsettlement.api.service.download;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

public class GetDaySerialFileURLServiceAttachment extends ActionAttachment {

	private String downloadUrl;

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
		
}
