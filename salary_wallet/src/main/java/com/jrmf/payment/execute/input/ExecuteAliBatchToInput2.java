package com.jrmf.payment.execute.input;

import com.jrmf.controller.constant.SignShareStatus;
import com.jrmf.controller.constant.SignSubmitType;
import com.jrmf.domain.*;
import com.jrmf.oldsalarywallet.service.ChannelInterimBatchService;
import com.jrmf.payment.util.AygPayBankName;
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

public class ExecuteAliBatchToInput2 implements Runnable {

  private Logger logger = LoggerFactory.getLogger(ExecuteAliBatchToInput2.class);
  public static final String PROCESS = "process";

  private String processId;

  private CountDownLatch cb;

  private List<Map<String, String>> param;

  private CommissionTemporary2Dao temporaryDao2;

  private ChannelRelated channelRelated;

  private OrderNoUtil orderNoUtil;

  private UserCommission2Dao userCommissionDao2;

  private TransferBankDao transferBankDao;

  private Set<String> validateSet;
  private CustomCompanyRateConf customCompanyRateConf;
  private Map<String, String> batchData;

  private AgreementTemplateService agreementTemplateService;

  private UsersAgreementService usersAgreementService;

  private Integer minAge;
  private Integer maxAge;

  private QbBlackUsersService blackUsersService;

  private ChannelInterimBatchService channelInterimBatchService;

  private CompanyService companyService;

  private SignShareService signShareService;

  private String realCompanyId;

  public ExecuteAliBatchToInput2(String processId,
      CountDownLatch cb,
      OrderNoUtil orderNoUtil,
      List<Map<String, String>> param,
      TransferBankDao transferBankDao,
      CommissionTemporary2Dao temporaryDao2,
      UserCommission2Dao userCommissionDao2,
      ChannelRelated channelRelated,
      CustomCompanyRateConf customCompanyRateConf, DataService dataService,
      Map<String, String> batchData,
      AgreementTemplateService agreementTemplateService,
      Set<String> validateSet,
      UsersAgreementService usersAgreementService, CompanyService companyService,
      Integer minAge,
      Integer maxAge,
      QbBlackUsersService blackUsersService,
      ChannelInterimBatchService channelInterimBatchService,
      SignShareService signShareService,
      String realCompanyId) {
    super();
    this.processId = processId;
    this.param = param;
    this.cb = cb;
    this.validateSet = validateSet;
    this.transferBankDao = transferBankDao;
    this.channelRelated = channelRelated;
    this.temporaryDao2 = temporaryDao2;
    this.orderNoUtil = orderNoUtil;
    this.userCommissionDao2 = userCommissionDao2;
    this.agreementTemplateService = agreementTemplateService;
    this.batchData = batchData;
    this.customCompanyRateConf = customCompanyRateConf;
    this.usersAgreementService = usersAgreementService;
    this.companyService = companyService;
    this.minAge = minAge;
    this.maxAge = maxAge;
    this.blackUsersService = blackUsersService;
    this.channelInterimBatchService = channelInterimBatchService;
    this.signShareService = signShareService;
    this.realCompanyId = realCompanyId;
  }

  @Override
  public void run() {

    MDC.put(PROCESS, processId);

    String batchId = batchData.get("batchId");
    String words = batchData.get("words");
    logger
        .info("------------导入支付宝发佣金明细开始---处理数目" + param.size() + "，临时批次号：" + batchId + "---------");

    Map<String, Object> paramMap = new HashMap<>(12);
    paramMap.put("companyId", channelRelated.getCompanyId());
    paramMap.put("originalId", channelRelated.getOriginalId());
    paramMap.put("agreementPayment", "1");//先签约后支付

    String businessManager = batchData.get("businessManager");
    String operationsManager = batchData.get("operationsManager");
    String businessPlatform = batchData.get("businessPlatform");
    String customLabel = batchData.get("customLabel");
    String businessChannel = batchData.get("businessChannel");
    String businessChannelKey = batchData.get("businessChannelKey");

    //获取服务公司签约规则
    SignElementRule signElementRule = signShareService
        .getSignElementRuleByCompanyId(channelRelated.getCompanyId());
    //获取商户配置的签约共享规则
    List<SignShare> signShareList = signShareService
        .getSignShareByCustomKey(channelRelated.getOriginalId(), channelRelated.getCompanyId());

    List<CommissionTemporary> commissionBatch = new ArrayList<CommissionTemporary>();
    Company company = companyService
        .getCompanyByUserId(Integer.parseInt(channelRelated.getCompanyId()));
    //真实下发公司id
    String realCompanyId = company.getRealCompanyId();
    if (StringUtil.isEmpty(realCompanyId)) {
      realCompanyId = channelRelated.getCompanyId();
    }
    if (this.realCompanyId != null && !"".equals(this.realCompanyId)) {
      realCompanyId = this.realCompanyId;
    }
    for (Map<String, String> map : param) {
      String validateMsg = "";
      String userName = map.get("userName");
      String amount = map.get("amount");
      String alipayAccount = map.get("alipayAccount");
      String certId = map.get("certId");
      String documentType = map.get("documentType");
      String remark = map.get("remark");
      String phoneNo = map.get("phoneNo");

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
      if (StringUtils.isEmpty(alipayAccount)) {
        emptyMsg = emptyMsg + "-支付宝账号为空";
        emptyFlag = true;
      }
//			if (StringUtils.isEmpty(certId)) {
//				emptyMsg = emptyMsg + "-证件号为空";
//				emptyFlag = true;
//			}
      if ((StringUtils.isEmpty(phoneNo) && "aiyuangong".equals(channelRelated.getMerchantId()))) {
        emptyMsg = emptyMsg + "-手机号为空";
        emptyFlag = true;
      }
      if (!StringUtil.isEmpty(remark) && !StringUtil.isEmpty(words) && remark.contains(words)) {
        emptyMsg = "备注包含敏感关键字,请联系运营人员";
        emptyFlag = true;
      }

      if ("keqijinyun".equals(channelRelated.getMerchantId()) || "keqijinyun"
          .equals(channelRelated.getMerchantId())) {
        logger.error("金财下发，不校验手机号");
      } else {
        if (!StringUtil.isMobileNOBy11(phoneNo)) {
          emptyMsg = emptyMsg + "-手机号错误";
          emptyFlag = true;
        }
      }

      if (!validateSet.add(userName + alipayAccount + amount)) {
        validateMsg = "(信息重复)";
      }
      String msg = "";
      if ("1".equals(documentType)) {

        msg = StringUtil.isValidateData(amount,
            certId,
            null,
            phoneNo,
            userName);
      }

      //身份证为空使用支付宝账号填充
      if (StringUtil.isEmpty(certId)) {
        certId = alipayAccount;
      }

      if (!StringUtil.isEmpty(msg)) {
        createCommission(amount,
            alipayAccount,
            AygPayBankName.ALI.getBankName(),
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
            alipayAccount,
            AygPayBankName.ALI.getBankName(),
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
          //以支付宝账户作为身份证的客户不校验年龄
          if (!alipayAccount.equals(certId)) {
            //格式校验之后加上下发年龄校验
            msg = StringUtil.checkAge(certId, minAge, maxAge);
          }
        }
        if (!StringUtil.isEmpty(msg)) {
          createCommission(amount,
              alipayAccount,
              AygPayBankName.ALI.getBankName(),
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
          if (Double.parseDouble(amount) > 50000.00) {
            createCommission(amount,
                alipayAccount,
                AygPayBankName.ALI.getBankName(),
                null,
                userName,
                certId,
                documentType,
                2,
                "单笔转账不能超过五万",
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

        //以支付宝账户作为身份证的客户不创建签约记录
        if (!alipayAccount.equals(certId)) {
          int signStatus = signShareService
              .checkUsersAgreement(signShareList, signElementRule, SignSubmitType.BATCH.getCode(),
                  channelRelated.getOriginalId(), channelRelated.getCompanyId(), certId, userName);
          if (SignShareStatus.SIGN_SHARE_FAIL.getCode() == signStatus
              || SignShareStatus.SIGN_FAIL.getCode() == signStatus) {
            createCommission(amount,
                alipayAccount,
                AygPayBankName.ALI.getBankName(),
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
        }

        Set<String> validateUserName = transferBankDao.getUserNameByCertId(certId);
        if (validateUserName.size() > 0 && !validateUserName.contains(userName)) {
          Set<String> validateCommsionUserName = userCommissionDao2
              .getCommissionsUserNameByCertId(certId);
          logger.info(
              "------------导入支付宝发佣金------validateCommsionUserName:" + validateCommsionUserName
                  .toArray());
          if (validateCommsionUserName.size() > 0 && !validateCommsionUserName.contains(userName)) {
            createCommission(amount,
                alipayAccount,
                AygPayBankName.ALI.getBankName(),
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
            alipayAccount,
            AygPayBankName.ALI.getBankName(),
            null,
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
            alipayAccount,
            AygPayBankName.ALI.getBankName(),
            null,
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
      logger.info("------------导入支付宝下发佣金--插入条数:" + count + "---------------");
      if (count != commissionBatch.size()) {
        logger.info("------------导入支付宝下发佣金--上送条数:" + commissionBatch.size() + "插入条数:" + count
            + "不一致------------");
      }
    }
    cb.countDown();
    logger.info("------------导入支付宝下发佣金明细结束------------");
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
      String businessChannelKey, List<CommissionTemporary> commissionBatch, String realCompanyId) {
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
    commission.setPayType(2);
    commission.setRepeatcheck(1);
    commission.setDocumentType(Integer.parseInt(documentType));
    commission.setBankNo(bankNo);
    commission.setMenuId(menuId);
    commission.setFeeRuleType(feeRuleType);
    commission.setPhoneNo(phoneNo);
    commission.setBusinessManager(businessManager);
    commission.setOperationsManager(operationsManager);
    commission.setBusinessPlatform(businessPlatform);
    commission.setCustomLabel(customLabel);
    commission.setBusinessChannel(businessChannel);
    commission.setBusinessChannelKey(businessChannelKey);
    commission.setRealCompanyId(realCompanyId);
    if (!"白名单用户".equals(commission.getStatusDesc())) {
      //        v 2.9.5 检验最小下发金额
      commission = channelInterimBatchService.checkCommissionTemporary(commission);
    }
    commissionBatch.add(commission);
    logger.info("--------createCommission-3---导入支付宝下发佣金----batchId-:" + batchId);

  }
}
