package com.jrmf.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.jrmf.utils.OrderNoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.oldsalarywallet.dao.UserCommissionDao;
import com.jrmf.service.UserSerivce;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil;

public class ExecuteBatchToHsInput implements Runnable {

	private Logger logger = LoggerFactory.getLogger(ExecuteBatchToHsInput.class);

	private CountDownLatch cb;

	private OrderNoUtil orderNoUtil;

	private List<Map<String, String>> param;

	private UserSerivce userSerivce;

	private UserCommissionDao userCommissionDao;
	
	private String batchId;
	
	private String customkey;
	
	private String operatorName;
	
	private ChannelRelated channelRelated;
	
	private BaseInfo baseInfo;
	
	public ExecuteBatchToHsInput(CountDownLatch cb, List<Map<String, String>> param,String operatorName,UserSerivce userSerivce,
			UserCommissionDao userCommissionDao,String batchId, String customkey,ChannelRelated channelRelated, BaseInfo baseInfo) {
		super();
		this.param = param;
		this.userCommissionDao = userCommissionDao;
		this.cb = cb;
		this.batchId = batchId;
		this.customkey = customkey;
		this.operatorName = operatorName;
		this.channelRelated = channelRelated;
		this.baseInfo = baseInfo;
	}

	@Override
	public void run() {
		
		logger.info("------------导入徽商下发佣金明细开始--处理数目"+param.size()+"----------");
		for (Map<String, String> map2 : param) {
			String userName = map2.get("userName");// 收款人真实姓名(必要)
			String cardNo = map2.get("cardNo");// 身份证号码
			String amount = map2.get("amount");//佣金
			if(StringUtil.isEmpty(cardNo) || StringUtil.isEmpty(userName) || StringUtil.isEmpty(amount)){
				createErrorCommion(amount, customkey, "", batchId, "", "", "", "",
						channelRelated.getCompanyId(), "佣金信息不完善","",userName,cardNo);
				continue;
			}
			Map<String,Object> paramMap= new HashMap<String,Object>();
			paramMap.put("certId",cardNo);
			paramMap.put("originalId",customkey);
			paramMap.put("userType","1");//已开户
			paramMap.put("userName",userName);
			/**
			 * 用户是否存在
			 */
			List<User> list = userSerivce.getUserRelatedByParam(paramMap);
			if(list.size()==0){
				createErrorCommion(amount, customkey, "", batchId, "", "", "", "", channelRelated.getCompanyId(),
						"未开通银行电子户","",userName,cardNo);
				continue;
			}
			User user = list.get(0);
			if (Double.parseDouble(amount) > 200000.00) {
				createErrorCommion(amount, customkey, user.getId() + "", batchId, channelRelated.getMerchantId(), "",
						"", "", channelRelated.getCompanyId(), "单笔转账金额超限（大于20万）。", user.getBankNo(), userName,
						user.getCertId());
				continue;
			}
			createSuccessUser(amount, user.getId()+"");
		}
		logger.info("------------导入徽商下发佣金明细结束------------");
		cb.countDown();
	}
	
	private void createErrorCommion(String amount,String customkey,String userId,String batchId,String merchantId,
			String commissionMfkjFree,String serviceRates, String commissionAygFree,
			String companyId,String remark,String alipayAccount,String userName,
			String certId){
		if(StringUtil.isEmpty(userId)){
			User user = new User();
			user.setCertId(certId);
			user.setUserName(userName);
			user.setMerchantId(merchantId);
			user.setUserType(0);//错误信息
			user.setCompanyUserNo(companyId);
			userSerivce.addUser(user);
			userId = user.getId()+"";
		}
		
		UserCommission commission = new UserCommission();
		commission.setAmount(amount);
		commission.setCreatetime(DateUtils.getNowDate());
		commission.setUserId(userId);
		commission.setStatus(2);
		commission.setStatusDesc(remark);
		commission.setBatchId(batchId);
		commission.setCompanyId(companyId);
		commission.setOrderNo(orderNoUtil.getChannelSerialno());
		commission.setProfiltFree(commissionMfkjFree);
		commission.setCalculationRates(serviceRates);
		commission.setSumFee(commissionAygFree);
		commission.setOriginalId(customkey);
		commission.setMerchantId(merchantId);
		commission.setOperatorName(operatorName);
		commission.setPayType(1);
		commission.setAccount(alipayAccount);
		userCommissionDao.addUserCommission(commission);
	}
	
	private void createSuccessUser(String amount ,String userId){
		/**
		 * 服务费计算
		 */
		UserCommission commission = new UserCommission();
		String commissionMfkjFree = "0";
		if(ArithmeticUtil.compareTod(amount, baseInfo.getCalculationLimit())<0){
			commissionMfkjFree = ArithmeticUtil.mulStr(amount, channelRelated.getProfiltLower(), 2);
			commission.setProfilt(channelRelated.getProfiltLower());
		}else{
			commissionMfkjFree = ArithmeticUtil.mulStr(amount, channelRelated.getProfiltUpper(), 2);
			commission.setProfilt(channelRelated.getProfiltUpper());
		}
		//服务费
		String commissionAygFree = ArithmeticUtil.mulStr(amount, channelRelated.getServiceRates(), 2);
    	
		commission.setAmount(amount);
		commission.setCreatetime(DateUtils.getNowDate());
		commission.setUserId(userId);
		commission.setStatus(0);
		commission.setBatchId(batchId);
		commission.setCompanyId(channelRelated.getCompanyId());
		commission.setOrderNo(orderNoUtil.getChannelSerialno());
		commission.setProfiltFree(commissionMfkjFree);
		commission.setCalculationRates(channelRelated.getServiceRates());
		commission.setSumFee(commissionAygFree);
		commission.setOriginalId(customkey);
		commission.setMerchantId(channelRelated.getMerchantId());
		commission.setOperatorName(operatorName);
		commission.setPayType(1);
		userCommissionDao.addUserCommission(commission);
	}

}
