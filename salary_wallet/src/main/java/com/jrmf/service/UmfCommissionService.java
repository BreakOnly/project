package com.jrmf.service;

import org.springframework.stereotype.Service;

/**
 * @author 种路路
 * @version 创建时间：2019年4月22日17:02:03
 */
@Service
public interface UmfCommissionService {

    /**
     * 下载对账文件
     *
     * @param merId  商户号
     * @param time  下载文件时间
     */
    void downloadUserCommission(String merId,String time);

}
