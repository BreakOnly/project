package com.jrmf.persistence;

import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.ChannelHistoryPic;
import com.jrmf.domain.MerchantTransaction;
import com.jrmf.domain.Page;

import com.jrmf.interceptor.InterceptJobServiceAnnotation;
import com.jrmf.interceptor.InterceptPlatformPermissionAnnotation;
import com.jrmf.taxsettlement.api.service.recharge.RechargeRecordListServiceAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author zhangzehui
* @time 2017-12-14
*
*/
@Mapper
public interface ChannelHistoryDao {

	public int addChannelHistory(ChannelHistory history);

	public ChannelHistory getChannelHistoryById(@Param("id")String id);

	public ChannelHistory getChannelHistoryByOrderno(@Param("orderno")String orderno);

	public List<ChannelHistory> getChannelHistoryByParam(Map<String,Object> param);

	@InterceptJobServiceAnnotation
	List<ChannelHistory> getChannelHistoryByParamOnJob(Map<String,Object> param);

	public List<ChannelHistory> getChannelHistoryBySubmit(Map<String,Object> param);

	public List<ChannelHistory> getChannelHistoryByCompany(Map<String,Object> param);

	public List<ChannelHistory> getChannelHistoryList(Map<String,Object> param);

	public int updateChannelHistory(ChannelHistory history);

	public void deleteById(int id);

	public void deleteByOrderno(@Param("orderno")String orderno);

	public String getRechangeSum(@Param("customkey")String customkey,@Param("payType")String payType);

	public String getCommissionSum(@Param("customkey")String customkey,@Param("payType")String payType);

	public String getCommissionSerFreeSum(@Param("customkey")String customkey,@Param("payType")String payType);

	public int updateChannelHistorySummary(ChannelHistory history);

	public List<Map<String,Object>> batchResultQuery(Map<String, Object> param);

	@InterceptPlatformPermissionAnnotation(aliasName = "r.businessPlatformId")
	public List<Map<String,Object>> batchResultQueryByCompany(Map<String, Object> param);

    List<Map<String,Object>> geCustomChargeDetail(Map<String, Object> paramMap);

    List<ChannelHistory> getHistoryList(Map<String,Object> param);

	List<MerchantTransaction> selectTransactionList(Map<String,Object> param);

	List<MerchantTransaction> selectTransactionListByProxy(Map<String,Object> param);

	public int querybillingListCount(Page page);

	public List<Map<String, Object>> billingList(Page page);

	public String getTotalAmountByOrderNo(String orderNo);

	public List<Map<String, Object>> checkCommonCompanyAndCustom(String orderNo);

	public List<Map<String, Object>> queryBillingListNoPage(Page page);

	@InterceptJobServiceAnnotation
	List<ChannelHistory> getAutoConfirmList();

	int getToBeConfirmedCount(String customKey, String companyId, Integer payType);

	ChannelHistory getRechargeInfoById(@Param("id")String id);

	/**
	 * 获取与代理商绑定关系的商户信息
	 * @param paramMap
	 * @return
	 */
    List<Map<String, Object>> getProxyCustomCompanyDetail(Map<String, Object> paramMap);

	@InterceptJobServiceAnnotation
	List<ChannelHistory> getWarningRechargeList(Integer minute);

	void updateSendStatus(int id);

	public List<Map<String, String>> queryUserAgreementMatch(Page page);

	public List<Map<String, String>> noAgreementCount(Map<String, Object> agreementMatch);

	public int agreementOtherCompanyCount(Map<String, String> params);

	public int queryUserAgreementMatchCount(Page page);

	public List<Map<String, String>> queryUserAgreementMatchNoPage(Page page);

	public String agreementOtherCompanyNames(Map<String, String> noAgreement);

	public List<Map<String, String>> payUsers(Map<String, Object> params);

	int updateByInvoice(ChannelHistory history);

    int selectTransactionListCount(Map<String, Object> model);

	int selectTransactionListByProxyCount(Map<String, Object> model);

    int batchResultQueryByCompanyCount(Map<String, Object> param);

	ChannelHistory getByOriginalBeachNo(String batchId);

	int updateRechargeStatus(ChannelHistory history);

	String geCustomChargeAmount(Map<String, Object> paramMap);

	List<Map<String, Object>> getPicListByOrderNo(String orderNo);

	void insertChannelHistoryPic(ChannelHistoryPic channelHistoryPic);

	void deleteRechargeFileById(String id);

	ChannelHistoryPic getChannelHistoryPicById(String id);

	void updateChannelHistoryFileNumAddByOrderNo(String orderNo);

	void updateChannelHistoryFileNumMinusByOrderNo(String id);

  List<RechargeRecordListServiceAttachment> apiGetChannelHistoryList(Map<String, Object> paramMap);

	String getTotalUninvoicedAmountByCompanyId(Integer companyId, String customKey);

  int selectCountByLetterStatus(int id);

  int updateLetterStatusById(@Param("url")String url,@Param("id")int id);
}
