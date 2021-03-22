package com.jrmf.service;
import com.jrmf.domain.PersonalIncomeTaxRate;
import com.jrmf.domain.UserCommission;
import java.util.List;
import java.util.Map;

/**
 * @author: YJY
 * @date: 2020/7/15 11:24
 * @description: 个税配置接口
 */
public interface PersonalIncomeTaxRateService {


  /**
   * @Description 查询服务公司配置的个税列表
   *
   * @return*/
  Map<String, Object> selectByCompanyId(Integer companyId,Integer pageNo,Integer pageSize);

  /**
   * @Description 添加公司个税配置
   **/
  Map<String, Object> insert(PersonalIncomeTaxRate personalIncomeTaxRate);

  /**
   * @Description  删除个税配置
   **/
  Map<String, Object> delete(Integer id);

  /**
   * @Description 获取该商户月内发票总计数据
   **/
  Map<String, Object> selectCommissionByCustom(String createDate, String originalId,Integer companyId);


  /**
   * @Description  通过流水号 查询发票明细
   **/
  Map<String, Object>  selectByInvoiceSerialNo(String invoiceSerialNo);

  /**
   * @Description  通过批量流水号 查询发票明细
   **/
  List<UserCommission>  selectByListInvoiceSerialNo(List list);
  /**
   * @Description  通过批量流水号 查询发票钱数总计
   **/
  List<UserCommission>  selectSumByListInvoiceSerialNo(List list);
  List<PersonalIncomeTaxRate> getCompanyTaxRateList(String companyId);

  /**
   * @Description 查询补充个税金额
   **/
  List<UserCommission> findIndividualBackTax(List list);
}
