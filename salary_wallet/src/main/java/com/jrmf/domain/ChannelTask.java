package com.jrmf.domain;


import io.swagger.annotations.ApiModelProperty;

public class ChannelTask {

  private Integer id;

  /**
   * 任务所属商户ID
   */
  private String customKey;

  /**
   * 任务所属下发公司ID
   */
  private String companyId;

  /**
   * 任务名称
   */
  private String taskName;

  /**
   * 任务划分种类 01-公共展示任务（站点关联）\r\n02-资金结算自动生成的任务(商户关联)\r\n03-商户主动导入的任务\r\n04-用户主动报名任务 05-商户任务资源池
   */
  private Integer taskPartition;

  /**
   * 所属站点
   */
  private String website;

  /**
   * 承接人名字
   */
  private String undertakerName;

  /**
   * 承接人证件号
   */
  private String undertakerCertId;

  /**
   * 任务状态 已完成结算、进行中、已报名、待录用、已录用、待到岗、已到岗、待支付、待匹配确认
   */
  private Integer status;

  /**
   * 状态描述
   */
  private String statusDesc;

  /**
   * 任务类型 关联channel_task_type id\r\n
   */
  private Integer taskType;

  /**
   * 任务描述
   */
  private String taskDesc;

  /**
   * 发布时间
   */
  private String publishTime;

  /**
   * 开始时间
   */
  private String startTime;

  /**
   * 结束时间
   */
  private String endTime;

  /**
   * 单位价格
   */
  private String unitPrice;

  /**
   * 单位标签
   */
  private String unitTab;

  /**
   * 结算金额
   */
  private String taskAmount;

  /**
   * 任务业绩量
   */
  private String taskAchievement;

  /**
   * 绩效费
   */
  private String achievementFee;

  /**
   * 其他费用
   */
  private String otherFee;

  /**
   * 佣金
   */
  private String commissionFee;

  /**
   * 任务工作局域
   */
  private String wordArea;

  /**
   * 关联channel_user表 id
   */
  private Integer userId;

  /**
   * 联系电话
   */
  private String linkPhoneNo;

  /**
   * 行政区划代码
   */
  private String regionCode;

  /**
   * 行政区划名称
   */
  private String regionName;

  /**
   * 市
   */
  private String cityCode;

  /**
   * 市
   */
  private String cityName;

  /**
   * 区县
   */
  private String countyCode;

  /**
   * 区县
   */
  private String countyName;

  /**
   * 用户主动报名任务时需要一个父级任务id
   */
  private Integer parentId;

  /**
   * 数据库表里无该字段
   */
  private String typeName;

  /**
   * 数据库表里无该字段
   */
  private String customName;

  /**
   * 关联下发订单号
   */
  private String orderNo;

  /**
   * 付款时间
   */
  private String paymentTime;

  /**
   * 业务订单号
   */
  private String customOrderNo;

  /**
   * 支付方式
   */
  private Integer payType;

  /**
   * 银行卡号
   */
  private String account;

  /**
   * 所属银行机构
   */
  private String bankName;

  /**
   * 服务公司名称
   */
  private String companyName;

  /**
   * 身份类型
   */
  private Integer documentType;

  /**
   * 限制人数
   */
  private Integer limitNumber;

  /**
   * 创建时间
   */
  private String createTime;

  /**
   * 最后修改时间
   */
  private String lastUpdateTime;

  /**
   * 操作员
   */
  private String operatorName;

  /**
   * 任务所属商户ID
   */
  private String customkey;

  /**
   * 开始金额
   */
  private String startAmount;

  /**
   * 结束金额
   */
  private String endAmount;

  @ApiModelProperty(name = "bizType", value = "业务类型 1普通任务项目 2个体户项目", required = false)
  private Byte bizType;
  @ApiModelProperty(name = "ecoCateCode", value = "项目类型编码", required = false)
  private String ecoCateCode;
  @ApiModelProperty(name = "ecoCateLevelCode", value = "项目类型层级编码", required = false)
  private String ecoCateLevelCode;
  @ApiModelProperty(name = "ecoCateName", value = "项目类型名称", required = false)
  private String ecoCateName;
  @ApiModelProperty(name = "invoiceType", value = "发票类型 1增值税票普通发票2增值税专用发票", required = false)
  private Byte invoiceType;
  @ApiModelProperty(name = "contractFileUrl", value = "合同附件url", required = false)
  private String contractFileUrl;
  @ApiModelProperty(name = "invoiceCategoryName", value = "开票信息名称", required = false)
  private String invoiceCategoryName;
  @ApiModelProperty(name = "fullInvoiceCategoryName", value = "开票信息名称全称", required = false)
  private String fullInvoiceCategoryName;
  @ApiModelProperty(name = "fullEcoCateName", value = "项目类型名称全称", required = false)
  private String fullEcoCateName;
  @ApiModelProperty(name = "firmId", value = "发包商编号", required = false)
  private String firmId;
  @ApiModelProperty(name = "contractFileName", value = "合同附件名称", required = false)
  private String contractFileName;

  @ApiModelProperty(name = "invoiceDetail", value = "开票明细", required = false)
  private String invoiceDetail;

  private String platsrl;

  private String bidno;

  private Boolean deletedFlag;

  public Boolean getDeletedFlag() {
    return deletedFlag;
  }

  public void setDeletedFlag(Boolean deletedFlag) {
    this.deletedFlag = deletedFlag;
  }

  public String getPlatsrl() {
    return platsrl;
  }

  public void setPlatsrl(String platsrl) {
    this.platsrl = platsrl;
  }

  public String getBidno() {
    return bidno;
  }

  public void setBidno(String bidno) {
    this.bidno = bidno;
  }

  public String getContractFileName() {
    return contractFileName;
  }

  public void setContractFileName(String contractFileName) {
    this.contractFileName = contractFileName;
  }

  public String getFirmId() {
    return firmId;
  }

  public void setFirmId(String firmId) {
    this.firmId = firmId;
  }

  public Byte getBizType() {
    return bizType;
  }

  public void setBizType(Byte bizType) {
    this.bizType = bizType;
  }

  public String getEcoCateCode() {
    return ecoCateCode;
  }

  public void setEcoCateCode(String ecoCateCode) {
    this.ecoCateCode = ecoCateCode;
  }

  public String getEcoCateLevelCode() {
    return ecoCateLevelCode;
  }

  public void setEcoCateLevelCode(String ecoCateLevelCode) {
    this.ecoCateLevelCode = ecoCateLevelCode;
  }

  public String getEcoCateName() {
    return ecoCateName;
  }

  public void setEcoCateName(String ecoCateName) {
    this.ecoCateName = ecoCateName;
  }

  public Byte getInvoiceType() {
    return invoiceType;
  }

  public void setInvoiceType(Byte invoiceType) {
    this.invoiceType = invoiceType;
  }

  public String getContractFileUrl() {
    return contractFileUrl;
  }

  public void setContractFileUrl(String contractFileUrl) {
    this.contractFileUrl = contractFileUrl;
  }

  public String getInvoiceCategoryName() {
    return invoiceCategoryName;
  }

  public void setInvoiceCategoryName(String invoiceCategoryName) {
    this.invoiceCategoryName = invoiceCategoryName;
  }

  public String getInvoiceDetail() {
    return invoiceDetail;
  }

  public void setInvoiceDetail(String invoiceDetail) {
    this.invoiceDetail = invoiceDetail;
  }

  public ChannelTask() {
  }

  public ChannelTask(ChannelTask task) {
    this.customKey = task.getCustomKey();
    this.companyId = task.getCompanyId();
    this.taskName = task.getTaskName();
    this.taskPartition = task.getTaskPartition();
    this.website = task.getWebsite();
    this.undertakerName = task.getUndertakerName();
    this.undertakerCertId = task.getUndertakerCertId();
    this.status = task.getStatus();
    this.statusDesc = task.getStatusDesc();
    this.taskType = task.getTaskType();
    this.publishTime = task.getPublishTime();
    this.startTime = task.getStartTime();
    this.endTime = task.getEndTime();
    this.unitPrice = task.getUnitPrice();
    this.unitTab = task.getUnitTab();
    this.taskAmount = task.getTaskAmount();
    this.taskAchievement = task.getTaskAchievement();
    this.achievementFee = task.getAchievementFee();
    this.otherFee = task.getOtherFee();
    this.commissionFee = task.getCommissionFee();
    this.wordArea = task.getWordArea();
    this.userId = task.getUserId();
    this.linkPhoneNo = task.getLinkPhoneNo();
    this.regionCode = task.getRegionCode();
    this.regionName = task.getRegionName();
    this.cityCode = task.getCityCode();
    this.cityName = task.getCityName();
    this.countyCode = task.getCountyCode();
    this.countyName = task.getCountyName();
    this.parentId = task.getParentId();
    this.orderNo = task.getOrderNo();
    this.fullEcoCateName = task.fullEcoCateName;
    this.fullInvoiceCategoryName = task.getFullInvoiceCategoryName();
    this.invoiceDetail = task.getInvoiceDetail();
  }

  public String getFullInvoiceCategoryName() {
    return fullInvoiceCategoryName;
  }

  public void setFullInvoiceCategoryName(String fullInvoiceCategoryName) {
    this.fullInvoiceCategoryName = fullInvoiceCategoryName;
  }

  public String getFullEcoCateName() {
    return fullEcoCateName;
  }

  public void setFullEcoCateName(String fullEcoCateName) {
    this.fullEcoCateName = fullEcoCateName;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getCustomKey() {
    return customKey;
  }

  public void setCustomKey(String customKey) {
    this.customKey = customKey == null ? null : customKey.trim();
    this.customkey = customKey;
  }

  public String getCompanyId() {
    return companyId;
  }

  public void setCompanyId(String companyId) {
    this.companyId = companyId == null ? null : companyId.trim();
  }

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName == null ? null : taskName.trim();
  }

  public Integer getTaskPartition() {
    return taskPartition;
  }

  public void setTaskPartition(Integer taskPartition) {
    this.taskPartition = taskPartition;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website == null ? null : website.trim();
  }

  public String getUndertakerName() {
    return undertakerName;
  }

  public void setUndertakerName(String undertakerName) {
    this.undertakerName = undertakerName == null ? null : undertakerName.trim();
  }

  public String getUndertakerCertId() {
    return undertakerCertId;
  }

  public void setUndertakerCertId(String undertakerCertId) {
    this.undertakerCertId = undertakerCertId == null ? null : undertakerCertId.trim();
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getStatusDesc() {
    return statusDesc;
  }

  public void setStatusDesc(String statusDesc) {
    this.statusDesc = statusDesc == null ? null : statusDesc.trim();
  }

  public Integer getTaskType() {
    return taskType;
  }

  public void setTaskType(Integer taskType) {
    this.taskType = taskType;
  }

  public String getPublishTime() {
    return publishTime;
  }

  public void setPublishTime(String publishTime) {
    this.publishTime = publishTime == null ? null : publishTime.trim();
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime == null ? null : startTime.trim();
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime == null ? null : endTime.trim();
  }

  public String getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(String unitPrice) {
    this.unitPrice = unitPrice == null ? null : unitPrice.trim();
  }

  public String getUnitTab() {
    return unitTab;
  }

  public void setUnitTab(String unitTab) {
    this.unitTab = unitTab == null ? null : unitTab.trim();
  }

  public String getTaskAmount() {
    return taskAmount;
  }

  public void setTaskAmount(String taskAmount) {
    this.taskAmount = taskAmount == null ? null : taskAmount.trim();
  }

  public String getTaskAchievement() {
    return taskAchievement;
  }

  public void setTaskAchievement(String taskAchievement) {
    this.taskAchievement = taskAchievement == null ? null : taskAchievement.trim();
  }

  public String getAchievementFee() {
    return achievementFee;
  }

  public void setAchievementFee(String achievementFee) {
    this.achievementFee = achievementFee == null ? null : achievementFee.trim();
  }

  public String getOtherFee() {
    return otherFee;
  }

  public void setOtherFee(String otherFee) {
    this.otherFee = otherFee == null ? null : otherFee.trim();
  }

  public String getCommissionFee() {
    return commissionFee;
  }

  public void setCommissionFee(String commissionFee) {
    this.commissionFee = commissionFee == null ? null : commissionFee.trim();
  }

  public String getWordArea() {
    return wordArea;
  }

  public void setWordArea(String wordArea) {
    this.wordArea = wordArea == null ? null : wordArea.trim();
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public String getLinkPhoneNo() {
    return linkPhoneNo;
  }

  public void setLinkPhoneNo(String linkPhoneNo) {
    this.linkPhoneNo = linkPhoneNo == null ? null : linkPhoneNo.trim();
  }

  public String getRegionCode() {
    return regionCode;
  }

  public void setRegionCode(String regionCode) {
    this.regionCode = regionCode;
  }

  public String getRegionName() {
    return regionName;
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public String getCityCode() {
    return cityCode;
  }

  public void setCityCode(String cityCode) {
    this.cityCode = cityCode;
  }

  public String getCityName() {
    return cityName;
  }

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }

  public String getCountyCode() {
    return countyCode;
  }

  public void setCountyCode(String countyCode) {
    this.countyCode = countyCode;
  }

  public String getCountyName() {
    return countyName;
  }

  public void setCountyName(String countyName) {
    this.countyName = countyName;
  }

  public Integer getParentId() {
    return parentId;
  }

  public void setParentId(Integer parentId) {
    this.parentId = parentId;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public String getCustomName() {
    return customName;
  }

  public void setCustomName(String customName) {
    this.customName = customName;
  }

  public String getOrderNo() {
    return orderNo;
  }

  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
  }

  public String getPaymentTime() {
    return paymentTime;
  }

  public void setPaymentTime(String paymentTime) {
    this.paymentTime = paymentTime;
  }

  public String getCustomOrderNo() {
    return customOrderNo;
  }

  public void setCustomOrderNo(String customOrderNo) {
    this.customOrderNo = customOrderNo;
  }

  public String getTaskDesc() {
    return taskDesc;
  }

  public void setTaskDesc(String taskDesc) {
    this.taskDesc = taskDesc;
  }

  public Integer getPayType() {
    return payType;
  }

  public void setPayType(Integer payType) {
    this.payType = payType;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getBankName() {
    return bankName;
  }

  public void setBankName(String bankName) {
    this.bankName = bankName;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public Integer getDocumentType() {
    return documentType;
  }

  public void setDocumentType(Integer documentType) {
    this.documentType = documentType;
  }

  public Integer getLimitNumber() {
    return limitNumber;
  }

  public void setLimitNumber(Integer limitNumber) {
    this.limitNumber = limitNumber;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(String lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public String getOperatorName() {
    return operatorName;
  }

  public void setOperatorName(String operatorName) {
    this.operatorName = operatorName;
  }

  public String getCustomkey() {
    return customkey;
  }

  public void setCustomkey(String customkey) {
    this.customkey = customkey;
  }

  public String getStartAmount() {
    return startAmount;
  }

  public void setStartAmount(String startAmount) {
    this.startAmount = startAmount;
  }

  public String getEndAmount() {
    return endAmount;
  }

  public void setEndAmount(String endAmount) {
    this.endAmount = endAmount;
  }

  @Override
  public String toString() {
    return "ChannelTask{" +
        "id=" + id +
        ", customKey='" + customKey + '\'' +
        ", companyId='" + companyId + '\'' +
        ", taskName='" + taskName + '\'' +
        ", taskPartition=" + taskPartition +
        ", website='" + website + '\'' +
        ", undertakerName='" + undertakerName + '\'' +
        ", undertakerCertId='" + undertakerCertId + '\'' +
        ", status=" + status +
        ", statusDesc='" + statusDesc + '\'' +
        ", taskType=" + taskType +
        ", taskDesc='" + taskDesc + '\'' +
        ", publishTime='" + publishTime + '\'' +
        ", startTime='" + startTime + '\'' +
        ", endTime='" + endTime + '\'' +
        ", unitPrice='" + unitPrice + '\'' +
        ", unitTab='" + unitTab + '\'' +
        ", taskAmount='" + taskAmount + '\'' +
        ", taskAchievement='" + taskAchievement + '\'' +
        ", achievementFee='" + achievementFee + '\'' +
        ", otherFee='" + otherFee + '\'' +
        ", commissionFee='" + commissionFee + '\'' +
        ", wordArea='" + wordArea + '\'' +
        ", userId=" + userId +
        ", linkPhoneNo='" + linkPhoneNo + '\'' +
        ", regionCode='" + regionCode + '\'' +
        ", regionName='" + regionName + '\'' +
        ", cityCode='" + cityCode + '\'' +
        ", cityName='" + cityName + '\'' +
        ", countyCode='" + countyCode + '\'' +
        ", countyName='" + countyName + '\'' +
        ", parentId=" + parentId +
        ", typeName='" + typeName + '\'' +
        ", customName='" + customName + '\'' +
        ", orderNo='" + orderNo + '\'' +
        ", paymentTime='" + paymentTime + '\'' +
        ", customOrderNo='" + customOrderNo + '\'' +
        ", payType=" + payType +
        ", account='" + account + '\'' +
        ", bankName='" + bankName + '\'' +
        ", companyName='" + companyName + '\'' +
        ", documentType=" + documentType +
        ", limitNumber=" + limitNumber +
        ", createTime='" + createTime + '\'' +
        ", lastUpdateTime='" + lastUpdateTime + '\'' +
        ", operatorName='" + operatorName + '\'' +
        ", customkey='" + customkey + '\'' +
        ", startAmount='" + startAmount + '\'' +
        ", endAmount='" + endAmount + '\'' +
        ", bizType=" + bizType +
        ", ecoCateCode='" + ecoCateCode + '\'' +
        ", ecoCateLevelCode='" + ecoCateLevelCode + '\'' +
        ", ecoCateName='" + ecoCateName + '\'' +
        ", invoiceType=" + invoiceType +
        ", contractFileUrl='" + contractFileUrl + '\'' +
        ", invoiceCategoryName='" + invoiceCategoryName + '\'' +
        ", firmId='" + firmId + '\'' +
        ", contractFileName='" + contractFileName + '\'' +
        ", platsrl='" + platsrl + '\'' +
        ", bidno='" + bidno + '\'' +
        ", deletedFlag=" + deletedFlag +
        '}';
  }
}
