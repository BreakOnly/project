package com.jrmf.taxsettlement.api.gateway.batch.dealer.ext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.domain.AgreementTemplate;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.CustomLimitConf;
import com.jrmf.domain.User;
import com.jrmf.oldsalarywallet.dao.UserCommissionDao;
import com.jrmf.persistence.CustomBalanceDao;
import com.jrmf.persistence.TransferBankDao;
import com.jrmf.service.AgreementTemplateService;
import com.jrmf.service.CustomLimitConfService;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.TaxSettlementInertnessDataCache;
import com.jrmf.taxsettlement.api.gateway.batch.DefaultBatchTransferRequestDealer;
import com.jrmf.taxsettlement.api.gateway.batch.TransformAndCheckResult;
import com.jrmf.taxsettlement.api.gateway.batch.form.BatchFormDistiller;
import com.jrmf.taxsettlement.api.gateway.paramseditor.ServiceParamsEditor;
import com.jrmf.taxsettlement.api.service.ActionAttachment;
import com.jrmf.taxsettlement.api.service.ActionParams;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.transfer.AbstractTransferServiceParams;
import com.jrmf.taxsettlement.api.service.transfer.TransferToAlipayAccountServiceParams;
import com.jrmf.taxsettlement.api.service.transfer.TransferToBankCardServiceParams;
import com.jrmf.taxsettlement.api.util.ChannelRelatedCache;
import com.jrmf.utils.SalaryConfigUtil;

public class CheckAmountBatchTransferRequestDealer extends DefaultBatchTransferRequestDealer {

	private static final CharSequence BALANCE_NOT_ENOUGH_TIP = "Out of range value for column 'balance'";

	@Autowired
	private TransferBankDao transferBankDao;

	@Autowired
	private AgreementTemplateService agreementTemplateService;

	@Autowired
	private UserCommissionDao userCommissionDao;

	@Autowired
	private TaxSettlementInertnessDataCache dataCache;

	@Autowired
	private BaseInfo baseInfo;

	@Autowired
	private CustomBalanceDao customBalanceDao;

	@Autowired
	private ChannelRelatedCache channelRelatedCache;
	@Autowired
	private CustomLimitConfService customLimitConfService;

	public CheckAmountBatchTransferRequestDealer(int concurrentThreadCount, BatchFormDistiller batchFormDistiller,
			List<ServiceParamsEditor> paramsEditors) {
		super(concurrentThreadCount, batchFormDistiller, paramsEditors);
	}

	@Override
    protected TransformAndCheckResult batchTransformAndCheck(String merchantId, String partnerId, String batchNo,
                                                             String notifyUrl, String transferCorpId, List<? extends ActionParams> distilledActionParamsList) {

		TransformAndCheckResult result = super.batchTransformAndCheck(merchantId, partnerId, batchNo, notifyUrl,
				transferCorpId, distilledActionParamsList);

		if (result.getTransferServiceParamsList().isEmpty())
			return result;

		int payType = judgePayType(result.getTransferServiceParamsList().get(0));

		List<AbstractTransferServiceParams> finalTransferServiceParamsList = new ArrayList<AbstractTransferServiceParams>();

		Map<String, UserGroupKey> userKeyTable = new HashMap<String, UserGroupKey>();
		Map<UserGroupKey, List<AbstractTransferServiceParams>> groupByUserMerchantTransferCorpIdTable = getGroupTransferParams(
				result, userKeyTable);

		Map<String, MerchantGroupKey> merchantKeyTable = new HashMap<String, MerchantGroupKey>();
		Map<MerchantGroupKey, List<UserVerifiedServiceParamsEntity>> groupByMerchantTransferCorpIdTable = getGroupByMerchantTransferParams(
				result, groupByUserMerchantTransferCorpIdTable, merchantKeyTable);

		for (Entry<MerchantGroupKey, List<UserVerifiedServiceParamsEntity>> entry : groupByMerchantTransferCorpIdTable
				.entrySet()) {
			List<UserVerifiedServiceParamsEntity> costRequests = precostMerchantBalance(entry.getKey(),
					entry.getValue(), payType, result);
			for (UserVerifiedServiceParamsEntity paramsEntity : costRequests) {
				AbstractTransferServiceParams params = paramsEntity.getServiceParams();
				Map<String, String> feeInfo = new HashMap<String, String>();
				for (Entry<String, BigDecimal> feeEntry : paramsEntity.feeInfos.entrySet()) {
					feeInfo.put(feeEntry.getKey(),
							feeEntry.getValue().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
				}
				params.setFeeInfo(feeInfo);
				finalTransferServiceParamsList.add(params);
			}
		}

		result.setTransferServiceParamsList(finalTransferServiceParamsList);

		return result;
	}

	private int judgePayType(AbstractTransferServiceParams serviceParams) {
		if (serviceParams instanceof TransferToBankCardServiceParams) {
			return 4;
		} else if (serviceParams instanceof TransferToAlipayAccountServiceParams) {
			return 2;
		} else
			return 0;
	}

	private List<UserVerifiedServiceParamsEntity> precostMerchantBalance(MerchantGroupKey merchantGroup,
			List<UserVerifiedServiceParamsEntity> entities, int payType, TransformAndCheckResult result) {

		String merchantId = merchantGroup.getMerchantId();
		String transferCorpId = merchantGroup.getTransferCorpId();

		List<UserVerifiedServiceParamsEntity> payableRequestList = new ArrayList<UserVerifiedServiceParamsEntity>(
				entities.size());
		List<AbstractTransferServiceParams> unpayableRequestList = new ArrayList<AbstractTransferServiceParams>(
				entities.size());
		do {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("customkey", merchantId);
			params.put("companyId", transferCorpId);
			params.put("payType", payType);

			Integer balance = customBalanceDao.queryBalance(params);
			if (balance == null)
				throw new APIDockingException(APIDockingRetCodes.NO_CONTRACT_WITH_AGENT.getCode(),
						new StringBuilder(merchantId).append("-").append(transferCorpId).toString());
			BigDecimal currentBalance = new BigDecimal(balance.intValue());

			BigDecimal totalCost = new BigDecimal(0);
			BigDecimal testTotal = null;
			boolean balanceNotEnough = false;
			for (UserVerifiedServiceParamsEntity entity : entities) {
				if (!balanceNotEnough) {
					testTotal = totalCost.add(new BigDecimal(entity.getServiceParams().getAmount())
							.add(entity.getFeeInfos().get(AbstractTransferServiceParams.SUM_FEE))
							.multiply(new BigDecimal(100)));
					if (testTotal.compareTo(currentBalance) <= 0) {
						totalCost = testTotal;
						payableRequestList.add(entity);
						continue;
					} else {
						balanceNotEnough = true;
					}
				}
				unpayableRequestList.add(entity.getServiceParams());
			}

			int totalCostAmount = totalCost.intValue();
			if (totalCostAmount <= 0) {
				break;
			}

			try {
				Map<String, Object> costParams = new HashMap<String, Object>();
				costParams.put(CommonString.CUSTOMKEY, merchantId);
				costParams.put(CommonString.COMPANYID, transferCorpId);
				costParams.put(CommonString.PAYTYPE, payType);
				costParams.put(CommonString.BALANCE, totalCost.intValue() * -1);
				customBalanceDao.updateBalance(costParams);
				break;
			} catch (Throwable e) {
				if (e.getMessage().contains(BALANCE_NOT_ENOUGH_TIP)) {
					payableRequestList.clear();
					unpayableRequestList.clear();
				}
			}
		} while (true);

		for (AbstractTransferServiceParams serviceParams : unpayableRequestList) {
			result.addUnacceptActionResults(new ActionResult<ActionAttachment>(
					APIDockingRetCodes.BALANCE_NOT_SUFFICIENT.getCode(), new StringBuilder(serviceParams.getRequestNo())
							.append(":").append(serviceParams.getAmount()).toString()));
		}

		return payableRequestList;
	}

	private Map<MerchantGroupKey, List<UserVerifiedServiceParamsEntity>> getGroupByMerchantTransferParams(
			TransformAndCheckResult result,
			Map<UserGroupKey, List<AbstractTransferServiceParams>> groupByUserMerchantTransferCorpIdTable,
			Map<String, MerchantGroupKey> merchantKeyTable) {
		Map<MerchantGroupKey, List<UserVerifiedServiceParamsEntity>> groupByMerchantTransferCorpIdTable = new HashMap<MerchantGroupKey, List<UserVerifiedServiceParamsEntity>>();

		for (Entry<UserGroupKey, List<AbstractTransferServiceParams>> entry : groupByUserMerchantTransferCorpIdTable
				.entrySet()) {
			UserGroupKey userGroupKey = entry.getKey();
			List<UserVerifiedServiceParamsEntity> verifiedRequestList = checkAndCountFeeByGroup(userGroupKey,
					entry.getValue(), result);

			String requestMerchantId = userGroupKey.getMerchantId();
			String requestTransferCorpId = userGroupKey.getTransferCorpId();
			String key = new StringBuilder(requestMerchantId).append(requestTransferCorpId).toString();
			MerchantGroupKey merchantGroupKey = merchantKeyTable.get(key);
			if (merchantGroupKey == null) {
				merchantGroupKey = new MerchantGroupKey(requestMerchantId, requestTransferCorpId);
				merchantKeyTable.put(key, merchantGroupKey);
			}
			List<UserVerifiedServiceParamsEntity> userParamsEntityList = groupByMerchantTransferCorpIdTable
					.get(merchantGroupKey);
			if (userParamsEntityList == null) {
				userParamsEntityList = new ArrayList<UserVerifiedServiceParamsEntity>();
				groupByMerchantTransferCorpIdTable.put(merchantGroupKey, userParamsEntityList);
			}
			userParamsEntityList.addAll(verifiedRequestList);
		}
		return groupByMerchantTransferCorpIdTable;
	}

	private List<UserVerifiedServiceParamsEntity> checkAndCountFeeByGroup(UserGroupKey userGroupKey,
			List<AbstractTransferServiceParams> paramsList, TransformAndCheckResult result) {
		String requestMerchantId = userGroupKey.getMerchantId();
		String requestCertificateNo = userGroupKey.getCertificateNo();
		String requestTransferCorpId = userGroupKey.getTransferCorpId();

		BigDecimal supplementFeeLimitAmount = new BigDecimal(baseInfo.getCalculationLimit());

		ChannelRelated channelWithContract = channelRelatedCache.getChannelRelated(requestMerchantId,
				requestTransferCorpId);

		Map<String, Object> paramsLimit = new HashMap<String, Object>();
		paramsLimit.put("customkey",  channelWithContract.getOriginalId());
		paramsLimit.put("companyId", channelWithContract.getCompanyId());
		CustomLimitConf customLimitConf = customLimitConfService.getCustomLimitConf(paramsLimit);
		
		String singleMonthLimit = "";
		if(customLimitConf != null){
			singleMonthLimit = customLimitConf.getSingleMonthLimit();	
		}
		BigDecimal monthBanLimit = !StringUtils.isEmpty(singleMonthLimit) ? new BigDecimal(singleMonthLimit) : null;
		
//		BigDecimal monthBanLimit = !StringUtils.isEmpty(channelWithContract.getMonthQuota())
//				? new BigDecimal(channelWithContract.getMonthQuota()) : null;

		String mounthSumAmountStr = userCommissionDao.getSumAmountOfMonthByCertId(requestCertificateNo,
				requestMerchantId, requestTransferCorpId);
		BigDecimal originalMounthSumAmount = StringUtils.isEmpty(mounthSumAmountStr) ? new BigDecimal(0)
				: new BigDecimal(mounthSumAmountStr);
		boolean mounthSumAmountBeyondLimit = originalMounthSumAmount.compareTo(supplementFeeLimitAmount) > 0;
		boolean needCostSupplementFee = true;

		BigDecimal mounthSumAmount = originalMounthSumAmount;
		List<UserVerifiedServiceParamsEntity> verifiedServiceParamsEntityList = new ArrayList<UserVerifiedServiceParamsEntity>(
				paramsList.size());
		List<User> userData = transferBankDao.getUserByCertId(requestCertificateNo);

		String verifiedName = null;
		if (userData.size() > 0) {
			verifiedName = userData.get(0).getUserName();
		}

		Map<String, Object> paramMap = new HashMap<>(12);
		paramMap.put("companyId", channelWithContract.getCompanyId());
		paramMap.put("originalId", channelWithContract.getOriginalId());
		paramMap.put("agreementPayment", "1");
		List<AgreementTemplate> agreementTemplateList = agreementTemplateService.getAgreementTemplateByParam(paramMap);
		int templateCount = agreementTemplateList == null ? 0 : agreementTemplateList.size();
		if (templateCount > 0) {
			paramMap.put("certId", requestCertificateNo);
			paramMap.put("signStatus", "5");
			String agreementTemplateId = "";
			if (agreementTemplateList != null && agreementTemplateList.size() > 0) {
				for (int ai = 0; ai < agreementTemplateList.size(); ai++) {
					agreementTemplateId = agreementTemplateList.get(ai).getId() + "," + agreementTemplateId;
				}
				agreementTemplateId = agreementTemplateId.substring(0, agreementTemplateId.lastIndexOf(","));
			}

			paramMap.put("agreementTemplateId", agreementTemplateId);
			int userAgreeCount = agreementTemplateService.getUserAgreementCountByParam(paramMap);
			if (userAgreeCount != templateCount) {
				for (AbstractTransferServiceParams params : paramsList) {
					result.addUnacceptActionResults(new ActionResult<ActionAttachment>(
							APIDockingRetCodes.NO_CONTRACT_WITH_USER.getCode(), new StringBuilder(params.getRequestNo())
									.append("-").append(requestCertificateNo).toString()));
				}
				return verifiedServiceParamsEntityList;
			}
		}

		BigDecimal profiltLower = new BigDecimal(channelWithContract.getProfiltLower());
		BigDecimal profiltUpper = new BigDecimal(channelWithContract.getProfiltUpper());
		BigDecimal serviceRates = new BigDecimal(channelWithContract.getServiceRates());
		BigDecimal upperServiceRates = StringUtils.isEmpty(channelWithContract.getUpperServiceRates())
				? new BigDecimal(0) : new BigDecimal(channelWithContract.getUpperServiceRates());

		String ruleType = channelWithContract.getFeeRuleType();
		for (AbstractTransferServiceParams params : paramsList) {
			if (StringUtils.isNotEmpty(verifiedName) && !verifiedName.equals(params.getName())) {
				result.addUnacceptActionResults(new ActionResult<ActionAttachment>(
						APIDockingRetCodes.REAL_NAME_VERIFY_FAILED.getCode(),
						new StringBuilder(requestCertificateNo).append(":").append(params.getName()).toString()));
				continue;
			}

			BigDecimal requestAmount = new BigDecimal(params.getAmount());
			BigDecimal testSumPaymentAmount = mounthSumAmount.add(requestAmount);

			Map<String, BigDecimal> serviceFeeInfo = null;
			if (monthBanLimit != null) {
				if (testSumPaymentAmount.compareTo(monthBanLimit) > 0) {
					result.addUnacceptActionResults(new ActionResult<ActionAttachment>(
							APIDockingRetCodes.MONTH_AMOUNT_BEYOND_LIMIT.getCode(), new StringBuilder(requestCertificateNo)
									.append(":").append(testSumPaymentAmount.toString()).toString()));
					continue;
				} else {
					mounthSumAmount = testSumPaymentAmount;
					serviceFeeInfo = countFee(requestAmount, serviceRates, profiltLower, ruleType);
				}
			} else {
				if (mounthSumAmountBeyondLimit) {
					serviceFeeInfo = countFee(requestAmount, upperServiceRates, profiltUpper, ruleType);
				} else {
					if (testSumPaymentAmount.compareTo(supplementFeeLimitAmount) > 0) {
						if (needCostSupplementFee) {
							serviceFeeInfo = countAndSupplementFee(requestAmount, upperServiceRates, serviceRates,
									profiltUpper, originalMounthSumAmount, profiltUpper.subtract(profiltLower),
									ruleType);
							needCostSupplementFee = false;
						} else {
							serviceFeeInfo = countFee(requestAmount, upperServiceRates, profiltUpper, ruleType);
						}
					} else {
						serviceFeeInfo = countFee(requestAmount, serviceRates, profiltLower, ruleType);
					}
				}
			}

			verifiedServiceParamsEntityList.add(new UserVerifiedServiceParamsEntity(params, serviceFeeInfo));
		}

		return verifiedServiceParamsEntityList;
	}

	private Map<String, BigDecimal> countAndSupplementFee(BigDecimal requestAmount, BigDecimal countRates,
			BigDecimal lowerRates, BigDecimal profiltRates, BigDecimal originalMounthSumAmount,
			BigDecimal supplementProfiltRate, String ruleType) {

		BigDecimal supplementFeeRate = countRates.subtract(lowerRates);
		Map<String, BigDecimal> feeInfo = new HashMap<String, BigDecimal>();

		BigDecimal sumFeeAmount = null;// requestAmount.multiply(countRates);
		BigDecimal supplementFee = null;// originalMounthSumAmount.multiply(supplementFeeRate).setScale(2,
										// BigDecimal.ROUND_HALF_UP);

		if ("1".equals(ruleType)) {
			supplementFee = originalMounthSumAmount.multiply(supplementFeeRate);
			sumFeeAmount = requestAmount.multiply(countRates);
		} else if ("2".equals(ruleType)) {
			sumFeeAmount = requestAmount.divide(new BigDecimal(1).subtract(countRates), 9, BigDecimal.ROUND_HALF_UP)
					.multiply(countRates);

			BigDecimal supplementFeeDecimalUpper = originalMounthSumAmount
					.divide(new BigDecimal(1).subtract(countRates), 9, BigDecimal.ROUND_HALF_UP).multiply(countRates);
			BigDecimal supplementFeeDecimaldown = originalMounthSumAmount
					.divide(new BigDecimal(1).subtract(lowerRates), 9, BigDecimal.ROUND_HALF_UP).multiply(lowerRates);

			supplementFee = supplementFeeDecimalUpper.subtract(supplementFeeDecimaldown);
		} else {
			supplementFee = new BigDecimal("0.00");
			sumFeeAmount = new BigDecimal("0.00");
		}

		BigDecimal profiltAmount = requestAmount.multiply(profiltRates);
		BigDecimal supplementProfilt = originalMounthSumAmount.multiply(supplementProfiltRate).setScale(2,
				BigDecimal.ROUND_HALF_UP);

		feeInfo.put(AbstractTransferServiceParams.SUM_FEE, sumFeeAmount.add(supplementFee));
		feeInfo.put(AbstractTransferServiceParams.PROFIT_AMOUNT, profiltAmount.add(supplementProfilt));
		feeInfo.put(AbstractTransferServiceParams.SUPPLEMENT_FEE, supplementFee);
		feeInfo.put(AbstractTransferServiceParams.SUPPLEMENT_AMOUNT, originalMounthSumAmount);
		feeInfo.put(AbstractTransferServiceParams.CALCULATION_RATES, countRates);
		feeInfo.put(AbstractTransferServiceParams.PROFILT_RATES, profiltRates);

		return feeInfo;
	}

	private Map<String, BigDecimal> countFee(BigDecimal requestAmount, BigDecimal countRates, BigDecimal profiltRates,
			String ruleType) {

		Map<String, BigDecimal> feeInfo = new HashMap<String, BigDecimal>();

		BigDecimal sumFee = null;// requestAmount.multiply(countRates).setScale(2,
									// BigDecimal.ROUND_HALF_UP);

		if ("1".equals(ruleType)) {
			sumFee = requestAmount.multiply(countRates);
		} else if ("2".equals(ruleType)) {
			sumFee = requestAmount.divide(new BigDecimal(1).subtract(countRates), 9, BigDecimal.ROUND_HALF_UP)
					.multiply(countRates);
		} else {
			sumFee = new BigDecimal("0.00");
		}

		BigDecimal profitAmount = requestAmount.multiply(profiltRates).setScale(2, BigDecimal.ROUND_HALF_UP);

		feeInfo.put(AbstractTransferServiceParams.SUM_FEE, sumFee);
		feeInfo.put(AbstractTransferServiceParams.PROFIT_AMOUNT, profitAmount);
		feeInfo.put(AbstractTransferServiceParams.SUPPLEMENT_FEE, new BigDecimal("0.00"));
		feeInfo.put(AbstractTransferServiceParams.SUPPLEMENT_AMOUNT, new BigDecimal("0.00"));
		feeInfo.put(AbstractTransferServiceParams.CALCULATION_RATES, countRates);
		feeInfo.put(AbstractTransferServiceParams.PROFILT_RATES, profiltRates);

		return feeInfo;
	}

	private String getNameFromCache(String id) {
		return dataCache.getNameOfId(id);
	}

	private Map<UserGroupKey, List<AbstractTransferServiceParams>> getGroupTransferParams(
			TransformAndCheckResult result, Map<String, UserGroupKey> keyTable) {
		Map<UserGroupKey, List<AbstractTransferServiceParams>> groupByUserMerchantTransferCorpIdTable = new HashMap<UserGroupKey, List<AbstractTransferServiceParams>>();

		for (AbstractTransferServiceParams actionParams : result.getTransferServiceParamsList()) {
			String requestMerchantId = actionParams.getMerchantId();
			String certificateNo = actionParams.getCertificateNo();
			String requestTransferCorpId = actionParams.getTransferCorpId();
			String key = new StringBuilder(requestMerchantId).append(certificateNo).append(requestTransferCorpId)
					.toString();
			UserGroupKey groupKey = keyTable.get(key);
			if (groupKey == null) {
				groupKey = new UserGroupKey(requestMerchantId, certificateNo, requestTransferCorpId);
				keyTable.put(key, groupKey);
			}
			List<AbstractTransferServiceParams> paramGroupList = groupByUserMerchantTransferCorpIdTable.get(groupKey);
			if (paramGroupList == null) {
				paramGroupList = new ArrayList<AbstractTransferServiceParams>();
				groupByUserMerchantTransferCorpIdTable.put(groupKey, paramGroupList);
			}
			paramGroupList.add(actionParams);
		}

		for (Entry<UserGroupKey, List<AbstractTransferServiceParams>> entry : groupByUserMerchantTransferCorpIdTable
				.entrySet()) {
			entry.getValue().sort(new Comparator<AbstractTransferServiceParams>() {

				@Override
				public int compare(AbstractTransferServiceParams paramsA, AbstractTransferServiceParams paramsB) {
					return new BigDecimal(paramsA.getAmount()).compareTo(new BigDecimal(paramsB.getAmount()));
				}
			});
		}

		return groupByUserMerchantTransferCorpIdTable;
	}

	private static class UserGroupKey {

		private String merchantId;

		private String certificateNo;

		private String transferCorpId;

		protected UserGroupKey(String merchantId, String certificateNo, String transferCorpId) {
			super();
			this.merchantId = merchantId;
			this.certificateNo = certificateNo;
			this.transferCorpId = transferCorpId;
		}

		public String getMerchantId() {
			return merchantId;
		}

		public String getCertificateNo() {
			return certificateNo;
		}

		public String getTransferCorpId() {
			return transferCorpId;
		}
	}

	private static class MerchantGroupKey {

		private String merchantId;

		private String transferCorpId;

		protected MerchantGroupKey(String merchantId, String transferCorpId) {
			super();
			this.merchantId = merchantId;
			this.transferCorpId = transferCorpId;
		}

		public String getMerchantId() {
			return merchantId;
		}

		public String getTransferCorpId() {
			return transferCorpId;
		}
	}

	private static class UserVerifiedServiceParamsEntity {

		private AbstractTransferServiceParams serviceParams;

		private Map<String, BigDecimal> feeInfos;

		protected UserVerifiedServiceParamsEntity(AbstractTransferServiceParams serviceParams,
				Map<String, BigDecimal> feeInfos) {
			super();
			this.serviceParams = serviceParams;
			this.feeInfos = feeInfos;
		}

		public AbstractTransferServiceParams getServiceParams() {
			return serviceParams;
		}

		public Map<String, BigDecimal> getFeeInfos() {
			return feeInfos;
		}

	}
}
