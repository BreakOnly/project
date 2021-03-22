package com.jrmf.utils.merchant;

import com.jrmf.controller.constant.TradeType;
import com.jrmf.domain.ForwardCompanyAccount;
import com.jrmf.domain.ForwardCompanyAccountHistory;

/**
 * @author: YJY
 * @date: 2020/11/25 14:19
 * @description:
 */
public class AccountBeanUtil {



  /**
   * @Description 初始化 历史记录Bean
   **/
  public static ForwardCompanyAccountHistory putData(ForwardCompanyAccount forwardCompanyAccount) {

    ForwardCompanyAccountHistory forwardCompanyAccountHistory = new ForwardCompanyAccountHistory();
    forwardCompanyAccountHistory.setAccountId(forwardCompanyAccount.getId());
    forwardCompanyAccountHistory.setAmount(1);
    forwardCompanyAccountHistory.setCompanyId(forwardCompanyAccount.getCompanyId());
    forwardCompanyAccountHistory.setRealCompanyId(forwardCompanyAccount.getRealCompanyId());
    forwardCompanyAccountHistory.setCustomKey(forwardCompanyAccount.getCustomKey());
    forwardCompanyAccountHistory.setTradeType(TradeType.SUBBALANCE.getCode());
    forwardCompanyAccountHistory.setMerchantName(forwardCompanyAccount.getMerchantName());
    forwardCompanyAccountHistory.setCompanyName(forwardCompanyAccount.getCompanyName());
    forwardCompanyAccountHistory.setRealCompanyName(forwardCompanyAccount.getRealCompanyName());
    forwardCompanyAccountHistory.setRelateOrderNo(forwardCompanyAccount.getId()+"");
    return forwardCompanyAccountHistory;
  }
}
