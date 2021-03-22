package com.jrmf.persistence;

import com.jrmf.domain.CustomerFirm;
import com.jrmf.domain.EconomicCategory;
import com.jrmf.domain.ProvinceCityArea;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface CustomerFirmDao {

  List<CustomerFirm> listCustomerFirm(Map<String, Object> map);

  CustomerFirm getCustomerFirmById(Integer id);

  void updateCustomerFirm(CustomerFirm customerFirm);

  CustomerFirm getCustomerFirmByCreditCode(String creditCode);

  void insertCustomerFirm(CustomerFirm customerFirm);

  void updateCustomerFirmStatus(Integer status,
      String statusDesc, Integer id);

  void removeCustomerFirm(Integer id);

  List<EconomicCategory> listEconomicCategory();

  List<ProvinceCityArea> listProvince();

  CustomerFirm getCustomerFirmByCustomkey(String customKey);

  List<ProvinceCityArea> listCityByProvinceId(Integer provinceId);

  List<ProvinceCityArea> listAreaByCityId(Integer cityId);

  List<Map<String, Object>> listEconomicCategoryByLevelAndValue(String level, String value);
}
