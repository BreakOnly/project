package com.jrmf.persistence;

import com.jrmf.domain.PersonalIncomeTaxRate;
import com.jrmf.domain.UserCommission;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author: YJY
 * @date: 2020/7/15 10:52
 * @description:
 */
@Mapper
public interface PersonalIncomeTaxRateDao {


  /**
   * @Description 查询服务公司配置的个税列表
   **/
  List<PersonalIncomeTaxRate> selectByCompanyId(Integer companyId,Integer excludeId);

  /**
   * @Description 添加公司个税配置
   **/
  int insert(PersonalIncomeTaxRate personalIncomeTaxRate);

  /**
   * @Description  删除个税配置
   **/
  int delete(Integer id);
  /**
   * @Description  根据ID删除个税配置
  **/
  int updateById(PersonalIncomeTaxRate personalIncomeTaxRate);
  /**
   * @Description  根据金额和公司ID 确定档位
   **/
  PersonalIncomeTaxRate selectByCompanyIdAndAmount(Integer amount,Integer companyId);
  /**
   * @Description  获取该商户月内发票数据
   **/
  List<UserCommission>  selectCommissionByCustom(String createDate,String originalId,Integer companyId);


  /**
   * @Description  通过流水号 查询发票明细
   **/
  List<UserCommission>  selectByInvoiceSerialNo(String invoiceSerialNo);

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
