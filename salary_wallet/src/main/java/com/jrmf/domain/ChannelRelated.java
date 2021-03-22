package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author zhangzehui
* @time 2017-12-14 
* @description: 商户关联表(薪税钱包)
*/
public class ChannelRelated implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	
	
	private int id;
	private String originalId;//商户标识（原公司）
	private String companyId;//关联的佣金代发商户（佣金代发公司） 对应company表的userid
	private String merchantId;//平台标识

	private String feeRuleType;//服务费计算规则类型
	private String feeRuleDesc;//规则描述
	private String feeRuleFormula;//计算公式
	private String serviceRates;//服务费
	private String upperServiceRates;//三万以上服务费费率
	private String profiltUpper;//三万以上收益率
	private String profiltLower;//三万以下收益率
	private int status;//关联状态：0 无效  1 目前 启用   2  历史
	private String createtime;//创建时间
	private String updatetime;//修改时间
	private String appIdAyg;//爱员工appid
	private String aygPlatForm;//爱员工平台标识
	private String monthQuota;//服务公司设置商户下发累计金额额度

	/**
	 * 冗余字段（不创建数据库字段）
	 * @return
	 */
	private String originalName;//商户名称
	private String companyName;//佣金发放公司名称
	private String customName;//平台名称
	private String balance;//商户对应的服务公司资金存量
	private Integer realCompanyOperate; //真实下发公司限额校验1.开启
	
	public String getAygPlatForm() {
		return aygPlatForm;
	}
	public void setAygPlatForm(String aygPlatForm) {
		this.aygPlatForm = aygPlatForm;
	}
	public String getFeeRuleType() {
		return feeRuleType;
	}
	public void setFeeRuleType(String feeRuleType) {
		this.feeRuleType = feeRuleType;
	}
	public String getFeeRuleDesc() {
		return feeRuleDesc;
	}
	public void setFeeRuleDesc(String feeRuleDesc) {
		this.feeRuleDesc = feeRuleDesc;
	}
	public String getFeeRuleFormula() {
		return feeRuleFormula;
	}
	public void setFeeRuleFormula(String feeRuleFormula) {
		this.feeRuleFormula = feeRuleFormula;
	}
	public String getMonthQuota() {
		return monthQuota;
	}
	public void setMonthQuota(String monthQuota) {
		this.monthQuota = monthQuota;
	}
	public String getAppIdAyg() {
		return appIdAyg;
	}
	public void setAppIdAyg(String appIdAyg) {
		this.appIdAyg = appIdAyg;
	}
	
	public String getUpperServiceRates() {
		return upperServiceRates;
	}
	public void setUpperServiceRates(String upperServiceRates) {
		this.upperServiceRates = upperServiceRates;
	}
	public String getServiceRates() {
		return serviceRates;
	}
	public void setServiceRates(String serviceRates) {
		this.serviceRates = serviceRates;
	}
	public String getProfiltUpper() {
		return profiltUpper;
	}
	public void setProfiltUpper(String profiltUpper) {
		this.profiltUpper = profiltUpper;
	}
	public String getProfiltLower() {
		return profiltLower;
	}
	public void setProfiltLower(String profiltLower) {
		this.profiltLower = profiltLower;
	}
	public String getOriginalName() {
		return originalName;
	}
	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getCustomName() {
		return customName;
	}
	public void setCustomName(String customName) {
		this.customName = customName;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getCompanyId() {
		return companyId;
	}
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getCreatetime() {
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
	public String getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getOriginalId() {
		return originalId;
	}
	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}
	public String getBalance() {
		return balance;
	}
	public void setBalance(String balance) {
		this.balance = balance;
	}
	@Override
	public String toString() {
		return "ChannelRelated [id=" + id + ", originalId=" + originalId + ", companyId=" + companyId + ", merchantId="
				+ merchantId + ", serviceRates=" + serviceRates + ", profiltUpper=" + profiltUpper + ", profiltLower="
				+ profiltLower + ", status=" + status + ", createtime=" + createtime + ", updatetime=" + updatetime
				+ ", originalName=" + originalName + ", companyName=" + companyName + ", customName=" + customName
				+ ", balance=" + balance + "]";
	}
	public Integer getRealCompanyOperate() {
		return realCompanyOperate;
	}
	public void setRealCompanyOperate(Integer realCompanyOperate) {
		this.realCompanyOperate = realCompanyOperate;
	}
	
	
}
 