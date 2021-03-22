package com.jrmf.domain;

import java.io.Serializable;

/**
* @author 种路路
* @version 创建时间：2017年8月21日 下午4:56:03
* 类说明
*/
public class Company implements Serializable{

	/**
	* @Fields serialVersionUID : TODO()
	*/

	private static final long serialVersionUID = 1854274067215179780L;
	private int id;//主键
	private int userId;//对应主键
	private String companyName;//公司名称
	private String egalPerson;//公司法人
	private String egalPersonCertNo;//公司法人身份证号
	private String contactor;//联系人
	private String contactorMobile;//联系人手机号
	private String email;//邮箱
	private String socialCreditCode;//统一社会信用代码（营业执照）
	private String expiresEnd;//证件有效期（营业执照）
	private String certOrganization;//发证机关（营业执照）
	private String contactorAddress;//联系地址
	private String taxRegisterNo;//税务登记号
	private String businessLicenseNo;//营业执照号
	private String cardNo;//对公账号银行卡号
	private String bankNo;//对公账号银行编号
	private String bankName;//对公账号银行名称
	private String bankCardProvince;//开户所在省
	private String bankCardPhoneNO;//银行预留手机号
	private String bankCardCity;//开户所在市
	private String businessLicenceImg;//营业执照扫描件路径
	private String identityImg;//身份证扫描件
	private String openingPermitsImg;//开户许可证
	private String letterOfAuthorizationImg;//合作企业开户授权委托
	private String proxyIdentityImg;//代理人身份证
	private String callBackUrl;//开通成功，回调地址
	private Long serviceCompanyId;
	private String singleMonthLimit;
	private String singleQuarterLimit;
	private Integer status; //下发公司使用状态
	private String startTime;//开始时间
	private String endTime;//结束时间
	private String realCompanyName;
	private String merchantId;
	private Integer invoiceCategory;
	private String companyKey;
	private String rechargeLetterTemplate;
	private Byte rechargeLetterType;
	private int individualBusiness;


	public Byte getRechargeLetterType() {
		return rechargeLetterType;
	}

	public void setRechargeLetterType(Byte rechargeLetterType) {
		this.rechargeLetterType = rechargeLetterType;
	}

	public String getRechargeLetterTemplate() {
		return rechargeLetterTemplate;
	}

	public void setRechargeLetterTemplate(String rechargeLetterTemplate) {
		this.rechargeLetterTemplate = rechargeLetterTemplate;
	}

	/**
	 * 支付前是否联动第三方平台签约（0：否，1：是）
	 */
	private Integer linkageSign;

	private Integer checkUserAuth; // 是否效验 下发用户完成了个体工商户 1:效验 2:不效验

	public Integer getCheckUserAuth() {
		return checkUserAuth;
	}

	public void setCheckUserAuth(Integer checkUserAuth) {
		this.checkUserAuth = checkUserAuth;
	}

	public String getCompanyKey() {
		return companyKey;
	}

	public void setCompanyKey(String companyKey) {
		this.companyKey = companyKey;
	}

	/**
	 * 真实下发公司id
	 */
	private String realCompanyId;
	/**
	 * 下发公司类型0.实际下发，1.转包下发，2.别名下发
	 */
	private int companyType;
    /**
     * 计算方式：
     0，本地
     1，爱员工/合摩
     */
	private int calculateType;
	private Integer maxAge;
	private Integer minAge;

	private String platformNum;
	private String tranPassword;
	private String agreementTemplateId;

	private Integer uploadFlag;
	private String realCompanyRate; //转包费率

    public int getCalculateType() {
        return calculateType;
    }

    public void setCalculateType(int calculateType) {
        this.calculateType = calculateType;
    }

    /**
	 * 统计报表   冗余字段
	 * @return
	 */
	private String customname;
	private String moduleNo;//业务类型编号
	private String rechangeAmount;//商户充值金额
	private String compensationAmount;//商户（报销、佣金、薪资）业务总金额
	private String accountNum;//商户电子账户个数
	private String withdrawAmount;//商户提现金额
	private String updateTime;

    /**
     * 服务公司属于清结算业务平台
     */
    private String businessPlatform;

    @Override
    public String toString() {
        return "Company{" + "id=" + id + ", userId=" + userId + ", companyName='" + companyName + '\'' + ", egalPerson='" + egalPerson + '\'' + ", egalPersonCertNo='" + egalPersonCertNo + '\'' + ", contactor='" + contactor + '\'' + ", contactorMobile='" + contactorMobile + '\'' + ", email='" + email + '\'' + ", socialCreditCode='" + socialCreditCode + '\'' + ", expiresEnd='" + expiresEnd + '\'' + ", certOrganization='" + certOrganization + '\'' + ", contactorAddress='" + contactorAddress + '\'' + ", taxRegisterNo='" + taxRegisterNo + '\'' + ", businessLicenseNo='" + businessLicenseNo + '\'' + ", cardNo='" + cardNo + '\'' + ", bankNo='" + bankNo + '\'' + ", bankName='" + bankName + '\'' + ", bankCardProvince='" + bankCardProvince + '\'' + ", bankCardPhoneNO='" + bankCardPhoneNO + '\'' + ", bankCardCity='" + bankCardCity + '\'' + ", businessLicenceImg='" + businessLicenceImg + '\'' + ", identityImg='" + identityImg + '\'' + ", openingPermitsImg='" + openingPermitsImg + '\'' + ", letterOfAuthorizationImg='" + letterOfAuthorizationImg + '\'' + ", proxyIdentityImg='" + proxyIdentityImg + '\'' + ", callBackUrl='" + callBackUrl + '\'' + ", serviceCompanyId=" + serviceCompanyId + ", singleMonthLimit='" + singleMonthLimit + '\'' + ", singleQuarterLimit='" + singleQuarterLimit + '\'' + ", status=" + status + ", startTime='" + startTime + '\'' + ", endTime='" + endTime + '\'' + ", realCompanyName='" + realCompanyName + '\'' + ", merchantId='" + merchantId + '\'' + ", realCompanyId='" + realCompanyId + '\'' + ", companyType=" + companyType + ", calculateType=" + calculateType + ", maxAge=" + maxAge + ", minAge=" + minAge + ", platformNum='" + platformNum + '\'' + ", tranPassword='" + tranPassword + '\'' + ", agreementTemplateId='" + agreementTemplateId + '\'' + ", uploadFlag=" + uploadFlag + ", realCompanyRate='" + realCompanyRate + '\'' + ", customname='" + customname + '\'' + ", moduleNo='" + moduleNo + '\'' + ", rechangeAmount='" + rechangeAmount + '\'' + ", compensationAmount='" + compensationAmount + '\'' + ", accountNum='" + accountNum + '\'' + ", withdrawAmount='" + withdrawAmount + '\'' + ", updateTime='" + updateTime + '\'' + ", businessPlatform='" + businessPlatform + '\'' + ", createTime='" + createTime + '\'' + '}';
    }

	public Integer getLinkageSign() {
		return linkageSign;
	}

	public void setLinkageSign(Integer linkageSign) {
		this.linkageSign = linkageSign;
	}

	public String getBusinessPlatform() {
        return businessPlatform;
    }

    public void setBusinessPlatform(String businessPlatform) {
        this.businessPlatform = businessPlatform;
    }

    public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getAgreementTemplateId() {
		return agreementTemplateId;
	}

	public void setAgreementTemplateId(String agreementTemplateId) {
		this.agreementTemplateId = agreementTemplateId;
	}

	public String getPlatformNum() {
		return platformNum;
	}

	public void setPlatformNum(String platformNum) {
		this.platformNum = platformNum;
	}

	public String getTranPassword() {
		return tranPassword;
	}

	public void setTranPassword(String tranPassword) {
		this.tranPassword = tranPassword;
	}

	public String getBankCardPhoneNO() {
		return bankCardPhoneNO;
	}
	public void setBankCardPhoneNO(String bankCardPhoneNO) {
		this.bankCardPhoneNO = bankCardPhoneNO;
	}
	public String getProxyIdentityImg() {
		return proxyIdentityImg;
	}
	public void setProxyIdentityImg(String proxyIdentityImg) {
		this.proxyIdentityImg = proxyIdentityImg;
	}
	private String createTime;//时间

	public String getRealCompanyName() {
		return realCompanyName;
	}

	public void setRealCompanyName(String realCompanyName) {
		this.realCompanyName = realCompanyName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getLetterOfAuthorizationImg() {
		return letterOfAuthorizationImg;
	}
	public void setLetterOfAuthorizationImg(String letterOfAuthorizationImg) {
		this.letterOfAuthorizationImg = letterOfAuthorizationImg;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getEgalPerson() {
		return egalPerson;
	}
	public void setEgalPerson(String egalPerson) {
		this.egalPerson = egalPerson;
	}
	public String getEgalPersonCertNo() {
		return egalPersonCertNo;
	}
	public void setEgalPersonCertNo(String egalPersonCertNo) {
		this.egalPersonCertNo = egalPersonCertNo;
	}
	public String getContactor() {
		return contactor;
	}
	public void setContactor(String contactor) {
		this.contactor = contactor;
	}
	public String getContactorMobile() {
		return contactorMobile;
	}
	public void setContactorMobile(String contactorMobile) {
		this.contactorMobile = contactorMobile;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getSocialCreditCode() {
		return socialCreditCode;
	}
	public void setSocialCreditCode(String socialCreditCode) {
		this.socialCreditCode = socialCreditCode;
	}
	public String getExpiresEnd() {
		return expiresEnd;
	}
	public void setExpiresEnd(String expiresEnd) {
		this.expiresEnd = expiresEnd;
	}
	public String getCertOrganization() {
		return certOrganization;
	}
	public void setCertOrganization(String certOrganization) {
		this.certOrganization = certOrganization;
	}
	public String getContactorAddress() {
		return contactorAddress;
	}
	public void setContactorAddress(String contactorAddress) {
		this.contactorAddress = contactorAddress;
	}
	public String getTaxRegisterNo() {
		return taxRegisterNo;
	}
	public void setTaxRegisterNo(String taxRegisterNo) {
		this.taxRegisterNo = taxRegisterNo;
	}
	public String getBusinessLicenseNo() {
		return businessLicenseNo;
	}
	public void setBusinessLicenseNo(String businessLicenseNo) {
		this.businessLicenseNo = businessLicenseNo;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getBankNo() {
		return bankNo;
	}
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBankCardProvince() {
		return bankCardProvince;
	}
	public void setBankCardProvince(String bankCardProvince) {
		this.bankCardProvince = bankCardProvince;
	}
	public String getBankCardCity() {
		return bankCardCity;
	}
	public void setBankCardCity(String bankCardCity) {
		this.bankCardCity = bankCardCity;
	}
	public String getBusinessLicenceImg() {
		return businessLicenceImg;
	}
	public void setBusinessLicenceImg(String businessLicenceImg) {
		this.businessLicenceImg = businessLicenceImg;
	}
	public String getIdentityImg() {
		return identityImg;
	}
	public void setIdentityImg(String identityImg) {
		this.identityImg = identityImg;
	}
	public String getOpeningPermitsImg() {
		return openingPermitsImg;
	}
	public void setOpeningPermitsImg(String openingPermitsImg) {
		this.openingPermitsImg = openingPermitsImg;
	}

    public String getCustomname() {
		return customname;
	}
	public void setCustomname(String customname) {
		this.customname = customname;
	}
	public String getModuleNo() {
		return moduleNo;
	}
	public void setModuleNo(String moduleNo) {
		this.moduleNo = moduleNo;
	}
	public String getRechangeAmount() {
		return rechangeAmount;
	}
	public void setRechangeAmount(String rechangeAmount) {
		this.rechangeAmount = rechangeAmount;
	}
	public String getCompensationAmount() {
		return compensationAmount;
	}
	public void setCompensationAmount(String compensationAmount) {
		this.compensationAmount = compensationAmount;
	}
	public String getAccountNum() {
		return accountNum;
	}
	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
	}
	public String getWithdrawAmount() {
		return withdrawAmount;
	}
	public void setWithdrawAmount(String withdrawAmount) {
		this.withdrawAmount = withdrawAmount;
	}
	public String getCallBackUrl() {
		return callBackUrl;
	}
	public void setCallBackUrl(String callBackUrl) {
		this.callBackUrl = callBackUrl;
	}

	public Long getServiceCompanyId() {
		return serviceCompanyId;
	}

	public void setServiceCompanyId(Long serviceCompanyId) {
		this.serviceCompanyId = serviceCompanyId;
	}

	public String getSingleMonthLimit() {
		return singleMonthLimit;
	}

	public void setSingleMonthLimit(String singleMonthLimit) {
		this.singleMonthLimit = singleMonthLimit;
	}

	public String getSingleQuarterLimit() {
		return singleQuarterLimit;
	}

	public void setSingleQuarterLimit(String singleQuarterLimit) {
		this.singleQuarterLimit = singleQuarterLimit;
	}

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

	public Integer getMinAge() {
		return minAge;
	}

	public void setMinAge(Integer minAge) {
		this.minAge = minAge;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getRealCompanyId() {
		return realCompanyId;
	}

	public void setRealCompanyId(String realCompanyId) {
		this.realCompanyId = realCompanyId;
	}

	public int getCompanyType() {
		return companyType;
	}

	public void setCompanyType(int companyType) {
		this.companyType = companyType;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public Integer getUploadFlag() {
		return uploadFlag;
	}

	public void setUploadFlag(Integer uploadFlag) {
		this.uploadFlag = uploadFlag;
	}

	public String getRealCompanyRate() {
		return realCompanyRate;
	}

	public void setRealCompanyRate(String realCompanyRate) {
		this.realCompanyRate = realCompanyRate;
	}

	public Integer getInvoiceCategory() {
		return invoiceCategory;
	}

	public void setInvoiceCategory(Integer invoiceCategory) {
		this.invoiceCategory = invoiceCategory;
	}

	public int getIndividualBusiness() {
		return individualBusiness;
	}

	public void setIndividualBusiness(int individualBusiness) {
		this.individualBusiness = individualBusiness;
	}
}
