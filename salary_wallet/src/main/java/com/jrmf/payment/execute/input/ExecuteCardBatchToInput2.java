package com.jrmf.payment.execute.input;

import com.jrmf.controller.constant.PayType;
import com.jrmf.controller.constant.SignShareStatus;
import com.jrmf.controller.constant.SignSubmitType;
import com.jrmf.domain.*;
import com.jrmf.oldsalarywallet.service.ChannelInterimBatchService;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.persistence.CommissionTemporary2Dao;
import com.jrmf.persistence.TransferBankDao;
import com.jrmf.persistence.UserCommission2Dao;
import com.jrmf.service.*;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ExecuteCardBatchToInput2 implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ExecuteCardBatchToInput2.class);
	public static final String PROCESS = "process";

	private String processId;

	private CountDownLatch cb;

	private List<Map<String, String>> param;

	private CommissionTemporary2Dao temporaryDao2;

	private TransferBankDao transferBankDao;

	private ChannelRelated channelRelated;

	private CustomCompanyRateConf customCompanyRateConf;

	private UserCommission2Dao userCommissionDao2;

	private Set<String> validateSet;

	private Map<String, String> batchData;

	private BankCardBinService cardBinService;

	private AgreementTemplateService agreementTemplateService;

	private OrderNoUtil orderNoUtil;

	private UsersAgreementService usersAgreementService;

	private CompanyService companyService;

	private QbBlackUsersService blackUsersService;

	private Integer minAge;

	private Integer maxAge;

	private ChannelInterimBatchService channelInterimBatchService;

	private SignShareService signShareService;

	private String realCompanyId;

	public ExecuteCardBatchToInput2(String processId,
									CountDownLatch cb,
									OrderNoUtil orderNoUtil,
									List<Map<String, String>> param,
									CommissionTemporary2Dao temporaryDao2,
									UserCommission2Dao userCommissionDao2,
									ChannelRelated channelRelated,
									TransferBankDao transferBankDao,
									CustomCompanyRateConf customCompanyRateConf, Map<String, String> batchData,
									Set<String> validateSet,
									AgreementTemplateService agreementTemplateService,
									BankCardBinService cardBinService,
									UsersAgreementService usersAgreementService,
									CompanyService companyService,
									Integer minAge,
									Integer maxAge,
									QbBlackUsersService blackUsersService, ChannelInterimBatchService channelInterimBatchService,
									SignShareService signShareService,String realCompanyId) {
		super();
		this.processId = processId;
		this.param = param;
		this.cb = cb;
		this.customCompanyRateConf = customCompanyRateConf;
		this.validateSet = validateSet;
		this.channelRelated = channelRelated;
		this.temporaryDao2 = temporaryDao2;
		this.orderNoUtil = orderNoUtil;
		this.transferBankDao = transferBankDao;
		this.userCommissionDao2 = userCommissionDao2;
		this.batchData = batchData;
		this.agreementTemplateService = agreementTemplateService;
		this.cardBinService = cardBinService;
		this.companyService = companyService;
		this.usersAgreementService = usersAgreementService;
		this.minAge = minAge;
		this.maxAge = maxAge;
		this.blackUsersService = blackUsersService;
		this.channelInterimBatchService = channelInterimBatchService;
		this.signShareService = signShareService;
		this.realCompanyId =realCompanyId;
	}

	@Override
	public void run() {

		MDC.put(PROCESS, processId);

		String batchId = batchData.get("batchId");
		String words = batchData.get("words");
		logger.info("------------导入银行卡下发佣金明细开始---处理数目" + param.size() + "，临时批次号：" + batchId + "---------");
		List<CommissionTemporary> commissionBatch = new ArrayList<>();

		Map<String, Object> paramMap = new HashMap<>(12);
		paramMap.put("companyId", channelRelated.getCompanyId());
		paramMap.put("originalId", channelRelated.getOriginalId());
		//先签约后支付
		paramMap.put("agreementPayment", "1");

		String businessManager = batchData.get("businessManager");
		String businessPlatform = batchData.get("businessPlatform");
		String customLabel = batchData.get("customLabel");
		String businessChannel = batchData.get("businessChannel");
		String businessChannelKey = batchData.get("businessChannelKey");
		String operationsManager = batchData.get("operationsManager");
		//获取服务公司签约规则
		SignElementRule signElementRule = signShareService.getSignElementRuleByCompanyId(channelRelated.getCompanyId());
		//获取商户配置的签约共享规则
		List<SignShare> signShareList = signShareService.getSignShareByCustomKey(channelRelated.getOriginalId(), channelRelated.getCompanyId());

		Company company = companyService.getCompanyByUserId(Integer.parseInt(channelRelated.getCompanyId()));
		String realCompanyId = company.getRealCompanyId();
		if(StringUtil.isEmpty(realCompanyId)){
			realCompanyId = channelRelated.getCompanyId();
		}
		if (this.realCompanyId!=null && !"".equals(this.realCompanyId)){
			realCompanyId =this.realCompanyId;
		}
		PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(
				String.valueOf(PayType.PINGAN_BANK.getCode()), channelRelated.getOriginalId(), realCompanyId);

		//封装导入临时批次数据
		for (Map<String, String> map : param) {
			String validateMsg = "";

			String userName = map.get("userName");
			String amount = map.get("amount");
			String bankCard = map.get("bankCard");
			String certId = map.get("certId");
			String bankName = map.get("bankName");
			String documentType = map.get("documentType");
			String remark = map.get("remark");
			String phoneNo = map.get("phoneNo");
			String bankNo = "";
			if ("身份证".equals(documentType)) {
				documentType = "1";
			} else if ("护照".equals(documentType)) {
				documentType = "3";
			} else if ("军官证".equals(documentType)) {
				documentType = "4";
			} else if ("港澳台通行证".equals(documentType)) {
				documentType = "2";
			} else {
				documentType = "0";
			}

			logger.info("----手机号判断-------------phoneNo:" + phoneNo + "--channelRelated.getMerchantId():" + channelRelated.getMerchantId() + "---判断结果：" + (StringUtils.isEmpty(phoneNo) && "aiyuangong".equals(channelRelated.getMerchantId())));

			boolean emptyFlag = false;

			String emptyMsg = "信息不完善";
			if (StringUtils.isEmpty(userName)) {
				emptyMsg = emptyMsg + "-姓名为空";
				emptyFlag = true;
			}
			if (StringUtils.isEmpty(amount)) {
				emptyMsg = emptyMsg + "-金额为空";
				emptyFlag = true;
			}
			if (!StringUtil.isNumber(amount)) {
				emptyMsg = emptyMsg + "-金额非数字";
				emptyFlag = true;
			}
			if (ArithmeticUtil.compareTod(amount, "0") <= 0) {
				emptyMsg = emptyMsg + "-金额需大于0";
				emptyFlag = true;
			}
			if ("0".equals(documentType)) {
				emptyMsg = emptyMsg + "-证件类型为空";
				emptyFlag = true;
			}
			if (StringUtils.isEmpty(bankCard)) {
				emptyMsg = emptyMsg + "-银行卡号为空";
				emptyFlag = true;
			}
			if (StringUtils.isEmpty(certId)) {
				emptyMsg = emptyMsg + "-证件号为空";
				emptyFlag = true;
			}
			if ((StringUtils.isEmpty(phoneNo) && "aiyuangong".equals(channelRelated.getMerchantId()))) {
				emptyMsg = emptyMsg + "-手机号为空";
				emptyFlag = true;
			}
			if (!StringUtil.isEmpty(remark) && !StringUtil.isEmpty(words) && remark.contains(words)) {
				emptyMsg = "备注包含敏感关键字,请联系运营人员";
				emptyFlag = true;
			}

			if ("keqijinyun".equals(channelRelated.getMerchantId())) {
				logger.info("金财下发，不校验手机号");
			} else {
				if (!StringUtil.isMobileNOBy11(phoneNo)) {
					emptyMsg = emptyMsg + "-手机号错误";
					emptyFlag = true;
				}
			}

			if (!validateSet.add(userName + bankCard + amount)) {
				//设置提示性话语
				validateMsg = "(信息重复)";
			}
			String msg;
			if ("1".equals(documentType)) {
				msg = StringUtil.isValidateData(amount,
						certId,
						bankCard,
						phoneNo,
						userName);

			} else {
				msg = StringUtil.isValidateData(amount,
						null,
						bankCard,
						phoneNo,
						null);
			}

			if (!StringUtil.isEmpty(msg)) {
				createCommission(amount,
						bankCard,
						bankName,
						null,
						userName,
						certId,
						documentType,
						2,
						msg,
						remark,
						customCompanyRateConf.getFeeRuleType() + "",
						phoneNo,
						businessManager,
						operationsManager,
						businessPlatform,
						customLabel,
						businessChannel,
						businessChannelKey,
						commissionBatch,
						realCompanyId);
				continue;
			}
			//黑名单校验
			QbBlackUsers blackUsers = new QbBlackUsers();
			blackUsers.setUserName(userName);
			blackUsers.setCustomkey(channelRelated.getOriginalId());
			blackUsers.setCertId(certId);
			blackUsers.setDocumentType(Integer.valueOf(documentType));
			int isBlack = blackUsersService.countExistByCertIdAndName(blackUsers);
			if (isBlack > 0) {
				emptyMsg = "风控限制用户，请联系运营人员";
				emptyFlag = true;
			}
			if (emptyFlag) {
				createCommission(amount,
						bankCard,
						bankName,
						null,
						userName,
						certId,
						documentType,
						2,
						emptyMsg,
						remark,
						customCompanyRateConf.getFeeRuleType() + "",
						phoneNo,
						businessManager,
						operationsManager,
						businessPlatform,
						customLabel,
						businessChannel,
						businessChannelKey,
						commissionBatch,
						realCompanyId);
				continue;
			}
			//白名单校验
			String isWhiteList = map.get("isWhiteList");
			if ("0".equals(isWhiteList)) {
				if (StringUtil.isEmpty(msg)) {
					//格式校验之后加上下发年龄校验
					msg = StringUtil.checkAge(certId, minAge, maxAge);
				}
				if (!StringUtil.isEmpty(msg)) {
					createCommission(amount,
							bankCard,
							bankName,
							null,
							userName,
							certId,
							documentType,
							2,
							msg,
							remark,
							customCompanyRateConf.getFeeRuleType() + "",
							phoneNo,
							businessManager,
							operationsManager,
							businessPlatform,
							customLabel,
							businessChannel,
							businessChannelKey,
							commissionBatch,
							realCompanyId);
					continue;
				}
				if ("aiyuangong".equals(channelRelated.getMerchantId())) {
					if (Double.parseDouble(amount) > 200000.00) {
						createCommission(amount,
								bankCard,
								bankName,
								null,
								userName,
								certId,
								documentType,
								2,
								"单笔转账不能超过二十万",
								remark,
								customCompanyRateConf.getFeeRuleType() + "",
								phoneNo,
								businessManager,
								operationsManager,
								businessPlatform,
								customLabel,
								businessChannel,
								businessChannelKey,
								commissionBatch,
								realCompanyId);
						continue;
					}
				}
				//中金通道bankNo为中金的银行编码
				if(paymentConfig != null && PaymentFactory.ZJPAY.equals(paymentConfig.getPathNo())){
					BankName cardBin = cardBinService.getBankName(bankCard);
					if ("true".equals(cardBin.getValidated())&&!StringUtil.isEmpty(cardBin.getBankCodeZJ())) {
						bankName = cardBin.getName();
						bankNo = cardBin.getBankCodeZJ();
					}else{
						createCommission(amount,
								bankCard,
								bankName,
								null,
								userName,
								certId,
								documentType,
								2,
								"银行卡格式校验不通过",
								remark,
								customCompanyRateConf.getFeeRuleType() + "",
								phoneNo,
								businessManager,
								operationsManager,
								businessPlatform,
								customLabel,
								businessChannel,
								businessChannelKey,
								commissionBatch,
								realCompanyId);
						continue;
					}
				}else{
					BankCard bankInfo = transferBankDao.getBankInfo(bankCard);
					if (bankInfo == null || StringUtil.isEmpty(bankInfo.getBankNo())) {
						// ali 接口查询
						BankName cardBin = cardBinService.getBankName(bankCard);
						if ("true".equals(cardBin.getValidated())) {
							bankName = cardBin.getName();
							bankNo = cardBin.getSuperNetNo();
							if (paymentConfig != null && PaymentFactory.YPDFDF.equals(paymentConfig.getPathNo())) {
								bankNo = cardBin.getBankCodeYP();
							}
						} else if ("false".equals(cardBin.getValidated())) {
							createCommission(amount,
									bankCard,
									bankName,
									null,
									userName,
									certId,
									documentType,
									2,
									"银行卡格式校验不通过",
									remark,
									customCompanyRateConf.getFeeRuleType() + "",
									phoneNo,
									businessManager,
									operationsManager,
									businessPlatform,
									customLabel,
									businessChannel,
									businessChannelKey,
									commissionBatch,
									realCompanyId);
							continue;
						}
					} else {
						bankNo = bankInfo.getBankNo() + "";
						bankName = bankInfo.getBankName();
					}
				}

				int signStatus = signShareService.checkUsersAgreement(signShareList, signElementRule, SignSubmitType.BATCH.getCode(), channelRelated.getOriginalId(), channelRelated.getCompanyId(), certId, userName);
				if (SignShareStatus.SIGN_SHARE_FAIL.getCode() == signStatus || SignShareStatus.SIGN_FAIL.getCode() == signStatus) {
					createCommission(amount,
							bankCard,
							bankName,
							null,
							userName,
							certId,
							documentType,
							2,
							"签约校验未通过，用户未创建或未签约",
							remark,
							customCompanyRateConf.getFeeRuleType() + "",
							phoneNo,
							businessManager,
							operationsManager,
							businessPlatform,
							customLabel,
							businessChannel,
							businessChannelKey,
							commissionBatch,
							realCompanyId);
					continue;
				} else if (SignShareStatus.SIGN_SHARE_SUCCESS.getCode() == signStatus) {
					//用于校验之后是否需要在mq执行批次落地
					batchData.put("sign_share_batchId", batchId);
				}

				Set<String> validateUserName = transferBankDao.getUserNameByCertId(certId);
				if (validateUserName.size() > 0 && !validateUserName.contains(userName)) {
					Set<String> validateCommsionUserName = userCommissionDao2.getCommissionsUserNameByCertId(certId);
					logger.info("------------导入银行卡发佣金------validateCommsionUserName:" + validateCommsionUserName.toArray());
					if (validateCommsionUserName.size() > 0 && !validateCommsionUserName.contains(userName)) {
						createCommission(amount,
								bankCard,
								bankName,
								null,
								userName,
								certId,
								documentType,
								2,
								"校验不通过，该身份证号有成功交易的姓名和当前的不一致",
								remark,
								customCompanyRateConf.getFeeRuleType() + "",
								phoneNo,
								businessManager,
								operationsManager,
								businessPlatform,
								customLabel,
								businessChannel,
								businessChannelKey,
								commissionBatch,
								realCompanyId);
						continue;
					}
				}

				createCommission(amount,
						bankCard,
						bankName,
						bankNo,
						userName,
						certId,
						documentType,
						1,
						"校验成功" + validateMsg,
						remark,
						customCompanyRateConf.getFeeRuleType() + "",
						phoneNo,
						businessManager,
						operationsManager,
						businessPlatform,
						customLabel,
						businessChannel,
						businessChannelKey,
						commissionBatch,
						realCompanyId);
			} else {
				createCommission(amount,
						bankCard,
						bankName,
						bankNo,
						userName,
						certId,
						documentType,
						1,
						"白名单用户",
						remark,
						customCompanyRateConf.getFeeRuleType() + "",
						phoneNo,
						businessManager,
						operationsManager,
						businessPlatform,
						customLabel,
						businessChannel,
						businessChannelKey,
						commissionBatch,
						realCompanyId);
			}
		}
		if (commissionBatch.size() != 0) {
			int count = temporaryDao2.addCommissionTemporary(commissionBatch);
			logger.info("------------导入银行卡下发佣金--插入条数:" + count + "----一致------------");
			if (count != commissionBatch.size()) {
				logger.info("------------导入银行卡发佣金--上送条数:" + commissionBatch.size() + "----插入条数:" + count + "不一致------------");
			}
		}
		cb.countDown();
		logger.info("------------导入银行卡下发佣金明细结束------------");
		MDC.remove(PROCESS);
	}


	private void createCommission(String amount,
								  String bankCard,
								  String bankName,
								  String bankNo,
								  String userName,
								  String idCard,
								  String documentType,
								  int status,
								  String statusDesc,
								  String remark,
								  String feeRuleType,
								  String phoneNo,
								  String businessManager,
									String operationsManager,
								  String businessPlatform,
								  String customLabel,
								  String businessChannel,
								  String businessChannelKey,
								  List<CommissionTemporary> commissionBatch,String realCompanyId) {
		String operatorName = batchData.get("operatorName");
		String batchId = batchData.get("batchId");
		String customkey = batchData.get("customkey");
		String menuId = batchData.get("menuId");
		CommissionTemporary commission = new CommissionTemporary();
		commission.setAmount(amount);
		commission.setBankCardNo(bankCard);
		commission.setIdCard(idCard);
		commission.setUserName(userName);

		//放开检验不通过才设置费率0，保证如果校验通过并且状态为充值预扣收时费率等信息为空
		//        if (status != 1) {//如果检验不通过，则设置费率为0
		commission.setSumFee("0.00");
		commission.setCalculationRates("0.00");
		commission.setSupplementAmount("0.00");
		commission.setSupplementFee("0.00");
		//        }
		commission.setStatus(status);
		commission.setBatchId(batchId);
		commission.setOriginalId(customkey);
		commission.setCompanyId(channelRelated.getCompanyId());
		commission.setOrderNo(orderNoUtil.getChannelSerialno());
		commission.setOperatorName(operatorName);
		commission.setRemark(remark);
		commission.setSourceRemark(remark);
		commission.setBankName(bankName);
		commission.setStatusDesc(statusDesc);
		commission.setBankNo(bankNo);
		commission.setPayType(4);
		commission.setBankNo(bankNo);
		commission.setMenuId(menuId);
		commission.setRepeatcheck(1);
		commission.setDocumentType(Integer.parseInt(documentType));
		commission.setFeeRuleType(feeRuleType);
		commission.setPhoneNo(phoneNo);
		commission.setBusinessManager(businessManager);
		commission.setOperationsManager(operationsManager);
		commission.setBusinessPlatform(businessPlatform);
		commission.setCustomLabel(customLabel);
		commission.setBusinessChannel(businessChannel);
		commission.setBusinessChannelKey(businessChannelKey);
		commission.setRealCompanyId(realCompanyId);
		if(!"白名单用户".equals(commission.getStatusDesc())){
			//        v 2.9.5 检验最小下发金额
			commission = channelInterimBatchService.checkCommissionTemporary(commission);
		}
		commissionBatch.add(commission);
	}
}
