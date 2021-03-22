package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserRelated;

/**
 * @author zhangzehui
 * @version 创建时间：2018年4月25日
 * 类说明
 */
@Mapper
public interface UserRelatedDao {

    void createUserRelated(UserRelated userRelated);

    void updateUserRelated(UserRelated userRelated);

    List<UserRelated> getRelatedByParam(Map<String, Object> param);

    UserRelated getRelatedByUserId(@Param("userId") String userId, @Param("originalId") String originalId);

    UserRelated getRelatedByUserNo(@Param("userNo") String userNo, @Param("originalId") String originalId);

    void deleteByOriginalId(@Param("userId") String userId, @Param("originalId") String originalId);

    void deleteRelatedByBatchId(@Param("batcheId") String batcheId, @Param("originalId") String originalId);

    void updateUserRelatedStatus(@Param("userId") String userId, @Param("originalId") String originalId,
                                 @Param("status") int status);

    UserRelated getRelatedById(@Param("id") String id);

    PaymentConfig getPaymentConfigByTypeOriginalId(@Param("paymentType") String paymentType, @Param("originalId") String originalId, @Param("companyId") String companyId);

    PaymentConfig getPaymentConfigByTypeCompanyId(@Param("paymentType") String paymentType, @Param("companyId") String companyId);

    void updateUserRelatedUserNo(UserRelated userRelated);

    UserRelated selectIsWhiteList(@Param("customKey") String customKey, @Param("companyId") String companyId, @Param("certId") String certId);

    PaymentConfig getPaymentConfigTwo(@Param("paymentType") String paymentType, @Param("originalId") String originalId, @Param("companyId") String companyId, @Param("pathNo") String pathNo);

    PaymentConfig getPaymentConfigCompanyTwo(@Param("paymentType") String paymentType, @Param("companyId") String companyId, @Param("pathNo") String pathNo);

    List<PaymentConfig> getSubAccountPaymentConfig();

    int getSubAccountList(@Param("companyId") String companyId);

    /**
     * 查先签约后支付的用户数量
     *
     * @param originalId
     * @return
     */
    int countByOriginalId(@Param("originalId") String originalId);


    /**
     * 获取所有先签约后支付用户
     *
     * @param originalId
     * @return
     */
    List<Integer> getByOriginalId(@Param("originalId") String originalId);

    /**
     * 查商户id列表
     *
     * @param userId
     * @return
     */
    List<String> getMerchantListByUserId(@Param("userId") int userId);
}
 