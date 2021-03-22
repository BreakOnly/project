package com.jrmf.api;

import java.io.Serializable;

import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;

public class PaymentRequest implements Serializable {
	
	private PaymentConfig paymentConfig; 
	
	private UserCommission userCommission;

	public PaymentConfig getPaymentConfig() {
		return paymentConfig;
	}

	public void setPaymentConfig(PaymentConfig paymentConfig) {
		this.paymentConfig = paymentConfig;
	}

	public UserCommission getUserCommission() {
		return userCommission;
	}

	public void setUserCommission(UserCommission userCommission) {
		this.userCommission = userCommission;
	}
}
