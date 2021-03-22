package com.jrmf.api;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.controller.constant.CommissionStatus;
import com.jrmf.controller.constant.LdCommissionBusinessTypeEnum;
import com.jrmf.controller.constant.LdCommissionIsSplitTypeEnum;
import com.jrmf.controller.constant.LdOrderStatusEnum;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.*;
import com.jrmf.domain.vo.CompanyAccountVo;
import com.jrmf.oldsalarywallet.dao.UserCommissionDao;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.persistence.UserCommission2Dao;
import com.jrmf.service.*;
import com.jrmf.taxsettlement.api.service.CommonRetCodes;
import com.jrmf.taxsettlement.api.service.transfer.TransferDealStatusNotifier;
import com.jrmf.taxsettlement.api.service.transfer.TransferStatus;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/8 15:07
 * Version:1.0
 */
public class ExecutePaymentApi implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(ExecutePaymentApi.class);

	@Autowired
	private UtilCacheManager utilCacheManager;

	@Autowired
	private BaseInfo baseInfo;
	@Autowired
	private UserCommissionDao userCommissionDao;
	@Autowired
	private UserCommission2Dao userCommissionDao2;
	@Autowired
	private LdOrderStepService ldOrderStepService;
	@Autowired
	private CustomLimitConfService customLimitConfService;
	@Autowired
	ForwardCompanyAccountService forwardCompanyAccountService;
	@Autowired
	private TransferDealStatusNotifier transferDealStatusNotifier;
	@Autowired
	private UserCommissionService userCommissionService;
	@Autowired
	private CustomBalanceService customBalanceService;
	@Autowired
	UserSerivce userSerivce;

	public static final String PROCESS = "process";

	@Override
	public void onMessage(Message message) {

		String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
		MDC.put(PROCESS, processId);

		PaymentRequest request;
		try {
			request = (PaymentRequest)((ObjectMessage)message).getObject();
            logger.info("mq的信息为"+request.toString());
		} catch (JMSException e) {
			logger.error("error occured in get payment request from object message and abort payment", e);
			return;
		}
		UserCommission userCommission = request.getUserCommission();
		//1.判断是否为联动交易
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("customkey", userCommission.getOriginalId());
		params.put("companyId", userCommission.getCompanyId());

		//api进行大于5w拆单处理,暂不考虑大于10w情况
		PaymentConfig customPaymentConfig = request.getPaymentConfig();
		if (PaymentFactory.PAYQZL.equals(customPaymentConfig.getPathNo()) &&
				StringUtil.isNumber(customPaymentConfig.getParameter2()) &&
				ArithmeticUtil.compareTod(customPaymentConfig.getParameter2(), "0") == 1 &&
				ArithmeticUtil.compareTod(userCommission.getAmount(), customPaymentConfig.getParameter2()) == 1) {

			List<LdOrderStep> splitOrderList = null;
			try {
				splitOrderList = ldOrderStepService
						.splitOrderPayment(userCommission, customPaymentConfig,
								customPaymentConfig.getParameter2());
			} catch (Exception e) {
				logger.error("api下发拆单异常:{}", e.getMessage(), e);
			}
			ldSplitOrderIssued(userCommission, customPaymentConfig, splitOrderList);

		} else {
			logger.info("订单号：" + userCommission.getOrderNo() + ",未命中拆单下发，使用原有模式下发");
			orderIssued(request);
		}

		MDC.remove(PROCESS);
	}

	private void orderIssued(PaymentRequest request) {
		PaymentConfig paymentConfig = request.getPaymentConfig();
		UserCommission transferParam = request.getUserCommission();

		PaymentReturn<String> paymentReturn = null;
		try {
			transferParam.setPathNo(paymentConfig.getPathNo());
			if ("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K".equals(transferParam.getOriginalId())) {//挡板有效
				String amount = transferParam.getAmount();
				amount = ArithmeticUtil.mulStr(amount, "100");
				int length = amount.length();
				//偶数
				if (Integer.valueOf(amount.substring(length - 1)) % 2 == 0) {
					transferParam.setStatus(CommissionStatus.SUCCESS.getCode());
					transferParam.setStatusDesc("成功");

					//更新用户ID
					Map<String, Object> stringObjectMap = userSerivce.addUserInfo(
							transferParam.getUserName(),
							transferParam.getDocumentType(),
							transferParam.getCertId(),
							transferParam.getUserNo(),
							transferParam.getPhoneNo(),
							transferParam.getOriginalId(),
							transferParam.getMerchantId(), "");
					transferParam.setUserId(stringObjectMap.get("userId") + "");

					//这里冗余修改为成功，否则回调时会根据实时状态进行回调，导致回调为处理中
					userCommissionDao.updateUserCommission(transferParam);

					transferDealStatusNotifier.notify(transferParam.getOrderNo(), TransferStatus.TRANSFER_DONE, CommonRetCodes.ACTION_DONE.getCode(), CommonRetCodes.ACTION_DONE.getDesc());

				} else {
					transferParam.setStatus(CommissionStatus.FAILURE.getCode());
					transferParam.setStatusDesc("失败");
					paymentReturn = new PaymentReturn(PayRespCode.RESP_CHECK_FAIL,
							transferParam.getStatusDesc(), transferParam.getOrderNo());
				}

			} else {
				paymentReturn = userCommissionService.getPaymentReturn(transferParam, paymentConfig);
				logger.info("支付返回paymentReturn>>>{}", paymentReturn.toString());

				if (PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())) {
					String bankOrderNo = paymentReturn.getAttachment();
					transferParam.setStatus(CommissionStatus.SUBMITTED.getCode());
					transferParam.setAygOrderNo(bankOrderNo);
					transferParam.setStatusDesc("处理中");
				} else if (PayRespCode.RESP_CHECK_FAIL.equals(paymentReturn.getRetCode()) || PayRespCode.RESP_CHECK_COUNT_FAIL.equals(paymentReturn.getRetCode())) {
					transferParam.setStatus(CommissionStatus.FAILURE.getCode());
					transferParam.setStatusDesc(paymentReturn.getFailMessage());
				} else {
//				paymentReturn = new PaymentReturn<>(ApiReturnCode.SUBMIT_EXCEPTION.getCode(),ApiReturnCode.SUBMIT_EXCEPTION.getDesc());
					setCommissionFailure(transferParam, paymentReturn);
				}
			}

			int updateCount = userCommissionDao.updateUserCommission(transferParam);
			//如果直接返回失败的订单,这里处理退款及累计额扣减
			if (paymentReturn != null && (PayRespCode.RESP_CHECK_FAIL.equals(paymentReturn.getRetCode()) || PayRespCode.RESP_CHECK_COUNT_FAIL.equals(paymentReturn.getRetCode())) && updateCount > 0) {
				customBalanceService.updateCustomBalance(CommonString.ADDITION,
						new CustomBalanceHistory(transferParam.getOriginalId(),
								transferParam.getCompanyId(), transferParam.getPayType(),
								ArithmeticUtil.addStr(transferParam.getAmount(), transferParam.getSumFee(), 2),
								1, TradeType.PAYMENTREFUND.getCode(),
								transferParam.getOrderNo(), "apiTaskJob"));

				logger.info("退款成功！订单号{}", transferParam.getOrderNo());

				customLimitConfService.updateCustomPaymentTotalAmount(transferParam.getCompanyId(),
						transferParam.getOriginalId(),
						transferParam.getCertId(),
						transferParam.getSourceAmount(),
						false);
				logger.info("更新累计金额：减去失败下发{}元", transferParam.getSourceAmount());

				String realCompanyId = transferParam.getRealCompanyId();
				if (!StringUtil.isEmpty(realCompanyId) && !transferParam.getCompanyId().equals(realCompanyId)){//转包服务公司
					//退转包服务公司在实际服务公司的记账户余额
					CompanyAccountVo companyAccountVo = new CompanyAccountVo();
					//String changeAmount = ArithmeticUtil.addStr(transferParam.getAmount(), transferParam.getSumFee(), 2);
					companyAccountVo.setBalance(transferParam.getAmount());
					companyAccountVo.setCustomKey(transferParam.getOriginalId());
					companyAccountVo.setCompanyId(transferParam.getCompanyId());
					companyAccountVo.setRealCompanyId(transferParam.getRealCompanyId());
					companyAccountVo.setTradeType(TradeType.PAYMENTREFUND.getCode());
					companyAccountVo.setRelateOrderNo(transferParam.getOrderNo());
					companyAccountVo.setOperator("apiTaskJob");
					companyAccountVo.setAmount(1);
					companyAccountVo.setOperating(CommonString.ADDITION);
					forwardCompanyAccountService.updateCompanyAccount(companyAccountVo);

					//退转包服务公司在实际服务公司的余额
					CustomBalanceHistory queryBalanceHistory = new CustomBalanceHistory();
					queryBalanceHistory.setTradeType(TradeType.APIPAYMENT.getCode());
					queryBalanceHistory.setRelateOrderNo(transferParam.getOrderNo());
					queryBalanceHistory.setCustomKey(transferParam.getCompanyId());
					queryBalanceHistory.setCompanyId(transferParam.getRealCompanyId());
					List<CustomBalanceHistory> customBalanceHistories = customBalanceService.listCustomBalanceHistory(queryBalanceHistory);
					if (!CollectionUtils.isEmpty(customBalanceHistories)){
						String tradeAmount = customBalanceHistories.get(0).getTradeAmount();
						CustomBalanceHistory customBalanceHistory = new CustomBalanceHistory(transferParam.getCompanyId(),
								transferParam.getRealCompanyId(), transferParam.getPayType(), tradeAmount,
								1, TradeType.PAYMENTREFUND.getCode(),transferParam.getOrderNo(),
								"apiTaskJob");
						customBalanceService.updateCustomBalance(CommonString.ADDITION, customBalanceHistory);
					}
				}
				//原api下发直接失败时没有回调
				transferDealStatusNotifier.notify(transferParam.getOrderNo(), TransferStatus.TRANSFER_FAILED, CommonRetCodes.UNCATCH_ERROR.getCode(), transferParam.getStatusDesc());

			}
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
			paymentReturn = new PaymentReturn<>(ApiReturnCode.SUBMIT_EXCEPTION.getCode(),ApiReturnCode.SUBMIT_EXCEPTION.getDesc());
			setCommissionFailure(transferParam, paymentReturn);
			userCommissionDao.updateUserCommission(transferParam);
		}
	}


	private void setCommissionFailure(UserCommission transferParam, PaymentReturn<String> paymentReturn) {
		transferParam.setStatus(CommissionStatus.SUBMITTED.getCode());
		String failMessage = paymentReturn.getFailMessage();
		if (failMessage.length() > 200) {
			failMessage = failMessage.substring(0, 200);
		}
		transferParam.setStatusDesc(failMessage);
	}

	private void ldSplitOrderIssued(UserCommission userCommission, PaymentConfig paymentConfig,
			List<LdOrderStep> orderSteps) {
		PaymentReturn<String> paymentReturn;

		try {

			Payment payment = PaymentFactory.paymentEntity(paymentConfig);
			PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME,
					utilCacheManager);
			Payment proxy = paymentProxy.getProxy();

			for (LdOrderStep ldOrderStep : orderSteps) {
				try {
					if (LdOrderStatusEnum.PANDING.getCode() == ldOrderStep.getStatus()) {
						logger.info(
								"交易明细订单号：" + userCommission.getOrderNo() + ",步骤订单号：" + ldOrderStep.getStepOrderNo()
										+ "获取的配置信息:" + paymentConfig.toString());

						UserCommission ldOrderStepCommission = ldOrderStep.toUserCommission(userCommission);

						if ("1".equals(baseInfo.getTransferBaffleSwitch()) && !"X99ov2M4dVMrn509aY8K"
								.equals(userCommission.getOriginalId())) {
							logger.info("交易明细订单号：" + userCommission.getOrderNo() + ",步骤订单号：" + ldOrderStep
									.getStepOrderNo() + ",交易走当板");
							//挡板有效
							paymentReturn = new PaymentReturn<>(PayRespCode.RESP_SUCCESS, "交易成功",
									ldOrderStep.getStepOrderNo());
						} else {
							logger.info("交易明细订单号：" + userCommission.getOrderNo() + ",步骤订单号：" + ldOrderStep
									.getStepOrderNo() + ",执行下发");
							paymentReturn = proxy.paymentTransfer(ldOrderStepCommission);
						}

						if (PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())) {
							ldOrderStep.setStatus(CommissionStatus.SUBMITTED.getCode());
							ldOrderStep.setStatusDesc(CommissionStatus.SUBMITTED.getDesc());
						} else if (PayRespCode.RESP_CHECK_FAIL.equals(paymentReturn.getRetCode())
								|| PayRespCode.RESP_CHECK_COUNT_FAIL.equals(paymentReturn.getRetCode())) {
							ldOrderStep.setStatus(CommissionStatus.FAILURE.getCode());
							ldOrderStep.setStatusDesc(paymentReturn.getFailMessage());
						} else {
							ldOrderStep.setStatus(CommissionStatus.SUBMITTED.getCode());
							ldOrderStep.setStatusDesc(paymentReturn.getFailMessage());
						}
						ldOrderStepService.update(ldOrderStep);
					}

				} catch (Exception e) {
					logger.error(
							"交易明细订单号：" + userCommission.getOrderNo() + ",步骤订单号：" + ldOrderStep.getStepOrderNo()
									+ "下发上送过程异常", e);
					logger.error(e.getMessage(), e);
				}
			}

			//更新主订单表为处理中，即使这里出现异常，isSplit标记没有更新成功，status不更新为处理中就不会被定时任务扫描到，进入人工干预流程
			userCommission.setPathNo(paymentConfig.getPathNo());
			userCommission.setStatus(CommissionStatus.SUBMITTED.getCode());
			userCommission.setStatusDesc(CommissionStatus.SUBMITTED.getDesc());
			userCommission.setIsSplit(LdCommissionIsSplitTypeEnum.YES.getCode());
			userCommission.setBusinessType(LdCommissionBusinessTypeEnum.B2CSPLIT.getCode());
			userCommissionDao2.updateUserCommissionByOrderNo(userCommission);

		} catch (Exception e) {
			logger.error("联动拆单下发主流程异常:{}", e.getMessage(), e);
		}

	}

}
