package com.jrmf.service;

import com.github.pagehelper.PageInfo;
import com.jrmf.domain.settlement.*;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2020-03-16 10:18
 * @desc
 **/
public interface SettlementService {

    /**
     * 清结算数据统计--服务公司数据统计
     */
    List<StatisticCompany> statisticByCompany(Map<String, Object> map);

    /**
     * 清结算数据统计--服务公司数据统计--明细
     */
    List<StatisticCompanyDetail> statisticByCompanyDetail(Map<String, Object> map);
    /**
     * 清结算数据统计--业务经理数据统计
     */
    List<StatisticBusinessManager> statisticByBusinessManager(Map<String, Object> map);

    /**
     * 清结算数据统计--业务经理数据统计--明细
     */
    List<StatisticBusinessManagerDetail> statisticByBusinessManagerDetail(Map<String, Object> map);
    /**
     * 清结算数据统计--运营经理数据统计
     */
    List<StatisticOperationsManager> statisticByOperationsManager(Map<String, Object> map);

    /**
     * 清结算数据统计--运营经理数据统计--明细
     */
    List<StatisticOperationsManagerDetail> statisticByOperationsManagerDetail(Map<String, Object> map);
    /**
     * 清结算数据统计--运营经理数据统计--总金额
     */
    String statisticSumAmountByOperationsManager(Map<String, Object> map);

    /**
     * 清结算数据统计--运营经理数据统计--明细--总金额
     */
    String statisticSumAmountByOperationsManagerDetail(Map<String, Object> map);

    /**
     * 清结算数据统计--服务公司数据统计--总金额
     */
    String statisticSumAmountByCompany(Map<String, Object> map);

    /**
     * 清结算数据统计--服务公司数据统计--明细--总金额
     */
    String statisticSumAmountByCompanyDetail(Map<String, Object> map);

    /**
     * 清结算数据统计--业务经理数据统计--总金额
     */
    String statisticSumAmountByBusinessManager(Map<String, Object> map);

    /**
     * 清结算数据统计--业务经理数据统计--明细--总金额
     */
    String statisticSumAmountByBusinessManagerDetail(Map<String, Object> map);

    /**
     * 清结算数据统计--业务经理数据月度统计
     */
    List<MonthStatisticBusinessManager> monthStatisticByBusinessManager(Map<String, Object> map);

    /**
     * 清结算数据统计--运营经理数据月度统计
     */
    List<MonthStatisticOperationsManager> monthStatisticByOperationsManager(Map<String, Object> map);

    /**
     * 清结算数据统计--业务经理数据月度统计--总金额
     */
    String monthStatisticSumAmountByBusinessManager(Map<String, Object> map);

    /**
     * 清结算数据统计--运营经理数据月度统计--总金额
     */
    String monthStatisticSumAmountByOperationsManager(Map<String, Object> map);

    /**
     * 清结算数据统计--业务经理数据月度统计--明细
     */
    List<MonthStatisticBusinessManagerDetail> monthStatisticByBusinessManagerDetail(Map<String, Object> map);

    /**
     * 清结算数据统计--运营经理数据月度统计--明细
     */
    List<MonthStatisticOperationsManagerDetail> monthStatisticByOperationsManagerDetail(Map<String, Object> map);

    /**
     * 清结算数据统计--业务经理数据月度统计--明细--总金额
     */
    String monthStatisticSumAmountByBusinessManagerDetail(Map<String, Object> map);

    /**
     * 清结算数据统计--运营经理数据月度统计--明细--总金额
     */
    String monthStatisticSumAmountByOperationsManagerDetail(Map<String, Object> map);

    /**
     * 清结算数据统计--服务公司数据统计--月度统计
     */
    List<StatisticCompanyGroupByMonth> statisticByCompanyGroupByMonth(Map<String, Object> map);

    /**
     * 清结算数据统计--服务公司数据统计--月度统计--明细
     */
    List<StatisticCompanyGroupByMonthDetail> statisticByCompanyDetailGroupByMonth(Map<String, Object> map);

    /**
     * 清结算数据统计--服务公司数据统计--月度统计--总金额
     */
    String statisticSumAmountByCompanyMonth(Map<String, Object> map);

    /**
     * 清结算数据统计--服务公司数据统计--月度统计--明细--总金额
     */
    String statisticSumAmountByCompanyMonthDetail(Map<String, Object> map);
}
