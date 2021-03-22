package com.jrmf.payment.execute;

import java.util.List;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.UserCommission;
import com.jrmf.persistence.CommissionTemporary2Dao;
import com.jrmf.persistence.UserCommission2Dao;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.utils.ArithmeticUtil;

public class CountOptionData2 implements Runnable {
	
	private Logger logger = LoggerFactory.getLogger(CountOptionData2.class);
	public static final String PROCESS = "process";
	
	private String processId;
	
	private UserCommission2Dao commissionDao2;

	private ChannelHistoryService channelHistoryService;
	
	private String batchId;
	
	private CommissionTemporary2Dao commissionTemporaryDao2;
	
	public CountOptionData2(String processId,
			String batchId,
			UserCommission2Dao commissionDao2,
			ChannelHistoryService channelHistoryService,
			CommissionTemporary2Dao commissionTemporaryDao2) {
		super();
		this.processId = processId;
		this.commissionDao2 = commissionDao2;
		this.channelHistoryService = channelHistoryService;
		this.commissionTemporaryDao2 = commissionTemporaryDao2;
		this.batchId = batchId;
	}

	@Override
	public void run() {
		MDC.put(PROCESS, processId);
		logger.info("-------------------发放佣金，批次信息汇总处理开始---------------------------");
		List<UserCommission> commissionsByBatchId = commissionDao2.getCommissionsByBatchId(batchId);
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
		commissionTemporaryDao2.updateStatusByBatchId(batchId);
		
		logger.info("-------------------发放佣金，批次信息汇总处理结束---------------------------");
		MDC.remove(PROCESS);
	}
}
