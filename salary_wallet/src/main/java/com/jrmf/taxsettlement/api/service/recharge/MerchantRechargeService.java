package com.jrmf.taxsettlement.api.service.recharge;

import static com.jrmf.controller.BaseController.PROCESS;

import com.jrmf.api.task.AutoAenerateRechargeLetter;
import com.jrmf.common.CommonString;
import com.jrmf.common.UserServiceFeignClient;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.CompanyType;
import com.jrmf.controller.constant.InvoiceOrderStatus;
import com.jrmf.controller.constant.RechargeType;
import com.jrmf.controller.constant.ServiceFeeType;
import com.jrmf.controller.constant.*;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.ChannelHistoryPic;
import com.jrmf.domain.Company;
import com.jrmf.domain.CustomCompanyRateConf;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.CompanyService;
import com.jrmf.service.CustomCompanyRateConfService;
import com.jrmf.service.CustomReceiveConfigService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.util.HexStringUtil;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.*;

import com.jrmf.utils.threadpool.ThreadUtil;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.units.qual.A;
import org.ehcache.xml.model.ServiceType;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author 种路路
 * @create 2019-08-19 11:22
 * @desc 商户充值接口
 **/
@ActionConfig(name = "商户充值")
public class MerchantRechargeService
    implements Action<MerchantRechargeServiceParams, MerchantRechargeServiceAttachment> {

  private static final Logger logger = LoggerFactory.getLogger(MerchantRechargeService.class);

  @Autowired
  private CustomCompanyRateConfService customCompanyRateConfService;
  @Autowired
  private CustomReceiveConfigService customReceiveConfigService;
  @Autowired
  private OrderNoUtil orderNoUtil;
  @Autowired
  private ChannelHistoryService channelHistoryService;
  @Autowired
  private BestSignConfig bestSignConfig;
  @Autowired
  private CompanyService companyService;
  @Autowired
  private CustomCompanyRateConfService rateConfService;
  @Autowired
  ChannelCustomService channelCustomService;

  @Value("${recharge.letter.tmp.path}")
  private String templateSavetmpPath;
  @Value("${fonts.path}")
  private String fontsPath;

  @Autowired
  UserServiceFeignClient userServiceFeignClient;

  @Override
  public String getActionType() {
    return APIDefinition.MERCHANT_RECHARGE.name();
  }

  @Override
  public ActionResult<MerchantRechargeServiceAttachment> execute(
      MerchantRechargeServiceParams actionParams) {
    String rechargeType = actionParams.getRechargeType();
    if (RechargeType.codeOfEnum(Integer.parseInt(rechargeType)) == null) {
      throw new APIDockingException(APIDockingRetCodes.RECHARGE_TYPE_ERROR.getCode(),
          APIDockingRetCodes.RECHARGE_TYPE_ERROR.getDesc());
    }
    String balanceAmount = actionParams.getBalanceAmount();
    Map<String, Object> hashMap = new HashMap<>(4);
    hashMap.put("customkey", actionParams.getMerchantId());
    hashMap.put("customOrderNo", actionParams.getCustomOrderNo());
    List<ChannelHistory> channelHistoryList = channelHistoryService.getChannelHistoryList(hashMap);
    if (!channelHistoryList.isEmpty()) {
      throw new APIDockingException(APIDockingRetCodes.REQUEST_NO_EXISTED.getCode(),
          actionParams.getCustomOrderNo());
    }

    String serviceFeeType = actionParams.getServiceFeeType();
    String amount = actionParams.getAmount();
    ChannelHistory chargeDetail = new ChannelHistory();
    chargeDetail.setCustomkey(actionParams.getMerchantId());
    chargeDetail.setStatus(0);
    chargeDetail.setTransfertype(1);
    if ((RechargeType.AMOUNT.getCode() + "").equals(rechargeType)) {
      List<Map<String, Object>> customRateConfList = customCompanyRateConfService
          .getCustomRateConfList(actionParams.getMerchantId(), actionParams.getTransferCorpId());
      if (customRateConfList.isEmpty()) {
        throw new APIDockingException(APIDockingRetCodes.FEE_RATE_GRADE_NOT_FOUND.getCode(),
            APIDockingRetCodes.FEE_RATE_GRADE_NOT_FOUND.getDesc());
      }
      Object serviceFeeTypeObj = customRateConfList.get(0).get("serviceFeeType");
      if (!serviceFeeType.equals(serviceFeeTypeObj + "")) {
        throw new APIDockingException(APIDockingRetCodes.FEE_TYPE_ERROR.getCode(),
            APIDockingRetCodes.FEE_TYPE_ERROR.getDesc());
      }
      int feeType = Integer.parseInt(serviceFeeType);

      chargeDetail.setServiceFeeType(feeType);
      String feeRate = actionParams.getFeeRate();
      logger.info("充值类型：" + RechargeType.AMOUNT.getDesc());
      if (feeType == ServiceFeeType.RECHARGE.getCode()) {
        if (StringUtil.isEmpty(feeRate)) {
          throw new APIDockingException(APIDockingRetCodes.FEE_RATE_ERROR.getCode(),
              APIDockingRetCodes.FEE_RATE_ERROR.getDesc());
        }

        Map<String, Object> customRateConf = checkRateConf(customRateConfList, feeRate);
        if (customRateConf == null) {
          throw new APIDockingException(APIDockingRetCodes.FEE_RATE_NOT_FOUND.getCode(),
              APIDockingRetCodes.FEE_RATE_NOT_FOUND.getDesc());
        }
        logger.info("服务费类型：" + ServiceFeeType.RECHARGE.getDesc());
        String amountStr = ArithmeticUtil.mulStr(balanceAmount, feeRate);
        int compare = ArithmeticUtil.compareTod(amount,
            ArithmeticUtil.getScale(ArithmeticUtil.addStr2(amountStr, balanceAmount), 2));
        if (compare != 0) {
          throw new APIDockingException(APIDockingRetCodes.CHARGE_AMOUNT_ERROR.getCode(),
              APIDockingRetCodes.CHARGE_AMOUNT_ERROR.getDesc());
        }
        chargeDetail.setServiceFeeRate(feeRate);
        chargeDetail.setServiceFee(ArithmeticUtil.subStr2(amount, balanceAmount));
        chargeDetail.setAmount(balanceAmount);
        chargeDetail.setRechargeAmount(amount);

        Company company = companyService
            .getCompanyByUserId(Integer.parseInt(actionParams.getTransferCorpId()));
        if (CompanyType.SUBCONTRACT.getCode() == company.getCompanyType()) {
          chargeDetail.setForwardCommissionAmount(balanceAmount);
          //如果是转包服务公司，从 custom_company_rate_conf 表中获取转包服务公司费率
          List<CustomCompanyRateConf> customCompanyRateConfs = rateConfService
              .getConfByCustomKey(actionParams.getTransferCorpId());
          if (customCompanyRateConfs == null || customCompanyRateConfs.isEmpty()) {
            throw new APIDockingException(APIDockingRetCodes.FEE_RATE_NOT_FOUND.getCode(),
                APIDockingRetCodes.FEE_RATE_NOT_FOUND.getDesc());
          }
          if (customCompanyRateConfs.size() == 1) {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setGroupingUsed(false);
            BigDecimal b1 = new BigDecimal(ArithmeticUtil.getFormatDouble(balanceAmount));
            CustomCompanyRateConf customCompanyRateConf = customCompanyRateConfs.get(0);
            BigDecimal customRate = new BigDecimal(
                ArithmeticUtil.getFormatDouble(customCompanyRateConf.getCustomRate()));
            String realCompanyAmount = nf.format(
                b1.multiply(customRate).add(b1).setScale(2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue());
            chargeDetail.setRealCompanyAmount(realCompanyAmount);
          }
        }
      } else if (feeType == ServiceFeeType.ISSUE.getCode()) {
        logger.info("服务费类型：" + ServiceFeeType.ISSUE.getDesc());
        int compare = ArithmeticUtil.compareTod(balanceAmount, amount);
        if (compare != 0) {
          throw new APIDockingException(APIDockingRetCodes.CHARGE_AMOUNT_ERROR.getCode(),
              APIDockingRetCodes.CHARGE_AMOUNT_ERROR.getDesc());
        }
        chargeDetail.setServiceFeeRate("0");
        chargeDetail.setServiceFee("0");
        chargeDetail.setAmount(amount);
        chargeDetail.setRechargeAmount(amount);
        Company company = companyService
            .getCompanyByUserId(Integer.parseInt(actionParams.getTransferCorpId()));
        if (CompanyType.SUBCONTRACT.getCode() == company.getCompanyType()) {
          CustomCompanyRateConf customCompanyMinRate = rateConfService
              .getCustomCompanyMinRate(actionParams.getMerchantId(),
                  actionParams.getTransferCorpId());
          chargeDetail.setForwardCommissionAmount(ArithmeticUtil.divideStr(amount,
              ArithmeticUtil.addStr2("1", customCompanyMinRate.getCustomRate())));
        }
      } else if (feeType == ServiceFeeType.PERSON.getCode()) {
        logger.info("服务费类型：" + ServiceFeeType.ISSUE.getDesc());
        int compare = ArithmeticUtil.compareTod(balanceAmount, amount);
        if (compare != 0) {
          throw new APIDockingException(APIDockingRetCodes.CHARGE_AMOUNT_ERROR.getCode(),
              APIDockingRetCodes.CHARGE_AMOUNT_ERROR.getDesc());
        }
        chargeDetail.setServiceFeeRate("0");
        chargeDetail.setServiceFee("0");
        chargeDetail.setAmount(amount);
        chargeDetail.setRechargeAmount(amount);
        Company company = companyService
            .getCompanyByUserId(Integer.parseInt(actionParams.getTransferCorpId()));
        if (CompanyType.SUBCONTRACT.getCode() == company.getCompanyType()) {
          CustomCompanyRateConf customCompanyMinRate = rateConfService
              .getCustomCompanyMinRate(actionParams.getMerchantId(),
                  actionParams.getTransferCorpId());
          chargeDetail.setForwardCommissionAmount(ArithmeticUtil.divideStr(amount,
              ArithmeticUtil.addStr2("1", customCompanyMinRate.getCustomRate())));
        }
      } else {
        throw new APIDockingException(APIDockingRetCodes.FEE_TYPE_ERROR.getCode(),
            APIDockingRetCodes.FEE_TYPE_ERROR.getDesc());
      }
    } else {
      logger.info("充值类型：" + RechargeType.SERVICEAMOUNT.getDesc());
      int compare = ArithmeticUtil.compareTod(balanceAmount, "0");
      if (compare != 0) {
        throw new APIDockingException(APIDockingRetCodes.CHARGE_BALANCE_AMOUNT_ERROR.getCode(),
            APIDockingRetCodes.CHARGE_BALANCE_AMOUNT_ERROR.getDesc());
      }
      chargeDetail.setRechargeAmount(amount);
      chargeDetail.setServiceFeeRate("0");
      chargeDetail.setServiceFee("0");
      chargeDetail.setAmount("0");
    }
    String transferCorpId = actionParams.getTransferCorpId();
    Company company = companyService.getCompanyByUserId(Integer.parseInt(transferCorpId));
    Integer status = company.getStatus();
    if (status != null && status == 2) {
      throw new APIDockingException(APIDockingRetCodes.COMPANY_RECHARGE_DISABLED.getCode(),
          APIDockingRetCodes.COMPANY_RECHARGE_DISABLED.getDesc());
    }
    Map<String, Object> params = new HashMap<>(2);
    params.put("customkey", actionParams.getMerchantId());
    params.put("channelId", actionParams.getTransferCorpId());
    params.put("companyId", actionParams.getTransferCorpId());
    params.put("payType", actionParams.getPayType());
    Map<String, Object> rechargeInfo = customReceiveConfigService.getRechargeInfo(params);
    if (rechargeInfo == null) {
      logger.error("未找到充值配置，请求参数是{}", params);
      throw new APIDockingException(APIDockingRetCodes.RECHARGE_INFO_NOT_FOUND.getCode(),
          APIDockingRetCodes.RECHARGE_INFO_NOT_FOUND.getDesc());
    }
    String receiveAccount = actionParams.getReceiveAccount();
    String receiveAccountName = actionParams.getReceiveAccountName();
    String receiveMerchantName = actionParams.getReceiveMerchantName();
    if ((!receiveAccount.equals(rechargeInfo.get("inAccountNo")))
        || (!receiveAccountName.equals(rechargeInfo.get("inAccountBankName")))
        || (!receiveMerchantName.equals(rechargeInfo.get("inAccountName")))) {
      throw new APIDockingException(APIDockingRetCodes.RECHARGE_INFO_ERROR.getCode(),
          APIDockingRetCodes.RECHARGE_INFO_ERROR.getDesc());
    }
    chargeDetail.setAccountNo(actionParams.getAccount());
    chargeDetail.setAccountName(actionParams.getAccountName());
    chargeDetail.setRecCustomkey(actionParams.getTransferCorpId());
    chargeDetail.setPayType(Integer.parseInt(actionParams.getPayType()));
    chargeDetail.setInAccountNo(receiveAccount);
    chargeDetail.setInAccountName(receiveMerchantName);
    chargeDetail.setInAccountBankName(receiveAccountName);
    chargeDetail.setRechargeType(Integer.parseInt(actionParams.getRechargeType()));
    chargeDetail
        .setRechargeConfirmType(Integer.parseInt(rechargeInfo.get("rechargeConfirmType") + ""));
    chargeDetail.setInvoiceStatus(InvoiceOrderStatus.NO_TYPE.getCode());
    chargeDetail.setInvoiceAmount("0");
    chargeDetail.setInvoiceingAmount("0");
    chargeDetail.setUnInvoiceAmount(chargeDetail.getRechargeAmount());
    String orderNo = orderNoUtil.getChannelSerialno();
    chargeDetail.setOrderno(orderNo);
    chargeDetail.setCustomOrderNo(actionParams.getCustomOrderNo());
    chargeDetail.setNotifyUrl(actionParams.getNotifyUrl());
    chargeDetail.setOperatorName("api");
    chargeDetail.setRechargeFileNum(0);
    chargeDetail.setOrdername(CommonString.ORDERNAME);
    chargeDetail.setRemark(actionParams.getRemark());
    ChannelCustom channelCustom = channelCustomService
        .getCustomByCustomkey(actionParams.getMerchantId());
    if (channelCustom != null && channelCustom.getNeedRechargeLetter()) {
      chargeDetail.setRechargeLetterStatus(new Byte("1"));
    }
    channelHistoryService.addChannelHistory(chargeDetail);
    String chargeFile = actionParams.getChargeFile();
    if (!StringUtil.isEmpty(chargeFile)) {
      uploadRechargeFile(chargeFile, orderNo, "api");
    }

    //异步生成充值确认函
    String processId = (String) MDC.get(PROCESS);
    ThreadUtil.pdfThreadPool
        .execute(new AutoAenerateRechargeLetter(processId, actionParams.getMerchantId(),
            actionParams.getTransferCorpId(), new BigDecimal(amount), actionParams.getRemark(),
            companyService, channelCustomService, channelHistoryService, chargeDetail.getId(),
            bestSignConfig, templateSavetmpPath, fontsPath, userServiceFeignClient));

    MerchantRechargeServiceAttachment attachment = new MerchantRechargeServiceAttachment();
    attachment.setOrderNo(orderNo);
    return new ActionResult<>(attachment);
  }

  private Map<String, Object> checkRateConf(List<Map<String, Object>> customRateConfList,
      String feeRate) {
    for (Map<String, Object> objectMap : customRateConfList) {
      Object customRate = objectMap.get("customRate");
      int i = ArithmeticUtil.compareTod(customRate + "", ArithmeticUtil.mulStr(feeRate, "100"));
      if (i == 0) {
        return objectMap;
      }
    }
    return null;
  }

  private void uploadRechargeFile(String rechargeFile, String orderNo, String userName) {
    byte[] file = HexStringUtil.hexStringToBytes(rechargeFile);
    String picName = orderNo + ".jpg";
    String uploadFile = FtpTool
        .uploadFile(bestSignConfig.getFtpURL(), 21, "/rechargeFile/", picName,
            new ByteArrayInputStream(file), bestSignConfig.getUsername(),
            bestSignConfig.getPassword());
    if (!"error".equals(uploadFile)) {
      ChannelHistoryPic channelHistoryPic = new ChannelHistoryPic();
      channelHistoryPic.setOrderNo(orderNo);
      channelHistoryPic.setRechargeFile("/rechargeFile/" + picName);
      channelHistoryPic.setAddUser(userName);
      channelHistoryService.insertChannelHistoryPic(channelHistoryPic);
      channelHistoryService.updateChannelHistoryFileNumAddByOrderNo(orderNo);
    } else {
      logger.error("api上传打款凭证有误，订单号:", orderNo);
    }
  }

}
