package com.jrmf.domain;

import lombok.Data;

/**
 * @author 种路路
 * @create 2020-01-07 21:36
 * @desc 二要素验证基础配置表
 **/
@Data
public class UserNameCertIdCheckBaseConfig extends UserNameCertIdConfig {

    /**
     * 配置验证类型
     * 1.限制黑名单
     * 2.增强白名单
     * 3.部分验证（代理商/服务公司）
     * 4.全部验证
     */
    private int checkType;
    /**
     * 部分验证类型
     * 1.代理商
     * 2.服务公司
     */
    private int notAllCheckRole;
    /**
     * 配置部分验证商户id
     */
    private String notAllCheckCustomkey;
}
