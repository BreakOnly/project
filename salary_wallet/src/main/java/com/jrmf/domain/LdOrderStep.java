package com.jrmf.domain;

public class LdOrderStep {
	private Integer id;

	private String stepOrderNo;

	private Integer registerType;

	private Integer businessType;

	private String orderno;

	private String paymentAccount;

	private String receiveAccount;

	private String amount;

	private Integer status;
	
	private String statusDesc;

	private String issuedCompanyid;

	private String pathno;

	private Integer stepOrder;

	private String aisleInfo;
	 
	private String paymentUser;
	
	private String receiveUser;
	
	private String createTime;
	
	private String preStepOrder;
	
	private Integer isCorrect;
	
	private Integer correctStatus;

	private String issuedRealCompanyId;
	
	public LdOrderStep() {
		super();
	}

	public LdOrderStep(String stepOrderNo, Integer registerType,
			Integer businessType, String orderno, String paymentAccount,
			String receiveAccount, String amount, Integer status,
			String issuedCompanyid, Integer stepOrder,String pathno,String paymentUser,String receiveUser,String createTime) {
		super();
		this.stepOrderNo = stepOrderNo;
		this.registerType = registerType;
		this.businessType = businessType;
		this.orderno = orderno;
		this.paymentAccount = paymentAccount;
		this.receiveAccount = receiveAccount;
		this.amount = amount;
		this.status = status;
		this.issuedCompanyid = issuedCompanyid;
		this.stepOrder = stepOrder;
		this.pathno=pathno;
		this.paymentUser=paymentUser;
		this.receiveUser=receiveUser;
		this.createTime=createTime;
	}

	public LdOrderStep(String stepOrderNo, Integer registerType,
			Integer businessType, String orderno, String paymentAccount,
			String receiveAccount, String amount, Integer status,
			String issuedCompanyid, String issuedRealCompanyId, Integer stepOrder, String pathno,
			String paymentUser, String receiveUser, String createTime) {
		super();
		this.stepOrderNo = stepOrderNo;
		this.registerType = registerType;
		this.businessType = businessType;
		this.orderno = orderno;
		this.paymentAccount = paymentAccount;
		this.receiveAccount = receiveAccount;
		this.amount = amount;
		this.status = status;
		this.issuedCompanyid = issuedCompanyid;
		this.stepOrder = stepOrder;
		this.pathno = pathno;
		this.paymentUser = paymentUser;
		this.receiveUser = receiveUser;
		this.createTime = createTime;
		this.issuedRealCompanyId = issuedRealCompanyId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getStepOrderNo() {
		return stepOrderNo;
	}

	public void setStepOrderNo(String stepOrderNo) {
		this.stepOrderNo = stepOrderNo;
	}

	public Integer getRegisterType() {
		return registerType;
	}

	public void setRegisterType(Integer registerType) {
		this.registerType = registerType;
	}

	public Integer getBusinessType() {
		return businessType;
	}

	public void setBusinessType(Integer businessType) {
		this.businessType = businessType;
	}

	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public String getPaymentAccount() {
		return paymentAccount;
	}

	public void setPaymentAccount(String paymentAccount) {
		this.paymentAccount = paymentAccount;
	}

	public String getReceiveAccount() {
		return receiveAccount;
	}

	public void setReceiveAccount(String receiveAccount) {
		this.receiveAccount = receiveAccount;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
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
		this.statusDesc = statusDesc;
	}

	public String getIssuedCompanyid() {
		return issuedCompanyid;
	}

	public void setIssuedCompanyid(String issuedCompanyid) {
		this.issuedCompanyid = issuedCompanyid;
	}

	public String getPathno() {
		return pathno;
	}

	public void setPathno(String pathno) {
		this.pathno = pathno;
	}

	public Integer getStepOrder() {
		return stepOrder;
	}

	public void setStepOrder(Integer stepOrder) {
		this.stepOrder = stepOrder;
	}

	public String getAisleInfo() {
		return aisleInfo;
	}

	public void setAisleInfo(String aisleInfo) {
		this.aisleInfo = aisleInfo;
	}

	public String getPaymentUser() {
		return paymentUser;
	}

	public void setPaymentUser(String paymentUser) {
		this.paymentUser = paymentUser;
	}

	public String getReceiveUser() {
		return receiveUser;
	}

	public void setReceiveUser(String receiveUser) {
		this.receiveUser = receiveUser;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getPreStepOrder() {
		return preStepOrder;
	}

	public void setPreStepOrder(String preStepOrder) {
		this.preStepOrder = preStepOrder;
	}

	public Integer getIsCorrect() {
		return isCorrect;
	}

	public void setIsCorrect(Integer isCorrect) {
		this.isCorrect = isCorrect;
	}

	public Integer getCorrectStatus() {
		return correctStatus;
	}

	public void setCorrectStatus(Integer correctStatus) {
		this.correctStatus = correctStatus;
	}

	public String getIssuedRealCompanyId() {
		return issuedRealCompanyId;
	}

	public void setIssuedRealCompanyId(String issuedRealCompanyId) {
		this.issuedRealCompanyId = issuedRealCompanyId;
	}

	public UserCommission toUserCommission(UserCommission commission) {
		UserCommission userCommission = new UserCommission();
		userCommission.setAccount(this.getReceiveAccount());
		userCommission.setAmount(this.getAmount());
		userCommission.setUserName(this.getReceiveUser());
		userCommission.setBankName(commission.getBankName());
		userCommission.setBankNo(commission.getBankNo());
		userCommission.setCompanyId(this.getIssuedCompanyid());
		userCommission.setOriginalId(commission.getOriginalId());
		userCommission.setOrderNo(this.getStepOrderNo());

		return userCommission;
	}

}