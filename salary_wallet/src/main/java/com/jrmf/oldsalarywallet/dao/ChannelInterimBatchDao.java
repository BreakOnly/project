package com.jrmf.oldsalarywallet.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.ChannelInterimBatch;

import org.springframework.stereotype.Repository;

/** 
* @author zhangzehui
* @time 2017-12-14 
*  
*/
@Mapper
public interface ChannelInterimBatchDao {

	public int addChannelInterimBatch(ChannelInterimBatch batch);
	
	public ChannelInterimBatch getChannelInterimBatchById(@Param("id") String id, @Param("originalId") String originalId);
	
	public ChannelInterimBatch getChannelInterimBatchByOrderno(@Param("orderno") String orderno, @Param("originalId") String originalId);
	
	public List<ChannelInterimBatch> getChannelInterimBatchByParam(Map<String, Object> param);
	
	public void updateChannelInterimBatch(ChannelInterimBatch batch);
	
	public void deleteByIds(@Param("ids") String ids, @Param("customkey") String customkey);
	
	public void deleteByOrderno(@Param("orderno") String orderno);
	
	public void updateInterimBatchStatus(@Param("orderno") String orderno);
	
	public void updateChannelHistorySummary(ChannelInterimBatch batch);

	public void submitReview(String batchId);

	public List<ChannelInterimBatch> queryReviewedBatch(Map<String, Object> param);

	public void batchReview(Map<String, Object> param);

	public void updateYmInfoByBatch(ChannelInterimBatch interimBatch);

	public ChannelInterimBatch getChannelInterimBatchBySuccess(@Param("orderno") String orderno, @Param("originalId") String originalId);

}
 