package com.jrmf.controller.settlement;

import com.github.pagehelper.Constant;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.settlement.*;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.SettlementService;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.RespCode;
import java.util.ArrayList;
import java.util.Set;

import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2020-03-16 10:01
 * @desc
 **/
@RestController
@RequestMapping("settlement")
public class SettlementController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(SettlementController.class);

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private ChannelCustomService channelCustomService;

    /**
     * 清结算数据统计--服务公司数据统计
     * type  1 服务公司
     *       2 平台
     */
    @RequestMapping(value = "company/statistics", method = RequestMethod.POST)
    public Map<String, Object> companyStatistics(HttpServletRequest request,String businessPlatformId,
                                                 int type, String startTime, String endTime,
                                                 @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                 @RequestParam(required = false, defaultValue = "10") int pageSize) {

        Map<String, Object> stringObjectMap = checkAndGetPlatformName(request, businessPlatformId);
        if (((Integer) stringObjectMap.get(RespCode.RESP_STAT)) != RespCode.success) {
            return stringObjectMap;
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("type",type);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("businessPlatform",(String) stringObjectMap.get("businessPlatform"));
        PageHelper.startPage(pageNo, pageSize);
        List<StatisticCompany> list = settlementService.statisticByCompany(map);
        PageInfo<StatisticCompany> page = new PageInfo(list);
        Map<String, Object> resultMap = new HashMap<>(4);

        resultMap.put("total", page.getTotal());
        resultMap.put("list", page.getList());
        String amount = settlementService.statisticSumAmountByCompany(map);
        resultMap.put("amount",amount);
        return returnSuccess(resultMap);
    }

    /**
     * 清结算数据统计--服务公司数据统计--明细
     * type  1 服务公司
     *       2 平台
     */
    @RequestMapping(value = "company/statistics/detail", method = RequestMethod.POST)
    public Map<String, Object> companyStatisticsDetail(HttpServletRequest request,String businessPlatformId,
                                                       int type,String key,String startTime,String endTime,
                                                       @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                       @RequestParam(required = false, defaultValue = "10") int pageSize) {

        Map<String, Object> stringObjectMap = checkAndGetPlatformName(request, businessPlatformId);
        if (((Integer) stringObjectMap.get(RespCode.RESP_STAT)) != RespCode.success) {
            return stringObjectMap;
        }
        Map<String, Object> map = new HashMap<>(8);
        map.put("type",type);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("key",key);
        map.put("businessPlatform",(String) stringObjectMap.get("businessPlatform"));
        PageHelper.startPage(pageNo, pageSize);
        List<StatisticCompanyDetail> list = settlementService.statisticByCompanyDetail(map);
        PageInfo<StatisticCompanyDetail> page = new PageInfo(list);
        Map<String, Object> resultMap = new HashMap<>(4);
        resultMap.put("total", page.getTotal());
        resultMap.put("list", page.getList());
        String amount = settlementService.statisticSumAmountByCompanyDetail(map);
        resultMap.put("amount",amount);
        return returnSuccess(resultMap);
    }

    /**
     * 清结算数据统计--服务公司数据统计
     * type  1 服务公司
     *       2 平台
     */
    @RequestMapping(value = "company/statistics/month", method = RequestMethod.POST)
    public Map<String, Object> companyStatisticsByMonth(HttpServletRequest request,String businessPlatformId,
                                                        int type,String startTime,String endTime,
                                                 @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                 @RequestParam(required = false, defaultValue = "10") int pageSize) {

        Map<String, Object> stringObjectMap = checkAndGetPlatformName(request, businessPlatformId);
        if (((Integer) stringObjectMap.get(RespCode.RESP_STAT)) != RespCode.success) {
            return stringObjectMap;
        }
        Map<String, Object> map = new HashMap<>(8);
        map.put("type",type);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("businessPlatform",(String) stringObjectMap.get("businessPlatform"));
        PageHelper.startPage(pageNo, pageSize);
        List<StatisticCompanyGroupByMonth> list = settlementService.statisticByCompanyGroupByMonth(map);
        PageInfo<StatisticCompanyGroupByMonth> page = new PageInfo(list);
        Map<String, Object> resultMap = new HashMap<>(4);

        resultMap.put("total", page.getTotal());
        resultMap.put("list", page.getList());
        String amount = settlementService.statisticSumAmountByCompanyMonth(map);
        resultMap.put("amount",amount);
        return returnSuccess(resultMap);
    }
    /**
     * 清结算数据统计--服务公司数据统计--明细
     * type  1 服务公司
     *       2 平台
     */
    @RequestMapping(value = "company/statistics/detail/month", method = RequestMethod.POST)
    public Map<String, Object> companyStatisticsDetailByMonth(HttpServletRequest request,String businessPlatformId,
                                                              int type,String key,String startTime,String endTime,
                                                       @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                       @RequestParam(required = false, defaultValue = "10") int pageSize) {
        Map<String, Object> stringObjectMap = checkAndGetPlatformName(request, businessPlatformId);
        if (((Integer) stringObjectMap.get(RespCode.RESP_STAT)) != RespCode.success) {
            return stringObjectMap;
        }
        Map<String, Object> map = new HashMap<>(8);
        map.put("type",type);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("key",key);
        map.put("businessPlatform",(String) stringObjectMap.get("businessPlatform"));
        PageHelper.startPage(pageNo, pageSize);
        List<StatisticCompanyGroupByMonthDetail> list = settlementService.statisticByCompanyDetailGroupByMonth(map);
        PageInfo<StatisticCompanyGroupByMonthDetail> page = new PageInfo(list);
        Map<String, Object> resultMap = new HashMap<>(4);
        resultMap.put("total", page.getTotal());
        resultMap.put("list", page.getList());
        String amount = settlementService.statisticSumAmountByCompanyMonthDetail(map);
        resultMap.put("amount",amount);
        return returnSuccess(resultMap);
    }

    /**
     * 清结算数据统计--业务经理数据统计
     */
    @RequestMapping(value = "businessmanager/statistics", method = RequestMethod.POST)
    public Map<String, Object> businessManagerStatistics(HttpServletRequest request,String businessManager, String startTime,
                                                         String endTime, String businessPlatformId,
                                                         @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                         @RequestParam(required = false, defaultValue = "10") int pageSize) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //判断是否有平台ID
        if (StringUtil.isEmpty(businessPlatformId)) {
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
            return result;
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        if (platformMap == null || platformMap.size() == 0){
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
            return result;
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("businessManager",businessManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        PageHelper.startPage(pageNo, pageSize);
        List<StatisticBusinessManager> list = settlementService.statisticByBusinessManager(map);
        PageInfo<StatisticBusinessManager> page = new PageInfo(list);
        Map<String, Object> resultMap = new HashMap<>(4);

        resultMap.put("total", page.getTotal());
        resultMap.put("list", page.getList());

        String amount = settlementService.statisticSumAmountByBusinessManager(map);
        resultMap.put("amount",amount);
        return returnSuccess(resultMap);
    }
    /**
     * 清结算数据统计--业务经理数据统计--明细
     */
    @RequestMapping(value = "businessmanager/statistics/detail", method = RequestMethod.POST)
    public Map<String, Object> businessManagerStatisticsDetail(HttpServletRequest request,String businessManager,String startTime,
                                                               String endTime,String businessPlatformId,
                                                 @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                 @RequestParam(required = false, defaultValue = "10") int pageSize) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //判断是否有平台ID
        if (StringUtil.isEmpty(businessPlatformId)) {
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
            return result;
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        if (platformMap == null || platformMap.size() == 0){
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
            return result;
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("businessManager",businessManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        PageHelper.startPage(pageNo, pageSize);
        List<StatisticBusinessManagerDetail> list = settlementService.statisticByBusinessManagerDetail(map);
        PageInfo<StatisticBusinessManagerDetail> page = new PageInfo(list);
        Map<String, Object> resultMap = new HashMap<>(4);

        resultMap.put("total", page.getTotal());
        resultMap.put("list", page.getList());

        String amount = settlementService.statisticSumAmountByBusinessManagerDetail(map);
        resultMap.put("amount",amount);
        return returnSuccess(resultMap);
    }


    /**
     * 清结算数据统计--业务经理月度数据统计
     */
    @RequestMapping(value = "businessmanager/monthStatistics", method = RequestMethod.POST)
    public Map<String, Object> businessManagerMonthStatistics(HttpServletRequest request,String businessManager,String startTime,
                                                              String endTime,String businessPlatformId,
                                                         @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                         @RequestParam(required = false, defaultValue = "10") int pageSize) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //判断是否有平台ID
        if (StringUtil.isEmpty(businessPlatformId)) {
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
            return result;
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        if (platformMap == null || platformMap.size() == 0){
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
            return result;
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("businessManager",businessManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        PageHelper.startPage(pageNo, pageSize);
        List<MonthStatisticBusinessManager> list = settlementService.monthStatisticByBusinessManager(map);
        PageInfo<StatisticBusinessManager> page = new PageInfo(list);
        Map<String, Object> resultMap = new HashMap<>(4);

        resultMap.put("total", page.getTotal());
        resultMap.put("list", page.getList());

        String amount = settlementService.monthStatisticSumAmountByBusinessManager(map);
        resultMap.put("amount",amount);
        return returnSuccess(resultMap);
    }
    /**
     * 清结算数据统计--业务经理月度数据统计--明细
     */
    @RequestMapping(value = "businessmanager/monthStatistics/detail", method = RequestMethod.POST)
    public Map<String, Object> businessManagerMonthStatisticsDetail(HttpServletRequest request,String businessManager,
                                                                    String month,String businessPlatformId,
                                                                    @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                                    @RequestParam(required = false, defaultValue = "10") int pageSize) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //判断是否有平台ID
        if (StringUtil.isEmpty(businessPlatformId)) {
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
            return result;
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        if (platformMap == null || platformMap.size() == 0){
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
            return result;
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("businessManager", businessManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("month", month);
        PageHelper.startPage(pageNo, pageSize);
        List<MonthStatisticBusinessManagerDetail> list = settlementService.monthStatisticByBusinessManagerDetail(map);
        PageInfo<MonthStatisticBusinessManagerDetail> page = new PageInfo(list);
        Map<String, Object> resultMap = new HashMap<>(4);

        resultMap.put("total", page.getTotal());
        resultMap.put("list", page.getList());

        String amount = settlementService.monthStatisticSumAmountByBusinessManagerDetail(map);
        resultMap.put("amount", amount);
        return returnSuccess(resultMap);
    }


    /**
     * 清结算数据统计--运营经理数据统计
     */
    @RequestMapping(value = "operationsManager/statistics", method = RequestMethod.POST)
    public Map<String, Object> operationsManagerStatistics(HttpServletRequest request,String operationsManager, String startTime,
        String endTime, String businessPlatformId,
        @RequestParam(required = false, defaultValue = "1") int pageNo,
        @RequestParam(required = false, defaultValue = "10") int pageSize) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //判断是否有平台ID
        if (StringUtil.isEmpty(businessPlatformId)) {
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
            return result;
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        if (platformMap == null || platformMap.size() == 0){
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
            return result;
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("operationsManager",operationsManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        PageHelper.startPage(pageNo, pageSize);
        List<StatisticOperationsManager> list = settlementService.statisticByOperationsManager(map);
        PageInfo<StatisticOperationsManager> page = new PageInfo(list);
        Map<String, Object> resultMap = new HashMap<>(4);

        resultMap.put("total", page.getTotal());
        resultMap.put("list", page.getList());

        String amount = settlementService.statisticSumAmountByOperationsManager(map);
        resultMap.put("amount",amount);
        return returnSuccess(resultMap);
    }
    /**
     * 清结算数据统计--运营经理数据统计--明细
     */
    @RequestMapping(value = "operationsManager/statistics/detail", method = RequestMethod.POST)
    public Map<String, Object> operationsManagerStatisticsDetail(HttpServletRequest request,String operationsManager,String startTime,
        String endTime,String businessPlatformId,
        @RequestParam(required = false, defaultValue = "1") int pageNo,
        @RequestParam(required = false, defaultValue = "10") int pageSize) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //判断是否有平台ID
        if (StringUtil.isEmpty(businessPlatformId)) {
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
            return result;
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        if (platformMap == null || platformMap.size() == 0){
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
            return result;
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("operationsManager",operationsManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("startTime",startTime);
        map.put("endTime",endTime);

        PageHelper.startPage(pageNo, pageSize);
        List<StatisticOperationsManagerDetail> list = settlementService.statisticByOperationsManagerDetail(map);
        Map<String, Object> resultMap = new HashMap<>(4);
        PageInfo<StatisticOperationsManager> page = new PageInfo(list);
        resultMap.put("total", page.getTotal());
        resultMap.put("list", page.getList());

        String amount = settlementService.statisticSumAmountByOperationsManagerDetail(map);
        resultMap.put("amount",amount);
        return returnSuccess(resultMap);
    }


    /**
     * 清结算数据统计--运营经理月度数据统计
     */
    @RequestMapping(value = "operationsManager/monthStatistics", method = RequestMethod.POST)
    public Map<String, Object> operationsManagerMonthStatistics(HttpServletRequest request,String operationsManager,String startTime,
        String endTime,String businessPlatformId,
        @RequestParam(required = false, defaultValue = "1") int pageNo,
        @RequestParam(required = false, defaultValue = "10") int pageSize) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //判断是否有平台ID
        if (StringUtil.isEmpty(businessPlatformId)) {
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
            return result;
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        if (platformMap == null || platformMap.size() == 0){
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
            return result;
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("operationsManager",operationsManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        PageHelper.startPage(pageNo, pageSize);
        List<MonthStatisticOperationsManager> list = settlementService.monthStatisticByOperationsManager(map);
        PageInfo<StatisticBusinessManager> page = new PageInfo(list);
        Map<String, Object> resultMap = new HashMap<>(4);

        resultMap.put("total", page.getTotal());
        resultMap.put("list", page.getList());

        String amount = settlementService.monthStatisticSumAmountByOperationsManager(map);
        resultMap.put("amount",amount);
        return returnSuccess(resultMap);
    }
    /**
     * 清结算数据统计--运营经理月度数据统计--明细
     */
    @RequestMapping(value = "operationsManager/monthStatistics/detail", method = RequestMethod.POST)
    public Map<String, Object> operationsManagerMonthStatisticsDetail(HttpServletRequest request,String operationsManager,
        String month,String businessPlatformId,
        @RequestParam(required = false, defaultValue = "1") int pageNo,
        @RequestParam(required = false, defaultValue = "10") int pageSize) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //判断是否有平台ID
        if (StringUtil.isEmpty(businessPlatformId)) {
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
            return result;
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        if (platformMap == null || platformMap.size() == 0){
            HashMap<String, Object> result = new HashMap<>();
            result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
            return result;
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("operationsManager", operationsManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("month", month);
        PageHelper.startPage(pageNo, pageSize);
        List<MonthStatisticOperationsManagerDetail> list = settlementService.monthStatisticByOperationsManagerDetail(map);
        PageInfo<MonthStatisticOperationsManagerDetail> page = new PageInfo(list);
        Map<String, Object> resultMap = new HashMap<>(4);

        resultMap.put("total", page.getTotal());
        resultMap.put("list", page.getList());

        String amount = settlementService.monthStatisticSumAmountByOperationsManagerDetail(map);
        resultMap.put("amount", amount);
        return returnSuccess(resultMap);
    }




    private Map<String, Object> checkAndGetPlatformName(HttpServletRequest request,String businessPlatformId){
        HashMap<String, Object> result = new HashMap<>();
        result.put(RespCode.RESP_STAT, RespCode.success);
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //判断是否有平台ID
        if (StringUtil.isEmpty(businessPlatformId)) {
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
            return result;
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        if (platformMap == null || platformMap.size() == 0){
            result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
            return result;
        }
        result.put("businessPlatform", platformMap.get("businessPlatform"));
        return result;
    }
}
