package com.jrmf.domain;

import lombok.Data;

/**
 * @author 种路路
 * @create 2020-02-26 13:40
 * @desc
 **/
@Data
public class OperateRecord {

    /**
     * 主键
     */
    private int id;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 请求链接
     */
    private String url;
    /**
     * 方法名称
     */
    private String methodName;
    /**
     * 请求参数
     */
    private String parameter;
    /**
     * 创建时间
     */
    private String createTime;

}
