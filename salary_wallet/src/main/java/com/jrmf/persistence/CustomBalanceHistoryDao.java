package com.jrmf.persistence;

import com.jrmf.domain.CustomBalanceHistory;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomBalanceHistoryDao {

  int insert(CustomBalanceHistory record);

  List<CustomBalanceHistory> queryCustomBalanceHistory(CustomBalanceHistory record);

  List<CustomBalanceHistory> selectByParamMap(Map<String, Object> paramMap);


}