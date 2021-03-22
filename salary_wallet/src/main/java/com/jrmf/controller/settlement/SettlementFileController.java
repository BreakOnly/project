package com.jrmf.controller.settlement;

import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.settlement.*;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.SettlementService;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2020-03-16 17:40
 * @desc
 **/
@Controller
@RequestMapping("settlement")
public class SettlementFileController extends BaseController {
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
    @RequestMapping(value = "company/statistics/export", method = RequestMethod.GET)
    public void companyStatistics(HttpServletRequest request,String businessPlatformId,
                                  int type, String startTime, String endTime,
                                  HttpServletResponse response) {
        Map<String, Object> stringObjectMap = checkAndGetPlatformName(request, businessPlatformId);
        if (((Integer) stringObjectMap.get(RespCode.RESP_STAT)) != RespCode.success) {
            return;
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("type",type);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("businessPlatform",(String) stringObjectMap.get("businessPlatform"));
        List<StatisticCompany> list = settlementService.statisticByCompany(map);
        String[] colunmName = new String[]{"平台名称", "服务公司名称", "交易总金额", "统计交易开始时间", "统计交易结束时间"};
        String filename = "服务公司数据统计";
        List<Map<String, Object>> data = new ArrayList<>();
        for (StatisticCompany statisticCompany : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", statisticCompany.getPlantForm());
            dataMap.put("2", statisticCompany.getCompanyName());
            dataMap.put("3", statisticCompany.getAmount());
            dataMap.put("4", startTime);
            dataMap.put("5", endTime);
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }
    /**
     * 清结算数据统计--服务公司数据统计--明细
     * type  1 服务公司
     *       2 平台
     */
    @RequestMapping(value = "company/statistics/detail/export", method = RequestMethod.GET)
    public void companyStatisticsDetail(HttpServletRequest request,String businessPlatformId,
                                        int type,String key,String startTime,String endTime,
                                        HttpServletResponse response) {
        Map<String, Object> stringObjectMap = checkAndGetPlatformName(request, businessPlatformId);
        if (((Integer) stringObjectMap.get(RespCode.RESP_STAT)) != RespCode.success) {
            return;
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("type",type);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("key",key);
        map.put("businessPlatform",(String) stringObjectMap.get("businessPlatform"));
        List<StatisticCompanyDetail> list = settlementService.statisticByCompanyDetail(map);
        String[] colunmName = new String[]{"平台名称", "服务公司名称","商户名称", "交易总金额", "统计交易开始时间", "统计交易结束时间"};
        String filename = "服务公司数据统计";
        List<Map<String, Object>> data = new ArrayList<>();
        for (StatisticCompanyDetail statisticCompanyDetail : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", statisticCompanyDetail.getPlantForm());
            dataMap.put("2", statisticCompanyDetail.getCompanyName());
            dataMap.put("3", statisticCompanyDetail.getCustomName());
            dataMap.put("4", statisticCompanyDetail.getAmount());
            dataMap.put("5", startTime);
            dataMap.put("6", endTime);
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }
    /**
     * 清结算数据统计--服务公司数据统计--月度统计
     * type  1 服务公司
     *       2 平台
     */
    @RequestMapping(value = "company/statistics/month/export", method = RequestMethod.GET)
    public void companyStatisticsMonth(HttpServletRequest request,String businessPlatformId,
                                       int type, String startTime, String endTime,
                                                 HttpServletResponse response) {
        Map<String, Object> stringObjectMap = checkAndGetPlatformName(request, businessPlatformId);
        if (((Integer) stringObjectMap.get(RespCode.RESP_STAT)) != RespCode.success) {
            return;
        }
        Map<String, Object> map = new HashMap<>(8);
        map.put("type",type);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("businessPlatform",(String) stringObjectMap.get("businessPlatform"));
        List<StatisticCompanyGroupByMonth> list = settlementService.statisticByCompanyGroupByMonth(map);
        String[] colunmName = new String[]{"平台名称","月份", "服务公司名称", "小额实发金额", "大额实发金额", "总实发金额"};
        String filename = "服务公司数据统计";
        List<Map<String, Object>> data = new ArrayList<>();
        for (StatisticCompanyGroupByMonth statisticCompany : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", statisticCompany.getPlantForm());
            dataMap.put("2", statisticCompany.getMonth());
            dataMap.put("3", statisticCompany.getCompanyName());
            dataMap.put("4", statisticCompany.getSmallAmount());
            dataMap.put("5", statisticCompany.getBigAmount());
            dataMap.put("6", statisticCompany.getAmount());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }
    /**
     * 清结算数据统计--服务公司数据统计--明细
     * type  1 服务公司
     *       2 平台
     */
    @RequestMapping(value = "company/statistics/detail/month/export", method = RequestMethod.GET)
    public void companyStatisticsDetailMonth(HttpServletRequest request,String businessPlatformId,
                                             int type,String key,String startTime,String endTime,
                                        HttpServletResponse response) {
        Map<String, Object> stringObjectMap = checkAndGetPlatformName(request, businessPlatformId);
        if (((Integer) stringObjectMap.get(RespCode.RESP_STAT)) != RespCode.success) {
            return;
        }
        Map<String, Object> map = new HashMap<>(8);
        map.put("type",type);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("key",key);
        map.put("businessPlatform",(String) stringObjectMap.get("businessPlatform"));
        List<StatisticCompanyGroupByMonthDetail> list = settlementService.statisticByCompanyDetailGroupByMonth(map);
        String[] colunmName = new String[]{"月份","平台名称", "服务公司名称","商户名称", "小额实发金额","大额实发金额","总实发金额"};
        String filename = "服务公司数据统计";
        List<Map<String, Object>> data = new ArrayList<>();
        for (StatisticCompanyGroupByMonthDetail detail : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", detail.getMonth());
            dataMap.put("2", detail.getPlantForm());
            dataMap.put("3", detail.getCompanyName());
            dataMap.put("4", detail.getCustomName());
            dataMap.put("5", detail.getSmallAmount());
            dataMap.put("6", detail.getBigAmount());
            dataMap.put("7", detail.getAmount());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 清结算数据统计--业务经理数据统计
     */
    @RequestMapping(value = "businessmanager/statistics/export", method = RequestMethod.GET)
    public void businessManagerStatistics(HttpServletRequest request,String businessManager,String businessPlatformId,
                                          String startTime, String endTime, HttpServletResponse response) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        Map<String, Object> map = new HashMap<>(8);
        map.put("businessManager",businessManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        List<StatisticBusinessManager> list = settlementService.statisticByBusinessManager(map);
        String[] colunmName = new String[]{"业务经理", "交易商户数", "交易总金额", "统计交易开始时间", "统计交易结束时间"};
        String filename = "服务公司数据统计";
        List<Map<String, Object>> data = new ArrayList<>();
        for (StatisticBusinessManager statisticBusinessManager : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", statisticBusinessManager.getBusinessManager());
            dataMap.put("2", statisticBusinessManager.getCount());
            dataMap.put("3", statisticBusinessManager.getAmount());
            dataMap.put("4", startTime);
            dataMap.put("5", endTime);
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 清结算数据统计--运营经理数据统计
     */
    @RequestMapping(value = "operationsManager/statistics/export", method = RequestMethod.GET)
    public void operationsManagerStatistics(HttpServletRequest request,String operationsManager,String businessPlatformId,
        String startTime, String endTime, HttpServletResponse response) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        Map<String, Object> map = new HashMap<>(8);
        map.put("operationsManager",operationsManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        List<StatisticOperationsManager> list = settlementService.statisticByOperationsManager(map);
        String[] colunmName = new String[]{"运营经理", "交易商户数", "交易总金额", "统计交易开始时间", "统计交易结束时间"};
        String filename = "服务公司数据统计";
        List<Map<String, Object>> data = new ArrayList<>();
        for (StatisticOperationsManager statisticBusinessManager : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", statisticBusinessManager.getOperationsManager());
            dataMap.put("2", statisticBusinessManager.getCount());
            dataMap.put("3", statisticBusinessManager.getAmount());
            dataMap.put("4", startTime);
            dataMap.put("5", endTime);
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }


    /**
     * 清结算数据统计--业务经理数据统计--明细
     */
    @RequestMapping(value = "businessmanager/statistics/detail/export", method = RequestMethod.GET)
    public void businessManagerStatisticsDetail(HttpServletRequest request,String businessManager,String businessPlatformId,
        String startTime,String endTime,HttpServletResponse response) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        Map<String, Object> map = new HashMap<>(8);
        map.put("businessManager",businessManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        List<StatisticBusinessManagerDetail> list = settlementService.statisticByBusinessManagerDetail(map);
        String[] colunmName = new String[]{"业务经理", "代理商名称","商户名称", "交易总金额", "统计交易开始时间", "统计交易结束时间"};
        String filename = "服务公司数据统计";
        List<Map<String, Object>> data = new ArrayList<>();
        for (StatisticBusinessManagerDetail statisticBusinessManagerDetail : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", statisticBusinessManagerDetail.getBusinessManager());
            dataMap.put("2", statisticBusinessManagerDetail.getBusinessChannel());
            dataMap.put("3", statisticBusinessManagerDetail.getCustomName());
            dataMap.put("4", statisticBusinessManagerDetail.getAmount());
            dataMap.put("5", startTime);
            dataMap.put("6", endTime);
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 清结算数据统计--运营经理数据统计--明细
     */
    @RequestMapping(value = "operationsManager/statistics/detail/export", method = RequestMethod.GET)
    public void operationsManagerStatisticsDetail(HttpServletRequest request,String operationsManager,String businessPlatformId,
                                                String startTime,String endTime,HttpServletResponse response) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        Map<String, Object> map = new HashMap<>(8);
        map.put("operationsManager",operationsManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        List<StatisticOperationsManagerDetail> list = settlementService.statisticByOperationsManagerDetail(map);
        String[] colunmName = new String[]{"运营经理", "代理商名称","商户名称", "交易总金额", "统计交易开始时间", "统计交易结束时间"};
        String filename = "运营经理数据统计";
        List<Map<String, Object>> data = new ArrayList<>();
        for (StatisticOperationsManagerDetail statisticBusinessManagerDetail : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", statisticBusinessManagerDetail.getOperationsManager());
            dataMap.put("2", statisticBusinessManagerDetail.getBusinessChannel());
            dataMap.put("3", statisticBusinessManagerDetail.getCustomName());
            dataMap.put("4", statisticBusinessManagerDetail.getAmount());
            dataMap.put("5", startTime);
            dataMap.put("6", endTime);
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }


    /**
     * 清结算数据统计--业务经理数据月度统计导出
     */
    @RequestMapping(value = "businessmanager/monthStatistics/export", method = RequestMethod.GET)
    public void businessManagerMonthStatistics(HttpServletRequest request, String businessManager,String businessPlatformId,
                                               String startTime, String endTime, HttpServletResponse response) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        Map<String, Object> map = new HashMap<>(8);
        map.put("businessManager", businessManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        List<MonthStatisticBusinessManager> list = settlementService.monthStatisticByBusinessManager(map);
        String[] colunmName = new String[]{"月份", "业务所属销售经理", "小额实发金额", "大额实发金额", "总实发金额", "完成交易的商户数"};
        String filename = "销售经理数据统计";
        List<Map<String, Object>> data = new ArrayList<>();
        for (MonthStatisticBusinessManager statisticBusinessManager : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", statisticBusinessManager.getMonth());
            dataMap.put("2", statisticBusinessManager.getBusinessManager());
            dataMap.put("3", statisticBusinessManager.getSmallAmount());
            dataMap.put("4", statisticBusinessManager.getBigAmount());
            dataMap.put("5", statisticBusinessManager.getAmount());
            dataMap.put("6", statisticBusinessManager.getCount());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 清结算数据统计--运营经理数据月度统计导出
     */
    @RequestMapping(value = "operationsManager/monthStatistics/export", method = RequestMethod.GET)
    public void operationsManagerMonthStatistics(HttpServletRequest request, String operationsManager,String businessPlatformId,
        String startTime, String endTime, HttpServletResponse response) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        Map<String, Object> map = new HashMap<>(8);
        map.put("operationsManager", operationsManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        List<MonthStatisticOperationsManager> list = settlementService.monthStatisticByOperationsManager(map);
        String[] colunmName = new String[]{"月份", "业务所属运营经理", "小额实发金额", "大额实发金额", "总实发金额", "完成交易的商户数"};
        String filename = "运营经理月度数据统计";
        List<Map<String, Object>> data = new ArrayList<>();
        for (MonthStatisticOperationsManager statisticBusinessManager : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", statisticBusinessManager.getMonth());
            dataMap.put("2", statisticBusinessManager.getOperationsManager());
            dataMap.put("3", statisticBusinessManager.getSmallAmount());
            dataMap.put("4", statisticBusinessManager.getBigAmount());
            dataMap.put("5", statisticBusinessManager.getAmount());
            dataMap.put("6", statisticBusinessManager.getCount());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }
    /**
     * 清结算数据统计--业务经理数据月度统计--明细导出
     */
    @RequestMapping(value = "businessmanager/monthStatistics/detail/export", method = RequestMethod.GET)
    public void businessManagerMonthStatisticsDetail(HttpServletRequest request,String businessManager, String businessPlatformId,
                                                     String month, HttpServletResponse response) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        Map<String, Object> map = new HashMap<>(8);
        map.put("businessManager", businessManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("month", month);
        List<MonthStatisticBusinessManagerDetail> list = settlementService.monthStatisticByBusinessManagerDetail(map);
        String[] colunmName = new String[]{"月份", "业务所属销售经理", "业务所属渠道", "商户名称", "小额实发金额", "大额实发金额", "总实发金额"};
        String filename = "销售经理明细数据统计";
        List<Map<String, Object>> data = new ArrayList<>();
        for (MonthStatisticBusinessManagerDetail statisticBusinessManagerDetail : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", statisticBusinessManagerDetail.getMonth());
            dataMap.put("2", statisticBusinessManagerDetail.getBusinessManager());
            dataMap.put("3", statisticBusinessManagerDetail.getBusinessChannel());
            dataMap.put("4", statisticBusinessManagerDetail.getCustomName());
            dataMap.put("5", statisticBusinessManagerDetail.getSmallAmount());
            dataMap.put("6", statisticBusinessManagerDetail.getBigAmount());
            dataMap.put("7", statisticBusinessManagerDetail.getAmount());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 清结算数据统计--运营经理数据月度统计--明细导出
     */
    @RequestMapping(value = "operationsManager/monthStatistics/detail/export", method = RequestMethod.GET)
    public void operationsManagerMonthStatisticsDetail(HttpServletRequest request,String operationsManager, String businessPlatformId,
        String month, HttpServletResponse response) {
        //判断是否是平台商户
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (isPlatformAccount(customLogin)) {
            Integer integer = checkCustom(customLogin);
            businessPlatformId = String.valueOf(integer);
        }
        //根据businessPlatformId获取businessPlatform
        HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
        Map<String, Object> map = new HashMap<>(8);
        map.put("operationsManager", operationsManager);
        map.put("businessPlatform", (String) platformMap.get("businessPlatform"));
        map.put("month", month);
        List<MonthStatisticOperationsManagerDetail> list = settlementService.monthStatisticByOperationsManagerDetail(map);
        String[] colunmName = new String[]{"月份", "业务所属运营经理", "业务所属渠道", "商户名称", "小额实发金额", "大额实发金额", "总实发金额"};
        String filename = "运营经理月明细数据统计";
        List<Map<String, Object>> data = new ArrayList<>();
        for (MonthStatisticOperationsManagerDetail statisticBusinessManagerDetail : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", statisticBusinessManagerDetail.getMonth());
            dataMap.put("2", statisticBusinessManagerDetail.getOperationsManager());
            dataMap.put("3", statisticBusinessManagerDetail.getBusinessChannel());
            dataMap.put("4", statisticBusinessManagerDetail.getCustomName());
            dataMap.put("5", statisticBusinessManagerDetail.getSmallAmount());
            dataMap.put("6", statisticBusinessManagerDetail.getBigAmount());
            dataMap.put("7", statisticBusinessManagerDetail.getAmount());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
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
