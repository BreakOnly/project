package com.jrmf.controller.subaccount;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.bankapi.ActionReturn;
import com.jrmf.bankapi.pingansub.PinganBankTransactionConstants;
import com.jrmf.bankapi.pingansub.SubAccountTransHistoryPage;
import com.jrmf.bankapi.pingansub.SubAccountTransHistoryRecord;
import com.jrmf.bankapi.pingansub.params.SubAccountQueryHistoryTransferResultParam;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.*;
import com.jrmf.domain.*;
import com.jrmf.payment.AccountSystemFactory;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.AccountSystem;
import com.jrmf.payment.entity.PingAnBankAccountSystem;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.service.*;
import com.jrmf.utils.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("/pingansub")
public class PingAnTransferResultController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(PingAnTransferResultController.class);

    @Autowired
    private CustomTransferRecordService customTransferRecordService;
    @Autowired
    private CustomReceiveConfigService customReceiveConfigService;
    @Autowired
    private ChannelCustomService customService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private CustomBalanceService balanceService;
    @Autowired
    private OrderNoUtil orderNoUtil;
    @Autowired
    private UserCommissionService userCommissionService;
    @Autowired
    private ChannelHistoryService channelHistoryService;
    @Autowired
    private LinkAccountTransService linkAccountTransService;

    /**
     * ??????????????????
     *
     * @author linsong
     * @date 2019/9/2
     */
    @RequestMapping(value = "/receiveTransferRecord")
    @ResponseBody
    public String receiveTransferRecord(HttpServletRequest res) {

        try {

            String resultStr = StringUtil.convertStreamToString(res.getInputStream());


            //??????222?????????
            if (!(resultStr.length() < 222)) {
                String retCode = resultStr.substring(87, 93);

                //????????????????????????000000?????????????????????xml?????????
                if (PinganBankTransactionConstants.TRAN_RET_CODE.equals(retCode)) {
                    resultStr = resultStr.substring(222);

                    Document doc = DocumentHelper.parseText(resultStr);
                    Element rootElt = doc.getRootElement();


                    String subAccount = rootElt.element("STT_VCH_CARD_NO").getStringValue();
                    String oppAccountNo = rootElt.element("STT_VCH_RCV_AC_NO").getStringValue();
                    String oppAccountName = rootElt.element("STT_VCH_RCV_AC_NAME").getStringValue();
                    String flag = rootElt.element("STT_VCH_SIGN").getStringValue();


                    //000000????????????????????????????????????????????????????????????
                    if (oppAccountNo.endsWith("000000")) {
                        return "SUCCESS";
                    }

                    if (subAccount.endsWith("000000")) {
                        if (!CustomTransferRecordType.SUBACCOUNTINTO.getFlag().equals(flag)) {
                            return "SUCCESS";
                        }
                        //??????????????????????????????????????????,???????????????,?????????????????????????????????,?????????????????????????????????
                        if (customReceiveConfigService.checkSubAccountIsExists(oppAccountNo, null) > 0) {
                            return "SUCCESS";
                        }

                        //??????????????????????????????????????????????????????
//                        if (customReceiveConfigService.checkSubAccountIsExists(null, oppAccountName) < 1) {
//                            return "SUCCESS";
//                        }
                    }

                    String tranTime = rootElt.element("STT_TR_TIME").getStringValue();
                    String mainAccount = rootElt.element("STT_VCH_AC_NO").getStringValue();
                    String tranAmount = rootElt.element("STT_VCH_AMT").getStringValue();
                    String oppBankName = rootElt.element("STT_VCH_RCV_BK_NAME").getStringValue();
                    String oppBankNo = rootElt.element("STT_VCH_RCV_BK_NO").getStringValue();
                    String remark = rootElt.element("STT_VCH_REMARK").getStringValue();
                    String bizFlowNo = rootElt.element("STT_CONSUMER_SEQ_NO").getStringValue();
                    String mainAccountName = rootElt.element("STT_VCH_CI_NAME").getStringValue();
                    String subAccoutName = rootElt.element("STT_SUBACC_ALIAS").getStringValue();


                    CustomTransferRecord record = new CustomTransferRecord();
                    record.setSubAccount(subAccount);
                    record.setSubAccoutName(subAccoutName);
                    record.setTranTime(tranTime);
                    record.setMainAccount(mainAccount);
                    record.setMainAccountName(mainAccountName);
                    record.setFlag(flag);
                    record.setTranAmount(tranAmount);
                    record.setOppAccountNo(oppAccountNo);
                    record.setOppAccountName(oppAccountName);
                    record.setOppBankName(oppBankName);
                    record.setOppBankNo(oppBankNo);
                    record.setRemark(remark);
                    record.setBizFlowNo(bizFlowNo);
                    record.setIsConfirm(ConfirmStatus.FAILURE.getCode());
                    record.setTranType(CustomTransferRecordType.codeOfFlag(record.getFlag()).getCode());
                    customTransferRecordService.insert(record);

                }
            } else {
                logger.error("-----------????????????????????????,??????:{}---------", resultStr);
            }


        } catch (DuplicateKeyException e) {
            logger.info("-----------???????????????????????????---------");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return "SUCCESS";
    }



    /**
     * ?????????????????????
     *
     * @author linsong
     * @date 2020/08/28
     */
//    @RequestMapping(value = "/mainAccountReceiveTransferRecord")
//    @ResponseBody
//    public String mainAccountReceiveTransferRecord(HttpServletRequest res) {
//
//        try {
//
//            String resultStr = StringUtil.convertStreamToString(res.getInputStream());
//
//
//            //??????222?????????
//            if (!(resultStr.length() < 222)) {
//                String retCode = resultStr.substring(87, 93);
//
//                //????????????????????????000000?????????????????????xml?????????
//                if (PinganBankTransactionConstants.TRAN_RET_CODE.equals(retCode)) {
//                    resultStr = resultStr.substring(222);
//
//                    Document doc = DocumentHelper.parseText(resultStr);
//                    Element rootElt = doc.getRootElement();
//
//
//                    String subAccount = rootElt.element("InAcctNo").getStringValue();
//                    String oppAccountNo = rootElt.element("OutAcctNo").getStringValue();
//                    String oppAccountName = rootElt.element("OutAcctName").getStringValue();
//                    String flag = rootElt.element("DcFlag").getStringValue();
//
//                    //??????????????????????????????
//                    if (!CustomTransferRecordType.SUBACCOUNTINTO.getFlag().equals(flag)) {
//                        return "SUCCESS";
//                    }
//
//                    //000000????????????????????????????????????????????????????????????
////                    if (oppAccountNo.endsWith("000000")) {
////                        return "SUCCESS";
////                    }
//
////                    if (subAccount.endsWith("000000")) {
////                        if (!CustomTransferRecordType.SUBACCOUNTINTO.getFlag().equals(flag)) {
////                            return "SUCCESS";
////                        }
////                        //??????????????????????????????????????????,???????????????,?????????????????????????????????,?????????????????????????????????
////                        if (customReceiveConfigService.checkSubAccountIsExists(oppAccountNo, null) > 0) {
////                            return "SUCCESS";
////                        }
////
////                        //??????????????????????????????????????????????????????
////                        if (customReceiveConfigService.checkSubAccountIsExists(null, oppAccountName) < 1) {
////                            return "SUCCESS";
////                        }
////                    }
//
//                    String tranTime = rootElt.element("TxTime").getStringValue();
//                    String mainAccount = rootElt.element("InAcctNo").getStringValue();
//                    String tranAmount = rootElt.element("TranAmount").getStringValue();
//                    String oppBankName = rootElt.element("STT_VCH_RCV_BK_NAME").getStringValue();
//                    String oppBankNo = rootElt.element("OutBankNo").getStringValue();
//                    String remark = rootElt.element("OutBankName").getStringValue();
//                    String bizFlowNo = rootElt.element("BussSeqNo").getStringValue();
//                    String mainAccountName = rootElt.element("InAcctName").getStringValue();
//                    String subAccoutName = rootElt.element("OutAcctName").getStringValue();
//
//
//                    CustomTransferRecord record = new CustomTransferRecord();
//                    record.setSubAccount(subAccount);
//                    record.setSubAccoutName(subAccoutName);
//                    record.setTranTime(tranTime);
//                    record.setMainAccount(mainAccount);
//                    record.setMainAccountName(mainAccountName);
//                    record.setFlag(flag);
//                    record.setTranAmount(tranAmount);
//                    record.setOppAccountNo(oppAccountNo);
//                    record.setOppAccountName(oppAccountName);
//                    record.setOppBankName(oppBankName);
//                    record.setOppBankNo(oppBankNo);
//                    record.setRemark(remark);
//                    record.setBizFlowNo(bizFlowNo);
//                    record.setIsConfirm(ConfirmStatus.FAILURE.getCode());
//                    record.setTranType(CustomTransferRecordType.codeOfFlag(record.getFlag()).getCode());
//                    customTransferRecordService.insert(record);
//
//                }
//            } else {
//                logger.error("-----------????????????????????????,??????:{}---------", resultStr);
//            }
//
//
//        } catch (DuplicateKeyException e) {
//            logger.info("-----------???????????????????????????---------");
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//
//        return "SUCCESS";
//    }


    /**
     * ?????????????????????(?????????)
     *
     * @author linsong
     * @date 2019/9/18
     */
    @RequestMapping(value = "/accountList")
    @ResponseBody
    public Map<String, Object> accountList(HttpServletRequest request,
                                           String customName,
                                           String companyId,
                                           String startTime,
                                           String endTime,
                                           String receiveAccount,
                                           @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                           @RequestParam(required = false, defaultValue = "10") Integer pageSize) {


        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        Map<String, Object> model = new HashMap<>(20);
        model.put("customName", customName);
        model.put("companyId", companyId);
        model.put("startTime", startTime);
        model.put("endTime", endTime);
        model.put("receiveAccount", receiveAccount);

        try {
            //????????????????????????????????????
            if (!isRootAdmin(loginUser) && (CustomType.COMPANY.getCode() != loginUser.getCustomType())) {
                if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
                    ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
                    if (CustomType.COMPANY.getCode() == masterCustom.getCustomType()) {
                        loginUser = masterCustom;
                    } else {
                        return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
                    }
                } else {
                    return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
                }
            }


            //???????????????????????????
            if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
                model.put("companyId", loginUser.getCustomkey());
                companyId = loginUser.getCustomkey();

            }

            if (!StringUtil.isEmpty(companyId)) {
                int count = companyService.getSubAccountList(companyId);
                if (count < 1) {
                    return returnFail(RespCode.error101, RespCode.SUBACCOUNT_NOT_OPEN);
                }

            }

            PageHelper.startPage(pageNo, pageSize);
            List<CustomReceiveConfig> accountList = customReceiveConfigService.querySubAccountList(model);
            PageInfo<CustomReceiveConfig> page = new PageInfo<>(accountList);

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
     * ???????????????????????????(??????????????????)
     *
     * @author linsong
     * @date 2019/9/18
     */
    @RequestMapping(value = "/queryTransferList")
    @ResponseBody
    public Map<String, Object> queryTransferList(Integer accountId,
                                                 String startTime,
                                                 String endTime,
                                                 String reqLastSubAccNo,
                                                 String reqLastDate,
                                                 String reqLastJNo,
                                                 String reqLastSeq) {

        if (accountId == null || StringUtil.isEmpty(startTime) || StringUtil.isEmpty(endTime)) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        try {

            CustomReceiveConfig receiveConfig = customReceiveConfigService.getCustomReceiveConfigById(accountId);

            List<SubAccountTransHistoryRecord> resultList = new ArrayList<>();
            String allCount = null;
            boolean hasNextPage = false;

            if (receiveConfig != null && receiveConfig.getIsSubAccount() == 1) {
                PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(receiveConfig.getPayType().toString(), receiveConfig.getCustomkey(), receiveConfig.getCompanyId());

                if (PaymentFactory.PAKHKF.equals(paymentConfig.getPathNo()) || PaymentFactory.PAYQZL.equals(paymentConfig.getPathNo())) {
                    PingAnBankAccountSystem pingAnBankAccountSystem = new PingAnBankAccountSystem(paymentConfig);

                    SubAccountQueryHistoryTransferResultParam param = new SubAccountQueryHistoryTransferResultParam();
                    param.setReqSubAccount(receiveConfig.getReceiveAccount());
                    param.setStartDate(startTime);
                    param.setEndDate(endTime);
                    param.setOpFlag("2");
                    param.setReqLastSubAccNo(reqLastSubAccNo);
                    param.setReqLastDate(reqLastDate);
                    param.setReqLastJNo(reqLastJNo);
                    param.setReqLastSeq(reqLastSeq);

                    ActionReturn<SubAccountTransHistoryPage> queryReturn = pingAnBankAccountSystem.queryTransferHistory(param);
                    if (PayRespCode.RESP_SUCCESS.equals(queryReturn.getRetCode())) {

                        SubAccountTransHistoryPage result = queryReturn.getAttachment();
                        resultList = result.getTransHistoryRecords();
                        allCount = result.getAllCount();
                        hasNextPage = result.isHasNextPage();
                    }
                }
            }

            Map<String, Object> result = new HashMap<>(5);

            result.put("total", allCount);
            result.put("list", resultList);
            result.put("hasNextPage", hasNextPage);

            return returnSuccess(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }

    }


    /**
     * ?????????????????????(?????????)??????
     *
     * @author linsong
     * @date 2019/9/18
     */
    @RequestMapping(value = "/accountList/export")
    @ResponseBody
    public void accountListExport(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String customName,
                                  String companyId,
                                  String startTime,
                                  String endTime,
                                  String receiveAccount) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        Map<String, Object> model = new HashMap<>(20);
        model.put("customName", customName);
        model.put("companyId", companyId);
        model.put("startTime", startTime);
        model.put("endTime", endTime);
        model.put("receiveAccount", receiveAccount);

        try {

            //????????????????????????????????????
            if (!isRootAdmin(loginUser) && (CustomType.COMPANY.getCode() != loginUser.getCustomType())) {
                if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
                    ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
                    if (CustomType.COMPANY.getCode() == masterCustom.getCustomType()) {
                        loginUser = masterCustom;
                    } else {
                        throw new Exception(RespCode.PERMISSION_ERROR);
                    }
                } else {
                    throw new Exception(RespCode.PERMISSION_ERROR);
                }
            }

            //???????????????????????????
            if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
                model.put("companyId", loginUser.getCustomkey());
            }

            List<CustomReceiveConfig> accountList = customReceiveConfigService.querySubAccountList(model);


            String[] colunmName = new String[]{"????????????","??????????????????????????????", "?????????", "??????????????????", "??????????????????", "??????????????????", "????????????", "????????????", "??????key", "????????????", "??????????????????", "????????????"};
            String filename = "????????????????????????";
            List<Map<String, Object>> data = new ArrayList<>();
            for (CustomReceiveConfig receiveConfig : accountList) {
                Map<String, Object> dataMap = new HashMap<>(20);
                dataMap.put("1", receiveConfig.getCompanyName());
                dataMap.put("2", receiveConfig.getContractCompanyName());
                dataMap.put("3", receiveConfig.getReceiveAccount());
                dataMap.put("4", receiveConfig.getReceiveUser());
                dataMap.put("5", receiveConfig.getReceiveBank());
                dataMap.put("6", RechargeConfirmType.codeOf(receiveConfig.getRechargeConfirmType()).getDesc());
                dataMap.put("7", receiveConfig.getReceiveUser());
                dataMap.put("8", PayType.codeOf(receiveConfig.getPayType()).getDesc());
                dataMap.put("9", receiveConfig.getCustomkey());
                dataMap.put("10", receiveConfig.getCreateTime());
                dataMap.put("11", receiveConfig.getUpdateTime());
                dataMap.put("12", receiveConfig.getAddUser());
                data.add(sortMapByKey(dataMap));
            }

            ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * ????????????????????????????????????
     *
     * @author linsong
     * @date 2019/9/24
     */
    @RequestMapping(value = "/subAccountBalance")
    @ResponseBody
    public Map<String, Object> subAccountBalance(Integer accountId) {

        if (accountId == null) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        try {
            CustomReceiveConfig receiveConfig = customReceiveConfigService.getCustomReceiveConfigById(accountId);

            if (receiveConfig != null && receiveConfig.getIsSubAccount() == 1) {
                PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(receiveConfig.getPayType().toString(), receiveConfig.getCustomkey(), receiveConfig.getCompanyId());

                if (paymentConfig.getIsSubAccount() == 1) {
                    AccountSystem accountSystem = AccountSystemFactory.accountSystemEntity(paymentConfig);


                    if (null != accountSystem) {

                        ActionReturn<String> res = accountSystem
                            .querySubAccountBalance(receiveConfig.getCustomkey(),
                                receiveConfig.getReceiveAccount());

                        logger.info("----------???????????????????????????----------");
                        if (res.isOk()) {
                            String subAccountBalance = res.getAttachment();
                            String customBalance = balanceService.queryCustomBalance(receiveConfig.getCustomkey(), receiveConfig.getCompanyId(), receiveConfig.getPayType());

                            receiveConfig.setCustomBalance(customBalance);
                            receiveConfig.setSubAccountBalance(subAccountBalance);
                            receiveConfig.setMainAccount(paymentConfig.getCorporationAccount());

                            return returnSuccess(receiveConfig);

                        } else {
                            logger.error("----------???????????????????????????:----------" + res.getFailMessage());
                            return returnFail(RespCode.error107, res.getFailMessage());
                        }

                    } else {
                        return returnFail(RespCode.error101, RespCode.SUBACCOUNT_UNSUPPORT_SYNC);
                    }

                } else {
                    return returnFail(RespCode.error101, RespCode.SUBACCOUNT_NOT_OPEN);
                }
            } else {
                return returnFail(RespCode.error101, RespCode.SUBACCOUNT_NOT_EXIST);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }
    }


    /**
     * ????????????????????????
     *
     * @author linsong
     * @date 2019/9/23
     */
    @RequestMapping(value = "/syncBalance")
    @ResponseBody
    public Map<String, Object> syncBalance(HttpServletRequest request, Integer accountId, String tranPassword, String customBalance, String subAccountBalance) {


        if (accountId == null || StringUtil.isEmpty(customBalance) || StringUtil.isEmpty(subAccountBalance)) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        if (ArithmeticUtil.compareTod(customBalance, subAccountBalance) == 0) {
            return returnFail(RespCode.error101, RespCode.SUBACCOUNT_BALANCE_EQUAL);
        }

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        if (StringUtil.isEmpty(tranPassword) || StringUtil.isEmpty(loginUser.getTranPassword())) {
            return returnFail(RespCode.error101, RespCode.PASSWORD_DOES_NOT_SET);
        }

        //??????????????????
        if (!loginUser.getTranPassword().equals(CipherUtil.generatePassword(tranPassword, StringUtil.isEmpty(loginUser.getCustomkey()) ? loginUser.getMasterCustom() : loginUser.getCustomkey()))) {
            return returnFail(RespCode.error101, RespCode.PASSWORD_ERROR);
        }


        try {

            String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
            MDC.put(PROCESS, processId);

            CustomReceiveConfig receiveConfig = customReceiveConfigService.getCustomReceiveConfigById(accountId);
            int count = userCommissionService.getPayingCount(receiveConfig.getCustomkey(), receiveConfig.getCompanyId(), receiveConfig.getPayType());
            if (count > 0) {
                return returnFail(RespCode.error101, RespCode.EXIST_PAY_RECORD);
            }

            int rechargeCount = channelHistoryService.getToBeConfirmedCount(receiveConfig.getCustomkey(), receiveConfig.getCompanyId(), receiveConfig.getPayType());
            if (rechargeCount > 0) {
                return returnFail(RespCode.error101, RespCode.EXIST_RECHARGE_RECORD);
            }

            if (receiveConfig.getIsSubAccount() == 1) {
                PaymentConfig paymentConfig = companyService.getPaymentConfigInfo(receiveConfig.getPayType().toString(), receiveConfig.getCustomkey(), receiveConfig.getCompanyId());

                if (paymentConfig.getIsSubAccount() == 1) {

                    Map<String, Object> param = new HashMap<>();
                    param.put("subAccount", receiveConfig.getReceiveAccount());
                    param.put("status", ConfirmStatus.Paying.getCode());
                    param.put("oppAccountNo", paymentConfig.getShadowAcctNo());
                    param.put("tranType", CustomTransferRecordType.ADJUSTMENTOUT.getCode() + "," + CustomTransferRecordType.ADJUSTMENTINTO.getCode());
                    List<CustomTransferRecord> transferRecordList = customTransferRecordService.getCustomTransferRecordByParam(param);

                    if (transferRecordList != null && transferRecordList.size() > 0) {
                        return returnFail(RespCode.error101, RespCode.EXIST_SYNC_RECORD);
                    }

                    String balance = balanceService.queryCustomBalance(receiveConfig.getCustomkey(), receiveConfig.getCompanyId(), receiveConfig.getPayType());
                    if (ArithmeticUtil.compareTod(customBalance, balance) != 0) {
                        return returnFail(RespCode.error101, RespCode.CUSTOM_BALANCE_CHANGE);
                    }

                    AccountSystem accountSystem = AccountSystemFactory.accountSystemEntity(paymentConfig);

                    if (accountSystem != null) {
                        logger.info("----------?????????????????????????????????---------????????????:{},??????????????????{}", receiveConfig.getReceiveAccount(), balance);
//                        PingAnBankAccountSystem pingAnBankAccountSystem = new PingAnBankAccountSystem(paymentConfig);

                        ActionReturn<String> res = accountSystem.querySubAccountBalance(receiveConfig.getCustomkey() ,receiveConfig.getReceiveAccount());

                        logger.info("----------???????????????????????????----------");
                        if (res.isOk()) {

                            String pinganBalance = res.getAttachment();

                            logger.info("----------??????????????????????????? ??????:{},????????????{}----------", receiveConfig.getReceiveAccount(), pinganBalance);
                            if (ArithmeticUtil.compareTod(pinganBalance, subAccountBalance) != 0) {
                                return returnFail(RespCode.error101, RespCode.SUBACCOUNT_BALANCE_CHANGE);
                            }

                            if (ArithmeticUtil.compareTod(balance, pinganBalance) == 0) {
                                return returnFail(RespCode.error101, RespCode.SUBACCOUNT_BALANCE_EQUAL);
                            }

                            CustomTransferRecord transferRecord = new CustomTransferRecord();
                            transferRecord.setCustomKey(receiveConfig.getCustomkey());
                            transferRecord.setCompanyId(receiveConfig.getCompanyId());
                            transferRecord.setMainAccount(paymentConfig.getCorporationAccount());
                            transferRecord.setMainAccountName(paymentConfig.getCorporationAccountName());
                            transferRecord.setSubAccount(receiveConfig.getReceiveAccount());
                            transferRecord.setSubAccoutName(receiveConfig.getContractCompanyName());

                            String tranAmount = null;
                            CustomTransferRecordType type = null;

                            //????????????????????????????????????
                            if (ArithmeticUtil.compareTod(balance, pinganBalance) > 0) {
                                tranAmount = ArithmeticUtil.subStr2(balance, pinganBalance);
                                type = CustomTransferRecordType.ADJUSTMENTINTO;
                            } else if (ArithmeticUtil.compareTod(balance, pinganBalance) < 0) {
                                tranAmount = ArithmeticUtil.subStr2(pinganBalance, balance);
                                type = CustomTransferRecordType.ADJUSTMENTOUT;
                            }


                            transferRecord.setTranAmount(tranAmount);
                            transferRecord.setTranType(type.getCode());
                            transferRecord.setFlag(type.getFlag());


                            transferRecord.setOppAccountNo(paymentConfig.getShadowAcctNo());
                            transferRecord.setOppAccountName(paymentConfig.getCorporationAccountName());
                            transferRecord.setOppBankNo(paymentConfig.getParameter1());
                            transferRecord.setOppBankName(paymentConfig.getCorporationName());
                            transferRecord.setIsConfirm(ConfirmStatus.Paying.getCode());
                            transferRecord.setRemark(type.getDesc() + "(" + loginUser.getUsername() + ")");

                            String date = DateUtils.formartDate(new Date(), "yyyyMMddHHmmss");

                            transferRecord.setTranTime(date);

                            logger.info("----------??????????????????????????????,????????????:{},????????????:{}----------", transferRecord.getRemark(), transferRecord.getTranAmount());

                            res = accountSystem.subAccountSubmitTransfer(orderNoUtil.getChannelSerialno(), transferRecord);
                            if (res.isOk()) {
                                String bizFlowNo = res.getAttachment();
                                transferRecord.setBizFlowNo(bizFlowNo);
                                customTransferRecordService.insert(transferRecord);

                                logger.info("----------??????????????????,??????????????????{}----------", bizFlowNo);

                                return returnSuccess();
                            } else {
                                logger.error("----------???????????????????????????:----------" + res.getFailMessage());
                                return returnFail(RespCode.error107, res.getFailMessage());
                            }

                        } else {
                            logger.error("----------???????????????????????????:----------" + res.getFailMessage());
                            return returnFail(RespCode.error107, res.getFailMessage());
                        }
                    } else {
                        return returnFail(RespCode.error101, RespCode.SUBACCOUNT_UNSUPPORT_SYNC);
                    }
                } else {
                    return returnFail(RespCode.error101, RespCode.SUBACCOUNT_NOT_OPEN);
                }
            } else {
                return returnFail(RespCode.error101, RespCode.SUBACCOUNT_NOT_EXIST);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        } finally {
            MDC.remove(PROCESS);
        }

    }


    /**
     * ????????????????????????
     *
     * @author linsong
     * @date 2020/3/13
     */
    @RequestMapping(value = "/linkageTransferRecord")
    @ResponseBody
    public String linkageTransferRecord(HttpServletRequest res) {

        try {

            String resultStr = StringUtil.convertStreamToString(res.getInputStream());


            //??????222?????????
            if (!(resultStr.length() < 222)) {
                String retCode = resultStr.substring(87, 93);

                //????????????????????????000000?????????????????????xml?????????
                if (PinganBankTransactionConstants.TRAN_RET_CODE.equals(retCode)) {
                    resultStr = resultStr.substring(222);

                    Document doc = DocumentHelper.parseText(resultStr);
                    Element rootElt = doc.getRootElement();


                    String subAccount = rootElt.element("STT_VCH_CARD_NO").getStringValue();
                    String oppAccountNo = rootElt.element("STT_VCH_RCV_AC_NO").getStringValue();
                    String oppAccountName = rootElt.element("STT_VCH_RCV_AC_NAME").getStringValue();
                    String flag = rootElt.element("STT_VCH_SIGN").getStringValue();
                    String tranTime = rootElt.element("STT_TR_TIME").getStringValue();
                    String mainAccount = rootElt.element("STT_VCH_AC_NO").getStringValue();
                    String tranAmount = rootElt.element("STT_VCH_AMT").getStringValue();
                    String oppBankName = rootElt.element("STT_VCH_RCV_BK_NAME").getStringValue();
                    String oppBankNo = rootElt.element("STT_VCH_RCV_BK_NO").getStringValue();
                    String remark = rootElt.element("STT_VCH_REMARK").getStringValue();
                    String bizFlowNo = rootElt.element("STT_CONSUMER_SEQ_NO").getStringValue();
                    String mainAccountName = rootElt.element("STT_VCH_CI_NAME").getStringValue();
                    String subAccoutName = rootElt.element("STT_SUBACC_ALIAS").getStringValue();

                    LinkAccountTrans linkAccountTrans = new LinkAccountTrans();


                    if (CustomTransferRecordType.SUBACCOUNTINTO.getFlag().equals(flag)) {
                        linkAccountTrans.setReceiveAccount(mainAccountName);
                        linkAccountTrans.setReceiveAccountNo(mainAccount);
                        linkAccountTrans.setReceiveBank("????????????");
                        linkAccountTrans.setPayAccount(oppAccountName);
                        linkAccountTrans.setPayAccountNo(oppAccountNo);
                        linkAccountTrans.setPayBank(oppBankName);
                        linkAccountTrans.setTranType(AccountTransType.transIn.getCode());
                    } else {
                        linkAccountTrans.setReceiveAccount(oppAccountName);
                        linkAccountTrans.setReceiveAccountNo(oppAccountName);
                        linkAccountTrans.setReceiveBank(oppBankName);
                        linkAccountTrans.setPayAccount(mainAccountName);
                        linkAccountTrans.setPayAccountNo(mainAccount);
                        linkAccountTrans.setPayBank("????????????");
                        linkAccountTrans.setTranType(AccountTransType.transOut.getCode());
                    }

//                    linkAccountTrans.setSubAccount(subAccount);
//                    linkAccountTrans.setSubAccoutName(subAccoutName);
                    linkAccountTrans.setTranTime(tranTime);
//                    linkAccountTrans.setMainAccount(mainAccount);

                    linkAccountTrans.setTranAmount(tranAmount);
                    linkAccountTrans.setMainAccount(oppAccountNo);

                    linkAccountTrans.setRemark(remark);
                    linkAccountTrans.setStatus(AccountTransStatus.success.getCode());
                    linkAccountTrans.setChannelNo(bizFlowNo);
                    linkAccountTransService.insert(linkAccountTrans);


                }
            } else {
                logger.error("-----------????????????????????????,??????:{}---------", resultStr);
            }


        } catch (DuplicateKeyException e) {
            logger.info("----------??????????????????????????????---------");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return "SUCCESS";
    }
}
