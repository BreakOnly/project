package com.jrmf.domain;

public class LdOrderCorrect {
    private Integer id;

    private String correctOrderNo;

    private String stepOrderNo;

    private String paymentUser;

    private String paymentAccount;

    private String receiveAccount;

    private String receiveUser;

    private String amount;

    private Integer status;

    private String statusDesc;

    private String issuedCompanyid;

    private String pathno;

    private String createTime;

    private String preStepOrder;
    
    private String orderNo;
    
    private Integer businessType;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCorrectOrderNo() {
        return correctOrderNo;
    }

    public void setCorrectOrderNo(String correctOrderNo) {
        this.correctOrderNo = correctOrderNo == null ? null : correctOrderNo.trim();
    }

    public String getStepOrderNo() {
        return stepOrderNo;
    }

    public void setStepOrderNo(String stepOrderNo) {
        this.stepOrderNo = stepOrderNo == null ? null : stepOrderNo.trim();
    }

    public String getPaymentUser() {
        return paymentUser;
    }

    public void setPaymentUser(String paymentUser) {
        this.paymentUser = paymentUser == null ? null : paymentUser.trim();
    }

    public String getPaymentAccount() {
        return paymentAccount;
    }

    public void setPaymentAccount(String paymentAccount) {
        this.paymentAccount = paymentAccount == null ? null : paymentAccount.trim();
    }

    public String getReceiveAccount() {
        return receiveAccount;
    }

    public void setReceiveAccount(String receiveAccount) {
        this.receiveAccount = receiveAccount == null ? null : receiveAccount.trim();
    }

    public String getReceiveUser() {
        return receiveUser;
    }

    public void setReceiveUser(String receiveUser) {
        this.receiveUser = receiveUser == null ? null : receiveUser.trim();
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount == null ? null : amount.trim();
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

    public String getIssuedCompanyid() {
        return issuedCompanyid;
    }

    public void setIssuedCompanyid(String issuedCompanyid) {
        this.issuedCompanyid = issuedCompanyid == null ? null : issuedCompanyid.trim();
    }

    public String getPathno() {
        return pathno;
    }

    public void setPathno(String pathno) {
        this.pathno = pathno == null ? null : pathno.trim();
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime == null ? null : createTime.trim();
    }

    public String getPreStepOrder() {
        return preStepOrder;
    }

    public void setPreStepOrder(String preStepOrder) {
        this.preStepOrder = preStepOrder == null ? null : preStepOrder.trim();
    }

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public Integer getBusinessType() {
		return businessType;
	}

	public void setBusinessType(Integer businessType) {
		this.businessType = businessType;
	}
	
}