package com.jrmf.domain;

import java.util.Date;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2019/1/22 11:50
 * Version:1.0
 * @author guoto
 */
public class ReceiptBatch {
    private Integer id;
    /**
     * 回单日期
     **/
    private String receiptTime;
    /**
     * 处理状态
     **/
    private Integer status;
    /**
     * 服务公司customKey
     **/
    private Integer companyId;
    private String companyName;
    private Integer payType;
    /**
     * 回单机构凭证类型
     **/
    private String receiptOrgType;
    /**
     * 回单凭证机构名称
     **/
    private String receiptOrgName;
    /**
     * 成功交易笔数
     **/
    private String commissionNum;
    /**
     * 回单文件笔数
     **/
    private String receiptNum;
    /**
     * 回单匹配笔数
     **/
    private String receiptMatchNum;
    /**
     * 回单处理方式
     **/
    private String receiptType;
    /**
     * 创建时间
     **/
    private String createTime;
    /**
     * 最后一次更新时间
     **/
    private String updateTime;
    
    private String merchantId;
    private Integer receiptImportType;

    public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReceiptTime() {
        return receiptTime;
    }

    public void setReceiptTime(String receiptTime) {
        this.receiptTime = receiptTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public String getReceiptOrgType() {
		return receiptOrgType;
	}

	public void setReceiptOrgType(String receiptOrgType) {
		this.receiptOrgType = receiptOrgType;
	}

	public String getReceiptOrgName() {
		return receiptOrgName;
	}

	public void setReceiptOrgName(String receiptOrgName) {
		this.receiptOrgName = receiptOrgName;
	}

	public String getCommissionNum() {
        return commissionNum;
    }

    public void setCommissionNum(String commissionNum) {
        this.commissionNum = commissionNum;
    }

    public String getReceiptNum() {
        return receiptNum;
    }

    public void setReceiptNum(String receiptNum) {
        this.receiptNum = receiptNum;
    }

    public String getReceiptMatchNum() {
        return receiptMatchNum;
    }

    public void setReceiptMatchNum(String receiptMatchNum) {
        this.receiptMatchNum = receiptMatchNum;
    }

    public String getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(String receiptType) {
        this.receiptType = receiptType;
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

    public Integer getReceiptImportType() {
        return receiptImportType;
    }

    public void setReceiptImportType(Integer receiptImportType) {
        this.receiptImportType = receiptImportType;
    }

    @Override
    public String toString() {
        return "ReceiptBatch{" +
                "id=" + id +
                ", receiptTime=" + receiptTime +
                ", status=" + status +
                ", companyId=" + companyId +
                ", payType=" + payType +
                ", receiptOrgType=" + receiptOrgType +
                ", receiptOrgName='" + receiptOrgName + '\'' +
                ", commissionNum=" + commissionNum +
                ", receiptNum=" + receiptNum +
                ", receiptMatchNum=" + receiptMatchNum +
                ", receiptType=" + receiptType +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", merchantId='" + merchantId + '\'' +
                '}';
    }
}
