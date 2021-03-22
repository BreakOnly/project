package com.jrmf.service;

import com.jrmf.domain.OperateRecord;
import com.jrmf.domain.OperateURL;

/**
 * @author 种路路
 * @create 2020-02-26 13:37
 * @desc 操作记录
 **/
public interface OperateRecordService {

    /**
     * 保存记录
     * @param operateRecord 记录
     */
    void addOperateRecord(OperateRecord operateRecord);
    /**
     * 获取OperateURL
     */
    OperateURL getOperateURL(String url);
}
