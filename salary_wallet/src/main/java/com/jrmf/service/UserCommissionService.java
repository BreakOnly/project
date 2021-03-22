package com.jrmf.service;

import com.jrmf.domain.CommissionDetail;
import com.jrmf.domain.CommissionInvoice;
import com.jrmf.domain.Page;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.PersonalIncomeTaxRate;
import com.jrmf.domain.QbInvoiceRecord;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PaymentReturn;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

/**
 * @author zhangzehui
 * @version 创建时间：2017年12月16日
 */
@Service
public interface UserCommissionService {

    public void addUserCommission(UserCommission commission);

    UserCommission getUserCommission(String orderNo);

    public List<UserCommission> getUserCommissionByParam(Map<String, Object> param);

    public String getUserCommissionSum(Map<String, Object> param);

    public List<CommissionDetail> getCommissionDeatailList(Map<String, Object> param);

    public List<UserCommission> getUserCommissionByIds(String ids);

    public void updateUserCommissionByCompanyId(Map<String, Object> param);

    public int updateUserCommission(UserCommission commission);

    public void deleteById(int id);

    public void deleteByBatchId(String batchId);

    public void deleteTemporary();

    public String getStockByBatchId(String batchId, String companyId);

    public String getAmountByBatchId(String batchId);

    public int getBatchNum(String batchId, String status);

    public List<UserCommission> getCommissionsByBatchId(String batchId);

    public UserCommission getCommissionsById(String id);

    public Map<String, Object> updateBatchMessage(String batchId, String originalId, Map<String, Object> model);

    public void updateBatchData(String batchId, String originalId);

    public void updateBatchDataByExecuting(String batchId);

    public void addUserCommissionBatch(List<UserCommission> commissionBatch);

    public void updateUserCommissionByBacthId(String newBatchId, String batchId);

    public Map<String, Object> transfer(String originalId, String batchId, String remark, String operatorName);

    public UserCommission getCommissionsByAygId(String orderNo);

    public List<UserCommission> getListByTypeAndStatus(int status, String payType);

    List<UserCommission> getListByTypeAndStatusOnJob(int status, String payType);

    public String isValidateData(String amount, String idCard, String bankCard, String mobileNo, String name);

    public void updateUserCommissionBatchIdAndStatus(UserCommission userCommission);

    public void updateUserCommissionByInvoice(String invoiceBatchNo, String ids);

    public List<UserCommission> getUserCommissionToInvoice(Map<String, Object> paramMap);

    public List<UserCommission> getUserCommissionedByParam(Map<String, Object> paramMap);

    public Map<String, Object> inputHsBankCommissionDate(Workbook workbook, String customkey, String operatorName);

    public Map<String, Object> inputPABankCommissionDate(Workbook workbook, Map<String, String> batchaData);

    public String getServiceRatesFreeByBatchId(String batchId);

    public void updateTemporaryBatchData(String batchId, String originalId, String companyId, boolean autoSupplement, Set<String> validateSet);

    public List<UserCommission> commissionResultQuery(Map<String, Object> param);

    public List<UserCommission> getUserDealRecord(Map<String, Object> param);

    public List<UserCommission> getUserDealDetail(Map<String, Object> param);

    public List<UserCommission> commissionDetailResult(Map<String, Object> param);

    public List<UserCommission> commissionDetailResult2(Map<String, Object> param);

    public List<UserCommission> commissionByMemuResult(Map<String, Object> param);

    public List<UserCommission> commissionByMemuDetail(Map<String, Object> param);

    public List<UserCommission> getUserTradeData(Map<String, Object> param);

    List<Map> getCommissionsByParams(Map<String, String> map);

    List<UserCommission> getCommissionsDetailByParams(Map<String, String> map);

    public List<UserCommission> getUserTradeCompany(Map<String, Object> param);

    public List<UserCommission> getUserTradeDetailCompany(Map<String, Object> param);

    List<UserCommission> getApiListByTypeAndStatus(int type, String status);

    List<UserCommission> getApiListByTypeAndStatusOnJob(int type, String status);

    int updateUserCommissionById(UserCommission userCommission);

    List<HashMap<String, Object>> getCompanyCommissions(Map<String, Object> params);

    /**
     * 根据customkeys 查询该角色所能查询的所有信息
     *
     * @return UserCommission list
     */
    List<UserCommission> listCommissionByCustomKeys(Map<String, Object> param);

    /**
     * 根据批次号cha查询  状态为 3的 批次订单
     *
     * @return UserCommission list
     */
    List<UserCommission> getToBeConfirmedUserCommissionByBatchId(String batchId);

    /**
     * 查询符合条件的条数
     *
     * @param paramMap 参数
     * @return 条数
     */
    int getCommissionsCountByParams(Map<Object, Object> paramMap);

    /**
     * 修改订单
     *
     * @param paramMap 参数
     * @return 修改条数
     */
    int updateUndifiedOrder(HashMap<String, Object> paramMap);

    /**
     * 未落地的预授权交易
     *
     * @param params
     * @return
     */
    List<UserCommission> getUnClosedPrepareUnifiedCommissions(Map<Object, Object> params);

    List<UserCommission> getAutogenerateTaskList(String startTime, String endTime, String customKey, String startAmount, String endAmount, String orderNos);

    int updateIsTask(Integer id);

    /**
     * 查询联动交易明细
     *
     * @param status
     * @param type
     * @return
     */
    public List<UserCommission> getLdListByTypeAndStatus(int status, String type);

    List<UserCommission> getLdListByTypeAndStatusOnJob(int status, String type);

    int getPayingCount(String customKey, String companyId, Integer payType);

    List<UserCommission> getSuccessUserCommission(Map<String, Object> param);

    String getSuccessAmount(Map<String, Object> param);

    int updateUserCommissionRate(UserCommission commission);

    /**
     * 构造上送交易
     * @param userCommission 交易明细
     * @param paymentConfig 路由配置
     * @return 上送交易结果
     */
    PaymentReturn<String> getPaymentReturn(UserCommission userCommission, PaymentConfig paymentConfig);

    /**
     * 根据customkeys 查询该角色所能查询的所有信息总条数
     *
     * @return UserCommission list
     */
    int listCommissionByCustomKeysCount(Map<String, Object> param);

    int commissionDetailResultCount(Map<String, Object> param);

    void updateYmTemporaryBatchData(String batchId, String originalId, String companyId, boolean autoSupplement, Set<String> validateSet);

	public String summaryInfoByMerchant(Map<String, String> params);

	public String summaryInfoByAgent(Map<String, String> params);

	public List<Map<String, Object>> summaryInfoGroupMerchant(Page page);

	public List<Map<String, Object>> summaryInfoGroupAgent(Page page);

	public int summaryInfoGroupMerchantCount(Page page);

	public int summaryInfoGroupAgentCount(Page page);

	public List<Map<String, Object>> summaryInfoGroupMerchantNoPage(Page page);

	public List<Map<String, Object>> summaryInfoGroupAgentNoPage(Page page);

//	public String getTotalSummary(Page page);

    Map<String, String> getCommissionDaySumAmonut(String originalId, String companyId, String batchId, String status);

    Map<String, String> getCommissionMounthSumAmonut(String originalId, String companyId, String batchId, String status);

    Map<String, String> getCommissionQuarterSumAmonut(String originalId, String companyId, String batchId, String status);

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

  void calculateInvoiceAmountAndUpdateCommission(int firstIndex, int endIndex, String userId, String companyId,
      Map<String,List<PersonalIncomeTaxRate>> personalTaxRatesMap,
      List<UserCommission> userCommissionList, CommissionInvoice commissionInvoice,QbInvoiceRecord invoiceRecord);

    List<UserCommission> groupUserCommissionInvoiceRecord(Map<String, Object> params);

    String getTaxRate(String userTotalAmount, String companyId, Map<String, List<PersonalIncomeTaxRate>> personalTaxRatesMap);

    List<String> getCommissionInvoiceSerialNos(Map<String, Object> params);

    Map<String, String> getRealCommissionMounthSumAmonut(String originalId, String companyId, String batchId, String status);

    String getRealCommissionMounthSumAmonutByCertId(String companyId, String certId);

    List<UserCommission> getLdListByTypeAndStatusAndBusinessTypeOnJob(int status, String type,String businessType);

    Map<String, String> getCommissionMounthSumAmonutByPlatformId(String batchId);

    String getTotalUninvoicedAmountByCompanyId(Integer companyId, String customKey);

  List<UserCommission> listCommissionByCompanyId(String companyId, String receiptTime);
}
