package com.jrmf.service;

import com.jrmf.domain.UserNameCertIdCheck;
import com.jrmf.domain.UserNameCertIdCheckBaseConfig;
import com.jrmf.domain.UserNameCertIdWhiteBlackConfig;

import java.util.List;

/**
 * @author 种路路
 * @create 2020-01-07 21:00
 * @desc 二要素验证接口
 **/
public interface UserNameCertIdCheckService {
    /**
     * 添加二要素验证记录
     * @param userNameCertIdCheck
     */
    void addUserNameCertIdCheck(UserNameCertIdCheck userNameCertIdCheck);

    /**
     * 二要素基础配置信息
     * @return 基础配置信息集合
     */
    List<UserNameCertIdCheckBaseConfig> getUserNameCertIdCheckBaseConfigList();
    /**
     * 二要素基础配置信息
     * @return 基础配置信息集合
     */
    UserNameCertIdCheckBaseConfig getUserNameCertIdCheckBaseConfigByCheckType(int checkType);

    /**
     * 根据商户key查询二要素校验黑白名单表
     * @param customkey 商户key
     * @param type  1.黑名单   2.白名单
     * @return UserNameCertIdWhiteBlackConfig
     */
    UserNameCertIdWhiteBlackConfig getUserNameCertIdWhiteAndBlackConfigByCustomkeyAndType(String customkey, int type);

    /**
     * 根据服务公司id或者代理商id 查询二要素基础配置信息
     * @param customkey 服务公司id或者代理商id
     * @return 基础配置信息集合
     */
    UserNameCertIdCheckBaseConfig getUserNameCertIdCheckBaseConfigByNotAllCheckCustomkey(String customkey);
}
