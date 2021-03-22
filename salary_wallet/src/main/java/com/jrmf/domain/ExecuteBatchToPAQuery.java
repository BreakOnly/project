package com.jrmf.domain;

import com.jrmf.bankapi.ActionReturn;
import com.jrmf.bankapi.BankService;
import com.jrmf.bankapi.TransferResult;
import com.jrmf.persistence.CustomBalanceDao;
import com.jrmf.service.UserCommissionService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecuteBatchToPAQuery implements Runnable {

	private Logger logger = LoggerFactory.getLogger(ExecuteBatchToPAQuery.class);

	List<UserCommission> batchData;

	UserCommissionService commissionService;

	BankService pinganBankService;

	private CustomBalanceDao customBalanceDao;

	public ExecuteBatchToPAQuery(UserCommissionService commissionService, List<UserCommission> batchData,
								 BankService pinganBankService,CustomBalanceDao customBalanceDao) {
		super();
		this.batchData = batchData;
		this.commissionService = commissionService;
		this.pinganBankService = pinganBankService;
		this.customBalanceDao = customBalanceDao;
	}

	@Override
	public void run() {
//		for (UserCommission commission : batchData) {
//
//			String orderNo = commission.getOrderNo();
//			if(StringUtil.isEmpty(orderNo)){
//				continue;
//			}
//			ActionReturn<TransferResult> ret = pinganBankService.queryTransferResult(orderNo);
//			logger.info("查询银企直联执行结果--------订单号："+orderNo);
//			if(ret.isOk()){
//				TransferResult result = ret.getAttachment();
//				logger.error("查询结果---------"+ret.getAttachment().getResultMsg());
//				if(TransferResult.TransferResultType.SUCCESS.equals(result.getResultType())){
//					commission.setStatus(1);
//					commission.setStatusDesc("成功");
//
//				}else if(TransferResult.TransferResultType.FAIL.equals(result.getResultType())){
//					commission.setStatus(2);
//					String errorMsg = ret.getAttachment().getResultMsg();
//					if(errorMsg.contains("余额")){
//						commission.setStatusDesc("余额不足，请联系服务公司");
//					}else{
//						String statusDesc = ret.getAttachment().getResultMsg();
//						if(statusDesc.length()>200){
//							statusDesc = statusDesc.substring(0, 200);
//						}
//						commission.setStatusDesc(statusDesc);
//					}
//
//				}else{
//					logger.error("未知错误---------"+result.getResultType()+"------"+ret.getAttachment().getResultMsg());
//				}
//				commission.setPaymentTime(DateUtils.getNowDate());
//				commissionService.updateUserCommission(commission);
//			} else {
//				logger.error("查询失败---------"+ret.getRetCode() + ret.getFailMessage());
//			}
//		}
	}
}
