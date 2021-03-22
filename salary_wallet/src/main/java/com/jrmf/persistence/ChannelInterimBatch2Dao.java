package com.jrmf.persistence;

import com.jrmf.domain.ChannelInterimBatch;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 
* @author zhangzehui
* @time 2017-12-14 
*  
*/
@Mapper
public interface ChannelInterimBatch2Dao {

	public int addChannelInterimBatch(ChannelInterimBatch batch);
	
	public ChannelInterimBatch getChannelInterimBatchById(@Param("id")String id,@Param("originalId")String originalId);
	
	public ChannelInterimBatch getChannelInterimBatchByOrderno(@Param("orderno")String orderno,@Param("originalId")String originalId);

	public List<ChannelInterimBatch>  getChannelInterimBatchByOrdernos(@Param("ordernos")String ordernos,@Param("originalId")String originalId);

	public List<ChannelInterimBatch> getChannelInterimBatchByParam(Map<String,Object> param);
	
	public void updateChannelInterimBatch(ChannelInterimBatch batch);
	
	public void deleteByIds(@Param("ids")String ids, @Param("customkey") String customkey);
	
	public void deleteByOrderno(@Param("orderno")String orderno);
	
	public void updateInterimBatchStatus(@Param("orderno")String orderno);

	public void submitReview(String batchId);

	public List<ChannelInterimBatch> queryReviewedBatch(Map<String, Object> param);

	public void batchReview(Map<String, Object> param);

	void batchLock(String batchNo);

	void batchUnLock(@Param("batchNo") String batchNo, @Param("status") int status);

	void updateBatchLockState(@Param("batchNo") String batchNo, @Param("status") int status);

  Map<String, Object> getBatchInfo(Map<String, Object> param);
}
 