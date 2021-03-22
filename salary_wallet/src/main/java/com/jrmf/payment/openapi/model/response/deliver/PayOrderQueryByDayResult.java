package com.jrmf.payment.openapi.model.response.deliver;

import com.jrmf.payment.openapi.model.response.IBizResult;

public class PayOrderQueryByDayResult implements IBizResult {

	private String day;
    private String downloadUrl;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
	
}
