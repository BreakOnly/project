package com.jrmf.domain;

import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

public class ChannelCustom implements Serializable {

    private static final long serialVersionUID = 2462530856016932583L;

    private int id;
    private String companyName;// 商户名称
    private String contractCompanyName;// 公司名称
    private String merchantName;// 商户名称
    private String password;// 密码
    private int enabled; //状态  0 未启用 ， 1 启用
    private String customkey; // 渠道商唯一标识
    private String createTime;// 创建时间
    private String phoneNo;// 联系电话
    private String email;// 邮箱
    private String username; // 用户名
    private String tranPassword;// 交易密码
    private int customType;   // 1 商户 2 服务公司 3 代理商 4 账户管理员 5集团型商户
    private String startTime;//服务开始时间
    private String endTime;//服务结束时间
    private String AgentId;//代理商ID
    private String masterCustom;//从属商户
    private String bankcardno;//账户号
    private String bankname;//开户行
    private String appSecret;//私钥
    private String receiverName;//收件人姓名
    private String workAddress;//工作地址
    private String address;//邮寄地址
    private String bankMessage;//账户名称
    private int invoiceType;//发票类型 1 ：企业管理咨询服务费
    private int taxpayerType;//纳税人类型 0 一般纳税人 1小规模纳税人
    private String invoiceNo;//纳税人识别号
    private int dataReview;//下发数据符合类型  -1不复核 1需要复核
    private int isReviewTransfer;//是否允许复合页面直接打款 1 允许 -1 不允许
    private int reviewType;//复核限制。1复核本机构 2 复核本机构及以下机构 3 复核本机构及查看以下机构
    private int transferType;//打款限制。1本机构支付，2本机构及上级机构支付
    private int loginRole;//登陆用户的二级角色 1管理员账号 2 操作员账号

    private String subscribeStatus;//公众号关注状态
    private String officialAccOpenId;//公众号平台openid;
    private String splitOrderLimit; //拆单限额
    private String businessPlatform; //所属业务平台
    private Integer businessPlatformId; //所属业务平台ID
    private String businessManager; //所属客户经理
    private String operationsManager;//所属运营经理
    private String businessChannel; // 业务所属渠道
    private String customLabel; // 商户标签
    private int proxyType; //是否关联性代理商 0否 1是
    private String companyPhone;//公司电话

    private String companyOpenNotifyUrl;//商户开通通知地址
    private String umfId;//联动优势id
//    v 2.9.5 添加字段
    private String minTransferAmount;//最小打款金额
    private String minSumFee;//最小服务费
    private String oemCUrl;
    private int fundModelType;
    private int subAccountType;
    private Integer sendSMS;
    private int masterCustomType;
    private String addAccount;//添加这账号
    private String checkAccount;//复核者账号
    private String updateTime;//更新时间
    private boolean needRechargeLetter;





    /**
     * 冗余字段
     */
    private String addressAndPhone;// 单位注册地址及电话
    private String bankNameAndBankNo;//开户行及账号
    private String productNo;//产品编号（银行审核认证后提供）
    private String bankId;//合作银行（默认为微商行"000001"）
    private String coinstCode;//对接平台编码（默认为魔方"000277"）
    private String moduleNo;//产品线（默认为 费控 "000001"）
    private String businessChannelKey;//下发实时渠道key
    private String loginAddress;//登录地址
    private String companyType;

    public boolean getNeedRechargeLetter() {
        return needRechargeLetter;
    }

    public void setNeedRechargeLetter(boolean needRechargeLetter) {
        this.needRechargeLetter = needRechargeLetter;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public String getUmfId() {
        return umfId;
    }

    public void setUmfId(String umfId) {
        this.umfId = umfId;
    }

    public String getSubscribeStatus() {
        return subscribeStatus;
    }

    public void setSubscribeStatus(String subscribeStatus) {
        this.subscribeStatus = subscribeStatus;
    }

    public String getOfficialAccOpenId() {
        return officialAccOpenId;
    }

    public void setOfficialAccOpenId(String officialAccOpenId) {
        this.officialAccOpenId = officialAccOpenId;
    }

    public String getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(String workAddress) {
        this.workAddress = workAddress;
    }

    public int getIsReviewTransfer() {
        return isReviewTransfer;
    }

    public void setIsReviewTransfer(int isReviewTransfer) {
        this.isReviewTransfer = isReviewTransfer;
    }

    public String getOperationsManager() {
        return operationsManager;
    }

    public void setOperationsManager(String operationsManager) {
        this.operationsManager = operationsManager;
    }

    public int getReviewType() {
        return reviewType;
    }

    public void setReviewType(int reviewType) {
        this.reviewType = reviewType;
    }

    public int getTransferType() {
        return transferType;
    }

    public void setTransferType(int transferType) {
        this.transferType = transferType;
    }

    public int getLoginRole() {
        return loginRole;
    }

    public void setLoginRole(int loginRole) {
        this.loginRole = loginRole;
    }

    public int getDataReview() {
        return dataReview;
    }

    public void setDataReview(int dataReview) {
        this.dataReview = dataReview;
    }

    public String getAddressAndPhone() {
        return addressAndPhone;
    }

    public void setAddressAndPhone(String addressAndPhone) {
        this.addressAndPhone = addressAndPhone;
    }

    public String getBankNameAndBankNo() {
        return bankNameAndBankNo;
    }

    public void setBankNameAndBankNo(String bankNameAndBankNo) {
        this.bankNameAndBankNo = bankNameAndBankNo;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getBankcardno() {
        return bankcardno;
    }

    public void setBankcardno(String bankcardno) {
        this.bankcardno = bankcardno;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public String getCustomkey() {
        return customkey;
    }

    public void setCustomkey(String customkey) {
        this.customkey = customkey;
    }

    public String getCreateTime() {
        if(StringUtils.isNotBlank(createTime)) {
            return createTime.substring(0, 19);
        }
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTranPassword() {
        return tranPassword;
    }

    public void setTranPassword(String tranPassword) {
        this.tranPassword = tranPassword;
    }

    public int getCustomType() {
        return customType;
    }

    public void setCustomType(int customType) {
        this.customType = customType;
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

    public String getProductNo() {
        return productNo;
    }

    public void setProductNo(String productNo) {
        this.productNo = productNo;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getCoinstCode() {
        return coinstCode;
    }

    public void setCoinstCode(String coinstCode) {
        this.coinstCode = coinstCode;
    }

    public String getModuleNo() {
        return moduleNo;
    }

    public void setModuleNo(String moduleNo) {
        this.moduleNo = moduleNo;
    }

    public String getCompanyOpenNotifyUrl() {
        return companyOpenNotifyUrl;
    }

    public void setCompanyOpenNotifyUrl(String companyOpenNotifyUrl) {
        this.companyOpenNotifyUrl = companyOpenNotifyUrl;
    }

    public String getAgentId() {
        return AgentId;
    }

    public void setAgentId(String agentId) {
        AgentId = agentId;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getMasterCustom() {
        return masterCustom;
    }

    public void setMasterCustom(String masterCustom) {
        this.masterCustom = masterCustom;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBankMessage() {
        return bankMessage;
    }

    public void setBankMessage(String bankMessage) {
        this.bankMessage = bankMessage;
    }

    public int getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(int invoiceType) {
        this.invoiceType = invoiceType;
    }

    public int getTaxpayerType() {
        return taxpayerType;
    }

    public void setTaxpayerType(int taxpayerType) {
        this.taxpayerType = taxpayerType;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getSplitOrderLimit() {
        return splitOrderLimit;
    }

    public void setSplitOrderLimit(String splitOrderLimit) {
        this.splitOrderLimit = splitOrderLimit;
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

    public int getProxyType() {
        return proxyType;
    }

    public void setProxyType(int proxyType) {
        this.proxyType = proxyType;
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

    public String getCompanyPhone() {
		return companyPhone;
	}

	public void setCompanyPhone(String companyPhone) {
		this.companyPhone = companyPhone;
	}

	@Override
    public boolean equals(Object obj) {
        if (obj instanceof ChannelCustom) {
            return obj.toString().equals(this.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        return "ChannelCustom{" +
            "id=" + id +
            ", companyName='" + companyName + '\'' +
            ", merchantName='" + merchantName + '\'' +
            ", password='" + password + '\'' +
            ", enabled=" + enabled +
            ", customkey='" + customkey + '\'' +
            ", createTime='" + createTime + '\'' +
            ", phoneNo='" + phoneNo + '\'' +
            ", email='" + email + '\'' +
            ", username='" + username + '\'' +
            ", tranPassword='" + tranPassword + '\'' +
            ", customType=" + customType +
            ", startTime='" + startTime + '\'' +
            ", endTime='" + endTime + '\'' +
            ", AgentId='" + AgentId + '\'' +
            ", masterCustom='" + masterCustom + '\'' +
            ", bankcardno='" + bankcardno + '\'' +
            ", bankname='" + bankname + '\'' +
            ", appSecret='" + appSecret + '\'' +
            ", receiverName='" + receiverName + '\'' +
            ", workAddress='" + workAddress + '\'' +
            ", address='" + address + '\'' +
            ", bankMessage='" + bankMessage + '\'' +
            ", invoiceType=" + invoiceType +
            ", taxpayerType=" + taxpayerType +
            ", invoiceNo='" + invoiceNo + '\'' +
            ", dataReview=" + dataReview +
            ", isReviewTransfer=" + isReviewTransfer +
            ", reviewType=" + reviewType +
            ", transferType=" + transferType +
            ", loginRole=" + loginRole +
            ", subscribeStatus='" + subscribeStatus + '\'' +
            ", officialAccOpenId='" + officialAccOpenId + '\'' +
            ", splitOrderLimit='" + splitOrderLimit + '\'' +
            ", businessPlatform='" + businessPlatform + '\'' +
            ", businessManager='" + businessManager + '\'' +
            ", operationsManager='" + operationsManager + '\'' +
            ", businessChannel='" + businessChannel + '\'' +
            ", customLabel='" + customLabel + '\'' +
            ", proxyType=" + proxyType +
            ", companyPhone='" + companyPhone + '\'' +
            ", companyOpenNotifyUrl='" + companyOpenNotifyUrl + '\'' +
            ", umfId='" + umfId + '\'' +
            ", minTransferAmount='" + minTransferAmount + '\'' +
            ", minSumFee='" + minSumFee + '\'' +
            ", oemCUrl='" + oemCUrl + '\'' +
            ", fundModelType=" + fundModelType +
            ", subAccountType=" + subAccountType +
            ", sendSMS=" + sendSMS +
            ", addressAndPhone='" + addressAndPhone + '\'' +
            ", bankNameAndBankNo='" + bankNameAndBankNo + '\'' +
            ", productNo='" + productNo + '\'' +
            ", bankId='" + bankId + '\'' +
            ", coinstCode='" + coinstCode + '\'' +
            ", moduleNo='" + moduleNo + '\'' +
            ", businessChannelKey='" + businessChannelKey + '\'' +
            '}';
    }

    public String getMinTransferAmount() {
        return minTransferAmount;
    }

    public void setMinTransferAmount(String minTransferAmount) {
        this.minTransferAmount = minTransferAmount;
    }

    public String getMinSumFee() {
        return minSumFee;
    }

    public void setMinSumFee(String minSumFee) {
        this.minSumFee = minSumFee;
    }

	public String getBusinessChannelKey() {
		return businessChannelKey;
	}

	public void setBusinessChannelKey(String businessChannelKey) {
		this.businessChannelKey = businessChannelKey;
	}

    public String getOemCUrl() {
        return oemCUrl;
    }

    public void setOemCUrl(String oemCUrl) {
        this.oemCUrl = oemCUrl;
    }

    public int getFundModelType() {
        return fundModelType;
    }

    public void setFundModelType(int fundModelType) {
        this.fundModelType = fundModelType;
    }

    public int getSubAccountType() {
        return subAccountType;
    }

    public void setSubAccountType(int subAccountType) {
        this.subAccountType = subAccountType;
    }

    public Integer getSendSMS() {
        return sendSMS;
    }

    public void setSendSMS(Integer sendSMS) {
        this.sendSMS = sendSMS;
    }

    public Integer getBusinessPlatformId() {
        return businessPlatformId;
    }

    public void setBusinessPlatformId(Integer businessPlatformId) {
        this.businessPlatformId = businessPlatformId;
    }

    public int getMasterCustomType() {
        return masterCustomType;
    }

    public void setMasterCustomType(int masterCustomType) {
        this.masterCustomType = masterCustomType;
    }

    public String getAddAccount() {
        return addAccount;
    }

    public void setAddAccount(String addAccount) {
        this.addAccount = addAccount;
    }

    public String getCheckAccount() {
        return checkAccount;
    }

    public void setCheckAccount(String checkAccount) {
        this.checkAccount = checkAccount;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getLoginAddress() {
        return loginAddress;
    }

    public void setLoginAddress(String loginAddress) {
        this.loginAddress = loginAddress;
    }


    public String getContractCompanyName() {
        return contractCompanyName;
    }

    public void setContractCompanyName(String contractCompanyName) {
        this.contractCompanyName = contractCompanyName;
    }
}
