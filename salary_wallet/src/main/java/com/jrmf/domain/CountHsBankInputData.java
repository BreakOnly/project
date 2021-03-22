package com.jrmf.domain;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrmf.oldsalarywallet.dao.UserCommissionDao;
import com.jrmf.persistence.ChannelHistoryDao;
import com.jrmf.utils.ArithmeticUtil;

public class CountHsBankInputData implements Runnable {
	
	private Logger logger = LoggerFactory.getLogger(CountHsBankInputData.class);
	
	private UserCommissionDao commissionDao;

	private ChannelHistoryDao channelHistoryDao;
	
	private String batchId;
	
	private String customkey;
	
	private String companyId;
	
	public CountHsBankInputData(String batchId,UserCommissionDao commissionDao, 
			ChannelHistoryDao channelHistoryDao, String customkey, String companyId) {
		super();
		this.commissionDao = commissionDao;
		this.channelHistoryDao = channelHistoryDao;
		this.batchId = batchId;
		this.customkey = customkey;
		this.companyId = companyId;
		
	}

	@Override
	public void run() {
		logger.info("-------------------佣金明细处理完成，开始生成批次信息---------------------------");
		List<UserCommission> commissionsByBatchId = commissionDao.getCommissionsByBatchId(batchId);
		int passNum = 0;//成功总数
		String amount = "0.00";//实际下发金额
		String serviceFee = "0.00";//实际服务费
		String serviceMFFee = "0.00";//实际魔方服务费
		int failedNum = 0;//失败总数
		int batchNum = commissionsByBatchId.size();//批次总数
		String batchAmount = "0.00";//批次文件总金额
		String handleAmount = "0.00";//订单应付总额
		String failedAmount = "0.00";
		for (UserCommission userCommission : commissionsByBatchId) {
		    batchAmount = ArithmeticUtil.addStr(batchAmount, userCommission.getAmount());
			if(userCommission.getStatus() == 0){
				++passNum;
				//批次总金额
				amount = ArithmeticUtil.addStr(amount, userCommission.getAmount());
				//批次总服务费
				serviceFee = ArithmeticUtil.addStr(serviceFee,userCommission.getSumFee());
				//批次总利润
				serviceMFFee = ArithmeticUtil.addStr(serviceMFFee, userCommission.getProfiltFree());
			}else {
				++failedNum;
				failedAmount = ArithmeticUtil.addStr(failedAmount, userCommission.getAmount());
			}
		}
		handleAmount = ArithmeticUtil.addStr(amount, serviceFee);
		ChannelHistory history = new ChannelHistory();
		history.setAmount(amount);
		history.setFailedAmount(failedAmount);
		history.setCustomkey(customkey);
		history.setRecCustomkey(companyId);
		history.setOrdername("佣金发放");
		history.setOrderno(batchId);
		history.setStatus(0);
		history.setPayType(1);
		history.setTransfertype(2);
		history.setServiceFee(serviceFee);//服务费
		history.setPassNum(passNum);
		history.setFailedNum(failedNum);
		history.setBatchNum(batchNum);
		history.setBatchAmount(batchAmount);
		history.setHandleAmount(handleAmount);
		channelHistoryDao.addChannelHistory(history);
		logger.info("-------------------批次信息生成结束---------------------------");
	}
}
