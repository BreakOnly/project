package com.jrmf.splitorder.domain;

public class CustomSplitSuccessOrder {
    private Integer id;

    private String splitOrderNo;

    private String splitOrderName;

    private String customKey;

    private String companyId;

    private Integer totalNumber;

    private String totalAmount;

    private String fileName;

    private String fileUrl;

    private Integer hasSubmitPay = 0;

    private Integer hasSynchrodata = 0;

    private String customName;
    private String companyName;
    private Integer customId;
    private Integer payType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSplitOrderNo() {
        return splitOrderNo;
    }

    public void setSplitOrderNo(String splitOrderNo) {
        this.splitOrderNo = splitOrderNo == null ? null : splitOrderNo.trim();
    }

    public String getSplitOrderName() {
        return splitOrderName;
    }

    public void setSplitOrderName(String splitOrderName) {
        this.splitOrderName = splitOrderName == null ? null : splitOrderName.trim();
    }

    public String getCustomKey() {
        return customKey;
    }

    public void setCustomKey(String customKey) {
        this.customKey = customKey == null ? null : customKey.trim();
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId == null ? null : companyId.trim();
    }

    public Integer getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(Integer totalNumber) {
        this.totalNumber = totalNumber;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount == null ? null : totalAmount.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl == null ? null : fileUrl.trim();
    }

    public Integer getHasSubmitPay() {
        return hasSubmitPay;
    }

    public void setHasSubmitPay(Integer hasSubmitPay) {
        this.hasSubmitPay = hasSubmitPay;
    }

    public Integer getHasSynchrodata() {
        return hasSynchrodata;
    }

    public void setHasSynchrodata(Integer hasSynchrodata) {
        this.hasSynchrodata = hasSynchrodata;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getCustomId() {
        return customId;
    }

    public void setCustomId(Integer customId) {
        this.customId = customId;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }
}