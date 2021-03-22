package com.jrmf.service;

import com.jrmf.domain.CustomBalanceHistory;
import java.util.List;
import java.util.Map;


public interface CustomBalanceHistoryService {

  int insert(CustomBalanceHistory record);

  List<CustomBalanceHistory> selectByParamMap(Map<String, Object> paramMap);

  List<CustomBalanceHistory> queryCustomBalanceHistory(CustomBalanceHistory record);

}
