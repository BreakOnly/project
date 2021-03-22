package com.jrmf.domain;

import java.io.Serializable;
import java.util.List;

public class CommissionGroup implements Serializable {

	private static final long serialVersionUID = -5968712208853308089L;

	private String certId;
	private List<TempCommission> commissionList;

	public String getCertId() {
		return certId;
	}

	public void setCertId(String certId) {
		this.certId = certId;
	}

	public List<TempCommission> getCommissionList() {
		return commissionList;
	}

	public void setCommissionList(List<TempCommission> commissionList) {
		this.commissionList = commissionList;
	}

}
