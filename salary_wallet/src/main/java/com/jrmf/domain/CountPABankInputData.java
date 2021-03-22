package com.jrmf.domain;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.oldsalarywallet.dao.ChannelInterimBatchDao;
import com.jrmf.oldsalarywallet.dao.CommissionTemporaryDao;
import com.jrmf.service.DataService;
import com.jrmf.utils.ArithmeticUtil;

public class CountPABankInputData implements Runnable {
	
	private Logger logger = LoggerFactory.getLogger(CountPABankInputData.class);
	
	private CommissionTemporaryDao commissionDao;

	private ChannelInterimBatchDao interimBatchDao;
	
	private ChannelRelated channelRelated;
	
	private Map<String, String> batchData;
	
	private DataService dataService;	
	
	private BaseInfo baseInfo;
	
	public CountPABankInputData(Map<String, String> batchData,CommissionTemporaryDao commissionDao, 
			ChannelInterimBatchDao interimBatchDao, ChannelRelated channelRelated,DataService dataService,
			BaseInfo baseInfo) {
		super();
		this.commissionDao = commissionDao;
		this.interimBatchDao = interimBatchDao;
		this.batchData = batchData;
		this.channelRelated = channelRelated;
		this.dataService = dataService;
		this.baseInfo = baseInfo;
		
	}

	@Override
	public void run() {
		logger.info("-------------------佣金明细处理完成，开始生成临时批次信息---------------------------");
		String batchId = batchData.get("batchId");
		String fileName = batchData.get("fileName");
		String operatorName = batchData.get("operatorName");
		String batchDesc = batchData.get("batchDesc");
		String batchName = batchData.get("batchName");
		String menuId = batchData.get("menuId");
		String originalId = channelRelated.getOriginalId();
		List<CommissionTemporary> commissionsByBatchId = commissionDao.getCommissionsByBatchId(batchId,originalId);
		int passNum = 0;//验证成功总数
		int failedNum = 0;//验证失败总数
		String amountSum = "0.00";//实际下发金额
		String failedAmountSum = "0.00";//失败总金额
		String serviceFeeSum = "0.00";//实际服务费
		String serviceMFFeeSum = "0.00";//实际魔方服务费
		String handleAmount = "0.00";//订单应付总额
		String batchAmount = "0.00";
		int batchNum = commissionsByBatchId.size();//批次总数
		for (CommissionTemporary temporary : commissionsByBatchId) {
			String amount = temporary.getAmount();
			if(temporary.getStatus() == 1){
				passNum++;
				String commissionMfkjFree = "0";
				if(ArithmeticUtil.compareTod(amount, baseInfo.getCalculationLimit())<0){
					commissionMfkjFree = ArithmeticUtil.mulStr(amount, channelRelated.getProfiltLower(), 2);
				}else{
					commissionMfkjFree = ArithmeticUtil.mulStr(amount, channelRelated.getProfiltUpper(), 2);
				}
				//服务费
				String commissionAygFree = ArithmeticUtil.mulStr(amount, channelRelated.getServiceRates(), 2);
		    	
				//批次总金额
				amountSum = ArithmeticUtil.addStr(amountSum, amount);
				//批次总服务费
				serviceFeeSum = ArithmeticUtil.addStr(serviceFeeSum,commissionAygFree);
				//批次总利润
				serviceMFFeeSum = ArithmeticUtil.addStr(serviceMFFeeSum, commissionMfkjFree);
			}else if(temporary.getStatus() == 2){
				failedAmountSum =  ArithmeticUtil.addStr(failedAmountSum, amount);;
				failedNum++;
			}
			batchAmount = ArithmeticUtil.addStr(batchAmount, amount);
		}
		handleAmount = ArithmeticUtil.addStr(amountSum, serviceFeeSum);
		ChannelInterimBatch batch = new ChannelInterimBatch();
		batch.setAmount(amountSum);
		batch.setCustomkey(channelRelated.getOriginalId());
		batch.setRecCustomkey(channelRelated.getCompanyId());
		batch.setOrdername("佣金发放");
		batch.setOrderno(batchId);
		batch.setPayType(4);
		batch.setServiceFee(serviceFeeSum);//服务费
		batch.setMfkjServiceFee(serviceMFFeeSum);
		batch.setPassNum(passNum);
		batch.setFailedNum(failedNum);
		batch.setBatchNum(batchNum);
		batch.setHandleAmount(handleAmount);
		batch.setBatchAmount(batchAmount);
		batch.setFileName(fileName);
		batch.setOperatorName(operatorName);
		batch.setBatchDesc(batchDesc);
		batch.setBatchName(batchName);
		batch.setFailedAmount(failedAmountSum);
		batch.setMenuId(Integer.parseInt(menuId));
		if(failedNum>0){
			if(passNum==0){
				batch.setStatus(2);//全部失败
			}else{
				batch.setStatus(3);//部分失败
			}
		}else if(failedNum==0){
			batch.setStatus(1);//全部成功
		}
		interimBatchDao.addChannelInterimBatch(batch);
		/**
		 * 批次缓存数据结束
		 */
//		dataService.complete(batchId);
		logger.info("-------------------临时批次信息生成结束---------------------------");
	}
}
