package com.jrmf.persistence;

import com.jrmf.domain.ChannelConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Title: ChannelConfigDao
 * @Description:
 * @create 2019/10/15 20:00
 */
@Mapper
public interface ChannelConfigDao {

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
    ChannelConfig queryChannelConfigById(@Param("id") int id);

    /**
     * 删除收款账户
     * @param id
     */
    void deleteChannelConfigById(@Param("id") int id);

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
