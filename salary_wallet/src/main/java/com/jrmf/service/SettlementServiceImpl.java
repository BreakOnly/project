package com.jrmf.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.domain.settlement.*;
import com.jrmf.persistence.CompanyDao;
import com.jrmf.persistence.SettlementDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2020-03-16 10:18
 * @desc
 **/
@Service("settlementService")
public class SettlementServiceImpl implements SettlementService {

    @Autowired
    private SettlementDao settlementDao;

    /**
     * 清结算数据统计--服务公司数据统计
     */
    @Override
    public List<StatisticCompany> statisticByCompany(Map<String, Object> map) {
        return settlementDao.statisticByCompany(map);
    }

    /**
     * 清结算数据统计--服务公司数据统计--明细
     */
    @Override
    public List<StatisticCompanyDetail> statisticByCompanyDetail(Map<String, Object> map) {
        return settlementDao.statisticByCompanyDetail(map);
    }

    /**
     * 清结算数据统计--业务经理数据统计
     */
    @Override
    public List<StatisticBusinessManager> statisticByBusinessManager(Map<String, Object> map) {
        return settlementDao.statisticByBusinessManager(map);
    }

    /**
     * 清结算数据统计--业务经理数据统计--明细
     */
    @Override
    public List<StatisticBusinessManagerDetail> statisticByBusinessManagerDetail(Map<String, Object> map) {
        return settlementDao.statisticByBusinessManagerDetail(map);
    }
    /**
     * 清结算数据统计--运营经理数据统计
     */
    @Override
    public List<StatisticOperationsManager> statisticByOperationsManager(Map<String, Object> map) {

        return  settlementDao.statisticByOperationsManager(map);
    }
    /**
     * 清结算数据统计--运营经理数据统计--明细
     */
    @Override
    public List<StatisticOperationsManagerDetail> statisticByOperationsManagerDetail(
        Map<String, Object> map) {

        return settlementDao.statisticByOperationsManagerDetail(map);
    }

    /**
     * 清结算数据统计--运营经理数据统计--总金额
     */
    @Override
    public String statisticSumAmountByOperationsManager(Map<String, Object> map) {
        return settlementDao.statisticSumAmountByOperationsManager(map);
    }

    @Override
    public String statisticSumAmountByOperationsManagerDetail(Map<String, Object> map) {
        return settlementDao.statisticSumAmountByOperationsManagerDetail(map);
    }

    /**
     * 清结算数据统计--服务公司数据统计--总金额
     */
    @Override
    public String statisticSumAmountByCompany(Map<String, Object> map) {
        return settlementDao.statisticSumAmountByCompany(map);
    }

    /**
     * 清结算数据统计--服务公司数据统计--明细--总金额
     */
    @Override
    public String statisticSumAmountByCompanyDetail(Map<String, Object> map) {
        return settlementDao.statisticSumAmountByCompanyDetail(map);
    }

    /**
     * 清结算数据统计--业务经理数据统计--总金额
     */
    @Override
    public String statisticSumAmountByBusinessManager(Map<String, Object> map) {
        return settlementDao.statisticSumAmountByBusinessManager(map);
    }

    /**
     * 清结算数据统计--业务经理数据统计--明细--总金额
     */
    @Override
    public String statisticSumAmountByBusinessManagerDetail(Map<String, Object> map) {
        return settlementDao.statisticSumAmountByBusinessManagerDetail(map);
    }

    /**
     * 清结算数据统计--业务经理数据月度统计
     */
    @Override
    public List<MonthStatisticBusinessManager> monthStatisticByBusinessManager(Map<String, Object> map) {
        return settlementDao.monthStatisticByBusinessManager(map);
    }

    /**
     * 清结算数据统计--运营经理数据月度统计
     */
    @Override
    public List<MonthStatisticOperationsManager> monthStatisticByOperationsManager(
        Map<String, Object> map) {
        return settlementDao.monthStatisticByOperationsManager(map);
    }

    /**
     * 清结算数据统计--业务经理数据月度统计--总金额
     */
    @Override
    public String monthStatisticSumAmountByBusinessManager(Map<String, Object> map) {
        return settlementDao.monthStatisticSumAmountByBusinessManager(map);
    }

    /**
     * 清结算数据统计--运营经理数据月度统计--总金额
     */
    @Override
    public String monthStatisticSumAmountByOperationsManager(Map<String, Object> map) {
        return settlementDao.monthStatisticSumAmountByOperationsManager(map);
    }

    /**
     * 清结算数据统计--业务经理数据月度统计--明细
     */
    @Override
    public List<MonthStatisticBusinessManagerDetail> monthStatisticByBusinessManagerDetail(Map<String, Object> map) {
        return settlementDao.monthStatisticByBusinessManagerDetail(map);
    }

    /**
     * 清结算数据统计--运营经理数据月度统计--明细
     */
    @Override
    public List<MonthStatisticOperationsManagerDetail> monthStatisticByOperationsManagerDetail(
        Map<String, Object> map) {
        return settlementDao.monthStatisticByOperationsManagerDetail(map);
    }

    /**
     * 清结算数据统计--业务经理数据月度统计--明细--总金额
     */
    @Override
    public String monthStatisticSumAmountByBusinessManagerDetail(Map<String, Object> map) {
        return settlementDao.monthStatisticSumAmountByBusinessManagerDetail(map);
    }

    @Override
    public String monthStatisticSumAmountByOperationsManagerDetail(Map<String, Object> map) {
        return settlementDao.monthStatisticSumAmountByOperationsManagerDetail(map);
    }

    /**
     * 清结算数据统计--服务公司数据统计--月度统计
     */
    @Override
    public List<StatisticCompanyGroupByMonth> statisticByCompanyGroupByMonth(Map<String, Object> map) {
        return settlementDao.statisticByCompanyGroupByMonth(map);
    }

    /**
     * 清结算数据统计--服务公司数据统计--月度统计--明细
     */
    @Override
    public List<StatisticCompanyGroupByMonthDetail> statisticByCompanyDetailGroupByMonth(Map<String, Object> map) {
        return settlementDao.statisticByCompanyDetailGroupByMonth(map);
    }

    /**
     * 清结算数据统计--服务公司数据统计--月度统计--总金额
     */
    @Override
    public String statisticSumAmountByCompanyMonth(Map<String, Object> map) {
        return settlementDao.statisticSumAmountByCompanyMonth(map);
    }

    /**
     * 清结算数据统计--服务公司数据统计--月度统计--明细--总金额
     */
    @Override
    public String statisticSumAmountByCompanyMonthDetail(Map<String, Object> map) {
        return settlementDao.statisticSumAmountByCompanyMonthDetail(map);
    }

}
