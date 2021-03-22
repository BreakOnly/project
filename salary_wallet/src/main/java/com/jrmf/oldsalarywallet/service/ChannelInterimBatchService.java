package com.jrmf.oldsalarywallet.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jrmf.domain.ChannelInterimBatch;
import com.jrmf.domain.CommissionTemporary;

/** 
* @author zhangzehui
* @version 创建时间：2018年10月16日
*/
@Service
public interface ChannelInterimBatchService {
	
	public int addChannelInterimBatch(ChannelInterimBatch batch);
	
	public ChannelInterimBatch getChannelInterimBatchById(String id, String originalId);
	
	public ChannelInterimBatch getChannelInterimBatchByOrderno(String orderno, String originalId);
	
	public List<ChannelInterimBatch> getChannelInterimBatchByParam(Map<String, Object> param);
	
	public void updateChannelInterimBatch(ChannelInterimBatch batch);
	
	public void deleteByIds(String ids, String customkey);
	
	public void deleteByOrderno(String orderno);
	
	public void updateChannelHistorySummary(ChannelInterimBatch batch);
	
	public int addCommissionTemporary(List<CommissionTemporary> commission);

	public void updateCommissionTemporary(CommissionTemporary history);
	
	public void deleteById(String ids, String originalId);
	
	public void deleteByBatchId(String batchId, String originalId);
	
	public void updateInterimBatchStatus(String batchId);
	
	public List<CommissionTemporary> getCommissionsByBatchId(String batchId, String originalId);
	
	public List<CommissionTemporary> getCommissionedByParam(Map<String, Object> paramMap);
	
	public String getStockByBatchId(String batchId, String companyId);

	public void submitReview(String batchId);

	public List<ChannelInterimBatch> queryReviewedBatch(Map<String, Object> param);

	public void batchReview(Map<String, Object> param);

    int updateCommToNotCheck(Integer[] ids);

    List<CommissionTemporary> getCommissionByIds(Integer[] commIds);

    /**
     * 根据临时明细检验下发最小限额
     * @param commissionTemporary  临时明细
     * @return commissionTemporary 修改后的临时明细
     */
    CommissionTemporary checkCommissionTemporary(CommissionTemporary commissionTemporary);

	public void updateYmInfoByBatch(ChannelInterimBatch interimBatch);

	public ChannelInterimBatch getChannelInterimBatchBySuccess(
			String originalBeachNo, String originalId);
}
 