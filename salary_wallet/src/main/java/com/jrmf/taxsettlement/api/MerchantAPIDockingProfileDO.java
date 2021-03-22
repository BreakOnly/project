package com.jrmf.taxsettlement.api;

public class MerchantAPIDockingProfileDO {

	private String merchantId;

	private String companyId;
	
	private int apiDockingMode;
	
	private String signType;

	private String updateTime;
	
	private String createTime;

	private String customName;

	public int getApiDockingMode() {
		return apiDockingMode;
	}

	public String getSignType() {
		return signType;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setApiDockingMode(int apiDockingMode) {
		this.apiDockingMode = apiDockingMode;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getCustomName() {
		return customName;
	}

	public void setCustomName(String customName) {
		this.customName = customName;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
}
