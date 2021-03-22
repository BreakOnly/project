package com.jrmf.domain;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.utils.ArithmeticUtil;

public class CountAliPayData implements Runnable {
	
	private Logger logger = LoggerFactory.getLogger(CountAliPayData.class);
	
	private UserCommissionService commissionService;

	private ChannelHistoryService channelHistoryService;
	
	private String batchId;
	
	public CountAliPayData(String batchId,UserCommissionService commissionService, ChannelHistoryService channelHistoryService) {
		super();
		this.commissionService = commissionService;
		this.channelHistoryService = channelHistoryService;
		this.batchId = batchId;
	}

	@Override
	public void run() {
		//查询支付宝下发实际情况，修改channleHistory表数据
		List<UserCommission> commissionsByBatchId = commissionService.getCommissionsByBatchId(batchId);
		int passNum = 0;//成功总数
		String amount = "0.00";//实际下发金额
		String serviceFee = "0.00";//实际服务费
		String serviceMFFee = "0.00";//实际魔方服务费
		int failedNum = 0;//失败总数
		int batchNum = commissionsByBatchId.size();//批次总数
		String handleAmount = "0.00";//订单应付总额
		String batchAmount = "0.00";//批次文件总金额
		for (UserCommission userCommission : commissionsByBatchId) {
		    batchAmount = ArithmeticUtil.addStr(batchAmount, userCommission.getAmount());
			if(userCommission.getStatus() == 1){
				++passNum;
				//批次总金额
				amount = ArithmeticUtil.addStr(amount, userCommission.getAmount());
				//批次总服务费
				serviceFee = ArithmeticUtil.addStr(serviceFee,userCommission.getSumFee());
				//批次总利润
				serviceMFFee = ArithmeticUtil.addStr(serviceMFFee, userCommission.getProfiltFree());
			}
			if(userCommission.getStatus() == 2 || userCommission.getStatus() == 3 || userCommission.getStatus() == 0 ){
				++failedNum;
			}
			if(userCommission.getStatus() == 3){
				userCommission.setStatus(2);
				userCommission.setStatusDesc("系统错误"); 
				commissionService.updateUserCommission(userCommission);
			}
		}
		handleAmount = ArithmeticUtil.addStr(amount, serviceFee);
		ChannelHistory channelHistory = channelHistoryService.getChannelHistoryById(batchId);
		channelHistory.setPassNum(passNum);
		channelHistory.setAmount(amount);
		channelHistory.setFailedNum(failedNum);
		channelHistory.setBatchNum(batchNum);
		channelHistory.setServiceFee(serviceFee);
		channelHistory.setBatchAmount(batchAmount);
		channelHistory.setHandleAmount(handleAmount);
		if(passNum == batchNum){
			channelHistory.setStatus(1);//成功
		}else if(passNum < batchNum && passNum != 0){
			channelHistory.setStatus(5);//部分失败
		}else{
			channelHistory.setStatus(2);//全部失败
		}
		logger.info("《《《支付宝下发结束！》》》成功："+passNum+"笔，失败："+failedNum+"笔，总共："+batchNum+"笔，实发金额(不含服务费)："+amount+"。");
		channelHistoryService.updateChannelHistory(channelHistory);
	}
}
