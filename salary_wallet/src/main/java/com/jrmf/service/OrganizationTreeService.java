package com.jrmf.service;

import com.jrmf.domain.OrganizationNode;

import java.util.List;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/28 15:10
 * Version:1.0
 */
public interface OrganizationTreeService {

    List<OrganizationNode> queryOrganizationTree(String customKey, int customType, String queryMode, int nodeId,Integer platformId);

    List<String> queryNodeCusotmKey(int customType, String queryMode, int nodeId);

    int queryNodeIdByCustomKey(String customKey);

    boolean addProxy(String customKey);

    boolean addGroup(String customKey);

    boolean addParentProxy(String agentCustomKey, String childCustomKey);

    boolean addParentGroup(String parentGroupCustomKey, String childCustomKey);

    boolean removeParentGroup(String childCustomKey);

    boolean removeParentProxy(String childCustomKey);

    boolean addProxyChilden(String customKey);

    boolean addParentProxyChilden(String agentCustomKey, String childCustomKey);

    boolean removeParentProxyChilden(String childCustomKey);

    List<String> queryAllCurrentAndChildrenCustomkeys(String customKey);
    
    List<String> queryNodeCusotmKey(String customKey,int customType, String queryMode, int nodeId);
    List<String> queryNodeCustomKeyListByChannelCustomId(int customType, String queryMode, int channelCustomId);
}

