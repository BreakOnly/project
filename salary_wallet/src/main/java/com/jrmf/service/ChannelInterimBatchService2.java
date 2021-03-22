package com.jrmf.service;

import com.jrmf.domain.CommissionUser;
import com.jrmf.domain.SubcontractRouter;
import com.jrmf.domain.dto.SubcontractRouterQueryDTO;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import com.jrmf.domain.ChannelInterimBatch;
import com.jrmf.domain.CommissionTemporary;

/**
 * @author zhangzehui
 * @version 创建时间：2018年10月16日
 */
@Service
public interface ChannelInterimBatchService2 {

  public int addChannelInterimBatch(ChannelInterimBatch batch);

  public ChannelInterimBatch getChannelInterimBatchById(String id, String originalId);

  public ChannelInterimBatch getChannelInterimBatchByOrderno(String orderno, String originalId);

  public List<ChannelInterimBatch> getChannelInterimBatchByOrdernos(String ordernos,
      String originalId);

  public List<ChannelInterimBatch> getChannelInterimBatchByParam(Map<String, Object> param);

  public void updateChannelInterimBatch(ChannelInterimBatch batch);

  public void deleteByIds(String ids, String customkey);

  public void deleteByOrderno(String orderno);

  public int addCommissionTemporary(List<CommissionTemporary> commission);

  public void updateCommissionTemporary(CommissionTemporary history);

  public void deleteById(String ids, String originalId);

  public void deleteByBatchId(String batchId, String originalId);

  public void updateInterimBatchStatus(String batchId);

  public List<CommissionTemporary> getCommissionsByBatchId(String batchId, String originalId);

  public List<CommissionTemporary> getCommissionedByParam(Map<String, Object> paramMap);

  public List<CommissionTemporary> getCommissionedByBatchIdsAndParam(Map<String, Object> paramMap);

  public String getStockByBatchId(String batchId, String companyId);

  public void submitReview(String batchId);

  public List<ChannelInterimBatch> queryReviewedBatch(Map<String, Object> param);

  public void batchReview(Map<String, Object> param);

  int updateCommToNotCheck(Integer[] ids);

  List<CommissionTemporary> getCommissionByIds(Integer[] commIds);

  List<CommissionTemporary> getSuccessCommissionList(String batchId);

  int updateCommissionTemporarys(@Param("commissionBatch") List<CommissionTemporary> commission);

  List<CommissionTemporary> getCommissionUserInfo(String batchId);

  String selectRealCompanyIdByBatchId(String batchId);

  Map<String, Object> getBatchInfo(Map<String, Object> param);

  List<SubcontractRouter> listSubcontractRouter(
      SubcontractRouterQueryDTO subcontractRouterQueryDTO);

  List<String> listPayTypesOfCompanyDefaultPayChannel(Integer companyId);

  List<CommissionUser> getCommissionUserByBatchId(String batchId);
}
