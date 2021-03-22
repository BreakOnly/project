package com.jrmf.domain;
/** 
* @author zhangzehui
* @time 2018-10-09
* @description: 用户佣金明细临时表
*/
public class CommissionTemporary {

	private int id;
	private String userName;
	private String idCard;
	private String bankCardNo;
	private String bankName;
	private String amount;

	private String feeRuleType;//服务费计算规则类型
	private String sumFee;//服务费总额
	private String supplementFee;//补充服务费
	private String supplementAmount;//补差价金额
	private String calculationRates;//本次服务费费率
	private String profilt;//利润
	private String profiltRates;//利润率
	
	private String batchId;//批次id 对应交易记录表orderNo
	private int documentType;//证件类型 1 身份证  2 港澳台通行证 3 护照  4 军官证
	private int payType;//支付通道  1 徽商银行  2 支付宝  3 微信 4 银企直联
	private int userId;  
	private String bankNo;  
	private String orderNo;//订单号
	private int status; // 验证结果 1 成功  2 失败  3 已打款 4 删除
	private String statusDesc;//状态描述
	private String originalId;//商户标识
	private String companyId;//对应薪税服务公司id
	private String operatorName;//操作人
	private String createTime;
	private String remark;//订单备注
	private String sourceRemark;
	private String menuId;
	private String updateTime;//最后更新时间
	private String phoneNo;//手机号
	private String batchName;//批次名称
	private String batchDesc;//批次说明
	private String customName;//商户名称

	private String businessPlatform; //所属业务平台
	private String businessManager; //所属客户经理
	private String operationsManager; //所属运营经理
	private String businessChannel;//业务所属渠道
	private String customLabel;//商户标签
	private String rateInterval;
	private String realCompanyId; //真实下发公司id
	private String businessChannelKey;//下发实时渠道key
	private String sourceAmount;


	public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getBatchDesc() {
        return batchDesc;
    }

    public void setBatchDesc(String batchDesc) {
        this.batchDesc = batchDesc;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getPhoneNo() {
		return phoneNo;
	}
	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}
	public String getFeeRuleType() {
		return feeRuleType;
	}
	public void setFeeRuleType(String feeRuleType) {
		this.feeRuleType = feeRuleType;
	}
	public String getProfilt() {
		return profilt;
	}
	public void setProfilt(String profilt) {
		this.profilt = profilt;
	}
	public String getProfiltRates() {
		return profiltRates;
	}
	public void setProfiltRates(String profiltRates) {
		this.profiltRates = profiltRates;
	}
	public String getSumFee() {
		return sumFee;
	}
	public void setSumFee(String sumFee) {
		this.sumFee = sumFee;
	}
	public String getSupplementFee() {
		return supplementFee;
	}
	public void setSupplementFee(String supplementFee) {
		this.supplementFee = supplementFee;
	}
	public String getSupplementAmount() {
		return supplementAmount;
	}
	public void setSupplementAmount(String supplementAmount) {
		this.supplementAmount = supplementAmount;
	}
	public String getCalculationRates() {
		return calculationRates;
	}
	public void setCalculationRates(String calculationRates) {
		this.calculationRates = calculationRates;
	}
	private int repeatcheck;//是否需要进行校验
	public int getRepeatcheck() {
		return repeatcheck;
	}
	public void setRepeatcheck(int repeatcheck) {
		this.repeatcheck = repeatcheck;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getIdCard() {
		return idCard;
	}
	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	public String getBankCardNo() {
		return bankCardNo;
	}
	public void setBankCardNo(String bankCardNo) {
		this.bankCardNo = bankCardNo;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getBatchId() {
		return batchId;
	}
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	public int getDocumentType() {
		return documentType;
	}
	public void setDocumentType(int documentType) {
		this.documentType = documentType;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getBankNo() {
		return bankNo;
	}
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getOriginalId() {
		return originalId;
	}
	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}
	public String getCompanyId() {
		return companyId;
	}
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	public String getOperatorName() {
		return operatorName;
	}
	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getMenuId() {
		return menuId;
	}
	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}
	public int getPayType() {
		return payType;
	}
	public void setPayType(int payType) {
		this.payType = payType;
	}
	public String getStatusDesc() {
		return statusDesc;
	}
	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getBusinessPlatform() {
		return businessPlatform;
	}

	public void setBusinessPlatform(String businessPlatform) {
		this.businessPlatform = businessPlatform;
	}

	public String getBusinessManager() {
		return businessManager;
	}

	public void setBusinessManager(String businessManager) {
		this.businessManager = businessManager;
	}

	public String getBusinessChannel() {
		return businessChannel;
	}

	public void setBusinessChannel(String businessChannel) {
		this.businessChannel = businessChannel;
	}

	public String getCustomLabel() {
		return customLabel;
	}

	public void setCustomLabel(String customLabel) {
		this.customLabel = customLabel;
	}

	@Override
	public String toString() {
		return "CommissionTemporary{" +
				"id=" + id +
				", userName='" + userName + '\'' +
				", idCard='" + idCard + '\'' +
				", bankCardNo='" + bankCardNo + '\'' +
				", bankName='" + bankName + '\'' +
				", amount='" + amount + '\'' +
				", feeRuleType='" + feeRuleType + '\'' +
				", sumFee='" + sumFee + '\'' +
				", supplementFee='" + supplementFee + '\'' +
				", supplementAmount='" + supplementAmount + '\'' +
				", calculationRates='" + calculationRates + '\'' +
				", profilt='" + profilt + '\'' +
				", profiltRates='" + profiltRates + '\'' +
				", batchId='" + batchId + '\'' +
				", documentType=" + documentType +
				", payType=" + payType +
				", userId=" + userId +
				", bankNo='" + bankNo + '\'' +
				", orderNo='" + orderNo + '\'' +
				", status=" + status +
				", statusDesc='" + statusDesc + '\'' +
				", originalId='" + originalId + '\'' +
				", companyId='" + companyId + '\'' +
				", operatorName='" + operatorName + '\'' +
				", createTime='" + createTime + '\'' +
				", remark='" + remark + '\'' +
				", sourceRemark='" + sourceRemark + '\'' +
				", menuId='" + menuId + '\'' +
				", updateTime='" + updateTime + '\'' +
				", phoneNo='" + phoneNo + '\'' +
				", batchName='" + batchName + '\'' +
				", batchDesc='" + batchDesc + '\'' +
				", customName='" + customName + '\'' +
				", businessPlatform='" + businessPlatform + '\'' +
				", businessManager='" + businessManager + '\'' +
				", operationsManager='" + operationsManager + '\'' +
				", businessChannel='" + businessChannel + '\'' +
				", customLabel='" + customLabel + '\'' +
				", rateInterval='" + rateInterval + '\'' +
				", realCompanyId='" + realCompanyId + '\'' +
				", businessChannelKey='" + businessChannelKey + '\'' +
				", sourceAmount='" + sourceAmount + '\'' +
				", repeatcheck=" + repeatcheck +
				'}';
	}

	public String getRateInterval() {
		return rateInterval;
	}

	public void setRateInterval(String rateInterval) {
		this.rateInterval = rateInterval;
	}

	public String getRealCompanyId() {
		return realCompanyId;
	}

	public void setRealCompanyId(String realCompanyId) {
		this.realCompanyId = realCompanyId;
	}

	public String getBusinessChannelKey() {
		return businessChannelKey;
	}

	public void setBusinessChannelKey(String businessChannelKey) {
		this.businessChannelKey = businessChannelKey;
	}

	public String getOperationsManager() {
		return operationsManager;
	}

	public void setOperationsManager(String operationsManager) {
		this.operationsManager = operationsManager;
	}

	public String getSourceAmount() {
		return sourceAmount;
	}

	public void setSourceAmount(String sourceAmount) {
		this.sourceAmount = sourceAmount;
	}

	public String getSourceRemark() {
		return sourceRemark;
	}

	public void setSourceRemark(String sourceRemark) {
		this.sourceRemark = sourceRemark;
	}
}
