package com.jrmf.payment.entity;

import com.jrmf.bankapi.ActionReturn;
import com.jrmf.bankapi.TransferResult;
import com.jrmf.bankapi.pingansub.SubAccountTransHistoryPage;
import com.jrmf.bankapi.pingansub.SubAccountTransHistoryRecord;
import com.jrmf.bankapi.pingansub.params.SubAccountQueryHistoryTransferResultParam;
import com.jrmf.domain.CustomReceiveConfig;
import com.jrmf.domain.CustomTransferRecord;
import java.util.List;

public interface AccountSystem {

  ActionReturn<String> addSubAccount(CustomReceiveConfig receiveConfig);

  ActionReturn<String> recoverySubAccount(CustomReceiveConfig receiveConfig);

  ActionReturn<String> deleteSubAccount(CustomReceiveConfig receiveConfig);

  ActionReturn<SubAccountTransHistoryPage> queryTransferHistory(
      SubAccountQueryHistoryTransferResultParam param);

  List<SubAccountTransHistoryRecord> getTransHistoryPage(
      SubAccountQueryHistoryTransferResultParam param,
      List<SubAccountTransHistoryRecord> transHistoryRecords);

  ActionReturn<String> querySubAccountBalance(String customKey,String subAccount);

  ActionReturn<String> subAccountSubmitTransfer(String orderNo, CustomTransferRecord transfer);

  ActionReturn<TransferResult> querySubAccountTransferResult(String bizFlowNo);
}
