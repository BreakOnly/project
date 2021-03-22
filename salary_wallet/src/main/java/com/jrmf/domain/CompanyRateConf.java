package com.jrmf.domain;

public class CompanyRateConf {

    private Integer id;
    // 平台名称
    private String merchantId;
    // 服务公司Id
    private Integer companyId;
    // 业务类型
    private Integer businessType;
    // 挡位
    private Integer gearPosition;
    // 金额起始
    private String amountStart;
    // 金额结束
    private String amountEnd;
    // 运算符
    private String operator;
    // 挡位标签简写
    private String gearPositionShorthand;
    // 挡位描述
    private String gearPositionDesc;
    // 成本费率
    private String costRate;
    // 魔方成本费率
    private String mfkjCostRate;
    // 创建时间
    private String createTime;
    // 最后一次更新时间
    private String updateTime;
    // 预留字段1
    private String reserved1;
    // 预留字段2
    private String reserved2;
    // 档位所属组
    private String gearGroup;
    // 服务公司名称
    private String companyName;
    // 档位金额报税标签 1:小金额 2：大金额
    private int gearLabel;
    /**
     * 报税id
     */
    private int netfileId;

    public int getNetfileId() {
        return netfileId;
    }

    public void setNetfileId(int netfileId) {
        this.netfileId = netfileId;
    }

    public int getGearLabel() {
        return gearLabel;
    }

    public void setGearLabel(int gearLabel) {
        this.gearLabel = gearLabel;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getId() {
        return id;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
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

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
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

    public String getGearPositionShorthand() {
        return gearPositionShorthand;
    }

    public void setGearPositionShorthand(String gearPositionShorthand) {
        this.gearPositionShorthand = gearPositionShorthand;
    }

    public String getGearPositionDesc() {
        return gearPositionDesc;
    }

    public void setGearPositionDesc(String gearPositionDesc) {
        this.gearPositionDesc = gearPositionDesc;
    }

    public String getCostRate() {
        return costRate;
    }

    public void setCostRate(String costRate) {
        this.costRate = costRate;
    }

    public String getMfkjCostRate() {
        return mfkjCostRate;
    }

    public void setMfkjCostRate(String mfkjCostRate) {
        this.mfkjCostRate = mfkjCostRate;
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

    public String getGearGroup() {
        return gearGroup;
    }

    public void setGearGroup(String gearGroup) {
        this.gearGroup = gearGroup;
    }

    @Override
    public String toString() {
        return "CompanyRateConf{" +
                "id=" + id +
                ", merchantId='" + merchantId + '\'' +
                ", companyId=" + companyId +
                ", businessType=" + businessType +
                ", gearPosition=" + gearPosition +
                ", amountStart='" + amountStart + '\'' +
                ", amountEnd='" + amountEnd + '\'' +
                ", operator='" + operator + '\'' +
                ", gearPositionShorthand='" + gearPositionShorthand + '\'' +
                ", gearPositionDesc='" + gearPositionDesc + '\'' +
                ", costRate='" + costRate + '\'' +
                ", mfkjCostRate='" + mfkjCostRate + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", reserved1='" + reserved1 + '\'' +
                ", reserved2='" + reserved2 + '\'' +
                ", gearGroup='" + gearGroup + '\'' +
                ", companyName='" + companyName + '\'' +
                ", gearLabel=" + gearLabel +
                ", netfileId=" + netfileId +
                '}';
    }
}
