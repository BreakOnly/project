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
import com.jrmf.domain.LdOrderCorrect;
import com.jrmf.domain.LdOrderStep;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.persistence.CustomBalanceDao;
import com.jrmf.service.CompanyService;
import com.jrmf.service.LdOrderCorrectService;
import com.jrmf.service.LdOrderStepService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.taxsettlement.api.service.CommonRetCodes;
import com.jrmf.taxsettlement.api.service.transfer.TransferDealStatusNotifier;
import com.jrmf.taxsettlement.api.service.transfer.TransferStatus;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil; 
@SuppressWarnings("all")
public class ExecuteLdCorrectQuery implements Runnable {

	private Logger logger = LoggerFactory.getLogger(ExecuteLdCorrectQuery.class);
	public static final String PROCESS = "process";
	private String processId;
	private List<LdOrderCorrect> batchData;
	private UserCommissionService commissionService;
	private CompanyService companyService;
	private UtilCacheManager utilCacheManager;
	private LdOrderStepService ldOrderStepService;
	private CustomBalanceDao customBalanceDao;
	private BaseInfo baseInfo;
	private LdOrderCorrectService ldOrderCorrectService;
	private TransferDealStatusNotifier transferDealStatusNotifier;

	public ExecuteLdCorrectQuery(String processId,
			List<LdOrderCorrect> batchData,
			UserCommissionService commissionService,
			CompanyService companyService,
			UtilCacheManager utilCacheManager,
			LdOrderStepService ldOrderStepService,CustomBalanceDao customBalanceDao,
			BaseInfo baseInfo,
			LdOrderCorrectService ldOrderCorrectService,
			TransferDealStatusNotifier transferDealStatusNotifier) {
		super();
		this.processId = processId;
		this.batchData = batchData;
		this.commissionService = commissionService;
		this.companyService = companyService;
		this.utilCacheManager = utilCacheManager;
		this.ldOrderStepService=ldOrderStepService;
		this.customBalanceDao=customBalanceDao;
		this.baseInfo=baseInfo;
		this.ldOrderCorrectService=ldOrderCorrectService;
		this.transferDealStatusNotifier=transferDealStatusNotifier;
	}

	@Override
	public void run() {

		MDC.put(PROCESS, processId);
		for (LdOrderCorrect ldOrderCorrect : batchData) {
			String correctOrderNo = ldOrderCorrect.getCorrectOrderNo();
			logger.info("冲正订单号："+correctOrderNo+"查询下发操作执行结果明细");
			if (StringUtil.isEmpty(correctOrderNo)) {
				continue;
			}
			if(ldOrderCorrect.getStatus()==3){
				//处理中
				logger.info("步骤明细订单号："+ldOrderCorrect.getStepOrderNo()+",冲正订单号："+correctOrderNo+"查询下发操作执行结果" );
				UserCommission commission = commissionService.getUserCommission(ldOrderCorrect.getOrderNo());
				LdOrderStep ldOrderStep = ldOrderStepService.getOrderStep(ldOrderCorrect.getStepOrderNo());
				PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(String.valueOf(commission.getPayType()), commission.getOriginalId(), ldOrderCorrect.getIssuedCompanyid(),ldOrderStep.getPathno());
				logger.info("步骤明细订单号："+ldOrderCorrect.getStepOrderNo()+",冲正订单号："+correctOrderNo+"查询下发操作执行结果----通道配置信息------paymentConfig.toString()：" + paymentConfig.toString());
				if (paymentConfig == null) {
					logger.info("步骤明细订单号："+ldOrderCorrect.getStepOrderNo()+",冲正订单号："+correctOrderNo+"查询下发操作执行结果异常----未配置商户下发通道路由-----customKey:" + commission.getOriginalId() + "--服务公司ID："+ ldOrderCorrect.getIssuedCompanyid());
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
					paymentReturn = proxy.queryTransferResult(correctOrderNo);
				}
				logger.info("步骤明细订单号："+ldOrderCorrect.getStepOrderNo()+",冲正订单号："+correctOrderNo+"查询下发操作执行结果----------：" + paymentReturn.toString());
				if (PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())) {
					TransStatus transStatus = paymentReturn.getAttachment();
					logger.error("步骤明细订单号："+ldOrderCorrect.getStepOrderNo()+",冲正订单号："+correctOrderNo+"查询结果---------resultCode:" + transStatus.getResultCode() + "--resultMsg:" + transStatus.getResultMsg());
					if (PayRespCode.RESP_TRANSFER_SUCCESS.equals(transStatus.getResultCode())) {
						ldOrderCorrect.setStatus(1);
						ldOrderCorrect.setStatusDesc("成功");
						ldOrderCorrectService.updateByPrimaryKeySelective(ldOrderCorrect);
						ldOrderStep.setStatus(2);
						ldOrderStep.setCorrectStatus(1);
						ldOrderStepService.updateById(ldOrderStep);
					} else if (PayRespCode.RESP_TRANSFER_FAILURE.equals(transStatus.getResultCode())) {
						ldOrderCorrect.setStatus(2);
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
						ldOrderCorrect.setStatusDesc(errorMsg);
						ldOrderCorrectService.updateByPrimaryKeySelective(ldOrderCorrect);
						ldOrderStep.setCorrectStatus(2);
						ldOrderStepService.updateById(ldOrderStep);
					} else {
						logger.error("未知错误---------" + paymentReturn.getRetCode() + "------" + transStatus.getResultMsg());
						ldOrderCorrect.setStatus(3);
						ldOrderCorrect.setStatusDesc(paymentReturn.getRetCode() + ":" + transStatus.getResultMsg());
						ldOrderCorrectService.updateByPrimaryKeySelective(ldOrderCorrect);
						ldOrderStep.setCorrectStatus(3);
						ldOrderStepService.updateById(ldOrderStep);
					}
					logger.info("步骤明细订单号："+ldOrderCorrect.getStepOrderNo()+",冲正订单号："+correctOrderNo+"明细落地修改操作完成 ");
				} else {
					logger.error("步骤明细订单号："+ldOrderCorrect.getStepOrderNo()+",冲正订单号："+correctOrderNo+"查询失败---------" + paymentReturn.getRetCode() + paymentReturn.getFailMessage());
				}
				int totalCount = ldOrderStepService.getCountByOrderNo(ldOrderStep.getOrderno());
				int failCount = ldOrderStepService.getCountFailByOrderNo(ldOrderStep.getOrderno());
				if(failCount==totalCount){
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
