package com.jrmf.persistence;

import com.jrmf.domain.CommissionUser;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.CommissionGroup;
import com.jrmf.domain.CommissionTemporary;

/**
 * @author zhangzehui
 * @time 2018-10-11
 */
@Mapper
public interface CommissionTemporary2Dao {

  public int addCommissionTemporary(@Param("commissionBatch") List<CommissionTemporary> commission);

  public void updateCommissionTemporary(CommissionTemporary history);

  public void deleteById(@Param("ids") String ids, @Param("originalId") String originalId);

  public void deleteByBatchId(@Param("batchIds") String batchIds,
      @Param("originalId") String originalId);

  public List<CommissionTemporary> getCommissionsByBatchId(@Param("batchId") String batchId,
      @Param("originalId") String originalId);

  public List<CommissionTemporary> getCommissionedByParam(Map<String, Object> paramMap);

  public List<CommissionTemporary> getCommissionedByBatchIdsAndParam(Map<String, Object> paramMap);

  public String getStockByBatchId(@Param("batchId") String batchId,
      @Param("companyId") String companyId);

  public void updateStatusByBatchId(@Param("batchId") String batchId);

  int updateCommToNotCheck(Integer[] array);

  public String getSumAmountOfBatchByCertId(@Param("certId") String certId,
      @Param("batchId") String batchId);

  List<CommissionTemporary> getCommissionByIds(Integer[] array);

  List<CommissionGroup> getCommissionGroupByCertId(@Param("batchId") String batchId,
      @Param("status") String status, @Param("certId") String certId);

  List<CommissionTemporary> getSuccessCommissionList(String batchId);

  int updateCommissionTemporarys(@Param("commissionBatch") List<CommissionTemporary> commission);

  List<CommissionTemporary> getCommissionUserInfo(String batchId);

  String selectRealCompanyIdByBatchId(String batchId);

  List<CommissionUser> getCommissionUserByBatchId(@Param("batchId") String batchId);
}
