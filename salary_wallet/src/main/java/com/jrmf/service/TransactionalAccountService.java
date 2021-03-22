package com.jrmf.service;

import com.jrmf.common.APIResponse;
import com.jrmf.domain.CustomBalanceHistory;
import com.jrmf.domain.ForwardCompanyAccount;
import com.jrmf.domain.vo.CompanyAccountVo;

/**
 * @author: YJY
 * @date: 2020/11/25 14:13
 * @description: 操作记账户
 */
public interface TransactionalAccountService {

  /**
   * @Author YJY
   * @Description 添加记账户
   * @Date  2020/11/25
   * @Param [accountVo]
   * @return com.jrmf.common.APIResponse
   **/
 APIResponse addAccountData(CompanyAccountVo accountVo);

 /**
  * @Author YJY
  * @Description 修改记账户
  * @Date  2020/11/25
  * @Param [accountVo, forwardCompanyAccount]
  * @return com.jrmf.common.APIResponse
  **/
 APIResponse updateAccountData(CompanyAccountVo accountVo, ForwardCompanyAccount forwardCompanyAccount);

  /**
  * @Description 扣减商户余额和记账户余额
  **/
  void updateBalance(int operating, CustomBalanceHistory balanceHistory);
}
