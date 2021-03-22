package com.jrmf.domain;

import java.io.Serializable;

/**
 * @author zhangzehui
 * @time 2017-12-14
 * @description: 用户佣金表
 */
public class UserCommission implements Serializable {

	private int id;
	private String amount; // 发放金额
	private String sourceAmount; // 交易金额
	private String feeRuleType;//服务费计算规则类型
	private String sumFee;//服务费总额
	private String supplementFee;//补充服务费
	private String supplementAmount;//补差价金额
	private String calculationRates;//本次服务费费率

	private String userId;// 关联qb_user
	private int userType;// 用户类型 1：普通自由职业者 2：企业合伙人
	private int status; // 0 待发放 1  发放成功  2 发放失败 3 已提交,处理中  4  业务删除，无效  5,预授权处理中
	private String createtime;// 创建时间
	private String updatetime;//最后更新时间
	private String batchId;//批次id 对应交易记录表id
	private String originalId;//商户标识
	private String merchantId;//平台标识
	private String companyId;//服务公司id
	private String orderNo;//订单号
	private String operatorName;//操作人
	private String aygOrderNo;//爱员工授权订单号
	private String statusDesc;//状态说明
	//	private String serviceRatesFree;//服务费--弃用
	private String profiltFree;//利润
	//	private String serviceRates;//服务费费率--弃用
	private String profilt;//利润率
	private int payType;//支付方式： 1 银行电子户  2 支付宝  3 微信 4 银企直联
	private String account; //收款账号
	private int invoiceStatus; // 1 已开票  2 未开票
	private int invoiceStatus2; // 0 未开票，1 开票处理中，2 开票完成 3 开票失败
	private String invoiceSerialNo;//开票流水号
	private String invoiceSerialNo2;//补个税开票流水号
	private String individualTax;//个税金额
	private String individualBackTax;//补个税金额
	private String taxRate;//个税税额
	private String invoiceBatchNo;//批次发票号
	private int menuId;//项目ID

	private String paymentTime;//付款成功时间
	private String userNo;
	private String companyName;//服务公司
	private String certId; //证件号码
	private int documentType;//证件类型 1 身份证  2 港澳台通行证 3 护照  4 军官证
	private String userName;//用户姓名
	private String customName;//用户所属公司
	private String batchFileName;//批次文件名称
	private String bankName;//所属银行 （上送银行请求参数）
	private String bankNo; //银行行号 （上送银行请求参数）
	private String description;//交易描述
	private String batchName;//批次名称
	private String batchDesc;//批次说明
	private String contentName;//项目名称
	private String remark;//备注
	private String sourceRemark;
	private String phoneNo;//手机号
	private String reviewName;// 复核员账号
	private String pathNo;//通道ID
	/**
	 * 服务类型:参考爱员工
	 */
	private String serviceType;
	/**
	 * 下发类型：
	 00表示默认，
	 01-表示联动B2B2B2C业务，
	 02表示风控预授权
	 */
	private String regType;
	/**
	 * 回调地址
	 */
	private String notifyUrl;
	/**
	 * 计算方式：
	 0，本地
	 1，爱员工/合摩
	 */
	private String calculateType;

	/**
	 * 冗余字段
	 */
	private String passNum;//用户维度交易成功条数
	private String batchNum;//批次数目
	private String userNum;//用户数目
	private int repeatcheck;//是否需要放重复下发校验
	private String receiptNo;//回单号
	private String accountDate;//记账日期addUserCommissionBatch

	// 外部系统 相关参数

	/**
	 * 支付通道手续费
	 */
	private String channelHandlingFee;
	/**
	 * //通道订单号
	 */
	private String channelOrderNo;
	/**
	 * //商户订单号
	 */
	private String customOrderNo;

	private String businessType;

	private Integer isSplit;

	private Integer isPulbic;//账户类型：1.对公，2.对私

	private String businessPlatform; //所属业务平台
	private String operationsManager; //所属运营经理
	private String businessManager; //所属客户经理
	private String businessChannel;//业务所属渠道
	private String customLabel;//商户标签
	private String subAcctNo;
	private String rateInterval;
	private String realCompanyId; //实际下发公司ID
	private String realCompanyName;
	private String payUserName; //实际打款操作人员
	private String businessChannelKey;//下发实时渠道key

	/**
	 * 商户名称
	 */
	private String merchantName;

	/**
	 * 代理商名称
	 */
	private String agentName;

	/**
	 * 公司名称
	 */
	private String contractCompanyName;

	public String getContractCompanyName() {
		return contractCompanyName;
	}

	public void setContractCompanyName(String contractCompanyName) {
		this.contractCompanyName = contractCompanyName;
	}

	public String getRealCompanyName() {
		return realCompanyName;
	}

	public void setRealCompanyName(String realCompanyName) {
		this.realCompanyName = realCompanyName;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getChannelHandlingFee() {
		return channelHandlingFee;
	}

	public void setChannelHandlingFee(String channelHandlingFee) {
		this.channelHandlingFee = channelHandlingFee;
	}

	public String getChannelOrderNo() {
		return channelOrderNo;
	}

	public void setChannelOrderNo(String channelOrderNo) {
		this.channelOrderNo = channelOrderNo;
	}

	public String getCustomOrderNo() {
		return customOrderNo;
	}

	public void setCustomOrderNo(String customOrderNo) {
		this.customOrderNo = customOrderNo;
	}

	public String getAccountDate() {
		return accountDate;
	}

	public void setAccountDate(String accountDate) {
		this.accountDate = accountDate;
	}

	public String getReceiptNo() {
		return receiptNo;
	}

	public void setReceiptNo(String receiptNo) {
		this.receiptNo = receiptNo;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getReviewName() {
		return reviewName;
	}

	public void setReviewName(String reviewName) {
		this.reviewName = reviewName;
	}

	public String getFeeRuleType() {
		return feeRuleType;
	}

	public void setFeeRuleType(String feeRuleType) {
		this.feeRuleType = feeRuleType;
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

	public int getRepeatcheck() {
		return repeatcheck;
	}

	public void setRepeatcheck(int repeatcheck) {
		this.repeatcheck = repeatcheck;
	}

	public int getMenuId() {
		return menuId;
	}
	public void setMenuId(int menuId) {
		this.menuId = menuId;
	}
	public int getInvoiceStatus() {
		return invoiceStatus;
	}
	public void setInvoiceStatus(int invoiceStatus) {
		this.invoiceStatus = invoiceStatus;
	}

	public int getInvoiceStatus2() {
		return invoiceStatus2;
	}

	public void setInvoiceStatus2(int invoiceStatus2) {
		this.invoiceStatus2 = invoiceStatus2;
	}

	public String getInvoiceSerialNo() {
		return invoiceSerialNo;
	}

	public void setInvoiceSerialNo(String invoiceSerialNo) {
		this.invoiceSerialNo = invoiceSerialNo;
	}

	public String getInvoiceSerialNo2() {
		return invoiceSerialNo2;
	}

	public void setInvoiceSerialNo2(String invoiceSerialNo2) {
		this.invoiceSerialNo2 = invoiceSerialNo2;
	}

	public String getIndividualTax() {
		return individualTax;
	}

	public void setIndividualTax(String individualTax) {
		this.individualTax = individualTax;
	}

	public String getIndividualBackTax() {
		return individualBackTax;
	}

	public void setIndividualBackTax(String individualBackTax) {
		this.individualBackTax = individualBackTax;
	}

	public String getInvoiceBatchNo() {
		return invoiceBatchNo;
	}
	public void setInvoiceBatchNo(String invoiceBatchNo) {
		this.invoiceBatchNo = invoiceBatchNo;
	}
	public int getPayType() {
		return payType;
	}
	public void setPayType(int payType) {
		this.payType = payType;
	}
	public String getOriginalId() {
		return originalId;
	}
	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getProfiltFree() {
		return profiltFree;
	}
	public void setProfiltFree(String profiltFree) {
		this.profiltFree = profiltFree;
	}
	public String getProfilt() {
		return profilt;
	}
	public void setProfilt(String profilt) {
		this.profilt = profilt;
	}
	public String getStatusDesc() {
		return statusDesc;
	}
	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getCreatetime() {
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getUserType() {
		return userType;
	}
	public void setUserType(int userType) {
		this.userType = userType;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getBatchId() {
		return batchId;
	}
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	public String getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getCompanyId() {
		return companyId;
	}
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getCertId() {
		return certId;
	}
	public void setCertId(String certId) {
		this.certId = certId;
	}
	public String getOperatorName() {
		return operatorName;
	}
	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}
	public String getCustomName() {
		return customName;
	}
	public void setCustomName(String customName) {
		this.customName = customName;
	}
	public String getAygOrderNo() {
		return aygOrderNo;
	}
	public void setAygOrderNo(String aygOrderNo) {
		this.aygOrderNo = aygOrderNo;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public int getDocumentType() {
		return documentType;
	}
	public void setDocumentType(int documentType) {
		this.documentType = documentType;
	}
	public String getBatchFileName() {
		return batchFileName;
	}
	public void setBatchFileName(String batchFileName) {
		this.batchFileName = batchFileName;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPaymentTime() {
		return paymentTime;
	}
	public void setPaymentTime(String paymentTime) {
		this.paymentTime = paymentTime;
	}
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
	public String getContentName() {
		return contentName;
	}
	public void setContentName(String contentName) {
		this.contentName = contentName;
	}
	public String getPassNum() {
		return passNum;
	}
	public void setPassNum(String passNum) {
		this.passNum = passNum;
	}
	public String getBatchNum() {
		return batchNum;
	}
	public void setBatchNum(String batchNum) {
		this.batchNum = batchNum;
	}
	public String getUserNum() {
		return userNum;
	}
	public void setUserNum(String userNum) {
		this.userNum = userNum;
	}
	public String getBankNo() {
		return bankNo;
	}
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}

	@Override
	public String toString() {
		return "UserCommission{" + "id=" + id + ", amount='" + amount + '\'' + ", feeRuleType='" + feeRuleType + '\'' + ", sumFee='" + sumFee + '\'' + ", supplementFee='" + supplementFee + '\'' + ", supplementAmount='" + supplementAmount + '\'' + ", calculationRates='" + calculationRates + '\'' + ", userId='" + userId + '\'' + ", userType=" + userType + ", status=" + status + ", createtime='" + createtime + '\'' + ", updatetime='" + updatetime + '\'' + ", batchId='" + batchId + '\'' + ", originalId='" + originalId + '\'' + ", merchantId='" + merchantId + '\'' + ", companyId='" + companyId + '\'' + ", orderNo='" + orderNo + '\'' + ", operatorName='" + operatorName + '\'' + ", aygOrderNo='" + aygOrderNo + '\'' + ", statusDesc='" + statusDesc + '\'' + ", profiltFree='" + profiltFree + '\'' + ", profilt='" + profilt + '\'' + ", payType=" + payType + ", account='" + account + '\'' + ", invoiceStatus=" + invoiceStatus + ", invoiceBatchNo='" + invoiceBatchNo + '\'' + ", menuId=" + menuId + ", paymentTime='" + paymentTime + '\'' + ", userNo='" + userNo + '\'' + ", companyName='" + companyName + '\'' + ", certId='" + certId + '\'' + ", documentType=" + documentType + ", userName='" + userName + '\'' + ", customName='" + customName + '\'' + ", batchFileName='" + batchFileName + '\'' + ", bankName='" + bankName + '\'' + ", bankNo='" + bankNo + '\'' + ", description='" + description + '\'' + ", batchName='" + batchName + '\'' + ", batchDesc='" + batchDesc + '\'' + ", contentName='" + contentName + '\'' + ", remark='" + remark + '\'' + ", phoneNo='" + phoneNo + '\'' + ", reviewName='" + reviewName + '\'' + ", serviceType='" + serviceType + '\'' + ", regType='" + regType + '\'' + ", notifyUrl='" + notifyUrl + '\'' + ", calculateType='" + calculateType + '\'' + ", passNum='" + passNum + '\'' + ", batchNum='" + batchNum + '\'' + ", userNum='" + userNum + '\'' + ", repeatcheck=" + repeatcheck + ", receiptNo='" + receiptNo + '\'' + ", accountDate='" + accountDate + '\'' + ", channelHandlingFee='" + channelHandlingFee + '\'' + ", channelOrderNo='" + channelOrderNo + '\'' + ", customOrderNo='" + customOrderNo + '\'' + '}';
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getRegType() {
		return regType;
	}

	public void setRegType(String regType) {
		this.regType = regType;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getCalculateType() {
		return calculateType;
	}

	public void setCalculateType(String calculateType) {
		this.calculateType = calculateType;
	}
	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public Integer getIsSplit() {
		return isSplit;
	}

	public void setIsSplit(Integer isSplit) {
		this.isSplit = isSplit;
	}

	public Integer getIsPulbic() {
		return isPulbic;
	}

	public void setIsPulbic(Integer isPulbic) {
		this.isPulbic = isPulbic;
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

	public String getPathNo() {
		return pathNo;
	}

	public void setPathNo(String pathNo) {
		this.pathNo = pathNo;
	}

	public String getSubAcctNo() {
		return subAcctNo;
	}

	public void setSubAcctNo(String subAcctNo) {
		this.subAcctNo = subAcctNo;
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

	public String getPayUserName() {
		return payUserName;
	}

	public void setPayUserName(String payUserName) {
		this.payUserName = payUserName;
	}

	public String getBusinessChannelKey() {
		return businessChannelKey;
	}

	public void setBusinessChannelKey(String businessChannelKey) {
		this.businessChannelKey = businessChannelKey;
	}

	public String getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(String taxRate) {
		this.taxRate = taxRate;
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

	public String getOperationsManager() {
		return operationsManager;
	}

	public void setOperationsManager(String operationsManager) {
		this.operationsManager = operationsManager;
	}
}
