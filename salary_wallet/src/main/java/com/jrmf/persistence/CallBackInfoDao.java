package com.jrmf.persistence;

import com.jrmf.domain.CallBackInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-05-21 10:03
 * @desc 回调信息
 **/
@Mapper
public interface CallBackInfoDao {

    void addCallBackInfo(CallBackInfo callBackInfo);

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
