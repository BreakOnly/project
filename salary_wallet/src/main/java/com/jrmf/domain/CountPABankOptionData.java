package com.jrmf.domain;

import com.jrmf.oldsalarywallet.dao.CommissionTemporaryDao;
import com.jrmf.oldsalarywallet.dao.UserCommissionDao;
import com.jrmf.persistence.ChannelHistoryDao;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.utils.ArithmeticUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CountPABankOptionData implements Runnable {
	
	private Logger logger = LoggerFactory.getLogger(CountPABankOptionData.class);
	
	private UserCommissionDao commissionDao;

	private ChannelHistoryService channelHistoryService;
	
	private String batchId;
	
	private CommissionTemporaryDao commissionTemporaryDao;
	
	public CountPABankOptionData(String batchId,UserCommissionDao commissionDao,
			ChannelHistoryService channelHistoryService,CommissionTemporaryDao commissionTemporaryDao) {
		super();
		this.commissionDao = commissionDao;
		this.channelHistoryService = channelHistoryService;
		this.commissionTemporaryDao = commissionTemporaryDao;
		this.batchId = batchId;
	}

	@Override
	public void run() {
		logger.info("-------------------银企直联发放佣金，批次信息汇总处理开始---------------------------");
		List<UserCommission> commissionsByBatchId = commissionDao.getCommissionsByBatchId(batchId);
		int failedNum = 0;//失败总数
		String failedAmountSum = "0.00";//失败总额
		for (UserCommission userCommission : commissionsByBatchId) {
			if(userCommission.getStatus() == 2){
				++failedNum;
				failedAmountSum = ArithmeticUtil.addStr(failedAmountSum, userCommission.getAmount());
			}
		}
		ChannelHistory history = channelHistoryService.getChannelHistoryById(batchId);
		history.setStatus(3);
		history.setFailedNum(failedNum);
		history.setFailedAmount(failedAmountSum);
		channelHistoryService.updateChannelHistory(history);
		/**
		 * 更新临时明细表状态为--已打款
		 */
		commissionTemporaryDao.updateStatusByBatchId(batchId);
		logger.info("-------------------银企直联发放佣金，批次信息汇总处理结束---------------------------");
	}
}
