package com.jrmf.oldsalarywallet.service;

import com.jrmf.controller.constant.CommissionStatus;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelInterimBatch;
import com.jrmf.domain.CommissionTemporary;
import com.jrmf.oldsalarywallet.dao.ChannelInterimBatchDao;
import com.jrmf.oldsalarywallet.dao.CommissionTemporaryDao;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** 
* @author zhangzehui
* @version 创建时间：2018年10月16日
* 
*/
@Service("channelInterimBatchService")
public class ChannelInterimBatchServiceImpl implements ChannelInterimBatchService {
	
	@Autowired
	private ChannelInterimBatchDao batchDao;
	
	@Autowired
	private CommissionTemporaryDao temporaryDao;

	@Autowired
	private ChannelCustomDao channelCustomDao;

	@Override
	public int addChannelInterimBatch(ChannelInterimBatch batch) {
		return batchDao.addChannelInterimBatch(batch);
	}
	@Override
	public ChannelInterimBatch getChannelInterimBatchById(String id,String originalId) {
		return batchDao.getChannelInterimBatchById(id,originalId);
	}
	@Override
	public ChannelInterimBatch getChannelInterimBatchByOrderno(String orderno,String originalId) {
		return batchDao.getChannelInterimBatchByOrderno(orderno,originalId);
	}
	@Override
	public List<ChannelInterimBatch> getChannelInterimBatchByParam(
			Map<String, Object> param) {
		return batchDao.getChannelInterimBatchByParam(param);
	}
	@Override
	public void updateChannelInterimBatch(ChannelInterimBatch batch) {
		batchDao.updateChannelInterimBatch(batch);
	}
	@Override
	public void deleteByIds(String ids,String customkey) {
		batchDao.deleteByIds(ids,customkey);
	}
	@Override
	public void deleteByOrderno(String orderno) {
		batchDao.deleteByOrderno(orderno);
	}
	@Override
	public void updateChannelHistorySummary(ChannelInterimBatch batch) {
		batchDao.updateChannelHistorySummary(batch);
	}
	@Override
	public int addCommissionTemporary(List<CommissionTemporary> commission) {
		return temporaryDao.addCommissionTemporary(commission);
	}
	@Override
	public void updateCommissionTemporary(CommissionTemporary history) {
		temporaryDao.updateCommissionTemporary(history);
	}
	@Override
	public void deleteById(String ids,String originalId) {
		temporaryDao.deleteById(ids, originalId);
	}
	@Override
	public void deleteByBatchId(String batchIds,String originalId) {
		temporaryDao.deleteByBatchId(batchIds, originalId);
	}
	@Override
	public List<CommissionTemporary> getCommissionsByBatchId(String batchId,String originalId) {
		return temporaryDao.getCommissionsByBatchId(batchId,originalId);
	}
	@Override
	public List<CommissionTemporary> getCommissionedByParam(
			Map<String, Object> paramMap) {
		return temporaryDao.getCommissionedByParam(paramMap);
	}
	@Override
	public String getStockByBatchId(String batchId, String companyId) {
		return temporaryDao.getStockByBatchId(batchId, companyId);
	}
	@Override
	public void updateInterimBatchStatus(String batchId) {
		batchDao.updateInterimBatchStatus(batchId);
	}
	@Override
	public void submitReview(String batchId) {
		batchDao.submitReview(batchId);
	}
	@Override
	public List<ChannelInterimBatch> queryReviewedBatch(Map<String, Object> param) {
		return batchDao.queryReviewedBatch(param);
	}
	@Override
	public void batchReview(Map<String, Object> param) {
		batchDao.batchReview(param);
	}

	@Override
	public int updateCommToNotCheck(Integer[] ids) {
		return temporaryDao.updateCommToNotCheck( ids);
	}

	@Override
	public List<CommissionTemporary> getCommissionByIds(Integer[] array) {
		return temporaryDao.getCommissionByIds(array);
	}

    /**
     * 根据临时明细检验下发最小限额
     * @param commissionTemporary  临时明细
     * @return commissionTemporary 修改后的临时明细
     */
	@Override
    public CommissionTemporary checkCommissionTemporary(CommissionTemporary commissionTemporary){
        ChannelCustom custom = channelCustomDao.getCustomByCustomkey(commissionTemporary.getOriginalId(),null);
        String minTransferAmount = custom.getMinTransferAmount();
        if(StringUtil.isNumber(minTransferAmount)){
            int compareTod = ArithmeticUtil.compareTod(commissionTemporary.getAmount(), minTransferAmount);
            if(compareTod < 0 ){
                commissionTemporary.setStatus(CommissionStatus.FAILURE.getCode());
                commissionTemporary.setStatusDesc("最小下发金额是："+minTransferAmount+"元");
            }
        }
        return commissionTemporary;
    }
	@Override
	public void updateYmInfoByBatch(ChannelInterimBatch interimBatch) {
		batchDao.updateYmInfoByBatch(interimBatch);
	}
	@Override
	public ChannelInterimBatch getChannelInterimBatchBySuccess(
			String originalBeachNo, String originalId) {
		return batchDao.getChannelInterimBatchBySuccess(originalBeachNo,originalId);
	}
}
 