package com.jrmf.service;

import com.jrmf.domain.ChannelConfig;
import com.jrmf.persistence.ChannelConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Title: ChannelConfigServiceImpl
 * @Description:
 * @create 2019/10/15 19:53
 */
@Service("channelConfigService")
public class ChannelConfigServiceImpl implements ChannelConfigService {

    @Autowired
    private ChannelConfigDao channelConfigDao;

    /**
     * 查询收款账户信息
     * @return
     */
    @Override
    public List<ChannelConfig> queryChannelConfig(Map<String, Object> paramMap) {
        return channelConfigDao.queryChannelConfig(paramMap);
    }

    /**
     * 通过id查询收款账户
     * @param id
     * @return
     */
    @Override
    public ChannelConfig queryChannelConfigById(int id) {
        return channelConfigDao.queryChannelConfigById(id);
    }

    /**
     * 删除收款账户
     * @param id
     */
    @Override
    public void deleteChannelConfigById(int id) {
        channelConfigDao.deleteChannelConfigById(id);
    }

    /**
     * 修改收款账户信息
     * @param channelConfig
     */
    @Override
    public void updatePaymentAccount(ChannelConfig channelConfig) {
        channelConfigDao.updatePaymentAccount(channelConfig);
    }

    /**
     * 新增收款账户信息
     * @param channelConfig
     */
    @Override
    public void insertPaymentAccount(ChannelConfig channelConfig) {
        channelConfigDao.insertPaymentAccount(channelConfig);
    }

    /**
     * 查询账户总条数
     * @param paramMap
     * @return
     */
    @Override
    public int queryChannelConfigCount(Map<String, Object> paramMap) {
        return channelConfigDao.queryChannelConfigCount(paramMap);
    }

}
