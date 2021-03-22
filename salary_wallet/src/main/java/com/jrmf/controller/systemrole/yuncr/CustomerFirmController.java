package com.jrmf.controller.systemrole.yuncr;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.CustomerFirmStatusEnum;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.CustomerFirm;
import com.jrmf.domain.ProvinceCityArea;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.CustomerFirmService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/customer/firm")
public class CustomerFirmController extends BaseController {

  @Autowired
  private CustomerFirmService customerFirmService;

  @Autowired
  private ChannelCustomService channelCustomService;

  @PostMapping(value = "/list")
  public Map<String, Object> list(HttpServletRequest request,
      @RequestParam(required = false) String customName,
      @RequestParam(required = false) String legalPersonName,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime,
      @RequestParam(required = false) Integer pageNo,
      @RequestParam(required = false) Integer pageSize) {

    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if (!CommonString.ROOT.equals(customLogin.getCustomkey()) && !CommonString.ROOT
        .equals(customLogin.getMasterCustom()) && CustomType.COMPANY.getCode() != customLogin
        .getCustomType()) {
      return returnFail(RespCode.error101, "权限不足！");
    } else if (!CommonString.ROOT.equals(customLogin.getCustomkey()) && !CommonString.ROOT
        .equals(customLogin.getMasterCustom()) && CustomType.ROOT.getCode() == customLogin.getCustomType() && !StringUtil
        .isEmpty(customLogin.getMasterCustom())) {
      return returnFail(RespCode.error101, "权限不足！");
    }

    Map<String, Object> map = new HashMap<>(7);
    map.put("customName", customName);
    map.put("legalPersonName", legalPersonName);
    map.put("status", status);
    map.put("startTime", startTime);
    map.put("endTime", endTime);

    if (pageNo != null && pageSize != null) {
      PageHelper.startPage(pageNo, pageSize);
    }
    List<CustomerFirm> list = customerFirmService.listCustomerFirm(map);
    PageInfo page = new PageInfo(list);

    Map<String, Object> result = new HashMap<>(4);
    result.put("list", page.getList());
    result.put("total", page.getTotal());
    return returnSuccess(result);
  }

  @PostMapping(value = "/config")
  public Map<String, Object> config(HttpServletRequest request, CustomerFirm customerFirm) {

    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    // 限制子商户不可以创建发包商
    if (!CommonString.ROOT.equals(customLogin.getCustomkey()) && !CommonString.ROOT
        .equals(customLogin.getMasterCustom()) && CustomType.COMPANY.getCode() != customLogin
        .getCustomType()) {
      return returnFail(RespCode.error101, "权限不足！");
    } else if (!CommonString.ROOT.equals(customLogin.getCustomkey()) && !CommonString.ROOT
        .equals(customLogin.getMasterCustom()) && CustomType.ROOT.getCode() == customLogin.getCustomType() && !StringUtil
        .isEmpty(customLogin.getMasterCustom())) {
      return returnFail(RespCode.error101, "权限不足！");
    }

    ChannelCustom custom = channelCustomService.getCustomByCustomkey(customerFirm.getCustomKey());
    if (!customerFirm.getCustomName().equals(custom.getContractCompanyName())) {
      return returnFail(RespCode.error101, "商户名称与公司名称不符");
    }

    Map<String, Object> result;
    try {
      customerFirm.setAddUser(customLogin.getUsername());
      result = customerFirmService.configCustomerFirm(customerFirm);
    } catch (Exception e) {
      log.error("编辑发包商失败：", e);
      return returnFail(RespCode.error101, "系统异常");
    }

    return result;
  }

  @PostMapping(value = "/remove")
  public Map<String, Object> remove(@RequestParam Integer id) {
    CustomerFirm customerFirm = customerFirmService.getCustomerFirmById(id);
    if (customerFirm != null && customerFirm.getStatus() == CustomerFirmStatusEnum.FAIL.getCode()) {
      customerFirmService.removeCustomerFirm(id);
    }
    return returnSuccess();
  }

  @PostMapping(value = "/list/category")
  public Map<String, Object> listCategory() {
    List<Map<String, Object>> dataList;
    try {
      dataList = new ArrayList<>();
      List<Map<String, Object>> levelOne = customerFirmService
          .listEconomicCategoryByLevelAndValue("1", null);
      for (Map one : levelOne) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", one.get("name"));
        data.put("value", one.get("value"));
        List<Map<String, Object>> levelTwo = customerFirmService
            .listEconomicCategoryByLevelAndValue("2", one.get("value") + "-");
        List<Map<String, Object>> levelOneChildrenList = new ArrayList<>();
        for (Map two : levelTwo) {
          Map<String, Object> children = new HashMap<>();
          children.put("name", two.get("name"));
          children.put("value", two.get("value"));
          levelOneChildrenList.add(children);
          List<Map<String, Object>> levelThree = customerFirmService
              .listEconomicCategoryByLevelAndValue("3", two.get("value") + "-");
          List<Map<String, Object>> levelTwoChildrenList = new ArrayList<>();
          for (Map three : levelThree) {
            Map<String, Object> children1 = new HashMap<>();
            children1.put("name", three.get("name"));
            children1.put("value", three.get("value"));
            levelTwoChildrenList.add(children1);
          }
          children.put("children", levelTwoChildrenList);
        }
        data.put("children", levelOneChildrenList);
        dataList.add(data);
      }
    } catch (Exception e) {
      log.error("查询经济编号失败,{}", e);
      return returnFail(RespCode.error101, "系统异常");
    }

    return returnSuccess(dataList);
  }

  @PostMapping(value = "/list/province")
  public Map<String, Object> listProvince(
      @RequestParam(required = false) Integer provinceId,
      @RequestParam(required = false) Integer cityId) {
    List<Map<String, Object>> resultList = new ArrayList<>();
    if (provinceId == null && cityId == null) {
      List<ProvinceCityArea> provinceCityArea = customerFirmService.listProvince();
      if (provinceCityArea != null && !provinceCityArea.isEmpty()) {
        for (ProvinceCityArea provinceInfo : provinceCityArea) {
          Map<String, Object> resultMap = new HashMap<>();
          resultMap.put("provinceId", provinceInfo.getProvinceId());
          resultMap.put("provinceName", provinceInfo.getProvinceName());
          resultList.add(resultMap);
        }
      }
    } else if (cityId != null) {
      // 获取区县信息
      List<ProvinceCityArea> areaInfoList = customerFirmService.listAreaByCityId(cityId);
      if (areaInfoList != null && areaInfoList.size() > 0) {
        for (ProvinceCityArea areaInfo : areaInfoList) {
          Map<String, Object> resultMap = new HashMap<>();
          resultMap.put("provinceId", areaInfo.getProvinceId());
          resultMap.put("cityId", areaInfo.getCityId());
          resultMap.put("areaId", areaInfo.getAreaId());
          resultMap.put("areaName", areaInfo.getAreaName());
          resultList.add(resultMap);
        }
      }
    } else {
      //获取市信息
      List<ProvinceCityArea> cityInfoList = customerFirmService.listCityByProvinceId(provinceId);
      if (cityInfoList != null && !cityInfoList.isEmpty()) {
        for (ProvinceCityArea cityInfo : cityInfoList) {
          Map<String, Object> resultMap = new HashMap<>();
          resultMap.put("provinceId", cityInfo.getProvinceId());
          resultMap.put("cityId", cityInfo.getCityId());
          resultMap.put("cityName", cityInfo.getCityName());
          resultList.add(resultMap);
        }
      }
    }
    return returnSuccess(resultList);
  }

}
