package com.jrmf.domain;

import java.io.Serializable;

/**
 * @author zhangzehui
 * @time 2017-12-14
 * @description: 渠道交易记录表
 */
public class ChannelHistory implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;


    private int id;
    private String createtime;
    private String amount;//下发金额
    private String ordername;
    private String orderno;//魔方订单号
    private String originalBeachNo;//商户订单号channelHistory
    private int transfertype;//交易类型  1  充值    2 发佣金  3 提现
    private String customkey;//商户标识
    private String recCustomkey;//薪税服务公司userId
    private String updatetime;//操作时间（确认 or 驳回）
    private String providetime;//佣金发放时间
    private String accountNo;//账户号
    private String accountName;//账户名称
    private String serviceFee;//服务费
    private String supplementServiceFee;//服务费补差价
    private String mfkjServiceFee;//利润
    private int payType;//支付方式： 1 徽商银行  2 支付宝  3 微信  4 银企直联
    private int status;//  TransferType=1 0充值待确认  1充值成功  2充值失败/驳回 3已退款完成 7 已开票 8未转账 9充值转账中 10充值转账成功 11充值转账失败    TransferType=2  0 待确认 1发放全部成功 2发放全部失败  3发放处理中  4 驳回 5发放部分失败  6 挂起
    private String operatorName;//操作人
    private String remark;//备注信息
    private int passNum;//成功订单数目
    private int failedNum;//失败订单数目
    private String failedAmount;// 失败金额
    private int batchNum;//总订单数目
    private String batchAmount;//批次文件总金额
    private String handleAmount;//应付总额（成功金额+服务费）
    private int menuId; //项目id
    private String batchName;//批次名称
    private String batchDesc;//批次说明
    private String fileName;//文件名称
    private String statusStr;   //状态中文标识
    private String taskAttachmentFile;//批次任务附件
    private Integer serviceFeeType;
    private Integer rechargeType;
    private String rechargeAmount;
    private String serviceFeeRate;
    private String rechargeFile;
    private Integer rechargeFileNum;
    private Integer rechargeConfirmType;
    private Integer invoiceStatus;
    private String invoiceAmount;
    private String unInvoiceAmount;
    private String invoiceingAmount;
    private String companyOperatorName;
    private String realRechargeAmount;//实际打款金额
    private String linkPhone;
    private String oemUrl;
    private String refundAmount;
    private String payUserName; //实际打款操作人员
    private String realCompanyAmount;
    private String forwardCompanyName;
    private String realCompanyId;
    private String realCompanyName;
    private String forwardRechargeRecordIds;
    private String forwardCommissionAmount;
    private Byte rechargeLetterStatus =new Byte("0");
    private String rechargeLetterUrl;
    private Byte rechargeLetterType;
    private String rechargeLetterErrMsg;

    /**
     * 商户订单号
     */
    private String customOrderNo;

    //非数据库字段
    private Integer currentStatus;
    private String companyName;
    private String customName;
    private String customBalance;

    public String getRechargeLetterErrMsg() {
        return rechargeLetterErrMsg;
    }

    public void setRechargeLetterErrMsg(String rechargeLetterErrMsg) {
        this.rechargeLetterErrMsg = rechargeLetterErrMsg;
    }

    public String getCustomOrderNo() {
        return customOrderNo;
    }

    public void setCustomOrderNo(String customOrderNo) {
        this.customOrderNo = customOrderNo;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    /**
     * 回调地址

     */
    private String notifyUrl;


    /**
     * 冗余字段
     *
     * @return
     */
    private String agentName;//薪税服务公司名称
    private int isEsitPartner;//是否存在合伙人公司
    private int deductionAmount;//批次扣减掉的金额
    private String inAccountNo;//充值收款账号
    private String inAccountName;//充值收款账号所属机构
    private String inAccountBankName;

    public Byte getRechargeLetterStatus() {
        return rechargeLetterStatus;
    }

    public void setRechargeLetterStatus(Byte rechargeLetterStatus) {
        this.rechargeLetterStatus = rechargeLetterStatus;
    }

    public String getRechargeLetterUrl() {
        return rechargeLetterUrl;
    }

    public void setRechargeLetterUrl(String rechargeLetterUrl) {
        this.rechargeLetterUrl = rechargeLetterUrl;
    }

    public Byte getRechargeLetterType() {
        return rechargeLetterType;
    }

    public void setRechargeLetterType(Byte rechargeLetterType) {
        this.rechargeLetterType = rechargeLetterType;
    }

    public String getRealCompanyId() {
        return realCompanyId;
    }

    public void setRealCompanyId(String realCompanyId) {
        this.realCompanyId = realCompanyId;
    }

    public String getRealCompanyName() {
        return realCompanyName;
    }

    public void setRealCompanyName(String realCompanyName) {
        this.realCompanyName = realCompanyName;
    }

    public Integer getRechargeFileNum() {
        return rechargeFileNum;
    }

    public void setRechargeFileNum(Integer rechargeFileNum) {
        this.rechargeFileNum = rechargeFileNum;
    }

    public String getInAccountNo() {
        return inAccountNo;
    }

    public void setInAccountNo(String inAccountNo) {
        this.inAccountNo = inAccountNo;
    }

    public String getInAccountName() {
        return inAccountName;
    }

    public void setInAccountName(String inAccountName) {
        this.inAccountName = inAccountName;
    }

    public String getSupplementServiceFee() {
        return supplementServiceFee;
    }

    public void setSupplementServiceFee(String supplementServiceFee) {
        this.supplementServiceFee = supplementServiceFee;
    }

    public int getDeductionAmount() {
        return deductionAmount;
    }

    public void setDeductionAmount(int deductionAmount) {
        this.deductionAmount = deductionAmount;
    }

    public int getPassNum() {
        return passNum;
    }

    public void setPassNum(int passNum) {
        this.passNum = passNum;
    }

    public int getFailedNum() {
        return failedNum;
    }

    public void setFailedNum(int failedNum) {
        this.failedNum = failedNum;
    }

    public int getBatchNum() {
        return batchNum;
    }

    public void setBatchNum(int batchNum) {
        this.batchNum = batchNum;
    }

    public String getHandleAmount() {
        return handleAmount;
    }

    public void setHandleAmount(String handleAmount) {
        this.handleAmount = handleAmount;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getMfkjServiceFee() {
        return mfkjServiceFee;
    }

    public void setMfkjServiceFee(String mfkjServiceFee) {
        this.mfkjServiceFee = mfkjServiceFee;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOrdername() {
        return ordername;
    }

    public void setOrdername(String ordername) {
        this.ordername = ordername;
    }

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public int getTransfertype() {
        return transfertype;
    }

    public void setTransfertype(int transfertype) {
        this.transfertype = transfertype;
    }

    public String getCustomkey() {
        return customkey;
    }

    public void setCustomkey(String customkey) {
        this.customkey = customkey;
    }

    public String getRecCustomkey() {
        return recCustomkey;
    }

    public void setRecCustomkey(String recCustomkey) {
        this.recCustomkey = recCustomkey;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getProvidetime() {
        return providetime;
    }

    public void setProvidetime(String providetime) {
        this.providetime = providetime;
    }

    public String getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(String serviceFee) {
        this.serviceFee = serviceFee;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public int getIsEsitPartner() {
        return isEsitPartner;
    }

    public void setIsEsitPartner(int isEsitPartner) {
        this.isEsitPartner = isEsitPartner;
    }

    public String getOriginalBeachNo() {
        return originalBeachNo;
    }

    public void setOriginalBeachNo(String originalBeachNo) {
        this.originalBeachNo = originalBeachNo;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFailedAmount() {
        return failedAmount;
    }

    public void setFailedAmount(String failedAmount) {
        this.failedAmount = failedAmount;
    }

    public String getBatchAmount() {
        return batchAmount;
    }

    public void setBatchAmount(String batchAmount) {
        this.batchAmount = batchAmount;
    }

    public String getStatusStr() {
        return statusStr;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }

    public String getTaskAttachmentFile() {
        return taskAttachmentFile;
    }

    public void setTaskAttachmentFile(String taskAttachmentFile) {
        this.taskAttachmentFile = taskAttachmentFile;
    }

    public Integer getServiceFeeType() {
        return serviceFeeType;
    }

    public void setServiceFeeType(Integer serviceFeeType) {
        this.serviceFeeType = serviceFeeType;
    }

    public Integer getRechargeType() {
        return rechargeType;
    }

    public void setRechargeType(Integer rechargeType) {
        this.rechargeType = rechargeType;
    }

    public String getRechargeAmount() {
        return rechargeAmount;
    }

    public void setRechargeAmount(String rechargeAmount) {
        this.rechargeAmount = rechargeAmount;
    }

    public String getServiceFeeRate() {
        return serviceFeeRate;
    }

    public void setServiceFeeRate(String serviceFeeRate) {
        this.serviceFeeRate = serviceFeeRate;
    }

    public String getRechargeFile() {
        return rechargeFile;
    }

    public void setRechargeFile(String rechargeFile) {
        this.rechargeFile = rechargeFile;
    }

    public String getInAccountBankName() {
        return inAccountBankName;
    }

    public void setInAccountBankName(String inAccountBankName) {
        this.inAccountBankName = inAccountBankName;
    }

    public Integer getRechargeConfirmType() {
        return rechargeConfirmType;
    }

    public void setRechargeConfirmType(Integer rechargeConfirmType) {
        this.rechargeConfirmType = rechargeConfirmType;
    }

    public Integer getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(Integer invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(String invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public String getUnInvoiceAmount() {
        return unInvoiceAmount;
    }

    public void setUnInvoiceAmount(String unInvoiceAmount) {
        this.unInvoiceAmount = unInvoiceAmount;
    }

    public String getInvoiceingAmount() {
        return invoiceingAmount;
    }

    public void setInvoiceingAmount(String invoiceingAmount) {
        this.invoiceingAmount = invoiceingAmount;
    }

    public String getCompanyOperatorName() {
        return companyOperatorName;
    }

    public String getRealRechargeAmount() {
		return realRechargeAmount;
	}

	public void setRealRechargeAmount(String realRechargeAmount) {
		this.realRechargeAmount = realRechargeAmount;
	}

	@Override
    public String toString() {
        return "ChannelHistory{" + "id=" + id + ", createtime='" + createtime + '\'' + ", amount='" + amount + '\'' + ", ordername='" + ordername + '\'' + ", orderno='" + orderno + '\'' + ", originalBeachNo='" + originalBeachNo + '\'' + ", transfertype=" + transfertype + ", customkey='" + customkey + '\'' + ", recCustomkey='" + recCustomkey + '\'' + ", updatetime='" + updatetime + '\'' + ", providetime='" + providetime + '\'' + ", accountNo='" + accountNo + '\'' + ", accountName='" + accountName + '\'' + ", serviceFee='" + serviceFee + '\'' + ", supplementServiceFee='" + supplementServiceFee + '\'' + ", mfkjServiceFee='" + mfkjServiceFee + '\'' + ", payType=" + payType + ", status=" + status + ", operatorName='" + operatorName + '\'' + ", remark='" + remark + '\'' + ", passNum=" + passNum + ", failedNum=" + failedNum + ", failedAmount='" + failedAmount + '\'' + ", batchNum=" + batchNum + ", batchAmount='" + batchAmount + '\'' + ", handleAmount='" + handleAmount + '\'' + ", menuId=" + menuId + ", batchName='" + batchName + '\'' + ", batchDesc='" + batchDesc + '\'' + ", fileName='" + fileName + '\'' + ", statusStr='" + statusStr + '\'' + ", taskAttachmentFile='" + taskAttachmentFile + '\'' + ", serviceFeeType=" + serviceFeeType + ", rechargeType=" + rechargeType + ", rechargeAmount='" + rechargeAmount + '\'' + ", serviceFeeRate='" + serviceFeeRate + '\'' + ", rechargeFile='" + rechargeFile + '\'' + ", rechargeConfirmType=" + rechargeConfirmType + ", invoiceStatus=" + invoiceStatus + ", invoiceAmount='" + invoiceAmount + '\'' + ", unInvoiceAmount='" + unInvoiceAmount + '\'' + ", invoiceingAmount='" + invoiceingAmount + '\'' + ", companyOperatorName='" + companyOperatorName + '\'' + ", customOrderNo='" + customOrderNo + '\'' + ", notifyUrl='" + notifyUrl + '\'' + ", companyName='" + companyName + '\'' + ", agentName='" + agentName + '\'' + ", isEsitPartner=" + isEsitPartner + ", deductionAmount=" + deductionAmount + ", inAccountNo='" + inAccountNo + '\'' + ", inAccountName='" + inAccountName + '\'' + ", inAccountBankName='" + inAccountBankName + '\'' + '}';
    }

    public void setCompanyOperatorName(String companyOperatorName) {
        this.companyOperatorName = companyOperatorName;
    }

    public Integer getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Integer currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getLinkPhone() {
        return linkPhone;
    }

    public void setLinkPhone(String linkPhone) {
        this.linkPhone = linkPhone;
    }

    public String getOemUrl() {
        return oemUrl;
    }

    public void setOemUrl(String oemUrl) {
        this.oemUrl = oemUrl;
    }

    public String getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCustomBalance() {
        return customBalance;
    }

    public void setCustomBalance(String customBalance) {
        this.customBalance = customBalance;
    }

    public String getPayUserName() {
	return payUserName;
    }

    public void setPayUserName(String payUserName) {
	this.payUserName = payUserName;
    }

    public String getRealCompanyAmount() {
        return realCompanyAmount;
    }

    public void setRealCompanyAmount(String realCompanyAmount) {
        this.realCompanyAmount = realCompanyAmount;
    }

    public String getForwardCompanyName() {
        return forwardCompanyName;
    }

    public void setForwardCompanyName(String forwardCompanyName) {
        this.forwardCompanyName = forwardCompanyName;
    }

    public String getForwardRechargeRecordIds() {
        return forwardRechargeRecordIds;
    }

    public void setForwardRechargeRecordIds(String forwardRechargeRecordIds) {
        this.forwardRechargeRecordIds = forwardRechargeRecordIds;
    }

    public String getForwardCommissionAmount() {
        return forwardCommissionAmount;
    }

    public void setForwardCommissionAmount(String forwardCommissionAmount) {
        this.forwardCommissionAmount = forwardCommissionAmount;
    }
}

