package com.jrmf.controller.systemrole.merchant.invoice;

import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.PersonalIncomeTaxRate;
import com.jrmf.service.PersonalIncomeTaxRateService;
import com.jrmf.utils.RespCode;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: YJY
 * @date: 2020/7/15 11:38
 * @description:
 */
@RestController
@RequestMapping("/company/personal/tax/rate")
public class PersonalIncomeTaxRateController extends BaseController {


  @Autowired
  PersonalIncomeTaxRateService personalIncomeTaxRateService;

  /**
   * @Author YJY
   * @Description
   * @Date  2020/7/20
   * @Param [request, companyId 企业ID, pageNo 页数, pageSize 每页]
   * @return java.util.Map<java.lang.String,java.lang.Object>
   **/
  @PostMapping("selectListByCompanyId")
  public Map<String, Object> selectByCompanyId(HttpServletRequest request, Integer companyId,
      @RequestParam(defaultValue = "1") Integer pageNo,
      @RequestParam(defaultValue = "10") Integer pageSize) {

    if (!checkPermission(request)) {
      return returnFail(RespCode.error101, "暂无权限进行此操作");
    }
    return personalIncomeTaxRateService.selectByCompanyId(companyId, pageNo, pageSize);
  }

  /**
   * @Author YJY
   * @Description 添加或者修改个税配置
   * @Date  2020/7/22
   * @Param [request, personalIncomeTaxRate]
   * @return java.util.Map<java.lang.String,java.lang.Object>
   **/
  @PostMapping("insertOrUpdate")
  public Map<String, Object> insertOrUpdate(HttpServletRequest request,
      PersonalIncomeTaxRate personalIncomeTaxRate) {
    if (!checkPermission(request)) {
      return returnFail(RespCode.error101, "暂无权限进行此操作");
    }
    return personalIncomeTaxRateService.insert(personalIncomeTaxRate);
  }

  /**
   * @Author YJY
   * @Description 删除个税配置
   * @Date  2020/7/22
   * @Param [request, id]
   * @return java.util.Map<java.lang.String,java.lang.Object>
   **/
  @PostMapping("delete")
  public Map<String, Object> delete(HttpServletRequest request, Integer id) {
    if (!checkPermission(request)) {
      return returnFail(RespCode.error101, "暂无权限进行此操作");
    }
    return personalIncomeTaxRateService.delete(id);
  }

 /**
  * @Author YJY
  * @Description 查询发票记录明细 已弃用
  * @Date  2020/7/22
  * @Param [createDate, originalId, companyId]
  * @return java.util.Map<java.lang.String,java.lang.Object>
  **/
  @PostMapping("selectCommissionByCustom")
  public Map<String, Object> selectCommissionByCustom(String createDate,
      String originalId, Integer companyId) {

    return personalIncomeTaxRateService.selectCommissionByCustom(createDate, originalId, companyId);
  }

  /**
   * @Author YJY
   * @Description  根据流水号查询详细 按月分组
   * @Date  2020/7/20
   * @Param [invoiceSerialNo]
   * @return java.util.Map<java.lang.String,java.lang.Object>
   **/
  @PostMapping("selectByInvoiceSerialNo")
  public Map<String, Object> selectByInvoiceSerialNo(String invoiceSerialNo) {

    return personalIncomeTaxRateService.selectByInvoiceSerialNo(invoiceSerialNo);
  }

  /**
   * @Author YJY
   * @Description 检查是否是超管
   * @Date  2020/7/22
   * @Param [request]
   * @return java.lang.Boolean
   **/
  public Boolean checkPermission(HttpServletRequest request) {
    boolean flag = true;
    ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if (!CommonString.ROOT.equals(loginUser.getCustomkey()) && !CommonString.ROOT
        .equals(loginUser.getMasterCustom())) {
      flag = false;
    }
    return flag;
  }
}
