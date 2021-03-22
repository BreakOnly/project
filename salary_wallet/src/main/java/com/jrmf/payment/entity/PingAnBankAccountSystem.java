package com.jrmf.payment.entity;

import com.jrmf.bankapi.ActionReturn;
import com.jrmf.bankapi.TransHistoryPage;
import com.jrmf.bankapi.TransferResult;
import com.jrmf.bankapi.pingansub.*;
import com.jrmf.bankapi.pingansub.params.SubAccountMaintainParam;
import com.jrmf.bankapi.pingansub.params.SubAccountQueryHistoryTransferResultParam;
import com.jrmf.bankapi.pingansub.params.SubAccountQueryParam;
import com.jrmf.bankapi.pingansub.params.SubAccountSubmitTransferParam;
import com.jrmf.bankapi.pingansub.transaction.*;
import com.jrmf.controller.constant.CustomTransferRecordType;
import com.jrmf.domain.CustomReceiveConfig;
import com.jrmf.domain.CustomTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.payment.util.PayRespCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PingAnBankAccountSystem implements AccountSystem {

  public PaymentConfig payment;

  public PingAnBankAccountSystem(PaymentConfig payment) {
    super();
    this.payment = payment;
  }

  /**
   * 添加平安子账号
   *
   * @author linsong
   * @date 2019/9/4
   */
  public ActionReturn<String> addSubAccount(CustomReceiveConfig receiveConfig) {

    PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(
        payment.getCorporationAccount(),
        payment.getCorporationAccountName(),
        payment.getCorpToBankStandardCode(),
        payment.getCorporationName());

    Map<String, TransReportTemplate<?, ?>> templates = new HashMap<String, TransReportTemplate<?, ?>>();
    templates.put(PinganBankTransactions.MAINTAIN_SUBACCOUNT, new SubAccountMaintainTemplate());

    TransReportTemplates reportFactory = new TransReportTemplates(templates);

    DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
        payment.getRemotePort(),
        payment.getReadTimeOut());

    PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory,
        exchanger);

    SubAccountMaintainParam param = new SubAccountMaintainParam();
    param.setMainAccount(payment.getCorporationAccount());
    DecimalFormat countFormat = new DecimalFormat("000000");
    param.setSubAccountSeq(countFormat.format(receiveConfig.getCustomId()));
    param.setSubAccountName(receiveConfig.getContractCompanyName());
    param.setOpFlag(PinganBankTransactionConstants.SUBACCOUNT_ADD);

    ActionReturn<String> ret = pinganBankService.maintainSubAccount(param);

    return ret;
  }

  public ActionReturn<String> recoverySubAccount(CustomReceiveConfig receiveConfig) {

    PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(
        payment.getCorporationAccount(),
        payment.getCorporationAccountName(),
        payment.getCorpToBankStandardCode(),
        payment.getCorporationName());

    Map<String, TransReportTemplate<?, ?>> templates = new HashMap<String, TransReportTemplate<?, ?>>();
    templates.put(PinganBankTransactions.MAINTAIN_SUBACCOUNT, new SubAccountMaintainTemplate());

    TransReportTemplates reportFactory = new TransReportTemplates(templates);

    DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
        payment.getRemotePort(),
        payment.getReadTimeOut());

    PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory,
        exchanger);

    SubAccountMaintainParam param = new SubAccountMaintainParam();
    param.setMainAccount(payment.getCorporationAccount());
    param.setSubAccount(receiveConfig.getReceiveAccount());
    param.setSubAccountName(receiveConfig.getContractCompanyName());
    param.setOpFlag(PinganBankTransactionConstants.SUBACCOUNT_RECOVERY);

    ActionReturn<String> ret = pinganBankService.maintainSubAccount(param);

    return ret;
  }


  public ActionReturn<String> deleteSubAccount(CustomReceiveConfig receiveConfig) {

    PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(
        payment.getCorporationAccount(),
        payment.getCorporationAccountName(),
        payment.getCorpToBankStandardCode(),
        payment.getCorporationName());

    Map<String, TransReportTemplate<?, ?>> templates = new HashMap<String, TransReportTemplate<?, ?>>();
    templates.put(PinganBankTransactions.MAINTAIN_SUBACCOUNT, new SubAccountMaintainTemplate());

    TransReportTemplates reportFactory = new TransReportTemplates(templates);

    DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
        payment.getRemotePort(),
        payment.getReadTimeOut());

    PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory,
        exchanger);

    SubAccountMaintainParam param = new SubAccountMaintainParam();
    param.setMainAccount(payment.getCorporationAccount());
    param.setSubAccount(receiveConfig.getReceiveAccount());
    param.setOpFlag(PinganBankTransactionConstants.SUBACCOUNT_DELETE);

    ActionReturn<String> ret = pinganBankService.maintainSubAccount(param);

    return ret;
  }


  public ActionReturn<SubAccountTransHistoryPage> queryTransferHistory(
      SubAccountQueryHistoryTransferResultParam param) {

    PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(
        payment.getCorporationAccount(),
        payment.getCorporationAccountName(),
        payment.getCorpToBankStandardCode(),
        payment.getCorporationName());

    Map<String, TransReportTemplate<?, ?>> templates = new HashMap<String, TransReportTemplate<?, ?>>();
    templates.put(PinganBankTransactions.QUERY_SUBACCOUNT_HISTORY_TRANSFER_RESULT,
        new SubAccountQueryHistoryTransferResultTemplate());

    TransReportTemplates reportFactory = new TransReportTemplates(templates);

    DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
        payment.getRemotePort(),
        payment.getReadTimeOut());

    PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory,
        exchanger);

    SubAccountQueryHistoryTransferResultParam transferResultParam = new SubAccountQueryHistoryTransferResultParam();
    transferResultParam.setMainAccount(payment.getCorporationAccount());
    transferResultParam.setReqSubAccount(param.getReqSubAccount());
    transferResultParam.setStartDate(param.getStartDate());
    transferResultParam.setEndDate(param.getEndDate());
    transferResultParam.setReqLastSubAccNo(param.getReqLastSubAccNo());
    transferResultParam.setReqLastDate(param.getReqLastDate());
    transferResultParam.setReqLastJNo(param.getReqLastJNo());
    transferResultParam.setReqLastSeq(param.getReqLastSeq());
    transferResultParam.setOpFlag(param.getOpFlag());

    ActionReturn<SubAccountTransHistoryPage> ret = pinganBankService
        .querySubAccountTransHistoryPage(transferResultParam);

    return ret;
  }


  /**
   * 递归获取平安分页数据
   *
   * @author linsong
   * @date 2019/9/5
   */
  public List<SubAccountTransHistoryRecord> getTransHistoryPage(
      SubAccountQueryHistoryTransferResultParam param,
      List<SubAccountTransHistoryRecord> transHistoryRecords) {

    ActionReturn<SubAccountTransHistoryPage> result = queryTransferHistory(param);
    if (result != null && result.getAttachment() != null) {
      SubAccountTransHistoryPage resultPage = result.getAttachment();

      if (resultPage.getTransHistoryRecords() != null) {
        transHistoryRecords.addAll(resultPage.getTransHistoryRecords());
      }

      if (resultPage.isHasNextPage()) {
        SubAccountTransHistoryRecord lastData = transHistoryRecords
            .get(transHistoryRecords.size() - 1);
        param.setReqLastSubAccNo(lastData.getSubAccount());
        param.setReqLastDate(lastData.getAccountDate());
        param.setReqLastJNo(lastData.getJournalNo());
        param.setReqLastSeq(lastData.getSeqNo());
        getTransHistoryPage(param, transHistoryRecords);
      }
    }

    return transHistoryRecords;
  }


  //子账户余额查询
  public ActionReturn<String> querySubAccountBalance(String customKey,String subAccount) {

    PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(
        payment.getCorporationAccount(),
        payment.getCorporationAccountName(),
        payment.getCorpToBankStandardCode(),
        payment.getCorporationName());

    Map<String, TransReportTemplate<?, ?>> templates = new HashMap<String, TransReportTemplate<?, ?>>();
    templates
        .put(PinganBankTransactions.QUERY_SUBACCOUNT_BALANCE, new SubAccountBalanceQueryTemplate());

    TransReportTemplates reportFactory = new TransReportTemplates(templates);

    DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
        payment.getRemotePort(),
        payment.getReadTimeOut());

    PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory,
        exchanger);

    SubAccountQueryParam param = new SubAccountQueryParam();
    param.setReqSubAccountNo(subAccount);
    ActionReturn<String> ret = pinganBankService.querySubAccountBalace(param);

    return ret;

  }


  public ActionReturn<String> subAccountSubmitTransfer(String orderNo,
      CustomTransferRecord transfer) {
    PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(
        payment.getCorporationAccount(),
        payment.getCorporationAccountName(),
        payment.getCorpToBankStandardCode(),
        payment.getCorporationName());

    Map<String, TransReportTemplate<?, ?>> templates = new HashMap<String, TransReportTemplate<?, ?>>();
    templates.put(PinganBankTransactions.SUBACCOUNT_SUBMIT_TRANSFER,
        new SubAccountSubmitTransferTemplate());

    TransReportTemplates reportFactory = new TransReportTemplates(templates);

    DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
        payment.getRemotePort(),
        payment.getReadTimeOut());

    PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory,
        exchanger);

    SubAccountSubmitTransferParam param = new SubAccountSubmitTransferParam();

    param.setThirdVoucher(orderNo);
    param.setCstInnerFlowNo(orderNo);
    param.setMainAccount(payment.getCorporationAccount());
    param.setMainAccountName(payment.getCorporationAccountName());
    param.setTranAmount(transfer.getTranAmount());
    param.setUseEx(transfer.getRemark());

    if (CustomTransferRecordType.ADJUSTMENTINTO.getCode() == transfer.getTranType()) {
      param.setOutSubAccount(transfer.getOppAccountNo());
      param.setOutSubAccountName(transfer.getOppAccountName());
      param.setInSubAccNo(transfer.getSubAccount());
      param.setInSubAccName(transfer.getSubAccoutName());
    } else if (CustomTransferRecordType.ADJUSTMENTOUT.getCode() == transfer.getTranType()) {
      param.setOutSubAccount(transfer.getSubAccount());
      param.setOutSubAccountName(transfer.getSubAccoutName());
      param.setInSubAccNo(transfer.getOppAccountNo());
      param.setInSubAccName(transfer.getOppAccountName());
    }

    ActionReturn<String> ret = pinganBankService.subAccountSubmitTransfer(param);

    return ret;
  }

  public ActionReturn<TransferResult> querySubAccountTransferResult(String bizFlowNo) {
    PinganBankAccountInfo bankAccountInfo = new PinganBankAccountInfo(
        payment.getCorporationAccount(),
        payment.getCorporationAccountName(),
        payment.getCorpToBankStandardCode(),
        payment.getCorporationName());

    Map<String, TransReportTemplate<?, ?>> templates = new HashMap<String, TransReportTemplate<?, ?>>();
    templates.put(PinganBankTransactions.QUERY_SUBACCOUNT_TRANSFER_RESULT,
        new SubAccountQueryTransferResultTemplate());

    TransReportTemplates reportFactory = new TransReportTemplates(templates);

    DataExchanger exchanger = new SocketDataExchanger(payment.getPreHost(),
        payment.getRemotePort(),
        payment.getReadTimeOut());

    PinganBankService pinganBankService = new PinganBankService(bankAccountInfo, reportFactory,
        exchanger);

    ActionReturn<TransferResult> ret = pinganBankService.querySubAccountTransferResult(bizFlowNo);

    return ret;
  }

}
