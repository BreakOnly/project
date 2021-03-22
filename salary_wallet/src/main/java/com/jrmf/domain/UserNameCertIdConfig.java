package com.jrmf.domain;

import lombok.Data;

/**
 * @author 种路路
 * @create 2020-02-06 16:12
 * @desc
 **/
@Data
public class UserNameCertIdConfig {
    /**
     * 主键
     */
    private int id;
    /**
     * 日限制使用次数
     */
    private int dayLimit;
    /**
     * 月限制使用次数
     */
    private int monthLimit;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 修改时间
     */
    private String updateTime;
}
