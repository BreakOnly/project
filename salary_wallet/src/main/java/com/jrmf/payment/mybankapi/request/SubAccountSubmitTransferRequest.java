package com.jrmf.payment.mybankapi.request;

import com.jrmf.payment.mybankapi.common.enums.Service;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubAccountSubmitTransferRequest extends MyBankBaseRequest{

  private String outer_trade_no;
  private String fundin_uid; //入款用户Id,金额增加方的用户ID（UID）或会员ID（内部会员ID）
  private String fundin_account_type; //入款账户类型。金额增加的账户的账户类型
  private String fundout_uid; //用户Id,金额减少方的用户ID（UID），或会员ID（内部会员ID）
  private String fundout_account_type; //账户类型,金额减少的账户的账户类型
  private String transfer_amount; //转账金额。必须大于0

  public SubAccountSubmitTransferRequest() {
    this.service = Service.payment_to_subaccount.getServiceName();
  }

}
