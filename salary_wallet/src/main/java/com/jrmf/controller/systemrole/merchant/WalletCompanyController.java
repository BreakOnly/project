package com.jrmf.controller.systemrole.merchant;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.jrmf.common.APIResponse;
import com.jrmf.common.CommonString;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.*;
import com.jrmf.domain.*;
import com.jrmf.domain.vo.CompanyAccountVo;
import com.jrmf.persistence.CustomGroupDao;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.service.*;
import com.jrmf.taxsettlement.api.APIDockingManager;
import com.jrmf.taxsettlement.api.security.sign.SignWorkers;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.CipherUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.exception.BalanceException;
import com.jrmf.utils.ftp.FTPClientPool;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.jms.Destination;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/wallet/company")
public class WalletCompanyController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(WalletCompanyController.class);

    protected final UserSerivce userSerivce;
    protected final CustomInfoService customInfoService;
    private final UserCommissionService commissionService;
    private final ChannelHistoryService channelHistoryService;
    private final ChannelInvoiceService channelInvoiceService;
    private final ChannelCustomService customService;
    private final CustomBalanceService customBalanceService;
    private final JmsTemplate providerJmsTemplate;
    private Destination rechargeRequestDestination;
    private APIDockingManager apiDockingManager;
    private SignWorkers signWorkers;
    private CustomProxyService customProxyService;
    private final OrganizationTreeService organizationTreeService;

    @Autowired
    public WalletCompanyController(UserSerivce userSerivce, CustomInfoService customInfoService, UserCommissionService commissionService, ChannelHistoryService channelHistoryService, ChannelInvoiceService channelInvoiceService, ChannelCustomService customService, CustomBalanceService customBalanceService, JmsTemplate providerJmsTemplate, Destination rechargeRequestDestination, APIDockingManager apiDockingManager, SignWorkers signWorkers, CustomProxyService customProxyService, OrganizationTreeService organizationTreeService) {
        this.userSerivce = userSerivce;
        this.customInfoService = customInfoService;
        this.commissionService = commissionService;
        this.channelHistoryService = channelHistoryService;
        this.channelInvoiceService = channelInvoiceService;
        this.customService = customService;
        this.customBalanceService = customBalanceService;
        this.providerJmsTemplate = providerJmsTemplate;
        this.rechargeRequestDestination = rechargeRequestDestination;
        this.apiDockingManager = apiDockingManager;
        this.signWorkers = signWorkers;
        this.customProxyService = customProxyService;
        this.organizationTreeService = organizationTreeService;
    }

    @Autowired
    private CustomReceiveConfigService customReceiveConfigService;
    @Autowired
    private CustomTransferRecordService transferRecordService;
    @Autowired
    private CustomBalanceService balanceService;
    @Autowired
    private UserCommissionService userCommissionService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private CustomProxyDao customProxyDao;
    @Autowired
    private ChannelCustomService channelCustomService;
    @Autowired
    private CustomGroupDao customGroupDao;
    @Autowired
    private ProxyCostMaintainService proxyCostMaintainService;
    @Autowired
    private CompanyNetfileRateConfService companyNetfileRateConfService;
    @Autowired
    private ChannelRelatedService channelRelatedService;
    @Autowired
    private BestSignConfig bestSignConfig;
    @Autowired
    private FTPClientPool ftpClientPool;
    @Autowired
    private ForwardCompanyAccountService forwardCompanyAccountService;
    @Autowired
    private ForwardCompanyAccountHistoryService forwardCompanyAccountHistoryService;


    /**
     * ??????????????????
     */
    @RequestMapping(value = "/receipt/affirmList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> channelAffirmList(HttpServletRequest request,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>(5);
        //????????????????????????
        String customkey = (String) request.getSession().getAttribute("customkey");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
//        String pageNo = request.getParameter("pageNo");
        String amount = request.getParameter("amount");
        String payType = request.getParameter("payType");
        String name = request.getParameter("name");
        String status = request.getParameter("status");
        logger.info("/company/affirmList??????  ????????? customkey=" + customkey);
        if (StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<>(15);
                paramMap.put("recCustomkey", customkey);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                //??????????????? ?????????
                paramMap.put("transfertype", 1);
                paramMap.put("payType", payType);
                paramMap.put("amount", amount);
                paramMap.put("status", status);
                paramMap.put("name", name);

//                int total = channelHistoryService.getChannelHistoryList(paramMap).size();
//                int pageSize = 10;
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<ChannelHistory> list = channelHistoryService.getChannelHistoryList(paramMap);
                PageInfo<ChannelHistory> pageInfo = new PageInfo<>(list);
                model.put("list", pageInfo.getList());
                model.put("total", pageInfo.getTotal());
            } catch (Exception e) {
                respstat = RespCode.error107;
                model.put(RespCode.RESP_STAT, respstat);
                model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
                logger.error(e.getMessage(), e);
                return model;
            }
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + model);
        return model;
    }

    /**
     * Author Nicholas-Ning
     * Description //??????????????????
     * Date 10:43 2018/11/14
     * Param [model, startTime, endTime, amount, payType, request, response]
     * return void
     **/
    @RequestMapping(value = "/custom/exportCustomData")
    public void exportCustomManage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String customkey = (String) request.getSession().getAttribute("customkey");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String amount = request.getParameter("amount");
        String payType = request.getParameter("payType");
        String name = request.getParameter("name");
        String status = request.getParameter("status");
        Map<String, Object> paramMap = new HashMap<>(15);
        paramMap.put("recCustomkey", customkey);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        //??????????????? ?????????
        paramMap.put("transfertype", 1);
        paramMap.put("payType", payType);
        paramMap.put("amount", amount);
        paramMap.put("status", status);
        paramMap.put("name", name);
        List<ChannelHistory> list = channelHistoryService.getChannelHistoryList(paramMap);
        String today = DateUtils.getNowDay();
        ArrayList<String> dataStr = new ArrayList<>();
        for (ChannelHistory channelHistory : list) {
            StringBuilder strBuff = new StringBuilder();
            strBuff.append(formartEmptyString(channelHistory.getCompanyName()))
                    .append(",").append(formartEmptyString(channelHistory.getAccountName()))
                    .append(",").append(formartEmptyString(channelHistory.getAccountNo()))
                    .append(",").append(formartEmptyString(channelHistory.getAmount()))
                    .append(",").append(formartEmptyString(getPayStatus("", channelHistory.getPayType())))
                    .append(",").append(formartEmptyString(channelHistory.getRemark()))
                    .append(",").append(formartEmptyString(HistoryStatus.codeOf(channelHistory.getStatus()).getDesc()))
                    .append(",").append(formartEmptyString(channelHistory.getUpdatetime()))
                    .append(",").append(formartEmptyString(channelHistory.getOperatorName()));
            dataStr.add(strBuff.toString());
        }
        ArrayList<String> fieldName = new ArrayList<>();
        fieldName.add("????????????");
        fieldName.add("????????????");
        fieldName.add("????????????");
        fieldName.add("????????????");
        fieldName.add("????????????");
        fieldName.add("??????");
        fieldName.add("??????");
        fieldName.add("????????????");
        fieldName.add("????????????");
        String filename = today + "??????????????????";
        ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
    }

    private String formartEmptyString(String str) {
        return StringUtil.isEmpty(str) ? "" : str;
    }

    private String getPayStatus(String payStatus, int pay) {
        if (pay == 1) {
            payStatus = "???????????????";
        } else if (pay == 2) {
            payStatus = "?????????";
        } else if (pay == 4) {
            payStatus = "?????????";
        }
        return payStatus;
    }

    /**
     * ?????????????????????
     */
//    @RequestMapping(value = "/receipt/confirm", method = RequestMethod.POST)
//    public @ResponseBody
//    Map<String, Object> channelAffirm(HttpServletRequest request) {
//        int respstat = RespCode.success;
//        Map<String, Object> model = new HashMap<>(10);
//        //????????????????????????
//        String customkey = (String) request.getSession().getAttribute("customkey");
//        //???????????????Id
//        String id = request.getParameter("id");
//        //????????????
//        String tranPassword = request.getParameter("tranPassword");
//        String status = request.getParameter("status");
//        ChannelCustom loginUser = (ChannelCustom) request.getSession()
//                .getAttribute("customLogin");
//        logger.info("/channel/affirm??????  ????????? customkey=" + customkey);
//        if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(id)
//                || StringUtil.isEmpty(tranPassword) || StringUtil.isEmpty(status)
//                || StringUtil.isEmpty(loginUser.getUsername())) {
//            respstat = RespCode.error101;
//            model.put(RespCode.RESP_STAT, respstat);
//            model.put(RespCode.RESP_MSG, "??????????????????");
//            return model;
//        } else {
//            //????????????
//            if (!StringUtil.isEmpty(tranPassword)) {
//                if (loginUser.getTranPassword() == null
//                        || !loginUser.getTranPassword().equals(
//                        CipherUtil.generatePassword(tranPassword, customkey))) {
//                    respstat = RespCode.error101;
//                    model.put(RespCode.RESP_STAT, respstat);
//                    model.put(RespCode.RESP_MSG, "?????????????????????");
//                    return model;
//                }
//            }
//            try {
//                ChannelHistory history = channelHistoryService.getChannelHistoryById(id);
//                if (history == null) {
//                    respstat = RespCode.error105;
//                    model.put(RespCode.RESP_STAT, respstat);
//                    model.put(RespCode.RESP_MSG, "???????????????");
//                    return model;
//                }
//                history.setOperatorName(loginUser.getUsername());
//                history.setUpdatetime(DateUtils.getNowDate());
//                //?????? ???????????? ?????? ????????????
//                history.setStatus(Integer.parseInt(status));
//                channelHistoryService.updateChannelHistory(history);
//                if (history.getStatus() == 1) {
//                    Map<String, Object> param = new HashMap<>(10);
//                    param.put("customkey", history.getCustomkey());
//                    param.put("companyId", history.getRecCustomkey());
//                    param.put("payType", history.getPayType());
//                    param.put("amount", new BigDecimal(history.getAmount()).multiply(new BigDecimal(100)).intValue());
//                    customBalanceService.updateBalance(param);
//                }
//            } catch (Exception e) {
//                respstat = RespCode.error107;
//                model.put(RespCode.RESP_STAT, respstat);
//                model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
//                logger.error(e.getMessage(), e);
//                return model;
//            }
//        }
//        model.put(RespCode.RESP_STAT, respstat);
//        model.put(RespCode.RESP_MSG, "??????");
//        logger.info("???????????????" + model);
//        return model;
//    }


    /**
     * ??????????????????-??????????????????
     */
    @RequestMapping(value = "/batch/listData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> channelHistory(HttpServletRequest request,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");//????????????????????????
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
//        String pageNo = request.getParameter("pageNo");
        String status = request.getParameter("status");
        String name = request.getParameter("name");
        String payType = request.getParameter("payType");
        if (StringUtil.isEmpty(status) && StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("recCustomkey", customkey);
                if (!StringUtil.isEmpty(status)) {
                    paramMap.put("status", status);
                }
                paramMap.put("transfertype", 2);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                paramMap.put("name", name);
                paramMap.put("payType", payType);
//                int total = channelHistoryService.getChannelHistoryByCompany(paramMap).size();
//                int pageSize = 10;
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<ChannelHistory> list = channelHistoryService.getChannelHistoryByCompany(paramMap);
                PageInfo<ChannelHistory> pageInfo = new PageInfo<>(list);
                model.put("list", pageInfo.getList());
                model.put("total", pageInfo.getTotal());
            } catch (Exception e) {
                respstat = RespCode.error107;
                model.put(RespCode.RESP_STAT, respstat);
                model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
                logger.error(e.getMessage(), e);
                return model;
            }
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + model);
        return model;
    }

    /**
     * ??????????????????-??????????????????????????????
     */
    @RequestMapping(value = "/batch/datailData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> commissionList(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");
        String batchId = request.getParameter("batchId");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(batchId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                ChannelHistory history = channelHistoryService.getChannelHistoryById(batchId);
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("batchId", batchId);
                paramMap.put("status", status);
                List<UserCommission> list = commissionService.getUserCommissionByParam(paramMap);
                model.put("list", list);
                model.put("history", history);
            } catch (Exception e) {
                respstat = RespCode.error107;
                model.put(RespCode.RESP_STAT, respstat);
                model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
                logger.error(e.getMessage(), e);
                return model;
            }
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + model);
        return model;
    }

    /**
     * ????????????????????????
     *
     * @param startTime
     * @param endTime
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/custom/exportBatchData")
    public void exportBatchData(String startTime, String endTime, String status,
                                String name, String payType, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String customkey = (String) request.getSession().getAttribute("customkey");// ????????????
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("recCustomkey", customkey);
        if (!StringUtil.isEmpty(status)) {
            paramMap.put("status", status);
        }
        paramMap.put("transfertype", 2);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("name", name);
        paramMap.put("payType", payType);
        List<ChannelHistory> list = channelHistoryService.getChannelHistoryByCompany(paramMap);

        String today = DateUtils.getNowDay();
        ArrayList<String> dataStr = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ChannelHistory channelHistory = list.get(i);
            StringBuffer strBuff = new StringBuffer();
            String result = "";
            String payStatus = "";
            int pay = channelHistory.getPayType();
            payStatus = getPayStatus(payStatus, pay);
            //0 ?????????       1 ????????????  2 ????????????  3 ????????? 4 ?????? 5 ????????????  6 ?????? 7 ?????????
            int type = channelHistory.getStatus();
            if (type == 1) {
                result = "????????????";
            } else if (type == 2) {
                result = "????????????";
            } else if (type == 3) {
                result = "?????????,????????????";
            } else if (type == 5) {
                result = "????????????";
            } else if (type == 6) {
                result = "??????";
            }
            strBuff.append(channelHistory.getCompanyName() == null ? ""
                    : channelHistory.getCompanyName())
                    .append(",")
                    .append(channelHistory.getId())
                    .append(",")
                    .append(payStatus)
                    .append(",")
                    .append(channelHistory.getPassNum())
                    .append(",")
                    .append(channelHistory.getAmount())
                    .append(",")
                    .append(channelHistory.getServiceFee())
                    .append(",")
                    .append(channelHistory.getHandleAmount())
                    .append(",")
                    .append(channelHistory.getCreatetime())
                    .append(",")
                    .append(channelHistory.getUpdatetime() == null ? ""
                            : channelHistory.getUpdatetime())
                    .append(",")
                    .append(result);

            dataStr.add(strBuff.toString());
        }
        ArrayList<String> fieldName = new ArrayList<>();
        fieldName.add("??????");
        fieldName.add("?????????");
        fieldName.add("????????????");
        fieldName.add("??????????????????");
        fieldName.add("??????????????????");
        fieldName.add("?????????");
        fieldName.add("??????????????????");
        fieldName.add("????????????");
        fieldName.add("????????????");
        fieldName.add("??????");
        String filename = today + "??????????????????";
        ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/user/commissionData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> commissionDetail(HttpServletRequest request,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");//????????????????????????
        String name = request.getParameter("name");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
//        String pageNo = request.getParameter("pageNo");
        logger.info("/user/commissionData ??????  ????????? customkey=" + customkey);
        if (StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("companyId", customkey);
                paramMap.put("name", name);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
//                int total = commissionService.getUserCommissionedByParam(paramMap).size();
//                int pageSize = 10;
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<UserCommission> list = commissionService.getUserCommissionedByParam(paramMap);
                PageInfo<UserCommission> pageInfo = new PageInfo<>(list);
                model.put("list", pageInfo.getList());
                model.put("total", pageInfo.getTotal());
                model.put("pageNo", pageNo);
            } catch (Exception e) {
                respstat = RespCode.error107;
                model.put(RespCode.RESP_STAT, respstat);
                model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
                logger.error(e.getMessage(), e);
                return model;
            }
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + model);
        return model;
    }

    /**
     * ????????????--????????????
     */
    @RequestMapping(value = "/user/commissionSumData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> commissionSumData(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");//????????????????????????
        String name = request.getParameter("name");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        logger.info("/user/commissionSumData ??????  ????????? customkey=" + customkey);
        if (StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("companyId", customkey);
                paramMap.put("name", name);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                paramMap.put("status", 1);
                //???????????????????????????
                String successAmount = commissionService.getUserCommissionSum(paramMap);
                paramMap.put("status", 3);
                //???????????????????????????
                String waitAmount = commissionService.getUserCommissionSum(paramMap);
                paramMap.put("status", 2);
                //???????????????????????????
                String failureAmount = commissionService.getUserCommissionSum(paramMap);
                model.put("successAmount", successAmount == null ? "0.00" : successAmount);
                model.put("waitAmount", waitAmount == null ? "0.00" : waitAmount);
                model.put("failureAmount", failureAmount == null ? "0.00" : failureAmount);
            } catch (Exception e) {
                respstat = RespCode.error107;
                model.put(RespCode.RESP_STAT, respstat);
                model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
                logger.error(e.getMessage(), e);
                return model;
            }
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + model);
        return model;
    }

    /**
     * ????????????--??????
     *
     * @throws Exception
     */
    @RequestMapping(value = "/user/commissionDataExport")
    public void commissionDataExport(HttpServletRequest request,
                                     HttpServletResponse response) throws Exception {
        String customkey = (String) request.getSession().getAttribute("customkey");//????????????????????????
        String name = request.getParameter("name");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String status = request.getParameter("status");
        logger.info("/user/commissionData ??????  ????????? customkey=" + customkey);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("companyId", customkey);
        paramMap.put("name", name);
        paramMap.put("status", status);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        List<UserCommission> list = commissionService.getUserCommissionByParam(paramMap);
        String today = DateUtils.getNowDay();
        ArrayList<String> dataStr = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            UserCommission commission = list.get(i);
            StringBuffer strBuff = new StringBuffer();

            strBuff.append(commission.getCustomName() == null ? ""
                    : commission.getCustomName())
                    .append(",")
                    .append(commission.getCreatetime() == null ? ""
                            : commission.getCreatetime())
                    .append(",")
                    .append(commission.getBatchId() == null ? ""
                            : commission.getBatchId())
                    .append(",")
                    .append(commission.getOrderNo() == null ? ""
                            : commission.getOrderNo())
                    .append(",")
                    .append(commission.getUserName() == null ? ""
                            : commission.getUserName())
                    .append(",")
                    .append(commission.getStatusDesc() == null ? ""
                            : commission.getStatusDesc())
                    .append(",")
                    .append(commission.getAmount() == null ? ""
                            : commission.getAmount())
                    .append(",")
                    .append(commission.getUpdatetime() == null ? ""
                            : commission.getUpdatetime());

            dataStr.add(strBuff.toString());
        }
        ArrayList<String> fieldName = new ArrayList<>();
        fieldName.add("????????????");
        fieldName.add("????????????");
        fieldName.add("?????????");
        fieldName.add("?????????");
        fieldName.add("??????");
        fieldName.add("??????");
        fieldName.add("????????????");
        fieldName.add("????????????");
        String filename = today + "????????????";
        ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
    }

    /**
     * ????????? ??????????????????
     */
    @RequestMapping(value = "/invoice/applicationData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> applyInvoiceList(HttpServletRequest request,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>(5);
        //??????
        String recCustomkey = (String) request.getSession().getAttribute("customkey");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        //????????????
        String status = request.getParameter("status");
//        String pageNo = request.getParameter("pageNo");
        String invoiceCompanyName = request.getParameter("invoiceCompanyName");
        logger.info("/channel/applyInvoiceList??????  ????????? customkey=" + recCustomkey);
        if (StringUtil.isEmpty(recCustomkey)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<>(10);
                paramMap.put("reCustomkey", recCustomkey);
                paramMap.put("status", status);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                paramMap.put("invoiceCompanyName", invoiceCompanyName);
//                int total = channelInvoiceService.getChannelInvoiceByParam(paramMap).size();
//                int pageSize = 10;
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<ChannelInvoice> invoiceList = channelInvoiceService.getChannelInvoiceByParam(paramMap);
                PageInfo<ChannelInvoice> pageInfo = new PageInfo<>(invoiceList);
                model.put("invoiceList", pageInfo.getList());
                model.put("total", pageInfo.getTotal());
                model.put("pageNo", pageNo);
            } catch (Exception e) {
                respstat = RespCode.error107;
                model.put(RespCode.RESP_STAT, respstat);
                model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
                logger.error(e.getMessage(), e);
                return model;
            }
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + model);
        return model;
    }

    /**
     * ?????????????????????????????????
     */
    @RequestMapping(value = "/invoice/operating", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> openInvoice(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");//??????
        String id = request.getParameter("id");
        String remark = request.getParameter("remark");//??????
        String status = request.getParameter("status");//??????
        String tranPassword = request.getParameter("tranPassword");//????????????
        String invoiceNum = request.getParameter("invoiceNum");//????????????
        ChannelCustom loginUser = (ChannelCustom) request.getSession()
                .getAttribute("customLogin");
        String operatorName = loginUser.getUsername();//?????????
        logger.info("/channel/openInvoice??????  ????????? customkey=" + customkey);
        if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(id) || StringUtil.isEmpty(status)
                || StringUtil.isEmpty(operatorName) || StringUtil.isEmpty(tranPassword)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                /**
                 * ??????????????????
                 */
                if (loginUser.getTranPassword() != null && !loginUser.getTranPassword().equals(CipherUtil.generatePassword(tranPassword, customkey))) {
                    respstat = RespCode.error101;
                    model.put(RespCode.RESP_STAT, respstat);
                    model.put(RespCode.RESP_MSG, "?????????????????????");
                    return model;
                }
                //????????????????????? ?????????
                Map<String, Object> paramMap = new HashMap<>();
                if ("2".equals(status)) {
                    //commissionService
                    ChannelInvoice channelInvoiceById = channelInvoiceService.getChannelInvoiceById(id);
                    Map<String, Object> param = new HashMap<>();
                    param.put("invoiceBatchNo", channelInvoiceById.getOrderno());
                    param.put("invoiceStatus", 1);
                    List<UserCommission> userCommissionByParam = commissionService.getUserCommissionByParam(param);
                    for (UserCommission userCommission : userCommissionByParam) {
                        userCommission.setInvoiceStatus(2);
                        commissionService.updateUserCommission(userCommission);
                    }
                }
                paramMap.put("status", status);
                paramMap.put("id", id);
                paramMap.put("remark", remark);
                paramMap.put("invoiceNum", invoiceNum);
                channelInvoiceService.updateChannelInvoice(paramMap);

            } catch (Exception e) {
                respstat = RespCode.error107;
                model.put(RespCode.RESP_STAT, respstat);
                model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
                logger.error(e.getMessage(), e);
                return model;
            }
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + model);
        return model;
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = "/invoice/mailing", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> mailing(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");//??????
        String id = request.getParameter("id");
        String courierNo = request.getParameter("courierNo");//???????????????
        String courierCompany = request.getParameter("courierCompany");//?????????
        String status = request.getParameter("status");//??????
        String tranPassword = request.getParameter("tranPassword");//????????????
        ChannelCustom loginUser = (ChannelCustom) request.getSession()
                .getAttribute("customLogin");
        String operatorName = loginUser.getUsername();//?????????
        if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(id) || StringUtil.isEmpty(courierNo)
                || StringUtil.isEmpty(status) || StringUtil.isEmpty(tranPassword)
                || StringUtil.isEmpty(operatorName)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {

                /**
                 * ??????????????????
                 */
                if (loginUser.getTranPassword() != null && !loginUser.getTranPassword().equals(CipherUtil.generatePassword(tranPassword, customkey))) {
                    respstat = RespCode.error101;
                    model.put(RespCode.RESP_STAT, respstat);
                    model.put(RespCode.RESP_MSG, "?????????????????????");
                    return model;
                }

                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("status", 1);
                paramMap.put("id", id);
                paramMap.put("courierNo", courierNo);
                paramMap.put("operatorName", operatorName);
                paramMap.put("courierCompany", courierCompany);
                channelInvoiceService.updateChannelInvoice(paramMap);

            } catch (Exception e) {
                respstat = RespCode.error107;
                model.put(RespCode.RESP_STAT, respstat);
                model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
                logger.error(e.getMessage(), e);
                return model;
            }
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + model);
        return model;
    }

    /**
     * ????????????????????????????????????
     */
    @RequestMapping(value = "/invoice/getCompanyAccountDetail", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> getCompanyAccountDetail(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");//????????????????????????
        String payType = request.getParameter("payType");
        if (StringUtils.isEmpty(payType)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
            return model;
        }
        ChannelCustom custom = customService.getCustomByCustomkey(customkey);
        if (custom == null) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "???????????????");
            return model;
        }
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("channelId", custom.getId());
        param.put("payType", payType);
        ChannelConfig channelConfig = customService.getChannelConfigByParam(param);
        model.put("channelConfig", channelConfig);
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + model);
        return model;
    }

    /**
     * ???????????????????????????????????????????????????
     */
    @RequestMapping(value = "/invoice/editCompanyAccountDetail", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> editCompanyAccountDetail(HttpServletRequest request, ChannelConfig channelConfig) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");//????????????????????????
        if (channelConfig == null) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, RespCode.codeMaps.get(request));
            return model;
        }
        Map<String, Object> param = new HashMap<>();
        ChannelCustom custom = customService.getCustomByCustomkey(customkey);
        param.put("payType", channelConfig.getPayType());
        param.put("channelId", custom.getId());
        ChannelConfig channelConfigByParam = customService.getChannelConfigByParam(param);
        if (channelConfigByParam == null) {
            channelConfig.setChannelId(custom.getId());
            customService.insertCompanyAccountDetail(channelConfig);
        } else {
            channelConfig.setId(channelConfigByParam.getId());
            channelConfig.setChannelId(custom.getId());
            customService.updateCompanyAccountDetail(channelConfig);
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + model);
        return model;
    }


    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/rechargeList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> recharge(HttpSession session, @RequestParam(required = false) String startTime,
                                 @RequestParam(required = false) String endTime,
                                 @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                 @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) String rechargeAmount,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String payType,
                                 @RequestParam(required = false) String companyId,
                                 @RequestParam(required = false) String customName,
                                 @RequestParam(required = false) String orderNo,
                                 @RequestParam(required = false) Integer rechargeType,
                                 @RequestParam(required = false) Integer rechargeConfirmType,
                                 @RequestParam(required = false) String businessPlatform) {

        ChannelCustom loginUser = (ChannelCustom) session.getAttribute(CommonString.CUSTOMLOGIN);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("companyId", companyId);

        if (!isRootAdmin(loginUser) && (CustomType.COMPANY.getCode() != loginUser.getCustomType())) {
            if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
                ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
                if (CustomType.COMPANY.getCode() == masterCustom.getCustomType()) {
                    loginUser = masterCustom;
                } else {
                    return returnFail(RespCode.error101, "????????????");
                }
            } else {
                return returnFail(RespCode.error101, "????????????");
            }
        }

        //???????????????????????????
        if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
            paramMap.put("companyId", loginUser.getCustomkey());
        }

        Map<String, Object> result = new HashMap<>(5);
        try {
            paramMap.put("startTime", startTime);
            paramMap.put("endTime", endTime);
            paramMap.put("rechargeAmount", rechargeAmount);
            paramMap.put("status", status);
            paramMap.put("payType", payType);
            paramMap.put("customName", customName);
            paramMap.put("orderNo", orderNo);
            paramMap.put("rechargeType", rechargeType);
            paramMap.put("rechargeConfirmType", rechargeConfirmType);
            paramMap.put("businessPlatform", businessPlatform);
            PageHelper.startPage(pageNo, pageSize);
            List<Map<String, Object>> list = channelHistoryService.geCustomChargeDetail(paramMap);
            PageInfo page = new PageInfo(list);
            result.put("total", page.getTotal());
            result.put("list", page.getList());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.codeMaps.get(RespCode.error107));
        }
        return returnSuccess(result);
    }


    /**
     * ????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/rechargeList/export")
    public void rechargeList(@RequestParam(required = false) String startTime,
                             @RequestParam(required = false) String endTime,
                             @RequestParam(required = false) String rechargeAmount,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String payType,
                             @RequestParam(required = false) String companyId,
                             @RequestParam(required = false) String customName,
                             @RequestParam(required = false) String orderNo,
                             @RequestParam(required = false) Integer rechargeType,
                             @RequestParam(required = false) Integer rechargeConfirmType,
                             @RequestParam(required = false) String businessPlatform,
                             HttpServletResponse response, HttpSession session) throws Exception {
        ChannelCustom loginUser = (ChannelCustom) session.getAttribute(CommonString.CUSTOMLOGIN);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("companyId", companyId);


        if (!isRootAdmin(loginUser) && (CustomType.COMPANY.getCode() != loginUser.getCustomType())) {
            if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
                ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
                if (CustomType.COMPANY.getCode() == masterCustom.getCustomType()) {
                    loginUser = masterCustom;
                }
            }
        }

        //???????????????????????????
        if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
            paramMap.put("companyId", loginUser.getCustomkey());
        }

        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("rechargeAmount", rechargeAmount);
        paramMap.put("status", status);
        paramMap.put("payType", payType);
        paramMap.put("customName", customName);
        paramMap.put("orderNo", orderNo);
        paramMap.put("rechargeType", rechargeType);
        paramMap.put("rechargeConfirmType", rechargeConfirmType);
        paramMap.put("businessPlatform", businessPlatform);

        List<Map<String, Object>> list = channelHistoryService.geCustomChargeDetail(paramMap);

        String[] colunmName = new String[]{"????????????", "????????????", "??????", "????????????", "????????????", "????????????",
                "????????????", "????????????", "??????????????????", "????????????", "??????????????????", "?????????????????????", "???????????????", "????????????",
                "????????????????????????", "??????????????????", "????????????", "??????????????????", "??????????????????", "????????????", "??????????????????",
                "??????????????????", "????????????", "?????????????????????", "?????????????????????", "???????????????", "???????????????", "?????????????????????", "??????????????????"};
        String filename = "???????????????";
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : list) {
            Map<String, Object> dataMap = new HashMap<>(25);
            dataMap.put("1", stringObjectMap.get("customName"));
            dataMap.put("2", stringObjectMap.get("orderNo"));
            dataMap.put("3", stringObjectMap.get("createTime"));

            Integer rechargeTypeCode = (Integer) stringObjectMap.get("rechargeType");

            dataMap.put("4", RechargeType.codeOf(rechargeTypeCode).getDesc());
            dataMap.put("5", stringObjectMap.get("rechargeAmount"));
            dataMap.put("6", stringObjectMap.get("amount"));

            String serviceFeeRate = (String) stringObjectMap.get("serviceFeeRate");
            if (!StringUtil.isEmpty(serviceFeeRate)) {
                dataMap.put("7", ArithmeticUtil.mulStr(serviceFeeRate, "100", 2) + "%");
            } else {
                dataMap.put("7", "0%");
            }

            dataMap.put("8", RechargeStatusType.codeOf((Integer) stringObjectMap.get("status")).getDesc());
            dataMap.put("9", PayType.codeOf((Integer) stringObjectMap.get("payType")).getDesc());
            dataMap.put("10", stringObjectMap.get("remark"));
            dataMap.put("11", stringObjectMap.get("realRechargeAmount"));

            if (rechargeTypeCode == RechargeType.AMOUNT.getCode()) {
                dataMap.put("12", ServiceFeeType.codeOf((Integer) stringObjectMap.get("serviceFeeType")).getDesc());
            } else {
                dataMap.put("12", "");
            }

            dataMap.put("13", stringObjectMap.get("serviceFee"));
            dataMap.put("14", stringObjectMap.get("refundAmount"));
            dataMap.put("15", stringObjectMap.get("businessPlatform"));
            dataMap.put("16", stringObjectMap.get("companyName"));
            dataMap.put("17", stringObjectMap.get("inAccountNo"));
            dataMap.put("18", stringObjectMap.get("inAccountBankName"));
            dataMap.put("19", stringObjectMap.get("customName"));
            dataMap.put("20", stringObjectMap.get("payAccountNo"));
            dataMap.put("21", stringObjectMap.get("payAccountBankName"));

            dataMap.put("22", RechargeConfirmType.codeOf((Integer) stringObjectMap.get("rechargeConfirmType")).getDesc());


            String invoiceStatus = stringObjectMap.get("invoiceStatus") + "";
            if (!StringUtil.isEmpty(invoiceStatus)) {
                dataMap.put("23", InvoiceOrderStatus.codeOf(Integer.parseInt(invoiceStatus)).getDesc());
            } else {
                dataMap.put("23", "");
            }
            dataMap.put("24", stringObjectMap.get("invoiceAmount"));
            dataMap.put("25", stringObjectMap.get("unInvoiceAmount"));
            dataMap.put("26", stringObjectMap.get("invoiceingAmount"));

            dataMap.put("27", stringObjectMap.get("operatorName"));
            dataMap.put("28", stringObjectMap.get("companyOperatorName"));
            dataMap.put("29", stringObjectMap.get("updateTime"));
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = "/uploadRechargeFile")
    @ResponseBody
    public Map<String, Object> uploadRechargeFile(HttpServletRequest request,
        MultipartFile[] rechargeFile, String orderNo)
        throws IOException {
        ChannelCustom customLogin = (ChannelCustom) request.getSession()
            .getAttribute("customLogin");
        InputStream in = null;
        if (rechargeFile != null && rechargeFile.length > 0) {
            for (MultipartFile file : rechargeFile) {
                in = new ByteArrayInputStream(file.getBytes());
                //??????UUID???????????????
                String name = UUID.randomUUID().toString().replaceAll("-", "");
                //?????????????????????
                String ext = FilenameUtils.getExtension(file.getOriginalFilename());
                //????????????????????????
                String fileName = name + "." + ext;
                String uploadFile = FtpTool
                    .uploadFile(bestSignConfig.getFtpURL(), 21, "/rechargeFile/", fileName, in,
                        bestSignConfig.getUsername(), bestSignConfig.getPassword());
                if (!"error".equals(uploadFile)) {
                    ChannelHistoryPic channelHistoryPic = new ChannelHistoryPic();
                    channelHistoryPic.setOrderNo(orderNo);
                    channelHistoryPic.setRechargeFile("/rechargeFile/" + fileName);
                    channelHistoryPic.setAddUser(customLogin.getUsername());
                    channelHistoryService.insertChannelHistoryPic(channelHistoryPic);
                    channelHistoryService.updateChannelHistoryFileNumAddByOrderNo(orderNo);
                }
            }
        }
        return returnSuccess();
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = "/deleteRechargeFile")
    @ResponseBody
    public Map<String, Object> deleteRechargeFile (@RequestParam String id)
        throws Exception {
        ChannelHistoryPic channelHistoryPic = channelHistoryService.getChannelHistoryPicById(id);
        FTPClient client = null;
        try {
            client = ftpClientPool.borrowObject();
            boolean b = client.deleteFile(channelHistoryPic.getRechargeFile());
            if (b) {
                channelHistoryService.deleteRechargeFileById(id);
                channelHistoryService.updateChannelHistoryFileNumMinusByOrderNo(channelHistoryPic.getOrderNo());
            } else {
                logger.error("??????????????????->????????????????????????");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.codeMaps.get(RespCode.error107));
        } finally {
            ftpClientPool.returnObject(client);
        }
        return returnSuccess();
    }

    /**
     * ?????????????????????
     */
    @RequestMapping(value = "/confirmRecharge", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> confirmRecharge(HttpServletRequest request, String id, String tranPassword, Integer status, String remark, String realRechargeAmount) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        String customKey = loginUser.getCustomkey();
        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
            customKey = masterCustom.getCustomkey();
            if (CustomType.COMPANY.getCode() != masterCustom.getCustomType()) {
                return returnFail(RespCode.error101, "????????????");
            }
        } else if (CustomType.COMPANY.getCode() != loginUser.getCustomType()) {
            return returnFail(RespCode.error101, "????????????");
        }

        if (status == RechargeStatusType.SUCCESS.getCode()) {
            if (StringUtil.isEmpty(realRechargeAmount)) {
                return returnFail(RespCode.error101, "??????????????????????????????");
            }
        }

        logger.info("????????????customkey:{}??????????????????" + customKey);
        if (StringUtil.isEmpty(customKey) || StringUtil.isEmpty(id)
                || StringUtil.isEmpty(tranPassword) || status == null
                || StringUtil.isEmpty(loginUser.getUsername())) {

            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        } else {
            //????????????
            if (loginUser.getTranPassword() == null
                    || !loginUser.getTranPassword().equals(
                    CipherUtil.generatePassword(tranPassword, customKey))) {

                return returnFail(RespCode.error101, RespCode.PASSWORD_ERROR);
            }

            ChannelHistory history = channelHistoryService.getChannelHistoryById(id);
            if (history == null || RechargeStatusType.CONFIRMING.getCode() != history.getStatus()) {
                return returnFail(RespCode.error101, "??????????????????????????????????????????,??????????????????");
            }

            if (status == RechargeStatusType.SUCCESS.getCode() && !ArithmeticUtil.isInTheInterval(history.getRechargeAmount(), realRechargeAmount, "0.80", "1.20")) {
                return returnFail(RespCode.error101, "???????????????????????????????????????");
            }

            try {
                history.setCompanyOperatorName(loginUser.getUsername());
                history.setUpdatetime(DateUtils.getNowDate());
                //?????? ???????????? ?????? ????????????
                history.setStatus(status);
                history.setRemark(remark);

                history.setCurrentStatus(RechargeStatusType.CONFIRMING.getCode());

                int count = channelHistoryService.updateChannelHistory(history);
                if (count > 0) {
                    history.setCurrentStatus(null);

                    if (history.getStatus() == RechargeStatusType.SUCCESS.getCode() && history.getRechargeType() == RechargeType.AMOUNT.getCode()) {
                        //??????????????????
                        history.setRealRechargeAmount(realRechargeAmount);
                        history.setUnInvoiceAmount(realRechargeAmount);
                        channelHistoryService.updateChannelHistory(history);

                        //????????????????????????????????????
                        CustomBalanceHistory customBalanceHistory = new CustomBalanceHistory();
                        customBalanceHistory.setCustomKey(history.getCustomkey());
                        customBalanceHistory.setCompanyId(history.getRecCustomkey());
                        customBalanceHistory.setPayType(history.getPayType());
                        customBalanceHistory.setRemark(history.getRemark());
                        customBalanceHistory.setTradeNumber(1);
                        customBalanceHistory.setTradeType(TradeType.RECHARGE.getCode());
                        customBalanceHistory.setTradeAmount(history.getAmount());
                        customBalanceHistory.setRelateOrderNo(history.getOrderno());
                        customBalanceHistory.setOperator(loginUser.getUsername());
                        customBalanceService.updateCustomBalance(CommonString.ADDITION,customBalanceHistory);

                    } else if (history.getStatus() == RechargeStatusType.SUCCESS.getCode() && history.getRechargeType() == RechargeType.SERVICEAMOUNT.getCode()) {

                        //??????????????????
                        history.setRealRechargeAmount(realRechargeAmount);
                        history.setUnInvoiceAmount(realRechargeAmount);
                        channelHistoryService.updateChannelHistory(history);
                    }
                } else {
                    logger.error("-------------??????????????????,orderNo:{}-------------", history.getOrderno());
                    return returnFail(RespCode.error107, "??????????????????");
                }

                history = channelHistoryService.getChannelHistoryById(id);
                channelHistoryService.rechargeCallback(history);
                channelHistoryService.approvalInvoice(history);

                return returnSuccess();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
            }
        }
    }

    /**
     * ????????????????????????????????????
     */
    @RequestMapping(value = "/forwardConfirmRecharge", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> forwardConfirmRecharge(HttpServletRequest request, String id, String tranPassword, Integer status, String remark,
                                               String realRechargeAmount,String realCompanyId,String transAmount,String commissionAmount) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        String customKey = loginUser.getCustomkey();
        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
            customKey = masterCustom.getCustomkey();
            if (CustomType.COMPANY.getCode() != masterCustom.getCustomType()) {
                return returnFail(RespCode.error101, "????????????");
            }
        } else if (CustomType.COMPANY.getCode() != loginUser.getCustomType()) {
            return returnFail(RespCode.error101, "????????????");
        }

        if (status == RechargeStatusType.SUCCESS.getCode()) {
            if (StringUtil.isEmpty(realRechargeAmount)) {
                return returnFail(RespCode.error101, "??????????????????????????????");
            }
        }

        logger.info("????????????customkey:{}??????????????????" + customKey);
        if (StringUtil.isEmpty(customKey) || StringUtil.isEmpty(id) || StringUtil.isEmpty(tranPassword)
                || status == null || StringUtil.isEmpty(loginUser.getUsername()) || StringUtil.isEmpty(realCompanyId)
                || StringUtil.isEmpty(transAmount) || StringUtil.isEmpty(commissionAmount)) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }
        //????????????
        if (loginUser.getTranPassword() == null || !loginUser.getTranPassword().equals(
                CipherUtil.generatePassword(tranPassword, customKey))) {
            return returnFail(RespCode.error101, RespCode.PASSWORD_ERROR);
        }
        ChannelHistory history = channelHistoryService.getChannelHistoryById(id);
        if (history == null || RechargeStatusType.CONFIRMING.getCode() != history.getStatus()) {
            return returnFail(RespCode.error101, "??????????????????????????????????????????,??????????????????");
        }

        if (status == RechargeStatusType.SUCCESS.getCode() && !ArithmeticUtil.isInTheInterval(history.getRechargeAmount(), realRechargeAmount, "0.80", "1.20")) {
            return returnFail(RespCode.error101, "???????????????????????????????????????");
        }

        if (ArithmeticUtil.compareTod(realRechargeAmount,transAmount) < 0){
            return returnFail(RespCode.error101, "????????????????????????????????????????????????");
        }

        //????????????????????????-???????????????????????????
        boolean forwardCompany = (!history.getRecCustomkey().equals(realCompanyId));
        Integer forwardCompanyAccountId = null;
        ForwardCompanyAccount queryCompanyAccount = new ForwardCompanyAccount(history.getCustomkey(),history.getRecCustomkey(),realCompanyId);
        if (forwardCompany){
            //?????????????????????,????????????????????????
            List<ForwardCompanyAccount> forwardCompanyAccounts = forwardCompanyAccountService.findBalanceByCondition(queryCompanyAccount);
            if (forwardCompanyAccounts == null || forwardCompanyAccounts.isEmpty()){
                //??????????????????
                ChannelCustom custom = channelCustomService.getCustomByCustomkey(history.getCustomkey());
                ChannelCustom company = channelCustomService.getCustomByCustomkey(history.getRecCustomkey());
                ChannelCustom realCompany = channelCustomService.getCustomByCustomkey(realCompanyId);
                queryCompanyAccount.setMerchantName(custom.getCompanyName());
                queryCompanyAccount.setCompanyName(company.getCompanyName());
                queryCompanyAccount.setRealCompanyName(realCompany.getCompanyName());

                queryCompanyAccount.setBalance(0);
                queryCompanyAccount.setStatus(1);
                forwardCompanyAccountService.insert(queryCompanyAccount);
            }else{
                ForwardCompanyAccount forwardCompanyAccount = forwardCompanyAccounts.get(0);
                int companyAccountStatus = forwardCompanyAccount.getStatus();
                forwardCompanyAccountId = forwardCompanyAccount.getId();
                if (2 == companyAccountStatus){
                    return returnFail(RespCode.error101,"??????????????????????????????");
                }
            }
        }

        try {
            history.setCompanyOperatorName(loginUser.getUsername());
            history.setUpdatetime(DateUtils.getNowDate());
            //?????? ???????????? ?????? ????????????
            history.setStatus(status);
            history.setRemark(remark);

            history.setCurrentStatus(RechargeStatusType.CONFIRMING.getCode());

            int count = channelHistoryService.updateChannelHistory(history);
            if (count > 0) {

                history.setCurrentStatus(null);

                if (history.getStatus() == RechargeStatusType.SUCCESS.getCode() && history.getRechargeType() == RechargeType.AMOUNT.getCode()) {
                    //??????????????????
                    history.setRealRechargeAmount(realRechargeAmount);
                    history.setUnInvoiceAmount(realRechargeAmount);
                    history.setRealCompanyAmount(transAmount);
                    channelHistoryService.updateChannelHistory(history);

                    //??????????????????????????????
                    CustomBalanceHistory customBalanceHistory = new CustomBalanceHistory();
                    customBalanceHistory.setCustomKey(history.getCustomkey());
                    customBalanceHistory.setCompanyId(history.getRecCustomkey());
                    customBalanceHistory.setPayType(history.getPayType());
                    customBalanceHistory.setRemark(history.getRemark());
                    customBalanceHistory.setTradeNumber(1);
                    customBalanceHistory.setTradeType(TradeType.RECHARGE.getCode());
                    customBalanceHistory.setTradeAmount(history.getAmount());
                    customBalanceHistory.setRelateOrderNo(history.getOrderno());
                    customBalanceHistory.setOperator(loginUser.getUsername());
                    customBalanceService.updateCustomBalance(CommonString.ADDITION,customBalanceHistory);

                    if (forwardCompany){
                        //???????????????????????????????????????
                        if (forwardCompanyAccountId == null){
                            List<ForwardCompanyAccount> balanceByCondition = forwardCompanyAccountService.findBalanceByCondition(queryCompanyAccount);
                            forwardCompanyAccountId = balanceByCondition.get(0).getId();
                        }
                        CompanyAccountVo companyAccountVo = new CompanyAccountVo();
                        companyAccountVo.setId(forwardCompanyAccountId);
                        companyAccountVo.setBalance(commissionAmount);
                        companyAccountVo.setCustomKey(history.getCustomkey());
                        companyAccountVo.setCompanyId(history.getRecCustomkey());
                        companyAccountVo.setRealCompanyId(realCompanyId);
                        companyAccountVo.setTradeType(TradeType.RECHARGE.getCode());
                        companyAccountVo.setRelateOrderNo(history.getOrderno());
                        companyAccountVo.setOperator(loginUser.getUsername());
                        companyAccountVo.setAmount(1);
                        companyAccountVo.setOperating(CommonString.ADDITION);
                        forwardCompanyAccountService.updateCompanyAccount(companyAccountVo);
                    }
                } else if (history.getStatus() == RechargeStatusType.SUCCESS.getCode() && history.getRechargeType() == RechargeType.SERVICEAMOUNT.getCode()) {

                    //??????????????????
                    history.setRealRechargeAmount(realRechargeAmount);
                    history.setUnInvoiceAmount(realRechargeAmount);
                    channelHistoryService.updateChannelHistory(history);
                }
            } else {
                logger.error("-------------??????????????????,orderNo:{}-------------", history.getOrderno());
                return returnFail(RespCode.error107, "??????????????????");
            }

            history = channelHistoryService.getChannelHistoryById(id);
            channelHistoryService.rechargeCallback(history);
            channelHistoryService.approvalInvoice(history);

            return returnSuccess();
        }catch (BalanceException be){
            //??????????????????????????????
            logger.error(be.getMessage(), be);
            return returnFail(RespCode.error107,RespCode.CONNECTION_ERROR);
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }
    }


    /**
     * ????????????????????????
     */
    @RequestMapping(value = "/transferRecordList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> transferRecordList(HttpServletRequest request, String rechargeId, String startTime, String endTime, Integer status,
                                           @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                           @RequestParam(required = false, defaultValue = "10") Integer pageSize) {


        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        if (!isCompany(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }

        if (StringUtil.isEmpty(rechargeId)) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        ChannelHistory history = channelHistoryService.getChannelHistoryById(rechargeId);
        if (history == null || RechargeStatusType.CONFIRMING.getCode() != history.getStatus()) {
            return returnFail(RespCode.error101, RespCode.RECHARGE_RECORD_ERROR);
        }

        try {

            PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(
                String.valueOf(history.getPayType()), history.getCustomkey(),
                history.getRecCustomkey());

            CustomReceiveConfig receiveConfig = customReceiveConfigService
                .getCustomReceiveConfig(history.getCustomkey(), history.getRecCustomkey(),
                    history.getPayType());

            String subAccount = history.getInAccountNo();
            if (receiveConfig != null && receiveConfig.getIsSubAccount() == 1) {
                subAccount = receiveConfig.getReceiveAccount();
            }
//            else if (receiveConfig == null) { //???????????????????????? ????????????????????????????????????
//                ChannelConfig channelConfig = customService
//                    .getChannelConfigByCustomKeyAndCompanyId(history.getRecCustomkey(),
//                        history.getPayType());
//                if (channelConfig != null
//                    && channelConfig.getRechargeConfirmType() == RechargeConfirmType.AUTO
//                    .getCode()) {
//                    subAccount = paymentConfig.getShadowAcctNo();
//                }
//            }

            ChannelCustom rechargeCustom = channelCustomService
                .getCustomByCustomkey(history.getCustomkey());

            Map<String, Object> param = new HashMap<>();
            param.put("subAccount", subAccount);
            param.put("status", status);
            param.put("startTime", startTime);
            param.put("endTime", endTime);
            param.put("tranType", CustomTransferRecordType.SUBACCOUNTINTO.getCode());

            if (paymentConfig != null && !StringUtil.isEmpty(paymentConfig.getShadowAcctNo())) {
                param.put("containEntity", 1);
                param.put("shadowAcctNo", paymentConfig.getShadowAcctNo());
                param.put("oppAccountName", rechargeCustom.getContractCompanyName());
            }

            PageHelper.startPage(pageNo, pageSize);
            List<CustomTransferRecord> transferRecordList = transferRecordService.getCustomTransferRecordByParam(param);


            PageInfo page = new PageInfo(transferRecordList);

            Map<String, Object> result = new HashMap<>(5);
            result.put("total", page.getTotal());
            result.put("list", page.getList());

            return returnSuccess(result);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }
    }

    /**
     * ?????????????????????(????????????????????????)
     * transferRecordIds:?????????????????????ID??????????????? , ??????
     */
    @RequestMapping(value = "/confirmAutoRecharge", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> confirmAutoRecharge(HttpServletRequest request, String rechargeId, String transferRecordIds, String tranPassword, Integer status) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        if (!isCompany(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }

        if (StringUtil.isEmpty(rechargeId) || transferRecordIds == null || StringUtil.isEmpty(tranPassword) || RechargeStatusType.SUCCESS.getCode() != status) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        //??????????????????
        if (!loginUser.getTranPassword().equals(CipherUtil.generatePassword(tranPassword, StringUtil.isEmpty(loginUser.getCustomkey()) ? loginUser.getMasterCustom() : loginUser.getCustomkey()))) {
            return returnFail(RespCode.error101, RespCode.PASSWORD_ERROR);
        }

        ChannelHistory history = channelHistoryService.getChannelHistoryById(rechargeId);
        if (history == null || RechargeStatusType.CONFIRMING.getCode() != history.getStatus()) {
            return returnFail(RespCode.error101, RespCode.RECHARGE_RECORD_ERROR);
        }

        List<CustomTransferRecord> transferRecords = transferRecordService.selectByPrimaryKeys(transferRecordIds);
        if (CollectionUtils.isEmpty(transferRecords)){
            return returnFail(RespCode.error101, RespCode.TRANSFER_RECORD_ERROR);
        }
        String transAmount = "0";
        StringBuilder bizFlowNos = new StringBuilder();
        for (CustomTransferRecord transferRecord : transferRecords) {
            if (ConfirmStatus.FAILURE.getCode() != transferRecord.getIsConfirm()){
                return returnFail(RespCode.error101, RespCode.TRANSFER_RECORD_ERROR);
            }
            transAmount = ArithmeticUtil.addStr(transAmount,transferRecord.getTranAmount());
            bizFlowNos.append(transferRecord.getBizFlowNo()).append(",");
        }

        try {
            //TODO-??????????????????????????????????????????????????????????????????????????????????????1.00???
            String rechargeAmount = history.getRechargeAmount();
            if (ArithmeticUtil.compareTod(transAmount,ArithmeticUtil.addStr2(rechargeAmount,"1")) > 0
                    || ArithmeticUtil.compareTod(transAmount,ArithmeticUtil.subStr2(rechargeAmount,"1")) < 0){
                return returnFail(RespCode.error101, RespCode.RECHARGE_AMOUNT_ERROR);
            }

            logger.error("------------?????????????????????????????????????????????,?????????{},????????????{}--------------", history.getOrderno(), bizFlowNos.toString());

            history.setCompanyOperatorName(loginUser.getUsername());
            boolean state = customBalanceService.manualConfirmBalance(history, transferRecords);

            if (!state) {
                logger.error("------------??????????????????,?????????{},????????????{}--------------", history.getOrderno(), bizFlowNos.toString());
                return returnFail(RespCode.error107, RespCode.RECHARGE_RECORD_EXCEPTION);
            }

            history = channelHistoryService.getChannelHistoryById(rechargeId);
            channelHistoryService.rechargeCallback(history);
            channelHistoryService.approvalInvoice(history);

            return returnSuccess();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }

    }


    /**
     * ??????????????????????????????
     */
    @RequestMapping(value = "/rechargeInfo", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> rechargeInfo(String rechargeId) {

        if (StringUtil.isEmpty(rechargeId)) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        ChannelHistory history = channelHistoryService.getRechargeInfoById(rechargeId);
        if (history == null || RechargeStatusType.SUCCESS.getCode() != history.getStatus() || InvoiceOrderStatus.NO_TYPE.getCode() != history.getInvoiceStatus() || RechargeType.SERVICEAMOUNT.getCode() == history.getRechargeType()) {
            return returnFail(RespCode.error101, RespCode.RECHARGE_RECORD_REFUND_ERROR);
        }

        String customBalance = balanceService.queryCustomBalance(history.getCustomkey(), history.getRecCustomkey(), history.getPayType());
        history.setCustomBalance(customBalance);

        return returnSuccess(history);
    }

    /**
     * ??????????????????????????????
     */
    @RequestMapping(value = "/rechargeRefund", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> rechargeRefund(HttpServletRequest request, String rechargeId, String customBalance, String currentRefundAmount, Integer refundType, String tranPassword) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        String operatorName = loginUser.getUsername();

        if (!isCompany(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }

        //??????????????????
        if (!loginUser.getTranPassword().equals(CipherUtil.generatePassword(tranPassword, StringUtil.isEmpty(loginUser.getCustomkey()) ? loginUser.getMasterCustom() : loginUser.getCustomkey()))) {
            return returnFail(RespCode.error101, RespCode.PASSWORD_ERROR);
        }

        if (refundType == null || StringUtil.isEmpty(rechargeId) || StringUtil.isEmpty(tranPassword) || StringUtil.isEmpty(customBalance) || StringUtil.isEmpty(currentRefundAmount) || ArithmeticUtil.compareTod(currentRefundAmount, "0") < 0) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        ChannelHistory history = channelHistoryService.getChannelHistoryById(rechargeId);
        if (history == null || RechargeStatusType.SUCCESS.getCode() != history.getStatus() || InvoiceOrderStatus.NO_TYPE.getCode() != history.getInvoiceStatus() || RechargeType.SERVICEAMOUNT.getCode() == history.getRechargeType()) {
            return returnFail(RespCode.error101, RespCode.RECHARGE_RECORD_REFUND_ERROR);
        }

        if (ArithmeticUtil.compareTod(history.getRealRechargeAmount(), history.getAmount()) < 0) {
            return returnFail(RespCode.error101, RespCode.RECHARGE_REFUND_AMOUNT_EQUAL);
        }

        //????????????????????????
        Company company = companyService.getCompanyByUserId(Integer.parseInt(history.getRecCustomkey()));
        if (CompanyType.SUBCONTRACT.getCode() == company.getCompanyType()){
            return returnFail(RespCode.error101, RespCode.COMPANY_NOT_SUPPORT_REFUND);
        }
        try {
            String customKey = history.getCustomkey();
            String companyId = history.getRecCustomkey();
            Integer payType = history.getPayType();
            String balanceRefund = currentRefundAmount;

            String balance = balanceService.queryCustomBalance(customKey, companyId, payType);
            if (ArithmeticUtil.compareTod(customBalance, balance) != 0) {
                return returnFail(RespCode.error101, RespCode.CUSTOM_BALANCE_CHANGE);
            }

            int count = userCommissionService.getPayingCount(customKey, companyId, payType);
            if (count > 0) {
                return returnFail(RespCode.error101, RespCode.EXIST_PAY_RECORD);
            }

            if (RechargeRefundType.ALL.getCode() == refundType) {
                if (ArithmeticUtil.compareTod(history.getRealRechargeAmount(), currentRefundAmount) != 0) {
                    return returnFail(RespCode.error101, RespCode.RECHARGE_REFUND_AMOUNT_ERROR);
                }

                if (ArithmeticUtil.compareTod(customBalance, history.getAmount()) < 0) {
                    return returnFail(RespCode.error101, RespCode.CUSTOM_BALANCE_REFUND_ERROR);
                }

                balanceRefund = history.getAmount();

                history.setStatus(RechargeStatusType.REFUND.getCode());
                history.setAmount("0");
                history.setRealRechargeAmount("0");
                history.setUnInvoiceAmount("0");

                if (ArithmeticUtil.compareTod(history.getRefundAmount(), "0") > 0) {
                    currentRefundAmount = ArithmeticUtil.addStr(history.getRefundAmount(), currentRefundAmount);
                }
                history.setRefundAmount(currentRefundAmount);

            } else if ((RechargeRefundType.PART.getCode() == refundType)) {

                if (ArithmeticUtil.compareTod(customBalance, currentRefundAmount) < 0) {
                    return returnFail(RespCode.error101, RespCode.CUSTOM_BALANCE_REFUNDAMOUNT_ERROR);
                }

                if (ArithmeticUtil.compareTod(history.getAmount(), currentRefundAmount) < 0) {
                    return returnFail(RespCode.error101, RespCode.CUSTOM_BALANCE_PART_ERROR);
                }

                history.setAmount(ArithmeticUtil.subStr2(history.getAmount(), currentRefundAmount));
                history.setRealRechargeAmount(ArithmeticUtil.subStr2(history.getRealRechargeAmount(), currentRefundAmount));
                history.setUnInvoiceAmount(ArithmeticUtil.subStr2(history.getUnInvoiceAmount(), currentRefundAmount));
                history.setRefundAmount(ArithmeticUtil.addStr(history.getRefundAmount(), currentRefundAmount));

            } else {
                return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
            }

            if (!customBalanceService.rechargeRefund(history, balanceRefund, operatorName)) {
                return returnFail(RespCode.error107, RespCode.RECHARGE_REFUND_ERROR);
            }

            return returnSuccess();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }

    }

    /**
     * ???????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/agentRechargeList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> agentRechargeList(HttpServletRequest request, @RequestParam(required = false) String startTime,
                                          @RequestParam(required = false) String endTime,
                                          @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                          @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) String rechargeAmount,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String payType,
                                          @RequestParam(required = false) String companyId,
                                          @RequestParam(required = false) String customName,
                                          @RequestParam(required = false) String orderNo,
                                          @RequestParam(required = false) Integer rechargeType,
                                          @RequestParam(required = false) Integer rechargeConfirmType,
                                          @RequestParam(required = false) String businessPlatform) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        logger.info("?????????????????????????????????,customkey :" + loginUser.getCustomkey());

        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            loginUser = customService.getCustomByCustomkey(loginUser.getMasterCustom());
        }

        if (CustomType.PROXY.getCode() != loginUser.getCustomType()) {
            return returnFail(RespCode.error106, "???????????????");
        }

        Map<String, Object> result = new HashMap<>(5);
        Map<String, Object> paramMap = new HashMap<>();
        List<String> allList = new ArrayList<>();
        try {
            String customkey = loginUser.getCustomkey();
            logger.info("????????????????????????????????????{}", customkey);
            if (!StringUtil.isEmpty(customkey)) {
                //?????????????????????????????????
                if (loginUser.getProxyType() == 1) {
                    OrganizationNode node = customProxyDao.getProxyChildenNodeByCustomKey(customkey,null);
                    List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());

                    if (stringList != null && stringList.size() > 0) {
                        allList.addAll(stringList);
                        String joinCustomkey = String.join(",", stringList);
                        List<OrganizationNode> customkeyList = customProxyDao.getCustomProxyByLevelCode(joinCustomkey);
                        if (customkeyList.size() > 0 && !customkeyList.isEmpty()) {
                            for (OrganizationNode o : customkeyList) {
                                allList.add(o.getCustomKey());
                                if (o.getCustomType() == CustomType.GROUP.getCode()) {
                                    OrganizationNode organizationNode = customGroupDao.getCustomGroupByCustomkey(o.getCustomKey());
                                    List<String> groupList = organizationTreeService.queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, organizationNode.getId());
                                    if (groupList.size() > 0 && !groupList.isEmpty()) {
                                        allList.addAll(groupList);
                                    }
                                }
                            }
                        }
                        String customKeys = Joiner.on(",").join(allList);
                        logger.info("?????????????????????????????????????????????{}", customKeys);
                    }
                } else {
                    OrganizationNode node = customProxyDao.getNodeByCustomKey(customkey,null);
                    List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
                    if (stringList.size() > 0 && !stringList.isEmpty()) {
                        allList.addAll(stringList);
                        String customKeys = Joiner.on(",").join(stringList);
                        logger.info("????????????????????????????????????{}", customKeys);
                    }
                }
            } else {
                return returnFail(RespCode.error106, "????????????");
            }
//            recursion(customkeyList, loginUser.getCustomkey());
            paramMap.put("customkey", String.join(",", allList));
            paramMap.put("companyId", companyId);
            paramMap.put("startTime", startTime);
            paramMap.put("endTime", endTime);
            paramMap.put("rechargeAmount", rechargeAmount);
            paramMap.put("status", status);
            paramMap.put("payType", payType);
            paramMap.put("customName", customName);
            paramMap.put("orderNo", orderNo);
            paramMap.put("rechargeType", rechargeType);
            paramMap.put("rechargeConfirmType", rechargeConfirmType);
            paramMap.put("businessPlatform", businessPlatform);
            PageHelper.startPage(pageNo, pageSize);
            List<Map<String, Object>> list = channelHistoryService.getProxyCustomCompanyDetail(paramMap);
            PageInfo page = new PageInfo(list);
            result.put("total", page.getTotal());
            result.put("list", page.getList());
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
            return returnFail(RespCode.error107, RespCode.codeMaps.get(RespCode.error107));
        }
        return returnSuccess(result);
    }


    /**
     * ???????????????????????????
     *
     * @param customkeyList
     * @param customkey
     */
    public void recursion(List<String> customkeyList, String customkey) {
        List<String> systemRoleTree = getSystemRoleTree(customkey);
        if (!systemRoleTree.isEmpty()) {
            for (String key : systemRoleTree) {
                customkeyList.add(key);
                recursion(customkeyList, key);
            }
        }
    }

    /**
     * ???????????????????????????
     *
     * @param customkey
     * @return
     */
    public List<String> getSystemRoleTree(String customkey) {
        String queryMode = "C1";
        Integer customType = 0;
        List<String> newCustomkey = new ArrayList<>();

        ChannelCustom custom = customService.getCustomByCustomkey(customkey);
        // ????????????????????????????????????????????????
        if (custom.getCustomType() == CustomType.PROXY.getCode() && custom.getProxyType() == 1) {
            logger.info("????????????{}?????????????????????", custom.getCompanyName());
            customType = CustomType.PROXYCHILDEN.getCode();
        }

        if (customType == 0) {
            customType = custom.getCustomType();
        }

        int nodeId = 0;
        if (customType == 3) {
            CustomProxy customProxy = customProxyService.getProxyIdByCustomkey(customkey);
            nodeId = customProxy.getId();
        } else if (customType == 5) {
            OrganizationNode customGroup = customGroupDao.getGroupIdByCustomkey(customkey);
            nodeId = customGroup.getId();
        } else if (customType == 1) {
            return new ArrayList<>();
        }

        List<OrganizationNode> organizationNodes = organizationTreeService.queryOrganizationTree(customkey, customType, queryMode, nodeId,null);
        if (organizationNodes.size() > 0 && !organizationNodes.isEmpty()) {
            for (OrganizationNode o : organizationNodes) {
                newCustomkey.add(o.getCustomKey());
            }
        }

        return newCustomkey;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/agentRechargeListExport")
    public void agentRechargeListExport(@RequestParam(required = false) String startTime,
                                        @RequestParam(required = false) String endTime,
                                        @RequestParam(required = false) String rechargeAmount,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String payType,
                                        @RequestParam(required = false) String companyId,
                                        @RequestParam(required = false) String customName,
                                        @RequestParam(required = false) String orderNo,
                                        @RequestParam(required = false) Integer rechargeType,
                                        @RequestParam(required = false) Integer rechargeConfirmType,
                                        @RequestParam(required = false) String businessPlatform,
                                        HttpServletResponse response, HttpSession session) throws Exception {

        ChannelCustom loginUser = (ChannelCustom) session.getAttribute(CommonString.CUSTOMLOGIN);
        logger.info("?????????????????????????????????,customkey :" + loginUser.getCustomkey());

        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            loginUser = customService.getCustomByCustomkey(loginUser.getMasterCustom());
        }

        if (CustomType.PROXY.getCode() == loginUser.getCustomType()) {

            List<String> allList = new ArrayList<>();
            String customkey = loginUser.getCustomkey();
            logger.info("????????????????????????????????????{}", customkey);
            if (!StringUtil.isEmpty(customkey)) {
                //?????????????????????????????????
                if (loginUser.getProxyType() == 1) {
                    OrganizationNode node = customProxyDao.getProxyChildenNodeByCustomKey(customkey,null);
                    List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());

                    if (stringList != null && stringList.size() > 0) {
                        allList.addAll(stringList);
                        String joinCustomkey = String.join(",", stringList);
                        List<OrganizationNode> customkeyList = customProxyDao.getCustomProxyByLevelCode(joinCustomkey);
                        if (customkeyList.size() > 0 && !customkeyList.isEmpty()) {
                            for (OrganizationNode o : customkeyList) {
                                allList.add(o.getCustomKey());
                                if (o.getCustomType() == CustomType.GROUP.getCode()) {
                                    OrganizationNode organizationNode = customGroupDao.getCustomGroupByCustomkey(o.getCustomKey());
                                    List<String> groupList = organizationTreeService.queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, organizationNode.getId());
                                    if (groupList.size() > 0 && !groupList.isEmpty()) {
                                        allList.addAll(groupList);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    OrganizationNode node = customProxyDao.getNodeByCustomKey(customkey,null);
                    List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
                    if (stringList.size() > 0 && !stringList.isEmpty()) {
                        allList.addAll(stringList);
                    }
                }
            }
            logger.info("????????????????????????????????????{}", String.join(",", allList));
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("customkey", String.join(",", allList));
            paramMap.put("companyId", companyId);
            paramMap.put("startTime", startTime);
            paramMap.put("endTime", endTime);
            paramMap.put("rechargeAmount", rechargeAmount);
            paramMap.put("status", status);
            paramMap.put("payType", payType);
            paramMap.put("customName", customName);
            paramMap.put("orderNo", orderNo);
            paramMap.put("rechargeType", rechargeType);
            paramMap.put("rechargeConfirmType", rechargeConfirmType);
            paramMap.put("businessPlatform", businessPlatform);
            List<Map<String, Object>> list = channelHistoryService.getProxyCustomCompanyDetail(paramMap);
            PageInfo page = new PageInfo(list);

            String[] colunmName = new String[]{"????????????", "????????????", "??????", "????????????", "????????????", "????????????", "????????????",
                    "????????????", "??????????????????", "????????????", "????????????????????????", "?????????????????????", "???????????????", "??????????????????",
                    "??????????????????", "????????????", "??????????????????", "??????????????????", "????????????", "??????????????????",
                    "??????????????????", "????????????", "?????????????????????", "?????????????????????", "???????????????", "???????????????", "?????????????????????", "??????????????????"};
            String filename = "???????????????";
            List<Map<String, Object>> data = new ArrayList<>();
            for (Map<String, Object> stringObjectMap : list) {
                Map<String, Object> dataMap = new HashMap<>(25);
                dataMap.put("1", stringObjectMap.get("customName"));
                dataMap.put("2", stringObjectMap.get("orderNo"));
                dataMap.put("3", stringObjectMap.get("createTime"));

                Integer rechargeTypeCode = (Integer) stringObjectMap.get("rechargeType");
                dataMap.put("4", RechargeType.codeOf(rechargeTypeCode).getDesc());
                dataMap.put("5", stringObjectMap.get("rechargeAmount"));
                dataMap.put("6", stringObjectMap.get("amount"));
                dataMap.put("7", stringObjectMap.get("serviceFee"));
                dataMap.put("8", RechargeStatusType.codeOf((Integer) stringObjectMap.get("status")).getDesc());
                dataMap.put("9", PayType.codeOf((Integer) stringObjectMap.get("payType")).getDesc());
                dataMap.put("10", stringObjectMap.get("remark"));
                dataMap.put("11", stringObjectMap.get("realRechargeAmount"));

                if (rechargeTypeCode == RechargeType.AMOUNT.getCode()) {
                    dataMap.put("12", ServiceFeeType.codeOf((Integer) stringObjectMap.get("serviceFeeType")).getDesc());
                } else {
                    dataMap.put("12", "");
                }
                String serviceFeeRate = (String) stringObjectMap.get("serviceFeeRate");
                if (!StringUtil.isEmpty(serviceFeeRate)) {
                    dataMap.put("13", ArithmeticUtil.mulStr(serviceFeeRate, "100", 2) + "%");
                } else {
                    dataMap.put("13", "0%");
                }

                dataMap.put("14", stringObjectMap.get("businessPlatform"));
                dataMap.put("15", stringObjectMap.get("companyName"));
                dataMap.put("16", stringObjectMap.get("inAccountNo"));
                dataMap.put("17", stringObjectMap.get("inAccountBankName"));

                dataMap.put("18", stringObjectMap.get("customName"));
                dataMap.put("19", stringObjectMap.get("payAccountNo"));
                dataMap.put("20", stringObjectMap.get("payAccountBankName"));

                dataMap.put("21", RechargeConfirmType.codeOf((Integer) stringObjectMap.get("rechargeConfirmType")).getDesc());


                String invoiceStatus = stringObjectMap.get("invoiceStatus") + "";
                if (!StringUtil.isEmpty(invoiceStatus)) {
                    dataMap.put("22", InvoiceOrderStatus.codeOf(Integer.parseInt(invoiceStatus)).getDesc());
                } else {
                    dataMap.put("22", "");
                }
                dataMap.put("23", stringObjectMap.get("invoiceAmount"));
                dataMap.put("24", stringObjectMap.get("unInvoiceAmount"));
                dataMap.put("25", stringObjectMap.get("invoiceingAmount"));

                dataMap.put("26", stringObjectMap.get("operatorName"));
                dataMap.put("27", stringObjectMap.get("companyOperatorName"));
                dataMap.put("28", stringObjectMap.get("updateTime"));
                data.add(sortMapByKey(dataMap));
            }
            ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
        }
    }


    /**
     * ?????????????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/agentCostMaintainList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> agentCostMaintainList(HttpServletRequest request,
                                              @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                              @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) String proxyName,
                                              @RequestParam(required = false) String companyId,
                                              @RequestParam(required = false) String startTime,
                                              @RequestParam(required = false) String endTime,
                                              @RequestParam(required = false) String phoneNo,
                                              @RequestParam(required = false) Integer businessPlatformId,
                                              @RequestParam(required = false) String proxyFeeRateStart,
                                              @RequestParam(required = false) String gearLabel,
                                              @RequestParam(required = false) String proxyFeeRateEnd) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        logger.info("?????????????????????????????????,customkey :{}, masterCustom:{}", loginUser.getCustomkey(), loginUser.getMasterCustom());

        //???????????????????????????????????????
        if (!isPlatformAccount(loginUser) && !isMFKJAccount(loginUser)) {
            return returnFail(RespCode.error106, "???????????????");
        }
        if (isPlatformAccount(loginUser)){
            businessPlatformId = loginUser.getBusinessPlatformId();
        }
        Map<String, Object> result = new HashMap<>(5);
        if (businessPlatformId == null){
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
            return result;
        }
        Map<String, Object> paramMap = new HashMap<>();
        try {
            paramMap.put("proxyName", proxyName);
            paramMap.put("companyId", companyId);
            paramMap.put("startTime", startTime);
            paramMap.put("endTime", endTime);
            paramMap.put("phoneNo", phoneNo);
            paramMap.put("businessPlatformId", businessPlatformId);
            BigDecimal b2 = new BigDecimal("100");
            if (!StringUtil.isEmpty(proxyFeeRateStart)) {
                BigDecimal start = new BigDecimal(proxyFeeRateStart);
                proxyFeeRateStart = start.divide(b2).toString();
            }
            if (!StringUtil.isEmpty(proxyFeeRateEnd)) {
                BigDecimal end = new BigDecimal(proxyFeeRateEnd);
                proxyFeeRateEnd = end.divide(b2).toString();
            }
            paramMap.put("proxyFeeRateStart", proxyFeeRateStart);
            paramMap.put("proxyFeeRateEnd", proxyFeeRateEnd);
            paramMap.put("gearLabel", gearLabel);
            PageHelper.startPage(pageNo, pageSize);
            List<ProxyCostMaintain> list = proxyCostMaintainService.getProxyCostMaintainList(paramMap);
            for (ProxyCostMaintain p : list) {
                if (!StringUtil.isEmpty(p.getProxyFeeRate())) {
                    p.setProxyFeeRate(ArithmeticUtil.mulStr(p.getProxyFeeRate(), "100"));
                }
            }
            PageInfo page = new PageInfo(list);
            result.put("total", page.getTotal());
            result.put("list", page.getList());
            logger.info("??????????????????????????????????????????" + result);
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
            return returnFail(RespCode.error107, RespCode.codeMaps.get(RespCode.error107));
        }
        return returnSuccess(result);
    }


    /**
     * @return
     * @description: ???????????????????????????
     */
    @RequestMapping(value = "/configAgentCostMaintain", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> configAgentCostMaintain(HttpServletRequest request, ProxyCostMaintain proxyCostMaintain) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
        //???????????????????????????????????????
        if (!isPlatformAccount(loginUser) && !isMFKJAccount(loginUser)) {
            return returnFail(RespCode.error101, "??????????????????");
        }

        if (!StringUtil.isEmpty(proxyCostMaintain.getCustomkey())) {
            int count = customService.getchannelcustomByCustomkey(proxyCostMaintain.getCustomkey());
            if (count < 0) {
                return returnFail(RespCode.error101, "?????????????????????");
            }
        } else {
            return returnFail(RespCode.error101, "???????????????????????????????????????????????????");
        }

        if (proxyCostMaintain.getProxyType() == 0 || proxyCostMaintain.getCountType() == 1) {
            proxyCostMaintain.setProxyType(0);
            proxyCostMaintain.setCountType(1);
        } else if (proxyCostMaintain.getProxyType() == 1 || proxyCostMaintain.getCountType() == 0) {
            proxyCostMaintain.setProxyType(1);
            proxyCostMaintain.setCountType(0);
        }

        String proxyFeeRate = proxyCostMaintain.getProxyFeeRate();
        BigDecimal b1 = new BigDecimal(proxyFeeRate);
        BigDecimal b2 = new BigDecimal("100");
        proxyCostMaintain.setProxyFeeRate(b1.divide(b2).toString());

        if (proxyCostMaintain.getGearLabel() != 0) {
            List<ProxyCostMaintain> list = new ArrayList<>();
            if (proxyCostMaintain.getId() != null) {
                list = proxyCostMaintainService.getNoProxyCostMaintainByCustomkeyCompanyIdGearLabel(proxyCostMaintain.getCustomkey(), proxyCostMaintain.getCompanyId(), proxyCostMaintain.getGearLabel(), proxyCostMaintain.getNetfileId());
            } else {
                list = proxyCostMaintainService.getProxyCostMaintainByCustomkeyCompanyIdNetfileId(proxyCostMaintain.getCustomkey(), proxyCostMaintain.getCompanyId(), proxyCostMaintain.getGearLabel());
            }
            if (list != null && list.size() > 0) {
                for (ProxyCostMaintain p : list) {
                    if (!StringUtil.isEmpty(p.getProxyFeeRate())) {
                        if (ArithmeticUtil.compareTod(proxyCostMaintain.getProxyFeeRate(), p.getProxyFeeRate()) != 0) {
                            if (proxyCostMaintain.getGearLabel() == 1) {
                                return returnFail(RespCode.UPDATE_FAIL, "?????????????????????????????????????????????????????????!");
                            } else {
                                return returnFail(RespCode.UPDATE_FAIL, "?????????????????????????????????????????????????????????!");
                            }
                        }
                    }
                }
            }
        }

        int count = 0;
        if (proxyCostMaintain.getId() != null) {
            ProxyCostMaintain costMaintain = proxyCostMaintainService.getProxyCostMaintainById(proxyCostMaintain.getId());
            CompanyNetfileRateConf companyNetfileRateConfById = companyNetfileRateConfService.getCompanyNetfileRateConfById(costMaintain.getNetfileId());
            if (2 == proxyCostMaintain.getGearLabel() && companyNetfileRateConfById.getGearLabel() == 1) {
                count = proxyCostMaintainService.getProxyCostMaintainByCustomkeyCompanyIdGearLabel(proxyCostMaintain.getCustomkey(), proxyCostMaintain.getCompanyId(), proxyCostMaintain.getGearLabel(), proxyCostMaintain.getNetfileId());
                if (count >= 1) {
                    return returnFail(RespCode.error101, "??????????????????");
                }
            } else if (1 == proxyCostMaintain.getGearLabel() && companyNetfileRateConfById.getGearLabel() == 2) {
                count = proxyCostMaintainService.getProxyCostMaintainByCustomkeyCompanyIdGearLabel(proxyCostMaintain.getCustomkey(), proxyCostMaintain.getCompanyId(), proxyCostMaintain.getGearLabel(), proxyCostMaintain.getNetfileId());
                if (count >= 1) {
                    return returnFail(RespCode.error101, "??????????????????");
                }
            }
        } else {
            if (proxyCostMaintain.getGearLabel() != 0) {
                count = proxyCostMaintainService.getProxyCostMaintainByCustomkeyCompanyIdGearLabel(proxyCostMaintain.getCustomkey(), proxyCostMaintain.getCompanyId(), proxyCostMaintain.getGearLabel(), proxyCostMaintain.getNetfileId());
                if (count >= 1) {
                    return returnFail(RespCode.error101, "??????????????????");
                }
            }
        }

        try {
            proxyCostMaintainService.configAgentCostMaintain(proxyCostMaintain);
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
            return returnFail(RespCode.error101, "?????????????????????????????????????????????????????????");
        }

        return returnSuccess();
    }

    /**
     * @return
     * @description: ???????????????????????????
     */
    @RequestMapping(value = "/deleteAgentCostMaintain", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> deleteAgentCostMaintain(@RequestParam(value = "id") int id) {
        logger.info("??????????????????????????? Id = " + id);
        ProxyCostMaintain proxyCostMaintain = proxyCostMaintainService.queryProxyCostMaintainById(id);
        if (proxyCostMaintain == null) {
            return returnFail(RespCode.error101, "?????????????????????????????????");
        }
        try {
            proxyCostMaintainService.deleteProxyCostMaintainById(id);
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
            return returnFail(RespCode.DELETE_FAIL, "?????????????????????????????????????????????????????????");
        }
        return returnSuccess();
    }

    /**
     * ?????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/exportAgentCostMaintain")
    public void exportAgentCostMaintain(@RequestParam(required = false) String proxyName,
                                        @RequestParam(required = false) String companyId,
                                        @RequestParam(required = false) String startTime,
                                        @RequestParam(required = false) String endTime,
                                        @RequestParam(required = false) String phoneNo,
                                        @RequestParam(required = false) Integer businessPlatformId,
                                        @RequestParam(required = false) String proxyFeeRateStart,
                                        @RequestParam(required = false) String gearLabel,
                                        @RequestParam(required = false) String proxyFeeRateEnd,
                                        HttpServletResponse response, HttpSession session) throws Exception {

        ChannelCustom loginUser = (ChannelCustom) session.getAttribute(CommonString.CUSTOMLOGIN);
        logger.info("?????????????????????????????????,customkey :" + loginUser.getCustomkey());
        //???????????????????????????????????? businessPlatformId
        if (isPlatformAccount(loginUser)){
            businessPlatformId = loginUser.getBusinessPlatformId();
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("businessPlatformId", businessPlatformId);
        paramMap.put("proxyName", proxyName);
        paramMap.put("companyId", companyId);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("phoneNo", phoneNo);
        BigDecimal b2 = new BigDecimal("100");
        if (!StringUtil.isEmpty(proxyFeeRateStart)) {
            BigDecimal start = new BigDecimal(proxyFeeRateStart);
            proxyFeeRateStart = start.divide(b2).toString();
        }
        if (!StringUtil.isEmpty(proxyFeeRateEnd)) {
            BigDecimal end = new BigDecimal(proxyFeeRateEnd);
            proxyFeeRateEnd = end.divide(b2).toString();
        }
        paramMap.put("proxyFeeRateStart", proxyFeeRateStart);
        paramMap.put("proxyFeeRateEnd", proxyFeeRateEnd);
        paramMap.put("gearLabel", gearLabel);
        List<ProxyCostMaintain> list = proxyCostMaintainService.getProxyCostMaintainList(paramMap);

        String[] colunmName = new String[]{"???????????????","??????????????????", "???????????????", "??????????????????", "?????????????????????", "?????????????????????",
                "??????????????????", "??????????????????", "????????????", "?????????", "????????????", "?????????????????????", "????????????", "?????????????????????", "????????????",
                "??????????????????"};
        String filename = "????????????????????????";
        List<Map<String, Object>> data = new ArrayList<>();
        for (ProxyCostMaintain stringObjectMap : list) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("1", stringObjectMap.getProxyName());
            dataMap.put("2", stringObjectMap.getBusinessPlatform());
            dataMap.put("3", stringObjectMap.getProxyLevel());
            dataMap.put("4", stringObjectMap.getCompanyName());
            dataMap.put("5", stringObjectMap.getProxyFeeRate());
            String type = stringObjectMap.getProxyType() + "";
            if (!StringUtil.isEmpty(type)) {
                dataMap.put("6", ProxyProxyType.codeOf(stringObjectMap.getProxyType()).getDesc());
            } else {
                dataMap.put("6", "");
            }

            String countType = stringObjectMap.getCountType() + "";
            if (!StringUtil.isEmpty(countType)) {
                dataMap.put("7", ProxyCountType.codeOf(stringObjectMap.getCountType()).getDesc());
            } else {
                dataMap.put("7", "");
            }

            String gl = stringObjectMap.getGearLabel() + "";
            if (!StringUtil.isEmpty(gl)) {
                dataMap.put("8", ProxyGearLabel.codeOf(stringObjectMap.getGearLabel()).getDesc());
            } else {
                dataMap.put("8", "");
            }

            dataMap.put("9", stringObjectMap.getGearPosition());
            dataMap.put("10", stringObjectMap.getPhoneNo());
            dataMap.put("11", stringObjectMap.getAmountStart());
            dataMap.put("12", stringObjectMap.getOperator());
            dataMap.put("13", stringObjectMap.getAmountEnd());
            dataMap.put("14", stringObjectMap.getMasterName());
            dataMap.put("15", stringObjectMap.getCreateTime());
            dataMap.put("16", stringObjectMap.getUpdateTime());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }


    /**
     * @Author YJY
     * @Description ???????????????
     * @Date  2020/12/28
     * @Param []
     * @return void
     **/
    @PostMapping(value = "/upload/letter")
    @ResponseBody
    public APIResponse confirmationLetter(@RequestParam("id") int id,@RequestParam("fileName")String fileName,@RequestParam("file")MultipartFile file){
        if(null == file){
            return APIResponse.errorResponse(ResponseCodeMapping.ERR_532);
        }
        int count = channelHistoryService.selectCountByLetterStatus(id);
        if(count == 0){
            return APIResponse.errorResponse(599,"????????????????????????,???????????????");
        }
        InputStream in = null;
        try {
             in = file.getInputStream();
           if(!file.getOriginalFilename().contains("pdf")){
           return APIResponse.errorResponse(600,"?????????????????????PDF??????");
           }
        } catch (IOException e) {
            logger.info("????????????????????????"+e);
            return APIResponse.errorResponse(600,"?????????????????????PDF??????");
        }
        //????????????????????????
        fileName = fileName+"-"+DateUtils.getNowTime()+".pdf";
        //??????????????????
        String fileUrl = bestSignConfig.getServerNameUrl()+"/download/rechargeletter/"+fileName;
       boolean update =  channelHistoryService.uploadLetter(id,in,fileName,bestSignConfig.getFtpURL(),bestSignConfig.getUsername(),bestSignConfig.getPassword(),fileUrl);
        if(update){
            HashMap hashMap = new HashMap();
            hashMap.put("url",fileUrl);
            return APIResponse.successResponse(hashMap);
        }
        return APIResponse.errorResponse(ResponseCodeMapping.ERR_502);
    }

}
