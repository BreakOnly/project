package com.jrmf.service;

import static com.jrmf.utils.RespCode.UPDATE_BALANCE_EXCEPTION;

import com.jrmf.common.APIResponse;
import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.domain.Company;
import com.jrmf.domain.CustomBalanceHistory;
import com.jrmf.domain.vo.CompanyAccountVo;
import com.jrmf.domain.vo.CustomBalanceAndAccount;
import com.jrmf.payment.service.ConfirmGrantService2Impl;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.exception.BalanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: YJY
 * @date: 2020/12/8 11:16
 * @description:
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CustomBalanceAndAccountServiceImpl implements CustomBalanceAndAccountService {

  private static Logger logger = LoggerFactory.getLogger(CustomBalanceAndAccountServiceImpl.class);
  @Autowired
  CustomBalanceService customBalanceService;
  @Autowired
  ForwardCompanyAccountService forwardCompanyAccountService;
  @Autowired
  CompanyService companyService;

  @Override
  public void updateCustomBalanceAndAccount(CustomBalanceAndAccount customBalanceAndAccount)
      throws Exception {

    try {

    //扣减商户余额
    customBalanceService.updateCustomBalance(CommonString.DEDUCTION,
        new CustomBalanceHistory(customBalanceAndAccount.getOriginalId(), customBalanceAndAccount.getCompanyId(),
            customBalanceAndAccount.getPayType(), customBalanceAndAccount.getHandleAmount(), customBalanceAndAccount.getPassNum(),
            customBalanceAndAccount.getTradeType(), customBalanceAndAccount.getOrderNo(), customBalanceAndAccount.getOperator()));
    }catch (Exception e){
      throw new Exception(UPDATE_BALANCE_EXCEPTION);
    }
    /**
     * @Description 类型
     **/
    Company checkCompany = companyService.getCompanyByUserId(Integer.parseInt(customBalanceAndAccount.getCompanyId()));
    if (checkCompany.getCompanyType() == 1 && !customBalanceAndAccount.getCompanyId().equals(customBalanceAndAccount.getRealCompanyId())) {
      /**
       * @Description 扣减记账户余额
       **/
      CompanyAccountVo accountVo = new CompanyAccountVo();
      accountVo.setBalance(customBalanceAndAccount.getAmount());
      accountVo.setCustomKey(customBalanceAndAccount.getOriginalId());
      accountVo.setTradeType(customBalanceAndAccount.getTradeType());
      accountVo.setRelateOrderNo(customBalanceAndAccount.getOrderNo());
      accountVo.setCompanyId(customBalanceAndAccount.getCompanyId());
      accountVo.setRealCompanyId(customBalanceAndAccount.getRealCompanyId());
      accountVo.setAmount(customBalanceAndAccount.getPassNum());
      accountVo.setOperating(CommonString.DEDUCTION);
      accountVo.setOperator(customBalanceAndAccount.getOperator());
      APIResponse response = forwardCompanyAccountService.updateCompanyAccount(accountVo);
      if (response.getState() != 1) {
        throw new Exception(response.getRespmsg());
      }
    }
  }


}
