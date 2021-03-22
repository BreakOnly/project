package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author zhangzehui
* @time 2018-10-14 
* @description: 渠道临时交易记录表
*/
public class ChannelInterimBatch implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private int id;
	private String createtime;
	private String amount;
	private String ordername;
	private String orderno;//魔方订单号
	private String originalBeachNo;//商户订单号channelHistory
	private String customkey;//商户标识
	private String merchantName;//商户名称
	private String recCustomkey;//薪税服务公司userId
	private String updatetime;//操作时间（确认 or 驳回）
	private String providetime;//佣金发放时间
	private String accountNo;//账户号
	private String accountName;//账户名称
	private String serviceFee;//服务费
	private String supplementServiceFee;//服务费补差价
	private String mfkjServiceFee;//利润
	private int payType;//支付方式： 1 徽商银行  2 支付宝  3 微信  4 银企直联
	private int status;// 1 全部 校验成功 2 全部校验失败 3 部分失败 4 已打款 5已删除 6带审核 7审核通过 8驳回 9已锁定
	private String operatorName;//操作人
	private String remark;//备注信息
	private int passNum;//校验成功订单数目
	private int failedNum;//校验失败订单数目
	private String failedAmount;//校验失败订单总金额
	private int batchNum;//总订单数目
	private String batchAmount;//批次总金额
	private String handleAmount;//应付总额
	private int menuId; //项目id
	private String batchName;//批次名称
	private String batchDesc;//批次说明
	private String fileUrl;//源文件地址
	private String fileName;//文件名称
	private String reviewor;//复核员
	private String reviewDesc;//复核意见
	
	private String taskAttachmentFile;//批次任务附件
	private Integer lockStatus;
	private String ymBatchNo;//溢美服务商预下单返回的批次号
	private String ymReqId;//溢美服务商预下单返回的requestId
	private Integer sourceFileNum; //原文件笔数
	private Integer splitOrderNum; //超过限额明细拆单笔数
	private Integer mergeOrderNum; //产生合并订单数
	private Integer mergeUserNum; //产生合并订单人数
	private String inputPathNo; //批次导入时实际下发公司通道(不能以该字段判断实际下发时的通道)

	/**
	 * 冗余字段
	 * @return
	 */
	private String menuName;//项目明细
	private String agentName;//薪税服务公司名称

	//非数据库字段
	private String realCompanyId;


	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getSupplementServiceFee() {
		return supplementServiceFee;
	}

	public void setSupplementServiceFee(String supplementServiceFee) {
		this.supplementServiceFee = supplementServiceFee;
	}

	public String getReviewor() {
		return reviewor;
	}
	public void setReviewor(String reviewor) {
		this.reviewor = reviewor;
	}
	public String getReviewDesc() {
		return reviewDesc;
	}
	public void setReviewDesc(String reviewDesc) {
		this.reviewDesc = reviewDesc;
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
	public static long getSerialversionuid() {
		return serialVersionUID;
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
	public String getMenuName() {
		return menuName;
	}
	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
    public String getBatchAmount() {
        return batchAmount;
    }
    public void setBatchAmount(String batchAmount) {
        this.batchAmount = batchAmount;
    }

	public String getTaskAttachmentFile() {
		return taskAttachmentFile;
	}

	public void setTaskAttachmentFile(String taskAttachmentFile) {
		this.taskAttachmentFile = taskAttachmentFile;
	}

	public String getYmBatchNo() {
		return ymBatchNo;
	}
	public void setYmBatchNo(String ymBatchNo) {
		this.ymBatchNo = ymBatchNo;
	}

	public String getYmReqId() {
		return ymReqId;
	}

	public void setYmReqId(String ymReqId) {
		this.ymReqId = ymReqId;
	}

	public Integer getLockStatus() {
		return lockStatus;
	}

	public void setLockStatus(Integer lockStatus) {
		this.lockStatus = lockStatus;
	}

	public Integer getSourceFileNum() {
		return sourceFileNum;
	}

	public void setSourceFileNum(Integer sourceFileNum) {
		this.sourceFileNum = sourceFileNum;
	}

	public Integer getSplitOrderNum() {
		return splitOrderNum;
	}

	public void setSplitOrderNum(Integer splitOrderNum) {
		this.splitOrderNum = splitOrderNum;
	}

	public Integer getMergeOrderNum() {
		return mergeOrderNum;
	}

	public void setMergeOrderNum(Integer mergeOrderNum) {
		this.mergeOrderNum = mergeOrderNum;
	}

	public Integer getMergeUserNum() {
		return mergeUserNum;
	}

	public void setMergeUserNum(Integer mergeUserNum) {
		this.mergeUserNum = mergeUserNum;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getInputPathNo() {
		return inputPathNo;
	}

	public void setInputPathNo(String inputPathNo) {
		this.inputPathNo = inputPathNo;
	}

	public String getRealCompanyId() {
		return realCompanyId;
	}

	public void setRealCompanyId(String realCompanyId) {
		this.realCompanyId = realCompanyId;
	}
}
 