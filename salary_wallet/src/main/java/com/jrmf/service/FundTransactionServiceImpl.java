package com.jrmf.service;

import com.jrmf.domain.UserCommission;
import com.jrmf.domain.vo.FundSummaryVO;
import com.jrmf.persistence.FundTransactionDao;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("fundTransactionService")
public class FundTransactionServiceImpl implements FundTransactionService{

  @Autowired
  private FundTransactionDao fundTransactionDao;

  @Override
  public List<UserCommission> listCommissionByCustomKeys(Map<String, Object> param) {
    return fundTransactionDao.listCommissionByCustomKeys(param);
  }

  @Override
  public Map<String, Object> getSumRecharge(Map<String, Object> param) {
    return fundTransactionDao.getSumRecharge(param);
  }

  @Override
  public Map<String, Object> getSumTransaction(Map<String, Object> param) {
    return fundTransactionDao.getSumTransaction(param);
  }

  @Override
  public List<FundSummaryVO> listFundSummary(Map<String, Object> param){
    return fundTransactionDao.listFundSummary(param);
  }

  @Override
  public FundSummaryVO getFundSummaryInfo(Map<String, Object> param){
    FundSummaryVO commissionSumInfo= fundTransactionDao.getCommissionSumInfo(param);
    FundSummaryVO rechargeSumInfo=fundTransactionDao.getRechargeSumInfo(param);

    if(commissionSumInfo==null){
      return rechargeSumInfo;
    }
    if(rechargeSumInfo==null){
      return commissionSumInfo;
    }
    commissionSumInfo.setRechargeAmount(rechargeSumInfo.getRechargeAmount());
    commissionSumInfo.setRechargeTimes(rechargeSumInfo.getRechargeTimes());

    return commissionSumInfo;
  }
}
