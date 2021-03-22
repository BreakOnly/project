package com.jrmf.service;

import com.jrmf.domain.CallBackInfo;
import com.jrmf.persistence.CallBackInfoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-05-21 10:47
 * @desc
 **/
@Service("callBackInfoService")
public class CallBackInfoServiceImpl implements CallBackInfoService {
    private final CallBackInfoDao callBackInfoDao;

    @Autowired
    public CallBackInfoServiceImpl(CallBackInfoDao callBackInfoDao) {
        this.callBackInfoDao = callBackInfoDao;
    }

    @Override
    public void addCallBackInfo(CallBackInfo callBackInfo) {
        callBackInfoDao.addCallBackInfo(callBackInfo);
    }

    @Override
    public CallBackInfo getCallBackInfoBySerialNo(String serialNo) {
        return callBackInfoDao.getCallBackInfoBySerialNo(serialNo);
    }

    /**
     * 修改回调信息
     * @param callBackInfo 回调信息
     */
    @Override
    public void updateCallBackInfo(CallBackInfo callBackInfo) {
        callBackInfoDao.updateCallBackInfo(callBackInfo);
    }

    /**
     * 查询所有  待通知的  回调信息
     * @return 列表
     */
    @Override
    public List<CallBackInfo> getNotifyCallBackInfos() {
        return callBackInfoDao.getNotifyCallBackInfos();
    }

    /**
     * 根据查询条件返回  list
     * @param paramMap 条件
     * @return list
     */
    @Override
    public List<CallBackInfo> getCallBackInfoByParams(Map<String, Object> paramMap) {
        return callBackInfoDao.getCallBackInfoByParams(paramMap);
    }
}
