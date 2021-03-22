package com.jrmf.service;

import static com.jrmf.utils.merchant.AccountBeanUtil.putData;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.APIResponse;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.domain.ForwardCompanyAccountHistory;
import com.jrmf.domain.ForwardCompanyAccount;
import com.jrmf.domain.dto.ForwardCompanyAccountRequestDTO;
import com.jrmf.domain.dto.ForwardCompanyAccountUpdateRequestDTO;
import com.jrmf.domain.vo.CompanyAccountVo;
import com.jrmf.persistence.ForwardCompanyAccountHistoryDao;
import com.jrmf.persistence.ForwardCompanyAccountDao;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author: YJY
 * @date: 2020/11/20 15:15
 * @description: 记账户余额
 */
@Slf4j
@Service
public class ForwardCompanyAccountServiceImpl implements ForwardCompanyAccountService {

  @Autowired
  ForwardCompanyAccountDao forwardCompanyAccountDao;
  @Autowired
  ForwardCompanyAccountHistoryDao forwardCompanyAccountHistoryDao;

  @Autowired
  TransactionalAccountService transactionalAccountService;

  /**
   * @Description 根据条件查询 记账户余额
   **/
  @Override
  public PageInfo<ForwardCompanyAccount> findByCondition(
      ForwardCompanyAccountRequestDTO requestDTO) {

    PageHelper.startPage(requestDTO.getPageNo(), requestDTO.getPageSize());
    return new PageInfo<>(forwardCompanyAccountDao.findByCondition(requestDTO));
  }

  /**
   * @Description 新增商户记账户
   **/
  @Override
  public APIResponse insert(ForwardCompanyAccount forwardCompanyAccount) {
    ForwardCompanyAccountRequestDTO check = new ForwardCompanyAccountRequestDTO();
    check.setCustomKey(forwardCompanyAccount.getCustomKey());
    check.setCompanyId(forwardCompanyAccount.getCompanyId());
    check.setRealCompanyId(forwardCompanyAccount.getRealCompanyId());
    List<ForwardCompanyAccount> checkList = forwardCompanyAccountDao.findByCondition(check);
    if (!CollectionUtils.isEmpty(checkList)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_544);
    }
    if(ObjectUtils.isEmpty(forwardCompanyAccount.getBalance()) || forwardCompanyAccount.getBalance()<=0){

      forwardCompanyAccount.setBalance(0);
    }
    forwardCompanyAccount.setStatus(1);
    boolean flag = forwardCompanyAccountDao.insert(forwardCompanyAccount) > 0;
    if(flag){
      return APIResponse.successResponse();
    }
    return APIResponse.errorResponse(ResponseCodeMapping.ERR_545);
  }

  /**
   * @Description 查询  Balance 单表
   **/
  @Override
  public List<ForwardCompanyAccount> findBalanceByCondition(
      ForwardCompanyAccount forwardCompanyAccount) {
    return forwardCompanyAccountDao.findBalanceByCondition(forwardCompanyAccount);
  }

  /**
   * @Description 更新余额和状态 历史记录表单位为 元  记账单单位为 分
   **/
  @Override
  @Transactional(rollbackFor = Exception.class)
  public APIResponse updateById(ForwardCompanyAccountUpdateRequestDTO requestDTO) {
    try {

      ForwardCompanyAccountRequestDTO search = new ForwardCompanyAccountRequestDTO();
      search.setId(requestDTO.getId());
      List<ForwardCompanyAccount> list = forwardCompanyAccountDao.findByCondition(search);
      if (CollectionUtils.isEmpty(list) || list.size() > 1) {
        return APIResponse.errorResponse(ResponseCodeMapping.ERR_532);
      }
      /**
       * @Description 如果是修改状态并且和原有状态一致 则直接返回成功
       **/
      if (requestDTO.getStatus() != 0 && list.get(0).getStatus() == requestDTO.getStatus()) {
        return APIResponse.successResponse();
      }
      /**
       * @Description 修改余额
       **/
      if (!ObjectUtils.isEmpty(requestDTO.getBalance()) && requestDTO.getBalance() != 0) {
        /**
         * @Description 需修改的金额 单位 元
         **/
        double balance = requestDTO.getBalance();
        /**
         * @Description 原有金额 保留两位小数
         **/
        BigDecimal bigDecimal = new BigDecimal(list.get(0).getBalance());
        bigDecimal = bigDecimal.divide(BigDecimal.valueOf(100));
        /**
         * @Description 封装历史记录 并存库
         **/
        requestDTO.setBalance(requestDTO.getBalance() * -100);
        ForwardCompanyAccountHistory forwardCompanyAccountHistory = putData(list.get(0));
        forwardCompanyAccountHistory.setOperator(requestDTO.getOperator());
        forwardCompanyAccountHistory.setPreTradeMoney(bigDecimal);
        forwardCompanyAccountHistory.setTradeMoney(BigDecimal.valueOf(balance));
        forwardCompanyAccountHistory.setRemark(requestDTO.getRemark());

        if (!StringUtils.isEmpty(requestDTO.getType()) && 1 == requestDTO.getType()) {
          requestDTO.setBalance(requestDTO.getBalance() * -1);
          forwardCompanyAccountHistory.setAfterTradeMoney(
              bigDecimal.add(BigDecimal.valueOf(balance)));
          forwardCompanyAccountHistory.setTradeType(TradeType.ADDBALANCE.getCode());
        }else{
          forwardCompanyAccountHistory.setAfterTradeMoney(
              bigDecimal.subtract(BigDecimal.valueOf(balance)));
        }

        boolean insert = forwardCompanyAccountHistoryDao.insert(forwardCompanyAccountHistory) > 0;
        boolean update = forwardCompanyAccountDao.updateById(requestDTO) > 0;
        /**
         * @Description 异常回滚
         **/
        if (!(insert && update)) {
          TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return APIResponse.successResponse();
      }
      boolean update = forwardCompanyAccountDao.updateById(requestDTO) > 0?true:false;

      if(update){
        return APIResponse.successResponse();
      }
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_502);
    } catch (Exception e) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      log.error("修改记账户金额错误!" + e);
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_543);
    }
  }

  /**
   * @return com.github.pagehelper.PageInfo<com.jrmf.domain.ForwardCompanyAccount>
   * @Author YJY
   * @Description 以转包公司为角度查询列表
   * @Date 2020/11/25
   * @Param [requestDTO]
   **/
  @Override
  public PageInfo<ForwardCompanyAccount> findCompanyList(
      ForwardCompanyAccountRequestDTO requestDTO) {
    PageHelper.startPage(requestDTO.getPageNo(), requestDTO.getPageSize());
    return new PageInfo<>(forwardCompanyAccountDao.findCompanyList(requestDTO));
  }


  /**
   * @return com.jrmf.common.APIResponse
   * @Author YJY
   * @Description 对外修改记账户余额接口
   * @Date 2020/11/25
   * @Param [accountVo]
   **/
  @Override
  public APIResponse updateCompanyAccount(CompanyAccountVo accountVo) {

    if (ObjectUtils.isEmpty(accountVo)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_532);
    }
    if (ObjectUtils.isEmpty(accountVo.getTradeType())) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_533);
    }
    if (ObjectUtils.isEmpty(accountVo.getBalance())) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_534);
    }
    if (ObjectUtils.isEmpty(accountVo.getRelateOrderNo())) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_535);
    }
    if (ObjectUtils.isEmpty(accountVo.getId())
        && (
           ObjectUtils.isEmpty(accountVo.getCustomKey())
        || ObjectUtils.isEmpty(accountVo.getCompanyId())
        || ObjectUtils.isEmpty(accountVo.getRealCompanyId())
          )
       ) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_536);
    }

    /**
    * @Description 查询是否有记录
    **/
    ForwardCompanyAccountRequestDTO account = new ForwardCompanyAccountRequestDTO();
    List<ForwardCompanyAccount> accountList = new ArrayList<>();
    if(!ObjectUtils.isEmpty(accountVo.getId())){
      account.setId(accountVo.getId());
      accountList =  forwardCompanyAccountDao.findByCondition(account);
    }else{
      account.setCustomKey(accountVo.getCustomKey());
      account.setCompanyId(accountVo.getCompanyId());
      account.setRealCompanyId(accountVo.getRealCompanyId());
      accountList =  forwardCompanyAccountDao.findByCondition(account);
    }

     /**
     * @Description 此记账户数据失效 不允许进行操作
     **/
    if(!CollectionUtils.isEmpty(accountList) && accountList.get(0).getStatus() != 1){
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_538);
    }
    /**
     * @Description 充值
     **/
    if (TradeType.RECHARGE.getCode() == accountVo.getTradeType()) {

      /**
      * @Description 新增记账户数据 技术讨论结果 新增不走这个接口 保留但是新增只初始化没有金额
      **/
      if(CollectionUtils.isEmpty(accountList)) {
        accountVo.setBalance("0");
        return transactionalAccountService.addAccountData(accountVo);
      }

      return transactionalAccountService.updateAccountData(accountVo,accountList.get(0));
    }

    /**
    * @Description 只有充值 有可能不存在记账户 其他操作都需要有记账户
    **/
    if(CollectionUtils.isEmpty(accountList)){
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_541);
    }
    return transactionalAccountService.updateAccountData(accountVo,accountList.get(0));
  }

}











