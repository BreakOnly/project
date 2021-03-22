package com.jrmf.service;

import com.jrmf.domain.vo.CustomBalanceAndAccount;

/**
 * @author: YJY
 * @date: 2020/12/8 11:15
 * @description:
 */
public interface CustomBalanceAndAccountService {

  /**
   * @Author YJY
   * @Description 扣减余额  1  3
   * @Date  2020/12/8
   * @return void
   **/
  void updateCustomBalanceAndAccount(CustomBalanceAndAccount customBalanceAndAccount)
      throws Exception;
}
