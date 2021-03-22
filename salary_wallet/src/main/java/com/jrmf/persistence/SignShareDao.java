package com.jrmf.persistence;

import com.jrmf.domain.SignElementRule;
import com.jrmf.domain.SignShare;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @Title: SignShareMapper
 * @Description:
 * @create 2020/4/27 10:24
 */
@Mapper
public interface SignShareDao {

    List<SignShare> getSignShareByParam(Map<String, Object> param);

    void insertSignShare(SignShare signShare);

    void updateSignShare(SignShare signShare);

    void deleteSignShare(String id);

    List<SignShare> getSignShareLimitByLimitGroupId(String limitGroupId);

    SignShare getSignShareByLimitName(String limitName, int id);

    void insertSignShareLimitInfo(SignShare signShare);

    void updateSignShareLimitStatus(String id, String status);

    void deleteSignShareLimit(String id);

    SignShare getSignShareLimitByShareCustomkey(String shareCustomkey, String limitGroupId);

    List<SignElementRule> getSignElementRule(Map<String, Object> param);

    void deleteSignElementRule(String id);

    void insertSignElementRule(SignElementRule signElementRule);

    SignShare getSignShareById(String id);

    void deleteSignShareLimitByGroupId(String groupId);

    SignShare getSignShareByCustomKey(Integer type, String customKey);

    void updateSignElementRule(SignElementRule signElementRule);

    List<SignShare> getSignShareByCustomKey2(String customkey, int id);

    SignElementRule getSignElementRuleByCompanyId(String companyId);

    String getSignShareLimitCustomKeysByLimitGroupId(String limitGroupId);

    SignShare getSignShareLimitByLimitName(String limitName);

    SignElementRule getSignElementRuleByCompanyIdAndId(int companyId, int id);

    void updateSignShareLimitByGroupId(SignShare signShare);

    void updateSignShareLimit(SignShare s1);

    List<SignShare> getSignShareByType(String type);
}
