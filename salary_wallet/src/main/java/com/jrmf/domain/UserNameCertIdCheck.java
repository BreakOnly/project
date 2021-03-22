package com.jrmf.domain;

import lombok.Data;

/**
 * @author 种路路
 * @create 2020-01-07 21:05
 * @desc 二要素验证
 **/
@Data
public class UserNameCertIdCheck {
    /**
     * 主键
     */
    private int id;
    /**
     * 商户key
     */
    private String customkey;
    /**
     * 姓名
     */
    private String userName;
    /**
     * 身份证号
     */
    private String certId;
    /**
     * 验证结果
     * 1  成功
     * 0  失败
     */
    private int result;
    /**
     * 验证结果描述
     */
    private String resultMessage;
    /**
     * 创建时间
     */
    private String createTime;
}
