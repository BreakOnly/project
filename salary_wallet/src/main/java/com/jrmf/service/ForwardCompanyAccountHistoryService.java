package com.jrmf.service;

import com.github.pagehelper.PageInfo;
import com.jrmf.domain.ForwardCompanyAccountHistory;
import com.jrmf.domain.dto.ForwardCompanyAccountHistoryRequestDTO;

/**
 * @author: YJY
 * @date: 2020/11/20 11:00
 * @description: 商户记账户历史记录
 */
public interface ForwardCompanyAccountHistoryService {


  PageInfo<ForwardCompanyAccountHistory> findByCondition(
      ForwardCompanyAccountHistoryRequestDTO merchantTradeRecord);

  int insert(ForwardCompanyAccountHistory forwardCompanyAccountHistory);
}
