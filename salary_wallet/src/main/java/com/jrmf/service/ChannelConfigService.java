package com.jrmf.service;

import com.jrmf.domain.ChannelConfig;

import java.util.List;
import java.util.Map;

/**
 * @Title: ChannelConfigService
 * @Description:
 * @create 2019/10/15 19:52
 */
public interface ChannelConfigService {
    /**
     * 查询收款账户信息
     * @return
     */
    List<ChannelConfig> queryChannelConfig(Map<String, Object> paramMap);

    /**
     * 通过id查询收款账户
     * @param id
     * @return
     */
    ChannelConfig queryChannelConfigById(int id);

    /**
     * 删除收款账户
     * @param id
     */
    void deleteChannelConfigById(int id);

    /**
     * 修改收款账户信息
     * @param channelConfig
     */
    void updatePaymentAccount(ChannelConfig channelConfig);

    /**
     * 新增收款账户信息
     * @param channelConfig
     */
    void insertPaymentAccount(ChannelConfig channelConfig);

    /**
     * 查询账户总条数
     * @param paramMap
     * @return
     */
    int queryChannelConfigCount(Map<String, Object> paramMap);
}
