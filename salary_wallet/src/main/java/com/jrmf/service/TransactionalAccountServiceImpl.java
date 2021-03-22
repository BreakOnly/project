package com.jrmf.service;

import static com.jrmf.utils.merchant.AccountBeanUtil.putData;

import com.jrmf.common.APIResponse;
import com.jrmf.common.CommonString;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.domain.CustomBalanceHistory;
import com.jrmf.domain.ForwardCompanyAccount;
import com.jrmf.domain.ForwardCompanyAccountHistory;
import com.jrmf.domain.dto.ForwardCompanyAccountRequestDTO;
import com.jrmf.domain.vo.CompanyAccountVo;
import com.jrmf.persistence.ForwardCompanyAccountDao;
import com.jrmf.persistence.ForwardCompanyAccountHistoryDao;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.RespCode;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

/**
 * @author: YJY
 * @date: 2020/11/25 14:15
 * @description:
 */
@Slf4j
@Service
public class TransactionalAccountServiceImpl implements TransactionalAccountService {


  @Autowired
  ForwardCompanyAccountDao forwardCompanyAccountDao;
  @Autowired
  ForwardCompanyAccountHistoryDao forwardCompanyAccountHistoryDao;

  @Autowired
  CustomBalanceService customBalanceService;

  /**
   * @Description 新增记账户 目前只有充值有新增  rollbackFor为了屏蔽idea提示 实际手动回滚
   **/
  @Override
  @Transactional(rollbackFor = Exception.class)
  public APIResponse addAccountData(CompanyAccountVo accountVo) {

    try {

      ForwardCompanyAccount forwardCompanyAccount = new ForwardCompanyAccount();
      forwardCompanyAccount.setCustomKey(accountVo.getCustomKey());
      forwardCompanyAccount.setCompanyId(accountVo.getCompanyId());
      forwardCompanyAccount.setRealCompanyId(accountVo.getRealCompanyId());
      forwardCompanyAccount.setStatus(1);
      BigDecimal balance = new BigDecimal(accountVo.getBalance());
      forwardCompanyAccount.setBalance(balance.intValue());
      boolean accountInsert =
          forwardCompanyAccountDao.insert(forwardCompanyAccount) > 0 ? true : false;

      if (accountInsert) {

        return APIResponse.successResponse(ResponseCodeMapping.SUCCESS);
      }
      /**
       * @Description 操作失败需要回滚
       **/
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

    } catch (Exception e) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      log.error("新增记账户异常" + e);
    }
    return APIResponse.errorResponse(ResponseCodeMapping.ERR_540);

  }

  /**
   * @Description 修改记账户
   **/
  @Override
  @Transactional(rollbackFor = Exception.class)
  public APIResponse updateAccountData(CompanyAccountVo accountVo,
      ForwardCompanyAccount forwardCompanyAccount) {

    try {

      /**
       * @Description 重新查询数据并加锁
       **/
      ForwardCompanyAccount lock = forwardCompanyAccountDao
          .findByUpdate(forwardCompanyAccount.getId());
      /**
       * @Description 以最后一次的金额为准进行更新
       **/
      forwardCompanyAccount.setBalance(lock.getBalance());
      ForwardCompanyAccount update = new ForwardCompanyAccount();
      /**
       * @Description 交易金额 单位 分
       **/
      String pointBalance = ArithmeticUtil
          .getScale(ArithmeticUtil.mulStr(accountVo.getBalance(), "100"), 0);

      /**
       * @Description 修改金额
       **/
      if (CommonString.ADDITION == accountVo.getOperating()) {
        update.setBalance(Integer.parseInt(pointBalance));
      }
      if (CommonString.DEDUCTION == accountVo.getOperating()) {
        update.setBalance(Integer.parseInt(pointBalance) * -1);
      }
      update.setId(forwardCompanyAccount.getId());
      boolean accountUpdate =
          forwardCompanyAccountDao.updateAccountBalance(update) > 0 ? true : false;
      /**
       * @Description 插入历史记录
       **/
      if (accountUpdate) {
        /**
         * @Description 原有金额 元
         **/
        String lockBalance = lock.getBalance()+"";
        BigDecimal bigDecimal = new BigDecimal(lockBalance);
        bigDecimal = bigDecimal.divide(new BigDecimal(100), 2,BigDecimal.ROUND_HALF_UP);
        /**
         * @Description 交易金额 元
         **/
        BigDecimal tradeMoney = ArithmeticUtil.getBigDecimalScale(accountVo.getBalance(), 2);

        /**
         * @Description 交易后金额
         **/
        BigDecimal afterBigDecimal = new BigDecimal("0");

        if (CommonString.DEDUCTION == accountVo.getOperating()) {
          afterBigDecimal = bigDecimal.subtract(tradeMoney);
        }
        if (CommonString.ADDITION == accountVo.getOperating()) {

          afterBigDecimal = bigDecimal.add(tradeMoney);
        }
        /**
         * @Description 添加历史记录
         **/
        ForwardCompanyAccountHistory history = putData(forwardCompanyAccount);
        history.setOperator(accountVo.getOperator());
        history.setRelateOrderNo(accountVo.getRelateOrderNo());
        history.setTradeType(accountVo.getTradeType());
        history.setTradeMoney(tradeMoney);
        history.setPreTradeMoney(bigDecimal);
        history.setAfterTradeMoney(afterBigDecimal);
        history.setAmount(accountVo.getAmount());

        boolean historyInsert = forwardCompanyAccountHistoryDao.insert(history) > 0 ? true : false;
        /**
         * @Description 操作成功
         **/
        if (historyInsert) {
          return APIResponse.errorResponse(ResponseCodeMapping.SUCCESS);
        }

        /**
         * @Description 操作失败需要回滚
         **/
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

      }
    } catch (DataIntegrityViolationException d) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      log.error("账户余额不足" + d);
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_543);
    } catch (Exception e) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      log.error("修改记账户异常" + e);
    }
    return APIResponse.errorResponse(ResponseCodeMapping.ERR_540);

  }

  @Override
  public void updateBalance(int operating, CustomBalanceHistory balanceHistory) {

//    try {
//      customBalanceService.updateCustomBalance(CommonString.DEDUCTION,
//          new CustomBalanceHistory(related.getOriginalId(), related.getCompanyId(),
//              interimBatch.getPayType(), handleAmount, interimBatch.getPassNum(),
//              TradeType.WEBPAYMENT.getCode()));
//    } catch (Exception e) {
//      logger.error(e.getMessage(), e);
//      respstat = RespCode.error115;
//      model.put(RespCode.RESP_STAT, respstat);
//      model.put(RespCode.RESP_MSG, "扣费失败，余额不足");
//      return model;
//    }

  }


}
