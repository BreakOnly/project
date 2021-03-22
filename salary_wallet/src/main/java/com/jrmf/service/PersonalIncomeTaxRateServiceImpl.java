package com.jrmf.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.domain.PersonalIncomeTaxRate;
import com.jrmf.domain.UserCommission;
import com.jrmf.persistence.PersonalIncomeTaxRateDao;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author: YJY
 * @date: 2020/7/15 11:27
 * @description: 个税配置接口实现类
 */
@Service
public class PersonalIncomeTaxRateServiceImpl implements PersonalIncomeTaxRateService {

  @Autowired
  PersonalIncomeTaxRateDao personalIncomeTaxRateDao;

  /**
   * @Description 查询服务公司配置的个税列表
   **/
  @Override
  public Map<String, Object> selectByCompanyId(Integer companyId, Integer pageNo,
      Integer pageSize) {

    List<PersonalIncomeTaxRate> list = new ArrayList<>();
    try {

      if (ObjectUtils.isEmpty(companyId)) {
        return returnMsg(RespCode.error101, "请检查您的参数", null);
      }
      PageHelper.startPage(pageNo, pageSize);
      PageInfo<PersonalIncomeTaxRate> pageInfo = new PageInfo(
          personalIncomeTaxRateDao.selectByCompanyId(companyId, null));

      list = pageInfo.getList();
      Map<String, Object> result = returnMsg(RespCode.success, "请求成功!", pageInfo.getList());
      result.put("total", pageInfo.getTotal());
      result.put("result", list);
      return result;

    } catch (Exception e) {
      return returnMsg(RespCode.error107, RespCode.CONNECTION_ERROR, null);
    }
  }

  /**
   * @Description 添加公司个税配置
   **/
  @Override
  public Map<String, Object> insert(PersonalIncomeTaxRate personalIncomeTaxRate) {
    try {
      if (ObjectUtils.isEmpty(personalIncomeTaxRate)
          || ObjectUtils.isEmpty(personalIncomeTaxRate.getAmountEnd())
          || ObjectUtils.isEmpty(personalIncomeTaxRate.getAmountStart())
          || ObjectUtils.isEmpty(personalIncomeTaxRate.getOperator())
          || ObjectUtils.isEmpty(personalIncomeTaxRate.getTaxRate())) {

        return returnMsg(RespCode.error101, "请检查您的参数,部分参数为空", null);
      }
      if (!StringUtil.isNumber(personalIncomeTaxRate.getAmountStart()) ||
          !StringUtil.isNumber(personalIncomeTaxRate.getAmountEnd())) {
        return returnMsg(RespCode.error124, "请输入正确的金额", null);
      }

      if (ArithmeticUtil
          .compareTod(personalIncomeTaxRate.getAmountEnd(), personalIncomeTaxRate.getAmountStart())
          == -1 ||
          ArithmeticUtil.compareTod(personalIncomeTaxRate.getAmountEnd(),
              personalIncomeTaxRate.getAmountStart()) == 0) {
        return returnMsg(RespCode.error124, "结束金额不能小于等于起始金额", null);
      }

      //根据ID 判断是修改还是新增
      Integer excludeId = null;
      if(StringUtils.isNotEmpty(personalIncomeTaxRate.getTaxRate())){

        Double taxRate= Double.parseDouble(personalIncomeTaxRate.getTaxRate())/100;

        personalIncomeTaxRate.setTaxRate(taxRate+"");

      }
      if (!ObjectUtils.isEmpty(personalIncomeTaxRate.getId())) {
        excludeId = personalIncomeTaxRate.getId();
      }
      List<PersonalIncomeTaxRate> list = personalIncomeTaxRateDao
          .selectByCompanyId(personalIncomeTaxRate.getCompanyId(), excludeId);
      for (PersonalIncomeTaxRate per : list) {

        if (ArithmeticUtil.compareTod(per.getAmountStart(), personalIncomeTaxRate.getAmountStart())
            == 0 &&
            ArithmeticUtil.compareTod(per.getAmountEnd(), personalIncomeTaxRate.getAmountEnd())
                == 0) {
          return returnMsg(RespCode.error107, "档位金额已存在，请重新输入金额！", null);
        }

        if (judgeOverlap(per.getAmountStart(), per.getAmountEnd(),
            personalIncomeTaxRate.getAmountStart(), personalIncomeTaxRate.getAmountEnd())) {
          return returnMsg(RespCode.error107, "档位金额存在交叉，请重新输入金额！", null);
        }

      }

      Boolean flag = false;
      //添加
      if (ObjectUtils.isEmpty(personalIncomeTaxRate.getId())) {
        flag = personalIncomeTaxRateDao.insert(personalIncomeTaxRate) > 0 ? true : false;
      } else {
        flag = personalIncomeTaxRateDao.updateById(personalIncomeTaxRate) > 0 ? true : false;
      }

      //判断是否操作成功
      if (flag) {
        return returnMsg(RespCode.success, "操作成功!", null);
      }
      return returnMsg(RespCode.error107, "操作失败 请刷新重试", null);
    } catch (Exception e) {
      return returnMsg(RespCode.error107, RespCode.CONNECTION_ERROR, null);
    }
  }

  /**
   * @Description 删除个税配置
   **/
  @Override
  public Map<String, Object> delete(Integer id) {
    try {
      if (ObjectUtils.isEmpty(id)) {
        return returnMsg(RespCode.error101, "参数不能为空!", null);
      }

      boolean flag = personalIncomeTaxRateDao.delete(id) > 0 ? true : false;
      if (flag) {

        return returnMsg(RespCode.success, "删除成功!", null);
      }

      return returnMsg(RespCode.DELETE_FAIL, "删除失败 请稍后重试", null);
    } catch (Exception e) {
      return returnMsg(RespCode.error107, RespCode.CONNECTION_ERROR, null);
    }
  }

  /**
   * @Description 获取该商户月内发票总计数据
   **/
  @Override
  public Map<String, Object> selectCommissionByCustom(String createDate, String originalId,
      Integer companyId) {

    HashMap<String, Object> bean = new HashMap();

    //判断参数
    if (ObjectUtils.isEmpty(createDate) || ObjectUtils.isEmpty(originalId) || ObjectUtils
        .isEmpty(companyId)) {

      return returnMsg(RespCode.error101, "部分参数为空!", null);
    }

    List<UserCommission> listUserCommission = personalIncomeTaxRateDao
        .selectCommissionByCustom(createDate, originalId, companyId);

    //返回空数据
    if (CollectionUtils.isEmpty(listUserCommission)) {

      return returnMsg(RespCode.QUERY_FAIL, "数据为空!", null);
    }
    //实发金额
    Double total = 0d;
    //服务费率
    String calculationRates = listUserCommission.get(0).getCalculationRates();
    //个税税额
    Double individualTax = 0d;
    //补充个税税额
    Double individualBackTax = 0d;
    //新增了一个税率字段
    String personalRates = listUserCommission.get(0).getTaxRate();
    ;

    for (UserCommission userCommission : listUserCommission) {

      //总金额
      total += Double.parseDouble(userCommission.getAmount());
      //个税税额
      individualTax += Double.parseDouble(userCommission.getIndividualTax());
      //补缴个税税额
      individualBackTax += Double.parseDouble(userCommission.getIndividualBackTax());

      //服务费率
      if (StringUtils.isNotBlank(userCommission.getCalculationRates())) {

        if (!calculationRates.contains(userCommission.getCalculationRates())) {

          calculationRates += "," + userCommission.getCalculationRates();
        }

      }
      //个税税率
      if (StringUtils.isNotBlank(userCommission.getTaxRate())) {

        if (!calculationRates.contains(userCommission.getTaxRate())) {

          personalRates += "," + userCommission.getTaxRate();
        }

      }

    }

    bean.put("total", total);
    bean.put("calculationRates", calculationRates);
    bean.put("individualTax", individualTax);
    bean.put("individualBackTax", individualBackTax);
    bean.put("personalRates", personalRates);
    List list = new ArrayList();
    list.add(bean);
    return returnMsg(RespCode.success, "请求成功!", list);
  }

  /**
   * @Author YJY
   * @Description 根据流水号 查询发票明细
   * @Date  2020/7/22
   * @Param [invoiceSerialNo]
   * @return java.util.Map<java.lang.String,java.lang.Object>
   **/
  @Override
  public Map<String, Object> selectByInvoiceSerialNo(String invoiceSerialNo) {

    //判断参数
    if (ObjectUtils.isEmpty(invoiceSerialNo)) {

      return returnMsg(RespCode.error101, "流水号不能为空!", null);
    }

    List list = new ArrayList();
    list.add(invoiceSerialNo);
    //费率数据
    List<UserCommission> listRate = personalIncomeTaxRateDao
        .selectByListInvoiceSerialNo(list);

    //统计数据
    List<UserCommission> listSum = personalIncomeTaxRateDao
        .selectSumByListInvoiceSerialNo(list);

    List backList = new ArrayList();backList.add(invoiceSerialNo);
    //查询补充个税金额
    List<UserCommission> listBackRate = personalIncomeTaxRateDao
      .findIndividualBackTax(backList);

    //返回空数据
    if (CollectionUtils.isEmpty(listRate)) {

      return returnMsg(RespCode.QUERY_FAIL, "数据为空!", null);
    }

    List<HashMap<String, String>> listData = integrationData(listRate, listSum,listBackRate);

    return returnMsg(RespCode.success, "请求成功!", listData);
  }

  /**
   * @Author YJY
   * @Description 根据流水号 查询服务费率 个税费率
   * @Date  2020/7/22
   * @Param [list]
   * @return java.util.List<com.jrmf.domain.UserCommission>
   **/
  @Override
  public List<UserCommission> selectByListInvoiceSerialNo(List list) {
    return personalIncomeTaxRateDao.selectByListInvoiceSerialNo(list);
  }

  @Override
  public List<PersonalIncomeTaxRate> getCompanyTaxRateList(String companyId) {
    return personalIncomeTaxRateDao.getCompanyTaxRateList(companyId);
  }

  /**
   * @Author YJY
   * @Description 查询补充个税税额
   * @Date  2020/7/28
   * @Param [list]
   * @return java.util.List<com.jrmf.domain.UserCommission>
   **/
  @Override
  public List<UserCommission> findIndividualBackTax(List list) {
    return personalIncomeTaxRateDao.findIndividualBackTax(list);
  }


  /**
   * @Author YJY
   * @Description 根据流水号 统计服务费 个税费相关总和
   * @Date  2020/7/22
   * @Param [list]
   * @return java.util.List<com.jrmf.domain.UserCommission>
   **/
  @Override
  public List<UserCommission> selectSumByListInvoiceSerialNo(List list) {
    return personalIncomeTaxRateDao.selectSumByListInvoiceSerialNo(list);
  }


  /**
   * @return java.util.List<java.lang.Object>
   * @Author YJY
   * @Description 按照流水号 月份 统计
   * @Date 2020/7/20
   * @Param [listUserCommission]
   **/
  public List<HashMap<String, String>> integrationData(List<UserCommission> listUserCommissionRate,
      List<UserCommission> listUserCommissionSum,List<UserCommission> listBackRate) {

    List<HashMap<String, String>> mapList = new ArrayList<>();
    if(CollectionUtils.isNotEmpty(listUserCommissionSum)) {
      //sum数据
      for (UserCommission sum : listUserCommissionSum) {
        sum.setIndividualBackTax("0");
        if(CollectionUtils.isNotEmpty(listUserCommissionRate)) {
          //计算费率
          for (UserCommission rate : listUserCommissionRate) {

            //服务费率 不为空 日期相等 不包含
            if (StringUtils.isNotBlank(rate.getCalculationRates())
                && sum.getAccountDate().equals(rate.getAccountDate())
                && (!sum.getCalculationRates().contains(rate.getCalculationRates()))
            ) {

              sum.setCalculationRates(
                  sum.getCalculationRates() + "%," + rate.getCalculationRates());


            }
            //个税税率 不为空 日期相等 不包含
            if (StringUtils.isNotBlank(rate.getTaxRate())
                && sum.getAccountDate().equals(rate.getAccountDate())
                && (!sum.getTaxRate().contains(rate.getTaxRate()))
            ) {

              sum.setTaxRate(sum.getTaxRate() + "%," + rate.getTaxRate());

            }
          }
        }
        if(CollectionUtils.isNotEmpty(listBackRate)) {
        //补充个税
        for (UserCommission back : listBackRate) {

          if (sum.getAccountDate().equals(back.getAccountDate())) {

            sum.setIndividualBackTax(back.getIndividualBackTax());
          }
        }
        }
        if (!sum.getTaxRate().endsWith("%")) {

          sum.setTaxRate(sum.getTaxRate() + "%");
        }

        if (!sum.getCalculationRates().endsWith("%")) {

          sum.setCalculationRates(sum.getCalculationRates() + "%");
        }

      }

      for (UserCommission sum : listUserCommissionSum) {

        HashMap hashMap = new HashMap();
        hashMap.put("date", sum.getAccountDate());
        hashMap.put("total", sum.getAmount());
        hashMap.put("personalRates", sum.getTaxRate());
        hashMap.put("individualBackTax", sum.getIndividualBackTax());
        hashMap.put("calculationRates", sum.getCalculationRates());
        hashMap.put("individualTax", sum.getIndividualTax());
        mapList.add(hashMap);
      }
    }
    return mapList;
  }


  /**
   * 返回结果
   */
  public Map<String, Object> returnMsg(int state, String message, Object data) {
    Map<String, Object> result = new HashMap<>();
    result.put(RespCode.RESP_STAT, state);
    if (StringUtil.isEmpty(message)) {
      message = "error";
    }
    if (!ObjectUtils.isEmpty(data)) {

      result.put(RespCode.RESULT, data);
    }
    result.put(RespCode.RESP_MSG, message);
    return result;
  }

  /**
   * 判断金额是否重叠
   */
  private static boolean judgeOverlap(String start1, String end1, String start2, String end2) {

    return Double.parseDouble(start2) < Double.parseDouble(end1)
        && Double.parseDouble(end2) > Double.parseDouble(start1);

  }

}
