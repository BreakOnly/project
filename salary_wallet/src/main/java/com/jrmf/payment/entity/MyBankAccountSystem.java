package com.jrmf.payment.entity;

import com.jrmf.bankapi.ActionReturn;
import com.jrmf.bankapi.CommonRetCodes;
import com.jrmf.bankapi.TransferResult;
import com.jrmf.bankapi.TransferResult.TransferResultType;
import com.jrmf.bankapi.pingansub.SubAccountTransHistoryPage;
import com.jrmf.bankapi.pingansub.SubAccountTransHistoryRecord;
import com.jrmf.bankapi.pingansub.params.SubAccountQueryHistoryTransferResultParam;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.CustomTransferRecordType;
import com.jrmf.domain.CustomReceiveConfig;
import com.jrmf.domain.CustomTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.payment.mybankapi.common.constant.BaseRequestConstant;
import com.jrmf.payment.mybankapi.request.ModifyEnterpriseInfoRequest;
import com.jrmf.payment.mybankapi.request.QueryAccountBalanceRequest;
import com.jrmf.payment.mybankapi.request.QueryTransferResultRequest;
import com.jrmf.payment.mybankapi.request.RegisterEnterpriseInfoRequest;
import com.jrmf.payment.mybankapi.request.SubAccountSubmitTransferRequest;
import com.jrmf.payment.mybankapi.response.MyBankBaseResponse;
import com.jrmf.payment.mybankapi.response.QueryAccountBalanceResponse;
import com.jrmf.payment.mybankapi.response.QueryTransferResultResponse;
import com.jrmf.payment.mybankapi.response.RegisterEnterpriseInfoResponse;
import com.jrmf.payment.mybankapi.service.MyBankService;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.utils.SpringContextUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyBankAccountSystem implements AccountSystem {

  private Logger logger = LoggerFactory.getLogger(MyBankAccountSystem.class);
  private BaseInfo baseInfo = SpringContextUtil.getBean(BaseInfo.class);


  public PaymentConfig payment;

  public MyBankAccountSystem(PaymentConfig payment) {
    super();
    this.payment = payment;
  }

  @Override
  public ActionReturn<String> addSubAccount(CustomReceiveConfig receiveConfig) {
    RegisterEnterpriseInfoRequest request = new RegisterEnterpriseInfoRequest();
    request.setUid(receiveConfig.getCustomkey());
    request.setEnterprise_name(receiveConfig.getContractCompanyName());

    MyBankService myBankService = new MyBankService.Builder()
        .reqUrl(payment.getPreHost()).configFilePath(payment.getPayPrivateKey())
        .keyStoreName(payment.getParameter2()).partnerId(payment.getApiKey())
        .build();

    RegisterEnterpriseInfoResponse response = (RegisterEnterpriseInfoResponse) myBankService
        .registerEnterpriseInfo(request);

    if (response == null) {
      return new ActionReturn<>(CommonRetCodes.UNEXPECT_ERROR.getCode(),
          CommonRetCodes.UNEXPECT_ERROR.getDesc());
    }

    if (BaseRequestConstant.SUCCESS.equals(response.getIs_success())) {
      return new ActionReturn<>(payment.getCorpToBankStandardCode() + response.getSub_account_no());
    } else {
      return new ActionReturn<>(CommonRetCodes.INVOCATION_NO_RESULT.getCode(),
          response.getError_message());
    }

  }

  @Override
  public ActionReturn<String> recoverySubAccount(CustomReceiveConfig receiveConfig) {

    ModifyEnterpriseInfoRequest request = new ModifyEnterpriseInfoRequest();
    request.setUid(receiveConfig.getCustomkey());
    request.setEnterprise_name(receiveConfig.getContractCompanyName());

    MyBankService myBankService = new MyBankService.Builder()
        .reqUrl(payment.getPreHost()).configFilePath(payment.getPayPrivateKey())
        .keyStoreName(payment.getParameter2()).partnerId(payment.getApiKey())
        .build();

    MyBankBaseResponse response = myBankService
        .modifyEnterpriseInfo(request);

    if (response == null) {
      return new ActionReturn<>(CommonRetCodes.UNEXPECT_ERROR.getCode(),
          CommonRetCodes.UNEXPECT_ERROR.getDesc());
    }

    if (BaseRequestConstant.SUCCESS.equals(response.getIs_success())) {
      return new ActionReturn<>();
    } else {
      return new ActionReturn<>(CommonRetCodes.INVOCATION_NO_RESULT.getCode(),
          response.getError_message());
    }
  }

  @Override
  public ActionReturn<String> deleteSubAccount(CustomReceiveConfig receiveConfig) {
    return null;
  }

  @Override
  public ActionReturn<SubAccountTransHistoryPage> queryTransferHistory(
      SubAccountQueryHistoryTransferResultParam param) {
    return null;
  }

  @Override
  public List<SubAccountTransHistoryRecord> getTransHistoryPage(
      SubAccountQueryHistoryTransferResultParam param,
      List<SubAccountTransHistoryRecord> transHistoryRecords) {
    return null;
  }

  @Override
  public ActionReturn<String> querySubAccountBalance(String customKey, String subAccount) {

    QueryAccountBalanceRequest request = new QueryAccountBalanceRequest();
    request.setUid(customKey);
    request.setAccount_type("BASIC");

    MyBankService myBankService = new MyBankService.Builder()
        .reqUrl(payment.getPreHost()).configFilePath(payment.getPayPrivateKey())
        .keyStoreName(payment.getParameter2()).partnerId(payment.getApiKey())
        .build();

    QueryAccountBalanceResponse response = (QueryAccountBalanceResponse) myBankService
        .queryAccountBaLance(request);

    if (response == null || response.getAccount_list() == null || !subAccount
        .endsWith(response.getAccount_list().get(0).getSub_account_no())) {
      return new ActionReturn<>(CommonRetCodes.UNEXPECT_ERROR.getCode(),
          CommonRetCodes.UNEXPECT_ERROR.getDesc());
    }

    if (BaseRequestConstant.SUCCESS.equals(response.getIs_success())) {
      return new ActionReturn<>(response.getAccount_list().get(0).getAvailable_balance());
    } else {
      return new ActionReturn<>(CommonRetCodes.INVOCATION_NO_RESULT.getCode(),
          response.getError_message());
    }
  }

  @Override
  public ActionReturn<String> subAccountSubmitTransfer(String orderNo,
      CustomTransferRecord transfer) {

    MyBankService myBankService = new MyBankService.Builder()
        .reqUrl(payment.getPreHost()).configFilePath(payment.getPayPrivateKey())
        .keyStoreName(payment.getParameter2()).partnerId(payment.getApiKey())
        .build();

    SubAccountSubmitTransferRequest request = new SubAccountSubmitTransferRequest();
    request.setOuter_trade_no(orderNo);
    request.setTransfer_amount(transfer.getTranAmount());

    if (CustomTransferRecordType.ADJUSTMENTINTO.getCode() == transfer.getTranType()) {
      request.setFundin_uid(transfer.getCustomKey());
      request.setFundin_account_type("BASIC");
      request.setFundout_uid(payment.getParameter9());
      request.setFundout_account_type("PINCOME");
    } else if (CustomTransferRecordType.ADJUSTMENTOUT.getCode() == transfer.getTranType()) {
      request.setFundout_uid(transfer.getCustomKey());
      request.setFundout_account_type("BASIC");
      request.setFundin_uid(payment.getParameter9());
      request.setFundin_account_type("PINCOME");
    }


    MyBankBaseResponse response = myBankService.subAccountSubmitTransfer(request);

    if (response == null) {
      return new ActionReturn<>(CommonRetCodes.UNEXPECT_ERROR.getCode(),
          CommonRetCodes.UNEXPECT_ERROR.getDesc());
    }

    if (BaseRequestConstant.SUCCESS.equals(response.getIs_success())) {
      return new ActionReturn<>(orderNo);
    } else {
      return new ActionReturn<>(CommonRetCodes.INVOCATION_NO_RESULT.getCode(),
          response.getError_message());
    }

  }

  @Override
  public ActionReturn<TransferResult> querySubAccountTransferResult(String bizFlowNo) {
    String transMsg = "";

    MyBankService myBankService = new MyBankService.Builder()
        .reqUrl(payment.getPreHost()).configFilePath(payment.getPayPrivateKey())
        .keyStoreName(payment.getParameter2()).partnerId(payment.getApiKey())
        .build();

    QueryTransferResultRequest request = new QueryTransferResultRequest();
    request.setOuter_trade_no(bizFlowNo);

    QueryTransferResultResponse response = (QueryTransferResultResponse) myBankService
        .queryTransferResult(request);
    if (BaseRequestConstant.SUCCESS.equals(response.getIs_success())) {
      //查询成功
      logger.info("订单号：" + bizFlowNo + "查询成功");

      if (BaseRequestConstant.TRADE_FINISHED.equals(response.getTrade_status())) {
        //交易成功
        logger.info("订单号：{} 交易成功,交易流水号为：{}", bizFlowNo, response.getInner_trade_no());
        return new ActionReturn<>(new TransferResult(TransferResultType.SUCCESS, transMsg));
      } else if (BaseRequestConstant.TRADE_FAILED.equals(response.getTrade_status())) {
        //交易失败
        logger.info("订单号：{} 交易失败：{}", bizFlowNo, response.getError_message());
        return new ActionReturn<>(new TransferResult(TransferResultType.FAIL, transMsg));
      } else {
        //待付款/汇款处理中
        logger.info("订单号：{} 处理中：{}", bizFlowNo, response);
        return new ActionReturn<>(new TransferResult(TransferResultType.UNKNOWN, transMsg));
      }
    } else {
      return new ActionReturn<>(new TransferResult(TransferResultType.UNKNOWN, transMsg));
    }

  }
}
