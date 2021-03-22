package com.jrmf.service;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.common.RespResult;
import com.jrmf.controller.constant.CustomerFirmStatusEnum;
import com.jrmf.controller.constant.YuncrFeignClient;
import com.jrmf.domain.CustomerFirm;
import com.jrmf.domain.EconomicCategory;
import com.jrmf.domain.ProvinceCityArea;
import com.jrmf.domain.dto.YuncrPushCustomerFirmDTO;
import com.jrmf.persistence.CustomerFirmDao;
import com.jrmf.utils.RespCode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service("customerFirmService")
public class CustomerFirmServiceImpl implements CustomerFirmService {

  @Autowired
  private YuncrFeignClient yuncrFeignClient;

  @Autowired
  private CustomerFirmDao customerFirmDao;

  @Override
  public List<CustomerFirm> listCustomerFirm(Map<String, Object> map) {
    return customerFirmDao.listCustomerFirm(map);
  }

  @Override
  public CustomerFirm getCustomerFirmById(Integer id) {
    return customerFirmDao.getCustomerFirmById(id);
  }

  @Override
  public CustomerFirm getCustomerFirmByCustomKey(String customKey) {
    return customerFirmDao.getCustomerFirmByCustomkey(customKey);
  }

  @Override
  public void removeCustomerFirm(Integer id) {
    customerFirmDao.removeCustomerFirm(id);
  }

  @Override
  public void updateCustomerFirmStatus(Integer status, String statusDesc, Integer id) {
    customerFirmDao.updateCustomerFirmStatus(status, statusDesc, id);
  }

  @Override
  public List<EconomicCategory> listEconomicCategory() {
    return customerFirmDao.listEconomicCategory();
  }

  @Override
  public List<ProvinceCityArea> listProvince() {
    return customerFirmDao.listProvince();
  }

  @Override
  public List<ProvinceCityArea> listAreaByCityId(Integer cityId) {
    return customerFirmDao.listAreaByCityId(cityId);
  }

  @Override
  public List<ProvinceCityArea> listCityByProvinceId(Integer provinceId) {
    return customerFirmDao.listCityByProvinceId(provinceId);
  }

  @Override
  public List<Map<String, Object>> listEconomicCategoryByLevelAndValue(String level, String value) {
    return customerFirmDao.listEconomicCategoryByLevelAndValue(level, value);
  }

  @Override
  public Map<String, Object> configCustomerFirm(CustomerFirm customerFirm) {
    Map<String, Object> result = new HashMap<>(4);
    result.put(RespCode.RESP_STAT, RespCode.success);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

    if (customerFirm.getId() != null) {
      CustomerFirm f = customerFirmDao.getCustomerFirmByCustomkey(customerFirm.getCustomKey());
      if (f != null && !f.getId().equals(customerFirm.getId())) {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, "该商户已经注册，请选择其他商户");
        return result;
      }

      CustomerFirm firm = customerFirmDao.getCustomerFirmById(customerFirm.getId());
      if (firm != null && !firm.getCreditCode().equals(customerFirm.getCreditCode())) {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, "统一社会信用代码有误，请检查后重试");
        return result;
      }
      if (firm.getStatus() != CustomerFirmStatusEnum.FAIL.getCode()) {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, "非失败状态不可修改");
        return result;
      }
      customerFirmDao.updateCustomerFirm(customerFirm);
    } else {
      CustomerFirm f = customerFirmDao.getCustomerFirmByCustomkey(customerFirm.getCustomKey());
      if (f != null) {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, "该商户已经注册，请选择其他商户");
        return result;
      }

      CustomerFirm creditCode = customerFirmDao
          .getCustomerFirmByCreditCode(customerFirm.getCreditCode());
      if (creditCode != null) {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, "统一社会信用代码已存在,不能重复添加");
        return result;
      }
      customerFirm.setStatus(CustomerFirmStatusEnum.DOING.getCode());
      customerFirm.setStatusDesc(CustomerFirmStatusEnum.DOING.getDesc());
      customerFirmDao.insertCustomerFirm(customerFirm);
    }

    ResponseEntity<RespResult<Map<String, String>>> respResult;
    YuncrPushCustomerFirmDTO yuncrPushCustomerFirmDTO = this
        .getCustomerFirmTemplateReq(customerFirm);
    try {
      JSONObject jsonObject = (JSONObject) JSONObject.toJSON(yuncrPushCustomerFirmDTO);
      log.info("请求云控数据:{}", jsonObject);
      respResult = yuncrFeignClient.openFirm(jsonObject);
    } catch (Exception e) {
      log.error("请求云控失败：系统异常:", e);
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "调用云控服务异常, 请联系管理员");
      customerFirmDao.updateCustomerFirmStatus(CustomerFirmStatusEnum.FAIL.getCode(),
          "调用云控服务异常, 请联系管理员", customerFirm.getId());
      return result;
    }

    if (!"00000".equals(respResult.getBody().getCode())) {
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, "通道方返回：" + respResult.getBody().getMsg());
      customerFirmDao.updateCustomerFirmStatus(CustomerFirmStatusEnum.FAIL.getCode(),
          "通道方返回：" + respResult.getBody().getMsg(), customerFirm.getId());
      return result;
    }
    LinkedHashMap<String, String> data = (LinkedHashMap<String, String>) respResult.getBody()
        .getData();
    log.info("云控数据响应:{}", data);
    customerFirm.setFirmId(data.get("firmId"));
    customerFirm.setPlatsrl(data.get("platsrl"));
    customerFirm.setStatus(CustomerFirmStatusEnum.SUCCESS.getCode());
    customerFirm.setStatusDesc(CustomerFirmStatusEnum.SUCCESS.getDesc());
    customerFirmDao.updateCustomerFirm(customerFirm);
    return result;
  }


  private YuncrPushCustomerFirmDTO getCustomerFirmTemplateReq(CustomerFirm customerFirm) {
    YuncrPushCustomerFirmDTO yuncrPushCustomerFirmDTO = new YuncrPushCustomerFirmDTO();
    yuncrPushCustomerFirmDTO.setFirmName(customerFirm.getCustomName());
    yuncrPushCustomerFirmDTO.setZuzhjgdm(customerFirm.getCreditCode());
    yuncrPushCustomerFirmDTO.setFarnName(customerFirm.getLegalPerson());
    yuncrPushCustomerFirmDTO.setFarnxngb(customerFirm.getSex() + "");
    yuncrPushCustomerFirmDTO.setFarnzjlx("A");
    yuncrPushCustomerFirmDTO.setFarnzjno(customerFirm.getIdentityCard());
    yuncrPushCustomerFirmDTO.setFarnshji(customerFirm.getLegalPersonPhone());
    yuncrPushCustomerFirmDTO.setFarndzyx(customerFirm.getLegalPersonEmail());
    yuncrPushCustomerFirmDTO.setCategoryId(customerFirm.getCategoryId()+"");
    yuncrPushCustomerFirmDTO.setFarnhkprovId(customerFirm.getProvinceId() + "");
    yuncrPushCustomerFirmDTO.setFarnhkcityId(customerFirm.getCityId() + "");
    yuncrPushCustomerFirmDTO.setFarnhkregiId(customerFirm.getAreaId() + "");
    yuncrPushCustomerFirmDTO.setFarnjzdz(customerFirm.getAddress());
    yuncrPushCustomerFirmDTO.setLxrnName(customerFirm.getContactsName());
    yuncrPushCustomerFirmDTO.setLxrnshji(customerFirm.getContactsPhone());
    yuncrPushCustomerFirmDTO.setLxrndzyx(customerFirm.getContactsEmail());
    return yuncrPushCustomerFirmDTO;
  }
}
