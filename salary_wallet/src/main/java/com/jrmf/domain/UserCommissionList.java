package com.jrmf.domain;

import java.util.List;

public class UserCommissionList {

	private List<UserCommissionJson> UserCommissionList;

	public List<UserCommissionJson> getUserCommissionList() {
		return UserCommissionList;
	}

	public void setUserCommissionList(List<UserCommissionJson> userCommissionList) {
		UserCommissionList = userCommissionList;
	}
	
	public UserCommissionList(){};
	
	public UserCommissionList(List<UserCommissionJson> userCommissionList){
		super();
		this.UserCommissionList = userCommissionList;
	} 
}
