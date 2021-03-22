package com.jrmf.controller.systemrole.grantCompany.statistics;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.CertType;
import com.jrmf.controller.constant.CommissionStatus;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.PayType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.CommissionTemporary;
import com.jrmf.domain.Company;
import com.jrmf.domain.UserCommission;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.service.UserSerivce;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.*;

/**
 * Author Nicholas-Ning
 * Description //TODO 服务公司相关查询统计
 * Date 17:25 2018/12/19
 * Param
 * return
 *
 * @author guoto*/
@Controller
@RequestMapping("/company/statistics")
public class CompanyStatisticsControlller extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(CompanyStatisticsControlller.class);
    @Autowired
    protected UserSerivce userSerivce;
    @Autowired
    private UserCommissionService commissionService;
    @Autowired
    private ChannelHistoryService channelHistoryService;
	  @Autowired
	  private BaseInfo baseInfo;
	  @Autowired
    private ChannelCustomService customService;

    /**
     * Author Nicholas-Ning
     * Description //TODO 服务公司查询，订单明细维度查询。
     * Date 16:29 2018/12/19
     * Param
     * return
     **/
    @RequestMapping("/query/commission")
    public @ResponseBody
    Map<String, Object> queryUserCommission(HttpSession session,
                                            @RequestParam(required = false) String startTime,
                                            @RequestParam(required = false) String endTime,
                                            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String userName,
                                            @RequestParam(required = false) String batchName,
                                            @RequestParam(required = false) String batchDesc,
                                            @RequestParam(required = false) String account,
                                            @RequestParam(required = false) Double amountStart,
                                            @RequestParam(required = false) Double amountEnd,
                                            @RequestParam(required = false) String certId,
                                            @RequestParam(required = false) Integer payType ,
                                            @RequestParam(required = false) Integer companyId ,
                                            @RequestParam(required = false) Integer status ,
                                            @RequestParam(required = false) String customName) {
        String recCustomkey =(String) session.getAttribute(CommonString.CUSTOMKEY);
        ChannelCustom custom = customService.getCustomByCustomkey(recCustomkey);
        if(StringUtil.isEmpty(recCustomkey)){
            return returnFail(RespCode.error101,RespCode.PARAMS_ERROR);
        }
        if(custom.getCustomType() != CustomType.COMPANY.getCode()){
            return returnFail(RespCode.error101,RespCode.ROLE_ERROR);
        }
        Map<String,Object> params = new HashMap<>(15);
        params.put("startTime",startTime);
        params.put("endTime",endTime);
        params.put("userName",userName);
        params.put("batchName",batchName);
        params.put("batchDesc",batchDesc);
        params.put("account",account);
        params.put("certId",certId);
        params.put("amountStart",amountStart);
        params.put("amountEnd",amountEnd);
        params.put("payType",payType);
        params.put("companyId",recCustomkey);
        params.put("status",status);
        params.put("customName",customName);
        PageHelper.startPage(pageNo,pageSize);
        List<HashMap<String, Object>> commissions = commissionService.getCompanyCommissions(params);
        PageInfo<HashMap<String, Object>> pageInfo = new PageInfo(commissions);
        getFitCommissions(commissions, recCustomkey);
        return returnSuccess(commissions,pageInfo.getTotal());
    }

    private List<HashMap<String, Object>> getFitCommissions(List<HashMap<String, Object>> commissions,String currCompanyId){
        if (commissions == null || commissions.isEmpty()){
            return commissions;
        }
        List<HashMap<String, Object>> resultCommussions = new ArrayList<>();
        for (HashMap<String, Object> commission : commissions) {
            String companyId = (String) commission.get("companyId");
            if (!currCompanyId.equals(companyId)){
                //通过其它服务公司转包到当前服务公司下发的数据
                //更正商户名称和服务公司名称
                String companyName = (String) commission.get("companyName");
                String realCompanyName = (String) commission.get("realCompanyName");
                commission.put("customName",companyName);
                commission.put("companyName",realCompanyName);
                //隐藏不需要展示的数据
                commission.put("calculationRates","");
                commission.put("sumFee","");
                commission.put("supplementAmount","");
                commission.put("supplementFee","");
                commission.put("feeRuleType","");
            }
        }
        return resultCommussions;
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 服务公司查询，订单明细维度查询。
     * Date 16:29 2018/12/19
     * Param
     * return
     **/
    @RequestMapping("/query/commission/excel")
    public void queryUserCommission(HttpSession session,HttpServletResponse response,
                                            @RequestParam(required = false) String startTime,
                                            @RequestParam(required = false) String endTime,
                                            @RequestParam(required = false) String userName,
                                            @RequestParam(required = false) String batchName,
                                            @RequestParam(required = false) String batchDesc,
                                            @RequestParam(required = false) String account,
                                            @RequestParam(required = false) String amountStart,
                                            @RequestParam(required = false) String amountEnd,
                                            @RequestParam(required = false) String certId,
                                            @RequestParam(required = false) Integer payType ,
                                            @RequestParam(required = false) Integer companyId ,
                                            @RequestParam(required = false) Integer status ,
                                            @RequestParam(required = false) String customName) {
        String recCustomkey =(String) session.getAttribute(CommonString.CUSTOMKEY);
        Map<String,Object> params = new HashMap<>(15);
        params.put("startTime",startTime);
        params.put("endTime",endTime);
        params.put("userName",userName);
        params.put("batchName",batchName);
        params.put("batchDesc",batchDesc);
        params.put("account",account);
        params.put("certId",certId);
        params.put("amountStart",amountStart);
        params.put("amountEnd",amountEnd);
        params.put("payType",payType);
        params.put("companyId",recCustomkey);
        params.put("status",status);
        params.put("customName",customName);
        List<HashMap<String, Object>> list = commissionService.getCompanyCommissions(params);
        getFitCommissions(list, recCustomkey);
        List<Map<String, Object>> data = new ArrayList<>();
        String filename = "交易明细统计";
        String[] colunmName = new String[] { "商户名称","订单ID","收款人姓名","证件类型","证件号","手机号","收款账号",
                "服务费计算规则","订单状态","交易金额","到账金额","服务费率","服务费(包含补差价)","补差价交易金额","补差价服务费",
                "订单备注","下发通道","服务公司","账号所属金融机构","交易时间","批次名称",
                "批次说明","交易结果描述","最后更新时间"};
        for (HashMap<String, Object> userCommission : list) {
            Map<String, Object> dataMap = new HashMap<>(25);
            dataMap.put("1",userCommission.get("customName"));
            dataMap.put("2",userCommission.get("orderNo"));
            dataMap.put("3",userCommission.get("userName"));
            dataMap.put("4",CertType.codeOf((int)userCommission.get("documentType")).getDesc());
            dataMap.put("5",userCommission.get("certId"));
            dataMap.put("6",userCommission.get("phoneNo"));
            dataMap.put("7",userCommission.get("account"));
            dataMap.put("8",userCommission.get("feeRuleType"));
            dataMap.put("9",CommissionStatus.codeOf((int)userCommission.get("status")).getDesc());
            dataMap.put("10",userCommission.get("sourceAmount"));
            dataMap.put("11",userCommission.get("amount"));
            dataMap.put("12",userCommission.get("calculationRates"));
            dataMap.put("13",userCommission.get("sumFee"));
            dataMap.put("14",userCommission.get("supplementAmount"));
            dataMap.put("15",userCommission.get("supplementFee"));
            dataMap.put("16",userCommission.get("remark"));
            dataMap.put("17",PayType.codeOf((int)userCommission.get("payType")).getDesc());
            dataMap.put("18",userCommission.get("companyName"));
            dataMap.put("19",userCommission.get("bankName"));
            dataMap.put("20",userCommission.get("createtime"));
            dataMap.put("21",userCommission.get("batchName"));
            dataMap.put("22",userCommission.get("batchDesc"));
            dataMap.put("23",userCommission.get("statusDesc"));
            dataMap.put("24",userCommission.get("updatetime"));
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }


    /**
     * Author Nicholas-Ning
     * Description //TODO 服务公司---收款用户交易统计---用户交易信息统计表 说明:两万八包含在大于的区间里
     * Date 17:08 2018/12/19
     * Param [request, response]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @RequestMapping(value = "/getUserTradeData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> getUserTradeData(HttpServletRequest request,
                                         HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(5);
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        String companyId = (String) request.getSession().getAttribute("customkey");
        ChannelCustom custom = customService.getCustomByCustomkey(companyId);
        if (custom.getCustomType() != CustomType.COMPANY.getCode()) {
            return returnFail(RespCode.error101, RespCode.ROLE_ERROR);
        }
        // 身份证号
        String certId = request.getParameter("certId");
        // 人月下发额 -1 小于  1 大于
        String monthAmount = request.getParameter("monthAmount");
        // 批次名称
        String batchName = request.getParameter("batchName");
        // 用户姓名
        String userName = request.getParameter("userName");
        // 交易时间起始
        String tradeTimeStart = request.getParameter("tradeTimeStart");
        // 交易时间结束
        String tradeTimeEnd = request.getParameter("tradeTimeEnd");
        // 批次备注
        String batchDesc = request.getParameter("batchDesc");
        // 商户名称
        String customName = request.getParameter("customName");
        // 下发通道
        String payType = request.getParameter("payType");
        // 交易总额起始
        String amountStart = request.getParameter("amountStart");
        // 交易总额结束
        String amountEnd = request.getParameter("amountEnd");
        // 当前页码
//        String pageNo = request.getParameter("pageNo");
        logger.info("收款用户交易统计方法入参： companyId=" + companyId
                + " certId=" + certId + " userName" + userName + " batchName"
                + batchName + " tradeTimeStart=" + " tradeTimeEnd" + tradeTimeEnd + " batchDesc="
                + batchDesc + " customName" + customName + " payType=" + payType);
        if ((!StringUtil.isEmpty(amountStart) || !StringUtil.isEmpty(amountStart)) && !StringUtil.isEmpty(monthAmount)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
            return result;
        }
        Map<String, Object> param = new HashMap<>(20);
        param.put("companyId", companyId);
        param.put("certId", certId);
        param.put("batchName", batchName);
        param.put("userName", userName);
        param.put("tradeTimeStart", tradeTimeStart);
        param.put("tradeTimeEnd", tradeTimeEnd);
        param.put("batchDesc", batchDesc);
        param.put("customName", customName);
        param.put("payType", payType);
        param.put("amountStart", amountStart);
        param.put("amountEnd", amountEnd);
        param.put("monthAmount", monthAmount);
        param.put("calculationLimit", baseInfo.getCalculationLimit());
        try {
//            int total = commissionService.getUserTradeCompany(param).size();
//            result.put("total", total);
//            int pageSize = 10;
//            if (!StringUtil.isEmpty(pageNo)) {
//                param.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                param.put("limit", pageSize);
//            }
            PageHelper.startPage(pageNo, pageSize);
            List<UserCommission> userTradeData = commissionService.getUserTradeCompany(param);
            PageInfo<UserCommission> pageInfo = new PageInfo<>(userTradeData);
            result.put("total", pageInfo.getTotal());
            result.put("userTradeData", pageInfo.getList());
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
            respstat = RespCode.error107;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
            return result;
        }
        return result;
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 服务公司---收款用户交易统计---用户交易信息明细统计表 说明:
     * Date 17:08 2018/12/19
     * Param [request, response]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @RequestMapping(value = "/getUserTradeDataDetail", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> getUserTradeDataDetail(HttpServletRequest request,
                                               HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(5);
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        String companyId = (String) request.getSession().getAttribute("customkey");
        ChannelCustom custom = customService.getCustomByCustomkey(companyId);
        if (custom.getCustomType() != CustomType.COMPANY.getCode()) {
            return returnFail(RespCode.error101, RespCode.ROLE_ERROR);
        }
        // 用户id
        String userId = request.getParameter("userId");
        // 当前页码
//        String pageNo = request.getParameter("pageNo");

        // 身份证号 -1 小于  1 大于
        String certId = request.getParameter("certId");
        // 人月下发额
        String monthAmount = request.getParameter("monthAmount");
        // 批次名称
        String batchName = request.getParameter("batchName");
        // 用户姓名
        String userName = request.getParameter("userName");
        // 交易时间起始
        String tradeTimeStart = request.getParameter("tradeTimeStart");
        // 交易时间结束
        String tradeTimeEnd = request.getParameter("tradeTimeEnd");
        // 批次备注
        String batchDesc = request.getParameter("batchDesc");
        // 商户名称
        String customName = request.getParameter("customName");
        // 下发通道
        String payType = request.getParameter("payType");
        // 交易总额起始
        String amountStart = request.getParameter("amountStart");
        // 交易总额结束
        String amountEnd = request.getParameter("amountEnd");
        if (StringUtil.isEmpty(userId)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
            return result;
        }
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("companyId", companyId);
        param.put("userId", userId);
        param.put("certId", certId);
        param.put("userName", userName);
        param.put("batchName", batchName);
        param.put("tradeTimeStart", tradeTimeStart);
        param.put("tradeTimeEnd", tradeTimeEnd);
        param.put("batchDesc", batchDesc);
        param.put("customName", customName);
        param.put("payType", payType);
        param.put("monthAmount", monthAmount);
        param.put("amountStart", amountStart);
        param.put("amountEnd", amountEnd);
        logger.info("收款用户交易统计详情方法入参： companyId=" + companyId + " userId=" + userId
                + " certId=" + certId + " userName=" + userName + " batchName="
                + batchName + " tradeTimeStart=" + tradeTimeStart + " tradeTimeEnd=" + tradeTimeEnd + " batchDesc="
                + batchDesc + " customName=" + customName + " payType=" + payType);
        try {
//            int total = commissionService.getUserTradeDetailCompany(param).size();
//            result.put("total", total);
//            int pageSize = 10;
//            if (!StringUtil.isEmpty(pageNo)) {
//                param.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                param.put("limit", pageSize);
//            }
            PageHelper.startPage(pageNo, pageSize);
            List<UserCommission> userTradeData = commissionService.getUserTradeDetailCompany(param);
            PageInfo<UserCommission> pageInfo = new PageInfo<>(userTradeData);
            result.put("total", pageInfo.getTotal());
            result.put("userTradeData", pageInfo.getList());
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
            respstat = RespCode.error107;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
            return result;
        }
        return result;
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 批次交易结果查询--列表
     * Date 17:08 2018/12/19
     * Param [request, response]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @RequestMapping(value = "/resultQueryByBatch", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> resultQueryByBatch(HttpServletRequest request, HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(20);
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        String customkey = (String) request.getSession().getAttribute("customkey");// 商户标识
        ChannelCustom custom = customService.getCustomByCustomkey(customkey);
        if (custom.getCustomType() != CustomType.COMPANY.getCode()) {
            return returnFail(RespCode.error101, RespCode.ROLE_ERROR);
        }
        //商户名称
        String customName = request.getParameter("customName");
        //批次名称
        String batchName = request.getParameter("batchName");
        //批次说明
        String batchDesc = request.getParameter("batchDesc");
        //项目名称
        String contentName = request.getParameter("contentName");
        //下发通道
        String payType = request.getParameter("payType");
        //订单状态
        String status = request.getParameter("status");
        //开始时间
        String submitTimeStart = request.getParameter("submitTimeStart");
        //结束时间
        String submitTimeEnd = request.getParameter("submitTimeEnd");
        //到账时间区间——开始
        String completeTimeStart = request.getParameter("completeTimeStart");
        //到账时间区间——结束
        String completeTimeEnd = request.getParameter("completeTimeEnd");
        String amount = request.getParameter("amount");
        //批次总金额
        amount = ArithmeticUtil.formatDecimals(amount);
        //文件名称
        String fileName = request.getParameter("fileName");
        //平台ID
        String businessPlatform = request.getParameter("businessPlatform");
        try {
            Map<String, Object> param = new HashMap<>(20);
            param.put("customkey", customkey);
            param.put("customName", customName);
            param.put("batchName", batchName);
            param.put("batchDesc", batchDesc);
            param.put("contentName", contentName);
            param.put("payType", payType);
            param.put("status", status);
            param.put("submitTimeStart", submitTimeStart);
            param.put("submitTimeEnd", submitTimeEnd);
            param.put("completeTimeStart", completeTimeStart);
            param.put("completeTimeEnd", completeTimeEnd);
            param.put("batchAmount", amount);
            param.put("fileName", fileName);
            param.put("businessPlatform", businessPlatform);
            PageHelper.startPage(pageNo, pageSize);
            List<Map<String, Object>> list = channelHistoryService.batchResultQueryByCompany(param);
            getFitCommissions2(list, customkey);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
            result.put("list", pageInfo.getList());
            result.put("total", pageInfo.getTotal());
        } catch (Exception e) {
            logger.error("", e);
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "查询失败");
            return result;
        }
        return result;
    }

    private void getFitCommissions2(List<Map<String, Object>> list, String currCompanyId){
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> var : list) {
                String companyId = (String) var.get("companyId");
                if (!currCompanyId.equals(companyId)){
                    //通过其它服务公司转包到当前服务公司下发的数据
                    //更正商户名称和服务公司名称
                    String companyName = (String) var.get("companyName");
                    String realCompanyName = (String) var.get("realCompanyName");
                    var.put("customName",companyName);
                    var.put("companyName",realCompanyName);
                }
            }
        }
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 批次交易结果查询(超管)--列表
     * Date 17:08 2018/12/19
     * Param [request, response]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @RequestMapping(value = "/resultQueryByBatchForRoot", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> resultQueryByBatchForRoot(HttpServletRequest request,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }

        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(20);
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        //服务公司
        String customkey = request.getParameter("recCustomkey");
        //商户名称
        String customName = request.getParameter("customName");
        //批次名称
        String batchName = request.getParameter("batchName");
        //批次说明
        String batchDesc = request.getParameter("batchDesc");
        //项目名称
        String contentName = request.getParameter("contentName");
        //下发通道
        String payType = request.getParameter("payType");
        if ("0".equals(payType)) {
            payType = null;
        }
        //订单状态
        String status = request.getParameter("status");
        //开始时间
        String submitTimeStart = request.getParameter("submitTimeStart");
        //结束时间
        String submitTimeEnd = request.getParameter("submitTimeEnd");
        //到账时间区间——开始
        String completeTimeStart = request.getParameter("completeTimeStart");
        //到账时间区间——结束
        String completeTimeEnd = request.getParameter("completeTimeEnd");
        String amount = request.getParameter("amount");
        //批次总金额
        amount = ArithmeticUtil.formatDecimals(amount);
        //文件名称
        String fileName = request.getParameter("fileName");
        Map<String, Object> param = new HashMap<>(20);
        try {
            param.put("customkey", customkey);
            param.put("customName", customName);
            param.put("batchName", batchName);
            param.put("batchDesc", batchDesc);
            param.put("contentName", contentName);
            param.put("payType", payType);
            param.put("status", status);
            param.put("submitTimeStart", submitTimeStart);
            param.put("submitTimeEnd", submitTimeEnd);
            param.put("completeTimeStart", completeTimeStart);
            param.put("completeTimeEnd", completeTimeEnd);
            param.put("batchAmount", amount);
            param.put("fileName", fileName);
            PageHelper.startPage(pageNo, pageSize);
            List<Map<String, Object>> list = channelHistoryService.batchResultQueryByCompany(param);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
            result.put("list", pageInfo.getList());
            result.put("total", pageInfo.getTotal());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "查询失败");
            return result;
        }
        return result;
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 批次交易结果查询(超管通用)--明细列表
     * Date 17:08 2018/12/19
     * Param [request, response]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @RequestMapping(value = "/resultQueryByBatchDetail", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> resultQueryByBatchDetail(HttpServletRequest request, HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(5);
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        String batchId = request.getParameter("batchId");
        String userName = request.getParameter("userName");
        String amount = request.getParameter("amount");
        amount = ArithmeticUtil.formatDecimals(amount);
        String certId = request.getParameter("certId");
        String account = request.getParameter("account");
        String status = request.getParameter("status");
//        String pageNo = request.getParameter("pageNo");
        if (StringUtil.isEmpty(batchId)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "参数异常！");
            return result;
        }
        try {
            Map<String, Object> param = new HashMap<>(15);
            param.put("userName", userName);
            param.put("amount", amount);
            param.put("certId", certId);
            param.put("account", account);
            param.put("status", status);
            param.put("batchId", batchId);
//            int total = commissionService.commissionResultQuery(param).size();
//            int pageSize = 10;
//            if (StringUtil.isEmpty(pageNo)) {
//                pageNo = "1";
//            }
//            param.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//            param.put("limit", pageSize);
            PageHelper.startPage(pageNo, pageSize);
            List<UserCommission> list = commissionService.commissionResultQuery(param);
            PageInfo<UserCommission> pageInfo = new PageInfo<>(list);

            ChannelHistory history = channelHistoryService.getChannelHistoryById(batchId);
            result.put("batchData", history);
            result.put("list", pageInfo.getList());
            result.put("total", pageInfo.getTotal());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "查询失败");
            return result;
        }
        return result;
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 商户交易统计
     * Date 17:08 2018/12/19
     * Param [request, customName, tradeStartTime, tradeEndTime, payType, batchName, batchDesc, companyId, amountUpperLimit, amountLowerLimit, payAmountLevel, contentName, pageNo, pageSize]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @RequestMapping(value = "/custom/trade/countData")
    public @ResponseBody
    Map<String, Object> countData(HttpServletRequest request,
                                  String customName,
                                  String tradeStartTime,
                                  String tradeEndTime,
                                  String payType,
                                  String batchName,
                                  String batchDesc,
                                  String companyId,
                                  String amountUpperLimit,
                                  String amountLowerLimit,
                                  String payAmountLevel,
                                  String contentName,
                                  String pageNo,
                                  String pageSize) {
        String company = (String) request.getSession().getAttribute("customkey");
        ChannelCustom customByCustomkey = customService.getCustomByCustomkey(company);
        if (customByCustomkey.getCustomType() != CustomType.COMPANY.getCode()) {
            return returnFail(RespCode.error101, RespCode.ROLE_ERROR);
        }
        logger.info("商户交易统计customkey:" + company);
        Map<String, String> map = new HashMap<>(20);
        map.put("companyId", company);
        map.put("customName", customName);
        map.put("tradeStartTime", tradeStartTime);
        map.put("tradeEndTime", tradeEndTime);
        map.put("payType", payType);
        map.put("batchName", batchName);
        map.put("batchDesc", batchDesc);
        map.put("payCompanyId", companyId);
        map.put("calculationLimit", baseInfo.getCalculationLimit());
        map.put("amountUpperLimit", amountUpperLimit);
        map.put("amountLowerLimit", amountLowerLimit);
//      payAmountLevel   大于2.8W  1,   小于2.8W  -1
        map.put("payAmountLevel", payAmountLevel);
        map.put("contentName", contentName);
        int total = commissionService.getCommissionsByParams(map).size();
        if (!StringUtil.isNumber(pageSize)) {
            pageSize = "10";
        }
        if (!StringUtil.isNumber(pageNo)) {
            pageNo = "1";
        }
        map.put("pageSize", pageSize);
        map.put("page", pageNo);
        List<Map> resultMap = commissionService.getCommissionsByParams(map);
        Map<String, Object> hashMap = new HashMap<>(4);
        hashMap.put("total", total);
        hashMap.put("resultMap", resultMap);
        return result(hashMap);
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 商户交易统计
     * Date 17:09 2018/12/19
     * Param [request, response, customName, tradeStartTime, tradeEndTime, payType, batchName, batchDesc, payCompanyId, amountUpperLimit, amountLowerLimit, payAmountLevel, contentName]
     * return void
     **/
    @RequestMapping(value = "/custom/trade/countData/excel")
    public void countDataExcel(HttpServletRequest request, HttpServletResponse response, String customName, String tradeStartTime, String tradeEndTime, String payType, String batchName, String batchDesc, String payCompanyId, String amountUpperLimit, String amountLowerLimit, String payAmountLevel, String contentName) {
        String companyId = (String) request.getSession().getAttribute("customkey");
        logger.info("商户交易统计customkey:" + companyId);
        Map<String, String> map = new HashMap<>(20);
        map.put("companyId", companyId);
        map.put("customName", customName);
        map.put("tradeStartTime", tradeStartTime);
        map.put("tradeEndTime", tradeEndTime);
        map.put("payType", payType);
        map.put("batchName", batchName);
        map.put("batchDesc", batchDesc);
        map.put("payCompanyId", payCompanyId);
        map.put("calculationLimit", baseInfo.getCalculationLimit());
        map.put("amountUpperLimit", amountUpperLimit);
        map.put("amountLowerLimit", amountLowerLimit);
//      payAmountLevel   大于2.8W  1,   小于2.8W  -1
        map.put("payAmountLevel", payAmountLevel);
        map.put("contentName", contentName);
        List<Map> resultMap = commissionService.getCommissionsByParams(map);
        List<Map<String, Object>> data = new ArrayList<>();
        String filename = "商户交易统计";
        String[] colunmName = new String[]{"商户名称", "成功交易总金额", "成功交易总笔数", "总服务费", "月累计下发金额超过2.8万人数", "月累计下发金额超过2.8万总金额"};
        for (Map<String, Object> objectMap : resultMap) {

            Map<String, Object> dataMap = new HashMap<>(10);
            dataMap.put("1", objectMap.get("customName"));
            dataMap.put("2", objectMap.get("amountSum"));
            dataMap.put("3", objectMap.get("countId"));
            dataMap.put("4", objectMap.get("sumFee"));
            dataMap.put("5", objectMap.get("countMore28000UserId"));
            dataMap.put("6", objectMap.get("amountMore28000Sum"));
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);

    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 用户交易统计明细
     * Date 17:09 2018/12/19
     * Param [request, customName, tradeStartTime, tradeEndTime, payType, batchName, batchDesc, payCompanyId, amountUpperLimit, amountLowerLimit, payAmountLevel, contentName, pageNo, pageSize]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @RequestMapping(value = "/custom/trade/countData/Detail")
    public @ResponseBody
    Map<String, Object> countDataDetail(HttpServletRequest request, String customName, String tradeStartTime, String tradeEndTime, String payType, String batchName, String batchDesc, String payCompanyId, String amountUpperLimit, String amountLowerLimit, String payAmountLevel, String contentName, String pageNo, String pageSize) {
        String companyId = (String) request.getSession().getAttribute("customkey");
        ChannelCustom customByCustomkey = customService.getCustomByCustomkey(companyId);
        if (customByCustomkey.getCustomType() != CustomType.COMPANY.getCode()) {
            return returnFail(RespCode.error101, RespCode.ROLE_ERROR);
        }
        logger.info("商户交易统计customkey:" + companyId);
        Map<String, String> map = new HashMap<String, String>(20);
        map.put("companyId", companyId);
        map.put("customName", customName);
        map.put("tradeStartTime", tradeStartTime);
        map.put("tradeEndTime", tradeEndTime);
        map.put("payType", payType);
        map.put("batchName", batchName);
        map.put("batchDesc", batchDesc);
        map.put("payCompanyId", payCompanyId);
        map.put("calculationLimit", baseInfo.getCalculationLimit());
        map.put("amountUpperLimit", amountUpperLimit);
        map.put("amountLowerLimit", amountLowerLimit);
//      payAmountLevel   大于2.8W  1,   小于2.8W  -1
        map.put("payAmountLevel", payAmountLevel);
        map.put("contentName", contentName);
        if (!StringUtil.isNumber(pageSize)) {
            pageSize = "10";
        }
        if (!StringUtil.isNumber(pageNo)) {
            pageNo = "1";
        }
        int page = getFirst(pageNo, pageSize);
        int count = commissionService.getCommissionsDetailByParams(map).size();
        map.put("pageSize", pageSize);
        map.put("page", page + "");
        List<UserCommission> list = commissionService.getCommissionsDetailByParams(map);
        Map<String, Object> hashMap = new HashMap<>(4);
        hashMap.put("total", count);
        hashMap.put("list", list);
        return result(hashMap);
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 用户交易统计明细
     * Date 17:09 2018/12/19
     * Param [request, response, customName, tradeStartTime, tradeEndTime, payType, batchName, batchDesc, payCompanyId, amountUpperLimit, amountLowerLimit, payAmountLevel, contentName]
     * return void
     **/
    @RequestMapping(value = "/custom/trade/countData/Detail/excel")
    public void countDataDetailExcel(HttpServletRequest request, HttpServletResponse response, String customName, String tradeStartTime, String tradeEndTime, String payType, String batchName, String batchDesc, String payCompanyId, String amountUpperLimit, String amountLowerLimit, String payAmountLevel, String contentName) {
        String companyId = (String) request.getSession().getAttribute("customkey");
        logger.info("商户交易统计customkey:" + companyId);
        Map<String, String> map = new HashMap<>(20);
        map.put("companyId", companyId);
        map.put("customName", customName);
        map.put("tradeStartTime", tradeStartTime);
        map.put("tradeEndTime", tradeEndTime);
        map.put("payType", payType);
        map.put("batchName", batchName);
        map.put("batchDesc", batchDesc);
        map.put("payCompanyId", payCompanyId);
        map.put("calculationLimit", baseInfo.getCalculationLimit());
        map.put("amountUpperLimit", amountUpperLimit);
        map.put("amountLowerLimit", amountLowerLimit);
//      payAmountLevel   大于2.8W  1,   小于2.8W  -1
        map.put("payAmountLevel", payAmountLevel);
        map.put("contentName", contentName);
        List<UserCommission> list = commissionService.getCommissionsDetailByParams(map);
        String filename = "用户交易统计明细";
        String[] colunmName = new String[]{"订单ID", "收款人姓名", "证件类型", "证件号","手机号", "收款账号", "交易金额"
                , "服务费率", "服务费(包含补差价)", "补差价交易金额", "补差价服务费", "服务费计算规则","订单状态", "订单状态描述", "订单备注"
                , "账号所属金融机构", "下发通道", "服务公司", "交易时间", "项目名称", "批次名称", "批次说明"
                , "最后更新时间"};
        List<Map<String, Object>> data = new ArrayList<>();
        for (UserCommission userCommission : list) {
            Map<String, Object> dataMap = new HashMap<>(25);
            //订单ID
            dataMap.put("1", userCommission.getOrderNo());
            //收款人姓名
            dataMap.put("2", userCommission.getUserName());
            //证件类型
            dataMap.put("3", CertType.codeOf(userCommission.getDocumentType()).getDesc());
            //证件号
            dataMap.put("4", userCommission.getCertId());
            dataMap.put("5", userCommission.getPhoneNo());
            //收款账号
            dataMap.put("6", userCommission.getAccount());
            //交易金额
            dataMap.put("7", userCommission.getAmount());
            //服务费率
            dataMap.put("8", userCommission.getCalculationRates());
            //服务费(包含补差价)
            dataMap.put("9", userCommission.getSumFee());
            //补差价交易金额
            dataMap.put("10", userCommission.getSupplementAmount());
            //补差价服务费
            dataMap.put("11", userCommission.getSupplementFee());
            //订单状态
            dataMap.put("12", userCommission.getFeeRuleType());
            dataMap.put("13", CommissionStatus.codeOf(userCommission.getStatus()).getDesc());
            //订单状态描述
            dataMap.put("14", userCommission.getStatusDesc());
            //订单备注
            dataMap.put("15", userCommission.getRemark());
            //账号所属金融机构
            dataMap.put("16", userCommission.getBankName());
            //下发通道
            dataMap.put("17", PayType.codeOf(userCommission.getPayType()).getDesc());
            dataMap.put("18", userCommission.getCompanyName());
            dataMap.put("19", userCommission.getPaymentTime());
            dataMap.put("20", userCommission.getContentName());
            dataMap.put("21", userCommission.getBatchName());
            dataMap.put("22", userCommission.getBatchDesc());
            dataMap.put("23", userCommission.getUpdatetime());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    private Map<String, Object> result(Object obj) {
        Map<String, Object> model = new HashMap<>(8);
        model.put(RespCode.RESP_STAT, RespCode.success);
        model.put("result", obj);
        return model;
    }
}
