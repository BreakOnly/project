package com.jrmf.service;

import com.jrmf.domain.CustomerFirm;
import com.jrmf.domain.EconomicCategory;
import com.jrmf.domain.ProvinceCityArea;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public interface CustomerFirmService {

  List<CustomerFirm> listCustomerFirm(Map<String, Object> map);

  Map<String, Object> configCustomerFirm(CustomerFirm customerFirm);

  CustomerFirm getCustomerFirmById(Integer id);

  CustomerFirm getCustomerFirmByCustomKey(String customKey);

  void removeCustomerFirm(Integer id);

  void updateCustomerFirmStatus(Integer status, String statusDesc, Integer id);

  List<EconomicCategory> listEconomicCategory();

  List<ProvinceCityArea> listProvince();

  List<ProvinceCityArea> listAreaByCityId(Integer cityId);

  List<ProvinceCityArea> listCityByProvinceId(Integer provinceId);

  List<Map<String, Object>> listEconomicCategoryByLevelAndValue(String level, String value);
}
