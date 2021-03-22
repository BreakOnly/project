package com.jrmf.payment.execute;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.LdOrderStep;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.persistence.CustomBalanceDao;
import com.jrmf.persistence.CustomPaymentTotalAmountDao;
import com.jrmf.service.CompanyService;
import com.jrmf.service.CustomLimitConfService;
import com.jrmf.service.LdOrderStepService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.taxsettlement.api.service.CommonRetCodes;
import com.jrmf.taxsettlement.api.service.transfer.TransferDealStatusNotifier;
import com.jrmf.taxsettlement.api.service.transfer.TransferStatus;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil;

public class ExecuteBatchGrantLdQuery implements Runnable {

	private Logger logger = LoggerFactory.getLogger(ExecuteBatchGrantLdQuery.class);
	public static final String PROCESS = "process";

	private String processId;
	private List<UserCommission> batchData;
	private UserCommissionService commissionService;
	private CompanyService companyService;
	private UtilCacheManager utilCacheManager;
	private CustomLimitConfService customLimitConfService;
	private LdOrderStepService ldOrderStepService;
	private CustomBalanceDao customBalanceDao;
	private BaseInfo baseInfo;
    private TransferDealStatusNotifier transferDealStatusNotifier;

	public ExecuteBatchGrantLdQuery(String processId,
			List<UserCommission> batchData,
			UserCommissionService commissionService,
			CompanyService companyService,
			UtilCacheManager utilCacheManager,
			CustomLimitConfService customLimitConfService,
			CustomPaymentTotalAmountDao customPaymentTotalAmountDao,
			LdOrderStepService ldOrderStepService,CustomBalanceDao customBalanceDao,
			BaseInfo baseInfo,
			TransferDealStatusNotifier transferDealStatusNotifier) {
		super();
		this.processId = processId;
		this.batchData = batchData;
		this.commissionService = commissionService;
		this.companyService = companyService;
		this.utilCacheManager = utilCacheManager;
		this.customLimitConfService =customLimitConfService;
		this.ldOrderStepService=ldOrderStepService;
		this.customBalanceDao=customBalanceDao;
		this.baseInfo=baseInfo;
		this.transferDealStatusNotifier=transferDealStatusNotifier;
	}

	@Override
	public void run() {

		MDC.put(PROCESS, processId);
		for (UserCommission commission : batchData) {
			String orderNo = commission.getOrderNo();
			logger.info("交易明细订单号："+commission.getOrderNo()+"查询下发操作执行结果明细");
			if (StringUtil.isEmpty(orderNo)) {
				continue;
			}
			boolean firstFlag = false;
			LdOrderStep ldFisrtOrderStep=null;
			List<LdOrderStep> ldOrderSteps = ldOrderStepService.getList(orderNo);
			for (LdOrderStep ldOrderStep:ldOrderSteps) {
				if(ldOrderStep.getStatus()==3){
					logger.info("交易明细订单号："+commission.getOrderNo()+",步骤订单号："+ldOrderStep.getStepOrderNo()+"查询下发操作执行结果" );
					//处理中
					PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(commission.getPayType()), commission.getOriginalId(), ldOrderStep.getIssuedCompanyid(),ldOrderStep.getPathno());
					logger.info("交易明细订单号："+commission.getOrderNo()+",步骤订单号："+ldOrderStep.getStepOrderNo()+"查询下发操作执行结果----通道配置信息------paymentConfig.toString()：" + paymentConfig.toString());
					if (paymentConfig == null) {
						logger.info("交易明细订单号："+commission.getOrderNo()+",步骤订单号："+ldOrderStep.getStepOrderNo()+"查询下发操作执行结果异常----未配置商户下发通道路由-----customKey:" + commission.getOriginalId() + "--服务公司ID："+ commission.getCompanyId());
						continue;
					}
					PaymentReturn<TransStatus> paymentReturn = null;
					if ("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K".equals(commission.getOriginalId())) {//挡板有效
						TransStatus transStatus1 = new TransStatus( ldOrderStep.getStepOrderNo(), 
								PayRespCode.RESP_TRANSFER_SUCCESS,
								"付款成功");

						paymentReturn = new PaymentReturn<TransStatus>(PayRespCode.RESP_SUCCESS,
								"查询成功",
								transStatus1);
					}else{
						//调用支付通道工厂模式
						Payment<?, ?, ?> payment = PaymentFactory.paymentEntity(paymentConfig);
						PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, utilCacheManager);
						Payment proxy = paymentProxy.getProxy();
						paymentReturn = proxy.queryTransferResult(ldOrderStep.getStepOrderNo());
					}
					logger.info("交易明细订单号："+commission.getOrderNo()+",步骤订单号："+ldOrderStep.getStepOrderNo()+"查询下发操作执行结果----------：" + paymentReturn.toString());
					if (PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())) {
						TransStatus transStatus = paymentReturn.getAttachment();
						logger.error("交易明细订单号："+commission.getOrderNo()+",步骤订单号："+ldOrderStep.getStepOrderNo()+"查询结果---------resultCode:" + transStatus.getResultCode() + "--resultMsg:" + transStatus.getResultMsg());
						if (PayRespCode.RESP_TRANSFER_SUCCESS.equals(transStatus.getResultCode())) {
							ldOrderStep.setStatus(1);
							ldOrderStep.setStatusDesc("成功");
							ldOrderStepService.update(ldOrderStep);
						} else if (PayRespCode.RESP_TRANSFER_FAILURE.equals(transStatus.getResultCode())) {
							ldOrderStep.setStatus(2);
							String errorMsg = transStatus.getResultMsg();
							if(errorMsg !=null){
								if (errorMsg.contains("余额")) {
									errorMsg = "网络异常，请联系管理员";
								} else {
									String statusDesc = transStatus.getResultMsg();
									if (statusDesc.length() > 200) {
										statusDesc = statusDesc.substring(0, 200);
									}
									String s = statusDesc.replaceAll(",", "-");
									errorMsg = s;
								}
							}
							ldOrderStep.setStatusDesc(errorMsg);
							ldOrderStepService.update(ldOrderStep);
							if(ldOrderStep.getBusinessType()==2||ldOrderStep.getBusinessType()==4){
								customLimitConfService.updateCustomPaymentTotalAmount(ldOrderStep.getIssuedCompanyid(),
										commission.getOriginalId(),
										commission.getCertId(),
										ldOrderStep.getAmount(),
										false);
								logger.info("交易明细订单号："+commission.getOrderNo()+",步骤订单号："+ldOrderStep.getStepOrderNo()+"更新累计金额：减去累计{}元", ldOrderStep.getAmount());
							}

						} else {
							logger.error("未知错误---------" + paymentReturn.getRetCode() + "------" + transStatus.getResultMsg());
							ldOrderStep.setStatus(3);
							ldOrderStep.setStatusDesc(paymentReturn.getRetCode() + ":" + transStatus.getResultMsg());
							ldOrderStepService.update(ldOrderStep);
						}
						logger.info("交易明细订单号："+commission.getOrderNo()+",步骤订单号："+ldOrderStep.getStepOrderNo()+"明细落地修改操作完成 ");
					} else {
						logger.error("交易明细订单号："+commission.getOrderNo()+",步骤订单号："+ldOrderStep.getStepOrderNo()+"查询失败---------" + paymentReturn.getRetCode() + paymentReturn.getFailMessage());
					}
					 
					if(ldOrderStep.getStepOrder()==1&&ldOrderStep.getStatus()==2){
						firstFlag = true;
						ldFisrtOrderStep = ldOrderStep;
						break;
					}
				}
			}
			int totalCount = ldOrderStepService.getCountByOrderNo(orderNo);
			int successCount = ldOrderStepService.getCountSuccessByOrderNo(orderNo);
			int failCount = ldOrderStepService.getCountFailByOrderNo(orderNo);
			if(successCount==totalCount){
				logger.info("交易明细订单号："+commission.getOrderNo()+"联动明细步骤全部为成功，更新交易明细订单为成功");
				commission.setPaymentTime(DateUtils.getNowDate());
				commission.setStatus(1);
				commission.setStatusDesc("成功");
				commissionService.updateUserCommissionById(commission);
				if(StringUtil.isEmpty(commission.getBatchId())){					
					transferDealStatusNotifier.notify(commission.getOrderNo(), TransferStatus.TRANSFER_DONE,CommonRetCodes.ACTION_DONE.getCode(),CommonRetCodes.ACTION_DONE.getDesc());
					logger.info("交易明细订单号："+commission.getOrderNo()+"交易成功！系统内部回调成功！");
				}
			}else if(failCount==totalCount){
				logger.info("交易明细订单号："+commission.getOrderNo()+"联动明细步骤全部为失败，更新交易明细订单为失败");
				commission.setPaymentTime(DateUtils.getNowDate());
				commission.setStatus(2);
				commission.setStatusDesc("交易失败");
				commissionService.updateUserCommissionById(commission);
				if(StringUtil.isEmpty(commission.getBatchId())){
					//退款
					updateBalance(commission,commission.getCompanyId(),CommonString.REFUND);	
                    logger.info("退款成功！订单号{}",commission.getOrderNo());
                    transferDealStatusNotifier.notify(commission.getOrderNo(), TransferStatus.TRANSFER_FAILED,CommonRetCodes.UNCATCH_ERROR.getCode(),commission.getStatusDesc());
					logger.info("交易明细订单号："+commission.getOrderNo()+"交易失败！系统内部回调失败！");
				}
			}else if(firstFlag==true){
				logger.info("交易明细订单号："+commission.getOrderNo()+"联动明细步骤首笔交易失败，更新交易明细订单为失败");
				commission.setPaymentTime(DateUtils.getNowDate());
				commission.setStatus(2);
				commission.setStatusDesc("交易失败-"+ldFisrtOrderStep.getStatusDesc());
				commissionService.updateUserCommissionById(commission);
				for (LdOrderStep ldOrderStep2 : ldOrderSteps) {
					ldOrderStep2.setStatus(2);
					ldOrderStep2.setStatusDesc("首笔交易失败");
					ldOrderStepService.update(ldOrderStep2);
				}
				if(StringUtil.isEmpty(commission.getBatchId())){
					//退款
					updateBalance(commission,commission.getCompanyId(),CommonString.REFUND);
                    logger.info("退款成功！订单号{}",commission.getOrderNo());
                    transferDealStatusNotifier.notify(commission.getOrderNo(), TransferStatus.TRANSFER_FAILED,CommonRetCodes.UNCATCH_ERROR.getCode(),commission.getStatusDesc());
					logger.info("交易明细订单号："+commission.getOrderNo()+"交易失败！系统内部回调失败！");
				}
			}
		}
		MDC.remove(PROCESS);
	}

	//扣款或退款
	private void updateBalance(UserCommission transferParam, String companyId,int operating) {
		Map<String,Object> params = new HashMap<>(5);
		BigDecimal Magnification = new BigDecimal(operating*100);
		params.put(CommonString.CUSTOMKEY,transferParam.getOriginalId());
		params.put(CommonString.COMPANYID,companyId);
		params.put(CommonString.PAYTYPE,transferParam.getPayType());
		params.put(CommonString.BALANCE, (new BigDecimal(transferParam.getAmount()).add(new BigDecimal(transferParam.getSumFee()))).multiply(Magnification));
		customBalanceDao.updateBalance(params);
        logger.info("订单号{}"+transferParam.getOrderNo()+"操作金额："+(new BigDecimal(transferParam.getAmount()).add(new BigDecimal(transferParam.getSumFee()))).multiply(new BigDecimal(operating))+"元");
	}

}
