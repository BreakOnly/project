package com.jrmf.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.domain.ForwardCompanyAccountHistory;
import com.jrmf.domain.dto.ForwardCompanyAccountHistoryRequestDTO;
import com.jrmf.persistence.ForwardCompanyAccountHistoryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: YJY
 * @date: 2020/11/20 11:00
 * @description: 商户记账户历史记录
 */
@Service
public class ForwardCompanyAccountHistoryServiceImpl implements
    ForwardCompanyAccountHistoryService {


  @Autowired
  ForwardCompanyAccountHistoryDao forwardCompanyAccountHistoryDao;

  @Override
  public PageInfo<ForwardCompanyAccountHistory> findByCondition(
      ForwardCompanyAccountHistoryRequestDTO merchantTradeRecord) {

    PageHelper.startPage(merchantTradeRecord.getPageNo(),merchantTradeRecord.getPageSize());
    return new PageInfo<>(forwardCompanyAccountHistoryDao.findByCondition(merchantTradeRecord));
  }

  @Override
  public int insert(ForwardCompanyAccountHistory forwardCompanyAccountHistory) {
    return forwardCompanyAccountHistoryDao.insert(forwardCompanyAccountHistory);
  }
}
