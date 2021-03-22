package com.jrmf.taxsettlement.api.service.transfer;

import com.jrmf.utils.OrderNoUtil;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.xwork.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.api.ApiReturnCode;
import com.jrmf.api.PaymentApi;
import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;

@ActionConfig(name = "支付宝账户审核下发")
public class TransferToAlipayAccountService extends
		AbstractTransferService<TransferToAlipayAccountServiceParams, TransferToAlipayAccountServiceAttachment> {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static final Logger logger = LoggerFactory.getLogger(TransferToAlipayAccountService.class);

	@Autowired
	private PaymentApi paymentApiImpl;
	@Autowired
	private OrderNoUtil orderNoUtil;

	@Override
	public String getActionType() {
		return APIDefinition.TRANSFER_TO_ALIPAY_ACCOUNT.name();
	}

	@Override
	protected Map<String, String> checkAndCountFeeBeforeTransfer(ChannelRelated channelWithContract,
			TransferToAlipayAccountServiceParams params) {
		Map<String, String> feeInfo = super.checkAndCountFeeBeforeTransfer(channelWithContract, params);

//		if (new BigDecimal(params.getAmount()).compareTo(new BigDecimal(50000)) > 0) {
//			throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), params.getAlipayAccountNo());
//		}

		return feeInfo;
	}

	@Override
	protected ActionResult<TransferToAlipayAccountServiceAttachment> transfer(String dealNo,
			TransferToAlipayAccountServiceParams actionParams, Map<String, String> feeInfo,
			ChannelRelated channelWithContract) {
		UserCommission userCommission = new UserCommission();
		userCommission.setAmount(feeInfo.get(AbstractTransferServiceParams.ARRIVL_AMOUNT));
		userCommission.setSourceAmount(actionParams.getSourceAmount());
		userCommission.setAccount(actionParams.getAlipayAccountNo());

		userCommission.setCalculationRates(feeInfo.get(AbstractTransferServiceParams.CALCULATION_RATES));
		userCommission.setRateInterval(feeInfo.get(AbstractTransferServiceParams.RATE_INTERVAL));
		userCommission.setCertId(actionParams.getCertificateNo().toUpperCase());

		String transferCorpId = actionParams.getTransferCorpId();
		userCommission.setCompanyId(transferCorpId);
		userCommission.setCompanyName(getNameFromCache(transferCorpId));

		String merchantId = actionParams.getMerchantId();
		userCommission.setOriginalId(merchantId);
		userCommission.setCustomName(getNameFromCache(merchantId));
		//增加回单号
		userCommission.setReceiptNo(orderNoUtil.getReceiptNo());
		if (actionParams.getRemark() == null){
			actionParams.setRemark("");
		}
		userCommission.setRemark(userCommission.getReceiptNo()+actionParams.getRemark());

		userCommission.setDocumentType(1);

		userCommission.setMerchantId(feeInfo.get("merchantId"));
		userCommission.setOrderNo(dealNo);

		userCommission.setPayType(2);

		userCommission.setProfilt(feeInfo.get(AbstractTransferServiceParams.PROFILT_RATES));
		userCommission.setProfiltFree(feeInfo.get(AbstractTransferServiceParams.PROFIT_AMOUNT));

		userCommission.setRepeatcheck(-1);
		userCommission.setStatus(0);
		userCommission.setSumFee(feeInfo.get(AbstractTransferServiceParams.SUM_FEE));
		userCommission.setSupplementAmount(feeInfo.get(AbstractTransferServiceParams.SUPPLEMENT_AMOUNT));
		userCommission.setSupplementFee(feeInfo.get(AbstractTransferServiceParams.SUPPLEMENT_FEE));
		// userCommission.setUserId(userId);
		userCommission.setUserName(actionParams.getName());
		userCommission.setFeeRuleType(channelWithContract.getFeeRuleType());
		userCommission.setAccountDate(sdf.format(new Date()));//增加对账时间
		userCommission.setPhoneNo(actionParams.getReservedMobile());

		PaymentReturn<?> ret = paymentApiImpl.transfer(userCommission, channelWithContract, StringUtils.isNotEmpty(actionParams.getBatchNo()));
		String retCode = ret.getRetCode();
		String retMsg = ret.getFailMessage();

		if (ApiReturnCode.SUCCESS.getCode().equals(retCode)) {
			TransferToAlipayAccountServiceAttachment attachment = new TransferToAlipayAccountServiceAttachment();
			attachment.setAmount(actionParams.getSourceAmount());
			attachment.setDealNo(dealNo);
			attachment.setDealStatus(TransferStatus.ACCEPTED.getCode());
			attachment.setDealStatusMsg(TransferStatus.ACCEPTED.getDesc());
			return new ActionResult<TransferToAlipayAccountServiceAttachment>(attachment);
		} else if (ApiReturnCode.DEDUCTION_FAILURE.getCode().equals(retCode)) {
			throw new APIDockingException(APIDockingRetCodes.BALANCE_NOT_SUFFICIENT.getCode(), retMsg);
		} else {
			throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(), retMsg);
		}
	}

}
