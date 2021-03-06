package com.jrmf.service;

import static com.jrmf.payment.PaymentFactory.MYBANK;
import static com.jrmf.payment.PaymentFactory.PAKHKF;
import static com.jrmf.payment.PaymentFactory.PAYQZL;
import static com.xxl.job.core.biz.model.ReturnT.FAIL_CODE;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.bankapi.ActionReturn;
import com.jrmf.bankapi.ReceiptFileResult;
import com.jrmf.bankapi.TransferResult;
import com.jrmf.bankapi.pingansub.SubAccountTransHistoryRecord;
import com.jrmf.bankapi.pingansub.params.SubAccountQueryHistoryTransferResultParam;
import com.jrmf.common.APIResponse;
import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.AccountTransStatus;
import com.jrmf.controller.constant.AccountTransType;
import com.jrmf.controller.constant.AgainCalculateType;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.ChannelTypeEnum;
import com.jrmf.controller.constant.ConfirmStatus;
import com.jrmf.controller.constant.CustomTransferRecordType;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.DataDictionaryDictKey;
import com.jrmf.controller.constant.DataDictionaryDictType;
import com.jrmf.controller.constant.DocumentStep;
import com.jrmf.controller.constant.GearLaberType;
import com.jrmf.controller.constant.HistoryStatus;
import com.jrmf.controller.constant.InvoiceStatusEnum;
import com.jrmf.controller.constant.LinkageTranStatus;
import com.jrmf.controller.constant.LinkageTranType;
import com.jrmf.controller.constant.LinkageType;
import com.jrmf.controller.constant.PathKeyTypeEnum;
import com.jrmf.controller.constant.PayType;
import com.jrmf.controller.constant.ReceiptImportType;
import com.jrmf.controller.constant.ReceiptStatus;
import com.jrmf.controller.constant.ReceiptStatusEnum;
import com.jrmf.controller.constant.RechargeStatusType;
import com.jrmf.controller.constant.ServiceFeeType;
import com.jrmf.controller.constant.SignStep;
import com.jrmf.controller.constant.SignSubmitType;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.controller.constant.UsersAgreementSignType;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.AgreementTemplate;
import com.jrmf.domain.BatchInvoiceCommission;
import com.jrmf.domain.CallBackInfo;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.Company;
import com.jrmf.domain.CustomBalanceHistory;
import com.jrmf.domain.CustomCompanyRateConf;
import com.jrmf.domain.CustomTransferRecord;
import com.jrmf.domain.CustomerFirm;
import com.jrmf.domain.DataDictionary;
import com.jrmf.domain.LdOrderCorrect;
import com.jrmf.domain.LinkAccountTrans;
import com.jrmf.domain.LinkageBaseConfig;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.QbClearingAccounts;
import com.jrmf.domain.ReceiptBatch;
import com.jrmf.domain.ReceiptCommission;
import com.jrmf.domain.UserCommission;
import com.jrmf.domain.UsersAgreement;
import com.jrmf.domain.vo.CompanyAccountVo;
import com.jrmf.oldsalarywallet.service.ChannelInterimBatchService;
import com.jrmf.payment.AccountSystemFactory;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.AccountSystem;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.entity.PingAnBankAccountSystem;
import com.jrmf.payment.entity.PingAnBankYqzl;
import com.jrmf.payment.execute.ExecuteBatchGrantLdQuery;
import com.jrmf.payment.execute.ExecuteBatchGrantQuery;
import com.jrmf.payment.execute.ExecuteLdCorrectQuery;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.model.request.deliver.ReceiptRequestParam;
import com.jrmf.payment.openapi.model.response.BaseResponseResult;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.util.ClientMapUtil;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.persistence.CustomBalanceDao;
import com.jrmf.persistence.CustomPaymentTotalAmountDao;
import com.jrmf.signContract.SignContractChannel;
import com.jrmf.signContract.SignContractChannelFactory;
import com.jrmf.taxsettlement.api.service.transfer.TransferDealStatusNotifier;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.EmailUtil;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

//import sun.plugin2.message.Message;

/**
 * ????????????
 *
 * @author chonglulu
 */
@Service("jobService")
public class JobService {

  private static Logger logger = LoggerFactory.getLogger(JobService.class);
  private static final Lock lock = new ReentrantLock();
  private static final Lock batchLock = new ReentrantLock();
  @Autowired
  private ChannelHistoryService channelHistoryService;
  @Autowired
  private UserCommissionService commissionService;
  @Autowired
  private CompanyService companyService;
  @Autowired
  private ChannelRelatedDao channelRelatedDao;
  @Autowired
  private CustomBalanceService customBalanceService;
  @Autowired
  private UtilCacheManager utilCacheManager;
  @Autowired
  private BaseInfo baseInfo;
  @Autowired
  private ReceiptService receiptService;
  @Autowired
  private OrderNoUtil orderNoUtil;
  @Autowired
  private CustomPaymentTotalAmountDao customPaymentTotalAmountDao;
  @Autowired
  private CustomLimitConfService customLimitConfService;
  @Autowired
  private UmfCommissionService umfCommissionService;
  @Autowired
  private ChannelCustomService channelCustomService;
  @Autowired
  private CallBackInfoService callBackInfoService;
  @Autowired
  private UsersAgreementService usersAgreementService;
  @Autowired
  private ChannelTaskService channelTaskService;
  @Autowired
  private LdOrderStepService ldOrderStepService;
  @Autowired
  private CustomBalanceDao customBalanceDao;
  @Autowired
  private TransferDealStatusNotifier transferDealStatusNotifier;
  @Autowired
  private LdOrderCorrectService ldOrderCorrectService;
  @Autowired
  private CustomTransferRecordService customTransferRecordService;
  @Autowired
  private CustomReceiveConfigService customReceiveConfigService;
  @Autowired
  private QbClearingAccountsService clearingAccountsService;
  @Autowired
  private ProxyCustomService proxyCustomService;
  @Autowired
  private DataDictionaryService dataDictionaryService;
  @Autowired
  private AgreementTemplateService agreementTemplateService;
  @Autowired
  private YmyfCommonService ymyfCommonService;
  @Autowired
  private ChannelInterimBatchService channelInterimBatchService;
  @Autowired
  private LinkageTransferRecordService linkageTransferRecordService;
  @Autowired
  private LinkageCustomConfigService linkageCustomConfigService;
  @Autowired
  private LinkAccountTransService linkAccountTransService;
  @Autowired
  private SmsService smsService;
  @Autowired
  private UserSerivce userSerivce;
  @Autowired
  private CustomThirdPaymentConfigServiceImpl customThirdPaymentConfigService;
  @Autowired
  private ForwardCompanyAccountService forwardCompanyAccountService;
  @Autowired
  private CustomCompanyRateConfService customCompanyRateConfService;
  @Autowired
  private BatchInvoiceCommissionService batchInvoiceCommissionService;
  @Autowired
  private CustomerFirmService customerFirmService;

  @Value("${companyId}")
  private String companyId;

  public static final String PROCESS = "process";

  /**
   * ????????????---????????????????????????
   */
  @XxlJob("bankPayInitStatus")
  public ReturnT<String> bankPayInitStatus(String args) {
    try {
      String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
      MDC.put(PROCESS, processId);

      logger.info("????????????--??????--????????????????????????");
      lock.lock();
      List<UserCommission> list = commissionService.getListByTypeAndStatusOnJob(3, "2,4");
      logger.info("????????????--??????--????????????????????????---?????????" + list.size());
      if (list.size() > 0) {

        List<List<UserCommission>> averageAssign = StringUtil.averageAssign(list, 2);
        for (int i = 0; i < averageAssign.size(); i++) {
          String subProcessId = processId + "--" + i;
          ThreadUtil.cashThreadPool.execute(
              new ExecuteBatchGrantQuery(subProcessId, averageAssign.get(i), commissionService,userSerivce,
                  channelRelatedDao, companyService, utilCacheManager, baseInfo,
                  customLimitConfService, customBalanceService, ymyfCommonService,
                  channelHistoryService, channelInterimBatchService, smsService,
                  channelCustomService,usersAgreementService));
        }
      }
      logger.info("????????????--??????--????????????????????????");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return new ReturnT<>(FAIL_CODE, e.toString());
    } finally {
      lock.unlock();
      MDC.remove(PROCESS);
    }
    return ReturnT.SUCCESS;
  }

  /**
   * ????????????---??????????????????????????????????????????
   */
  @XxlJob("initChannelHistoryStatus")
  public ReturnT<String> initChannelHistoryStatus(String args) {
    try {
      String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
      MDC.put(PROCESS, processId);
      batchLock.lock();
      logger.info("????????????--??????--????????????????????????");
      // ?????? ?????????????????????????????????
      Map<String, Object> param = new HashMap<>(5);
      param.put("transfertype", 2);
      param.put("status", HistoryStatus.SUBMITTED.getCode());
      List<ChannelHistory> list = channelHistoryService.getChannelHistoryByParamOnJob(param);
      update(list);
      logger.info("????????????--??????---????????????????????????");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return new ReturnT<>(FAIL_CODE, e.toString());
    } finally {
      MDC.remove(PROCESS);
      batchLock.unlock();
    }
    return ReturnT.SUCCESS;
  }

  public void update(List<ChannelHistory> list) {
    for (ChannelHistory history : list) {
      try {

        // ????????????????????????
        int errorCount = commissionService.getBatchNum(history.getId() + "", "2");
        int successCount = commissionService.getBatchNum(history.getId() + "", "1");

        // ?????????????????????????????????????????????
        if (errorCount == 0 && successCount == 0) {
          continue;
        }
        // ?????????????????????????????????
        int sendCount = commissionService.getBatchNum(history.getId() + "", "3");
        int noSendCount = commissionService.getBatchNum(history.getId() + "", "0");
        boolean flag = false;
        if (sendCount == 0 && noSendCount == 0) {
          // ???????????????????????????????????????????????????
          if (errorCount == 0) {
            if (successCount == 0) {
              history.setStatus(HistoryStatus.FAILURE.getCode());
            } else {
              history.setStatus(HistoryStatus.SUCCESS.getCode());
            }
          } else {
            if (successCount == 0) {
              history.setStatus(HistoryStatus.FAILURE.getCode());
            } else {
              history.setStatus(HistoryStatus.PARTIALFAILURE.getCode());
            }
          }
          history.setProvidetime(DateUtils.getNowDate());
          history.setCurrentStatus(HistoryStatus.SUBMITTED.getCode());
          flag = channelHistoryService.updateChannelHistory(history) == 1;
        }
        // ??????????????????
        commissionService.updateBatchData(history.getId() + "", history.getCustomkey());
        if (flag && (HistoryStatus.FAILURE.getCode() == history.getStatus() || HistoryStatus.PARTIALFAILURE.getCode() == history.getStatus())) {
          ChannelHistory channelHistoryById = channelHistoryService
              .getChannelHistoryById(history.getId() + "");
          int deductionAmount = channelHistoryById.getDeductionAmount();
          String handleAmount = channelHistoryById.getHandleAmount();
          String sub = ArithmeticUtil.divideStr(String.valueOf(deductionAmount), "100");
//          BigDecimal cost = new BigDecimal(handleAmount);
          String amount = ArithmeticUtil
              .subStr(sub,
                  handleAmount, 2);

          customBalanceService.updateCustomBalance(CommonString.ADDITION,
              new CustomBalanceHistory(channelHistoryById.getCustomkey(),
                  channelHistoryById.getRecCustomkey(), channelHistoryById.getPayType(), amount
                  , channelHistoryById.getFailedNum(), TradeType.PAYMENTREFUND.getCode(),
                  channelHistoryById.getOrderno(), "webTaskJob"));

          if (!StringUtils.isEmpty(channelHistoryById.getRecCustomkey()) &&
              !StringUtils.isEmpty(channelHistoryById.getRealCompanyId())
              && !channelHistoryById.getRecCustomkey()
              .equals(channelHistoryById.getRealCompanyId())) {

            /**
             * @Description ???????????????????????????????????????????????????
             **/
            CustomBalanceHistory queryBalanceHistory = new CustomBalanceHistory();
            queryBalanceHistory.setTradeType(TradeType.WEBPAYMENT.getCode());
            queryBalanceHistory.setRelateOrderNo(channelHistoryById.getOrderno());
            queryBalanceHistory.setCustomKey(channelHistoryById.getRecCustomkey());
            queryBalanceHistory.setCompanyId(channelHistoryById.getRealCompanyId());
            List<CustomBalanceHistory> customBalanceHistories = customBalanceService
                .listCustomBalanceHistory(queryBalanceHistory);
            if (!CollectionUtils.isEmpty(customBalanceHistories)) {

              CustomCompanyRateConf customCompanyMinRate = customCompanyRateConfService
                  .getCustomCompanyMinRate(channelHistoryById.getRecCustomkey(),
                      channelHistoryById.getRealCompanyId());

              String additionAmount = channelHistoryById.getFailedAmount();
              if (ServiceFeeType.ISSUE.getCode() == customCompanyMinRate.getServiceFeeType()
                  || ServiceFeeType.PERSON.getCode() == customCompanyMinRate.getServiceFeeType()) {
                additionAmount = ArithmeticUtil
                    .subStr(channelHistoryById.getFailedAmount(), ArithmeticUtil
                        .mulStr(channelHistoryById.getFailedAmount(),
                            customCompanyMinRate.getCustomRate(), 2), 2);
              }

              CustomBalanceHistory customBalanceHistory = new CustomBalanceHistory(
                  channelHistoryById.getRecCustomkey(),
                  channelHistoryById.getRealCompanyId(), channelHistoryById.getPayType(),
                  additionAmount, channelHistoryById.getFailedNum(), TradeType.PAYMENTREFUND.getCode(),
                  channelHistoryById.getOrderno(),
                  "webTaskJob");
              customBalanceService.updateCustomBalance(CommonString.ADDITION, customBalanceHistory);
            }

            /**
             * @Description ???????????????
             **/
            CompanyAccountVo accountVo = new CompanyAccountVo();
            accountVo.setBalance(amount);
            accountVo.setCustomKey(channelHistoryById.getCustomkey());
            accountVo.setTradeType(TradeType.PAYMENTREFUND.getCode());
            accountVo.setRelateOrderNo(channelHistoryById.getOrderno());
            accountVo.setCompanyId(channelHistoryById.getRecCustomkey());
            accountVo.setRealCompanyId(channelHistoryById.getRealCompanyId());
            accountVo.setAmount(channelHistoryById.getFailedNum());
            accountVo.setOperating(CommonString.ADDITION);
            APIResponse response = forwardCompanyAccountService.updateCompanyAccount(accountVo);


          }

          logger.info("{}?????????????????????{}-??????{}=??????{}", history.getId(), sub,
              handleAmount, amount);
        }
      } catch (Exception e) {
        logger.error(e.getMessage(),e);
      }
    }
  }

  @XxlJob("receiptBatchCreateJob")
  public ReturnT<String>  receiptBatchCreateJob(String args) {
    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);
    logger.info("????????????--??????--????????????");

    List<ReceiptBatch> receiptBatchList = null;

    //?????????
    Map<String, Object> params = new HashMap<>(20);
    params.put("merchantId", "aiyuangong");
    params.put("payType", "4");
    params.put("status", "1");
    params.put("receiptTime", DateUtils.getBeforeDayString(2));

    receiptBatchList = receiptService.listReceiptBatchGroup(params);
    for (ReceiptBatch batch : receiptBatchList) {//?????????????????????????????????????????????

      ReceiptBatch batchNew = new ReceiptBatch();
      batchNew.setStatus(0);
      batchNew.setCommissionNum(batch.getCommissionNum());
      batchNew.setCompanyId(batch.getCompanyId());
      batchNew.setPayType(batch.getPayType());
      batchNew.setReceiptTime(batch.getReceiptTime());
      batchNew.setReceiptType("0");
      batchNew.setReceiptOrgType("3");
      batchNew.setReceiptOrgName("ayg");
      batchNew.setMerchantId(batch.getMerchantId());

      receiptService.saveReceiptBatch(batchNew);
      logger.info("????????????--??????--??????--?????????id???" + batchNew.getId());
    }

    //????????????
    params.clear();
    params.put("merchantId", "shebaokeji");
    params.put("payType", "4");
    params.put("status", "1");
    params.put("receiptTime", DateUtils.getBeforeDayString(1));

    receiptBatchList = receiptService.listReceiptBatchGroup(params);
    for (ReceiptBatch batch : receiptBatchList) {//?????????????????????????????????????????????

      ReceiptBatch batchNew = new ReceiptBatch();
      batchNew.setStatus(0);
      batchNew.setCommissionNum(batch.getCommissionNum());
      batchNew.setCompanyId(batch.getCompanyId());
      batchNew.setPayType(batch.getPayType());
      batchNew.setMerchantId(batch.getMerchantId());
      batchNew.setReceiptTime(batch.getReceiptTime());
      batchNew.setReceiptType("1");
      batchNew.setReceiptOrgType("1");
      batchNew.setReceiptOrgName("pa");

      receiptService.saveReceiptBatch(batchNew);
      logger.info("????????????--??????--??????--????????????id???" + batchNew.getId());
    }

    //????????????
    params.clear();
    params.put("merchantId", "keqijinyun");
    params.put("payType", "4");
    params.put("status", "1");
    params.put("receiptTime", DateUtils.getBeforeDayString(1));

    receiptBatchList = receiptService.listReceiptBatchGroup(params);
    for (ReceiptBatch batch : receiptBatchList) {//?????????????????????????????????????????????

      ReceiptBatch batchNew = new ReceiptBatch();
      batchNew.setStatus(0);
      batchNew.setCommissionNum(batch.getCommissionNum());
      batchNew.setCompanyId(batch.getCompanyId());
      batchNew.setPayType(batch.getPayType());
      batchNew.setMerchantId(batch.getMerchantId());
      batchNew.setReceiptTime(batch.getReceiptTime());
      batchNew.setReceiptType("1");
      batchNew.setReceiptOrgType("1");
      batchNew.setReceiptOrgName("pa");

      receiptService.saveReceiptBatch(batchNew);
      logger.info("????????????--??????--??????--????????????id???" + batchNew.getId());
    }

    //?????????
    params.clear();
    params.put("merchantId", "huiyonggong");
    params.put("payType", "4");
    params.put("status", "1");
    params.put("receiptTime", DateUtils.getBeforeDayString(1));

    receiptBatchList = receiptService.listReceiptBatchGroup(params);
    for (ReceiptBatch batch : receiptBatchList) {//?????????????????????????????????????????????

      ReceiptBatch batchNew = new ReceiptBatch();
      batchNew.setStatus(0);
      batchNew.setCommissionNum(batch.getCommissionNum());
      batchNew.setCompanyId(batch.getCompanyId());
      batchNew.setPayType(batch.getPayType());
      batchNew.setMerchantId(batch.getMerchantId());
      batchNew.setReceiptTime(batch.getReceiptTime());
      batchNew.setReceiptType("1");
      batchNew.setReceiptOrgType("1");
      batchNew.setReceiptOrgName("pa");

      receiptService.saveReceiptBatch(batchNew);
      logger.info("????????????--??????--??????--?????????id???" + batchNew.getId());
    }

    //?????????
    params.clear();
    params.put("merchantId", "xiaohuangfeng");
    params.put("payType", "4");
    params.put("status", "1");
    params.put("receiptTime", DateUtils.getBeforeDayString(1));

    receiptBatchList = receiptService.listReceiptBatchGroup(params);
    for (ReceiptBatch batch : receiptBatchList) {//?????????????????????????????????????????????

      ReceiptBatch batchNew = new ReceiptBatch();
      batchNew.setStatus(0);
      batchNew.setCommissionNum(batch.getCommissionNum());
      batchNew.setCompanyId(batch.getCompanyId());
      batchNew.setPayType(batch.getPayType());
      batchNew.setMerchantId(batch.getMerchantId());
      batchNew.setReceiptTime(batch.getReceiptTime());
      batchNew.setReceiptType("1");
      batchNew.setReceiptOrgType("1");
      batchNew.setReceiptOrgName("pa");

      receiptService.saveReceiptBatch(batchNew);
      logger.info("????????????--??????--??????--?????????id???" + batchNew.getId());
    }

    //????????????
    params.clear();
    params.put("merchantId", "mybank");
    params.put("payType", "4");
    params.put("status", "1");
    params.put("receiptTime", DateUtils.getBeforeDayString(1));

    receiptBatchList = receiptService.listReceiptBatchGroup(params);
    for (ReceiptBatch batch : receiptBatchList) {//?????????????????????????????????????????????

      ReceiptBatch batchNew = new ReceiptBatch();
      batchNew.setStatus(0);
      batchNew.setCommissionNum(batch.getCommissionNum());
      batchNew.setCompanyId(batch.getCompanyId());
      batchNew.setPayType(batch.getPayType());
      batchNew.setMerchantId(batch.getMerchantId());
      batchNew.setReceiptTime(batch.getReceiptTime());
      batchNew.setReceiptType("1");
      batchNew.setReceiptOrgType("1");
      batchNew.setReceiptOrgName("pa");

      receiptService.saveReceiptBatch(batchNew);
      logger.info("????????????--??????--??????--????????????id???" + batchNew.getId());
    }

    logger.info("????????????--??????--????????????--");
    MDC.remove(PROCESS);
    return ReturnT.SUCCESS;
  }

  //????????????

  @XxlJob("receiptCommissionCreateJob")
  public ReturnT<String> receiptCommissionCreateJob(String args) {
    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);
    logger.info("????????????--??????--????????????--");
    Map<String, Object> params = new HashMap<>(20);

    params.put("merchantId", "aiyuangong");
    params.put("payType", "4");
    params.put("status", "1");
    params.put("receiptTime", DateUtils.getBeforeDayString(2));
    receiptService.addReceipt(params);

    params.clear();
    params.put("merchantId", "shebaokeji");
    params.put("payType", "4");
    params.put("status", "1");
    params.put("receiptTime", DateUtils.getBeforeDayString(1));
    receiptService.addReceipt(params);

    params.clear();
    params.put("merchantId", "keqijinyun");
    params.put("payType", "4");
    params.put("status", "1");
    params.put("receiptTime", DateUtils.getBeforeDayString(1));
    receiptService.addReceipt(params);

    params.clear();
    params.put("merchantId", "huiyonggong");
    params.put("payType", "4");
    params.put("status", "1");
    params.put("receiptTime", DateUtils.getBeforeDayString(1));
    receiptService.addReceipt(params);

    params.clear();
    params.put("merchantId", "xiaohuangfeng");
    params.put("payType", "4");
    params.put("status", "1");
    params.put("receiptTime", DateUtils.getBeforeDayString(1));
    receiptService.addReceipt(params);

    params.clear();
    params.put("merchantId", "mybank");
    params.put("payType", "4");
    params.put("status", "1");
    params.put("receiptTime", DateUtils.getBeforeDayString(1));
    receiptService.addReceipt(params);

    logger.info("????????????--??????--????????????--");
    MDC.remove(PROCESS);
    return ReturnT.SUCCESS;
  }

  public void aygRecepitRequestJob() {
    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);
    logger.info("????????????--???????????????---?????????????????????---????????????--");
    OpenApiClient client = null;
    String priKeyString = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCcdJ+nqtInOaHn/fZTbxdj5oTMFU6AJKD3DWmoYqUGcMCebtpdKhT/BbAb/t8Rsp1QLv1r4h+nf7s98LZkV9J6dSa5iHDIqCGT12byaTftKrYZlf3Hr8SFDkzBPTSBz56LRnYCRaYlrMQgZgY9U/T5UIwKVhJEordPvwbH2MqueKxUdk9vD4S8Dj7rEgor31Ifc3E/qRivKcRCncWBpiAtOTNSPLtsx0TIxizPXYicjL8Tr2+eLnuUxjKhWnFxvpRMC2ghLVrsWiVU0oW/0a6Qbv+TtB4/xR1JWV+pn4fqQhiJAssMHN4kEn7JxPpXB+/MzWUfrrGsYnSlNwpxPojzAgMBAAECggEAYJNEsfSpwHi8zj1fveTHJW1375n/WO5DRfzLiZtKjo0u+R0oQXXme/0A1mcfPwdoP8ShveRY8cXQyM07aPkk/V4vRztHkzTldSLzcxMr6IQC4AxMGOUQg6luC6JCNRb5oLMfyQtBIeRhNDaGB3k5sGPd7ctvf1qJmPorr1TM16C9vW8MdF4YjOfxpvm2g5AuaFHB3O9jTd6+tN3oAiusC3TwjtvhfQDmnPqfJOdp2x2NZAf47luU7J/eiEvyEPkjfmwSIQKvXfzIw0uu2rLX+52OmtJlJ8DYF8Ep9iCSJJMLiHhxYkSkNuwyB5/5oVZmRGj8mZVNdfIXyCaFXnEw0QKBgQDzZFAYCg+QRQZx9fdWwM4mSeHRCeZpQ9QBGjwNN0yWF+XioNtnMIr/pZYV/25biVVF5Pc/20r/8ZSBCShgR3u1ePuoaxzU907lT3R7tBoqaIfvjEBaZuOQcaGJqvNNJnySmhP1PnsjD5x9wWwer7nJLvl8UqmgbED0gKoClfWopwKBgQCkj2ypjvo6fHg19huw96y7CaYQjljWslY6GsWzE1WqSVy0xJb6JEzARZe38XhXPiAeg9mhlyUfkum8LJaYGe1JA5a2c9qhUOPfFeGM6PblGcF1jlMZYLv8shhxkjeWQA/kJngbW6lCHQBbc2G/W7irUC+PbkZkknAcYkYlQE3a1QKBgD9fVxtrQzIlRtBVYtlLymFdy1ZKZZvy9Th0RD6Mr3xFLK4dhAMSOJ7n1nRT1cAvuexA+b++sYCCvk/6unCXLDbMEXqAqTkqS3iZf5LWChoQrZRJyFfBgm8RpyXZRRBJfRYO2DN62UT/w5dazXQP/SfM+1jLjS8gAKmo9ptFwHjxAoGAK3y3g4uENwaDogb6xGZ/YCIpn4Bum7YfMVW33x4B6nFerWqyV0JWgg0iDfsjCTMiu82uKpTNu61QVWkXFvTrDvuCzY6KPU0qGt8mbt11uY9334AQF8nHg/zwlrrEM9GUIX/FB73OWeleGczBDRfJEoSrPOUwdw130RhrXxbCPE0CgYEAvXFClSExFaNvOz5eowwn09mOtgWEJEU6TFBEJNVoCaqavQlpelIyLGMA/+KP6Xg3ZvbfPTf1iTQhX3nLhOBEV4DanUo+9SH+96j0petltFgzkogf7vw3NHDf/bAuYAvyecrhRXAnRSy/YIAqF/p3nZDird3vSVgvdkjKtAUGrsc=";

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("receiptTime", DateUtils.getBeforeDayString(2));
    params.put("payType", "4");
    params.put("merchantId", "aiyuangong");

    List<ReceiptCommission> listReceipt = receiptService.listReceiptCommission(params);
    for (ReceiptCommission commission : listReceipt) {

      ChannelRelated related = channelRelatedDao
          .getRelatedByCompAndOrig(commission.getOriginalId(), commission.getCompanyId());
      String aygAppId = related.getAppIdAyg();
      logger.info("-----????????????--???????????????---?????????????????????----?????????appid------------:" + aygAppId);
      client = ClientMapUtil.httpClient.get(aygAppId);

      if (client == null) {
        synchronized (ClientMapUtil.httpClient) {
          client = ClientMapUtil.httpClient.get(aygAppId);
          if (client == null) {
            client = new OpenApiClient.Builder().appId(aygAppId).privateKey(priKeyString).build();
            ClientMapUtil.httpClient.putIfAbsent(aygAppId, client);
          }
        }
      }

      ReceiptRequestParam param = new ReceiptRequestParam();
      param.setReqNo(orderNoUtil.getChannelSerialno());
      param.setNotifyUrl(baseInfo.getDomainName() + "/receipt/receiptAsyncNotify.do");
      param.setOutOrderNo(commission.getOrderNo());
      param.setAttach("??????");
      OpenApiBaseResponse<BaseResponseResult<String>> response = client.execute(param);
      logger.info("-----????????????--???????????????---?????????????????????----?????????response------------:" + response);
      String aygReceiptStatus;
      String aygCode = response.getCode();
      if (OpenApiBaseResponse.SUCCESS_CODE.equals(aygCode)) {
        aygReceiptStatus = "1";
      } else if ("2002".equals(aygCode) || "2101".equals(aygCode)) {
        aygReceiptStatus = "2";
      } else {
        aygReceiptStatus = "3";
      }

      params.clear();
      params.put("aygRreceiptStatus", aygReceiptStatus);
      params.put("accountDate", commission.getAccountDate());
      params.put("receiptNo", commission.getReceiptNo());
      receiptService.updateReceiptCommissionByReceiptNo(params);

    }
    logger.info("????????????--???????????????---?????????????????????---????????????--");
    MDC.remove(PROCESS);
  }

  public void aygRecepitBatchJob() {
    receiptService.initAygRecepitBatchJob();
  }

  @XxlJob("initDayPaymentTotalAmountJob")
  public ReturnT<String> initDayPaymentTotalAmountJob(String args) {
    initPaymentTotalAmount("???", "D");
    return ReturnT.SUCCESS;
  }

  @XxlJob("initMonthPaymentTotalAmountJob")
  public ReturnT<String> initMonthPaymentTotalAmountJob(String args) {
    initPaymentTotalAmount("???", "M");
    return ReturnT.SUCCESS;
  }

  @XxlJob("initQuarterPaymentTotalAmountJob")
  public ReturnT<String> initQuarterPaymentTotalAmountJob(String args) {
    initPaymentTotalAmount("??????", "Q");
    return ReturnT.SUCCESS;
  }

  /**
   * ??????????????????????????????????????????????????????????????????
   *
   * @param desc ??????????????????
   * @param flag ??????????????????
   */
  private void initPaymentTotalAmount(String desc, String flag) {
    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);
    logger.info("??????" + desc + "??????????????????--?????????---????????????---");
    Map<String, Object> param = new HashMap<>(2);
    param.put("timeFlag", flag);
    int initCount = customLimitConfService.initDayMonthPaymentTotalAmount(param);
    logger.info("??????" + desc + "??????????????????--?????????--?????????" + initCount + "-????????????---");
    MDC.remove(PROCESS);
  }

  /**
   * ??????????????????????????????
   */
  public void initUmfintechReconciliationDocuments() {
    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);
    logger.info("??????????????????????????????--?????????---????????????---");
    LocalDate localDate = LocalDate.now().minusDays(1);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    String time = localDate.format(dateTimeFormatter);
    Map<String, Object> params = new HashMap<>(2);
    params.put("customType", CustomType.COMPANY.getCode());
    params.put("umfIdLength", "umfIdLength");
    List<ChannelCustom> companyList = channelCustomService.getCustomByParam(params);
    for (ChannelCustom channelCustom : companyList) {
      String umfId = channelCustom.getUmfId();
      logger.info("??????????????????????????????????????????" + umfId);
      umfCommissionService.downloadUserCommission(umfId, time);
    }

    MDC.remove(PROCESS);
  }


  /**
   * ??????????????????????????????
   */
  @XxlJob("initAgreementSignNotify")
  public ReturnT<String> initAgreementSignNotify(String args) {
    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);
    logger.info("??????????????????????????????--?????????---????????????---");
    List<CallBackInfo> list = callBackInfoService.getNotifyCallBackInfos();
    logger.info("???????????????" + list.size());
    for (CallBackInfo callBackInfo : list) {
      String serialNo = callBackInfo.getSerialNo();
      Map<String, Object> params = new HashMap<>(2);
      params.put("thirdNo", serialNo);
      List<UsersAgreement> usersAgreements = usersAgreementService
          .getUsersAgreementsByParams(params);
      if (!usersAgreements.isEmpty()) {
        checkStatus(usersAgreements, callBackInfo);
      } else {
        logger.info("??????????????????");
        callBackInfo.setNotifyCount(10);
        callBackInfo.setNotifyContent("?????????????????????????????????");
        callBackInfoService.updateCallBackInfo(callBackInfo);
      }

    }

    MDC.remove(PROCESS);
    return ReturnT.SUCCESS;
  }

  private void checkStatus(List<UsersAgreement> usersAgreements, CallBackInfo callBackInfo) {
    int signFail = 0;
    for (UsersAgreement usersAgreement : usersAgreements) {
      if (usersAgreement.getSignStatus() == UsersAgreementSignType.SIGN_SUCCESS.getCode()) {
        usersAgreementService.afterSignProcess(1, callBackInfo.getSerialNo());
        return;
      }
      if (usersAgreement.getSignStep() == SignStep.SIGN_SUCCESS.getCode()
          && usersAgreement.getDocumentStep() == DocumentStep.DOCUMENT_SUCCESS.getCode()) {
        usersAgreementService.checkSignStatus(usersAgreement);
      }
      if (usersAgreement.getSignStatus() == UsersAgreementSignType.SIGN_FAIL.getCode()) {
        signFail++;
      }
    }
    //????????????
    if (signFail == usersAgreements.size()) {
      usersAgreementService.afterSignProcess(0, callBackInfo.getSerialNo());
    }
  }


  /**
   * ????????????????????????
   */
  @XxlJob("initAutogenerateTask")
  public ReturnT<String> initAutogenerateTask(String args) {
    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);
    logger.info("????????????????????????--?????????---????????????---");

    channelTaskService.autogenerateTask(null, null, null, null, null, null, null);

    MDC.remove(PROCESS);
    return ReturnT.SUCCESS;
  }

  /**
   * 72??????????????????????????????????????????
   */
  public void initClosePrepareUnifiedOrder() {
    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);
    logger.info("72??????????????????????????????????????????--?????????---????????????---");
    Map<Object, Object> params = new HashMap<>(4);
    params.put("status", 5);
    params.put("regType", "02");
    params.put("diffTime", 2);
    List<UserCommission> list = commissionService.getUnClosedPrepareUnifiedCommissions(params);
    for (UserCommission userCommission : list) {
      logger.info("????????????,????????????[{}]", userCommission.getOrderNo());
      userCommission.setStatus(2);
      userCommission.setStatusDesc("??????????????????");
      commissionService.updateUserCommission(userCommission);
      logger.info("??????????????????");
      customLimitConfService.updateCustomPaymentTotalAmount(userCommission.getCompanyId(),
          userCommission.getOriginalId(), userCommission.getCertId(), userCommission.getAmount(),
          false);
    }
    MDC.remove(PROCESS);
  }

  /**
   * ????????????????????????
   */
  @XxlJob("bankPayInitLdStatus")
  public ReturnT<String> bankPayInitLdStatus(String args) {
    try {
      String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
      MDC.put(PROCESS, processId);

      logger.info("????????????-????????????--????????????????????????");
      lock.lock();
      List<UserCommission> list = commissionService.getLdListByTypeAndStatusOnJob(3, "4");
      logger.info("????????????--??????z??????--????????????????????????---?????????" + list.size());
      if (list.size() > 0) {
        List<List<UserCommission>> averageAssign = StringUtil.averageAssign(list, 5);
        for (int i = 0; i < averageAssign.size(); i++) {
          String subProcessId = processId + "--" + i;
          ThreadUtil.cashThreadPool.execute(
              new ExecuteBatchGrantLdQuery(subProcessId, averageAssign.get(i), commissionService,
                  companyService, utilCacheManager, customLimitConfService,
                  customPaymentTotalAmountDao, ldOrderStepService, customBalanceDao, baseInfo,
                  transferDealStatusNotifier));
        }
      }
      logger.info("????????????--??????--????????????????????????");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return new ReturnT<>(FAIL_CODE, e.toString());
    } finally {
      MDC.remove(PROCESS);
      lock.unlock();
    }
    return ReturnT.SUCCESS;
  }

  /**
   * ????????????????????????
   */
  @XxlJob("bankPayInitLdCorrectStatus")
  public ReturnT<String> bankPayInitLdCorrectStatus(String args) {
    try {
      String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
      MDC.put(PROCESS, processId);

      logger.info("????????????-??????????????????--????????????????????????");
      lock.lock();
      List<LdOrderCorrect> list = ldOrderCorrectService.getLdCorrectListByTypeAndStatusOnJob();
      logger.info("????????????--????????????--????????????????????????---?????????" + list.size());
      if (list.size() > 0) {
        List<List<LdOrderCorrect>> averageAssign = StringUtil.averageAssign(list, 5);
        for (int i = 0; i < averageAssign.size(); i++) {
          String subProcessId = processId + "--" + i;
          ThreadUtil.cashThreadPool.execute(
              new ExecuteLdCorrectQuery(subProcessId, averageAssign.get(i), commissionService,
                  companyService, utilCacheManager, ldOrderStepService, customBalanceDao, baseInfo,
                  ldOrderCorrectService, transferDealStatusNotifier));
        }
      }
      logger.info("????????????--??????--????????????????????????");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return new ReturnT<>(FAIL_CODE, e.toString());
    } finally {
      MDC.remove(PROCESS);
      lock.unlock();
    }
    return ReturnT.SUCCESS;
  }


  /**
   * ???????????????????????????????????????
   */
  @XxlJob("querySubAccountTransferRecord")
  public ReturnT<String> querySubAccountTransferRecord(String args) {
    try {
      String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
      MDC.put(PROCESS, processId);

      logger.info("------------???????????????????????????????????????????????????--------------");

      List<PaymentConfig> paymentConfigs = companyService.getSubAccountPaymentConfig();
      if (paymentConfigs != null && paymentConfigs.size() > 0) {

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        String date = df.format(calendar.getTime());

        logger.info("-----------???????????????????????????????????????--------");

        for (PaymentConfig paymentConfig : paymentConfigs) {
          switch (paymentConfig.getPathNo()) {
            case PAKHKF:
            case PAYQZL:
              PingAnBankAccountSystem pingAnBankAccountSystem = new PingAnBankAccountSystem(
                  paymentConfig);
              List<SubAccountTransHistoryRecord> resultList = new ArrayList<>();

              SubAccountQueryHistoryTransferResultParam param = new SubAccountQueryHistoryTransferResultParam();
              param.setStartDate(date);
              param.setEndDate(date);

              logger.info("-------??????????????????{}-{} ???????????????{}", date, date, resultList.size());

              pingAnBankAccountSystem.getTransHistoryPage(param, resultList);

              for (SubAccountTransHistoryRecord transHistory : resultList) {
                //000000????????????????????????????????????????????????????????????
                if (!transHistory.getOppAccountNo().endsWith("000000")) {

                  try {

                    if (transHistory.getSubAccount().endsWith("000000")) {
                      if (!CustomTransferRecordType.SUBACCOUNTINTO.getFlag()
                          .equals(transHistory.getFlag())) {
                        continue;
                      }
                      //??????????????????????????????????????????,???????????????,?????????????????????????????????,?????????????????????????????????
                      if (customReceiveConfigService
                          .checkSubAccountIsExists(transHistory.getOppAccountNo(), null) > 0) {
                        continue;
                      }

                      //??????????????????????????????????????????????????????
//										if (customReceiveConfigService.checkSubAccountIsExists(null, transHistory.getOppAccountName()) < 1) {
//											continue;
//										}
                    }

                    //???????????????????????????,???????????????????????????????????????????????????
                    transHistory.setAccountDate(date);
                    CustomTransferRecord record = new CustomTransferRecord(transHistory);
                    customTransferRecordService.insert(record);

                  } catch (DuplicateKeyException e) {
                    logger.info("-----------???????????????????????????---------");
                  } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                  }
                }
              }
              break;
          }
        }

      }

    } finally {
      logger.info("------------???????????????????????????????????????????????????--------------");
      MDC.remove(PROCESS);
    }
    return ReturnT.SUCCESS;
  }


  /**
   * ????????????????????????????????????
   */
  @XxlJob("rechargeAutoConfirm")
  public ReturnT<String> rechargeAutoConfirm(String args) {
    try {
      String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
      MDC.put(PROCESS, processId);

      logger.info("------------??????????????????????????????????????????--------------");

      lock.lock();

      List<ChannelHistory> list = channelHistoryService.getAutoConfirmList();
      if (list != null) {

        logger.info("-----------????????????????????????????????????,??????????????????{}---------", list.size());

        for (ChannelHistory info : list) {

          logger.info("-----------??????????????????????????????:{}---------", info.getOrderno());

          List<CustomTransferRecord> toBeConfirmedList = customTransferRecordService.getToBeConfirmedRecord();
          for (CustomTransferRecord record : toBeConfirmedList) {
            logger.info("-----------?????????:{}???????????????:{}????????????---------", info.getOrderno(), record.getBizFlowNo());
            info.setCompanyOperatorName("rechargeTaskJob");
            if (customBalanceService.confirmBalance(info, record)) {
              logger.info("-----------?????????:{}???????????????:{}?????????---------", info.getOrderno(), record.getBizFlowNo());

              ChannelHistory history = channelHistoryService.getChannelHistoryById(String.valueOf(info.getId()));
              channelHistoryService.rechargeCallback(history);
              channelHistoryService.approvalInvoice(history);
              break;
            }
          }
        }


      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return new ReturnT<>(FAIL_CODE, e.toString());
    } finally {
      lock.unlock();
      logger.info("------------??????????????????????????????????????????--------------");
      MDC.remove(PROCESS);
    }
    return ReturnT.SUCCESS;
  }

  /**
   * ?????????????????????????????????
   *
   * @author linsong
   * @date 2019/9/24
   */
  @XxlJob("syncBalanceStatus")
  public ReturnT<String> syncBalanceStatus(String args) {
    try {

      String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
      MDC.put(PROCESS, processId);

      logger.info("--------------???????????????????????????????????????????????????--------------");

      List<CustomTransferRecord> syncBalanceList = customTransferRecordService.getSyncBalanceList();

      if (syncBalanceList != null && syncBalanceList.size() > 0) {

        logger.info("--------------??????????????????????????????????????????????????????:{}--------------", syncBalanceList.size());

        for (CustomTransferRecord transferRecord : syncBalanceList) {

          logger.info("---------------?????????????????????????????????,???????????????:{}---------------",
              transferRecord.getBizFlowNo());

          PaymentConfig paymentConfig = companyService
              .getPaymentConfigInfo(String.valueOf(PayType.PINGAN_BANK.getCode()),
                  transferRecord.getCustomKey(), transferRecord.getCompanyId());

          AccountSystem accountSystem = AccountSystemFactory.accountSystemEntity(paymentConfig);

          if (accountSystem == null){
            continue;
          }

          ActionReturn<TransferResult> ret = accountSystem
              .querySubAccountTransferResult(transferRecord.getBizFlowNo());

          if (ret.isOk()) {
            TransferResult transStatus = ret.getAttachment();

            if (TransferResult.TransferResultType.SUCCESS.equals(transStatus.getResultType())) {

              customTransferRecordService
                  .updateState(transferRecord.getBizFlowNo(), ConfirmStatus.PaySuccess.getCode());
              logger.info(
                  "------------????????????????????????,???????????????:{}---------------" + transferRecord.getBizFlowNo());
            } else if (TransferResult.TransferResultType.FAIL.equals(transStatus.getResultType())) {

              customTransferRecordService
                  .updateState(transferRecord.getBizFlowNo(), ConfirmStatus.PayFail.getCode());
              logger.info(
                  "------------????????????????????????,???????????????:{}---------------" + transferRecord.getBizFlowNo());
            } else {
              logger.info(
                  "------------???????????????????????????,???????????????:{}---------------" + transferRecord.getBizFlowNo());
            }
          } else {
            logger
                .error("-----------???????????????????????????,????????????:{},{}------------", transferRecord.getBizFlowNo(),
                    ret.getFailMessage());
          }
        }

      }

      logger.info("---------------???????????????????????????????????????????????????--------------");

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return new ReturnT<>(FAIL_CODE, e.toString());
    } finally {
      MDC.remove(PROCESS);
    }
    return ReturnT.SUCCESS;
  }

  /**
   * ???????????????
   */
  @XxlJob("clearingAccounts")
  public ReturnT<String> clearingAccounts(String args) {

    String month = DateUtils.getSeveralMonthAgo(1);
    try {
      String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
      MDC.put(PROCESS, processId);
      logger.info("???????????????????????????...");
      proxyCustomService.calculate(AgainCalculateType.ALL.getCode(), "", month, null);
    } catch (Exception e) {
      logger.error(e.getMessage());
    } finally {
      logger.info("???????????????????????????...");
      MDC.remove(PROCESS);
    }
    try {
      String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
      MDC.put(PROCESS, processId);
      logger.info("?????????????????????...");
      //1.??????????????????????????????????????????????????????
      //???????????????????????????
      logger.info("?????????????????????" + month);
      List<Map<String, String>> clearTermlist = clearingAccountsService.groupClearTerm(month);
      Map<String, List<Map<String, String>>> clearAccountMap = new HashMap<String, List<Map<String, String>>>();
      for (Map<String, String> clearTerm : clearTermlist) {
        Map<String, String> merchantAccount = new HashMap<String, String>();
        //?????????????????????????????????????????????10???????????????
        clearTerm.put("month", month);
        //????????????????????????????????????
        Map<String, String> summaryAccount = clearingAccountsService.getSumAmountByTerm(clearTerm);
        //????????????
        merchantAccount.put("month", month);
        merchantAccount.put("merchantName", summaryAccount.get("merchantName"));
        merchantAccount.put("customkey", clearTerm.get("originalId"));
        merchantAccount.put("companyId", clearTerm.get("companyId"));
        merchantAccount.put("companyName", summaryAccount.get("companyName"));
        merchantAccount.put("agentName", summaryAccount.get("agentName"));
        merchantAccount.put("agentId", clearTerm.get("businessChannelKey"));
        merchantAccount.put("businessManager", summaryAccount.get("businessManager"));
        merchantAccount.put("gearLaber", String.valueOf(clearTerm.get("gearLabel")));
        merchantAccount.put("merchantRateRule", clearTerm.get("calculationRates"));
        merchantAccount.put("proxyFeeRate", clearTerm.get("proxyFeeRate"));
        merchantAccount.put("totalAmount", String.valueOf(summaryAccount.get("totalAmount")));
        merchantAccount.put("gearPosition", String.valueOf(clearTerm.get("gearPosition")));
        merchantAccount.put("rateInterval", clearTerm.get("rateInterval"));
        merchantAccount.put("rateConfId", String.valueOf(clearTerm.get("rateConfId")));
        merchantAccount.put("netfileId", clearTerm.get("netfileId"));
        merchantAccount.put("oneAmount", String.valueOf(summaryAccount.get("oneAmount")));
        merchantAccount.put("twoAmount", String.valueOf(summaryAccount.get("twoAmount")));
        String mapKey = clearTerm.get("originalId") + clearTerm.get("companyId") + clearTerm
            .get("businessChannelKey");
        if (clearAccountMap.containsKey(mapKey)) {
          clearAccountMap.get(mapKey).add(merchantAccount);
        } else {
          List<Map<String, String>> merchantTransList = new ArrayList<Map<String, String>>();
          merchantTransList.add(merchantAccount);
          clearAccountMap.put(mapKey, merchantTransList);
        }
      }
      if (!clearAccountMap.isEmpty()) {
        //2.??????????????????????????????
        List<QbClearingAccounts> clearingAccountList = new ArrayList<QbClearingAccounts>();
        for (String key : clearAccountMap.keySet()) {
          List<Map<String, String>> merchantTransList = clearAccountMap.get(key);
          setClearAccountInfo(merchantTransList, clearingAccountList);
        }
        for (QbClearingAccounts qbClearingAccounts : clearingAccountList) {
          clearingAccountsService.insert(qbClearingAccounts);
        }
        logger.info("???????????????????????????");
      } else {
        logger.info("?????????????????????????????????");
      }
    } catch (Exception e) {
      logger.error("???????????????????????????", e);
      return new ReturnT<>(FAIL_CODE, e.toString());
    } finally {
      MDC.remove(PROCESS);
    }
    return ReturnT.SUCCESS;
  }

  /**
   * ???????????????????????????
   *
   * @param merchantTransList
   */
  public void setClearAccountInfo(List<Map<String, String>> merchantTransList,
      List<QbClearingAccounts> clearingAccountList) {
    List<Map<String, String>> merchantTransOverList = new ArrayList<Map<String, String>>();
    String gearPositionDesc = "";
    String merchantRateRuleDesc = "";
    int smallCount = 1;
    int bigCount = 1;
    Map<String, String> merchantTrans = merchantTransList.get(0);
    String merchantName = merchantTrans.get("merchantName");
    String companyName = merchantTrans.get("companyName");
    String agentName = merchantTrans.get("agentName");
    String agentId = merchantTrans.get("agentId");
    String companyId = merchantTrans.get("companyId");
    String customkey = merchantTrans.get("customkey");
    String transMonth = merchantTrans.get("month");
    String businessManager = merchantTrans.get("businessManager");
    QbClearingAccounts clearingAccounts = new QbClearingAccounts();
    clearingAccounts.setTransMonth(transMonth);
    clearingAccounts.setCompanyId(companyId);
    clearingAccounts.setCompanyName(companyName);
    clearingAccounts.setCustomkey(customkey);
    clearingAccounts.setMerchantName(merchantName);
    clearingAccounts.setAgentId(agentId);
    clearingAccounts.setAgentName(agentName);
    clearingAccounts.setBusinessManager(businessManager);
    Map<String, String> rateIntervalMap = new HashMap<String, String>();
    for (int i = 0; i < merchantTransList.size(); i++) {
      Map<String, String> merchantTranMap = merchantTransList.get(i);
      //??????????????????????????????????????????
      String rateInterval = merchantTranMap.get("rateInterval");
      String merchantRate = merchantTranMap.get("merchantRateRule");
      if (rateIntervalMap.containsKey(rateInterval)) {
        merchantTransOverList.add(merchantTranMap);
      } else {
        rateIntervalMap.put(rateInterval, merchantRate);
        String totalAmount = merchantTranMap.get("totalAmount");
        String proxyFeeRate =
            ArithmeticUtil.mulStr(merchantTranMap.get("proxyFeeRate"), "100") + "%";
        String gearLaber = GearLaberType.codeOf(Integer.parseInt(merchantTranMap.get("gearLaber")))
            .getDesc();
        String merchantRateRule = ArithmeticUtil.mulStr(merchantRate, "100") + "%";
        if (Integer.parseInt(merchantTranMap.get("gearLaber")) == 1) {
          if (StringUtil.isEmpty(clearingAccounts.getSmallAmountOne()) || StringUtil
              .isEmpty(clearingAccounts.getSmallAmountTwo())) {
            gearPositionDesc += rateInterval + ",????????????" + gearLaber + smallCount + "\n";
            merchantRateRuleDesc +=
                rateInterval + ",????????????" + gearLaber + smallCount + ":" + merchantRateRule + "\n";
          }
        } else {
          if (StringUtil.isEmpty(clearingAccounts.getBigAmount())) {
            gearPositionDesc += rateInterval + ",????????????" + gearLaber + bigCount + "\n";
            merchantRateRuleDesc +=
                rateInterval + ",????????????" + gearLaber + bigCount + ":" + merchantRateRule + "\n";
          }
        }
        if (Integer.parseInt(merchantTranMap.get("gearLaber")) == 1) {
          clearingAccounts.setAgentSmallRate(proxyFeeRate);
          clearingAccounts.setAgentSmallRateUpdate(merchantTranMap.get("updateMerRate"));
          if (StringUtil.isEmpty(clearingAccounts.getSmallAmountOne())) {
            //????????????1
            clearingAccounts.setSmallAmountOne(totalAmount);
            clearingAccounts.setSmallAmountOneRate(merchantRateRule);
            String oneTypeAmount = merchantTranMap.get("oneAmount");
            String twoTypeAmount = merchantTranMap.get("twoAmount");
            String oneTypeAmountFee = ArithmeticUtil.mulStr(oneTypeAmount, merchantRate, 2);
            String differRate = ArithmeticUtil.subStr("1", merchantRate);
            String twoTypeAmountFee = ArithmeticUtil
                .mulStr(ArithmeticUtil.divideStr(twoTypeAmount, differRate), merchantRate, 2);
            String fee = ArithmeticUtil.addStr(oneTypeAmountFee, twoTypeAmountFee);
            clearingAccounts.setSmallAmountOneFee(fee);
            //?????????????????????????????????????????????????????????????????????????????????????????????????????????
            List<Map<String, String>> merRateUpdateList = clearingAccountsService
                .getMerRateUpdate(merchantTranMap);
            if (merRateUpdateList != null && merRateUpdateList.size() > 0) {
              Map<String, String> differMap = clearingAccountsService
                  .differSummary(merchantTranMap, merchantRate, merRateUpdateList, 1);
              clearingAccounts.setSmallAmountOneRateUpdate(differMap.get("updateRateDesc"));
              clearingAccounts.setDifferSmallOneMerAmount(differMap.get("differAmount"));
              clearingAccounts.setDifferSmallOneMerAmountDesc(differMap.get("differAmountDesc"));
            }
            //?????????????????????????????????????????????????????????????????????????????????????????????????????????
            List<Map<String, String>> agentRateUpdateList = clearingAccountsService
                .getAgentRateUpdate(merchantTranMap);
            if (agentRateUpdateList != null && agentRateUpdateList.size() > 0) {
              Map<String, String> differMap = clearingAccountsService
                  .differSummary(merchantTranMap, merchantTranMap.get("proxyFeeRate"),
                      agentRateUpdateList, 2);
              clearingAccounts.setDifferAgentSmallAmount(differMap.get("differAmount"));
              clearingAccounts.setAgentSmallRateUpdate(differMap.get("updateRateDesc"));
              clearingAccounts.setDifferAgentSmallAmountDesc(differMap.get("differAmountDesc"));
            }
          } else {
            if (StringUtil.isEmpty(clearingAccounts.getSmallAmountTwo())) {
              //????????????2
              clearingAccounts.setSmallAmountTwo(totalAmount);
              clearingAccounts.setSmallAmountTwoRate(merchantRateRule);
              String oneTypeAmount = merchantTranMap.get("oneAmount");
              String twoTypeAmount = merchantTranMap.get("twoAmount");
              String oneTypeAmountFee = ArithmeticUtil.mulStr(oneTypeAmount, merchantRate, 2);
              String differRate = ArithmeticUtil.subStr("1", merchantRate);
              String twoTypeAmountFee = ArithmeticUtil
                  .mulStr(ArithmeticUtil.divideStr(twoTypeAmount, differRate), merchantRate, 2);
              String fee = ArithmeticUtil.addStr(oneTypeAmountFee, twoTypeAmountFee);
              clearingAccounts.setSmallAmountTwoFee(fee);
              List<Map<String, String>> merRateUpdateList = clearingAccountsService
                  .getMerRateUpdate(merchantTranMap);
              if (merRateUpdateList != null && merRateUpdateList.size() > 0) {
                Map<String, String> differMap = clearingAccountsService
                    .differSummary(merchantTranMap, merchantRate, merRateUpdateList, 1);
                clearingAccounts.setSmallAmountTwoRateUpdate(differMap.get("updateRateDesc"));
                clearingAccounts.setDifferSmallTwoMerAmount(differMap.get("differAmount"));
                clearingAccounts.setDifferSmallTwoMerAmountDesc(differMap.get("differAmountDesc"));
              }
              //?????????????????????????????????????????????????????????????????????????????????????????????????????????
              List<Map<String, String>> agentRateUpdateList = clearingAccountsService
                  .getAgentRateUpdate(merchantTranMap);
              if (agentRateUpdateList != null && agentRateUpdateList.size() > 0) {
                Map<String, String> differMap = clearingAccountsService
                    .differSummary(merchantTranMap, merchantTranMap.get("proxyFeeRate"),
                        agentRateUpdateList, 2);
                String differAmount = ArithmeticUtil
                    .addStr(clearingAccounts.getDifferAgentSmallAmount(),
                        differMap.get("differAmount"));
                clearingAccounts.setDifferAgentSmallAmount(differAmount);
                String[] differAgentAmountArray = clearingAccounts.getDifferAgentSmallAmountDesc()
                    .split(";");
                String[] differAgentAmountArrayTwo = differMap.get("differAmountDesc").split(";");
                String differAgentAmountDesc = "";
                for (int j = 0; j < differAgentAmountArray.length; j++) {
                  String differAgentAmount = differAgentAmountArray[j];
                  String differAgentAmountTwo = differAgentAmountArrayTwo[j];
                  String differAgentAmountTotal = ArithmeticUtil
                      .addStr(differAgentAmount, differAgentAmountTwo);
                  differAgentAmountDesc = differAgentAmountDesc + differAgentAmountTotal + ";";
                }
                clearingAccounts.setDifferAgentSmallAmountDesc(differAgentAmountDesc);
              }
            } else {
              merchantTransOverList.add(merchantTranMap);
            }
          }
          smallCount++;
        } else {
          if (StringUtil.isEmpty(clearingAccounts.getBigAmount())) {
            //????????????
            clearingAccounts.setAgentBigRate(proxyFeeRate);
            clearingAccounts.setBigAmount(totalAmount);
            clearingAccounts.setBigAmountRate(merchantRateRule);
            clearingAccounts.setCompanyBigAmount(totalAmount);
            String oneTypeAmount = merchantTranMap.get("oneAmount");
            String twoTypeAmount = merchantTranMap.get("twoAmount");
            String oneTypeAmountFee = ArithmeticUtil.mulStr(oneTypeAmount, merchantRate, 2);
            String differRate = ArithmeticUtil.subStr("1", merchantRate);
            String twoTypeAmountFee = ArithmeticUtil
                .mulStr(ArithmeticUtil.divideStr(twoTypeAmount, differRate), merchantRate, 2);
            String fee = ArithmeticUtil.addStr(oneTypeAmountFee, twoTypeAmountFee);
            clearingAccounts.setBigAmountFee(fee);
            //?????????????????????????????????????????????????????????????????????????????????????????????????????????
            List<Map<String, String>> merRateUpdateList = clearingAccountsService
                .getMerRateUpdate(merchantTranMap);
            if (merRateUpdateList != null && merRateUpdateList.size() > 0) {
              Map<String, String> differMap = clearingAccountsService
                  .differSummary(merchantTranMap, merchantRate, merRateUpdateList, 1);
              clearingAccounts.setBigAmountRateUpdate(differMap.get("updateRateDesc"));
              clearingAccounts.setDifferBigMerAmount(differMap.get("differAmount"));
              clearingAccounts.setDifferBigMerAmountDesc(differMap.get("differAmountDesc"));
            }

            //?????????????????????????????????????????????????????????????????????????????????????????????????????????
            List<Map<String, String>> agentRateUpdateList = clearingAccountsService
                .getAgentRateUpdate(merchantTranMap);
            if (agentRateUpdateList != null && agentRateUpdateList.size() > 0) {
              Map<String, String> differMap = clearingAccountsService
                  .differSummary(merchantTranMap, merchantTranMap.get("proxyFeeRate"),
                      agentRateUpdateList, 2);
              clearingAccounts.setAgentBigRateUpdate(differMap.get("updateRateDesc"));
              clearingAccounts.setDifferAgentBigAmount(differMap.get("differAmount"));
              clearingAccounts.setDifferAgentBigAmountDesc(differMap.get("differAmountDesc"));
            }
          } else {
            merchantTransOverList.add(merchantTranMap);
          }
          bigCount++;
        }
      }
    }
    //????????????????????????
    clearingAccounts.setMerchantRateRule(merchantRateRuleDesc);
    //?????????????????????????????????
    clearingAccounts.setGearLaber(gearPositionDesc);
    //????????????????????????=??????????????????1+??????????????????2+??????????????????
    String totalAmount = ArithmeticUtil
        .addStr(clearingAccounts.getSmallAmountOne(), clearingAccounts.getSmallAmountTwo());
    totalAmount = ArithmeticUtil.addStr(totalAmount, clearingAccounts.getBigAmount());
    clearingAccounts.setTotalAmount(totalAmount);
    //??????????????????=??????????????????1+??????????????????2
    String companySmallAmount = ArithmeticUtil
        .addStr(clearingAccounts.getSmallAmountOne(), clearingAccounts.getSmallAmountTwo());
    clearingAccounts.setCompanySmallAmount(companySmallAmount);
    String agentSamllRate = clearingAccounts.getAgentSmallRate() == null ? "0"
        : ArithmeticUtil.divideStr2(clearingAccounts.getAgentSmallRate().replace("%", ""), "100");
    String agentBigRate = clearingAccounts.getAgentBigRate() == null ? "0"
        : ArithmeticUtil.divideStr2(clearingAccounts.getAgentBigRate().replace("%", ""), "100");
    String agentSmallAmount = ArithmeticUtil.mulStr(companySmallAmount, agentSamllRate, 2);
    String agentBigAmount = ArithmeticUtil
        .mulStr(clearingAccounts.getCompanyBigAmount(), agentBigRate, 2);
    String agentTotalAmount = ArithmeticUtil.addStr(agentSmallAmount, agentBigAmount);
    clearingAccounts.setAgentSmallAmount(agentSmallAmount);
    clearingAccounts.setAgentBigAmount(agentBigAmount);
    clearingAccounts.setAgentTotalAmount(agentTotalAmount);
    String merchantTotalAmount = ArithmeticUtil
        .addStr(clearingAccounts.getSmallAmountOneFee(), clearingAccounts.getSmallAmountTwoFee());
    merchantTotalAmount = ArithmeticUtil
        .addStr(merchantTotalAmount, clearingAccounts.getBigAmountFee());
    String agentCommission = ArithmeticUtil.subStr2(merchantTotalAmount, agentTotalAmount);
    clearingAccounts.setAgentCommission(agentCommission);
    clearingAccounts.setCreateTime(DateUtils.getNowDate());
    //????????????????????????????????????
    String differSmallAmount = ArithmeticUtil.addStr(clearingAccounts.getDifferSmallOneMerAmount(),
        clearingAccounts.getDifferSmallTwoMerAmount());
    //????????????????????????????????????
    String differBigAmount = clearingAccounts.getDifferBigMerAmount();
    //???????????????
    String totalDifferMerAmount = ArithmeticUtil.addStr(differSmallAmount, differBigAmount);
    //???????????????
    String totalDifferAgentAmount = ArithmeticUtil
        .addStr(clearingAccounts.getDifferAgentSmallAmount(),
            clearingAccounts.getDifferAgentBigAmount());
    //?????????????????? = ????????????-????????????
    String totalDifferAmount = ArithmeticUtil.subStr2(totalDifferMerAmount, totalDifferAgentAmount);
    clearingAccounts.setRepairCommission(totalDifferAmount);
    //?????????????????? =?????????????????????+????????????
    String agentFianlCommission = ArithmeticUtil
        .addStr(clearingAccounts.getAgentCommission(), clearingAccounts.getRepairCommission());
    clearingAccounts.setAgentFinalCommission(agentFianlCommission);
    clearingAccountList.add(clearingAccounts);
    boolean size = merchantTransOverList.size() > 0;
    if (size) {
      setClearAccountInfo(merchantTransOverList, clearingAccountList);
    }
  }

  /**
   * ???????????????????????????
   */
  @XxlJob("warningRechargeJob")
  public ReturnT<String> warningRechargeJob(String args) {
    try {
      String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
      MDC.put(PROCESS, processId);

      logger.info("------------???????????????????????????????????????--------------");

      DataDictionary minuteDictionary = dataDictionaryService
          .getByDictTypeAndKey(DataDictionaryDictType.RECHARGE_WARNING.getDictType(),
              DataDictionaryDictKey.MINUTE.getDictKey());
      DataDictionary emailDictionary = dataDictionaryService
          .getByDictTypeAndKey(DataDictionaryDictType.RECHARGE_WARNING.getDictType(),
              DataDictionaryDictKey.EMAIL.getDictKey());

      if (minuteDictionary == null || StringUtil.isEmpty(minuteDictionary.getDictValue())
          || emailDictionary == null || StringUtil.isEmpty(emailDictionary.getDictValue())) {
        logger.info("------------?????????????????????????????????{}?????????--------------");
        return ReturnT.SUCCESS;
      }

      List<ChannelHistory> list = channelHistoryService
          .getWarningRechargeList(Integer.valueOf(minuteDictionary.getDictValue()));
      if (list != null) {

        logger.info("-----------???????????????????????????????????????,????????????????????????{}---------", list.size());

        for (ChannelHistory info : list) {

          logger.info("-----------???????????????????????????:{}---------", info.getOrderno());

          String url = "zstservice@jrmf360.com";
          String password = "Jrmf#2019";
          String host = "smtp.jrmf360.com";
          StringBuilder context = new StringBuilder(
              "<html><body><table cellpadding=\"0\" cellspacing=\"0\" border=\"1\" style=\"margin:0;padding:0;border:1px solid #cccccc;border-collapse: collapse;\">");
          context.append("<tr>" + "<th style=\"padding:10px;\">?????????</th>"
              + "<th style=\"padding:10px;\">????????????</th>" + "<th style=\"padding:10px;\">?????????</th>"
              + "<th style=\"padding:10px;\">??????</th>" + "<th style=\"padding:10px;\">??????????????????</th>"
              + "</tr>");
          context.append("<tr>");
          context.append("<td style=\"padding:10px;\">").append(info.getOrderno()).append("</td>");
          context.append("<td style=\"padding:10px;\">").append(info.getCustomName())
              .append("</td>");
          context.append("<td style=\"padding:10px;\">").append(info.getCompanyName())
              .append("</td>");
          context.append("<td style=\"padding:10px;\">").append(info.getAmount()).append("</td>");
          context.append("<td style=\"padding:10px;\">").append(info.getCreatetime())
              .append("</td>");
          context.append("</tr>");
          context.append("</table>" + "</body>" + "</html>");
          String[] receivers = emailDictionary.getDictValue().split(",");
          String title = "????????????????????????";
          EmailUtil.send(url, password, host, receivers, title, context.toString(), null,
              "text/html;charset=GB2312");

          channelHistoryService.updateSendStatus(info.getId());

        }
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return new ReturnT<>(FAIL_CODE, e.toString());
    } finally {
      logger.info("------------???????????????????????????????????????--------------");
      MDC.remove(PROCESS);
    }

    return ReturnT.SUCCESS;
  }

  /**
   * ???????????????????????????
   */
  @XxlJob("signContractJob")
  public ReturnT<String> signContractJob(String args) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("agreementType", 2);
    params.put("signStatus", 2);
    params.put("channelType", 2);
    List<UsersAgreement> agreements = usersAgreementService.getUsersAgreementsByChannelType(params);
    for (UsersAgreement usersAgreement : agreements) {
      Company company = companyService
          .getCompanyByUserId(Integer.parseInt(usersAgreement.getCompanyId()));
      AgreementTemplate agreementTemplate = agreementTemplateService
          .getAgreementTemplateById(usersAgreement.getAgreementTemplateId());
      if (agreementTemplate != null && agreementTemplate.getChannelType() == 2) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", usersAgreement.getUserName());
        jsonObject.put("certId", usersAgreement.getCertId());
        jsonObject.put("serviceCompanyId", company.getServiceCompanyId());

        PaymentConfig paymentConfig = followSignTypeGetPaymentConfig(usersAgreement, agreementTemplate);

        //??????????????????????????????
        SignContractChannel signContractChannel = SignContractChannelFactory
            .createChannel(agreementTemplate, paymentConfig);
        Map<String, String> respMap = signContractChannel
            .signContractQuery(jsonObject.toJSONString());
        if (respMap != null && respMap.get("code").equals("0000")) {
          if (Integer.parseInt(respMap.get("state")) == 1) {
            //????????????
            usersAgreement.setSignStep(1);
            usersAgreement.setSignStatus(5);
            usersAgreement.setSignStatusDes("??????");
            usersAgreement.setLastUpdateTime(DateUtils.getNowDate());
            usersAgreementService.updateUsersAgreement(usersAgreement);
          } else if (Integer.parseInt(respMap.get("state")) == 2) {
            //????????????
            usersAgreement.setSignStatus(4);
            usersAgreement.setSignStatusDes(respMap.get("msg"));
            usersAgreement.setLastUpdateTime(DateUtils.getNowDate());
            usersAgreementService.updateUsersAgreement(usersAgreement);
          } else if (Integer.parseInt(respMap.get("state")) == 0) {
            //???????????????
            usersAgreement.setSignStatus(4);
            usersAgreement.setSignStatusDes("???????????????");
            usersAgreement.setLastUpdateTime(DateUtils.getNowDate());
          } else {
            logger.info("??????????????????????????????");
          }
        } else if (respMap != null && respMap.get("code").equals("1014")) {
          //??????????????????
          usersAgreement.setSignStatus(5);
          usersAgreement.setSignStatusDes("??????");
          usersAgreement.setLastUpdateTime(DateUtils.getNowDate());
          usersAgreementService.updateUsersAgreement(usersAgreement);
        } else {
          logger.info("????????????????????????");
        }
      }
    }

    return ReturnT.SUCCESS;
  }

  private PaymentConfig followSignTypeGetPaymentConfig(UsersAgreement usersAgreement,
      AgreementTemplate agreementTemplate) {
    PaymentConfig paymentConfig = new PaymentConfig();
    if (usersAgreement.getSignSubmitType() == SignSubmitType.SERVICE_COMPANY.getCode()) {
      logger.info("????????????????????????...");
      if (agreementTemplate.getChannelType() == ChannelTypeEnum.AI_YUAN_GONG.getCode()) {
        paymentConfig.setPathKeyType(PathKeyTypeEnum.COMPANY.getCode());
        paymentConfig.setPathNo(PaymentFactory.HMZFTD);
      } else if (agreementTemplate.getChannelType() == ChannelTypeEnum.YI_MEI.getCode()) {
        paymentConfig.setPathKeyType(PathKeyTypeEnum.CUSTOM.getCode());
        paymentConfig.setPathNo(PaymentFactory.YMFWSPAY);
      }

      paymentConfig = customThirdPaymentConfigService
          .getConfigBySubcontract(null, usersAgreement.getOriginalId(),
              usersAgreement.getCompanyId(), paymentConfig);
      if (paymentConfig == null) {
        // 000014????????????????????????????????????????????????000018??????????????????
        if (agreementTemplate.getChannelType() == ChannelTypeEnum.YI_MEI.getCode()) {
          paymentConfig = new PaymentConfig();
          paymentConfig.setPathKeyType(PathKeyTypeEnum.CUSTOM.getCode());
          paymentConfig.setPathNo(PaymentFactory.YFSH);
          paymentConfig = customThirdPaymentConfigService
              .getConfigBySubcontract(null, usersAgreement.getOriginalId(),
                  usersAgreement.getCompanyId(), paymentConfig);
        }
        if (paymentConfig == null) {
          paymentConfig = new PaymentConfig();
          logger.info("??????????????????????????????????????????:{}", agreementTemplate);
          this.agreementTemplateInToPaymentConfig(paymentConfig, agreementTemplate);
        }
      }
      if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())) {
        paymentConfig.setAppIdAyg(agreementTemplate.getThirdMerchId());
      }
    } else {
      String pathNo = "";
      if (agreementTemplate.getChannelType() == ChannelTypeEnum.AI_YUAN_GONG.getCode()) {
        pathNo = PaymentFactory.HMZFTD;
      } else if (agreementTemplate.getChannelType() == ChannelTypeEnum.YI_MEI.getCode()) {
        pathNo = PaymentFactory.YMFWSPAY;
      }

      paymentConfig = this.getPaymentConfigInfo(usersAgreement, pathNo);
      if (paymentConfig == null) {
        // 000014????????????????????????????????????????????????000018??????????????????
        if (agreementTemplate.getChannelType() == ChannelTypeEnum.YI_MEI.getCode()) {
          pathNo = PaymentFactory.YFSH;
          paymentConfig = this.getPaymentConfigInfo(usersAgreement, pathNo);
        }
        if (paymentConfig == null) {
          logger.info("??????????????????????????????????????????:{}", agreementTemplate);
          paymentConfig = new PaymentConfig();
          this.agreementTemplateInToPaymentConfig(paymentConfig, agreementTemplate);
        }
      }
      if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())) {
        paymentConfig.setAppIdAyg(agreementTemplate.getThirdMerchId());
      }
    }
    return paymentConfig;
  }

  private PaymentConfig getPaymentConfigInfo(UsersAgreement usersAgreement, String pathNo) {
    PaymentConfig paymentConfig = companyService
        .getPaymentConfigInfoPlus(String.valueOf(PayType.PINGAN_BANK.getCode()),
            usersAgreement.getOriginalId(), usersAgreement.getCompanyId(),
            usersAgreement.getCompanyId(), pathNo);
    logger.info("??????????????????:{}, ???????????????{}?????????:{}, ????????????:{}", paymentConfig, pathNo,
        usersAgreement.getOriginalId(), usersAgreement.getCompanyId());
    return paymentConfig;
  }

  private void agreementTemplateInToPaymentConfig(PaymentConfig paymentConfig,
      AgreementTemplate agreementTemplate) {
    paymentConfig.setAppIdAyg(agreementTemplate.getThirdMerchId());
    paymentConfig.setApiKey(agreementTemplate.getApiKey());
    paymentConfig.setPreHost(agreementTemplate.getReqUrl());
    paymentConfig.setPayPrivateKey(agreementTemplate.getPrivateKey());
    paymentConfig.setPayPublicKey(agreementTemplate.getPublicKey());
  }


  /**
   * ??????????????????????????????
   *
   * @author linsong
   * @date 2020/3/12
   */
  @XxlJob("linkageTransferStatus")
  public ReturnT<String> linkageTransferStatus(String args) {

    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);

    try {

      logger.info("--------------??????????????????????????????????????????????????????--------------");

      List<LinkageTransferRecord> linkageTransferRecords = linkageTransferRecordService
          .getPayingList();

      if (linkageTransferRecords != null && linkageTransferRecords.size() > 0) {

        logger.info("--------------???????????????????????????????????????????????????:{}--------------",
            linkageTransferRecords.size());

        for (LinkageTransferRecord transferRecord : linkageTransferRecords) {

          logger.info("---------------????????????????????????????????????,???????????????:{}---------------",
              transferRecord.getOrderNo());

          LinkageBaseConfig baseConfig = linkageCustomConfigService
              .getConfigByCustomKey(transferRecord.getCustomKey(),
                  LinkageType.RECHARGENO.getCode());

          if (baseConfig != null && !StringUtil.isEmpty(baseConfig.getPathNo())
              && LinkageTranType.MAINACCOUNT.getCode() == transferRecord.getTranType()) {
            PaymentConfig paymentConfig = new PaymentConfig(baseConfig);
            //??????????????????????????????
            Payment payment = PaymentFactory.paymentEntity(paymentConfig);
            //?????????????????????????????????UtilCacheManager
            PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME,
                utilCacheManager);
            Payment proxy = paymentProxy.getProxy();

            PaymentReturn<TransStatus> paymentReturn = proxy
                .queryTransferResult(transferRecord.getOrderNo());

            logger.error("----------??????????????????????????????:{}--------------", paymentReturn);

            if (PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())) {
              ChannelHistory rechargeInfo = channelHistoryService
                  .getChannelHistoryByOrderno(transferRecord.getOrderNo());

              TransStatus transStatus = paymentReturn.getAttachment();
              if (PayRespCode.RESP_TRANSFER_SUCCESS.equals(transStatus.getResultCode())) {
                transferRecord.setStatus(LinkageTranStatus.SUCCESS.getCode());
                //??????????????????,??????????????????????????????
                rechargeInfo.setStatus(RechargeStatusType.CONFIRMING.getCode());
                transferRecord.setStatusDesc(LinkageTranStatus.SUCCESS.getDesc());

                LinkAccountTrans linkAccountTrans = new LinkAccountTrans(transferRecord);
                linkAccountTrans.setTranType(AccountTransType.transOut.getCode());
                linkAccountTrans.setStatus(AccountTransStatus.success.getCode());
                linkAccountTransService.insert(linkAccountTrans);

              } else if (PayRespCode.RESP_TRANSFER_FAILURE.equals(transStatus.getResultCode())) {
                transferRecord.setStatus(LinkageTranStatus.FAILURE.getCode());
                rechargeInfo.setStatus(RechargeStatusType.RECHARGEFAILURE.getCode());
                transferRecord.setStatusDesc(transStatus.getResultMsg());

              } else {
                transferRecord.setStatus(LinkageTranStatus.PAYING.getCode());
                rechargeInfo.setStatus(RechargeStatusType.RECHARGEING.getCode());
                transferRecord
                    .setStatusDesc(paymentReturn.getRetCode() + ":" + transStatus.getResultMsg());

              }

              channelHistoryService.updateRechargeStatus(rechargeInfo);

            } else {
              transferRecord.setStatus(LinkageTranStatus.PAYING.getCode());
              transferRecord.setStatusDesc(paymentReturn.getFailMessage());
              logger.error("----------??????????????????????????????---------{}",
                  paymentReturn.getRetCode() + paymentReturn.getFailMessage());
            }

            linkageTransferRecordService.updateStatus(transferRecord);
          } else {
            logger.error("----------???????????????????????????????????????,orderNo:{}--------------",
                transferRecord.getOrderNo());
          }

        }

      }

      logger.info("---------------????????????????????????????????????????????????????????????--------------");

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      logger.error("---------------????????????????????????????????????????????????????????????--------------");
      return new ReturnT<>(FAIL_CODE, e.toString());
    } finally {
      MDC.remove(PROCESS);
    }

    return ReturnT.SUCCESS;
  }

  @XxlJob("receiptFileJob")
	public ReturnT<String> receiptFileJob(String args) {
//  public void receiptFileJob() {

    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);

    String date = DateUtils.getBeforeDayStrByNYR(1);
    String receiptTime = DateUtils.getBeforeDayStrShort(1);

    try {
      List<PaymentConfig> paymentConfigs = companyService.getSubAccountPaymentConfig();
      logger
          .info("--------------------??????????????????begin---------------------date???{},receiptTime???{}", date,
              receiptTime);

      if (paymentConfigs != null && paymentConfigs.size() > 0) {
        for (PaymentConfig paymentConfig : paymentConfigs) {
          logger.info("------????????????id------:{}", paymentConfig.getCompanyId());

          Map<String, Object> params = new HashMap<>(20);
          params.put("payType", 4);
          params.put("companyId", paymentConfig.getCompanyId());
          params.put("receiptTime", receiptTime);
          params.put("receiptOrgName", "pa");

          ReceiptBatch receiptBatch;

          List<ReceiptBatch> listReceiptBatch = receiptService.listReceiptBatch(params);
          params.clear();
          if (listReceiptBatch == null || listReceiptBatch.size() <= 0) {
            logger.info("??????????????????????????????companyId:" + paymentConfig.getCompanyId() + "?????????" + date);
            continue;
          }

          receiptBatch = listReceiptBatch.get(0);
          if (ReceiptStatus.SUCCESS.getCode() == receiptBatch.getStatus()) {
            logger.info("??????????????????????????????????????????");
            continue;
          }

          try {

            BestSignConfig ftpConfig = null;
            String fileName = "";

            switch (paymentConfig.getPathNo()) {
              case PAKHKF:
              case PAYQZL:
                PingAnBankYqzl pingAnBank = new PingAnBankYqzl(paymentConfig);
                List<ReceiptFileResult> receiptFileResultList = pingAnBank
                    .queryTransHistoryFile(date);
                if (receiptFileResultList != null && receiptFileResultList.size() > 0) {
                  logger.info("-------------------------??????????????????????????????-------------------------------");
                  for (ReceiptFileResult result : receiptFileResultList) {
                    if (result.getFileName().trim().equals(
                        "RECPDF_" + paymentConfig.getCorporationAccount() + "_" + date + ".zip")) {
                      fileName = result.getFileName();
                    }
                    logger
                        .info("-----------resultFileName------------------:" + result.getFileName()
                            + ",FilePath" + result.getFilePath());
                    pingAnBank.DowloadQueryTransHistoryFile(
                        "YQT" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()),
                        result.getFileName(), result.getRandomPwd());
                  }
                  logger.info("--------------------??????????????????????????????---------------------------");

                }

                receiptBatch.setReceiptImportType(ReceiptImportType.PINGANBANKONE.getCode());
                break;
              case MYBANK:
                String ftpPath =
                    File.separator + paymentConfig.getParameter8() + File.separator + date;
                ftpConfig = new BestSignConfig(paymentConfig.getParameter4(),
                    paymentConfig.getParameter5(), ftpPath,
                    paymentConfig.getParameter6(),
                    paymentConfig.getParameter7());
                fileName = paymentConfig.getParameter8() + "+" + date + "+000001.zip";
                receiptBatch.setReceiptImportType(ReceiptImportType.MYBANKONE.getCode());
                break;
            }

            receiptService.autoImportReceipt(receiptBatch, fileName, ftpConfig);

          } catch (Exception e) {
            logger.error("------????????????id------:{}????????????????????????", paymentConfig.getCompanyId());
            logger.error(e.getMessage(), e);
          }

        }
      }
      logger.info("------------------------??????????????????end------------------------");

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      logger.error("---------------????????????????????????????????????--------------");
			return new ReturnT<>(FAIL_CODE, e.toString());
    } finally {
      MDC.remove(PROCESS);
    }

		return ReturnT.SUCCESS;
  }

  @XxlJob("insertApplyInvoiceDetailJob")
  public ReturnT<String> insertApplyInvoiceDetailJob(String appointTime) {
    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);
    String receiptTime = DateUtils.getBeforeDayStrShort(1);
    if (!StringUtil.isEmpty(appointTime)) {
      receiptTime = appointTime;
    }
    logger.info("??????????????????????????????????????? ????????????:" + receiptTime);
    try {
      List<UserCommission> userCommissions = commissionService.listCommissionByCompanyId(companyId, receiptTime);
      Map<String, Object> map = new HashMap<>();
      map.put("status", "1");
      List<CustomerFirm> customerFirms = customerFirmService.listCustomerFirm(map);
      for (UserCommission userCommission : userCommissions) {
        BatchInvoiceCommission invoiceCommission = batchInvoiceCommissionService.getBatchInvoiceCommissionByOrderNo(userCommission.getOrderNo());
        if (invoiceCommission == null) {
          String contractCompanyName = getContractCompanyName(userCommission, customerFirms);
          String receiptUrl = receiptService
              .getReceiptCommissionByOrderNo(userCommission.getOrderNo());
          BatchInvoiceCommission batchInvoiceCommission = parameterTransfer(userCommission, receiptUrl,
              contractCompanyName);
          batchInvoiceCommissionService.insert(batchInvoiceCommission);
        }
      }
      logger.info("??????????????????????????????????????????????????????????????????...");
    } catch (Exception e) {
      logger.error("?????????????????????????????????:", e);
      return new ReturnT<>(FAIL_CODE, e.toString());
    } finally {
      MDC.remove(PROCESS);
    }
    return ReturnT.SUCCESS;
  }

  private BatchInvoiceCommission parameterTransfer(UserCommission userCommission, String receiptUrl,
      String contractCompanyName) throws ParseException {
    BatchInvoiceCommission batchInvoiceCommission = new BatchInvoiceCommission();
    batchInvoiceCommission.setCompanyName(userCommission.getCustomName());
    batchInvoiceCommission.setCustomKey(userCommission.getOriginalId());
    batchInvoiceCommission.setInvoiceStatus(InvoiceStatusEnum.NOT_INVOICE.getCode());
    batchInvoiceCommission.setIndividualName(userCommission.getUserName());
    batchInvoiceCommission.setAccountTime(userCommission.getPaymentTime());
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
    Date date = simpleDateFormat.parse(userCommission.getPaymentTime());
    String accountDate = simpleDateFormat.format(date);
    batchInvoiceCommission.setAccountDate(accountDate);
    BigDecimal amount = new BigDecimal(userCommission.getAmount());
    amount = amount.setScale(2, RoundingMode.HALF_UP);
    batchInvoiceCommission.setAmount(amount.toString());
    BigDecimal sumFee = new BigDecimal(userCommission.getSumFee());
    sumFee = sumFee.setScale(2, RoundingMode.HALF_UP);
    batchInvoiceCommission.setFee(sumFee.toString());
    batchInvoiceCommission.setInAccountNo(userCommission.getAccount());
    batchInvoiceCommission.setInAccountName(userCommission.getBankName());
    batchInvoiceCommission.setCompanyId(Integer.parseInt(userCommission.getCompanyId()));
    batchInvoiceCommission.setDocumentType(userCommission.getDocumentType());
    batchInvoiceCommission.setOrderNo(userCommission.getOrderNo());
    batchInvoiceCommission.setContractCompanyName(contractCompanyName);
    batchInvoiceCommission.setReceiptStatus(
        StringUtil.isEmpty(receiptUrl) ? ReceiptStatusEnum.NOT_RECEIPT.getCode()
            : ReceiptStatusEnum.EXIST_RECEIPT.getCode());
    batchInvoiceCommission.setReceiptUrl(receiptUrl);
    batchInvoiceCommission.setCertId(userCommission.getCertId());
    batchInvoiceCommission.setRemark(userCommission.getRemark());
    return batchInvoiceCommission;
  }

  /**
   * ?????????????????????????????????????????????????????????????????????????????????
   * @return
   */
  private String getContractCompanyName(UserCommission userCommission,
      List<CustomerFirm> customerFirms) {
    String contractCompanyName = "";
    for (CustomerFirm customerFirm : customerFirms) {
      if (userCommission.getContractCompanyName().equals(customerFirm.getCustomName())) {
        contractCompanyName = userCommission.getContractCompanyName();
        return contractCompanyName;
      }
    }
    return contractCompanyName;
  }

}
