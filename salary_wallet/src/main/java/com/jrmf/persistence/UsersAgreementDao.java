package com.jrmf.persistence;

import com.jrmf.domain.UsersAgreement;

import com.jrmf.interceptor.InterceptJobServiceAnnotation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2018-11-13 15:30
 * @desc
 **/
@Mapper
public interface UsersAgreementDao {

    /**
     * 根据条件查询用户所以签约的记录
     *
     * @param map
     * @return 签约记录list
     */
    List<UsersAgreement> getUsersAgreementsByParams(Map<String, Object> map);
    List<UsersAgreement> selectUsersAgreementsByParams(Map<String, Object> map);
    /**
     * 根据条件查询用户所以签约的记录
     *
     * @param map
     * @return 签约记录list
     */
    List<UsersAgreement> getUsersAgreementsByParamsForCopy(Map<String, Object> map);

    void updateUsersAgreement(UsersAgreement usersAgreement);

    void updateUsersAgreementImageURL(Map<String, Object> hashMap);

    void createAgreement(UsersAgreement agreement);

    void deleteUsersAgreement(UsersAgreement usersAgreement);

    List<Map<String, Object>> getAgreementsByParams(Map<String, Object> params);

    List<Map<String, Object>> getAgreementsForPlatform(Map<String, Object> params);

    int getUserAgreementCountByParam(Map<String, Object> params);

    void updateUsersAgreementSignStep(UsersAgreement usersAgreement);

    void updateUsersAgreementDocumentStep(UsersAgreement usersAgreement);

    List<Map<String, Object>> getUserAgreementsForPayCompanyByParam(Map<String, Object> map);

    List<Map<String, Object>> getUserAgreementsByParam(Map<String, Object> map);

    int updateUsersAgreementByBatch(String ids);

    /**
     * 修改api签约的  信息提交
     * 提交方式
     * 回调地址
     * 回调次数
     * 流水号
     *
     * @param agreement 协议
     */
    void updateApiSignDetail(UsersAgreement agreement);

    /**
     * 更改白名单
     *
     * @param agreement
     */
    void updateWhiteList(UsersAgreement agreement);

    int getWhiteListCount(@Param("customKey") String customKey, @Param("companyId") String companyId, @Param("certId") String certId);

    /**
     * 查询符合条件的id
     * @param params
     * @return id
     */
    List<String> getUserAgreementsIdForPayCompanyByParam(Map<String, Object> params);

    /**
     * 查询符合条件的 结果条数
     * @param hashMap 查询条件
     * @return 条数
     */
    int getUsersAgreementsCountByParams(Map<String, Object> hashMap);

    int getAgreementsByParamsCount(Map<String, Object> params);

    int getUserAgreementsIdForPayCompanyByParamCount(Map<String, Object> params);

    @InterceptJobServiceAnnotation
	List<UsersAgreement> getUsersAgreementsByChannelType(Map<String, Object> params);

	void updateUsersAgreementSignStepNew(UsersAgreement usersAgreement);

    UsersAgreement getCompanySignShare(String companyId,Integer userId);

    UsersAgreement getCustomSignShare(String customKeys, int userId, String signLevel, int papersRequire);

    UsersAgreement getUsersAgreement(int agreementTemplateId, int userId);

    int countByTemplateId(@Param("agreementTemplateId") String agreementTemplateId);

    UsersAgreement getAgreementsSignSuccess(String customkey, String recCustomkey, int userId);

    /**
     * @Author YJY
     * @Description  查询未签约用户的数据
     * @Date  2020/10/28
     * @Param  customKey:商户key needPhone:是否筛选有无手机号
     * @return java.util.List<com.jrmf.domain.UsersAgreement>
     **/
    List<UsersAgreement> findSendSmsUsers(@Param("customKey")String customKey,@Param("needPhone")String needPhone,@Param("userIds")List userIds);


    /**
     * @Author YJY
     * @Description 通过ID查询用户
     * @Date  2020/11/2
     * @Param [list]
     * @return java.util.List<com.jrmf.domain.UsersAgreement>
     **/
    List<UsersAgreement> findByIds(List list);

    /**
    * @Description 查询下发成功的数量
    **/
    int findCommissionCount(Map<String, Object> params);

    /**
    * @Description 查询统计所需数据
    **/
    List<UsersAgreement> getAgreementStatistical(Map<String, Object> params);

    Map<String,Object> getlinkageSignProcessingCount(String batchId, String customKey, String companyId);
}
