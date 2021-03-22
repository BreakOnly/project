package com.jrmf.domain;

public class CustomCompanyRateConf {
    private Integer id;
    // 商户标识
    private String customkey;
    // 关联服务公司费率配置id
    private Integer rateConfId;
    // 服务费计算规则
    private Integer feeRuleType;
    // 收费计算规则
    private String chargeRule;
    // 商户承担费率
    private String customRate;
    // 魔方收益率
    private String mfIncomeRate;
    // 配置描述
    private String confDesc;
    // 创建时间
    private String createTime;
    // 最后一次更新时间
    private String updateTime;
    // 预留1
    private String reserved1;
    // 预留2
    private String reserved2;


    // 冗余字段
    // 挡位标签简写
    private String gearPositionShorthand;
    // 商户名称
    private String customName;
    // 服务公司名称
    private String companyName;
    // 挡位
    private Integer gearPosition;
    // 金额起始
    private String amountStart;
    // 金额结束
    private String amountEnd;
    // 运算符
    private String operator;
    // 平台标识
    private String merchantName;
    // 平台key
    private String merchantId;
    // 挡位描述
    private String gearPositionDesc;
    // 服务公司id
    private Integer companyId;
    //爱员工appid
    private String appId;
    //服务费收取方式(1.充值预扣收,2.下发实时扣收)
    private Integer serviceFeeType;
    
    public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getGearPositionDesc() {
        return gearPositionDesc;
    }

    public void setGearPositionDesc(String gearPositionDesc) {
        this.gearPositionDesc = gearPositionDesc;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomkey() {
        return customkey;
    }

    public void setCustomkey(String customkey) {
        this.customkey = customkey;
    }

    public Integer getRateConfId() {
        return rateConfId;
    }

    public void setRateConfId(Integer rateConfId) {
        this.rateConfId = rateConfId;
    }

    public Integer getFeeRuleType() {
        return feeRuleType;
    }

    public void setFeeRuleType(Integer feeRuleType) {
        this.feeRuleType = feeRuleType;
    }

    public String getChargeRule() {
        return chargeRule;
    }

    public void setChargeRule(String chargeRule) {
        this.chargeRule = chargeRule;
    }

    public String getCustomRate() {
        return customRate;
    }

    public void setCustomRate(String customRate) {
        this.customRate = customRate;
    }

    public String getMfIncomeRate() {
        return mfIncomeRate;
    }

    public void setMfIncomeRate(String mfIncomeRate) {
        this.mfIncomeRate = mfIncomeRate;
    }

    public String getConfDesc() {
        return confDesc;
    }

    public void setConfDesc(String confDesc) {
        this.confDesc = confDesc;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getReserved1() {
        return reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
    }

    public String getReserved2() {
        return reserved2;
    }

    public void setReserved2(String reserved2) {
        this.reserved2 = reserved2;
    }

    public String getGearPositionShorthand() {
        return gearPositionShorthand;
    }

    public void setGearPositionShorthand(String gearPositionShorthand) {
        this.gearPositionShorthand = gearPositionShorthand;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getGearPosition() {
        return gearPosition;
    }

    public void setGearPosition(Integer gearPosition) {
        this.gearPosition = gearPosition;
    }

    public String getAmountStart() {
        return amountStart;
    }

    public void setAmountStart(String amountStart) {
        this.amountStart = amountStart;
    }

    public String getAmountEnd() {
        return amountEnd;
    }

    public void setAmountEnd(String amountEnd) {
        this.amountEnd = amountEnd;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    public Integer getServiceFeeType() {
		return serviceFeeType;
	}

	public void setServiceFeeType(Integer serviceFeeType) {
		this.serviceFeeType = serviceFeeType;
	}

	@Override
    public String toString() {
        return "CustomCompanyRateConf{" + "id=" + id + ", customkey='" + customkey + '\'' + ", rateConfId=" + rateConfId + ", feeRuleType=" + feeRuleType + ", chargeRule='" + chargeRule + '\'' + ", customRate='" + customRate + '\'' + ", mfIncomeRate='" + mfIncomeRate + '\'' + ", confDesc='" + confDesc + '\'' + ", createTime='" + createTime + '\'' + ", updateTime='" + updateTime + '\'' + ", reserved1='" + reserved1 + '\'' + ", reserved2='" + reserved2 + '\'' + ", gearPositionShorthand='" + gearPositionShorthand + '\'' + ", customName='" + customName + '\'' + ", companyName='" + companyName + '\'' + ", gearPosition=" + gearPosition + ", amountStart='" + amountStart + '\'' + ", amountEnd='" + amountEnd + '\'' + ", operator='" + operator + '\'' + ", merchantName='" + merchantName + '\'' + ", merchantId='" + merchantId + '\'' + ", gearPositionDesc='" + gearPositionDesc + '\'' + ", companyId=" + companyId + ", appId='" + appId + '\'' + '}';
    }
}
