package com.jrmf.controller.systemrole.merchant;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.PayType;
import com.jrmf.domain.*;
import com.jrmf.payment.service.ConfirmGrantService2;
import com.jrmf.service.*;
import com.jrmf.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Controller
@RequestMapping("/wallet/business")
public class WalletBusinessController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(WalletBusinessController.class);
    @Autowired
    protected UserSerivce userSerivce;
    @Autowired
    protected CustomInfoService customInfoService;
    @Autowired
    private UserCommissionService commissionService;
    @Autowired
    private ChannelHistoryService channelHistoryService;
    @Autowired
    private ChannelInvoiceService channelInvoiceService;
    @Autowired
    private TransferBankService transferBankService;
    @Autowired
    private UserRelatedService userRelatedService;
    @Autowired
    private OrderNoUtil orderNoUtil;
    @Autowired
    private ChannelRelatedService channelRelatedService;
    @Autowired
    private ChannelCustomService customService;
    @Autowired
    private ConfirmGrantService2 confirmGrantService2;
    @Autowired
    private ChannelInterimBatchService2 interimBatchService2;
    @Autowired
    private CustomBalanceService customBalanceService;


    /**
     * ??????????????????????????????
     */
    @RequestMapping(value = "/custom/account", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyAccount(HttpServletRequest request, String companyId) {
        //????????????
        String originalId = (String) request.getSession().getAttribute("customkey");

        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>(20);
        logger.info("/custom/account??????  ????????? originalId=" + originalId);
        if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(companyId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                //????????????????????????????????????????????????????????????????????????????????????????????????:?????? = 1:N--??????????????????--????????????
                ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrigAll(originalId, companyId);
                if (channelRelated == null) {
                    respstat = RespCode.error107;
                    model.put(RespCode.RESP_STAT, respstat);
                    model.put(RespCode.RESP_MSG, "????????????????????????");
                    return model;
                }
                HashMap<String, Object> param = new HashMap<>(10);
                //?????????????????????
                param.put("companyId", companyId);
                param.put("originalId", originalId);
                param.put("merchantId", originalId);

                List<CompanyPayment> paymentList = customService.getPaymentList(param, false);
                if (paymentList.size() == 0) {
                    paymentList = customService.getPaymentList(param, true);
                }

                ChannelRelated hsBankAccount = null;
                ChannelRelated aLiAccount = null;
                ChannelRelated weChatAccount = null;
                ChannelRelated bankAccount = null;
                //????????????????????????   20181109 11:22
                for (CompanyPayment payment : paymentList) {

                    String balance = channelHistoryService.getBalance(originalId, companyId, payment.getPayType());
                    if (String.valueOf(PayType.HS_BANK.getCode()).equals(payment.getPayType())) {
                        hsBankAccount = new ChannelRelated();
                        hsBankAccount.setBalance(balance);
                        hsBankAccount.setCompanyName("???????????????");
                    }

                    if (String.valueOf(PayType.ALI_PAY.getCode()).equals(payment.getPayType())) {
                        aLiAccount = new ChannelRelated();
                        aLiAccount.setBalance(balance);
                        aLiAccount.setCompanyName("???????????????");
                    }
                    if (String.valueOf(PayType.WECHAT.getCode()).equals(payment.getPayType())) {
                        weChatAccount = new ChannelRelated();
                        weChatAccount.setBalance(balance);
                        weChatAccount.setCompanyName("????????????");
                    }
                    if (String.valueOf(PayType.PINGAN_BANK.getCode()).equals(payment.getPayType())) {
                        bankAccount = new ChannelRelated();
                        bankAccount.setBalance(balance);
                        bankAccount.setCompanyName("???????????????");
                    }
                }

                model.put("hsBankAccount", hsBankAccount);
                model.put("aLiAccount", aLiAccount);
                model.put("weChatAccount", weChatAccount);
                model.put("bankAccount", bankAccount);

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


    @RequestMapping(value = "/custom/new/account", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyAccountNew(HttpServletRequest request, String companyId) {

        String batchId = request.getParameter("batchId");

        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>(20);
        logger.info("/custom/account??????  ????????? batchId=" + batchId);
        if (StringUtil.isEmpty(batchId) || StringUtil.isEmpty(companyId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {

                ChannelInterimBatch batch = interimBatchService2.getChannelInterimBatchByOrderno(batchId, "");
                String customKey = batch.getCustomkey();

                ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrigAll(customKey, companyId);
                if (channelRelated == null) {
                    respstat = RespCode.error107;
                    model.put(RespCode.RESP_STAT, respstat);
                    model.put(RespCode.RESP_MSG, "????????????????????????");
                    return model;
                }
                HashMap<String, Object> param = new HashMap<>(10);
                //?????????????????????
                param.put("companyId", companyId);
                param.put("originalId", customKey);
                param.put("merchantId", customKey);

                List<CompanyPayment> paymentList = customService.getPaymentList(param, false);
                if (paymentList.size() == 0) {
                    paymentList = customService.getPaymentList(param, true);
                }

                ChannelRelated hsBankAccount = null;
                ChannelRelated aLiAccount = null;
                ChannelRelated weChatAccount = null;
                ChannelRelated bankAccount = null;
                //????????????????????????   20181109 11:22
                for (CompanyPayment payment : paymentList) {

                    String balance = channelHistoryService.getBalance(customKey, companyId, payment.getPayType());
                    if (String.valueOf(PayType.HS_BANK.getCode()).equals(payment.getPayType())) {
                        hsBankAccount = new ChannelRelated();
                        hsBankAccount.setBalance(balance);
                        hsBankAccount.setCompanyName("???????????????");
                    }

                    if (String.valueOf(PayType.ALI_PAY.getCode()).equals(payment.getPayType())) {
                        aLiAccount = new ChannelRelated();
                        aLiAccount.setBalance(balance);
                        aLiAccount.setCompanyName("???????????????");
                    }
                    if (String.valueOf(PayType.WECHAT.getCode()).equals(payment.getPayType())) {
                        weChatAccount = new ChannelRelated();
                        weChatAccount.setBalance(balance);
                        weChatAccount.setCompanyName("????????????");
                    }
                    if (String.valueOf(PayType.PINGAN_BANK.getCode()).equals(payment.getPayType())) {
                        bankAccount = new ChannelRelated();
                        bankAccount.setBalance(balance);
                        bankAccount.setCompanyName("???????????????");
                    }
                }

                model.put("hsBankAccount", hsBankAccount);
                model.put("aLiAccount", aLiAccount);
                model.put("weChatAccount", weChatAccount);
                model.put("bankAccount", bankAccount);

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
     * ????????????
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/custom/accountMessage")
    public @ResponseBody
    Map<String, Object> details(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();
        String payType = (String) request.getParameter("payType");
        String companyId = (String) request.getParameter("companyId");
        if (StringUtils.isEmpty(payType)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
            return result;
        }
        try {
            String customkey = (String) request.getSession().getAttribute("customkey");
            ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(customkey, companyId);
            ChannelCustom channelCustom = customService.getCustomByCustomkey(related.getCompanyId());

            Map<String, Object> param = new HashMap<>();
            param.put("payType", payType);
            param.put("channelId", channelCustom.getId());
            ChannelConfig channelConfig = customService.getChannelConfigByParam(param);
            if (channelConfig == null) {
                respstat = RespCode.error107;
                result.put(RespCode.RESP_STAT, respstat);
                result.put(RespCode.RESP_MSG, "???????????????????????????");
                return result;
            }
            result.put("bankcardno", channelConfig.getAccountNum());
            result.put("bankname", channelConfig.getBankName());
            result.put("companyName", channelCustom.getCompanyName());
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "??????");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            respstat = RespCode.error107;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
            return result;
        }
        return result;
    }

    /**
     * ?????????????????????
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/custom/originalData")
    public @ResponseBody
    Map<String, Object> originalData(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>();
        String payType = (String) request.getParameter("payType");
        if (StringUtils.isEmpty(payType)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
            return result;
        }
        try {
            String customkey = (String) request.getSession().getAttribute("customkey");
            ChannelCustom channelCustom = customService.getCustomByCustomkey(customkey);

            result.put("bankcardno", channelCustom.getBankcardno());
            result.put("bankname", channelCustom.getBankname());
            result.put("companyName", channelCustom.getCompanyName());
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "??????");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            respstat = RespCode.error107;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
            return result;
        }
        return result;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/custom/accountList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyAccountList(HttpServletRequest request, HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        //????????????
        String originalId = (String) request.getSession().getAttribute("customkey");
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>(15);
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
//        String pageNo = request.getParameter("pageNo");
        String amount = request.getParameter("amount");
        String payType = request.getParameter("payType");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(originalId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<>(15);
                paramMap.put("customkey", originalId);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                paramMap.put("status", status);
                paramMap.put("amount", amount);
                paramMap.put("payType", payType);
//                List<ChannelHistory> totalList = channelHistoryService.getChannelHistoryBySubmit(paramMap);
//                int total = totalList == null ? 0 : totalList.size();
//                int pageSize = 10;
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<ChannelHistory> list = channelHistoryService.getChannelHistoryBySubmit(paramMap);
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
     * ????????????
     *
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value = "/user/listData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> listData(HttpServletResponse response, HttpServletRequest request,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        String originalId = (String) request.getSession().getAttribute(
                "customkey");//????????????
        Map<String, Object> model = new HashMap<>();
        String userType = (String) request.getParameter("userType");//???????????????1?????? ??? 2?????????  0????????? -1  ?????????  11  ????????????  ,-2???????????????
        String startTime = (String) request.getParameter("startTime");
        String endTime = (String) request.getParameter("endTime");
        String userName = (String) request.getParameter("userName");
//        String pageNo = (String) request.getParameter("pageNo");
        String status = (String) request.getParameter("status");
        logger.info("userList??????  ?????????startTime=" + startTime + " endTime=" + endTime + "customkey=" + originalId + "pageNo=" + pageNo);
        if (StringUtil.isEmpty(originalId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("userType", userType);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                paramMap.put("userName", userName);
                paramMap.put("originalId", originalId);
                paramMap.put("status", status);

//                int total = userSerivce.getUserRelatedCountByParam(paramMap);
//                model.put("total", total);
//                int pageSize = 10;
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<User> userList = userSerivce.getUserRelatedByParam(paramMap);
                PageInfo<User> pageInfo = new PageInfo<>(userList);
                model.put("userList", pageInfo.getList());
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
     * ??????????????????
     *
     * @param model
     * @param startTime
     * @param endTime
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/user/exportUserData")
    public void exportUserData(ModelMap model, String startTime, String endTime, String status,
                               String userType, String userName, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String customkey = (String) request.getSession().getAttribute("customkey");// ????????????
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userType", userType);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("userName", userName);
        paramMap.put("originalId", customkey);
        paramMap.put("status", status);
        List<User> list = userSerivce.getUserRelatedByParam(paramMap);
        String today = DateUtils.getNowDay();
        ArrayList<String> dataStr = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            StringBuffer strBuff = new StringBuffer();
            String role = "";
            String payStatus = "";
            int UserType = user.getUserType();
            if (UserType == 1) {
                role = "?????????";
            } else {
                role = "?????????";
            }

            String type = user.getStatus();
            if ("1".equals(type)) {
                payStatus = "??????";
            } else {
                payStatus = "?????????";
            }
            strBuff.append(user.getCreateTime() == null ? "" : user.getCreateTime())
                    .append(",")
                    .append(user.getUserNo() == null ? "" : user.getUserNo())
                    .append(",")
                    .append(user.getUserName() == null ? "" : user.getUserName())
                    .append(",")
                    .append(user.getCertId() == null ? "" : user.getCertId())
                    .append(",")
                    .append(user.getBankNo() == null ? "" : user.getBankNo())
                    .append(",")
                    .append(user.getMobilePhone() == null ? "" : user.getMobilePhone())
                    .append(",")
                    .append(role)
                    .append(",")
                    .append(payStatus);

            dataStr.add(strBuff.toString());
        }
        ArrayList<String> fieldName = new ArrayList<>();
        fieldName.add("????????????");
        fieldName.add("????????????ID");
        fieldName.add("??????");
        fieldName.add("???????????????");
        fieldName.add("????????????");
        fieldName.add("?????????");
        fieldName.add("???????????????");
        fieldName.add("????????????");
        String filename = today + "????????????";
        ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
    }


    /**
     * ??????????????????
     */
    @RequestMapping(value = "/openAccount/inputData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> inputData(HttpServletRequest request,
                                  HttpServletResponse response, String name, MultipartFile file) throws Exception {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String originalId = (String) request.getSession().getAttribute("customkey");//????????????
        String companyId = request.getParameter("companyId");
        int passNum = 0;
        int batchNum = 0;
        int errorNum = 0;
        String batcheId = StringUtil.formatDate(new Date(), "yyyyMMddHHmmss");
        model.put("batchId", batcheId);
        try {
            if (file != null) {
                InputStream is = file.getInputStream();
                Workbook workbook = null;
                try {
                    workbook = new XSSFWorkbook(is);
                } catch (Exception ex) {
                    workbook = new HSSFWorkbook(is);
                }
                Sheet sheet = workbook.getSheetAt(0);
                /**
                 * ?????????????????????
                 */
                ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(originalId, companyId);
                if (related == null) {
                    logger.info("???????????????" + model);
                    respstat = RespCode.error107;
                    model.put(RespCode.RESP_STAT, respstat);
                    model.put(RespCode.RESP_MSG, "?????????????????????????????????????????????????????????");
                    return model;
                }
                /**
                 * ?????????????????????
                 */
                XSSFRow title = (XSSFRow) sheet.getRow(0);
                if (title != null) {
                    String titleName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(title.getCell(0)));
                    if (!titleName.contains("????????????")) {
                        respstat = RespCode.error107;
                        model.put(RespCode.RESP_STAT, respstat);
                        model.put(RespCode.RESP_MSG, "????????????????????????????????????????????????");
                        return model;
                    }
                }
                /**
                 * ????????????amount,idCard, bankCard, mobileNo, name
                 */
                int num = 0;
                Set<String> result = new HashSet<>();
                for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {// ????????????
                    XSSFRow row = (XSSFRow) sheet.getRow(j);
                    if (row == null) {
                        continue;
                    }
                    if (StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0))) &&
                            StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1))) &&
                            StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(2)))) {
                        continue;
                    }
                    ++num;
                    if (num > 2000 || num < 0) {
                        respstat = RespCode.error107;
                        model.put(RespCode.RESP_STAT, respstat);
                        model.put(RespCode.RESP_MSG, "????????????????????????2000????????????0????????????????????????");
                        return model;
                    }
                    String msg = commissionService.isValidateData(null,
                            StringUtil.getXSSFCell(row.getCell(1)),
                            StringUtil.getXSSFCell(row.getCell(2)),
                            StringUtil.getXSSFCell(row.getCell(3)),
                            StringUtil.getXSSFCell(row.getCell(0)));
                    String userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));// ?????????????????????(??????)
                    String cardNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(1)));//???????????????
                    if (StringUtil.isEmpty(userName + cardNo)) {
                        continue;
                    }
                    if (!result.add(userName + cardNo)) {
                        respstat = RespCode.error107;
                        model.put(RespCode.RESP_STAT, respstat);
                        model.put(RespCode.RESP_MSG, userName + ":???????????????????????????????????????");
                        return model;
                    }
                    if (!StringUtil.isEmpty(msg)) {
                        respstat = RespCode.error107;
                        model.put(RespCode.RESP_STAT, respstat);
                        model.put(RespCode.RESP_MSG, userName + ":" + msg);
                        return model;
                    }
                }
                for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {//????????????
                    XSSFRow row = (XSSFRow) sheet.getRow(j);
                    if (row == null) {
                        break;
                    }
                    if (StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0))) &&
                            StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1))) &&
                            StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(2)))) {
                        continue;
                    }
                    batchNum++;
                    String userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));//??????
                    String cardNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(1)));//???????????????
                    String bankcardNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(2)));//???????????????
                    String mobile = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(3)));//?????????
                    String userNo = ArithmeticUtil.subZeroAndDot(row.getCell(4) + "");//????????????id
                    if (StringUtil.isEmpty(userNo)) {
                        userNo = mobile;
                    }
                    /**
                     * ???????????????  ?????????????????????
                     */
                    if (StringUtil.isEmpty(cardNo) && StringUtil.isEmpty(userName)
                            && StringUtil.isEmpty(bankcardNo) && StringUtil.isEmpty(mobile)) {
                        createErrorUser(cardNo, userName, mobile, userNo, related.getMerchantId(),
                                related.getCompanyId(), "??????????????????", batcheId, bankcardNo, "");
                        errorNum++;
                        continue;
                    }
                    /**
                     * ????????????
                     */
                    userName = userName.replace(" ", "");
                    cardNo = cardNo.replace(" ", "");
                    bankcardNo = bankcardNo.replace(" ", "");
                    mobile = mobile.replace(" ", "");

                    /**
                     * ???????????????????????????
                     */
                    BankCard bankInfo = transferBankService.getBankInfo(bankcardNo);
                    if (bankInfo == null) {
                        createErrorUser(cardNo, userName, mobile, userNo, related.getMerchantId(),
                                related.getCompanyId(), "?????????????????????", batcheId, bankcardNo, "");
                        errorNum++;
                        continue;
                    }

                    /**
                     *  ????????????????????????????????????????????????
                     */
                    Map<String, Object> paramMap = new HashMap<String, Object>();
                    paramMap.put("merchantId", related.getMerchantId());
                    paramMap.put("mobilePhone", mobile);
                    paramMap.put("userType", 1);
                    paramMap.put("originalId", originalId);
                    int size = userSerivce.getUsersCountByParam(paramMap);
                    if (size > 0) {
                        createErrorUser(cardNo, userName, mobile, userNo, related.getMerchantId(),
                                related.getCompanyId(), "?????????????????????????????????????????????", batcheId, bankcardNo, bankInfo.getBankNo() + "");
                        errorNum++;
                        continue;
                    }
                    /**
                     * ?????????????????????????????????????????????????????????
                     */
                    paramMap.clear();
                    paramMap.put("merchantId", related.getMerchantId());
                    paramMap.put("certId", cardNo);
                    paramMap.put("userName", userName);
                    paramMap.put("userType", 1);
                    paramMap.put("originalId", originalId);
                    int size1 = userSerivce.getUsersCountByParam(paramMap);
                    if (size1 > 0) {
                        createErrorUser(cardNo, userName, mobile, userNo, related.getMerchantId(),
                                related.getCompanyId(), "?????????????????????????????????", batcheId, bankcardNo, bankInfo.getBankNo() + "");
                        errorNum++;
                        continue;
                    }
                    passNum++;
                    /**
                     * ??????1???????????????????????????????????????
                     */
                    paramMap.clear();
                    paramMap.put("merchantId", related.getMerchantId());
                    paramMap.put("certId", cardNo);
                    paramMap.put("userName", userName);
                    paramMap.put("userType", 1);
                    int size2 = userSerivce.getUsersCountByParam(paramMap);
                    if (size2 > 0) {
                        paramMap.clear();
                        paramMap.put("userName", userName);
                        paramMap.put("certId", cardNo);
                        User oldUser = userSerivce.getUsersCountByCard(paramMap);
                        UserRelated oldRelated = userRelatedService.getRelatedByUserId(oldUser.getId() + "", originalId);
                        if (oldRelated == null) {
                            /**
                             * ????????????-??????????????????
                             */
                            UserRelated userRelated = new UserRelated();
                            userRelated.setStatus(0);//????????????????????????
                            userRelated.setCreateTime(DateUtils.getNowDate());
                            userRelated.setOriginalId(originalId);
                            userRelated.setUserNo(userNo);
                            userRelated.setUserId(oldUser.getId());
                            userRelated.setCompanyId(related.getCompanyId());
                            userRelatedService.createUserRelated(userRelated);
                        }
                        continue;
                    }
                    /**
                     * ??????2????????????????????????????????????????????????????????????????????????????????????????????????
                     */
                    createSuccessUser(originalId, cardNo, userName, mobile, userNo,
                            related.getMerchantId(), related.getCompanyId(), "", batcheId, bankcardNo, bankInfo.getBankNo() + "");
                    continue;


                }
                UserBatch batch = new UserBatch();
                batch.setBatchId(batcheId);
                batch.setBatchNum(batchNum);
                batch.setCustomkey(originalId);
                batch.setPassNum(passNum);
                batch.setErrorNum(errorNum);
                userSerivce.addUserBatch(batch);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error(e.getMessage());
            respstat = RespCode.error107;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
            logger.error(e.getMessage(), e);
            return model;
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + model);
        return model;
    }

    /**
     * ????????????--????????????
     *
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value = "/openAccount/batchData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> openAccountBatchData(HttpServletResponse response, HttpServletRequest request) {
        int respstat = RespCode.success;
        String originalId = (String) request.getSession().getAttribute(
                "customkey");//????????????
        Map<String, Object> model = new HashMap<>();
        String batchId = (String) request.getParameter("batchId");
        if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(batchId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                UserBatch batch = userSerivce.getUserBatchByBatchId(batchId);
                model.put("batch", batch);
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
     *
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value = "/openAccount/batchList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> openAccountBatchList(HttpServletResponse response, HttpServletRequest request,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        String originalId = (String) request.getSession().getAttribute(
                "customkey");//????????????
        Map<String, Object> model = new HashMap<>();
        String startTime = (String) request.getParameter("startTime");
        String endTime = (String) request.getParameter("endTime");
        String name = (String) request.getParameter("name");
//        String pageNo = (String) request.getParameter("pageNo");
        if (StringUtil.isEmpty(originalId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                paramMap.put("name", name);
                paramMap.put("customkey", originalId);
//                int total = userSerivce.getUserBatchByParam(paramMap).size();
//                model.put("total", total);
//                int pageSize = 10;
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<UserBatch> list = userSerivce.getUserBatchByParam(paramMap);
                PageInfo<UserBatch> pageInfo = new PageInfo<>(list);
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
     * ??????????????????
     *
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value = "/openAccount/userList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyUserList(HttpServletResponse response, HttpServletRequest request,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        String originalId = (String) request.getSession().getAttribute(
                "customkey");//????????????
        Map<String, Object> model = new HashMap<>();
        String userName = (String) request.getParameter("userName");
        String userType = (String) request.getParameter("userType");
        String batcheId = (String) request.getParameter("batcheId");
//        String pageNo = (String) request.getParameter("pageNo");
        if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(batcheId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("userType", userType);
                paramMap.put("batcheId", batcheId);
                paramMap.put("userName", userName);
//                int total = userSerivce.getUserRelatedCountByParam(paramMap);
//                int pageSize = 10;
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<User> userList = userSerivce.getUserRelatedByParam(paramMap);
                PageInfo<User> pageInfo = new PageInfo<>(userList);
                model.put("userList", pageInfo.getList());
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
     * ????????????????????????
     *
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value = "/openAccount/deleteUser", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> deleteUser(HttpServletResponse response, HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");//????????????
        String ids = request.getParameter("ids");
        String batchId = request.getParameter("batchId");
        logger.info("deleteUser??????  ????????? ids=" + ids + "customkey=" + customkey);
        if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(ids)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                User user = userSerivce.getUserByUserId(Integer.parseInt(ids));
                userSerivce.deleteByIds(ids);
                transferBankService.deleteByUserIds(ids);
                userRelatedService.deleteByOriginalId(ids, customkey);
                UserBatch userBatch = userSerivce.getUserBatchByBatchId(batchId);
                int passnum = userBatch.getPassNum();
                int errornum = userBatch.getErrorNum();
                int batchnum = userBatch.getBatchNum();
                if (user.getUserType() == 11) {
                    passnum = passnum - 1;
                } else if (user.getUserType() == 0) {
                    errornum = errornum - 1;
                } else if (user.getUserType() == 1) {
                    passnum = passnum - 1;
                }
                batchnum = batchnum - 1;
                userSerivce.updateUserBatch(batchId, passnum, batchnum, errornum);
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
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value = "/openAccount/deleteBatch", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> deleteBatch(HttpServletResponse response, HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");//????????????
        String batchId = request.getParameter("batchId");
        if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(batchId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                userSerivce.deleteByBatcheId(batchId, customkey);
                transferBankService.deleteByBatcheId(batchId, customkey);
                userRelatedService.deleteByBatchId(batchId, customkey);
                userSerivce.deleteUserBatch(batchId);
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
     * ??????????????????
     *
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value = "/commission/deleteBatch", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> commissionDeleteBatch(HttpServletResponse response, HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");//????????????
        String batchId = request.getParameter("batchId");
        if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(batchId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                commissionService.deleteByBatchId(batchId);
                channelHistoryService.deleteByOrderno(batchId);
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
     * ??????????????????
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/download/userTemplate")
    public ResponseEntity<byte[]> userTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String prifix = "/excel/userTemplate.xls";
        String fileName = prifix.substring(prifix.lastIndexOf("/")+1);
        byte[] bytes = FtpTool.downloadFtpFile(prifix.substring(0, prifix.lastIndexOf("/")), fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// ??????????????????
        headers.add("Content-Disposition", "attachment;filename=" + fileName);
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<byte[]>(bytes,
                headers, HttpStatus.OK);
    }

    /**
     * ??????????????????
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/download/commissionTemplate")
    public ResponseEntity<byte[]> commissionTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String prifix = "/excel/commissionTemplate.xlsx";
        String fileName = prifix.substring(prifix.lastIndexOf("/")+1);
        byte[] bytes = FtpTool.downloadFtpFile(prifix.substring(0, prifix.lastIndexOf("/")), fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// ??????????????????
        headers.add("Content-Disposition", "attachment;filename=" + fileName);
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<byte[]>(bytes,
                headers, HttpStatus.OK);
    }

    /**
     * ????????????????????????
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/download/bankPayTemplate")
    public ResponseEntity<byte[]> bankPayTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String prifix = "/excel/bankPayTemplate.xlsx";
        String fileName = prifix.substring(prifix.lastIndexOf("/")+1);
        byte[] bytes = FtpTool.downloadFtpFile(prifix.substring(0, prifix.lastIndexOf("/")), fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// ??????????????????
        headers.add("Content-Disposition", "attachment;filename=" + fileName);
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<byte[]>(bytes,
                headers, HttpStatus.OK);
    }

    /**
     * ?????????????????????
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/download/aliCommissionTemplate")
    public ResponseEntity<byte[]> aliCommissionTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String prifix = "/excel/aliCommissionTemplate.xlsx";
        String fileName = prifix.substring(prifix.lastIndexOf("/")+1);
        byte[] bytes = FtpTool.downloadFtpFile(prifix.substring(0, prifix.lastIndexOf("/")), fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// ??????????????????
        headers.add("Content-Disposition", "attachment;filename=" + fileName);
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<byte[]>(bytes,
                headers, HttpStatus.OK);
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = "/hsBank/inputCommission", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> inputCommission(HttpServletRequest request,
                                        HttpServletResponse response, String name, MultipartFile file) throws Exception {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");//????????????
        ChannelCustom loginUser = (ChannelCustom) request.getSession()
                .getAttribute("customLogin");
        String operatorName = loginUser.getUsername();//?????????
        if (StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        }
        try {
            if (file != null) {

                InputStream is = file.getInputStream();
                Workbook workbook = null;
                try {
                    workbook = new XSSFWorkbook(is);
                } catch (Exception ex) {
                    workbook = new HSSFWorkbook(is);
                }
                model = commissionService.inputHsBankCommissionDate(workbook, customkey, operatorName);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error(e.getMessage());
            respstat = RespCode.error107;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + model);
        return model;
    }

    /**
     * ??????: ??????????????????????????????????????????
     *
     * @param request
     * @param response
     * @param file
     * @return:
     */
    @RequestMapping("/banbkPay/inputBatchInfo")
    @ResponseBody
    @Deprecated
    public Map<String, Object> inputBatchInfo(HttpServletRequest request,
                                              HttpServletResponse response,
                                              MultipartFile file) {

        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String customkey = (String) request.getSession().getAttribute("customkey");// ????????????
        String menuId = request.getParameter("menuId");//??????id
        String batchName = request.getParameter("batchName");//????????????
        String batchDesc = request.getParameter("batchDesc");//????????????
        int payType = Integer.valueOf(request.getParameter("payType"));//????????????
        String companyId = request.getParameter("companyId");//??????id
        ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(customkey, companyId);

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
        String operatorName = loginUser.getUsername();//?????????

        if (StringUtil.isEmpty(customkey) || channelRelated == null) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "???????????????");
            return model;
        }
        if (StringUtil.isEmpty(menuId) && StringUtil.isEmpty(batchName)
                && StringUtil.isEmpty(batchDesc)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "????????????????????????");
            return model;
        }

        InputStream is = null;
        Workbook workbook = null;
        ByteArrayOutputStream bytesOut = null;
        try {
            if (file != null) {
                is = file.getInputStream();

                int readLen = -1;
                byte[] byteBuffer = new byte[1024];
                bytesOut = new ByteArrayOutputStream();

                while ((readLen = is.read(byteBuffer)) > -1) {
                    bytesOut.write(byteBuffer, 0, readLen);
                }
                byte[] fileData = bytesOut.toByteArray();

                try {
                    workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData));
                } catch (Exception ex) {
                    workbook = new HSSFWorkbook(new ByteArrayInputStream(fileData));
                }

                Map<String, String> batchData = new HashMap<String, String>();
                batchData.put("operatorName", operatorName);
                batchData.put("batchName", batchName);
                batchData.put("menuId", menuId);
                batchData.put("fileName", file.getName());
                batchData.put("batchDesc", batchDesc);
                batchData.put("customkey", customkey);

                model = confirmGrantService2.inputCommissionData(payType, null, batchData);

            } else {
                respstat = RespCode.error101;
                model.put(RespCode.RESP_STAT, respstat);
                model.put(RespCode.RESP_MSG, "???????????????????????????");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            respstat = RespCode.error101;
            logger.error(e.getMessage());
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "????????????");
            return model;
        } finally {
            try {
                if (bytesOut != null) {
                    bytesOut.close();
                    if (is != null) {
                        is.close();
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
        logger.info("???????????????" + model);
        return model;
    }


    /**
     * ??????????????????--??????
     */
    @RequestMapping(value = "/commonOption", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> bankPayCommonOption(HttpServletRequest request,
                                            HttpServletResponse response) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();
        String originalId = (String) request.getSession().getAttribute("customkey");//????????????
        String batchId = request.getParameter("batchId");//??????id
        String tranPassword = request.getParameter("tranPassword");
        String remark = request.getParameter("remark");//????????????
        ChannelCustom loginUser = (ChannelCustom) request.getSession()
                .getAttribute("customLogin");
        String operatorName = loginUser.getUsername();//?????????
        logger.info("/bankPay/commonOption ??????  ?????????batchId=" + batchId +
                ",operatorName=" + operatorName);
        if (StringUtil.isEmpty(batchId) | StringUtil.isEmpty(operatorName)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "????????????");
            return model;
        } else {
            try {

                /**
                 * ??????????????????
                 */
                if (loginUser.getTranPassword() != null && !loginUser.getTranPassword().equals(CipherUtil.generatePassword(tranPassword, originalId))) {
                    respstat = RespCode.error101;
                    model.put(RespCode.RESP_STAT, respstat);
                    model.put(RespCode.RESP_MSG, "?????????????????????");
                    return model;
                }
                model = commissionService.transfer(originalId, batchId, remark, operatorName);
            } catch (Exception e) {
                respstat = RespCode.error107;
                model.put(RespCode.RESP_STAT, respstat);
                model.put("errorMessage", "???????????????");
                logger.error(e.getMessage(), e);
                logger.info(e.getMessage(), e);
                return model;
            }
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "????????????");
        logger.info("???????????????" + model);
        return model;
    }

    /**
     * ??????: ???????????????????????????????????????
     *
     * @param request
     * @param file
     * @return:
     */
    @RequestMapping("/alipay/inputBatchInfo")
    @ResponseBody
    public Map<String, Object> alipayInputBatchInfo(HttpServletRequest request,
                                                    MultipartFile file) {

        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();

//        String nodeCustomKey = (String) request.getParameter("customkey");//???????????????????????????key
        String customkey = (String) request.getSession().getAttribute("customkey");// ????????????
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
        String operatorName = loginUser.getUsername();//?????????

        String menuId = request.getParameter("menuId");//??????id
        String batchName = request.getParameter("batchName");//????????????
        String batchDesc = request.getParameter("batchDesc");//????????????
        int payType = Integer.valueOf(request.getParameter("payType"));//????????????
        String companyId = request.getParameter("companyId");//??????id
        ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(customkey, companyId);

        if (StringUtil.isEmpty(customkey) || channelRelated == null) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "???????????????");
            return model;
        }

        if (file != null) {
            try {
                model = customService.alipayInputBatchInfo(respstat, operatorName, customkey, menuId, payType, companyId, batchName, batchDesc, file.getInputStream(), file.getOriginalFilename(), model);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "???????????????????????????");
        }

        return model;
    }

    @RequestMapping("/alipay/new/inputBatchInfo")
    @ResponseBody
    public Map<String, Object> alipayInputBatchInfoNew(HttpServletRequest request,
                                                       HttpServletResponse response,
                                                       MultipartFile file) {

        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>();

        String nodeCustomKey = request.getParameter("customkey");//???????????????????????????key
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
        String operatorName = loginUser.getUsername();//?????????

        String menuId = request.getParameter("menuId");//??????id
        String batchName = request.getParameter("batchName");//????????????
        String batchDesc = request.getParameter("batchDesc");//????????????
        int payType = Integer.valueOf(request.getParameter("payType"));//????????????
        String companyId = request.getParameter("companyId");//??????id
        String realCompanyId = request.getParameter("realCompanyId");//????????????
        ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(nodeCustomKey, companyId);

        if (channelRelated == null) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "???????????????");
            return model;
        }

        if (file != null) {
            try {
                model = customService.alipayInputBatchInfoNew(respstat, operatorName, nodeCustomKey, menuId, payType, companyId, batchName, batchDesc, file.getInputStream(), file.getName(), model,realCompanyId);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "???????????????????????????");
        }


        return model;
    }

    /**
     * ??????:??????????????????????????????????????? ??????????????????
     *
     * @param response
     * @return:
     */
    @RequestMapping(value = "/alipay/batchTransferAccounts", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> batchTransferAccounts(HttpServletRequest request, HttpServletResponse response) {
//		int respstat = RespCode.success;
//		Map<String, Object> model = new HashMap<>();
//		String customkey = (String) request.getSession().getAttribute("customkey");// ????????????originalId
//		String remark = (String) request.getParameter("remark");// ????????????
//		String batchId = (String) request.getParameter("batchId");// ???????????????
//		String tranPassword = (String) request.getParameter("tranPassword");//????????????
//		ChannelCustom loginUser = (ChannelCustom) request.getSession()
//				.getAttribute("customLogin");
//		if (StringUtil.isEmpty(customkey) || StringUtil.isEmpty(batchId) || StringUtil.isEmpty(tranPassword)) {
//			respstat = RespCode.error101;
//			model.put(RespCode.RESP_STAT, respstat);
//			model.put(RespCode.RESP_MSG, "??????????????????");
//			return model;
//		}
//		logger.info("/channel/commonOption ??????  ????????? customkey=" + customkey + ",batchId=" + batchId );
//
//		/**
//		 * ??????????????????
//		 */
//		if(loginUser.getTranPassword() != null && !loginUser.getTranPassword().equals(CipherUtil.generatePassword(tranPassword,customkey))){
//			respstat = RespCode.error101;
//			model.put(RespCode.RESP_STAT, respstat);
//			model.put(RespCode.RESP_MSG, "?????????????????????");
//			return model;
//		}
//		// ???????????????????????????
//		String amountSum = "0.00";// ???????????????
//		String mfkjServiceFee = "0.00";// ?????????????????????
//		String batchServiceFee = "0.00";// ????????????
//
//		Map<String, Object> map = new HashMap<>(3);
//		map.put("batchId", batchId);
//		//???????????????????????????
//		List<UserCommission> list = commissionService.getUserCommissionByParam(map);
//		ChannelRelated channelRelated = channelRelatedService.getRelatedByEnable(customkey);//????????????
//		ChannelCustom custom = customService.getCustomByCustomkey(customkey);//????????????
//		for (UserCommission user : list) {
//			String user_amount = user.getAmount();
//			String commissionMfkjFree = "0";
//			// ??????
//			if (ArithmeticUtil.compareTod(user_amount, baseInfo.getCalculationLimit()) < 0) {
//				commissionMfkjFree = ArithmeticUtil.mulStr(user_amount, channelRelated.getProfiltLower(), 2);
//			} else {
//				commissionMfkjFree = ArithmeticUtil.mulStr(user_amount, channelRelated.getProfiltUpper(), 2);
//			}
//			// ?????????
//			String commissionFree = ArithmeticUtil.mulStr(user_amount, channelRelated.getServiceRates(), 2);
//
//			amountSum = ArithmeticUtil.addStr(amountSum, user_amount);// ?????????
//			batchServiceFee = ArithmeticUtil.addStr(batchServiceFee, commissionFree);// ????????????
//			mfkjServiceFee = ArithmeticUtil.addStr(mfkjServiceFee, commissionMfkjFree);// ??????????????????
//		}
//		String handleAmount = ArithmeticUtil.addStr(amountSum, batchServiceFee);
//		// ????????????
//		String balance = channelHistoryService.getBalance(customkey,channelRelated.getCompanyId() ,"2"); //????????????2 ???????????????
//		if (ArithmeticUtil.compareTod(balance, ArithmeticUtil.addStr(amountSum, batchServiceFee))>0) {
//			String orderNo = StringUtil.getChannelSerialno();// ???????????????????????????????????????
//			//??????????????????
//			ChannelHistory history = new ChannelHistory();
//			history.setAmount(amountSum);
//			history.setHandleAmount(handleAmount);
//			history.setAccountName(custom.getCompanyName());
//			history.setCustomkey(customkey);
//			// ??????????????????userId
//			history.setRecCustomkey(channelRelated.getCompanyId());
//			history.setOrdername("????????????");
//			history.setOperatorName(loginUser.getUsername());
//			history.setOrderno(orderNo);
//			history.setStatus(3);
//			history.setRemark(remark);
//			// ?????????
//			history.setPayType(2);
//			// ???????????? ????????????
//			history.setTransfertype(2);
//			// ?????????
//			history.setServiceFee(batchServiceFee);
//			//?????????????????????????????????
//			history.setMfkjServiceFee(mfkjServiceFee);
//			channelHistoryService.addChannelHistory(history);
//			//???????????????????????????
//			channelHistoryService.deleteByOrderno(batchId);
//			//????????????????????????
//			String newBatchId = history.getId()+"";
////			commissionService.updateUserCommissionByBacthId(newBatchId,batchId);
////			model.put("batchNo", newBatchId); //????????????
//			//??????????????????????????????????????????
//			for (UserCommission userCommission : list) {
//				userCommission.setStatus(3);//????????????????????????
//				userCommission.setBatchId(newBatchId);//??????batchId
//				commissionService.updateUserCommissionBatchIdAndStatus(userCommission);
//			}
//			// ?????????????????????????????????
//			List<Map<String, Object>> param = new ArrayList<Map<String, Object>>();
//			for (UserCommission userCommission : list) {
//				if(userCommission.getStatus()!=3){
//					continue;
//				}
//
//				Map<String, Object> param_map = new HashMap<String, Object>();
//				User user2 = userSerivce.getUserByUserId(Integer.parseInt(userCommission.getUserId()));
//
//				param_map.put("out_biz_no", userCommission.getOrderNo());
//				param_map.put("payee_type", AlipayConfigUtil.getLoginID());
//				param_map.put("payee_account", userCommission.getAccount());
//				param_map.put("amount", userCommission.getAmount());
//				param_map.put("payer_show_name", AlipayConfigUtil.getPayerShowName());
//				param_map.put("payee_real_name", user2.getUserName());
//				param_map.put("remark","??????");
//				param.add(param_map);
//			}
//			if(param.size()==0){
//				ChannelHistory history1 = channelHistoryService.getChannelHistoryById(newBatchId);
//				if(history1.getStatus()==0){
//					history.setStatus(2);
//					channelHistoryService.updateChannelHistory(history);
//				}
//				model.put(RespCode.RESP_STAT, respstat);
//				model.put(RespCode.RESP_MSG, "???????????????????????????????????????");
//				return model;
//			}
//			List<List<Map<String, Object>>> averageAssign = averageAssign(param, 5);
//			CyclicBarrier cb = new CyclicBarrier(averageAssign.size(), new CountAliPayData(newBatchId,commissionService, channelHistoryService));
//			for (int i = 0; i < averageAssign.size(); i++) {
//				new Thread(new ExecuteBatch(cb,alipayServiceImpl,averageAssign.get(i),commissionService)).start();
//			}
//		} else {
//			respstat = RespCode.error107;
//			model.put(RespCode.RESP_STAT, respstat);
//			model.put(RespCode.RESP_MSG, "?????????????????????");
//			return model;
//		}
//		model.put(RespCode.RESP_STAT, respstat);
//		model.put(RespCode.RESP_MSG, "???????????????????????????????????????");
        return null;
    }

    /**
     * ????????????--????????????????????????
     *
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value = "/company/deleteCommission", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> deleteCommission(HttpServletResponse response, HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String originalId = (String) request.getSession().getAttribute("customkey");//????????????
        String id = (String) request.getParameter("id");//??????id
        String batchId = (String) request.getParameter("batchId");//??????Id
        logger.info("deleteCommission??????  ????????? id=" + id);
        if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(id) || StringUtil.isEmpty(batchId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                commissionService.deleteById(Integer.parseInt(id));
                /**
                 * ????????????????????????
                 */
                commissionService.updateBatchMessage(batchId, originalId, model);
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
     * ????????????
     *
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value = "/company/batchHang", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> batchHang(HttpServletResponse response, HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String originalId = (String) request.getSession().getAttribute("customkey");//????????????
        String batchId = (String) request.getParameter("batchId");//??????Id
        if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(batchId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(batchId);
                channelHistory.setStatus(6);
                channelHistoryService.updateChannelHistory(channelHistory);
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
     * ??????????????????
     */
    @RequestMapping(value = "/company/invoiceDetail", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> invoiceDetail(HttpServletRequest request,
                                      HttpServletResponse response) {
        int respstat = RespCode.success;

        Map<String, Object> model = new HashMap<String, Object>();
        String originalId = (String) request.getSession().getAttribute("customkey");//????????????
        String id = (String) request.getParameter("id");
        logger.info("/company/invoiceDetail??????  ????????? originalId=" + originalId);
        if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(id)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                ChannelCustom custom = customService.getCustomByCustomkey(originalId);
                ChannelInvoice invoice = channelInvoiceService.getChannelInvoiceById(id);
                model.put("invoice", invoice);
                model.put("custom", custom);
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
     * ????????????
     */
    @RequestMapping(value = "/invoice/listData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyInvoice(HttpServletRequest request,
                                       HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String customkey = (String) request.getSession().getAttribute(
                "customkey");//????????????
//        String pageNo = (String) request.getParameter("pageNo");
        String name = (String) request.getParameter("name");
        String startTime = (String) request.getParameter("startTime");
        String endTime = (String) request.getParameter("endTime");
        String status = (String) request.getParameter("status");
        logger.info("/company/invoice??????  ????????? customkey=" + customkey);
        if (StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {

                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("customkey", customkey);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                paramMap.put("name", name);
                paramMap.put("status", status);
//                paramMap.put("pageNo", pageNo);
//                int total = channelInvoiceService.getChannelInvoiceByParam(paramMap).size();
//                int pageSize = 10;
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<ChannelInvoice> invoiceList = channelInvoiceService.getChannelInvoiceByParam(paramMap);
                PageInfo<ChannelInvoice> pageInfo = new PageInfo<>(invoiceList);
                model.put("total", pageInfo.getTotal());
                model.put("invoiceList", pageInfo.getList());
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
     * ????????????
     */
    @RequestMapping(value = "/invoice/invoiceMessage", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> invoiceMessage(HttpServletRequest request,
                                       HttpServletResponse response) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String customkey = (String) request.getSession().getAttribute(
                "customkey");//????????????
        if (StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                ChannelCustom custom = customService.getCustomByCustomkey(customkey);
                model.put("custom", custom);
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
     * ????????????
     */
    @RequestMapping(value = "/invoice/address", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> address(HttpServletRequest request,
                                HttpServletResponse response) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String address = (String) request.getParameter("address");
        String phoneNo = (String) request.getParameter("phoneNo");
        String receiverName = (String) request.getParameter("receiverName");
        String customkey = (String) request.getSession().getAttribute(
                "customkey");//????????????
        if (StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                if (!StringUtil.isMobileNO(phoneNo)) {
                    respstat = RespCode.error101;
                    model.put(RespCode.RESP_MSG, "????????????????????????");
                    return model;
                }
                ChannelCustom custom = customService.getCustomByCustomkey(customkey);
                custom.setAddress(address);
                custom.setReceiverName(receiverName);
                custom.setPhoneNo(phoneNo);
                customService.updateCustomById(custom);
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
     * ????????????
     */
    @RequestMapping(value = "/invoice/chooseInvoce", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> chooseInvoce(HttpServletRequest request,
                                     HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        String originalId = (String) request.getSession().getAttribute("customkey");//????????????
        Map<String, Object> model = new HashMap<String, Object>();
        String amount = (String) request.getParameter("amount");
        String startTime = (String) request.getParameter("startTime");
        String endTime = (String) request.getParameter("endTime");
        String companyId = (String) request.getParameter("companyId");
//        String pageNo = (String) request.getParameter("pageNo");
        logger.info("/invoice/chooseInvoce.do ???????????????amount:" + amount);
        if (StringUtil.isEmpty(originalId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                //?????????????????????????????????
                Map<String, Object> paramMap = new HashMap<>(15);
                paramMap.put("originalId", originalId);
                paramMap.put("companyId", companyId);
                paramMap.put("status", "1");
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
//                int total = commissionService.getUserCommissionToInvoice(paramMap).size();
//                int pageSize = 10;
//                if (!StringUtils.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<UserCommission> list = commissionService.getUserCommissionToInvoice(paramMap);
                PageInfo<UserCommission> pageInfo = new PageInfo<>(list);

                String amountSum = "0";
                int orderNum = 0;
                String orderAmount = "0";
                List<UserCommission> data = new ArrayList<UserCommission>();
                String ids = "";
                if (StringUtil.isEmpty(amount)) {
                    for (UserCommission commission : list) {
                        orderAmount = ArithmeticUtil.addStr(orderAmount, commission.getAmount());
                        orderNum++;
                        ids = ids + commission.getId() + ",";
                    }
                    data = list;
                } else {
                    for (UserCommission commission : list) {
                        amountSum = ArithmeticUtil.addStr(amountSum, commission.getAmount());
                        //???????????????????????????
                        if (ArithmeticUtil.compareTod(amount, amountSum) < 0) {
                            break;
                        } else {
                            ids = ids + commission.getId() + ",";
                            data.add(commission);
                            orderNum++;
                            orderAmount = ArithmeticUtil.addStr(orderAmount, commission.getAmount());
                        }
                    }
                }
                model.put("orderNum", orderNum);
                model.put("orderAmount", orderAmount);
                model.put("list", data);
                model.put("ids", ids);
                model.put("pageNo", pageNo);
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
     * ????????????
     */
    @RequestMapping(value = "/invoice/addData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyAddInvoice(HttpServletRequest request,
                                          HttpServletResponse response) {
        int respstat = RespCode.success;
        //????????????
        String originalId = (String) request.getSession().getAttribute("customkey");
        Map<String, Object> model = new HashMap<>(5);
        String amount = request.getParameter("amount");
        String companyId = request.getParameter("companyId");
        //????????????
        String invoiceCompanyName = request.getParameter("invoiceCompanyName");
        //????????????????????????
        String invoiceNo = request.getParameter("invoiceNo");
        //??????
        String remark = request.getParameter("remark");
        //?????????
        String mobileNo = request.getParameter("mobileNo");
        //??????
        String address = request.getParameter("address");
        String taxpayerType = request.getParameter("taxpayerType");
        String receiverName = request.getParameter("receiverName");
        String bankNameAndBankNo = customService.getCustomByCustomkey(originalId).getBankNameAndBankNo();
        String ids = request.getParameter("ids");
//		ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(originalId,companyId);
        logger.info("/company/addData??????  ????????? originalId=" + originalId
                + " amount=" + amount
                + " companyId=" + companyId
                + " invoiceCompanyName=" + invoiceCompanyName
                + " invoiceNo=" + invoiceNo
                + " remark=" + remark
                + " taxpayerType" + taxpayerType
                + " address=" + address
                + " ids=" + ids
                + " bankNameAndBankNo=" + bankNameAndBankNo
                + " mobileNo=" + mobileNo);
        if (StringUtil.isEmpty(originalId)
                || StringUtil.isEmpty(amount) || StringUtil.isEmpty(companyId)
                || StringUtil.isEmpty(invoiceCompanyName) || StringUtil.isEmpty(invoiceNo)
                || StringUtil.isEmpty(address) || StringUtil.isEmpty(mobileNo)
                || StringUtil.isEmpty(taxpayerType) || StringUtil.isEmpty(receiverName)
                || StringUtil.isEmpty(ids)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                String batchNo = orderNoUtil.getChannelSerialno();
                ChannelInvoice invoice = new ChannelInvoice();
                invoice.setAmount(amount);
                invoice.setCustomkey(originalId);
                invoice.setReCustomkey(companyId);
                invoice.setInvoiceNo(invoiceNo);
                invoice.setAddress(address);
                invoice.setRemark(remark);
                invoice.setNum(1);
                invoice.setMobileNo(mobileNo);
                invoice.setTaxpayerType(Integer.parseInt(taxpayerType));
                invoice.setReceiverName(receiverName);
                invoice.setOrderno(batchNo);
                invoice.setBankNameAndBankNo(bankNameAndBankNo);
                channelInvoiceService.addChannelInvoice(invoice);
                //????????????????????????????????????????????????
                commissionService.updateUserCommissionByInvoice(batchNo, ids);
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
     * ??????????????????
     */
    @RequestMapping(value = "/batch/listData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> ChannelHistory(HttpServletRequest request,
                                       HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(5);
        // ????????????
        String customkey = (String) request.getSession().getAttribute("customkey");
//        String pageNo = request.getParameter("pageNo");
        String name = request.getParameter("name");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String status = request.getParameter("status");
        String companyId = request.getParameter("companyId");
        logger.info("/company/invoice??????  ????????? customkey=" + customkey);
        if (StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "??????????????????");
            return result;
        } else {
            try {
                ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(customkey, companyId);
                if (related == null) {
                    respstat = RespCode.error101;
                    result.put(RespCode.RESP_STAT, respstat);
                    result.put(RespCode.RESP_MSG, "???????????????????????????");
                    return result;
                }
                Map<String, Object> paramMap = new HashMap<>(15);
                paramMap.put("reCustomkey", related.getCompanyId());
                paramMap.put("customkey", customkey);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                paramMap.put("name", name);
                paramMap.put("status", status);
//                paramMap.put("pageNo", pageNo);
//                int total = channelInvoiceService.getChannelInvoiceByParam(paramMap).size();
//                int pageSize = 10;
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<ChannelInvoice> invoiceList = channelInvoiceService.getChannelInvoiceByParam(paramMap);
                PageInfo<ChannelInvoice> pageInfo = new PageInfo<>(invoiceList);
                result.put("total", pageInfo.getTotal());
                result.put("invoiceList", pageInfo.getList());
            } catch (Exception e) {
                respstat = RespCode.error107;
                result.put(RespCode.RESP_STAT, respstat);
                result.put(RespCode.RESP_MSG, "??????????????????????????????????????????");
                logger.error(e.getMessage(), e);
                return result;
            }
        }
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "??????");
        logger.info("???????????????" + result);
        return result;
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = "/batch/summaryData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> summaryData(HttpServletRequest request,
                                    HttpServletResponse response) {
        int respstat = RespCode.success;
        String originalId = (String) request.getSession().getAttribute("customkey");//????????????
        Map<String, Object> model = new HashMap<String, Object>();
        String batchId = (String) request.getParameter("batchId");
        if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(originalId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                ChannelHistory history = channelHistoryService.getChannelHistoryById(batchId);
                if (history == null) {
                    history = channelHistoryService.getChannelHistoryByOrderno(batchId);
                }
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
     * ??????????????????
     */
    @RequestMapping(value = "/batch/detailData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> commissionList(HttpServletRequest request,
                                       HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String batchId = (String) request.getParameter("batchId");
        String certId = (String) request.getParameter("certId");
        String userName = (String) request.getParameter("userName");
        String status = (String) request.getParameter("status");
//        String pageNo = (String) request.getParameter("pageNo");
        if (StringUtil.isEmpty(batchId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("batchId", batchId);
                paramMap.put("status", status);
                paramMap.put("certId", certId);
                paramMap.put("userName", userName);
//                int pageSize = 10;
//                int total = commissionService.getUserCommissionByParam(paramMap).size();
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<UserCommission> list = commissionService.getUserCommissionByParam(paramMap);
                PageInfo<UserCommission> pageInfo = new PageInfo<>(list);
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
        return model;
    }

    /**
     * ??????????????????--??????
     *
     * @param model
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/batch/exportDetailData")
    public void exportDetailData(ModelMap model, String batchId, String certId, String status,
                                 String userName, String companyId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("batchId", batchId);
        paramMap.put("status", status);
        paramMap.put("certId", certId);
        paramMap.put("userName", userName);
        List<UserCommission> list = commissionService.getUserCommissionByParam(paramMap);

        String today = DateUtils.getNowDay();
        ArrayList<String> dataStr = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            UserCommission user = list.get(i);
            StringBuffer strBuff = new StringBuffer();
            String statusDesc = "";

            int status1 = user.getStatus();
            if (status1 == 0) {
                statusDesc = "????????????";
            } else if (status1 == 1) {
                statusDesc = "????????????";
            } else if (status1 == 2) {
                statusDesc = "????????????-" + user.getRemark();
            } else if (status1 == 3) {
                statusDesc = "?????????";
            }
            strBuff.append(user.getUserName() == null ? "" : user.getUserName())
                    .append(",")
                    .append(user.getAccount() == null ? "" : user.getAccount())
                    .append(",")
                    .append(user.getCertId() == null ? "" : user.getCertId())
                    .append(",")
                    .append(user.getAmount() == null ? "" : user.getAmount())
                    .append(",")
                    .append(user.getOrderNo() == null ? "" : user.getOrderNo())
                    .append(",")
                    .append(statusDesc);

            dataStr.add(strBuff.toString());
        }
        ArrayList<String> fieldName = new ArrayList<String>();
        fieldName.add("??????");
        fieldName.add("????????????/???????????????");
        fieldName.add("???????????????");
        fieldName.add("???????????????");
        fieldName.add("?????????");
        fieldName.add("????????????");
        fieldName.add("??????");
        String filename = today + "??????????????????????????????";
        ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
    }

    /**
     * ????????????????????????
     *
     * @param model
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/batch/exportUserDetailData")
    public void exportUserDetailData(ModelMap model, String batcheId, String userType,
                                     String userName, HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("userType", userType);
        paramMap.put("batcheId", batcheId);
        paramMap.put("userName", userName);
        List<User> userList = userSerivce.getUserRelatedByParam(paramMap);

        String today = DateUtils.getNowDay();
        ArrayList<String> dataStr = new ArrayList<String>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            StringBuffer strBuff = new StringBuffer();
            String statusDesc = "";

            int status1 = user.getUserType();
            if (status1 == 11) {
                statusDesc = "?????????";
            } else if (status1 == 1) {
                statusDesc = "????????????";
            } else if (status1 == 0) {
                statusDesc = "????????????-" + user.getRemark();
            } else if (status1 == 12) {
                statusDesc = "????????????-" + user.getRemark();
            }
            strBuff.append(user.getCreateTime() == null ? "" : user.getCreateTime())
                    .append(",")
                    .append(user.getUserName() == null ? "" : user.getUserName())
                    .append(",")
                    .append(user.getCertId() == null ? "" : user.getCertId())
                    .append(",")
                    .append(user.getBankNo() == null ? "" : user.getBankNo())
                    .append(",")
                    .append(user.getMobilePhone() == null ? "" : user.getMobilePhone())
                    .append(",")
                    .append(statusDesc);

            dataStr.add(strBuff.toString());
        }
        ArrayList<String> fieldName = new ArrayList<String>();
        fieldName.add("????????????");
        fieldName.add("??????");
        fieldName.add("???????????????");
        fieldName.add("????????????");
        fieldName.add("?????????");
        fieldName.add("??????");
        String filename = today + "???????????????????????????";
        ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
    }

    private void createErrorUser(String cardNo, String userName, String mobile, String userNo
            , String merchantId, String companyId, String remark, String batcheId, String bankcardNo, String bankNo) {
        User user = new User();
        user.setCertId(cardNo);
        user.setUserName(userName);
        user.setMobilePhone(mobile);
        user.setMerchantId(merchantId);
        user.setUserType(0);//????????????
        user.setUserNo(userNo);
        user.setCompanyUserNo(companyId);
        user.setRemark(remark);
        user.setBatcheId(batcheId);
        userSerivce.addUser(user);

        /**
         *  ???????????????????????????
         */
        TransferBank transferBank = new TransferBank();
        transferBank.setBankNo(bankNo);
        transferBank.setBankCardPhoneNo(mobile);
        transferBank.setUser_id(user.getId() + "");
        transferBank.setBankCardNo(bankcardNo);
        transferBank.setTransferType("2");
        transferBank.setStatus(1);
        transferBankService.addTransferBank(transferBank);
    }

    private int createSuccessUser(String originalId, String cardNo, String userName, String mobile, String userNo
            , String merchantId, String companyId, String remark, String batcheId, String bankcardNo, String bankNo) {
        /**
         *  ????????????????????????
         */
        User user = new User();
        user.setCertId(cardNo);
        user.setUserName(userName);
        user.setMobilePhone(mobile);
        user.setMerchantId(merchantId);
        user.setUserType(11);//????????????
        user.setUserNo(userNo);
        user.setCompanyUserNo(companyId);
        user.setBatcheId(batcheId);
        user.setAccount(bankcardNo);
        userSerivce.addUser(user);
        int userid = user.getId();
        /**
         *  ???????????????????????????
         */
        TransferBank transferBank = new TransferBank();
        transferBank.setBankNo(bankNo);
        transferBank.setStatus(1);
        transferBank.setBankCardPhoneNo(mobile);
        transferBank.setUser_id(user.getId() + "");
        transferBank.setBankCardNo(bankcardNo);
        transferBank.setTransferType("2");
        transferBankService.addTransferBank(transferBank);

        /**
         * ???????????????????????????
         */
        UserRelated userRelated = new UserRelated();
        userRelated.setStatus(0);//????????????????????????
        userRelated.setCreateTime(DateUtils.getNowDate());
        userRelated.setOriginalId(originalId);
        userRelated.setUserNo(userNo);
        userRelated.setUserId(userid);
        userRelated.setCompanyId(companyId);
        userRelatedService.createUserRelated(userRelated);
        return userid;
    }


    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */

    /**
     * ????????????
     */
    @RequestMapping(value = "/user/commissionData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> commissionDetail(HttpServletRequest request,
                                         HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String originalId = (String) request.getSession().getAttribute("customkey");//????????????
        String name = (String) request.getParameter("name");
        String startTime = (String) request.getParameter("startTime");
        String endTime = (String) request.getParameter("endTime");
//        String pageNo = (String) request.getParameter("pageNo");
        logger.info("/user/commissionData ??????  ????????? customkey=" + originalId);
        if (StringUtil.isEmpty(originalId)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("originalId", originalId);
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
    Map<String, Object> commissionSumData(HttpServletRequest request,
                                          HttpServletResponse response) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String customkey = (String) request.getSession().getAttribute("customkey");//????????????????????????
        String name = (String) request.getParameter("name");
        String startTime = (String) request.getParameter("startTime");
        String endTime = (String) request.getParameter("endTime");
        logger.info("/user/commissionSumData ??????  ????????? customkey=" + customkey);
        if (StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "??????????????????");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("name", name);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                paramMap.put("status", 1);
                paramMap.put("originalId", customkey);
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
        String name = (String) request.getParameter("name");
        String startTime = (String) request.getParameter("startTime");
        String endTime = (String) request.getParameter("endTime");
        String status = (String) request.getParameter("status");
        logger.info("/user/commissionData ??????  ????????? customkey=" + customkey);
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("originalId", customkey);
        paramMap.put("name", name);
        paramMap.put("status", status);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        List<UserCommission> list = commissionService.getUserCommissionByParam(paramMap);
        String today = DateUtils.getNowDay();
        ArrayList<String> dataStr = new ArrayList<String>();
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
        ArrayList<String> fieldName = new ArrayList<String>();
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
     * ??????????????????
     *
     * @author linsong
     * @date 2019/4/8
     */
    @PostMapping(value = "/custom/companyAccount")
    @ResponseBody
    public Map<String, Object> companyAccount(HttpServletRequest request, String customName, String companyId, @RequestParam(required = false, defaultValue = "1") Integer pageNo, @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

        Map<String, Object> model = new HashMap<>(20);
        model.put("customName", customName);
        model.put("companyId", companyId);

        //????????????????????????????????????
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
            model.put("companyId", loginUser.getCustomkey());
        }

        PageHelper.startPage(pageNo, pageSize);
        List<CompanyAccount> companyAccountList = customBalanceService.queryCompanyAccount(model);

        PageInfo page = new PageInfo(companyAccountList);

        model.put("total", page.getTotal());
        model.put("list", page.getList());

        return returnSuccess(model);
    }


    /**
     * ????????????????????????
     *
     * @author linsong
     * @date 2019/4/8
     */
    @GetMapping(value = "/custom/companyAccount/export")
    public void balanceExport(HttpServletRequest request, HttpServletResponse response, String customName, String companyId) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

        Map<String, Object> model = new HashMap<>(20);
        model.put("customName", customName);
        model.put("companyId", companyId);

        //????????????????????????????????????
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
            model.put("companyId", loginUser.getCustomkey());
        }

        List<CompanyAccount> companyAccountList = customBalanceService.queryCompanyAccount(model);

        String[] colunmName = new String[]{"????????????", "????????????????????????", "?????????????????????", "????????????????????????", "???????????????", "???????????????", "????????????"};
        String filename = "?????????????????????";
        List<Map<String, Object>> data = new ArrayList<>();
        for (CompanyAccount companyAccount : companyAccountList) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", companyAccount.getCustomName());
            dataMap.put("2", companyAccount.getCompanyName());
            dataMap.put("3", companyAccount.getWaitConfirmedBalance());
            dataMap.put("4", companyAccount.getBalanceSum());
            dataMap.put("5", companyAccount.getBankCardBalance());
            dataMap.put("6", companyAccount.getAlipayBlance());
            dataMap.put("7", companyAccount.getWechatBalance());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }


    /**
     * ????????????????????????
     *
     * @author linsong
     * @date 2019/4/8
     */
    @PostMapping(value = "/custom/companyAccountHistory")
    @ResponseBody
    public Map<String, Object> companyAccountHistory(HttpServletRequest request, String customKey, String companyId, String startTime, String endTime, Integer transferType,
                                                     @RequestParam(required = false, defaultValue = "1") Integer pageNo, @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

        //????????????????????????????????????
        if (!isRootAdmin(loginUser) && (CustomType.COMPANY.getCode() != loginUser.getCustomType())) {
            if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
                ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
                if (CustomType.COMPANY.getCode() != masterCustom.getCustomType()) {
                    return returnFail(RespCode.error101, "????????????");
                }
            } else {
                return returnFail(RespCode.error101, "????????????");
            }
        }

        Map<String, Object> model = new HashMap<>(20);

        if (StringUtil.isEmpty(customKey) || StringUtil.isEmpty(companyId)) {
            return returnFail(RespCode.error101, "???????????????????????????");
        } else {
            model.put("customkey", customKey);
            model.put("recCustomkey", companyId);
            model.put("startTime", startTime);
            model.put("endTime", endTime);
            model.put("transfertype", transferType);


            PageHelper.startPage(pageNo, pageSize);
            List<ChannelHistory> list = channelHistoryService.getHistoryList(model);

            PageInfo page = new PageInfo(list);

            model.put("total", page.getTotal());
            model.put("list", page.getList());
        }


        return returnSuccess(model);
    }


    /**
     * ??????????????????????????????
     *
     * @author linsong
     * @date 2019/4/8
     */
    @GetMapping(value = "/custom/companyAccountHistory/export")
    public void companyAccountHistoryExport(HttpServletResponse response, String customKey, String companyId, String startTime, String endTime, Integer transferType) {

        Map<String, Object> model = new HashMap<>(20);

        model.put("customkey", customKey);
        model.put("recCustomkey", companyId);
        model.put("startTime", startTime);
        model.put("endTime", endTime);
        model.put("transfertype", transferType);


        List<ChannelHistory> list = channelHistoryService.getHistoryList(model);


        String[] colunmName = new String[]{"????????????", "????????????", "??????", "??????", "????????????", "?????????", "??????", "????????????", "????????????????????????"};
        String filename = "???????????????????????????";
        List<Map<String, Object>> data = new ArrayList<>();
        for (ChannelHistory channelHistory : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", channelHistory.getCompanyName());
            dataMap.put("2", channelHistory.getCreatetime());
            dataMap.put("3", TradeType.codeOf(channelHistory.getTransfertype()).getDesc());
            dataMap.put("4", PayType.codeOf(channelHistory.getPayType()).getDesc());
            dataMap.put("5", channelHistory.getAmount());
            dataMap.put("6", channelHistory.getServiceFee());
            dataMap.put("7", channelHistory.getStatusStr());
            dataMap.put("8", channelHistory.getOperatorName());
            dataMap.put("9", channelHistory.getUpdatetime());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }


    @PostMapping(value = "/custom/changeBalance")
    @ResponseBody
    public Map<String, Object> changeBalance(HttpServletRequest request, String customKey, String companyId, int payType, String amount, int changeType, String tranPassword) {

        if (StringUtil.isEmpty(customKey) || StringUtil.isEmpty(companyId) || payType == 0 || StringUtil.isEmpty(amount)) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        if (changeType != TradeType.ADDBALANCE.getCode() && changeType != TradeType.SUBBALANCE.getCode()) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

        if (StringUtil.isEmpty(tranPassword) || StringUtil.isEmpty(loginUser.getTranPassword())) {
            return returnFail(RespCode.error101, RespCode.PASSWORD_DOES_NOT_SET);
        }

        //??????????????????
        if (!loginUser.getTranPassword().equals(CipherUtil.generatePassword(tranPassword, StringUtil.isEmpty(loginUser.getCustomkey()) ? loginUser.getMasterCustom() : loginUser.getCustomkey()))) {
            return returnFail(RespCode.error101, RespCode.PASSWORD_ERROR);
        }
        String operatorName = loginUser.getUsername();

        //????????????????????????????????????
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

        try {

            int operating = 0;

            if (changeType == TradeType.ADDBALANCE.getCode()) {
                operating = CommonString.ADDITION;
            } else if (changeType == TradeType.SUBBALANCE.getCode()) {
                operating = CommonString.DEDUCTION;
            }

            customBalanceService.updateCustomBalanceAndSubBalance(customKey, companyId, payType, amount, operating, TradeType.codeOf(changeType), operatorName, null);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error101, RespCode.UPDATE_FAILED);
        }

        return returnSuccess();
    }


    @PostMapping(value = "/custom/deductBalance")
    @ResponseBody
    public Map<String, Object> deductBalance(HttpServletRequest request, String customKey,
        String companyId, Integer payType, String amount, String tranPassword) {

        if (StringUtil.isEmpty(customKey) || StringUtil.isEmpty(companyId) || payType == null
            || StringUtil.isEmpty(amount)) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        if (ArithmeticUtil.compareTod(amount, "0") != 1){
            return returnFail(RespCode.error101, RespCode.DEDUCT_AMOUNT_ERROR);
        }

        ChannelCustom loginUser = (ChannelCustom) request.getSession()
            .getAttribute(CommonString.CUSTOMLOGIN);

        try {

            String loginCustomKey =
                !StringUtil.isEmpty(loginUser.getMasterCustom()) ? loginUser.getMasterCustom()
                    : loginUser.getCustomkey();

            if (!loginCustomKey.equals(customKey)) {
                return returnFail(RespCode.error101, RespCode.DEDUCT_BALANCE_CUSTOM_ERROR);
            }

            if (StringUtil.isEmpty(tranPassword) || StringUtil.isEmpty(loginUser.getTranPassword())) {
                return returnFail(RespCode.error101, RespCode.PASSWORD_DOES_NOT_SET);
            }

            //??????????????????
            if (!loginUser.getTranPassword().equals(CipherUtil.generatePassword(tranPassword,
                StringUtil.isEmpty(loginUser.getCustomkey()) ? loginUser.getMasterCustom()
                    : loginUser.getCustomkey()))) {
                return returnFail(RespCode.error101, RespCode.PASSWORD_ERROR);
            }
            String operatorName = loginUser.getUsername();

            customBalanceService.updateCustomBalanceAndSubBalance(customKey, companyId, payType, amount, CommonString.DEDUCTION,
                    TradeType.codeOf(TradeType.YXYSUBBALANCE.getCode()), operatorName, null);

            return returnSuccess();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error101, RespCode.DEDUCT_BALANCE_ERROR);
        }
    }

    @RequestMapping(value = "/custom/queryBalance", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> queryBalance(HttpServletRequest request, String customKey, String companyId,
        Integer payType) {

        if (StringUtil.isEmpty(customKey) || StringUtil.isEmpty(companyId) || payType == null) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        ChannelCustom loginUser = (ChannelCustom) request.getSession()
            .getAttribute(CommonString.CUSTOMLOGIN);

        String loginCustomKey =
            !StringUtil.isEmpty(loginUser.getMasterCustom()) ? loginUser.getMasterCustom()
                : loginUser.getCustomkey();

        if (!loginCustomKey.equals(customKey)) {
            return returnFail(RespCode.error101, RespCode.QUERY_BALANCE_CUSTOM_ERROR);
        }

        try {

            ChannelRelated channelRelated = channelRelatedService
                .getRelatedByCompAndOrigAll(customKey, companyId);
            if (channelRelated == null) {
                return returnFail(RespCode.error107, RespCode.CUSTOM_COMPANY_NOT_RELATED);
            }

            HashMap<String, Object> responseParam = new HashMap<>(10);

            String balance = channelHistoryService
                .getBalance(customKey, companyId, String.valueOf(payType));

            responseParam.put("customKey", customKey);
            responseParam.put("companyId", companyId);
            responseParam.put("balance", balance);
            responseParam.put("payType", payType);

            return returnSuccess(responseParam);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error101, RespCode.QUERY_BALANCE_ERROR);
        }

    }

}
