package com.jrmf.oldsalarywallet.dao;

import com.jrmf.domain.CommissionDetail;
import com.jrmf.domain.UserCommission;
import com.jrmf.interceptor.InterceptJobServiceAnnotation;
import com.jrmf.interceptor.InterceptPlatformPermissionAnnotation;
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
public interface UserCommissionDao {

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

    @InterceptJobServiceAnnotation
    List<UserCommission> getListByTypeAndStatusOnJob(@Param("status") int status, @Param("payType") String payType);

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

    @InterceptPlatformPermissionAnnotation(aliasName = "cc.businessPlatformId")
    public List<UserCommission> commissionDetailResult(Map<String, Object> param);

    public List<UserCommission> commissionByMemuResult(Map<String, Object> param);

    public List<UserCommission> commissionByMemuDetail(Map<String, Object> param);

    List<UserCommission> getSumCommissionsByParams(Map<String, String> map);

    List<UserCommission> getCommissionsDetailByParams(Map<String, String> map);

    public List<UserCommission> getUserTradeData(Map<String, Object> param);

    public List<UserCommission> getUserTradeCompany(Map<String, Object> param);

    public List<UserCommission> getUserTradeDetailCompany(Map<String, Object> param);

    public String getSumAmountOfMonthByCertId(@Param("certId") String certId, @Param("originalId") String originalId, @Param("companyId") String companyId);

    public Set<String> getCommissionsUserNameByCertId(@Param("certId") String certId);

    List<UserCommission> getApiListByTypeAndStatus(@Param("status") int status, @Param("payType") String payType);

    @InterceptJobServiceAnnotation
    List<UserCommission> getApiListByTypeAndStatusOnJob(@Param("status") int status, @Param("payType") String payType);

    int updateUserCommissionById(UserCommission userCommission);

    List<HashMap<String, Object>> getCompanyCommissions(Map<String, Object> params);

    /**
     * 根据批次号cha查询  状态为 3的 批次订单
     *
     * @param batchId 批次号
     * @return UserCommission list
     */
    List<UserCommission> getToBeConfirmedUserCommissionByBatchId(@Param("batchId") String batchId);

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

    public List<UserCommission> getLdListByTypeAndStatus(@Param("status") int status, @Param("payType") String payType);

    @InterceptJobServiceAnnotation
    List<UserCommission> getLdListByTypeAndStatusOnJob(@Param("status") int status, @Param("payType") String payType);

    List<UserCommission> getAutogenerateTaskList(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("customKey") String customKey, @Param("startAmount") String startAmount, @Param("endAmount") String endAmount, @Param("orderNos") String orderNos);

    int updateIsTask(@Param("id") Integer id);

    int commissionDetailResultCount(Map<String, Object> param);

    @InterceptJobServiceAnnotation
    List<UserCommission> getLdListByTypeAndStatusAndBusinessTypeOnJob(@Param("status") int status,
        @Param("payType") String payType, @Param("businessType") String businessType);

  String getTotalUninvoicedAmountByCompanyId(Integer companyId, String customKey);

  List<UserCommission> listCommissionByCompanyId(String companyId, String receiptTime);
}
