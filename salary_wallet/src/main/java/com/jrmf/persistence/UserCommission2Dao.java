package com.jrmf.persistence;

import com.jrmf.domain.CommissionDetail;
import com.jrmf.domain.CommissionInvoice;
import com.jrmf.domain.Page;
import com.jrmf.domain.UserCommission;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangzehui
 * @time 2017-12-14
 */
@Mapper
public interface UserCommission2Dao {

    public void addUserCommission(UserCommission commission);

    UserCommission getCommByOrderNo(String orderNo);

    public List<UserCommission> getUserCommissionByParam(Map<String, Object> param);

    public List<CommissionDetail> getCommissionDeatailList(Map<String, Object> param);

    public List<UserCommission> getUserCommissionByIds(@Param("ids") String ids);

    public void updateUserCommissionByInvoice(@Param("invoiceBatchNo") String invoiceBatchNo, @Param("ids") String ids);

    public void updateUserCommissionByCompanyId(Map<String, Object> param);

    public int updateUserCommission(UserCommission commission);

    public int updateUserCommissionByOrderNo(UserCommission commission);

    public void deleteById(@Param("id") int id);

    public void deleteTemporary();

    public void updateUserCommissionByBacthId(@Param("newBatchId") String newBatchId, @Param("batchId") String batchId);

    public void deleteByBatchId(@Param("batchId") String batchId);

    public List<UserCommission> getCommissionsByBatchId(@Param("batchId") String batchId);

    public UserCommission getCommissionsByAygId(@Param("orderNo") String orderNo);

    public List<UserCommission> getListByTypeAndStatus(@Param("status") int status, @Param("payType") String payType);

    public String getStockByBatchId(@Param("batchId") String batchId, @Param("companyId") String companyId);

    public String getAmountByBatchId(@Param("batchId") String batchId);

    public String getServiceRatesFreeByBatchId(@Param("batchId") String batchId);

    public int getBatchNum(@Param("batchId") String batchId, @Param("status") String status);

    List<CommissionDetail> getCommissionDeatailList(
            @Param("companyNo") String companyNo,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("status") String status,
            @Param("start") int start,
            @Param("pageSize") int pageSize);

    public UserCommission getCommissionsById(@Param("id") String id);

    public int addUserCommissionBatch(@Param("commissionBatch") List<UserCommission> commissionBatch);

    public String getUserCommissionSum(Map<String, Object> param);

    public void updateUserCommissionBatchIdAndStatus(UserCommission userCommission);

    public List<UserCommission> getUserCommissionToInvoice(Map<String, Object> paramMap);

    public List<UserCommission> getUserCommissionedByParam(Map<String, Object> paramMap);

    public List<UserCommission> commissionResultQuery(Map<String, Object> param);

    public List<UserCommission> getUserDealRecord(Map<String, Object> param);

    public List<UserCommission> getUserDealDetail(Map<String, Object> param);

    public List<UserCommission> commissionDetailResult(Map<String, Object> param);

    public List<UserCommission> commissionByMemuResult(Map<String, Object> param);

    public List<UserCommission> commissionByMemuDetail(Map<String, Object> param);

    List<UserCommission> getSumCommissionsByParams(Map<String, String> map);

    List<UserCommission> getCommissionsDetailByParams(Map<String, String> map);

    public List<UserCommission> getUserTradeData(Map<String, Object> param);

    public List<UserCommission> getUserTradeCompany(Map<String, Object> param);

    public List<UserCommission> getUserTradeDetailCompany(Map<String, Object> param);

//	public String getSumAmountOfMonthByCertId(@Param("certId")String certId, @Param("originalId") String originalId, @Param("companyId")String companyId);
//
//	public String getSumAmountOfDayByCertId(@Param("certId")String certId, @Param("originalId") String originalId, @Param("companyId")String companyId);

    public Set<String> getCommissionsUserNameByCertId(@Param("certId") String certId);

    List<UserCommission> getApiListByTypeAndStatus(@Param("status") int status, @Param("payType") String payType);

    int updateUserCommissionById(UserCommission userCommission);

    List<HashMap<String, Object>> getCompanyCommissions(Map<String, Object> params);

    List<UserCommission> listCommissionByCustomKeys(Map<String, Object> param);

    List<UserCommission> getSuccessUserCommission(Map<String, Object> param);

    int getPayingCount(String customKey, String companyId, Integer payType);

    String getSuccessAmount(Map<String, Object> param);

    int updateUserCommissionRate(UserCommission commission);

    String getSumAmountOfDayByCertId(@Param("certId") String certId, @Param("originalId") String originalId, @Param("companyId") String companyId);

    String getSumAmountOfMonthByCertId(@Param("certId") String certId, @Param("originalId") String originalId, @Param("companyId") String companyId);

    String getSumAmountOfQuarterByCertId(@Param("certId") String certId, @Param("originalId") String originalId, @Param("companyId") String companyId);

    int listCommissionByCustomKeysCount(Map<String, Object> param);

	public String summaryInfoByMerchant(Map<String, String> params);

	public String summaryInfoByAgent(Map<String, String> params);

	public List<Map<String, Object>> summaryInfoGroupMerchant(Page page);

	public List<Map<String, Object>> summaryInfoGroupAgent(Page page);

	public int summaryInfoGroupMerchantCount(Page page);

	public int summaryInfoGroupAgentCount(Page page);

	public List<Map<String, Object>> summaryInfoGroupMerchantNoPage(Page page);

	public List<Map<String, Object>> summaryInfoGroupAgentNoPage(Page page);

//	public String getTotalSummary(Page page);

    List<Map<String, String>> getCommissionDaySumAmonut(String originalId, String companyId, String batchId, String status);

    List<Map<String, String>> getCommissionMounthSumAmonut(String originalId, String companyId, String batchId, String status);

    List<Map<String, String>> getCommissionQuarterSumAmonut(String originalId, String companyId, String batchId, String status);

	public List<Map<String, Object>> summaryInfoByMerchantMonth(Page page);

	public String summaryInfoByMonth(Page page);

	public int summaryInfoByMerchantMonthCount(Page page);

	public int summaryInfoGroupMerchantMonthCount(Page page);

	public List<Map<String, Object>> summaryInfoGroupMerchantMonth(Page page);

	public String summaryInfoByMonthDetail(Page page);

	public List<Map<String, Object>> summaryInfoGroupMerchantMonthNoPage(
			Page page);

	public List<Map<String, Object>> summaryInfoByMerchantMonthNoPage(Page page);

	public List<Map<String, Object>> summaryInfoGroupAgentMonth(Page page);

	public int summaryInfoGroupAgentMonthCount(Page page);

	public List<Map<String, Object>> summaryInfoGroupAgentMonthNoPage(Page page);

  List<Map<String, Object>> getInvoiceCustomInfos(CommissionInvoice commissionInvoice);

  List<UserCommission> getUserCommissionInvoiceRecord(Map<String, Object> params);

  void updateCommissionInvoiceInfo(Map<String, Object> updateParams);

  void updateCommissionInvoiceInfoByInvoiceSerialNo(Map<String, Object> updateParams);

  List<UserCommission> groupUserCommissionInvoiceRecord(Map<String, Object> params);

  List<String> getCommissionInvoiceSerialNos(Map<String, Object> params);

  List<Map<String, String>> getRealCommissionMounthSumAmonut(String originalId, String companyId, String batchId, String status);

  String getRealCommissionMounthSumAmonutByCertId(String companyId, String certId);

  List<Map<String, String>> getCommissionMounthSumAmonutByPlatformId(String batchId);

  String getSignleCommissionMounthSumAmonutByPlatformId(int platformId, String certId);
}
