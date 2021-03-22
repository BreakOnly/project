package com.jrmf.service;

import com.jrmf.controller.constant.SignSubmitType;
import com.jrmf.domain.AgreementTemplate;
import com.jrmf.domain.CallBackInfo;
import com.jrmf.domain.UsersAgreement;
import com.jrmf.domain.dto.UsersAgreementDTO;
import com.jrmf.taxsettlement.api.MerchantAPIDockingConfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2018-11-13 15:26
 * @desc
 **/
public interface UsersAgreementService {
    /**
     * 根据条件查询用户所以签约的记录
     * @param map
     * @return 签约记录list
     */
    List<UsersAgreement> getUsersAgreementsByParams(Map<String, Object> map);

    List<UsersAgreement> selectUsersAgreementsByParams(Map<String, Object> map);

    /**
     * 根据条件修改用户签约记录
     * @param usersAgreement
     */
    void updateUsersAgreement(UsersAgreement usersAgreement);

    /**
     * 根据userId修改ImageURL
     * @param hashMap
     */
    void updateUsersAgreementImageURL(Map<String, Object> hashMap);

    void createOrUpdateAgreement(UsersAgreement agreement);

    /**
     * 删除用户协议
     * @param usersAgreement
     */
    void deleteUsersAgreement(UsersAgreement usersAgreement);

    HashMap getAgreementsByParamsAndStatistical(Map<String,Object> params);

    List<Map<String, Object>> getAgreementsByParams(Map<String,Object> params);

    void updateUsersAgreementSignStep(UsersAgreement usersAgreement);

    void updateUsersAgreementDocumentStep(UsersAgreement usersAgreement);

    List<Map<String, Object>> getUserAgreementsForPayCompanyByParam(Map<String,Object> map);

    List<Map<String, Object>> getAgreementsForPlatform(Map<String, Object> params);

    /**
     * 添加签约记录，如果存在就跳过
     * @param agreementTemplate
     * @param userId
     * @param originalId
     * @param userName
     * @param certId
     * @param documentType
     * @param remark
     */
    void addUserAgreement(AgreementTemplate agreementTemplate, int userId, String originalId, String userName, String certId, int documentType, String remark, SignSubmitType signSubmitType);

    UsersAgreement generateUsersAgreement(AgreementTemplate agreementTemplate, int userId, String originalId, String userName, String certId, int documentType, String remark);

    /**
     * 批量审核通过
     * @param ids
     */
    int updateUsersAgreementByBatch(String ids);

    /**
     * 根据类型导出 协议
     * 类型查看 com.jrmf.controller.constant.AgreementExportType
     * @param params 参数
     * @return 协议导出成功或者失败
     */
    Map<String,Object> exportAgreementByType(Map<String, Object> params);

    void singleSign(List<UsersAgreement> usersAgreements,  byte[] frontFileBytes, byte[] backFileBytes, String mobileNo, int signSubmitType, String channelSerialno, boolean uploadIdCard, String bankCardNo) throws IOException;

    void signAgreement(AgreementTemplate agreementTemplate,UsersAgreement usersAgreement, int signSubmitType, String channelSerialno, String mobileNo, boolean thirdSign,boolean isCheckedByPhoto, String bankCardNo);

    void uploadPic(AgreementTemplate agreementTemplate,UsersAgreement usersAgreement, String frontUrl, String backUrl, Boolean thirdSign, File backPic, File frontPic);

    void eContractSingleSubmit(AgreementTemplate agreementTemplate,String notifyUrl, String extrOrderId, UsersAgreement usersAgreement, String bankCardNo);

    void identityUpload(AgreementTemplate agreementTemplate,UsersAgreement usersAgreement, String notifyUrl, File backFile, File frontFile);

    void checkSignStatus(UsersAgreement usersAgreement);

    /**
     * 签约后的  通知处理
     *
     * @param status   状态  1，成功   0，失败
     * @param serialNo 序列号
     */
    void afterSignProcess(int status, String serialNo);

    /**
     * 回调通知
     * @param originalId 商户id
     * @param callBackInfo 回调信息
     * @param signStatus 签约状态
     * @param signStatusDesc 描述
     */
    void notifyProcess(String originalId, CallBackInfo callBackInfo, String signStatus, String signStatusDesc);

    Map<String, Object> getOutData(String signStatus, String signStatusDesc, MerchantAPIDockingConfig dockingConfig, String signType, Map<String, Object> outData);

    /**
     * 修改api签约的  信息提交
     * 提交方式
     * 回调地址
     * 回调次数
     * 流水号
     * @param agreement 协议
     */
    void updateApiSignDetail(UsersAgreement agreement);

    int getWhiteListCount(String customKey,String companyId,String certId);

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

    /**
     * 签约复制
     * @param fromTemplateId 原下发模版
     * @param toTemplateId 新下发模版
     * @return 协议模版列表
     */
    void copyAgreementsByTemplateId(String fromTemplateId, String toTemplateId);

    int getAgreementsByParamsCount(Map<String, Object> params);

    int getUserAgreementsIdForPayCompanyByParamCount(Map<String, Object> params);

    void copyAgreement(UsersAgreement usersAgreement, AgreementTemplate agreementTemplate) throws Exception;

	List<UsersAgreement> getUsersAgreementsByChannelType(Map<String, Object> params);

    /**
     *      * 二要素对外暴露接口
     *      * @param userName 姓名
     *      * @param certId 身份证号
     *      * @param companyId 下发公司
     *      * @param originalId 商户id
     *      * @return
     */
    Map<String,Object> checkUserNameAndCertId(String userName, String certId, String companyId, String originalId);

    Map<String,String> checkTwoElements(String userName, String certId);

    UsersAgreement getCompanySignShare(String companyId,Integer userId);

    UsersAgreement getCustomSignShare(String customKeys, int userId, String signLevel, int papersRequire);

    UsersAgreement getUsersAgreement(int agreementTemplateId, int userId);

    UsersAgreement getAgreementsSignSuccess(String customkey, String recCustomkey, int id);

    void singleSignTwo(UsersAgreement usersAgreement, UsersAgreementDTO usersAgreementDTO) throws IOException;

    Map<String,Object> getlinkageSignProcessingCount(String batchId, String customKey, String companyId);
}
