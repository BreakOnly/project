package com.jrmf.service;

import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import java.util.List;
import java.util.Map;

import com.jrmf.domain.LdOrderStep;
import com.jrmf.domain.Page;

public interface LdOrderStepService {
	
	public void insert(LdOrderStep ldOrderStep);
	
	public void update(LdOrderStep ldOrderStep);
	
	public List<LdOrderStep> getList(String orderNo);

	public int getCountByOrderNo(String orderno);

	public int getCountSuccessByOrderNo(String orderno);

	public int getCountFailByOrderNo(String orderno);

	public int queryLdStepOrderDetailListCount(Page page);

	public List<Map<String, Object>> queryLdStepOrderDetailList(Page page);

	public LdOrderStep getOrderStep(String stepOrderNo);

	public void updateById(LdOrderStep ldOrderStepDetail);

	public LdOrderStep getPreStepOrder(Map<String, Object> params);

	List<LdOrderStep> splitOrderPayment(UserCommission commission, PaymentConfig paymentConfig,
			String splitAmountMax);

}