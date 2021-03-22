package com.jrmf.service;

import com.jrmf.domain.CustomThirdPaymentConfig;
import com.jrmf.domain.PaymentConfig;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

public interface CustomThirdPaymentConfigService {

  int deleteByPrimaryKey(Integer id);

  int insert(CustomThirdPaymentConfig record);

  CustomThirdPaymentConfig selectByPrimaryKey(Integer id);

  int updateByPrimaryKey(CustomThirdPaymentConfig record);

  int updateByPrimaryKeySelective(CustomThirdPaymentConfig record);

  int saveOrUpdateConfig(CustomThirdPaymentConfig record);

  List<CustomThirdPaymentConfig> listAllByParam(String customName, String startTime,
      String endTime, String pathNo, String configType,String thirdMerchid);

  PaymentConfig getConfigByCustomKeyAndTypeAndPathNo(String customKey, Integer configType,
      String companyId, String pathNo);

  PaymentConfig getConfigBySubcontract(String customKey, String companyId,
      String realCompanyId, PaymentConfig paymentConfigCustom);

  CustomThirdPaymentConfig getByCustomKeyAndPathNo(String customKey, String pathNo,Integer id);

  PaymentConfig getConfigByMerchId(String merchantId);
}