package com.jrmf.service;

import com.jrmf.controller.constant.LdBusinessTypeEnum;
import com.jrmf.controller.constant.LdOrderStatusEnum;
import com.jrmf.controller.constant.LdRegisterTypeEnum;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.OrderNoUtil;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jrmf.domain.LdOrderStep;
import com.jrmf.domain.Page;
import com.jrmf.persistence.LdOrderStepDao;

@Service
public class LdOrderStepServiceImpl implements LdOrderStepService{

	private Logger logger = LoggerFactory.getLogger(LdOrderStepServiceImpl.class);

	@Resource
	private LdOrderStepDao ldOrderStepDao;
	
	@Override
	public void insert(LdOrderStep ldOrderStep) {
		ldOrderStepDao.insert(ldOrderStep);
	}

	@Override
	public void update(LdOrderStep ldOrderStep) {
		ldOrderStepDao.update(ldOrderStep);
	}

	@Override
	public List<LdOrderStep> getList(String orderNo) {
		return ldOrderStepDao.getList(orderNo);
	}

	@Override
	public int getCountByOrderNo(String orderno) {
		return ldOrderStepDao.getCountByOrderNo(orderno);
	}

	@Override
	public int getCountSuccessByOrderNo(String orderno) {
		return ldOrderStepDao.getCountSuccessByOrderNo(orderno);
	}

	@Override
	public int getCountFailByOrderNo(String orderno) {
		return ldOrderStepDao.getCountFailByOrderNo(orderno);
	}

	@Override
	public int queryLdStepOrderDetailListCount(Page page) {
		return ldOrderStepDao.queryLdStepOrderDetailListCount(page);
	}

	@Override
	public List<Map<String, Object>> queryLdStepOrderDetailList(Page page) {
		return ldOrderStepDao.queryLdStepOrderDetailList(page);
	}

	@Override
	public LdOrderStep getOrderStep(String stepOrderNo) {
		return ldOrderStepDao.getOrderStep(stepOrderNo);
	}

	@Override
	public void updateById(LdOrderStep ldOrderStepDetail) {
		ldOrderStepDao.updateById(ldOrderStepDetail);
	}

	@Override
	public LdOrderStep getPreStepOrder(Map<String, Object> params) {
		return ldOrderStepDao.getPreStepOrder(params);
	}

	@Override
	public List<LdOrderStep> splitOrderPayment(UserCommission userCommission,
			PaymentConfig paymentConfig,
			String splitAmountMax) {

		List<LdOrderStep> splitOrderList = new LinkedList<>();

		//用于测试模拟失败订单
		String testMode = paymentConfig.getParameter3();
		String account1 = userCommission.getAccount();
		String account2 = userCommission.getAccount();
		if ("0".equals(testMode)) {
			account1 = account1 + testMode;
			account2 = account2 + testMode;
		} else if ("1".equals(testMode)) {
			account1 = account1 + testMode;
		} else if ("2".equals(testMode)) {
			account2 = account2 + testMode;
		}

		LdOrderStep ldOrderStepOne = new LdOrderStep(OrderNoUtil.getOrderNo(),
				LdRegisterTypeEnum.APIPAYMENT.getCode(),
				LdBusinessTypeEnum.SPLITORDER.getCode(),
				userCommission.getOrderNo(), null, account1,
				splitAmountMax, LdOrderStatusEnum.PANDING.getCode(), userCommission.getCompanyId(),
				userCommission.getRealCompanyId(),
				1, paymentConfig.getPathNo(),
				userCommission.getCompanyName(), userCommission.getUserName(),
				DateUtils.getNowDate());

		this.insert(ldOrderStepOne);
		splitOrderList.add(ldOrderStepOne);

		String laveAmount = ArithmeticUtil.subStr2(userCommission.getAmount(), splitAmountMax);

		LdOrderStep ldOrderStepTwo = new LdOrderStep(OrderNoUtil.getOrderNo(),
				LdRegisterTypeEnum.APIPAYMENT.getCode(),
				LdBusinessTypeEnum.SPLITORDER.getCode(),
				userCommission.getOrderNo(), null, account2,
				laveAmount, LdOrderStatusEnum.PANDING.getCode(), userCommission.getCompanyId(),
				userCommission.getRealCompanyId(),
				2, paymentConfig.getPathNo(),
				userCommission.getCompanyName(), userCommission.getUserName(),
				DateUtils.getNowDate());

		this.insert(ldOrderStepTwo);
		splitOrderList.add(ldOrderStepTwo);

		return splitOrderList;
	}


}
