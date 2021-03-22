package com.jrmf.payment.service;

import com.google.code.yanf4j.util.ConcurrentHashSet;
import com.jrmf.common.APIResponse;
import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.AgreementPayment;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.BatchLockStatus;
import com.jrmf.controller.constant.CompanyType;
import com.jrmf.controller.constant.DataDictionaryDictKey;
import com.jrmf.controller.constant.DataDictionaryDictType;
import com.jrmf.controller.constant.DocumentStep;
import com.jrmf.controller.constant.LinkageSignType;
import com.jrmf.controller.constant.PayType;
import com.jrmf.controller.constant.ServiceFeeType;
import com.jrmf.controller.constant.SignStep;
import com.jrmf.controller.constant.SignSubmitType;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.controller.constant.UsersAgreementSignType;
import com.jrmf.domain.AgreementTemplate;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.ChannelInterimBatch;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.CommissionTemporary;
import com.jrmf.domain.CommissionUser;
import com.jrmf.domain.Company;
import com.jrmf.domain.CustomBalanceHistory;
import com.jrmf.domain.CustomCompanyRateConf;
import com.jrmf.domain.CustomMenu;
import com.jrmf.domain.DataDictionary;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.User;
import com.jrmf.domain.UserRelated;
import com.jrmf.domain.UsersAgreement;
import com.jrmf.domain.WhiteUser;
import com.jrmf.domain.vo.CompanyAccountVo;
import com.jrmf.domain.vo.CustomBalanceAndAccount;
import com.jrmf.oldsalarywallet.service.ChannelInterimBatchService;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.execute.CountOptionData2;
import com.jrmf.payment.execute.ExecuteBatchGrantOption2;
import com.jrmf.payment.execute.ExecuteCalculationFeeInfo2;
import com.jrmf.payment.execute.ExecuteYmBatchGrantOption;
import com.jrmf.payment.execute.input.ExecuteAliBatchToInput2;
import com.jrmf.payment.execute.input.ExecuteCardBatchToInput2;
import com.jrmf.payment.execute.input.ExecuteWeChatBatchToInput;
import com.jrmf.persistence.*;
import com.jrmf.service.*;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.dto.InputAliBatchData;
import com.jrmf.utils.dto.InputBankBatchData;
import com.jrmf.utils.dto.InputBatchData;
import com.jrmf.utils.exception.BalanceException;
import com.jrmf.utils.threadpool.ThreadUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.jms.Destination;
import javax.jms.TextMessage;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

@Service("confirmGrantService2")
public class ConfirmGrantService2Impl implements ConfirmGrantService2 {

  private static Logger logger = LoggerFactory.getLogger(ConfirmGrantService2Impl.class);
  public static final String PROCESS = "process";

  @Autowired
  private UserCommission2Dao commissionDao2;
  @Autowired
  private OrderNoUtil orderNoUtil;
  @Autowired
  private ChannelRelatedDao channelRelatedDao;
  @Autowired
  private ChannelHistoryService channelHistoryService;
  @Autowired
  private TransferBankDao transferBankDao;
  @Autowired
  private CustomCompanyRateConfService customCompanyRateConfImpl;
  @Autowired
  private UserSerivce userSerivce;
  @Autowired
  private DataService dataService;
  @Autowired
  private CommissionTemporary2Dao temporaryDao2;
  @Autowired
  private ChannelInterimBatch2Dao interimBatchDao2;
  @Autowired
  private ChannelCustomDao channelCustomDao;
  @Autowired
  private CustomMenuDao customMenuDao;
  @Autowired
  private BankCardBinService cardBinService;
  @Autowired
  private AgreementTemplateService agreementTemplateService;
  @Autowired
  private UtilCacheManager utilCacheManager;
  @Autowired
  private BaseInfo baseInfo;
  @Autowired
  private CalculationFeeService calculationFeeService;
  @Autowired
  private CustomLimitConfService customLimitConfService;
  @Autowired
  CustomPaymentTotalAmountDao customPaymentTotalAmountDao;
  @Autowired
  private JmsTemplate providerJmsTemplate;
  @Autowired
  private Destination warningOrderDestination;
  @Autowired
  private CompanyService companyService;
  @Autowired
  private UsersAgreementService usersAgreementService;
  @Autowired
  private CustomLdConfigService customLdConfigService;
  @Autowired
  private QbBlackUsersService blackUsersService;
  @Autowired
  private ChannelInterimBatchService channelInterimBatchService;
  @Autowired
  private CustomReceiveConfigService customReceiveConfigService;
  @Autowired
  private WhiteUserService whiteUserService;
  @Autowired
  private UserCommissionService userCommissionService;
  @Autowired
  private YmyfCommonService ymyfCommonService;
  @Autowired
  private DataDictionaryService dataDictionaryService;
  @Autowired
  private SignShareService signShareService;
  @Autowired
  private Destination inputBatchCheckSignDestination;
  @Autowired
  private Destination inputBatchCompanySignDestination;
  @Autowired
  private ChannelInterimBatchService2 interimBatchService2;
  @Autowired
  UsersAgreementSmsRemindService usersAgreementSmsRemindService;
  @Autowired
  private CustomBalanceService customBalanceService;
  @Autowired
  private ChannelInterimBatchService2Impl channelInterimBatchService2;
  @Autowired
  private UserSerivce userService;
  @Autowired
  private UserRelatedService userRelatedService;
  @Autowired
  private UsersAgreementDao usersAgreementDao;
  @Autowired
  private ChannelRelatedService channelRelatedService;
  @Autowired
  private ForwardCompanyAccountService forwardCompanyAccountService;
  @Autowired
  CustomBalanceAndAccountService customBalanceAndAccountService;

  @Value("${warning.time}")
  private String warningTime;


  @Value("${sign.share.time}")
  private String signShareTime;

  @Value("${jrmfLimitState}")
  private Integer jrmfLimitState;

  @Override
  public Map<String, Object> grantTransfer(String originalId,
      String companyId,
      String batchId,
      String remark,
      String operatorName,
      String realCompanyId) {

    int respstat = RespCode.success;
    Map<String, Object> model = new HashMap<>(10);
    try {
      ChannelRelated related = channelRelatedDao.getRelatedByCompAndOrig(originalId, companyId);
      //计算该批次薪税服务公司在商户对应的备付金是否足够
      ChannelInterimBatch interimBatch = interimBatchDao2
          .getChannelInterimBatchByOrderno(batchId, originalId);
      if (interimBatch == null) {
        respstat = RespCode.error115;
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "批次不存在！");
        return model;
      }

      //导入前检测是否有正在的数据，有的话返回前端提示
      boolean checkFlag = checkRepeatCommission(batchId, originalId, companyId);
      if (checkFlag) {
        respstat = RespCode.error107;
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, RespCode.CHEACK_REPEAT_WARN);
        return model;
      }

      ChannelCustom custom = channelCustomDao.getCustomByCustomkey(originalId, null);//商户对象

      if (custom.getBusinessPlatformId() != null
          && CommonString.JRMF_PLATFORM_LIMIT_OPEN == jrmfLimitState
          && CommonString.JRMF_PLATFORM_ID == custom.getBusinessPlatformId()) {
        customLimitConfService.platformAmountLimit(batchId);
      }
      calculationFeeService.locationCustomCompanyRateConf(companyId, originalId, batchId);
      customLimitConfService.customAmountLimit2(companyId, originalId, batchId);
      boolean autoSupplement = customLimitConfService.autoSupplement(companyId, originalId);

      Set<String> validateSet = new ConcurrentHashSet<>();
      userCommissionService
          .updateTemporaryBatchData(batchId, originalId, companyId, autoSupplement, validateSet);
      validateSet.clear();

      //代码重复 interimBatch = interimBatchDao2.getChannelInterimBatchByOrderno(batchId, originalId);

      ChannelInterimBatch currentInterimBatch = interimBatchDao2
          .getChannelInterimBatchByOrderno(batchId, originalId);

      if (ArithmeticUtil
          .compareTod(currentInterimBatch.getHandleAmount(), interimBatch.getHandleAmount()) != 0) {
        respstat = RespCode.BATCH_AMOUNT_CHANGE;
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "本批次总金额已发生改变,请联系运营或重新导入批次下发");
        logger.error(
            "---------批次下发金额发生改变 batchId:{} sourceHandleAmount:{} currentHandleAmount:{} sourceServiceFee:{} currentServiceFee:{}----------",
            interimBatch.getOrderno(), interimBatch.getHandleAmount(),
            currentInterimBatch.getHandleAmount(), interimBatch.getServiceFee(),
            currentInterimBatch.getServiceFee());
        return model;
      }

      //计算该批次薪税服务公司在商户对应的备付金是否足够
      String handleAmount = interimBatch.getHandleAmount(); //校验成功总金额+服务费
      String compBalance = channelHistoryService.getBalance(originalId, related.getCompanyId(),
          String.valueOf(interimBatch.getPayType()));//根据提交批次数据支付类型获取对应的余额
      ChannelCustom company = channelCustomDao
          .getCustomByCustomkey(interimBatch.getRecCustomkey(), null);//商户所属服务公司对象

      logger.info("余额：" + compBalance + "下发总金额：" + handleAmount);
      if (ArithmeticUtil.compareTod(compBalance, handleAmount) < 0) {
        respstat = RespCode.error115;
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, custom.getCompanyName() + "下发预存金额不足");
        return model;
      }
      // 临时批次迁移至正式批次(一次下发记录)
      String orderNo = orderNoUtil.getChannelSerialno();
      //扣减商户余额
      try {
        CustomBalanceAndAccount customBalanceAndAccount = new CustomBalanceAndAccount()
            .setOriginalId(interimBatch.getCustomkey())
            .setAmount(interimBatch.getAmount())
            .setCompanyId(related.getCompanyId())
            .setPayType(interimBatch.getPayType())
            .setHandleAmount(handleAmount)
            .setPassNum(interimBatch.getPassNum()).setRealCompanyId(realCompanyId)
            .setAmount(interimBatch.getAmount())
            .setOrderNo(orderNo).setOperator(operatorName)
            .setTradeType(TradeType.WEBPAYMENT.getCode());

        customBalanceAndAccountService
            .updateCustomBalanceAndAccount(customBalanceAndAccount);
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        respstat = RespCode.error115;
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, e.getMessage());
        return model;
      }

      /**
       * @Description 类型
       **/
      Company checkCompany = companyService.getCompanyByUserId(Integer.parseInt(companyId));
      if (checkCompany.getCompanyType() == 1 && !companyId.equals(realCompanyId)) {

        /**
         * @Description 扣减转包服务公司在实际下发公司余额
         **/
        try {

          CustomCompanyRateConf customCompanyMinRate = customCompanyRateConfImpl
              .getCustomCompanyMinRate(companyId, realCompanyId);

          String deductionAmount = interimBatch.getAmount();
          if (ServiceFeeType.ISSUE.getCode() == customCompanyMinRate.getServiceFeeType()
          || ServiceFeeType.PERSON.getCode() == customCompanyMinRate.getServiceFeeType()){
            deductionAmount = ArithmeticUtil.subStr(interimBatch.getAmount(), ArithmeticUtil
                .mulStr(interimBatch.getAmount(), customCompanyMinRate.getCustomRate(), 2), 2);
          }

          customBalanceService.updateCustomBalance(CommonString.DEDUCTION,
              new CustomBalanceHistory(companyId, realCompanyId,
                  interimBatch.getPayType(), deductionAmount, interimBatch.getPassNum(),
                  TradeType.WEBPAYMENT.getCode(), orderNo, operatorName));
        } catch (Exception e) {
          logger.error("扣减转包服务公司在实际下发公司余额异常" + e.getMessage(), e);

        }
      }

      ChannelHistory history = new ChannelHistory();
      history.setOrdername(currentInterimBatch.getOperatorName());
      history.setAmount("0");
      history.setPassNum(0);
      history.setAccountName(custom.getCompanyName());
      history.setCustomkey(originalId);
      history.setRecCustomkey(currentInterimBatch.getRecCustomkey());
      history.setOrdername("佣金发放");
      history.setOrderno(orderNo);
      history.setOriginalBeachNo(currentInterimBatch.getOrderno());
      history.setStatus(3);// 初始化状态为处理中
      history.setRemark(remark);
      history.setPayType(currentInterimBatch.getPayType());// 设置支付方式
      history.setTransfertype(2);// 交易类型 发放佣金
      history.setServiceFee(currentInterimBatch.getServiceFee());
      history.setSupplementServiceFee(currentInterimBatch.getSupplementServiceFee());
      history.setMfkjServiceFee(currentInterimBatch.getMfkjServiceFee());
      history.setOperatorName(currentInterimBatch.getOperatorName());
      history.setPayUserName(operatorName);
      history.setBatchAmount(currentInterimBatch.getBatchAmount());
      history.setBatchNum(currentInterimBatch.getBatchNum());
      history.setHandleAmount("0");
      history.setMenuId(currentInterimBatch.getMenuId());
      history.setBatchName(currentInterimBatch.getBatchName());
      history.setBatchDesc(currentInterimBatch.getBatchDesc());
      history.setFileName(currentInterimBatch.getFileName());
      history.setFailedAmount(currentInterimBatch.getFailedAmount());
      history.setTaskAttachmentFile(currentInterimBatch.getTaskAttachmentFile());
      BigDecimal deducationAmount = new BigDecimal(handleAmount).multiply(new BigDecimal(100));
      history.setDeductionAmount(deducationAmount.intValue());
      history.setRealCompanyId(realCompanyId);
      channelHistoryService.addChannelHistory(history);

      //更新临时批次状态为：已经打款状态
      interimBatchDao2.updateInterimBatchStatus(batchId);

      //更新佣金表批次号
      String newBatchId = history.getId() + "";
      CustomMenu customMenu = customMenuDao.getCustomMenuById(interimBatch.getMenuId());

      Map<String, Object> batchData = new HashMap<>(20);
      batchData.put("batchId", newBatchId);
      batchData.put("operatorName", operatorName);
      batchData.put("originalId", originalId);
      batchData.put("fileName", interimBatch.getFileName());
      batchData.put("batchDesc", interimBatch.getBatchDesc());
      batchData.put("batchName", interimBatch.getBatchName());
      batchData.put("menuId", interimBatch.getMenuId());
      batchData.put("menuName", customMenu.getContentName());
      batchData.put("customName", custom.getCompanyName());
      batchData.put("companyName", company.getCompanyName());
      batchData.put("companyId", interimBatch.getRecCustomkey());
      //增加子账号下发参数
      batchData.put("subAcctNo", customReceiveConfigService
          .getSubAccount(originalId, interimBatch.getRecCustomkey(), interimBatch.getPayType()));

      //查询待发放明细列表
      List<CommissionTemporary> commissionTempList = temporaryDao2
          .getCommissionsByBatchId(batchId, originalId);
      String processId = (String) MDC.get(PROCESS);

      List<List<CommissionTemporary>> averageAssign = StringUtil
          .averageAssign(commissionTempList, 1);
      CyclicBarrier cb = new CyclicBarrier(averageAssign.size(),
          new CountOptionData2(processId,
              newBatchId,
              commissionDao2,
              channelHistoryService,
              temporaryDao2));

      logger.info("-----打款开始-分配多个线程----");
      for (int i = 0; i < averageAssign.size(); i++) {
        String subProcessId = processId + "--" + i;
        logger.info("线程" + i + ",执行数据量--" + averageAssign.get(i).size());
        ThreadUtil.cashThreadPool.execute(new ExecuteBatchGrantOption2(subProcessId,
            cb,
            orderNoUtil,
            averageAssign.get(i),
            commissionDao2,
            userSerivce,
            related,
            batchData,
            baseInfo,
            customLimitConfService,
            companyService,
            customLdConfigService,
            userCommissionService));
      }
      logger.info("---打款结束---");

      //延迟队列监控下发明细的落地状态，经过10分钟后还存在没有落地的订单就发消息通知运维人员
      providerJmsTemplate.send(warningOrderDestination, session -> {
        TextMessage message = session.createTextMessage(newBatchId);
        message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, Long.parseLong(warningTime));
        return message;
      });

      // 正式表的批次号
      model.put("batchNo", newBatchId);
      model.put(RespCode.RESP_STAT, respstat);
      model.put(RespCode.RESP_MSG, "打款成功");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      logger.info("---打款异常---");
      respstat = RespCode.error115;
      model.put(RespCode.RESP_STAT, respstat);
      model.put(RespCode.RESP_MSG, "打款异常");
    }
    return model;
  }

  @Override
  public Map<String, Object> inputCommissionData(int payType,
      List<InputBatchData> inputBatchData,
      Map<String, String> batchData) {
    int respstat = RespCode.success;
    Map<String, Object> model = new HashMap<>(5);
    try {
      String customkey = batchData.get("customkey");
      String companyId = batchData.get("companyId");
      String menuId = batchData.get("menuId");
      String template = batchData.get("template");
      String batchDesc = batchData.get("batchDesc");
      String batchName = batchData.get("batchName");
      String realCompanyId = batchData.get("realCompanyId");

      DataDictionary wordsDictionary = dataDictionaryService
          .getByDictTypeAndKey(DataDictionaryDictType.INPUT_BATCH_WORDS.getDictType(),
              DataDictionaryDictKey.WORDS.getDictKey());

      String words = wordsDictionary != null ? wordsDictionary.getDictValue() : null;

      if (!StringUtil.isEmpty(words) && (batchName.contains(words) || batchDesc.contains(words))) {
        respstat = RespCode.error101;
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "批次名称或描述包含敏感关键字,请联系运营人员");
        return model;
      }

      //导入批次备注关键字拦截
      batchData.put("words", words);

      ChannelRelated related = channelRelatedDao.getRelatedByCompAndOrig(customkey, companyId);
      CustomCompanyRateConf customCompanyRateConf = customCompanyRateConfImpl
          .getConfByCustomKeyAndCompanyId(customkey, companyId);
      if (related == null || customCompanyRateConf == null
          || customCompanyRateConf.getServiceFeeType() == null) {
        respstat = RespCode.error107;
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, RespCode.COMPANY_NOT_EXISTS);
        logger.info("返回结果：" + model);
        return model;
      }

      ChannelCustom custom = channelCustomDao.getBusinessInfoByCustomkey(customkey);
      batchData.put("businessManager", custom.getBusinessManager());
      batchData.put("businessPlatform", custom.getBusinessPlatform());
      batchData.put("customLabel", custom.getCustomLabel());
      batchData.put("businessChannel", custom.getBusinessChannel());
      batchData.put("businessChannelKey", custom.getBusinessChannelKey());
      batchData.put("operationsManager", custom.getOperationsManager());

      Company company = companyService.getCompanyByUserId(Integer.parseInt(companyId));

      logger.info("excel导入数据条数------------------" + inputBatchData.size() + "---------");
      if (inputBatchData.size() > 2000 || inputBatchData.size() <= 0) {
        respstat = RespCode.error107;
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, RespCode.UNSUPPORT_LINE_NUM);
        logger.info("返回结果：" + model);
        return model;
      }

      // 临时批次号
      String batchId = orderNoUtil.getChannelSerialno();
      model.put("batchId", batchId);
      batchData.put("batchId", batchId);

      ChannelInterimBatch batch = new ChannelInterimBatch();

      //存放批次详情，进行批处理
      if (PayType.HS_BANK.getCode() == payType) {
        respstat = RespCode.error107;
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, RespCode.UNSUPPORT_PAYTYPE);
        return model;
      } else if (PayType.ALI_PAY.getCode() == payType) {
        executeInputDataByAli(batch,
            inputBatchData,
            menuId,
            related,
            customCompanyRateConf,
            batchData,
            company.getMinAge(),
            company.getMaxAge(), realCompanyId);
      } else if (PayType.PINGAN_BANK.getCode() == payType) {
        executeInputDataByCard(batch,
            inputBatchData,
            template,
            menuId,
            related,
            customCompanyRateConf,
            batchData,
            company.getMinAge(),
            company.getMaxAge(), realCompanyId);
      } else {
        //未知下发方式
        respstat = RespCode.error107;
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, RespCode.UNSUPPORT_PAYTYPE);
        return model;
      }

      logger.info("-------------------临时批次信息生成开始---------------------------");
      String fileName = batchData.get("fileName");
      String fileUrl = batchData.get("fileUrl");
      String operatorName = batchData.get("operatorName");
      String taskAttachmentFile = batchData.get("taskAttachmentFile");

      //查询此批次总数
      String originalId = related.getOriginalId();
      List<CommissionTemporary> commissionsByBatchId = temporaryDao2
          .getCommissionsByBatchId(batchId, originalId);
      int batchNum = 0;
      if (commissionsByBatchId != null) {
        batchNum = commissionsByBatchId.size();
      }

      batch.setCustomkey(related.getOriginalId());
      batch.setRecCustomkey(related.getCompanyId());
      batch.setOrdername("佣金发放");
      batch.setOrderno(batchId);
      batch.setPayType(payType);
      batch.setFileName(fileName);
      batch.setOperatorName(operatorName);
      batch.setBatchDesc(batchDesc);
      batch.setBatchName(batchName);
      batch.setBatchNum(batchNum);
      batch.setMenuId(Integer.parseInt(menuId));
      batch.setStatus(0);
      batch.setTaskAttachmentFile(taskAttachmentFile);
      batch.setFileUrl(fileUrl);
      interimBatchDao2.addChannelInterimBatch(batch);
      logger.info("-------------------临时批次信息生成结束---------------------------");

      String processId = (String) MDC.get(PROCESS);

      ThreadUtil.cashThreadPool.execute(new Thread(() -> {


        try {

          String currentProcessId = processId;
          MDC.put(PROCESS,currentProcessId+"--inputThread");

          //商户下发拦截商户在工商公示的人员信息
          //下发效验是否完成个体工商户注册 和绑定银行卡
          usersAgreementSmsRemindService.checkUser(batchId, customkey, companyId,realCompanyId,payType,inputBatchData);

          if (custom.getBusinessPlatformId() != null
              && CommonString.JRMF_PLATFORM_LIMIT_OPEN == jrmfLimitState
              && CommonString.JRMF_PLATFORM_ID == custom.getBusinessPlatformId()) {
            customLimitConfService.platformAmountLimit(batchId);
          }
          calculationFeeService.locationCustomCompanyRateConf(companyId, customkey, batchId);
          customLimitConfService.customAmountLimit2(companyId, customkey, batchId);
          boolean autoSupplement = customLimitConfService.autoSupplement(companyId, customkey);

          logger.info("发送mq验证服务公司信息和签约模板信息... batchId:{}", batchId);
          providerJmsTemplate.send(inputBatchCompanySignDestination, session -> {
            TextMessage message = session.createTextMessage(batchId);
            return message;
          });

          //存在共享签约明细
          if (batchData.containsKey("sign_share_batchId")) {
            //校验是否存在共享签约订单，存在进入mq落地批次
            providerJmsTemplate.send(inputBatchCheckSignDestination, session -> {
              TextMessage message = session.createTextMessage(batchId);
              message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,
                  Long.parseLong(signShareTime));
              return message;
            });

            logger.info(
                "-------------------批次导入数据准备完成,存在共享签约明细,进入mq落地批次状态  batchId：{}---------------------------",
                batchId);

          } else {


            currentProcessId = currentProcessId + "--calculationFeeInfo";
            Set<String> validateSet = new ConcurrentHashSet<>();
            ThreadUtil.cashThreadPool.execute(new ExecuteCalculationFeeInfo2(currentProcessId,
                temporaryDao2,
                interimBatchDao2,
                related,
                validateSet,
                batchData,
                autoSupplement,
                calculationFeeService,
                customCompanyRateConf.getServiceFeeType()));

            logger
                .info("-------------------批次信息生成结束 batchId：{}--------------------------", batchId);

          }

        } catch (Exception e) {
          logger.error("----批次数据导入异步线程异常---", e);
        }

      }));


    } catch (Exception e) {
      logger.error("----数据导入异常---", e);
      respstat = RespCode.error107;
      model.put(RespCode.RESP_STAT, respstat);
      model.put(RespCode.RESP_MSG, RespCode.EXPORT_FAILIURE);
      return model;
    }
    model.put(RespCode.RESP_STAT, respstat);
    model.put(RespCode.RESP_MSG, RespCode.EXPORT_SUCCESS);
    return model;
  }

  @Override
  public void verifyCompanyAndSendMq(String batchId) {
    ChannelInterimBatch batch = interimBatchService2.getChannelInterimBatchByOrderno(batchId, "");
    // 防止出现重复校验情况
    if (batch != null) {
      // 查询服务公司所属的实际服务公司
      Company company = companyService
          .getCompanyByUserId(Integer.parseInt(batch.getRecCustomkey()));
      logger.info(
          "服务公司id:" + company.getUserId() + ", 真实服务公司id:" + company.getRealCompanyId() + ", 服务公司类型:"
              + company.getCompanyType());
      Optional.ofNullable(company)
          .ifPresent(c -> {
            Integer companyType = c.getCompanyType();
            Integer companyId = Integer.parseInt(c.getRealCompanyId());
            if (companyType == CompanyType.SUBCONTRACT.getCode()
                && c.getUserId() != companyId) {
              // 服务公司类型为转包类型，并且不是自己转包自己
              logger.info("开始进行转包服务公司签约...");
              subcontractCompanyIfConformConditionSendMq(c, batch);
            } else if (companyType == CompanyType.ACTUAL.getCode()) {
              // 服务公司类型为实际下发类型
              logger.info("开始进行真实服务公司签约...");
              realCompanySignIfNoSignSuccess(c, batch);
            }
          });
    }
  }

  @Override
  public String lockCommissionUsers(String batchId) {
    List<CommissionUser> userList = temporaryDao2.getCommissionUserByBatchId(batchId);
    for (CommissionUser user : userList) {
      String userKey = "commonOption,user," + user.getCertId();
      if (!utilCacheManager.putIfAbsent(userKey, 1, 0)) {
        return user.getUserName();
      }
    }
    return null;
  }

  @Override
  public void unLockCommissionUsers(String batchId) {
    List<CommissionUser> userList = temporaryDao2.getCommissionUserByBatchId(batchId);
    for (CommissionUser user : userList) {
      String userKey = "commonOption,user," + user.getCertId();
      utilCacheManager.remove(userKey);
    }
  }

  private void subcontractCompanyIfConformConditionSendMq(Company company,
      ChannelInterimBatch batch) {
    // 查询批次明细获取用户信息
    List<CommissionTemporary> temporaryList = channelInterimBatchService2
        .getCommissionUserInfo(batch.getOrderno());
    if (temporaryList != null && !temporaryList.isEmpty()) {
      for (CommissionTemporary commissionTemporary : temporaryList) {
        try {
          // 通过用户信息查询用户的ID
          User user = userService.getUserByUserNameAndCertId(commissionTemporary.getUserName(),
              commissionTemporary.getIdCard());
          logger.info("用户名:" + commissionTemporary.getUserName() + ",身份证号:" + commissionTemporary
              .getIdCard());
          if (user == null) {
            // 创建用户
            logger.info("创建用户...");
            user = addUserInfo(commissionTemporary);
          }

          List<ChannelRelated> channelRelatedList = channelRelatedService
              .getRelatedByOriginalId(null, batch.getCustomkey());
          if (channelRelatedList.isEmpty()) {
            logger.info("商户服务公司关系未配置, 用户id:" + user.getId() + ", 商户:" + batch.getCustomkey());
          }
          //插入商户-服务公司-用户关系表
          User finalUser = user;
          Optional.ofNullable(channelRelatedList)
              .ifPresent(channelRelateds -> {
                channelRelateds.forEach(channelRelated -> {
                  logger.info("插入商户-服务公司-用户关系表...");
                  userRelatedService.addUserRelated(channelRelated, null, finalUser.getId(),
                      commissionTemporary.getPhoneNo());
                });
              });

          //插入用户协议表
          Map<String, Object> param = new HashMap<>();
          param.put("originalId", batch.getCustomkey());
          List<AgreementTemplate> templateList = agreementTemplateService
              .getAgreementTemplateByParam(param);
          if (templateList == null || templateList.isEmpty()) {
            logger.info(
                "用户:" + user.getId() + "商户" + batch.getCustomkey() + ", 未配置签约模板, 暂不新增签约协议...");
          }

          Optional.ofNullable(templateList)
              .ifPresent(agreementTemplates -> {
                agreementTemplates.forEach(agreementTemplate -> {
                  logger.info("模板id: " + agreementTemplate.getId() + ", 创建签约协议...");
                  usersAgreementService
                      .addUserAgreement(agreementTemplate, finalUser.getId(), batch.getCustomkey(),
                          finalUser.getUserName(), finalUser.getCertId(),
                          commissionTemporary.getDocumentType(), commissionTemporary.getRemark(),
                          null);

                });
              });

          UsersAgreement usersAgreement = usersAgreementService
              .getAgreementsSignSuccess(company.getUserId() + "", company.getRealCompanyId(),
                  user.getId());

          if (usersAgreement == null) {
            logger.info("用户未签约,查询转包服务公司签约模板...");
            Map<String, Object> paramMap = new HashMap<>(8);
            paramMap.put("originalId", company.getUserId());
            paramMap.put("companyId", company.getRealCompanyId());
            List<AgreementTemplate> agreementTemplateList = agreementTemplateService
                .getAgreementTemplateByParam(paramMap);
            if (agreementTemplateList == null || agreementTemplateList.isEmpty()) {
              logger.error("服务公司未配置签约模板协议...");
              return;
            }

            // 若只有一个签约模板需要上个传证件照则全部需要上传
            boolean uploadIdCard = false;
            for (AgreementTemplate agreementTemplate : agreementTemplateList) {
              if (String.valueOf(AgreementPayment.SIGN_FIRST.getCode())
                  .equals(agreementTemplate.getAgreementPayment())
                  && agreementTemplate.getUploadIdCard() == 1) {
                uploadIdCard = true;
              }
            }
            logger.info("是否上传证件照:" + uploadIdCard);

            for (AgreementTemplate template : agreementTemplateList) {
              // 先签约后下发则签约
              logger.info("模板id：" + template.getId() + ",签约类型为:" + template.getAgreementPayment());
              if (String.valueOf(AgreementPayment.SIGN_FIRST.getCode())
                  .equals(template.getAgreementPayment())) {
                this.subcontractCompanySign(company, commissionTemporary, user, batch, uploadIdCard,
                    template);
              }
            }
          }
        } catch (Exception e) {
          logger.error("用户:" + commissionTemporary.getIdCard() + ", 签约异常: {}", e);
        }
      }
    }
  }

  private void subcontractCompanySign(Company company, CommissionTemporary commissionTemporary,
      User user, ChannelInterimBatch batch, boolean uploadIdCard, AgreementTemplate template) {
    byte[] front = null;
    byte[] back = null;
    if (uploadIdCard) {
      // 查询用户是否有签约成功记录
      UsersAgreement usersAgreement = usersAgreementService
          .getAgreementsSignSuccess(batch.getCustomkey(), batch.getRecCustomkey(),
              user.getId());
      if (usersAgreement != null) {
        // 获取用户照片
        String frontFileName = usersAgreement.getImageURLA()
            .substring(usersAgreement.getImageURLA().lastIndexOf("/") + 1);
        String frontFilePath = usersAgreement.getImageURLA()
            .substring(0, usersAgreement.getImageURLA().lastIndexOf("/") + 1);
        String backFileName = usersAgreement.getImageURLB()
            .substring(usersAgreement.getImageURLB().lastIndexOf("/") + 1);
        String backFilePath = usersAgreement.getImageURLB()
            .substring(0, usersAgreement.getImageURLB().lastIndexOf("/") + 1);
        logger.info("用户：{}，身份证人像面名称：{}，身份证人像面路径：{}", commissionTemporary.getIdCard(),
            frontFileName, frontFilePath);
        logger.info("用户：{}，身份证人像面名称：{}，身份证人像面路径：{}", commissionTemporary.getIdCard(),
            backFileName, backFilePath);
        try {
          front = FtpTool.downloadFtpFile(frontFilePath, frontFileName);
          back = FtpTool.downloadFtpFile(backFilePath, backFileName);
        } catch (Exception e) {
          logger.error("下载图片异常，用户:{}", commissionTemporary.getIdCard());
        }
      } else {
        logger.error(
            "用户：" + commissionTemporary.getUserName() + "，身份证：" + commissionTemporary.getIdCard()
                + ",未在商户:" + batch.getCustomkey() + ",服务公司:" + batch.getRecCustomkey()
                + " 有签约成功记录，跳过签约..");
        return;
      }
    } else {
      front = null;
      back = null;
    }
    this.insertUsersAgreementAndSign(template, commissionTemporary, user, batch,
        front, back, uploadIdCard, commissionTemporary.getBankCardNo());
  }

  private void insertUsersAgreementAndSign(AgreementTemplate agreementTemplate,
      CommissionTemporary commissionTemporary, User user, ChannelInterimBatch batch, byte[] front,
      byte[] back, boolean uploadIdCard, String bankCardNo) {

    String channelSerialno = orderNoUtil.getChannelSerialno();
    logger.info("转包签约- 用户:" + commissionTemporary.getIdCard() + ", 订单号:" + channelSerialno);
    usersAgreementService.addUserAgreement(agreementTemplate, user.getId(), batch.getRecCustomkey(),
        commissionTemporary.getUserName(), commissionTemporary.getIdCard(),
        1, "", SignSubmitType.SERVICE_COMPANY);
    logger.info("配置的模板有:{}", agreementTemplate.getId());

    Map<String, Object> paramMap = new HashMap<>(8);
    paramMap.put("userId", user.getId());
    paramMap.put("agreementTemplateIds", agreementTemplate.getId());
    List<UsersAgreement> usersAgreements = new ArrayList<>();
    List<UsersAgreement> agreements = usersAgreementService.getUsersAgreementsByParams(paramMap);
    if (!agreements.isEmpty()) {
      for (UsersAgreement agreement : agreements) {
        if (UsersAgreementSignType.SIGN_PROCESSING.getCode() == agreement.getSignStatus()
            || UsersAgreementSignType.SIGN_PRE_REVIEW.getCode() == agreement.getSignStatus()) {
          logger.error("协议签署中或者正在审核中,签约协议为:{}", agreement.getId());
          continue;
        }
        usersAgreements.add(agreement);
      }
    }
    try {
      usersAgreementService
          .singleSign(usersAgreements, front, back, commissionTemporary.getPhoneNo(),
              SignSubmitType.SERVICE_COMPANY.getCode(), channelSerialno, uploadIdCard, bankCardNo);
    } catch (Exception e) {
      logger.error("签约异常:", e);
    }
  }

  private void realCompanySignIfNoSignSuccess(Company company, ChannelInterimBatch batch) {
    logger
        .info("服务公司:" + company.getCompanyName() + ", 支付前是否联动第三方平台签约:" + company.getLinkageSign());
    if (company.getLinkageSign() == LinkageSignType.YES.getCode()) {
      // 查询批次明细获取用户信息
      List<CommissionTemporary> temporaryList = channelInterimBatchService2
          .getCommissionUserInfo(batch.getOrderno());
      Optional.ofNullable(temporaryList)
          .ifPresent(commissionTemporarys -> {
            commissionTemporarys.forEach(commissionTemporary -> {
              // 通过用户信息查询用户的ID
              User user = userService.getUserByUserNameAndCertId(commissionTemporary.getUserName(),
                  commissionTemporary.getIdCard());
              if (user == null) {
                // 创建用户
                logger.info("创建用户...");
                user = addUserInfo(commissionTemporary);
              }
              logger.info(
                  "用户名:" + commissionTemporary.getUserName() + ",身份证号:" + commissionTemporary
                      .getIdCard());
              // 查询用户是否有签约成功记录
              UsersAgreement usersAgreement = usersAgreementService
                  .getAgreementsSignSuccess(batch.getCustomkey(), batch.getRecCustomkey(),
                      user.getId());
              if (usersAgreement == null) {
                logger.info("用户未签约,开始签约...");
                this.sign(batch, user, company, commissionTemporary);
              }
            });
          });
    }
  }

  private void sign(ChannelInterimBatch batch, User user, Company company,
      CommissionTemporary commissionTemporary) {
    // 用户+商户是否建立关联关系，未建立则建立
    Map<String, Object> param = new HashMap<>();
    String originalId = batch.getCustomkey();
    String companyId = batch.getRecCustomkey();
    Integer userId = user.getId();
    param.put("originalId", originalId);
    param.put("companyId", companyId);
    param.put("userId", userId);
    List<UserRelated> userRelatedList = userRelatedService.getRelatedByParam(param);
    if (userRelatedList == null || userRelatedList.isEmpty()) {
      // 插入商户 + 服务公司 + 用户关系表
      logger.info("建立用户 + 商户 +服务公司的关联关系");
      UserRelated userRelated = new UserRelated();
      userRelated.setOriginalId(originalId);
      userRelated.setUserId(userId);
      userRelated.setCompanyId(companyId);
      userRelated.setCreateTime(DateUtils.getNowDate());
      userRelated.setMobileNo(commissionTemporary.getPhoneNo());
      userRelatedService.createUserRelated(userRelated);
    }
    // 商户+服务公司是否有签约模板
    param.clear();
    param.put("originalId", originalId);
    param.put("companyId", companyId);
    List<AgreementTemplate> agreementTemplates = agreementTemplateService
        .listUserAgreementTemplates(param);
    if (agreementTemplates == null || agreementTemplates.isEmpty()) {
      // 未配置签约模板，更新支付订单为失败
      logger.info("商户:" + originalId + ", 服务公司:" + companyId + ", 未配置签约模板，更新支付订单为失败");
      return;
    }

    // 若只有一个签约模板需要上个传证件照则全部需要上传
    boolean uploadIdCard = false;
    for (AgreementTemplate agreementTemplate : agreementTemplates) {
      if (agreementTemplate.getUploadIdCard() == 1) {
        uploadIdCard = true;
      }
    }
    logger.info("是否上传证件照:" + uploadIdCard);
    // 用户+商户+服务公司是否建立签约协议，未建立则建立
    param.clear();
    param.put("originalId", originalId);
    param.put("companyId", companyId);
    param.put("userId", userId);
    List<UsersAgreement> usersAgreementList = usersAgreementService
        .getUsersAgreementsByParams(param);
    String channelSerialno = orderNoUtil.getChannelSerialno();
    if (usersAgreementList == null || usersAgreementList.isEmpty()) {
      logger.info("新增签约协议...");
      AgreementTemplate agreementTemplate = agreementTemplates.get(0);
      UsersAgreement agreement = setParamAndCreateAgreement(agreementTemplate, userId,
          channelSerialno, originalId, commissionTemporary);
      usersAgreementList.add(agreement);
    }
    // 是否需要上传证件信息（0.否，1.是）
    byte[] front = null;
    byte[] back = null;
    if (uploadIdCard) {
      logger.info("服务公司需要上传证件信息，查询用户是否有签约成功记录...");
      // 查询用户在商户下是否有签约成功记录
      UsersAgreement usersAgreement = usersAgreementService
          .getAgreementsSignSuccess(batch.getCustomkey(), "",
              user.getId());
      if (usersAgreement != null && !StringUtil.isEmpty(usersAgreement.getImageURLA())
          && !StringUtil.isEmpty(usersAgreement.getImageURLB())) {
        // 获取用户照片
        String frontFileName = usersAgreement.getImageURLA()
            .substring(usersAgreement.getImageURLA().lastIndexOf("/") + 1);
        String frontFilePath = usersAgreement.getImageURLA()
            .substring(0, usersAgreement.getImageURLA().lastIndexOf("/") + 1);
        String backFileName = usersAgreement.getImageURLB()
            .substring(usersAgreement.getImageURLB().lastIndexOf("/") + 1);
        String backFilePath = usersAgreement.getImageURLB()
            .substring(0, usersAgreement.getImageURLB().lastIndexOf("/") + 1);
        logger.info("用户：{}，身份证人像面名称：{}，身份证人像面路径：{}", user.getCertId(),
            frontFileName, frontFilePath);
        logger.info("用户：{}，身份证人像面名称：{}，身份证人像面路径：{}", user.getCertId(),
            backFileName, backFilePath);
        try {
          front = FtpTool.downloadFtpFile(frontFilePath, frontFileName);
          back = FtpTool.downloadFtpFile(backFilePath, backFileName);
        } catch (Exception e) {
          logger.error("下载图片异常，用户:{}", user.getCertId());
        }
      } else {
        front = null;
        back = null;
        logger.error("用户：{}，身份证：{},因为没有在商户:{},服务公司:{}有签约成功记录，所以没有身份证正反面，跳过签约..",
            user.getUserName(), user.getCertId(), batch.getCustomkey(), batch.getRecCustomkey());
        return;
      }
    }
    try {
      usersAgreementService
          .singleSign(usersAgreementList, front, back, commissionTemporary.getPhoneNo(),
              SignSubmitType.PAYMENT_BEFORE_LINKAGE.getCode(), channelSerialno, uploadIdCard,
              commissionTemporary.getBankCardNo());
    } catch (Exception e) {
      logger.error("签约异常:{}", e);
    }
  }

  public User addUserInfo(CommissionTemporary commissionTemporary) {
    //用户信息不存在，插入user表
    User user = new User();
    user.setMobilePhone(commissionTemporary.getPhoneNo());
    user.setUserType(11);
    user.setCertId(commissionTemporary.getIdCard());
    user.setUserName(commissionTemporary.getUserName());
    user.setDocumentType(commissionTemporary.getDocumentType() + "");
    user.setCheckTruth(0);
    userSerivce.addUser(user);
    return user;
  }

  private UsersAgreement setParamAndCreateAgreement(AgreementTemplate agreementTemplate,
      Integer userId, String channelSerialno, String originalId,
      CommissionTemporary commissionTemporary) {
    UsersAgreement agreement = new UsersAgreement();
    agreement.setSignStatus(UsersAgreementSignType.SIGN_CREATE.getCode());
    agreement.setSignStatusDes(UsersAgreementSignType.SIGN_CREATE.getDesc());
    agreement.setDocumentStep(DocumentStep.DOCUMENT_CREATE.getCode());
    agreement.setSignStep(SignStep.SIGN_CREATE.getCode());
    agreement.setAgreementName(agreementTemplate.getAgreementName());
    agreement.setAgreementTemplateURL(agreementTemplate.getAgreementTemplateURL());
    agreement.setAgreementNo(
        agreementTemplate.getId() + "_" + userId + "_" + orderNoUtil.getChannelSerialno());
    agreement.setAgreementTemplateId(agreementTemplate.getId() + "");
    agreement.setThirdTemplateId(agreementTemplate.getThirdTemplateId());
    agreement.setAgreementType(agreementTemplate.getAgreementType());
    agreement.setOrderNo(channelSerialno);
    agreement.setOriginalId(originalId);
    agreement.setUserId(userId + "");
    agreement.setUserName(commissionTemporary.getUserName());
    agreement.setCertId(commissionTemporary.getIdCard());
    agreement.setDocumentType(commissionTemporary.getDocumentType() + "");
    agreement.setCompanyId(agreementTemplate.getCompanyId());
    agreement.setThirdMerchId(agreementTemplate.getThirdMerchId());
    agreement.setSignSubmitType(SignSubmitType.PAYMENT_BEFORE_LINKAGE.getCode());
    usersAgreementDao.createAgreement(agreement);
    return agreement;
  }

  public void executeInputDataByCard(ChannelInterimBatch batch,
      List<InputBatchData> inputBatchData,
      String template,
      String menuId,
      ChannelRelated related,
      CustomCompanyRateConf customCompanyRateConf,
      Map<String, String> batchData,
      Integer minAge,
      Integer maxAge, String trueCompanyId) throws Exception {

    String processId = (String) MDC.get(PROCESS);

    Company company = companyService.getCompanyByUserId(Integer.parseInt(related.getCompanyId()));
    //真实下发公司id
    String realCompanyId = company.getRealCompanyId();
    if (StringUtil.isEmpty(realCompanyId)) {
      realCompanyId = related.getCompanyId();
    }
    if (null != trueCompanyId && !"".equals(trueCompanyId) && !"null".equals(trueCompanyId)) {
      realCompanyId = trueCompanyId;
    }
    PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(
        String.valueOf(PayType.PINGAN_BANK.getCode()), related.getOriginalId(), realCompanyId);

    //用于获取重复数据后得到list中的上一个数据
    Map<String, HashMap<String, String>> validateOrderMap = new HashMap<>();
    //原文件笔数
    Integer sourceFileNum = 0;

    logger.info("--------------导入---card---------封装导入的数据-------------------------");
    List<Map<String, String>> commissionDatas = new ArrayList<>();
    for (int j = 1; j < inputBatchData.size(); j++) {// 获取每行
      InputBankBatchData row = (InputBankBatchData) inputBatchData.get(j);
      if (row == null) {
        continue;
      }
      if (StringUtil.isEmpty(row.getUserName())
          && StringUtil.isEmpty(row.getBankAccount())
          && StringUtil.isEmpty(row.getCertId())) {
        continue;
      }

      sourceFileNum++;

      String merchantId = related.getMerchantId();
      String userName, bankCard, certId, phoneNo, amount, bankName, documentType, remark = "";
      if ("aiyuangong".equals(merchantId)) {
        // new
        userName = ArithmeticUtil
            .subNameSpace(ArithmeticUtil.subZeroAndDot(row.getUserName()));// 收款人真实姓名(必要)
        bankCard = ArithmeticUtil
            .subSpace(ArithmeticUtil.subZeroAndDot(row.getBankAccount()));// 收款人银行卡号
        certId = ArithmeticUtil.subSpace(ArithmeticUtil.subZeroAndDot(row.getCertId()));// 身份证号
        phoneNo = ArithmeticUtil.subSpace(ArithmeticUtil.subZeroAndDot(row.getPhoneNo()));// 手机号
        amount = ArithmeticUtil.subSpace(ArithmeticUtil.subZeroAndDot(row.getAmount()));// 金额(必要)
        bankName = ArithmeticUtil.subSpace(ArithmeticUtil.subZeroAndDot(row.getBankName()));// 银行姓名
        documentType = ArithmeticUtil
            .subSpace(ArithmeticUtil.subZeroAndDot(row.getCertType()));// 证件类型
        remark = ArithmeticUtil.subZeroAndDot(row.getRemark());// 备注
      } else {
        if ("2".equals(template)) {
          userName = ArithmeticUtil
              .subNameSpace(ArithmeticUtil.subZeroAndDot(row.getUserName()));// 收款人真实姓名(必要)
          bankCard = ArithmeticUtil
              .subSpace(ArithmeticUtil.subZeroAndDot(row.getBankAccount()));// 收款人银行卡号
          certId = ArithmeticUtil.subSpace(ArithmeticUtil.subZeroAndDot(row.getCertId()));// 身份证号
          phoneNo = ArithmeticUtil.subSpace(ArithmeticUtil.subZeroAndDot(row.getPhoneNo()));// 手机号
          amount = ArithmeticUtil.subSpace(ArithmeticUtil.subZeroAndDot(row.getAmount()));// 金额(必要)
          bankName = ArithmeticUtil
              .subSpace(ArithmeticUtil.subZeroAndDot(row.getBankName()));// 银行姓名
          documentType = ArithmeticUtil
              .subSpace(ArithmeticUtil.subZeroAndDot(row.getCertType()));// 证件类型
          remark = ArithmeticUtil.subZeroAndDot(row.getRemark());// 备注
        } else {
          userName = ArithmeticUtil
              .subNameSpace(ArithmeticUtil.subZeroAndDot(row.getUserName()));// 收款人真实姓名(必要)
          bankCard = ArithmeticUtil
              .subSpace(ArithmeticUtil.subZeroAndDot(row.getBankAccount()));// 收款人银行卡号
          certId = ArithmeticUtil.subSpace(ArithmeticUtil.subZeroAndDot(row.getCertId()));// 身份证号
          amount = ArithmeticUtil.subSpace(ArithmeticUtil.subZeroAndDot(row.getAmount()));// 金额(必要)
          bankName = ArithmeticUtil
              .subSpace(ArithmeticUtil.subZeroAndDot(row.getBankName()));// 银行姓名
          documentType = ArithmeticUtil
              .subSpace(ArithmeticUtil.subZeroAndDot(row.getCertType()));// 证件类型
          remark = ArithmeticUtil.subZeroAndDot(row.getRemark());// 备注
          phoneNo = "";
        }
      }

      HashMap<String, String> data = new HashMap<>();
      data.put("userName", userName);
      data.put("bankCard", bankCard);
      data.put("certId", certId);
      data.put("amount", amount);
      data.put("bankName", bankName);
      data.put("documentType", documentType);
      data.put("remark", remark);
      data.put("menuId", menuId);
      data.put("phoneNo", phoneNo);

      WhiteUser whiteUser = new WhiteUser();
      whiteUser.setCertId(certId);
      whiteUser.setDocumentType(1);
      whiteUser.setCustomkey(related.getOriginalId());
      whiteUser.setCompanyId(related.getCompanyId());
      Integer isWhiteUser = whiteUserService.checkIsWhiteUser(whiteUser);
      String isWhiteList = "0";
      if (isWhiteUser > 0) {
        isWhiteList = "1";
      }
      data.put("isWhiteList", isWhiteList);

      //在这里处理慧用工合并订单及新生、平安银企直连拆单
      if (PaymentFactory.NEWPAY.equals(paymentConfig.getPathNo()) || PaymentFactory.PAYQZL
          .equals(paymentConfig.getPathNo())) {

        addSplitOrder(amount, "50000", data, commissionDatas, batch);

        //addSplitOrder方法中已往commissionDatas添加当前循环记录，直接跳过当前循环
        continue;

      } else if (PaymentFactory.HYGPAY.equals(paymentConfig.getPathNo())) {
        String key = userName + certId + bankCard;
        //校验该key是否重复，不重复只记录当前对象用于后续remove
        if (!validateOrderMap.containsKey(key)) {
          validateOrderMap.put(key, data);
        } else {
          boolean canContinue = addMerageOrder(validateOrderMap, key, amount, commissionDatas,
              batch);
          //如果在合并方法中已经添加该记录则直接跳过循环，出现异常情况则继续添加该记录，防止合并失败数据丢失
          if (canContinue) {
            continue;
          }
        }
      }

      commissionDatas.add(data);
      logger.info("------------导入---card--------userName：" + userName
          + " bankCard=" + bankCard
          + " certId=" + certId
          + " phoneNo=" + phoneNo
          + " amount=" + amount
          + " bankName=" + bankName
          + " documentType=" + documentType
          + " isWhiteList=" + isWhiteList
          + "---------------------------");
    }

    batch.setSourceFileNum(sourceFileNum);
    batch.setInputPathNo(paymentConfig.getPathNo());

    logger.info("---------导入---card-----------批次信息生成开始---------------------------");
    //用于校验所有线程中是否有信息重复的set
    Set<String> validateSet = new ConcurrentHashSet<>();
    List<List<Map<String, String>>> averageAssign = StringUtil.averageAssign(commissionDatas, 5);
    CountDownLatch barrier = new CountDownLatch(averageAssign.size());

    for (int i = 0; i < averageAssign.size(); i++) {
      String subProcessId = processId + "--" + i;
      logger.info("------导入---card---线程" + i + ",执行数据量--" + averageAssign.get(i).size());
      ThreadUtil.cashThreadPool.execute(new ExecuteCardBatchToInput2(subProcessId,
          barrier,
          orderNoUtil,
          averageAssign.get(i),
          temporaryDao2,
          commissionDao2,
          related,
          transferBankDao,
          customCompanyRateConf, batchData,
          validateSet,
          agreementTemplateService,
          cardBinService,
          usersAgreementService,
          companyService,
          minAge,
          maxAge,
          blackUsersService, channelInterimBatchService, signShareService, realCompanyId));
    }
    barrier.await();
    logger.info("------------导入---card-------佣金明细处理完成，开始生成临时批次信息---------------------------");
  }

  public void executeInputDataByAli(ChannelInterimBatch batch,
      List<InputBatchData> inputBatchData,
      String menuId,
      ChannelRelated related,
      CustomCompanyRateConf customCompanyRateConf,
      Map<String, String> batchData,
      Integer minAge,
      Integer maxAge, String trueCompanyId) throws InterruptedException {

    String processId = (String) MDC.get(PROCESS);

    Company company = companyService.getCompanyByUserId(Integer.parseInt(related.getCompanyId()));
    //真实下发公司id
    String realCompanyId = company.getRealCompanyId();
    if (StringUtil.isEmpty(realCompanyId)) {
      realCompanyId = related.getCompanyId();
    }
    if (null != trueCompanyId && !"".equals(trueCompanyId) && !"null".equals(trueCompanyId)) {
      realCompanyId = trueCompanyId;
    }

    PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(
        String.valueOf(PayType.ALI_PAY.getCode()), related.getOriginalId(), realCompanyId);

    //原文件笔数
    Integer sourceFileNum = 0;

    logger.info("-------------导入---Ali---------封装导入的数据-------------------------");
    List<Map<String, String>> commissionDatas = new ArrayList<Map<String, String>>();
    for (int j = 1; j < inputBatchData.size(); j++) {// 获取每行
      InputAliBatchData row = (InputAliBatchData) inputBatchData.get(j);
      if (row == null) {
        continue;
      }
      if (StringUtil.isEmpty(row.getUserName()) &&
          StringUtil.isEmpty(row.getAmount()) &&
          StringUtil.isEmpty(row.getAliAccount())) {
        continue;
      }

      sourceFileNum++;

      String userName = ArithmeticUtil
          .subNameSpace(ArithmeticUtil.subZeroAndDot(row.getUserName()));// 收款人真实姓名(必要)
      String amount = ArithmeticUtil
          .subSpace(ArithmeticUtil.subZeroAndDot(row.getAmount()));// 金额(必要)
      String alipayAccount = ArithmeticUtil.subSpace(row.getAliAccount());// 收款人支付宝账号(必要)
      String phoneNo = ArithmeticUtil
          .subSpace(ArithmeticUtil.subZeroAndDot(row.getPhoneNo()));// 手机号
      String certId = ArithmeticUtil.subSpace(ArithmeticUtil.subZeroAndDot(row.getCertId()));// 身份证号
      String documentType = ArithmeticUtil
          .subSpace(ArithmeticUtil.subZeroAndDot(row.getCertType()));// 证件类型
      String remark = ArithmeticUtil.subZeroAndDot(row.getRemark());//  备注

      HashMap<String, String> data = new HashMap<>();
      data.put("userName", userName);
      data.put("alipayAccount", alipayAccount);
      data.put("certId", certId);
      data.put("amount", amount);
      data.put("documentType", documentType);
      data.put("remark", remark);
      data.put("menuId", menuId);
      data.put("phoneNo", phoneNo);

      //UserRelated userRelated = userRelatedDao.selectIsWhiteList(related.getOriginalId(), related.getCompanyId(), certId);
      WhiteUser whiteUser = new WhiteUser();
      whiteUser.setCertId(certId);
      whiteUser.setDocumentType(1);
      whiteUser.setCustomkey(related.getOriginalId());
      whiteUser.setCompanyId(related.getCompanyId());
      Integer isWhiteUser = whiteUserService.checkIsWhiteUser(whiteUser);
      String isWhiteList = "0";
      if (isWhiteUser > 0) {
        isWhiteList = "1";
      }
      data.put("isWhiteList", isWhiteList);


      //在这里处理支付宝通道拆单
      if (PaymentFactory.ALIPAY.equals(paymentConfig.getPathNo())) {

        addSplitOrder(amount, "50000", data, commissionDatas, batch);

        //addSplitOrder方法中已往commissionDatas添加当前循环记录，直接跳过当前循环
        continue;

      }


      commissionDatas.add(data);
      logger.info("-----------导入---Ali------------userName：" + userName
          + " alipayAccount=" + alipayAccount
          + " certId=" + certId
          + " amount=" + amount
          + " documentType=" + documentType
          + " remark=" + remark
          + "---------------------------");
    }

    batch.setSourceFileNum(sourceFileNum);
    batch.setInputPathNo(paymentConfig.getPathNo());

    logger.info("-----------导入---Ali-----------批次信息生成开始--------------------------");
    Set<String> validateSet = new ConcurrentHashSet<String>();
    List<List<Map<String, String>>> averageAssign = StringUtil.averageAssign(commissionDatas, 5);
    CountDownLatch barrier = new CountDownLatch(averageAssign.size());

    for (int i = 0; i < averageAssign.size(); i++) {
      String subProcessId = processId + "--" + i;
      logger.info("------导入---Ali----线程" + i + ",执行数据量--" + averageAssign.get(i).size());
      ThreadUtil.cashThreadPool.execute(new ExecuteAliBatchToInput2(subProcessId,
          barrier,
          orderNoUtil,
          averageAssign.get(i),
          transferBankDao,
          temporaryDao2,
          commissionDao2,
          related,
          customCompanyRateConf,
          dataService,
          batchData,
          agreementTemplateService,
          validateSet,
          usersAgreementService,
          companyService,
          minAge,
          maxAge,
          blackUsersService, channelInterimBatchService, signShareService, realCompanyId));
    }
    barrier.await();
    logger.info("---------导入---Ali-----------佣金明细处理完成，开始生成临时批次信息------------------------");
  }


  public void executeInputDataByWeChat(String batchId,
      Sheet sheet,
      String menuId,
      ChannelRelated related,
      CustomCompanyRateConf customCompanyRateConf, Map<String, String> batchData)
      throws InterruptedException {

    //		String processId = (String) MDC.get(PROCESS);

    logger.info("------------导入---WeChat----------封装导入的数据---------------------------");
    List<Map<String, String>> commissionDatas = new ArrayList<Map<String, String>>();
    for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {// 获取每行
      XSSFRow row = (XSSFRow) sheet.getRow(j);
      if (row == null) {
        continue;
      }
      if (StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0)))
          && StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1)))
          && StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(3)))) {
        continue;
      }
      String userName = ArithmeticUtil.subNameSpace(
          ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0))));// 收款人真实姓名(必要)
      String bankCard = ArithmeticUtil
          .subSpace(ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(1))));// 收款人银行卡号
      String certId = ArithmeticUtil
          .subSpace(ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(3))));// 身份证号
      String amount = ArithmeticUtil
          .subSpace(ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(4))));// 金额(必要)
      String bankName = ArithmeticUtil
          .subSpace(ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(5))));// 银行姓名
      String documentType = ArithmeticUtil
          .subSpace(ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(6))));// 证件类型
      String remark = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(7)))
          .trim();// 备注
      String phoneNo = "17701393451";

      Map<String, String> data = new HashMap<String, String>();
      data.put("userName", userName);
      data.put("bankCard", bankCard);
      data.put("certId", certId);
      data.put("amount", amount);
      data.put("bankName", bankName);
      data.put("documentType", documentType);
      data.put("remark", remark);
      data.put("menuId", menuId);
      data.put("phoneNo", phoneNo);
      commissionDatas.add(data);
      logger.info("-----------导入---WeChat--------userName：" + userName
          + " bankCard=" + bankCard
          + " certId=" + certId
          + " amount=" + amount
          + " bankName=" + bankName
          + " documentType=" + documentType
          + "---------------------------");
    }

    logger.info("-----------导入---WeChat-----------批次信息生成开始---------------------------");
    //		Set<String> validateSet = new ConcurrentHashSet<String>();
    List<List<Map<String, String>>> averageAssign = StringUtil.averageAssign(commissionDatas, 5);
    CountDownLatch barrier = new CountDownLatch(averageAssign.size());

    for (int i = 0; i < averageAssign.size(); i++) {
      logger.info("---导入---WeChat----线程" + i + ",执行数据量--" + averageAssign.get(i).size());
      ThreadUtil.cashThreadPool.execute(new ExecuteWeChatBatchToInput());
    }
    barrier.await();
    logger.info("------------导入---WeChat----------佣金明细处理完成，开始生成临时批次信息-------------------------");
  }

  @Override
  public boolean checkRepeatCommission(String batchId, String originalId, String companyId) {

    String transferAmount = null;
    String transferInAccountNo = null;
    String transferInAccountName = null;

    Map<String, Object> param = new HashMap<>(12);
    param.put("originalId", originalId);
    param.put("batchIds", batchId);
    param.put("status", "1");
    List<CommissionTemporary> commissionTempList = temporaryDao2
        .getCommissionedByBatchIdsAndParam(param);
    //		List<CommissionTemporary> commissionTempList = temporaryDao2.getCommissionsByBatchId(batchId, originalId);
    CommissionTemporary commissionTemporary = null;
    for (int j = 0; j < commissionTempList.size(); j++) {
      commissionTemporary = commissionTempList.get(j);

      transferAmount = commissionTemporary.getAmount();
      transferInAccountNo = commissionTemporary.getBankCardNo();
      transferInAccountName = commissionTemporary.getUserName();

      logger.info(
          "--------------提示检查---------->下发参数上送前校验 ,参数[amount={},userName={},account={},customKey={},companyId={}]",
          transferAmount, transferInAccountName, transferInAccountNo, originalId, companyId);
      String key1 = transferInAccountName + "," + transferAmount + "," + transferInAccountNo + ","
          + originalId + "," + companyId;

      logger.info("--------------提示检查---------->下发参数上送前校验key1:" + key1);
      boolean setPayInfoSuccess = utilCacheManager.putIfAbsent(key1, "1", CommonString.LIFETIME);
      if (!setPayInfoSuccess) {
        long lastLife = utilCacheManager.getCacheLife(key1) / 60;
        logger.info("----------提示检查-------------->重复下发,剩余时间(分钟):[{}]", lastLife);
        return true;
      } else {
        logger.info("----------提示检查---------通过-----删除key1");
        utilCacheManager.remove(key1);
      }
    }
    return false;
  }

//  @Override
//  public Map<String, Object> ymGrantTransferBefore(String originalId, String companyId,
//      String batchId, String remark, String operatorName, String phone, String realCompanyId) {
//
//    try {
//
//      logger.info("--------溢美服务商模式预下单开始,batchId:{}---------------", batchId);
//
//      ChannelInterimBatch interimBatch = interimBatchDao2
//          .getChannelInterimBatchByOrderno(batchId, originalId);
//      if (interimBatch == null) {
//        return returnInfo(RespCode.error115, RespCode.BATCH_NOT_EXIST);
//      }
//
//      ChannelCustom custom = channelCustomDao.getCustomByCustomkey(originalId, null);//商户对象
//      ChannelRelated related = channelRelatedDao.getRelatedByCompAndOrig(originalId, companyId);
//
//      //导入前检测是否有正在的数据，有的话返回前端提示
//      boolean checkFlag = checkRepeatCommission(batchId, originalId, companyId);
//      if (checkFlag) {
//        return returnInfo(RespCode.error107, RespCode.CHEACK_REPEAT_WARN);
//      }
//
//      calculationFeeService.locationCustomCompanyRateConf(companyId, originalId, batchId);
//      customLimitConfService.customAmountLimit2(companyId, originalId, batchId);
//      boolean autoSupplement = customLimitConfService.autoSupplement(companyId, originalId);
//
//      Set<String> validateSet = new ConcurrentHashSet<>();
//      userCommissionService
//          .updateYmTemporaryBatchData(batchId, originalId, companyId, autoSupplement, validateSet);
//      validateSet.clear();
//
//      interimBatch = interimBatchDao2.getChannelInterimBatchByOrderno(batchId, originalId);
//
//      //判断是未加锁的批次进行余额扣减
//      if (interimBatch.getStatus() != 9) {
//
//        String handleAmount = interimBatch.getHandleAmount(); //校验成功总金额+服务费
//        String compBalance = channelHistoryService.getBalance(originalId, related.getCompanyId(),
//            String.valueOf(interimBatch.getPayType()));//根据提交批次数据支付类型获取对应的余额
//        logger.info("余额：" + compBalance + "下发总金额：" + handleAmount);
//        if (ArithmeticUtil.compareTod(compBalance, handleAmount) < 0) {
//          return returnInfo(RespCode.error115, custom.getCompanyName() + "下发预存金额不足");
//        }
//
//        //锁定临时批次
//        interimBatchDao2.batchLock(batchId);
//
//        //扣减余额
//        try {
//          CustomBalanceAndAccount customBalanceAndAccount = new CustomBalanceAndAccount()
//              .setOriginalId(interimBatch.getCustomkey())
//              .setAmount(interimBatch.getAmount())
//              .setCompanyId(related.getCompanyId())
//              .setPayType(interimBatch.getPayType())
//              .setHandleAmount(handleAmount)
//              .setPassNum(interimBatch.getPassNum()).setRealCompanyId(realCompanyId)
//              .setAmount(interimBatch.getAmount())
//              .setOrderNo(interimBatch.getOrderno()).setOperator(operatorName)
//              .setTradeType(TradeType.WEBPAYMENT.getCode());
//          customBalanceAndAccountService
//              .updateCustomBalanceAndAccount(customBalanceAndAccount);
//
//        } catch (Exception e) {
//          logger.error(e.getMessage(), e);
//          //扣费失败解锁临时批次
//          interimBatchDao2.batchUnLock(batchId, interimBatch.getStatus());
//          return returnInfo(RespCode.error115, e.getMessage());
//        }
//        /**
//         * @Description 类型
//         **/
//        Company checkCompany = companyService.getCompanyByUserId(Integer.parseInt(companyId));
//        if (checkCompany.getCompanyType() == 1 && !companyId.equals(realCompanyId)) {
//          /**
//           * @Description 扣减转包服务公司在实际下发公司余额
//           *
//           **/
//          try {
//
//            CustomCompanyRateConf customCompanyMinRate = customCompanyRateConfImpl
//                .getCustomCompanyMinRate(companyId, realCompanyId);
//
//            String deductionAmount = interimBatch.getAmount();
//            if (ServiceFeeType.ISSUE.getCode() == customCompanyMinRate.getServiceFeeType()
//                || ServiceFeeType.PERSON.getCode() == customCompanyMinRate.getServiceFeeType()){
//              deductionAmount = ArithmeticUtil.subStr(interimBatch.getAmount(), ArithmeticUtil
//                  .mulStr(interimBatch.getAmount(), customCompanyMinRate.getCustomRate(), 2), 2);
//            }
//
//            customBalanceService.updateCustomBalance(CommonString.DEDUCTION,
//                new CustomBalanceHistory(companyId, realCompanyId,
//                    interimBatch.getPayType(), deductionAmount, interimBatch.getPassNum(),
//                    TradeType.WEBPAYMENT.getCode(), interimBatch.getOrderno(), operatorName));
//          } catch (BalanceException e) {
//            logger.error("扣减转包服务公司在实际下发公司余额异常" + e.getMessage(), e);
//
//          }
//        }
//
//      }
//
//      interimBatch.setRealCompanyId(realCompanyId);
//      //溢美预下单
//      Map<String, String> prePayResult = ymyfCommonService.prePay(interimBatch, phone);
//      //更改获取短信验证码状态
//      interimBatchDao2.updateBatchLockState(batchId, BatchLockStatus.GETTING.getCode());
//      logger.info("--------溢美服务商模式预下单返回结果:{}---------------", prePayResult);
//
//      if (prePayResult.get("code").equals("0000")) {
//        Map<String, Object> result = new HashMap<>(10);
//        result.put(RespCode.RESP_STAT, RespCode.success);
//        result.put(RespCode.RESULT, prePayResult);
//
//        interimBatchDao2.updateBatchLockState(batchId, BatchLockStatus.GETSUCCESS.getCode());
//        return result;
//      } else {
//        interimBatchDao2.updateBatchLockState(batchId, BatchLockStatus.GETFAILURE.getCode());
//        //预下单失败
//        return returnInfo(RespCode.PRE_PAY_FAIL,
//            RespCode.codeMaps.get(RespCode.PRE_PAY_FAIL) + prePayResult.get("msg"));
//      }
//
//    } catch (Exception e) {
//      logger.error(e.getMessage(), e);
//      interimBatchDao2.updateBatchLockState(batchId, BatchLockStatus.CONFIRMERROR.getCode());
//      return returnInfo(RespCode.error115, "打款异常");
//    }
//  }

  private Map<String, Object> returnInfo(int state, String msg) {
    Map<String, Object> result = new HashMap<>(10);
    result.put(RespCode.RESP_STAT, state);
    result.put(RespCode.RESP_MSG, msg);
    return result;
  }

  @Override
  public Map<String, Object> ymGrantTransferAfter(ChannelInterimBatch batch, String code,
      String operatorName, String customName, String companyName) {

    String processId = (String) MDC.get(PROCESS);
    Map<String, Object> result;
    try {

      result = ymyfCommonService.smsPayResultBatchQuery(batch);
      if (!"1010".equals(result.get("code"))) {
        logger.error("------------溢美预下单确认验证码异常下发,result:{}--------------", result);
        return returnInfo(RespCode.error101, "存在该批次下发记录,请勿重复操作下发");
      }
      result.clear();

      logger.info("------------溢美预下单确认验证码开始,batchId:{},code:{}------------", batch.getOrderno(),
          code);

      interimBatchDao2
          .updateBatchLockState(batch.getOrderno(), BatchLockStatus.CONFIRMING.getCode());
      result = ymyfCommonService.smsPay(batch, code);
      logger.info("------------溢美预下单确认验证码返回结果{}------------", result);

      if ("0000".equals(result.get("code"))) {
        interimBatchDao2
            .updateBatchLockState(batch.getOrderno(), BatchLockStatus.CONFIRMSUCCESS.getCode());

        // 生成交易订单号（申请单号）
        String orderNo = orderNoUtil.getChannelSerialno();

        logger.info("------------溢美预下单校验验证码成功,batchId:{},开始同步正式明细表,orderNo:{}------------",
            batch.getOrderno(), orderNo);

        logger.info("------------溢美服务商下发开始同步正式明细表,orderNo:{}------------", batch.getOrderno(),
            orderNo);

        String newBatchId = syncBatch(batch, orderNo, customName, companyName, operatorName,
            processId);

        if (StringUtil.isEmpty(newBatchId)) {
          return returnInfo(RespCode.error101, RespCode.OPERATING_FAILED);
        }

        logger.info("------------溢美服务商下发同步正式明细表结束,orderNo:{}------------", batch.getOrderno(),
            orderNo);

        //发送消息到mq  经过一段时间后。接收消息。开始查询。
        providerJmsTemplate.send(warningOrderDestination, session -> {
          TextMessage message = session.createTextMessage(newBatchId);
          message
              .setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, Long.parseLong(warningTime));
          return message;
        });

        // 正式表的批次号
        result.put("batchNo", newBatchId);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, "打款成功");
      } else if ("1010".equals(result.get("code"))) {
        interimBatchDao2
            .updateBatchLockState(batch.getOrderno(), BatchLockStatus.CONFIRMERROR.getCode());
        return returnInfo(RespCode.error115, "打款异常,请联系运营人员");
      } else {
        interimBatchDao2
            .updateBatchLockState(batch.getOrderno(), BatchLockStatus.CONFIRMFAILURE.getCode());

        return returnInfo(RespCode.error101, String.valueOf(result.get("msg")));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      interimBatchDao2
          .updateBatchLockState(batch.getOrderno(), BatchLockStatus.CONFIRMUNKNOWNERROR.getCode());
      return returnInfo(RespCode.error115, "打款异常");
    }

    return result;
  }

  @Override
  @Transactional
  public void unlockBatch(ChannelInterimBatch batch) {
    if (batch.getFailedNum() > 0) {
      if (batch.getPassNum() == 0) {
        batch.setStatus(2);//全部失败
      } else {
        batch.setStatus(3);//部分失败
      }
    } else if (batch.getFailedNum() == 0) {
      if (batch.getPassNum() == 0) {
        batch.setStatus(2);//全部失败
      } else {
        batch.setStatus(1);//全部成功
      }
    }
    interimBatchDao2.updateChannelInterimBatch(batch);
    customBalanceService.updateCustomBalance(CommonString.ADDITION,
        new CustomBalanceHistory(batch.getCustomkey(), batch.getRecCustomkey(),
            batch.getPayType(), batch.getHandleAmount(), 1,
            TradeType.PAYMENTREFUND.getCode()));
//		updateBalance(batch.getCustomkey(), batch.getRecCustomkey(), batch.getPayType(), batch.getHandleAmount(), CommonString.REFUND);

  }


  @Transactional
  @Override
  public String syncBatch(ChannelInterimBatch batch, String orderNo, String customName,
      String companyName, String operatorName, String processId) {
    ChannelRelated related = channelRelatedDao
        .getRelatedByCompAndOrig(batch.getCustomkey(), batch.getRecCustomkey());

    //临时数据持久化到正式表中
    ChannelHistory history = new ChannelHistory();
    history.setOrdername(batch.getOperatorName());
    history.setAmount("0");
    history.setPassNum(0);
    history.setAccountName(customName);
    history.setCustomkey(batch.getCustomkey());
    history.setRecCustomkey(batch.getRecCustomkey());
    history.setOrdername("佣金发放");
    history.setOrderno(orderNo);
    history.setOriginalBeachNo(batch.getOrderno());
    history.setStatus(3);// 初始化状态为处理中
    history.setRemark(batch.getRemark());
    history.setPayType(batch.getPayType());// 设置支付方式
    history.setTransfertype(2);// 交易类型 发放佣金
    history.setServiceFee(batch.getServiceFee());
    history.setSupplementServiceFee(batch.getSupplementServiceFee());
    history.setMfkjServiceFee(batch.getMfkjServiceFee());
    history.setOperatorName(batch.getOperatorName());
    history.setPayUserName(operatorName);
    history.setBatchAmount(batch.getBatchAmount());
    history.setBatchNum(batch.getBatchNum());
    history.setHandleAmount("0");
    history.setMenuId(batch.getMenuId());
    history.setBatchName(batch.getBatchName());
    history.setBatchDesc(batch.getBatchDesc());
    history.setFileName(batch.getFileName());
    history.setFailedAmount(batch.getFailedAmount());
    history.setTaskAttachmentFile(batch.getTaskAttachmentFile());
    BigDecimal deducationAmount = new BigDecimal(batch.getHandleAmount())
        .multiply(new BigDecimal(100));
    history.setDeductionAmount(deducationAmount.intValue());
    history.setRealCompanyId(batch.getRealCompanyId());
    channelHistoryService.addChannelHistory(history);

    //更新临时批次此信息
    interimBatchDao2.updateInterimBatchStatus(batch.getOrderno());
//				interimBatchDao2.batchUnLock(batch.getOrderno());

    //更新佣金表批次号
    String newBatchId = history.getId() + "";

    CustomMenu customMenu = customMenuDao.getCustomMenuById(batch.getMenuId());
    Map<String, Object> batchData = new HashMap<>(20);
    batchData.put("batchId", newBatchId);
    batchData.put("operatorName", operatorName);
    batchData.put("originalId", batch.getCustomkey());
    batchData.put("fileName", batch.getFileName());
    batchData.put("batchDesc", batch.getBatchDesc());
    batchData.put("batchName", batch.getBatchName());
    batchData.put("menuId", batch.getMenuId());
    batchData.put("menuName", customMenu.getContentName());
    batchData.put("customName", customName);
    batchData.put("companyName", companyName);
    batchData.put("companyId", batch.getRecCustomkey());

    logger.info("------------溢美服务商下发开始同步正式明细表,orderNo:{}------------", batch.getOrderno(), orderNo);

    //待发放佣金列表
    List<CommissionTemporary> commissionTempList = temporaryDao2
        .getCommissionsByBatchId(batch.getOrderno(), batch.getCustomkey());

    List<List<CommissionTemporary>> averageAssign = StringUtil.averageAssign(commissionTempList, 5);
    CyclicBarrier cb = new CyclicBarrier(averageAssign.size(),
        new CountOptionData2(processId,
            newBatchId,
            commissionDao2,
            channelHistoryService,
            temporaryDao2));

    for (int i = 0; i < averageAssign.size(); i++) {
      String subProcessId = processId + "--" + i;
      logger.info("线程" + i + ",执行数据量--" + averageAssign.get(i).size());
      ThreadUtil.cashThreadPool.execute(new ExecuteYmBatchGrantOption(subProcessId,
          cb,
          orderNoUtil,
          averageAssign.get(i),
          commissionDao2,
          userSerivce,
          related,
          batchData));
    }

    return newBatchId;
  }


  public void addSplitOrder(String amount, String singleOrderLimit,
      HashMap<String, String> sourceData,
      List<Map<String, String>> commissionDatas,
      ChannelInterimBatch batch) {

    //clone一个新的HashMap对象，请确保HashMap中不包含引用对象数据
    HashMap<String, String> targetData = (HashMap<String, String>) sourceData.clone();

    //通道限制单笔大于限额拆单,不大于不进行拆单
    if (ArithmeticUtil.compareTod(amount, singleOrderLimit) != 1) {
      //对金额重新赋值，防止递归调用时金额非拆单后剩余金额
      targetData.put("amount", amount);
      commissionDatas.add(targetData);
    } else {
      Integer splitOrderNum = batch.getSplitOrderNum() != null ? batch.getSplitOrderNum() : 0;
      //产生拆单数量+1
      batch.setSplitOrderNum(++splitOrderNum);
      targetData.put("amount", singleOrderLimit);
      //拆单后剩余金额
      String laveAmount = ArithmeticUtil.subStr2(amount, singleOrderLimit);
      //创建一条满限额记录，剩下金额继续拆单
      commissionDatas.add(targetData);
      logger.info(
          "--------------批次导入执行拆分限额订单方法产生一条拆单记录,sourceData:{},targetData:{},laveAmount:{}-------------------------",
          sourceData, targetData, laveAmount);
      //剩余金额继续拆单
      addSplitOrder(laveAmount, singleOrderLimit, targetData, commissionDatas,
          batch);
    }
  }

  public boolean addMerageOrder(Map<String, HashMap<String, String>> validateOrderMap, String key,
      String amount, List<Map<String, String>> commissionDatas,
      ChannelInterimBatch batch) {

    HashMap<String, String> sourceData = validateOrderMap.get(key);
    logger.info("--------------批次导入执行合并订单方法,sourceData:{}-------------------------",
        sourceData);

    //校验该记录是否已经合并过订单，未合并过计为一个合并人数
    if (!sourceData.containsKey("preMergeAmount")) {
      Integer mergeUserNum = batch.getMergeUserNum() != null ? batch.getMergeUserNum() : 0;
      //产生拆单数量+1
      batch.setMergeUserNum(++mergeUserNum);
      logger.info("--------------批次导入执行合并订单增加一人,key:{}-------------------------",
          key);
    }
    //删除当前list中原有的相同key记录,确保删除成功再执行
    if (commissionDatas.remove(sourceData)) {
      Integer mergeOrderNum = batch.getMergeOrderNum() != null ? batch.getMergeOrderNum() : 0;
      //产生拆单数量+1
      batch.setMergeOrderNum(++mergeOrderNum);
      //记录合并订单之前的金额，主要用于判断合并了多少人
      sourceData.put("preMergeAmount", sourceData.get("amount"));
      //更新累加金额
      sourceData.put("amount", ArithmeticUtil.addStr(sourceData.get("amount"), amount));
      //重新add list中
      commissionDatas.add(sourceData);
      //重新put更新后记录，确保下次取出的sourceData是更新后的
      validateOrderMap.put(key, sourceData);
      logger.info("--------------批次导入执行合并订单方法成功,合并后sourceData:{}-------------------------",
          sourceData);
      return true;
    } else {
      logger.error(
          "--------------批次导入执行合并订单方法异常,删除原有data失败,sourceData:{}-------------------------",
          sourceData);

      return false;
    }

  }


}
