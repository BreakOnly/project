package com.jrmf.taxsettlement.api.service.transfer;

import com.jrmf.taxsettlement.api.gateway.NotNull;

public class TransferToWechatAccountServiceParams extends AbstractTransferServiceParams {
	
	@NotNull
	private String openId;
	
	private String mobileNo;

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	
	
}
