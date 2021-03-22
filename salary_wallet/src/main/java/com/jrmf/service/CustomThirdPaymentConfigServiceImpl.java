package com.jrmf.service;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.CustomThirdPaymentConfigTypeEnum;
import com.jrmf.controller.constant.HistoryStatus;
import com.jrmf.controller.constant.InvoiceApprovalStatus;
import com.jrmf.controller.constant.InvoiceOrderStatus;
import com.jrmf.controller.constant.PathKeyTypeEnum;
import com.jrmf.controller.constant.TransferType;
import com.jrmf.controller.constant.sms.SmsTemplateCodeEnum;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.CustomThirdPaymentConfig;
import com.jrmf.domain.MerchantTransaction;
import com.jrmf.domain.OemConfig;
import com.jrmf.domain.Page;
import com.jrmf.domain.Parameter;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.QbInvoiceApprovalRecord;
import com.jrmf.domain.QbInvoiceRecord;
import com.jrmf.domain.SMSChannelConfig;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.persistence.ChannelHistoryDao;
import com.jrmf.persistence.CompanyDao;
import com.jrmf.persistence.CustomBalanceDao;
import com.jrmf.persistence.CustomThirdPaymentConfigDao;
import com.jrmf.persistence.ParameterDao;
import com.jrmf.taxsettlement.api.APIDockingManager;
import com.jrmf.taxsettlement.api.MerchantAPIDockingConfig;
import com.jrmf.taxsettlement.api.gateway.APIDockingGatewayDataUtil;
import com.jrmf.taxsettlement.api.gateway.restful.APIDefinitionConstants;
import com.jrmf.taxsettlement.api.security.sign.SignWorker;
import com.jrmf.taxsettlement.api.security.sign.SignWorkers;
import com.jrmf.taxsettlement.api.service.recharge.APIRechargeStatus;
import com.jrmf.taxsettlement.api.service.recharge.RechargeStatusNotice;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.ClientUtils;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.sms.channel.SMSChannel;
import com.jrmf.utils.sms.channel.SMSChannelFactory;
import com.jrmf.utils.threadpool.ThreadUtil;
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

/**
 * @author zhangzehui
 * @version 创建时间：2017年12月16日
 */
@Service
public class CustomThirdPaymentConfigServiceImpl implements CustomThirdPaymentConfigService {

  private static Logger logger = LoggerFactory.getLogger(CustomThirdPaymentConfigServiceImpl.class);

  @Autowired
  private CustomThirdPaymentConfigDao customThirdPaymentConfigDao;


  @Override
  public int deleteByPrimaryKey(Integer id) {
    return customThirdPaymentConfigDao.deleteByPrimaryKey(id);
  }

  @Override
  public int insert(CustomThirdPaymentConfig record) {
    return customThirdPaymentConfigDao.insert(record);
  }

  @Override
  public CustomThirdPaymentConfig selectByPrimaryKey(Integer id) {
    return customThirdPaymentConfigDao.selectByPrimaryKey(id);
  }

  @Override
  public int updateByPrimaryKey(CustomThirdPaymentConfig record) {
    return customThirdPaymentConfigDao.updateByPrimaryKey(record);
  }

  @Override
  public int updateByPrimaryKeySelective(CustomThirdPaymentConfig record) {
    return customThirdPaymentConfigDao.updateByPrimaryKeySelective(record);
  }

  @Override
  public int saveOrUpdateConfig(CustomThirdPaymentConfig record) {
    if (record.getId() != null && record.getId() > 0) {
      return this.updateByPrimaryKeySelective(record);
    } else {
      return this.insert(record);
    }
  }

  @Override
  public List<CustomThirdPaymentConfig> listAllByParam(String customName, String startTime,
      String endTime, String pathNo, String configType, String thirdMerchid) {
    return customThirdPaymentConfigDao
        .listAllByParam(customName, startTime, endTime, pathNo, configType, thirdMerchid);
  }

  @Override
  public PaymentConfig getConfigByCustomKeyAndTypeAndPathNo(String customKey,
      Integer configType, String companyId, String pathNo) {
    return customThirdPaymentConfigDao
        .getConfigByCustomKeyAndTypeAndPathNo(customKey, configType, companyId, pathNo);
  }

  @Override
  public PaymentConfig getConfigBySubcontract(String customKey, String companyId,
      String realCompanyId, PaymentConfig paymentConfigCustom) {
    //非转包下发
    if (realCompanyId.equals(companyId)) {
      //非转包下发情况下，平台和服务公司共用秘钥模式只获取当前通道的appid
      if (PathKeyTypeEnum.COMPANY.getCode() == paymentConfigCustom
          .getPathKeyType() || PathKeyTypeEnum.PLAYFORM.getCode() == paymentConfigCustom
          .getPathKeyType()) {

        PaymentConfig appIdConfig = this
            .getConfigByCustomKeyAndTypeAndPathNo(customKey,
                CustomThirdPaymentConfigTypeEnum.CUSTOM_MERCHANTID.getCode(), realCompanyId,
                paymentConfigCustom.getPathNo());

        if (appIdConfig != null) {
          paymentConfigCustom.setAppIdAyg(appIdConfig.getAppIdAyg());
        }

      } else if (PathKeyTypeEnum.CUSTOM.getCode() == paymentConfigCustom
          .getPathKeyType()) {
        paymentConfigCustom = this.getConfigByCustomKeyAndTypeAndPathNo(customKey,
            CustomThirdPaymentConfigTypeEnum.CUSTOM_KEY_AND_MERCHANTID.getCode(),
            realCompanyId,
            paymentConfigCustom.getPathNo());
      }
    } else { //转包下发
      Integer configType = CustomThirdPaymentConfigTypeEnum.REALCOMPANY_KEY.getCode();
      //转包下发情况下，平台和服务公司共用秘钥模式只获取实际下发通道的appid
      if (PathKeyTypeEnum.COMPANY.getCode() == paymentConfigCustom
          .getPathKeyType() || PathKeyTypeEnum.PLAYFORM.getCode() == paymentConfigCustom
          .getPathKeyType()) {
        //转包下发，非独立秘钥时获取appid配置
        PaymentConfig appIdConfig = this
            .getConfigByCustomKeyAndTypeAndPathNo(companyId,
                configType, realCompanyId,
                paymentConfigCustom.getPathNo());

        if (appIdConfig != null) {
          paymentConfigCustom.setAppIdAyg(appIdConfig.getAppIdAyg());
        }
      } else if (PathKeyTypeEnum.CUSTOM.getCode() == paymentConfigCustom
          .getPathKeyType()) { //转包下发，独立秘钥时直接获取公私钥信息及appid
        paymentConfigCustom = this
            .getConfigByCustomKeyAndTypeAndPathNo(companyId,
                configType, realCompanyId,
                paymentConfigCustom.getPathNo());
      }
    }

    return paymentConfigCustom;
  }

  @Override
  public CustomThirdPaymentConfig getByCustomKeyAndPathNo(String customKey, String pathNo,Integer id) {
    return customThirdPaymentConfigDao.getByCustomKeyAndPathNo(customKey,pathNo,id);
  }

  @Override
  public PaymentConfig getConfigByMerchId(String merchantId) {
    return customThirdPaymentConfigDao.getConfigByMerchId(merchantId);
  }
}
