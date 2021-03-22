package com.jrmf.service;

import com.jrmf.domain.CustomBalanceHistory;
import com.jrmf.persistence.CustomBalanceHistoryDao;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("CustomBalanceHistoryService")
public class CustomBalanceHistoryServiceImpl implements CustomBalanceHistoryService {

  private static Logger logger = LoggerFactory.getLogger(CustomBalanceHistoryServiceImpl.class);

  @Autowired
  private CustomBalanceHistoryDao customBalanceHistoryDao;

  @Override
  public int insert(CustomBalanceHistory record) {
    return customBalanceHistoryDao.insert(record);
  }

  @Override
  public List<CustomBalanceHistory> queryCustomBalanceHistory(CustomBalanceHistory record) {
    return customBalanceHistoryDao.queryCustomBalanceHistory(record);
  }



  @Override
  public List<CustomBalanceHistory> selectByParamMap(Map<String, Object> paramMap) {
    return customBalanceHistoryDao.selectByParamMap(paramMap);
  }
}
