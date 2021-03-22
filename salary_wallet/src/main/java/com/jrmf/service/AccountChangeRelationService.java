package com.jrmf.service;


import com.jrmf.domain.AccountChangeRelation;
import com.jrmf.domain.ChannelCustom;

import java.util.List;
import java.util.Map;

public interface AccountChangeRelationService {

    int deleteByPrimaryKey(Integer id);

    int insert(AccountChangeRelation record);

    int insertSelective(AccountChangeRelation record);

    AccountChangeRelation selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AccountChangeRelation record);

    int updateByPrimaryKey(AccountChangeRelation record);

    List<Map<String, Object>> changeAccountList(Integer accountId,Integer changeAccountId);

    /**
     * 通过id查询账户切换列表信息
     * @param id
     * @return
     */
    AccountChangeRelation getAccountChangeRelationById(String id);

    /**
     * 通过id删除账号切换列表信息
     * @param id
     */
    void deleteAccountChangeRelationById(String id);

    /**
     * 切换账号配置管理-> 新增/修改
     * @param customId
     * @param configCustomId
     * @return
     */
    Map<String, Object> configAccount(ChannelCustom loginUser, String customId, String[] configCustomId, String id);
}