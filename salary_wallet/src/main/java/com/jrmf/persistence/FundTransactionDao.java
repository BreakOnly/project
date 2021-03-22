package com.jrmf.persistence;

import com.jrmf.domain.UserCommission;
import java.util.List;
import java.util.Map;

import com.jrmf.domain.vo.FundSummaryVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FundTransactionDao {

  List<UserCommission> listCommissionByCustomKeys(Map<String, Object> param);

  Map<String, Object> getSumRecharge(Map<String, Object> param);

  Map<String, Object> getSumTransaction(Map<String, Object> param);

  List<FundSummaryVO> listFundSummary(Map<String, Object> param);

  FundSummaryVO getRechargeSumInfo(Map<String, Object> param);

  FundSummaryVO getCommissionSumInfo(Map<String, Object> param);

}
