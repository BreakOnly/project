package com.jrmf.persistence;

import com.jrmf.domain.CustomThirdPaymentConfig;
import com.jrmf.domain.PaymentConfig;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomThirdPaymentConfigDao {

  int deleteByPrimaryKey(Integer id);

  int insert(CustomThirdPaymentConfig record);

  int insertSelective(CustomThirdPaymentConfig record);

  CustomThirdPaymentConfig selectByPrimaryKey(Integer id);

  int updateByPrimaryKeySelective(CustomThirdPaymentConfig record);

  int updateByPrimaryKey(CustomThirdPaymentConfig record);

  PaymentConfig getConfigByCustomKeyAndTypeAndPathNo(String customKey, Integer configType,
      String companyId, String pathNo);

  List<CustomThirdPaymentConfig> listAllByParam(String customName, String startTime,
      String endTime, String pathNo, String configType,String thirdMerchid);

  CustomThirdPaymentConfig getByCustomKeyAndPathNo(String customKey, String pathNo,Integer id);

  PaymentConfig getConfigByMerchId(String merchantId);

}