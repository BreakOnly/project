package com.jrmf.domain;

import java.io.Serializable;

/**
 * @Title: CompanyNetfileRateConf
 * @Description: 服务公司报税金额信息
 * @create 2019/10/29 17:13
 */
public class CompanyNetfileRateConf implements Serializable {

    private Integer id;

    private String merchantId;

    private int companyId;

    private String companyName;

    private int businessType;

    private Integer gearPosition;

    private String amountStart;

    private String amountEnd;

    private String operator;

    private String gearPositionShorthand;

    private String gearPositionDesc;

    private String costRate;

    private String mfkjCostRate;

    private String createTime;

    private String updateTime;

    private int gearLabel;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getBusinessType() {
        return businessType;
    }

    public void setBusinessType(int businessType) {
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

    public int getGearLabel() {
        return gearLabel;
    }

    public void setGearLabel(int gearLabel) {
        this.gearLabel = gearLabel;
    }
}
