package com.jrmf.splitorder.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class CustomSplitOrder {
    private Integer id;

    private String splitOrderNo;

    private String splitOrderName;

    private String customKey;

    private Integer status;

    private String statusDesc;

    private Integer payType;

    private Integer totalNumber = 0;

    private String totalAmount = "0.0";

    private Integer successNumber = 0;

    private String successAmount = "0.0";

    private Integer failNumber = 0;

    private String failAmount = "0.0";

    private String failFileName;

    private String failFileUrl;

    private Integer laveNumber = 0;

    private String laveAmount = "0.0";

    private String laveFileName;

    private String laveFileUrl;

    private String sourceFileName;

    private String sourceFileUrl;

    private String operatorName;

    private String createTime;

    private String updateTime;

    private String customName;

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

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
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

    public Integer getSuccessNumber() {
        return successNumber;
    }

    public void setSuccessNumber(Integer successNumber) {
        this.successNumber = successNumber;
    }

    public String getSuccessAmount() {
        return successAmount;
    }

    public void setSuccessAmount(String successAmount) {
        this.successAmount = successAmount == null ? null : successAmount.trim();
    }

    public Integer getFailNumber() {
        return failNumber;
    }

    public void setFailNumber(Integer failNumber) {
        this.failNumber = failNumber;
    }

    public String getFailAmount() {
        return failAmount;
    }

    public void setFailAmount(String failAmount) {
        this.failAmount = failAmount == null ? null : failAmount.trim();
    }

    public String getFailFileName() {
        return failFileName;
    }

    public void setFailFileName(String failFileName) {
        this.failFileName = failFileName == null ? null : failFileName.trim();
    }

    public String getFailFileUrl() {
        return failFileUrl;
    }

    public void setFailFileUrl(String failFileUrl) {
        this.failFileUrl = failFileUrl == null ? null : failFileUrl.trim();
    }

    public Integer getLaveNumber() {
        return laveNumber;
    }

    public void setLaveNumber(Integer laveNumber) {
        this.laveNumber = laveNumber;
    }

    public String getLaveAmount() {
        return laveAmount;
    }

    public void setLaveAmount(String laveAmount) {
        this.laveAmount = laveAmount == null ? null : laveAmount.trim();
    }

    public String getLaveFileName() {
        return laveFileName;
    }

    public void setLaveFileName(String laveFileName) {
        this.laveFileName = laveFileName == null ? null : laveFileName.trim();
    }

    public String getLaveFileUrl() {
        return laveFileUrl;
    }

    public void setLaveFileUrl(String laveFileUrl) {
        this.laveFileUrl = laveFileUrl == null ? null : laveFileUrl.trim();
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName == null ? null : sourceFileName.trim();
    }

    public String getSourceFileUrl() {
        return sourceFileUrl;
    }

    public void setSourceFileUrl(String sourceFileUrl) {
        this.sourceFileUrl = sourceFileUrl == null ? null : sourceFileUrl.trim();
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName == null ? null : operatorName.trim();
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

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }
}