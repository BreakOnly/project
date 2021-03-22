package com.jrmf.service;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.HistoryStatus;
import com.jrmf.controller.constant.InvoiceApprovalStatus;
import com.jrmf.controller.constant.InvoiceOrderStatus;
import com.jrmf.controller.constant.TransferType;
import com.jrmf.controller.constant.sms.SmsTemplateCodeEnum;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.ChannelHistoryPic;
import com.jrmf.domain.CompanyEsignContractTemplate;
import com.jrmf.domain.MerchantTransaction;
import com.jrmf.domain.OemConfig;
import com.jrmf.domain.Page;
import com.jrmf.domain.Parameter;
import com.jrmf.domain.QbInvoiceApprovalRecord;
import com.jrmf.domain.QbInvoiceRecord;
import com.jrmf.domain.SMSChannelConfig;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.persistence.ChannelHistoryDao;
import com.jrmf.persistence.CompanyDao;
import com.jrmf.persistence.CompanyEsignContractTemplateDao;
import com.jrmf.persistence.CustomBalanceDao;
import com.jrmf.persistence.ParameterDao;
import com.jrmf.taxsettlement.api.APIDockingManager;
import com.jrmf.taxsettlement.api.MerchantAPIDockingConfig;
import com.jrmf.taxsettlement.api.gateway.APIDockingGatewayDataUtil;
import com.jrmf.taxsettlement.api.gateway.restful.APIDefinitionConstants;
import com.jrmf.taxsettlement.api.security.sign.SignWorker;
import com.jrmf.taxsettlement.api.security.sign.SignWorkers;
import com.jrmf.taxsettlement.api.service.recharge.APIRechargeStatus;
import com.jrmf.taxsettlement.api.service.recharge.RechargeRecordListServiceAttachment;
import com.jrmf.taxsettlement.api.service.recharge.RechargeStatusNotice;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.ClientUtils;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.sms.channel.SMSChannel;
import com.jrmf.utils.sms.channel.SMSChannelFactory;
import com.jrmf.utils.threadpool.ThreadUtil;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.Destination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhangzehui
 * @version 创建时间：2017年12月16日
 */
@Service("channelHistoryService")
public class ChannelHistoryServiceImpl implements ChannelHistoryService {

  private static Logger logger = LoggerFactory.getLogger(ChannelHistoryServiceImpl.class);

  public final static String PROCESS = "process";

  @Autowired
  private ChannelHistoryDao channelHistoryDao;
  @Autowired
  private CustomBalanceDao customBalanceDao;
  @Autowired
  private JmsTemplate providerJmsTemplate;
  @Autowired
  private Destination rechargeRequestDestination;
  @Autowired
  private APIDockingManager apiDockingManager;
  @Autowired
  private SignWorkers signWorkers;
  @Autowired
  private OemConfigService oemConfigService;
  @Autowired
  private ChannelCustomDao channelCustomDao;
  @Autowired
  private QbInvoiceApprovalRecordService invoiceApprovalRecordService;
  @Autowired
  private QbInvoiceRecordService invoiceRecordService;
  @Autowired
  protected CompanyDao companyDao;
  @Autowired
  protected ParameterDao parameterDao;
  @Autowired
  private CompanyEsignContractTemplateDao companyEsignContractTemplateDao;


  @Override
  public int addChannelHistory(ChannelHistory history) {
    return channelHistoryDao.addChannelHistory(history);
  }

  @Override
  public List<ChannelHistory> getChannelHistoryByParam(Map<String, Object> param) {
    return channelHistoryDao.getChannelHistoryByParam(param);
  }

  @Override
  public List<ChannelHistory> getChannelHistoryByParamOnJob(Map<String, Object> param) {
    return channelHistoryDao.getChannelHistoryByParamOnJob(param);
  }

  @Override
  public int updateChannelHistory(ChannelHistory history) {
    return channelHistoryDao.updateChannelHistory(history);
  }

  @Override
  public void deleteById(int id) {
    channelHistoryDao.deleteById(id);
  }

  @Override
  public String getBalance(String originalId, String companyId, String payType) {
    Map<String, Object> params = new HashMap<>(5);
    params.put(CommonString.CUSTOMKEY, originalId);
    params.put(CommonString.COMPANYID, companyId);
    params.put(CommonString.PAYTYPE, payType);
    Integer queryBalance = customBalanceDao.queryBalance(params);
    if (queryBalance == null || queryBalance == 0) {
      return CommonString.MINBALANCE;
    }
    return new BigDecimal(queryBalance).divide(new BigDecimal(100)).toString();
  }

  @Override
  public ChannelHistory getChannelHistoryById(String id) {
    return channelHistoryDao.getChannelHistoryById(id);
  }

  @Override
  public List<ChannelHistory> getChannelHistoryList(Map<String, Object> param) {
    return channelHistoryDao.getChannelHistoryList(param);
  }

  @Override
  public List<ChannelHistory> getChannelHistoryByCompany(Map<String, Object> param) {
    return channelHistoryDao.getChannelHistoryByCompany(param);
  }

  @Override
  public List<ChannelHistory> getChannelHistoryBySubmit(Map<String, Object> param) {
    return channelHistoryDao.getChannelHistoryBySubmit(param);
  }

  @Override
  public void deleteByOrderno(String orderno) {
    channelHistoryDao.deleteByOrderno(orderno);
  }

  @Override
  public ChannelHistory getChannelHistoryByOrderno(String orderno) {
    return channelHistoryDao.getChannelHistoryByOrderno(orderno);
  }

  @Override
  public List<Map<String, Object>> batchResultQuery(Map<String, Object> param) {
    return channelHistoryDao.batchResultQuery(param);
  }

  @Override
  public List<Map<String, Object>> batchResultQueryByCompany(Map<String, Object> param) {
    return channelHistoryDao.batchResultQueryByCompany(param);
  }

  @Override
  public List<Map<String, Object>> geCustomChargeDetail(Map<String, Object> paramMap) {
    return channelHistoryDao.geCustomChargeDetail(paramMap);
  }

  @Override
  public List<ChannelHistory> getHistoryList(Map<String, Object> param) {
    return channelHistoryDao.getHistoryList(param);
  }

  @Override
  public List<MerchantTransaction> selectTransactionList(Map<String, Object> param) {
    return channelHistoryDao.selectTransactionList(param);
  }

  @Override
  public List<MerchantTransaction> selectTransactionListByProxy(Map<String, Object> param) {
    return channelHistoryDao.selectTransactionListByProxy(param);
  }

  @Override
  public int querybillingListCount(Page page) {
    return channelHistoryDao.querybillingListCount(page);
  }

  @Override
  public List<Map<String, Object>> billingList(Page page) {
    return channelHistoryDao.billingList(page);
  }

  @Override
  public String getTotalAmountByOrderNo(String orderNo) {
    return channelHistoryDao.getTotalAmountByOrderNo(orderNo);
  }

  @Override
  public List<Map<String, Object>> checkCommonCompanyAndCustom(String orderNo) {
    return channelHistoryDao.checkCommonCompanyAndCustom(orderNo);
  }

  @Override
  public List<Map<String, Object>> queryBillingListNoPage(Page page) {
    return channelHistoryDao.queryBillingListNoPage(page);
  }

  @Override
  public List<ChannelHistory> getAutoConfirmList() {
    return channelHistoryDao.getAutoConfirmList();
  }

  @Override
  public void rechargeCallback(ChannelHistory history) {

    String processId = MDC.get(PROCESS);

    ThreadUtil.pdfThreadPool.execute(() -> {
      try {
        MDC.put(PROCESS, processId + "recharge");
        providerJmsTemplate.send(rechargeRequestDestination, session -> {
          Map<String, Object> outData = new HashMap<>(18);
          outData.put("customOrderNo", history.getCustomOrderNo());
          outData.put("orderNo", history.getOrderno());
          APIRechargeStatus apiRechargeStatus = APIRechargeStatus.codeOf(history.getStatus());
          outData.put("dealStatus", apiRechargeStatus.getAPIStatus());
          outData.put("dealStatusMsg", apiRechargeStatus.getDesc());
          if (apiRechargeStatus == APIRechargeStatus.FAILED) {
            outData.put("dealStatusMsg", history.getRemark());
          }
          outData.put("accountTime", history.getUpdatetime());
          outData.put("amount", history.getAmount());
          MerchantAPIDockingConfig dockingConfig = apiDockingManager
              .getMerchantAPIDockingConfig(history.getCustomkey());
          String signType = dockingConfig.getSignType();

          outData.put(APIDefinitionConstants.CFN_SIGN_TYPE, signType);
          outData.put(APIDefinitionConstants.CFN_TIMESTAMP,
              new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));

          Map<String, Object> toSignMap = APIDockingGatewayDataUtil.toSignMap(outData);
          SignWorker generator = signWorkers.get(signType);
          try {
            String sign = generator.generateSign(toSignMap, dockingConfig.getSignGenerationKey());
            outData.put(APIDefinitionConstants.CFN_SIGN, sign);
          } catch (Exception e) {
            logger.error("充值回调通知异常", e);
          }

          final RechargeStatusNotice notice = new RechargeStatusNotice(history.getNotifyUrl(),
              outData);
          return session.createObjectMessage(notice);
        });
      } finally {
        MDC.remove(PROCESS);
      }
    });
    ThreadUtil.pdfThreadPool.execute(() -> {
      try {
        MDC.put(PROCESS, processId + "notify");
        String oemUrl = history.getOemUrl();
        Map<String, Object> map = new HashMap<>(4);
        map.put("portalDomain", oemUrl);
        OemConfig oemConfig = oemConfigService.getOemByParam(map);
        String linkPhone = history.getLinkPhone();
        if (!StringUtil.isMobileNOBy11(linkPhone)) {
          logger.error("手机号:{},不是11位数字", linkPhone);
          return;
        }
        if (oemConfig == null) {
          logger.error("oem:{},配置未找到", oemUrl);
          return;
        }
        if (oemConfig.getSmsStatus() != 1) {
          logger.info("不需要发短信，{}", oemUrl);
          return;
        }
        String customkey = history.getCustomkey();
        ChannelCustom channelCustom = channelCustomDao.getCustomByCustomkey(customkey, null);
        if (channelCustom == null) {
          logger.error("customkey:{},未找到该商户", customkey);
          return;
        }
        String createtime = history.getCreatetime();
        String companyName = channelCustom.getCompanyName();
        String rechargeAmount = history.getRechargeAmount();
        String statusDesc = "审核失败";
        if (history.getStatus() == 1) {
          statusDesc = "已确认成功";
        }
        final String content = new StringBuilder().append("【").append(oemConfig.getSmsSignature())
            .append("】").append(createtime).append(",").append(companyName).append("充值申请")
            .append(statusDesc).append("，充值金额").append(rechargeAmount).append("元，请登录系统查看充值记录。")
            .toString();
        final String templateParam =
            "{\"createTime\":\"" + createtime + "\",\"companyName\":\"" + companyName
                + "\",\"statusDesc\":\"" + statusDesc + "\",\"rechargeAmount\":\"" + rechargeAmount
                + "\"}";
        Parameter param = new Parameter();
        param.setParamName(linkPhone);
        param.setParamValue(content);
        try {
          param.setFromip(ClientUtils.getLocalIP());
        } catch (Exception e) {
          logger.error("获取本地ip失败");
        }
        param.setParamStatus(1);
        param.setParamDate(new Date());
        param.setParamFlag("smsContent");
        param.setIsVoice(0);
        parameterDao.saveParameter(param);
        SMSChannelConfig channelConfig = companyDao.getSmsConfig();
        SMSChannel smsChannel = SMSChannelFactory.createChannel(channelConfig);

        boolean flag = smsChannel
            .sendSMS(new String[]{linkPhone}, content, oemConfig.getSmsSignature(),
                SmsTemplateCodeEnum.RECHARGE_CONFIRM.getCode(), templateParam);
        if (!flag) {
          logger.error("短信发送失败");
        }
      } finally {
        MDC.remove(PROCESS);
      }
    });
  }

  @Override
  public int getToBeConfirmedCount(String customKey, String companyId, Integer payType) {
    return channelHistoryDao.getToBeConfirmedCount(customKey, companyId, payType);
  }

  @Override
  public ChannelHistory getRechargeInfoById(String id) {
    return channelHistoryDao.getRechargeInfoById(id);
  }

  /**
   * 获取与代理商绑定关系的商户信息
   */
  @Override
  public List<Map<String, Object>> getProxyCustomCompanyDetail(Map<String, Object> paramMap) {
    return channelHistoryDao.getProxyCustomCompanyDetail(paramMap);
  }

  @Override
  public List<ChannelHistory> getWarningRechargeList(Integer minute) {
    return channelHistoryDao.getWarningRechargeList(minute);
  }

  @Override
  public void updateSendStatus(int id) {
    channelHistoryDao.updateSendStatus(id);
  }

  @Override
  public List<Map<String, String>> queryUserAgreementMatch(Page page) {
    return channelHistoryDao.queryUserAgreementMatch(page);
  }

  @Override
  public List<Map<String, String>> noAgreementCount(Map<String, Object> agreementMatch) {
    return channelHistoryDao.noAgreementCount(agreementMatch);
  }

  @Override
  public int agreementOtherCompanyCount(Map<String, String> params) {
    return channelHistoryDao.agreementOtherCompanyCount(params);
  }

  @Override
  public int queryUserAgreementMatchCount(Page page) {
    return channelHistoryDao.queryUserAgreementMatchCount(page);
  }

  @Override
  public List<Map<String, String>> queryUserAgreementMatchNoPage(Page page) {
    return channelHistoryDao.queryUserAgreementMatchNoPage(page);
  }

  @Override
  public String agreementOtherCompanyNames(Map<String, String> noAgreement) {
    return channelHistoryDao.agreementOtherCompanyNames(noAgreement);
  }

  @Override
  public List<Map<String, String>> payUsers(Map<String, Object> params) {
    return channelHistoryDao.payUsers(params);
  }

  @Override
  @Transactional
  public void approvalInvoice(ChannelHistory history) {

    if (StringUtil.isEmpty(history.getOrderno())) {
      return;
    }

    String processId = MDC.get(PROCESS);
    MDC.put(PROCESS, processId + "approvalInvoice");

    history = channelHistoryDao.getChannelHistoryByOrderno(history.getOrderno());

    if (InvoiceOrderStatus.SECTION_TYPE.getCode() == history.getInvoiceStatus()) {
      logger.error("-------------充值申请记录{}不满足勾兑预开票申请条件--------------------", history.getOrderno());
      return;
    }

    try {
      //获取该商户未核销完的发票记录
      QbInvoiceRecord invoiceRecord = invoiceRecordService
          .getAdvanceInvoice(history.getCustomkey(), history.getRecCustomkey());

      //如果存在未核销完的发票记录就进行核销
      if (invoiceRecord != null) {

        if (TransferType.SUCCESS.getCode() != history.getTransfertype()
            || HistoryStatus.SUCCESS.getCode() != history.getStatus()) {
          logger.error("-----------充值申请勾兑预开票记录申请异常,充值记录状态异常-------------orderNo:{}",
              history.getOrderno());
          return;
        }

        String orderNo = history.getOrderno();
        String invoiceSerialNo = invoiceRecord.getInvoiceSerialNo();
        String invoiceAmount = invoiceRecord.getInvoiceAmount();
        String approvalAmount = invoiceRecord.getApprovalAmount();
        String rechargeUnInvoiceAmount = history.getUnInvoiceAmount();

        //计算该预开票记录剩余未核销金额
        String unApprovalAmount = ArithmeticUtil.subStr2(invoiceAmount, approvalAmount);

        logger.info(
            "--------------充值申请勾兑预开票记录开始,orderNo:{},invoiceSerialNo:{},invoiceAmount:{},approvalAmount:{},rechargeUnInvoiceAmount:{}",
            orderNo, invoiceSerialNo, invoiceAmount, approvalAmount, rechargeUnInvoiceAmount);

        //创建核销记录
        QbInvoiceApprovalRecord invoiceApprovalRecord = new QbInvoiceApprovalRecord();
        invoiceApprovalRecord.setInvoiceAmount(invoiceAmount);
        invoiceApprovalRecord.setInvoiceSerialNo(invoiceSerialNo);
        invoiceApprovalRecord.setRechargeOrderNo(orderNo);

        //剩余未核销金额
        if (ArithmeticUtil.compareTod(unApprovalAmount, "0") > 0) {

          //充值申请记录未开票大于预开票剩余未核销金额
          if (ArithmeticUtil.compareTod(rechargeUnInvoiceAmount, unApprovalAmount) > 0) {
            invoiceRecord.setApproval(InvoiceApprovalStatus.FINISH_TYPE.getCode());
            invoiceRecord.setApprovalAmount(invoiceAmount);

            history.setInvoiceStatus(InvoiceOrderStatus.DOING_TYPE.getCode());
            history.setInvoiceAmount(unApprovalAmount);
            history.setUnInvoiceAmount(
                ArithmeticUtil.subStr2(rechargeUnInvoiceAmount, history.getInvoiceAmount()));

            invoiceApprovalRecord.setApprovalAmount(unApprovalAmount);
            invoiceApprovalRecord.setUnApprovalAmount("0");

          } else if (ArithmeticUtil.compareTod(rechargeUnInvoiceAmount, unApprovalAmount)
              < 0) {            //充值申请记录未开票金额小于预开票剩余未核销金额
            invoiceRecord.setApproval(InvoiceApprovalStatus.PART_TYPE.getCode());
            invoiceRecord.setApprovalAmount(
                ArithmeticUtil.addStr(approvalAmount, rechargeUnInvoiceAmount, 2));

            history.setInvoiceStatus(InvoiceOrderStatus.SECTION_TYPE.getCode());
            history.setInvoiceAmount(history.getRealRechargeAmount());
            history.setUnInvoiceAmount("0");

            invoiceApprovalRecord.setApprovalAmount(rechargeUnInvoiceAmount);
            invoiceApprovalRecord.setUnApprovalAmount(
                ArithmeticUtil.subStr2(invoiceAmount, invoiceRecord.getApprovalAmount()));

          } else { //充值申请未开票金额等于预开票剩余未核销金额
            invoiceRecord.setApproval(InvoiceApprovalStatus.FINISH_TYPE.getCode());
            invoiceRecord.setApprovalAmount(invoiceAmount);

            history.setInvoiceStatus(InvoiceOrderStatus.SECTION_TYPE.getCode());
            history.setInvoiceAmount(history.getRealRechargeAmount());
            history.setUnInvoiceAmount("0");

            invoiceApprovalRecord.setApprovalAmount(rechargeUnInvoiceAmount);
            invoiceApprovalRecord.setUnApprovalAmount("0");
          }

          if (StringUtil.isEmpty(invoiceRecord.getOrderNo())) {
            invoiceRecord.setOrderNo(orderNo);
          } else {
            invoiceRecord.setOrderNo(invoiceRecord.getOrderNo() + "," + orderNo);
          }

          channelHistoryDao.updateByInvoice(history);
          invoiceRecordService.updateByRecharge(invoiceRecord);
          invoiceApprovalRecordService.insert(invoiceApprovalRecord);

          logger.info(
              "--------------充值申请勾兑预开票记录完成-------------orderNo:{},invoiceSerialNo:{},充值申请记录未开票金额:{}",
              history.getOrderno(), invoiceRecord.getInvoiceSerialNo(),
              history.getUnInvoiceAmount());
          if (ArithmeticUtil.compareTod(history.getUnInvoiceAmount(), "0") > 0) {
            logger.info("--------------充值记录{}存在未开票金额,尝试勾兑下一笔预开票申请---------------",
                history.getOrderno());
            approvalInvoice(history);
          }

        }
      }
    } catch (Exception e) {
      logger.error("-------------充值申请记录{}勾兑预开票申请记录异常,勾兑可能出现失败--------------------",
          history.getOrderno());
      logger.error(e.getMessage(), e);
    } finally {
      MDC.remove(PROCESS);
    }

  }

  @Override
  public int selectTransactionListCount(Map<String, Object> model) {
    return channelHistoryDao.selectTransactionListCount(model);
  }

  @Override
  public int selectTransactionListByProxyCount(Map<String, Object> model) {
    return channelHistoryDao.selectTransactionListByProxyCount(model);
  }

  @Override
  public int batchResultQueryByCompanyCount(Map<String, Object> param) {
    return channelHistoryDao.batchResultQueryByCompanyCount(param);
  }

  @Override
  public ChannelHistory getByOriginalBeachNo(String batchId) {
    return channelHistoryDao.getByOriginalBeachNo(batchId);
  }

  @Override
  public int updateChannelHistorySummary(ChannelHistory history) {
    return channelHistoryDao.updateChannelHistorySummary(history);
  }

  @Override
  public int updateRechargeStatus(ChannelHistory history) {
    return channelHistoryDao.updateRechargeStatus(history);
  }

  @Override
  public String geCustomChargeAmount(Map<String, Object> paramMap) {
    return channelHistoryDao.geCustomChargeAmount(paramMap);
  }

  @Override
  public List<Map<String, Object>> getPicListByOrderNo(String orderNo) {
    return channelHistoryDao.getPicListByOrderNo(orderNo);
  }

  @Override
  public void insertChannelHistoryPic(ChannelHistoryPic channelHistoryPic) {
    channelHistoryDao.insertChannelHistoryPic(channelHistoryPic);
  }

  @Override
  public void deleteRechargeFileById(String id) {
    channelHistoryDao.deleteRechargeFileById(id);
  }

  @Override
  public ChannelHistoryPic getChannelHistoryPicById(String id) {
    return channelHistoryDao.getChannelHistoryPicById(id);
  }

  @Override
  public void updateChannelHistoryFileNumAddByOrderNo(String orderNo) {
    channelHistoryDao.updateChannelHistoryFileNumAddByOrderNo(orderNo);
  }

  @Override
  public void updateChannelHistoryFileNumMinusByOrderNo(String id) {
    channelHistoryDao.updateChannelHistoryFileNumMinusByOrderNo(id);
  }

  @Override
  public int selectCountByLetterStatus(int id) {
    return channelHistoryDao.selectCountByLetterStatus(id);
  }

  /**
   * @return void
   * @Author YJY
   * @Description 上传确认函
   * @Date 2020/12/28
   * @Param [id, file]
   **/
  @Override
  public boolean uploadLetter(int id, InputStream in, String fileName, String ftpUrl,
      String ftpUserName, String ftpPassword, String fileUrl) {

    String uploadFile = FtpTool
        .uploadFile(ftpUrl, 21, "/download/rechargeletter/", fileName, in, ftpUserName, ftpPassword);
    //上传成功
    if (!"error".equals(uploadFile)) {
      return channelHistoryDao.updateLetterStatusById(fileUrl, id) > 0 ? true : false;

    }
    return false;
  }

  @Override
  public CompanyEsignContractTemplate getCompanyEsignContractTemplateByParams(int companyId,
      Byte bizType) {
    return companyEsignContractTemplateDao
        .getCompanyEsignContractTemplateByParams(companyId, bizType);
  }

	@Override
	public List<RechargeRecordListServiceAttachment> apiGetChannelHistoryList(
			Map<String, Object> paramMap) {
		return channelHistoryDao.apiGetChannelHistoryList(paramMap);
	}

	@Override
	public String getTotalUninvoicedAmountByCompanyId(Integer companyId, String customKey) {
		return channelHistoryDao.getTotalUninvoicedAmountByCompanyId(companyId, customKey);
	}

}
