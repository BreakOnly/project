package com.jrmf.service;

import com.jrmf.domain.CallBackInfo;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-05-21 10:46
 * @desc 回调信息
 **/
public interface CallBackInfoService {
    /**
     *
     * @param callBackInfo
     */
    void addCallBackInfo(CallBackInfo callBackInfo);

    /**'
     * 获取回调信息
     * @param serialNo
     * @return
     */
    CallBackInfo getCallBackInfoBySerialNo(String serialNo);

    /**
     * 修改回调信息
     * @param callBackInfo 回调信息
     */
    void updateCallBackInfo(CallBackInfo callBackInfo);

    /**
     * 查询所有  待通知的  回调信息
     * @return 列表
     */
    List<CallBackInfo> getNotifyCallBackInfos();

    /**
     * 根据查询条件返回  list
     * @param paramMap 条件
     * @return list
     */
    List<CallBackInfo> getCallBackInfoByParams(Map<String, Object> paramMap);
}
