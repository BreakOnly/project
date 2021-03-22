package com.jrmf.payment.entity;

import com.jrmf.bankapi.*;
import com.jrmf.bankapi.pingan.transaction.QueryBalanceReportTemplate;
import com.jrmf.bankapi.pingansub.*;
import com.jrmf.bankapi.pingansub.params.LinkageSubAccountQueryHistoryTransferResultParam;
import com.jrmf.bankapi.pingansub.transaction.LinkageSubAccountQueryHistoryTransferResultTemplate;
import com.jrmf.bankapi.pingansub.transaction.QueryTransferResultTemplate;
import com.jrmf.bankapi.pingansub.transaction.SubmitTransferReportTemplate;
import com.jrmf.controller.constant.IsSubAccount;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class PingAnBankKhkf implements Payment<SubmitTransferParams, ActionReturn, String> {

    private Logger logger = LoggerFactory.getLogger(PingAnBankYqzl.class);

    public PaymentConfig payment;

    public PingAnBankKhkf(PaymentConfig payment) {
        super();
        this.payment = payment;
    }

    @Override
    public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {

        PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(payment.getCorporationAccount(),
                payment.getCorporationAccountName(),
                payment.getCorpToBankStandardCode(),
                payment.getCorporationName());

        Map<String, TransReportTemplate<?, ?>> templates = new HashMap<String, TransReportTemplate<?, ?>>();
        templates.put(PinganBankTransactions.SUBMIT_TRANSFER, new SubmitTransferReportTemplate());

        TransReportTemplates reportFactory = new TransReportTemplates(templates);

        DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
                payment.getRemotePort(),
                payment.getReadTimeOut());

        PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory, exchanger);


        ActionReturn<String> ret = null;
        ret = pinganBankService.submitTransfer(getTransferTemple(userCommission));

        return getTransferResult(ret);
    }

    @Override
    public SubmitTransferParams getTransferTemple(UserCommission userCommission) {

        SubmitTransferParams params = new SubmitTransferParams();
        params.setTransferAmount(userCommission.getAmount());
        params.setTransferInAccountName(userCommission.getUserName());
        params.setTransferInAccountNo(userCommission.getAccount());
        params.setTransferInBankName(userCommission.getBankName());
        params.setTransferSerialNo(userCommission.getOrderNo());
        params.setPhoneNo(userCommission.getPhoneNo());
        params.setSubAcctNo(userCommission.getSubAcctNo());

        //兼容现金管理代理结算时只能输入13个汉字
        String commissionRemark = userCommission.getRemark() == null ? "" : userCommission.getRemark();
        if (commissionRemark.length() > 13) {
            commissionRemark = commissionRemark.substring(0, 13);
        }
        params.setRemark(commissionRemark);

        if (!StringUtil.isEmpty(userCommission.getBankNo())) {
            params.setTransferInBankOrgNo(userCommission.getBankNo());
        }

        logger.info("平安--跨行快付-银企直联上送下发参数：" + params.getTransferSerialNo() + "-------" +
                params.getTransferAmount() + "-------" +
                params.getTransferInAccountNo() + "-------" +
                params.getTransferInBankOrgNo() + "-------" +
                params.getTransferInBankName() + "-------" +
                params.getTransferInAccountName() + "-------");

        return params;
    }

    @Override
    public PaymentReturn<String> getTransferResult(ActionReturn paramter) {

        String retCode = paramter.getRetCode();
        String message = paramter.getFailMessage();
        if (CommonRetCodes.INVOCATION_NO_RESULT.getCode().equals(retCode)
                || CommonRetCodes.UNEXPECT_ERROR.getCode().equals(retCode)) {
            retCode = PayRespCode.RESP_UNKNOWN;
            message = CommonRetCodes.INVOCATION_NO_RESULT.getDesc();
        }

        PaymentReturn<String> transferReturn = new PaymentReturn<>(retCode,
                message,
                String.valueOf(paramter.getAttachment()));

        logger.info("平安--跨行快付-下发返回参数：" + transferReturn.toString());

        return transferReturn;
    }

    @Override
    public PaymentReturn<TransStatus> queryTransferResult(String orderNo) {

        PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(payment.getCorporationAccount(),
                payment.getCorporationAccountName(),
                payment.getCorpToBankStandardCode(),
                payment.getCorporationName(),
                payment.getSubAcctNo());

        Map<String, TransReportTemplate<?, ?>> templates = new HashMap<String, TransReportTemplate<?, ?>>();
        templates.put(PinganBankTransactions.QUERY_TRANSFER_RESULT, (TransReportTemplate<?, ?>) new QueryTransferResultTemplate());

        TransReportTemplates reportFactory = new TransReportTemplates(templates);

        DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
                payment.getRemotePort(),
                payment.getReadTimeOut());
        PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory, exchanger);

        ActionReturn<TransferResult> transferResult = pinganBankService.queryTransferResult(orderNo);

        String code = "";
        String massage = "";
        String transCode = "";
        String transMsg = "";
        String transOrderNo = orderNo;

        if (transferResult.isOk()) {
            code = PayRespCode.RESP_SUCCESS;
            if (TransferResult.TransferResultType.SUCCESS.equals(transferResult.getAttachment().getResultType())) {
                transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
            } else if (TransferResult.TransferResultType.FAIL.equals(transferResult.getAttachment().getResultType())) {
                transCode = PayRespCode.RESP_TRANSFER_FAILURE;
            } else {
                transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
            }
        } else if (PayRespCode.PA_RESP_ORDER_NOEXISTS_KHKF.equals(transferResult.getRetCode())) {
            code = PayRespCode.RESP_SUCCESS;
            transCode = PayRespCode.RESP_TRANSFER_FAILURE;
            transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_NETWORK_EXCEPTION);
        } else {
            code = PayRespCode.RESP_FAILURE;
        }
        massage = transferResult.getFailMessage();
        TransferResult transResult = transferResult.getAttachment();
        if (transResult != null) {
            transMsg = transResult.getResultMsg();
        }

        TransStatus transStatus = new TransStatus(transOrderNo,
                transCode,
                transMsg);

        PaymentReturn<TransStatus> paymentReturn = new PaymentReturn<TransStatus>(code,
                massage,
                transStatus);

        return paymentReturn;
    }

    @Override
    public PaymentReturn<String> queryBalanceResult(String type) {
        com.jrmf.bankapi.pingan.PinganBankAccountInfo bankAccountInfo = new com.jrmf.bankapi.pingan.PinganBankAccountInfo(payment.getCorporationAccount(),
                payment.getCorporationAccountName(),
                payment.getCorpToBankStandardCode(),
                payment.getCorporationName());
        Map<String, com.jrmf.bankapi.pingan.TransReportTemplate<?,?>> templates = new HashMap<String, com.jrmf.bankapi.pingan.TransReportTemplate<?,?>>();
        templates.put(com.jrmf.bankapi.pingan.PinganBankTransactions.QUERY_BALANCE, new QueryBalanceReportTemplate());
        com.jrmf.bankapi.pingan.TransReportTemplates reportFactory = new com.jrmf.bankapi.pingan.TransReportTemplates(templates);
        com.jrmf.bankapi.pingan.DataExchanger exchanger = new com.jrmf.bankapi.pingan.SocketDataExchanger(payment.getPreHost(),
                payment.getRemotePort(),
                payment.getReadTimeOut());

        BankService pinganBankService = new com.jrmf.bankapi.pingan.PinganBankService(bankAccountInfo, reportFactory, exchanger);
        ActionReturn<String> paramter = pinganBankService.queryBalace();
        PaymentReturn<String> transferReturn = new PaymentReturn<String>(paramter.getRetCode(),
                paramter.getFailMessage(),
                String.valueOf(paramter.getAttachment()));
        logger.info("跨行快付查询返回参数：" + transferReturn.toString());

        return transferReturn;
    }

    @Override
    public PaymentReturn<String> linkageTransfer(LinkageTransferRecord transferRecord) {

        PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(payment.getCorporationAccount(),
                payment.getCorporationAccountName(),
                payment.getCorpToBankStandardCode(),
                payment.getCorporationName());

        Map<String, TransReportTemplate<?, ?>> templates = new HashMap<>();
        templates.put(PinganBankTransactions.SUBMIT_TRANSFER, new SubmitTransferReportTemplate());

        TransReportTemplates reportFactory = new TransReportTemplates(templates);

        DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
                payment.getRemotePort(),
                payment.getReadTimeOut());

        PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory, exchanger);


        SubmitTransferParams params = new SubmitTransferParams();
        params.setTransferAmount(transferRecord.getTranAmount());
        params.setTransferInAccountName(transferRecord.getInAccountName());
        params.setTransferInAccountNo(transferRecord.getInAccountNo());
        params.setTransferInBankName(transferRecord.getInBankName());
        params.setTransferSerialNo(transferRecord.getOrderNo());
        if (IsSubAccount.YES.getCode() == transferRecord.getIsSubAccount()) {
            params.setSubAcctNo(transferRecord.getPaySubAccount());
        }

        String remark = transferRecord.getTranRemark() == null ? "" : transferRecord.getTranRemark();
        params.setRemark(remark);

        if (!StringUtil.isEmpty(transferRecord.getInBankNo())) {
            params.setTransferInBankOrgNo(transferRecord.getInBankNo());
        }

        logger.info("平安--跨行快付-联动交易转账上送参数：" + params.getTransferSerialNo() + "-------" +
                params.getTransferAmount() + "-------" +
                params.getTransferInAccountNo() + "-------" +
                params.getTransferInBankOrgNo() + "-------" +
                params.getTransferInBankName() + "-------" +
                params.getTransferInAccountName() + "-------");

        ActionReturn<String> ret = pinganBankService.submitTransfer(params);

        return getTransferResult(ret);
    }

    @Override
    public PaymentReturn<LinkageTransHistoryPage> queryTransHistoryPage(LinkageQueryTranHistory params) {

        PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(payment.getCorporationAccount(),
                payment.getCorporationAccountName(),
                payment.getCorpToBankStandardCode(),
                payment.getCorporationName());

        Map<String, TransReportTemplate<?, ?>> templates = new HashMap<>();
        templates.put(PinganBankTransactions.QUERY_SUBACCOUNT_HISTORY_TRANSFER_RESULT_NEW, new LinkageSubAccountQueryHistoryTransferResultTemplate());

        TransReportTemplates reportFactory = new TransReportTemplates(templates);

        DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
                payment.getRemotePort(),
                payment.getReadTimeOut());

        PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory, exchanger);

        LinkageSubAccountQueryHistoryTransferResultParam queryParams = new LinkageSubAccountQueryHistoryTransferResultParam();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        String time = df.format(new Date()).replaceAll("[[\\s-:punct:]]","");
        queryParams.setStartDate(params.getStartDate() + time);
        queryParams.setEndDate(params.getEndDate() + time);
        queryParams.setMainAccount(payment.getCorporationAccount());
        queryParams.setPageSize(params.getPageSize()+"");
        queryParams.setPageNo(params.getPageNo());
        // 判断是否是子账户
        if (IsSubAccount.YES.getCode() == payment.getIsSubAccount()) {
            queryParams.setOpFlag("2");
            queryParams.setReqSubAccount(payment.getSubAcctNo());

        }

        ActionReturn<LinkageTransHistoryPage> paramter = pinganBankService.querySubAccountTransHistoryPageNew(queryParams);
        PaymentReturn<LinkageTransHistoryPage> transferReturn = new PaymentReturn<LinkageTransHistoryPage>(paramter.getRetCode(),
                paramter.getFailMessage(),
                paramter.getAttachment());

        return transferReturn;
    }

}
