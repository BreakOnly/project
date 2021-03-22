package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbClearingAccounts;
@Mapper
public interface QbClearingAccountsDao {
	
    int insert(QbClearingAccounts record);

	List<Map<String, String>> groupClearTerm();

	List<Map<String, Object>> getClearAccountsByPage(Page page);

	int getClearAccountsCount(Page page);

	List<QbClearingAccounts> getClearAccountsNoPage(Page page);

	Map<String, String> getSumAmountByTerm(Map<String, String> clearTerm);

	List<Map<String, String>> groupClearTerm(String month);

	int deleteClearAccountAllByMonth(Map<String, String> params);

	int deleteClearAccountAllByCompany(Map<String, String> params);

	int deleteClearAccountAllByAgent(Map<String, String> params);

	int deleteClearAccountAllByMerchant(Map<String, String> params);

	List<Map<String, String>> groupClearTermByCompany(Map<String, String> params);

	List<Map<String, String>> groupClearTermByAgent(Map<String, String> params);

	List<Map<String, String>> groupClearTermByMerchat(Map<String, String> params);

	int selectClearAccountAllByMonthCount(Map<String, String> params);

	int selectClearAccountAllByCompanyCount(Map<String, String> params);

	int selectClearAccountAllByAgentCount(Map<String, String> params);

	int selectClearAccountAllByMerchantCount(Map<String, String> params);

	Map<String, String> getSumAmountByUpdateTime(Map<String, String> merchantTranMap);

	String getSumAmountByAgentUpdateTime(Map<String, String> merchantTranMap);

	List<Map<String, String>> getMerRateUpdate(Map<String, String> merchantTranMap);

	List<Map<String, String>> getAgentRateUpdate(Map<String, String> merchantTranMap);

	Map<String, String> getSumAmountByTermNew(Map<String, String> clearTerm);

	List<Map<String, String>> groupClearTermMonth(Map<String, String> params);

	List<Map<String, String>> groupClearTermMonthNew(Map<String, String> params);

}