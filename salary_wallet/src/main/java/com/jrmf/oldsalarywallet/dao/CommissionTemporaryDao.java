package com.jrmf.oldsalarywallet.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.CommissionGroup;
import com.jrmf.domain.CommissionTemporary;

/** 
* @author zhangzehui
* @time 2018-10-11 
*  
*/
@Mapper
public interface CommissionTemporaryDao {

	public int addCommissionTemporary(@Param("commissionBatch") List<CommissionTemporary> commission);

	public void updateCommissionTemporary(CommissionTemporary history);
	
	public void deleteById(@Param("ids") String ids, @Param("originalId") String originalId);
	
	public void deleteByBatchId(@Param("batchIds") String batchIds, @Param("originalId") String originalId);
	
	public List<CommissionTemporary> getCommissionsByBatchId(@Param("batchId") String batchId, @Param("originalId") String originalId);
	
	public List<CommissionTemporary> getCommissionedByParam(Map<String, Object> paramMap);
	
	public String getStockByBatchId(@Param("batchId") String batchId, @Param("companyId") String companyId);
	
	public void updateStatusByBatchId(@Param("batchId") String batchId);

    int updateCommToNotCheck(Integer[] array);

	List<CommissionTemporary> getCommissionByIds(Integer[] array);
	
	List<CommissionGroup> getCommissionGroupByCertId(@Param("batchId") String batchId, @Param("status") String status, @Param("certId") String certId);
	
}
 