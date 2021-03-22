package com.jrmf.taxsettlement.api.service.transfer;

import com.jrmf.common.Constant;
import com.jrmf.domain.Company;
import com.jrmf.domain.YuncrUserAuthentication;
import com.jrmf.persistence.YuncrUserAuthenticationDao;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jrmf.utils.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.api.ApiReturnCode;
import com.jrmf.api.PaymentApi;
import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.domain.BankName;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.service.BankCardBinService;
import com.jrmf.service.CompanyService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.util.BankInfoCache;
import com.jrmf.utils.OrderNoUtil;

@ActionConfig(name = "银行卡审核下发", supportBatch = true)
public class TransferToBankCardService
    extends
    AbstractTransferService<TransferToBankCardServiceParams, TransferToBankCardServiceAttachment> {

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  private static final Logger logger = LoggerFactory.getLogger(TransferToBankCardService.class);

  @Autowired
  private BankInfoCache bankInfoCache;

  @Autowired
  private BankCardBinService cardBinService;

  @Autowired
  private PaymentApi paymentApiImpl;
  @Autowired
  private CompanyService companyService;
  @Autowired
  private OrderNoUtil orderNoUtil;
  @Autowired
  YuncrUserAuthenticationDao yuncrUserAuthenticationDao;

  private ThreadLocal<String> bankName = new ThreadLocal<String>();

  private ThreadLocal<String> bankNo = new ThreadLocal<String>();

  @Override
  public String getActionType() {
    return APIDefinition.TRANSFER_TO_BANK_CARD.name();
  }

  @Override
  protected Map<String, String> checkAndCountFeeBeforeTransfer(
      ChannelRelated channelWithContract, TransferToBankCardServiceParams params) {
    checkUserAuth(params, channelWithContract.getCompanyId());
    Map<String, String> feeInfo = super.checkAndCountFeeBeforeTransfer(channelWithContract, params);

    String bankCardNo = params.getBankCardNo();
    String[] bankInfo = bankInfoCache.searchBankInfo(bankCardNo);
    PaymentConfig paymentConfig = companyService
        .getPaymentConfigInfo("4", channelWithContract.getOriginalId(),
            channelWithContract.getCompanyId());
    if (bankInfo == null) {
      BankName cardBin = cardBinService.getBankName(bankCardNo);
      if ("true".equals(cardBin.getValidated())) {
        bankName.set(cardBin.getName());
        bankNo.set(cardBin.getSuperNetNo());

        if (paymentConfig != null && PaymentFactory.YPDFDF.equals(paymentConfig.getPathNo())) {
          bankNo.set(cardBin.getBankCodeYP());
        }
        if (paymentConfig != null && PaymentFactory.ZJPAY.equals(paymentConfig.getPathNo())) {
          bankNo.set(cardBin.getBankCodeZJ());
        }

      } else if ("false".equals(cardBin.getValidated())) {
        throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), bankCardNo);
      }
    } else {
      bankNo.set(bankInfo[1]);
      bankName.set(bankInfo[0]);
      if (paymentConfig != null && PaymentFactory.YPDFDF.equals(paymentConfig.getPathNo())) {
        BankName cardBin = cardBinService.getBankName(bankCardNo);
        bankNo.set(cardBin.getBankCodeYP());
      }
      if (paymentConfig != null && PaymentFactory.ZJPAY.equals(paymentConfig.getPathNo())) {
        BankName cardBin = cardBinService.getBankName(bankCardNo);
        bankNo.set(cardBin.getBankCodeZJ());
      }
    }

//		if (new BigDecimal(params.getAmount()).compareTo(new BigDecimal(50000)) > 0) {
//			throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), bankCardNo);
//		}

    return feeInfo;
  }

  @Override
  protected ActionResult<TransferToBankCardServiceAttachment> transfer(String dealNo,
      TransferToBankCardServiceParams actionParams, Map<String, String> feeInfo,
      ChannelRelated channelWithContract) {

		UserCommission userCommission = new UserCommission();
		userCommission.setAmount(feeInfo.get(AbstractTransferServiceParams.ARRIVL_AMOUNT));
		userCommission.setSourceAmount(actionParams.getSourceAmount());
		String bankCardNo = actionParams.getBankCardNo();
		userCommission.setAccount(StringUtil.replaceHeadTailSpecialChar(bankCardNo));
		userCommission.setBankName(bankName.get());

    userCommission
        .setCalculationRates(feeInfo.get(AbstractTransferServiceParams.CALCULATION_RATES));
    userCommission.setRateInterval(feeInfo.get(AbstractTransferServiceParams.RATE_INTERVAL));
    String certNo = StringUtil.replaceHeadTailSpecialChar(actionParams.getCertificateNo());
    userCommission.setCertId(certNo.toUpperCase());

    String transferCorpId = actionParams.getTransferCorpId();
    userCommission.setCompanyId(transferCorpId);
    userCommission.setCompanyName(getNameFromCache(transferCorpId));

    String merchantId = actionParams.getMerchantId();
    userCommission.setOriginalId(merchantId);
    userCommission.setCustomName(getNameFromCache(merchantId));
    //增加回单号
    userCommission.setReceiptNo(orderNoUtil.getReceiptNo());
    if (actionParams.getRemark() == null) {
      actionParams.setRemark("");
    }
    userCommission.setRemark(userCommission.getReceiptNo() + actionParams.getRemark());

    userCommission.setDocumentType(1);
    userCommission.setMerchantId(feeInfo.get("merchantId"));
    userCommission.setOrderNo(dealNo);

    userCommission.setPayType(4);

    userCommission.setProfilt(feeInfo.get(AbstractTransferServiceParams.PROFILT_RATES));
    userCommission.setProfiltFree(feeInfo.get(AbstractTransferServiceParams.PROFIT_AMOUNT));

    userCommission.setRepeatcheck(-1);
    userCommission.setStatus(0);
    userCommission.setSumFee(feeInfo.get(AbstractTransferServiceParams.SUM_FEE));
    userCommission
        .setSupplementAmount(feeInfo.get(AbstractTransferServiceParams.SUPPLEMENT_AMOUNT));
    userCommission.setSupplementFee(feeInfo.get(AbstractTransferServiceParams.SUPPLEMENT_FEE));
    // userCommission.setUserId(userId);
    userCommission.setUserName(StringUtil.replaceHeadTailSpecialChar(actionParams.getName()));
    userCommission.setFeeRuleType(channelWithContract.getFeeRuleType());
    userCommission.setAccountDate(sdf.format(new Date()));
    userCommission.setBankNo(bankNo.get());
    userCommission.setBankName(bankName.get());
    userCommission.setPhoneNo(actionParams.getReservedMobile());
    PaymentReturn<?> ret = paymentApiImpl.transfer(userCommission, channelWithContract,
        StringUtils.isNotEmpty(actionParams.getBatchNo()));
    String retCode = ret.getRetCode();
    String retMsg = ret.getFailMessage();

		if (ApiReturnCode.SUCCESS.getCode().equals(retCode)) {
			TransferToBankCardServiceAttachment attachment = new TransferToBankCardServiceAttachment();
			attachment.setAmount(actionParams.getSourceAmount());
			attachment.setDealNo(dealNo);
			attachment.setDealStatus(TransferStatus.ACCEPTED.getCode());
			attachment.setDealStatusMsg(TransferStatus.ACCEPTED.getDesc());
			return new ActionResult<TransferToBankCardServiceAttachment>(attachment);
		} else if (ApiReturnCode.DEDUCTION_FAILURE.getCode().equals(retCode)) {
			throw new APIDockingException(APIDockingRetCodes.BALANCE_NOT_SUFFICIENT.getCode(), retMsg);
		} else {
			throw new APIDockingException(CommonRetCodes.UNEXPECT_ERROR.getCode(), retMsg);
		}
	}

  //个体工商户检测
  private void checkUserAuth(TransferToBankCardServiceParams params, String companyId) {
    try {
      Company company = companyService.getCompanyByUserId(Integer.parseInt(companyId));
      Integer checkUserAuth = company.getCheckUserAuth();

      if (company.getCompanyType() == 1) {
        Company realCompany = companyService
            .getCompanyByUserId(Integer.parseInt(company.getRealCompanyId()));
        checkUserAuth = realCompany.getCheckUserAuth();
      }
      if (null != checkUserAuth && checkUserAuth == 1) {
        List<YuncrUserAuthentication> list = yuncrUserAuthenticationDao
            .selectByCondition(params.getCertificateNo(), null, null, null);
        if (CollectionUtils.isEmpty(list)
            || list.get(0).getGovernmentAudit() != Constant.AUDIT_SUCCESS) {
          throw new APIDockingException(APIDockingRetCodes.NO_YUNCR_USER_AUTHENTICATION.getCode(),
              APIDockingRetCodes.NO_YUNCR_USER_AUTHENTICATION.getDesc());
        }

        int count = yuncrUserAuthenticationDao
            .selectBank(params.getCertificateNo(), params.getBankCardNo());
        if (count == 0) {
          throw new APIDockingException(APIDockingRetCodes.UN_BIND_BANK_CARD.getCode(),
              APIDockingRetCodes.UN_BIND_BANK_CARD.getDesc());
        }
      }
    } catch (APIDockingException e) {
      throw new APIDockingException(e.getErrorCode(), e.getErrorMsg());
    }catch (Exception e){
      logger.info("银行卡下发效验个体工商户出错"+e);
    }
  }

}
