package com.jrmf.service;

import com.jrmf.domain.UserNameCertIdCheck;
import com.jrmf.domain.UserNameCertIdCheckBaseConfig;
import com.jrmf.domain.UserNameCertIdWhiteBlackConfig;
import com.jrmf.persistence.UserNameCertIdCheckDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 种路路
 * @create 2020-01-07 21:15
 * @desc 二要素验证
 **/
@Service("userNameCertIdCheckService")
public class UserNameCertIdCheckServiceImpl implements UserNameCertIdCheckService {

    @Autowired
    private UserNameCertIdCheckDao userNameCertIdCheckDao;
    /**
     * 添加二要素验证记录
     * @param userNameCertIdCheck 二要素验证记录
     */
    @Override
    public void addUserNameCertIdCheck(UserNameCertIdCheck userNameCertIdCheck) {
        userNameCertIdCheckDao.addUserNameCertIdCheck(userNameCertIdCheck);
    }
    /**
     * 二要素基础配置信息
     * @return 基础配置信息集合
     */
    @Override
    public List<UserNameCertIdCheckBaseConfig> getUserNameCertIdCheckBaseConfigList() {
        return userNameCertIdCheckDao.getUserNameCertIdCheckBaseConfigList();
    }

    /**
     * 二要素基础配置信息
     * @param checkType 配置类型
     * @return 基础配置信息集合
     */
    @Override
    public UserNameCertIdCheckBaseConfig getUserNameCertIdCheckBaseConfigByCheckType(int checkType) {
        return userNameCertIdCheckDao.getUserNameCertIdCheckBaseConfigByCheckType(checkType);
    }

    /**
     * 根据商户key查询二要素校验黑白名单表
     * @param customkey 商户key
     * @param type  1.黑名单   2.白名单
     * @return UserNameCertIdWhiteBlackConfig
     */
    @Override
    public UserNameCertIdWhiteBlackConfig getUserNameCertIdWhiteAndBlackConfigByCustomkeyAndType(String customkey, int type) {
        return userNameCertIdCheckDao.getUserNameCertIdWhiteAndBlackConfigByCustomkeyAndType(customkey,type);
    }

    /**
     * 根据服务公司id或者代理商id 查询二要素基础配置信息
     *
     * @param customkey 服务公司id或者代理商id
     * @return 基础配置信息集合
     */
    @Override
    public UserNameCertIdCheckBaseConfig getUserNameCertIdCheckBaseConfigByNotAllCheckCustomkey(String customkey) {
        return userNameCertIdCheckDao.getUserNameCertIdCheckBaseConfigByNotAllCheckCustomkey(customkey);
    }
}
