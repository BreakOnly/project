package com.jrmf.service;

import com.jrmf.domain.CommissionUser;
import com.jrmf.domain.SubcontractRouter;
import com.jrmf.domain.dto.SubcontractRouterQueryDTO;
import com.jrmf.persistence.SubcontractRouterDao;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.ChannelInterimBatch;
import com.jrmf.domain.CommissionTemporary;
import com.jrmf.persistence.ChannelInterimBatch2Dao;
import com.jrmf.persistence.CommissionTemporary2Dao;

/**
 * @author zhangzehui
 * @version 创建时间：2018年10月16日
 */
@Service("channelInterimBatchService2")
public class ChannelInterimBatchService2Impl implements ChannelInterimBatchService2 {

  @Autowired
  private ChannelInterimBatch2Dao batchDao2;

  @Autowired
  private CommissionTemporary2Dao temporaryDao2;

  @Autowired
  SubcontractRouterDao subcontractRouterDao;


  @Override
  public int addChannelInterimBatch(ChannelInterimBatch batch) {
    return batchDao2.addChannelInterimBatch(batch);
  }

  @Override
  public ChannelInterimBatch getChannelInterimBatchById(String id, String originalId) {
    return batchDao2.getChannelInterimBatchById(id, originalId);
  }

  @Override
  public ChannelInterimBatch getChannelInterimBatchByOrderno(String orderno, String originalId) {
    return batchDao2.getChannelInterimBatchByOrderno(orderno, originalId);
  }

  @Override
  public List<ChannelInterimBatch> getChannelInterimBatchByOrdernos(String ordernos,
      String originalId) {
    return batchDao2.getChannelInterimBatchByOrdernos(ordernos, originalId);
  }

  @Override
  public List<ChannelInterimBatch> getChannelInterimBatchByParam(
      Map<String, Object> param) {
    return batchDao2.getChannelInterimBatchByParam(param);
  }

  @Override
  public void updateChannelInterimBatch(ChannelInterimBatch batch) {
    batchDao2.updateChannelInterimBatch(batch);
  }

  @Override
  public void deleteByIds(String ids, String customkey) {
    batchDao2.deleteByIds(ids, customkey);
  }

  @Override
  public void deleteByOrderno(String orderno) {
    batchDao2.deleteByOrderno(orderno);
  }

  @Override
  public int addCommissionTemporary(List<CommissionTemporary> commission) {
    return temporaryDao2.addCommissionTemporary(commission);
  }

  @Override
  public void updateCommissionTemporary(CommissionTemporary history) {
    temporaryDao2.updateCommissionTemporary(history);
  }

  @Override
  public void deleteById(String ids, String originalId) {
    temporaryDao2.deleteById(ids, originalId);
  }

  @Override
  public void deleteByBatchId(String batchIds, String originalId) {
    temporaryDao2.deleteByBatchId(batchIds, originalId);
  }

  @Override
  public List<CommissionTemporary> getCommissionsByBatchId(String batchId, String originalId) {
    return temporaryDao2.getCommissionsByBatchId(batchId, originalId);
  }

  @Override
  public List<CommissionTemporary> getCommissionedByParam(
      Map<String, Object> paramMap) {
    return temporaryDao2.getCommissionedByParam(paramMap);
  }

  @Override
  public List<CommissionTemporary> getCommissionedByBatchIdsAndParam(
      Map<String, Object> paramMap) {
    return temporaryDao2.getCommissionedByBatchIdsAndParam(paramMap);
  }

  @Override
  public String getStockByBatchId(String batchId, String companyId) {
    return temporaryDao2.getStockByBatchId(batchId, companyId);
  }

  @Override
  public void updateInterimBatchStatus(String batchId) {
    batchDao2.updateInterimBatchStatus(batchId);
  }

  @Override
  public void submitReview(String batchId) {
    batchDao2.submitReview(batchId);
  }

  @Override
  public List<ChannelInterimBatch> queryReviewedBatch(Map<String, Object> param) {
    return batchDao2.queryReviewedBatch(param);
  }

  @Override
  public void batchReview(Map<String, Object> param) {
    batchDao2.batchReview(param);
  }

  @Override
  public int updateCommToNotCheck(Integer[] ids) {
    return temporaryDao2.updateCommToNotCheck(ids);
  }

  @Override
  public List<CommissionTemporary> getCommissionByIds(Integer[] array) {
    return temporaryDao2.getCommissionByIds(array);
  }

  @Override
  public List<CommissionTemporary> getSuccessCommissionList(String batchId) {
    return temporaryDao2.getSuccessCommissionList(batchId);
  }

  @Override
  public int updateCommissionTemporarys(List<CommissionTemporary> commission) {
    return temporaryDao2.updateCommissionTemporarys(commission);
  }

  @Override
  public List<CommissionTemporary> getCommissionUserInfo(String batchId) {
    return temporaryDao2.getCommissionUserInfo(batchId);
  }

  @Override
  public String selectRealCompanyIdByBatchId(String batchId) {
    return temporaryDao2.selectRealCompanyIdByBatchId(batchId);
  }


  @Override
  public Map<String, Object> getBatchInfo(Map<String, Object> param) {
    return batchDao2.getBatchInfo(param);
  }

  @Override
  public List<SubcontractRouter> listSubcontractRouter(
      SubcontractRouterQueryDTO subcontractRouterQueryDTO) {
    return subcontractRouterDao.listSubcontractRouter(subcontractRouterQueryDTO);
  }

  @Override
  public List<String> listPayTypesOfCompanyDefaultPayChannel(Integer companyId) {
    return subcontractRouterDao.listPayTypesOfCompanyDefaultPayChannel(companyId);
  }

  @Override
  public List<CommissionUser> getCommissionUserByBatchId(String batchId) {
    return temporaryDao2.getCommissionUserByBatchId(batchId);
  }
}
