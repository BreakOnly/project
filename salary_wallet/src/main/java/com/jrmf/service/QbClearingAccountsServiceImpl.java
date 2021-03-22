package com.jrmf.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbClearingAccounts;
import com.jrmf.persistence.QbClearingAccountsDao;
import com.jrmf.utils.ArithmeticUtil;

@Service
public class QbClearingAccountsServiceImpl implements QbClearingAccountsService{

	@Autowired
	private QbClearingAccountsDao qbClearingAccountsDao;

	@Override
	public int insert(QbClearingAccounts record) {
		return qbClearingAccountsDao.insert(record);
	}

	@Override
	public List<Map<String, String>> groupClearTerm(String month) {
		return qbClearingAccountsDao.groupClearTerm(month);
	}

	@Override
	public List<Map<String, Object>> getClearAccountsByPage(Page page) {
		return qbClearingAccountsDao.getClearAccountsByPage(page);
	}

	@Override
	public int getClearAccountsCount(Page page) {
		return qbClearingAccountsDao.getClearAccountsCount(page);
	}

	@Override
	public List<QbClearingAccounts> getClearAccountsNoPage(Page page) {
		return qbClearingAccountsDao.getClearAccountsNoPage(page);
	}

	@Override
	public Map<String, String> getSumAmountByTerm(Map<String, String> clearTerm) {
		return qbClearingAccountsDao.getSumAmountByTerm(clearTerm);
	}

	@Override
	public int deleteClearAccountAllByMonth(Map<String, String> params) {
		return qbClearingAccountsDao.deleteClearAccountAllByMonth(params);
	}

	@Override
	public int deleteClearAccountAllByCompany(Map<String, String> params) {
		return qbClearingAccountsDao.deleteClearAccountAllByCompany(params);
	}

	@Override
	public int deleteClearAccountAllByAgent(Map<String, String> params) {
		return qbClearingAccountsDao.deleteClearAccountAllByAgent(params);
	}

	@Override
	public int deleteClearAccountAllByMerchant(Map<String, String> params) {
		return qbClearingAccountsDao.deleteClearAccountAllByMerchant(params);
	}

	@Override
	public List<Map<String, String>> groupClearTermByCompany(Map<String, String> params) {
		return qbClearingAccountsDao.groupClearTermByCompany(params);
	}

	@Override
	public List<Map<String, String>> groupClearTermByAgent(Map<String, String> params) {
		return qbClearingAccountsDao.groupClearTermByAgent(params);
	}

	@Override
	public List<Map<String, String>> groupClearTermByMerchat(Map<String, String> params) {
		return qbClearingAccountsDao.groupClearTermByMerchat(params);
	}

	@Override
	public int selectClearAccountAllByMonthCount(Map<String, String> params) {
		return qbClearingAccountsDao.selectClearAccountAllByMonthCount(params);
	}

	@Override
	public int selectClearAccountAllByCompanyCount(Map<String, String> params) {
		return qbClearingAccountsDao.selectClearAccountAllByCompanyCount(params);
	}

	@Override
	public int selectClearAccountAllByAgentCount(Map<String, String> params) {
		return qbClearingAccountsDao.selectClearAccountAllByAgentCount(params);
	}

	@Override
	public int selectClearAccountAllByMerchantCount(Map<String, String> params) {
		return qbClearingAccountsDao.selectClearAccountAllByMerchantCount(params);
	}

	@Override
	public Map<String, String> getSumAmountByUpdateTime(Map<String, String> merchantTranMap) {
		return qbClearingAccountsDao.getSumAmountByUpdateTime(merchantTranMap);
	}

	@Override
	public String getSumAmountByAgentUpdateTime(Map<String, String> merchantTranMap) {
		return qbClearingAccountsDao.getSumAmountByAgentUpdateTime(merchantTranMap);
	}

	@Override
	public List<Map<String, String>> getMerRateUpdate(Map<String, String> merchantTranMap) {
		return qbClearingAccountsDao.getMerRateUpdate(merchantTranMap);
	}

	@Override
	public List<Map<String, String>> getAgentRateUpdate(Map<String, String> merchantTranMap) {
		return qbClearingAccountsDao.getAgentRateUpdate(merchantTranMap);
	}

	/**
	 * 计算差额
	 * @param merchantTranMap
	 * @param merchantRate
	 * @param merRateUpdateList
	 * @return
	 */
	@Override
	public Map<String, String> differSummary(Map<String, String> merchantTranMap, String oldRate,List<Map<String, String>> merRateUpdateList,int belongType) {
		String differAmount = "0";
		String updateMerRateDesc = "";
		String differSmallOneMerAmountDesc = "";
		Map<String, String> differMap = new HashMap<String, String>();
		for (Map<String, String> rateUpdate : merRateUpdateList) {
			merchantTranMap.put("modifyEffectStartTime", rateUpdate.get("modifyEffectStartTime"));
			merchantTranMap.put("modifyEffectEndTime", rateUpdate.get("modifyEffectEndTime"));
			//获取变更费率在当月指定生效时间内的下发金额
			Map<String, String> totalAmountUpdate = qbClearingAccountsDao.getSumAmountByUpdateTime(merchantTranMap);
			//变更后的费率
			String updateRate = rateUpdate.get("modifyRate");
			if(totalAmountUpdate!=null && ArithmeticUtil.compareTod(String.valueOf(totalAmountUpdate.get("totalAmount")), "0")==1){
				String differAmountDetail = "";
				//费率差（负数则是降低，正数则为增加）
				String differRate = ArithmeticUtil.subStr2(updateRate, oldRate);	
				if(belongType==1){
					//差额费用
					String totalAmount = String.valueOf(totalAmountUpdate.get("totalAmount"));
					differAmountDetail = ArithmeticUtil.mulStr(totalAmount, differRate, 2);
				}else{
					//差额费用
					differAmountDetail = ArithmeticUtil.mulStr(String.valueOf(totalAmountUpdate.get("totalAmount")),differRate ,2);
				}
				differAmount = ArithmeticUtil.addStr(differAmount, differAmountDetail);
				differSmallOneMerAmountDesc = differSmallOneMerAmountDesc + differAmountDetail+";";
			}else{
				differSmallOneMerAmountDesc = differSmallOneMerAmountDesc +"0;";
			}
			String dateScopeDesc = rateUpdate.get("modifyEffectStartTime")+"至"+rateUpdate.get("modifyEffectEndTime");
			updateMerRateDesc =  updateMerRateDesc+ArithmeticUtil.mulStr(updateRate,"100")+"%("+dateScopeDesc+");";
		}
		differMap.put("differAmount", differAmount);
		differMap.put("updateRateDesc", updateMerRateDesc);
		differMap.put("differAmountDesc", differSmallOneMerAmountDesc);
		return differMap;
	}

	@Override
	public Map<String, String> getSumAmountByTermNew(
			Map<String, String> clearTerm) {
		return qbClearingAccountsDao.getSumAmountByTermNew(clearTerm);
	}

	@Override
	public List<Map<String, String>> groupClearTermMonth(Map<String, String> params) {
		return qbClearingAccountsDao.groupClearTermMonth(params);
	}

	@Override
	public List<Map<String, String>> groupClearTermMonthNew(
			Map<String, String> params) {
		return qbClearingAccountsDao.groupClearTermMonthNew(params);
	}

}
