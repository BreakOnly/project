package com.jrmf.persistence;

import com.jrmf.domain.CustomProxy;
import com.jrmf.domain.OrganizationNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CustomProxyDao {

    OrganizationNode getNodeById(int id,Integer platformId);

    List<String> queryProxyCurrentAndChildrenCustomkeys(String levelCode);

    List<OrganizationNode> queryChild(@Param("levelCode") String levelCode, @Param("nodeId") int nodeId,@Param("platformId")Integer platformId);

    OrganizationNode getNodeByCustomKey(String customKey,@Param("platformId") Integer platformId);

    List<OrganizationNode> queryCurrentAndChildrenNodesLevelOne(@Param("levelCode") String levelCode, @Param("contentLevel") int contentLevel,@Param("platformId")Integer platformId);

    List<OrganizationNode> queryNodesChildLevelOne(@Param("levelCode") String levelCode, @Param("contentLevel") int contentLevel,@Param("platformId")Integer platformId);

    List<OrganizationNode> queryNodesCurrentAndChildren(String levelCode,Integer platformId);

    List<OrganizationNode> listAll(Integer platformId);

    int insertProxy(OrganizationNode node);

    int updateNodeById(OrganizationNode newNode);

    List<OrganizationNode> queryNodeByParam(Map<String, Object> params);

    int enable(String customkey);

    List<OrganizationNode> queryProxyTree(@Param("platformId") Integer platformId);

    int removeById(Integer id);

    int insertProxyChilden(OrganizationNode node);

    int updateProxyChildenNodeById(OrganizationNode newNode);

    OrganizationNode getProxyChildenNodeByCustomKey(String customKey,Integer platformId);

    int removeProxyChildenById(Integer id);

    OrganizationNode getProxyChildenNodeById(int nodeId);

    List<OrganizationNode> queryProxyChildenChild(@Param("levelCode") String levelCode, @Param("nodeId") int nodeId);

    List<OrganizationNode> listAllProxyChilden();

    List<OrganizationNode> queryProxyChildenNodesChildLevelOne(@Param("levelCode") String levelCode, @Param("contentLevel") int contentLevel, @Param("proxyLevelCode") String proxyLevelCode, @Param("proxyContentLevel") int proxyContentLevel);

    List<String> queryProxyChildenCustomKeyCurrentAndChildren(String customKey);

    List<String> queryProxyChildenCurrentAndChildrenCustomkeys(String levelCode);

    /**
     * 获取代理商ID根据商户唯一标识
     * @param customkey
     * @return
     */
    CustomProxy getProxyIdByCustomkey(@Param("customkey") String customkey);

    /**
     * 通过代理商ID查询绑定的商户key
     * @param levelCode
     * @return
     */
    List<String> getCustomkeyByProxyId(@Param("levelCode") String levelCode);

    /**
     * 通过唯一标识查询levelcode
     * @param newCustomkey
     * @return
     */
    List<String> getProxyLevelCodeByCustomkey(@Param("customkey") String newCustomkey);

    String getProxyById(@Param("id") int parentId);

    OrganizationNode getNodeByIdCustomkey(@Param("nodeId") int nodeId,@Param("customkey") String customkey);

    List<OrganizationNode> getCustomProxyByLevelCode(String joinCustomkey);
}
