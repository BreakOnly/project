package com.jrmf.domain;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2019/1/22 15:09
 * Version:1.0
 */
public class ReceiptCommission {
    private int id;
    private String amount;

    private String feeRuleType;//服务费计算规则类型
    private String sumFee;//服务费总额
    private String supplementFee;//补充服务费
    private String supplementAmount;//补差价金额
    private String calculationRates;//本次服务费费率

    private String userId;
    private int userType;
    private int status; // 0 待发放 1  发放成功  2 发放失败 3 已提交,处理中  4  业务删除，无效
    private String createtime;
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
    private String phoneNo;//手机号
    private String reviewName;
    private String receiptChecked;//回单是否匹配
    private String receiptUrl;//回单url
    private int repeatcheck;//是否需要放重复下发校验
    private String receiptNo;//回单号
	private String accountDate;//记账日期
	private String aygRreceiptStatus;//爱员工平台回单请求状态，1：请求成功 2：回单不存在或不支持  3:请求失败 4：回单回调通知接受成功 5：回单下载成功6：回单下载失败

    public String getAygRreceiptStatus() {
		return aygRreceiptStatus;
	}

	public void setAygRreceiptStatus(String aygRreceiptStatus) {
		this.aygRreceiptStatus = aygRreceiptStatus;
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

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
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

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getAygOrderNo() {
        return aygOrderNo;
    }

    public void setAygOrderNo(String aygOrderNo) {
        this.aygOrderNo = aygOrderNo;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
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

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(int invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getInvoiceBatchNo() {
        return invoiceBatchNo;
    }

    public void setInvoiceBatchNo(String invoiceBatchNo) {
        this.invoiceBatchNo = invoiceBatchNo;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
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

    public int getDocumentType() {
        return documentType;
    }

    public void setDocumentType(int documentType) {
        this.documentType = documentType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
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

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public String getReceiptChecked() {
        return receiptChecked;
    }

    public void setReceiptChecked(String receiptChecked) {
        this.receiptChecked = receiptChecked;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }

    public int getRepeatcheck() {
        return repeatcheck;
    }

    public void setRepeatcheck(int repeatcheck) {
        this.repeatcheck = repeatcheck;
    }

    @Override
    public String toString() {
        return "ReceiptCommission{" +
                "id=" + id +
                ", amount='" + amount + '\'' +
                ", feeRuleType='" + feeRuleType + '\'' +
                ", sumFee='" + sumFee + '\'' +
                ", supplementFee='" + supplementFee + '\'' +
                ", supplementAmount='" + supplementAmount + '\'' +
                ", calculationRates='" + calculationRates + '\'' +
                ", userId='" + userId + '\'' +
                ", userType=" + userType +
                ", status=" + status +
                ", createtime='" + createtime + '\'' +
                ", updatetime='" + updatetime + '\'' +
                ", batchId='" + batchId + '\'' +
                ", originalId='" + originalId + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", companyId='" + companyId + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", aygOrderNo='" + aygOrderNo + '\'' +
                ", statusDesc='" + statusDesc + '\'' +
                ", profiltFree='" + profiltFree + '\'' +
                ", profilt='" + profilt + '\'' +
                ", payType=" + payType +
                ", account='" + account + '\'' +
                ", invoiceStatus=" + invoiceStatus +
                ", invoiceBatchNo='" + invoiceBatchNo + '\'' +
                ", menuId=" + menuId +
                ", paymentTime='" + paymentTime + '\'' +
                ", userNo='" + userNo + '\'' +
                ", companyName='" + companyName + '\'' +
                ", certId='" + certId + '\'' +
                ", documentType=" + documentType +
                ", userName='" + userName + '\'' +
                ", customName='" + customName + '\'' +
                ", batchFileName='" + batchFileName + '\'' +
                ", bankName='" + bankName + '\'' +
                ", bankNo='" + bankNo + '\'' +
                ", description='" + description + '\'' +
                ", batchName='" + batchName + '\'' +
                ", batchDesc='" + batchDesc + '\'' +
                ", contentName='" + contentName + '\'' +
                ", remark='" + remark + '\'' +
                ", phoneNo='" + phoneNo + '\'' +
                ", reviewName='" + reviewName + '\'' +
                ", receiptChecked=" + receiptChecked +
                ", receiptUrl='" + receiptUrl + '\'' +
                ", repeatcheck=" + repeatcheck +
                '}';
    }
}
