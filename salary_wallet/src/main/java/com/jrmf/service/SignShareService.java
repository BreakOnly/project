package com.jrmf.service;

import com.jrmf.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Title: SignShareService
 * @Description: 共享签约配置
 * @create 2020/4/27 10:09
 */
@Service
public interface SignShareService {

    List<SignShare> getSignShareByParam(Map<String, Object> param);

    Map<String, Object> configSignShare(SignShare signShare);

    Map<String, Object> deleteSignShare(String id);

    List<SignShare> getSignShareLimitByLimitGroupId(String limitGroupId);

    Map<String, Object> updateSignShareLimitStatus(String id, String status);

    Map<String, Object> deleteSignShareLimit(String id);

    Map<String, Object> insertSignShareLimitNameInfo(SignShare signShare);

    List<SignElementRule> getSignElementRule(Map<String, Object> param);

    Map<String, Object> configSignElementRule(SignElementRule signElementRule);

    Map<String, Object> deleteSignElementRule(String id);

    SignShare getSignShareByLimitName(String limitName, int id);

    SignShare getSignShareByLimitName(Integer limitName, int id);

    /**
     * 获取商户配置的所有共享签约规则
     */
    List<SignShare> getSignShareByCustomKey(String customKey, String companyId);

    /**
     * 根据共享签约规则发起用户签约
     */
    UsersAgreement shareSignAgreementByUser(List<SignShare> list, SignElementRule signElementRule, AgreementTemplate template, int signSubmitType, String userName,String certId);

    List<SignShare> getSignShareByCustomKey2(String customkey, int id);

    SignElementRule getSignElementRuleByCompanyId(String companyId);

    int checkUsersAgreement(List<SignShare> signShareList, SignElementRule signElementRule, int signSubmitType, String customKey, String companyId, String certId, String userName);


    /**
     * 获取需要共享签约的签约数量
     *
     * @param originalId
     * @return
     */
    int getNeedShareAgreementNum(String originalId);

    /**
     * 后台触发签约共享
     *
     * @param originalId
     * @param shareType
     */
    void triggerShareAgreement(String originalId,int shareType);

    /**
     * 根据用户查询商户列表
     *
     * @param userid
     * @return
     */
    List<ChannelCustom>  getMerhcantListByUserId(int userid);

    /**
     * 根据共享类型查询共享签约商户
     * @param type
     * @return
     */
    List<SignShare> getSignShareByType(String type);
}
