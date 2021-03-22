package com.jrmf.taxsettlement.api.service.contract;

public class MerchantUserOpenIdRelationDO {

	private String merchantId;
	
	private String userOpenId;
	
	private String relatedCorpId;
	
	private String relatedOpenId;
	
	private String createTime;

	public String getMerchantId() {
		return merchantId;
	}

	public String getUserOpenId() {
		return userOpenId;
	}

	public String getRelatedCorpId() {
		return relatedCorpId;
	}

	public String getRelatedOpenId() {
		return relatedOpenId;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public void setUserOpenId(String userOpenId) {
		this.userOpenId = userOpenId;
	}

	public void setRelatedCorpId(String relatedCorpId) {
		this.relatedCorpId = relatedCorpId;
	}

	public void setRelatedOpenId(String relatedOpenId) {
		this.relatedOpenId = relatedOpenId;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
