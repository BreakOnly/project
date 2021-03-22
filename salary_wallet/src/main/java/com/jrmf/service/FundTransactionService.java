package com.jrmf.service;

import com.jrmf.domain.UserCommission;
import java.util.List;
import java.util.Map;

import com.jrmf.domain.vo.FundSummaryVO;
import org.springframework.stereotype.Service;

@Service
public interface FundTransactionService {

  List<UserCommission> listCommissionByCustomKeys(Map<String, Object> param);

  Map<String, Object> getSumRecharge(Map<String, Object> param);

  Map<String, Object> getSumTransaction(Map<String, Object> param);

  /**
   * 按商户和服务公司统计
   * @param param
   * @return
   */
  List<FundSummaryVO> listFundSummary(Map<String, Object> param);

  /**
   * 获取汇总信息
   *
   * @param param
   * @return
   */
  FundSummaryVO getFundSummaryInfo(Map<String, Object> param);

}
