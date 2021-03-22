package com.jrmf.domain;

import java.io.Serializable;
import java.util.List;

public class TPolicyGroup implements Serializable {


	private String typeName;
	private List<TPolicyList> policyList;

	public TPolicyGroup() {
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public List<TPolicyList> getPolicyList() {
		return policyList;
	}

	public void setPolicyList(List<TPolicyList> policyList) {
		this.policyList = policyList;
	}

	public TPolicyGroup(String typeName) {
		this.typeName = typeName;
	}
}
