package com.jrmf.domain;

import lombok.Data;

/**
 * @author 种路路
 * @create 2020-01-08 20:13
 * @desc 二要素商户黑白名单
 **/
@Data
public class UserNameCertIdWhiteBlackConfig extends UserNameCertIdConfig {
    /**
     * 1.限制黑名单
     * 2.增强白名单
     */
    private int type;
    /**
     * 商户id
     */
    private String customkey;

}
