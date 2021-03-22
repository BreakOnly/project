package com.jrmf.persistence;

import com.jrmf.domain.OrganizationNode;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


@Mapper
public interface CustomGroupDao {

    /**
    * @Description platformId 所属平台ID
    **/
    OrganizationNode getNodeById(@Param("id")int id,@Param("platformId") Integer platformId);

    List<String> queryGroupCurrentAndChildrenCustomkeys(String levelCode);

    List<OrganizationNode> queryChild(@Param("levelCode") String levelCode, @Param("nodeId") int nodeId,@Param("platformId")Integer platformId);

    OrganizationNode getNodeByCustomKey(@Param("customKey")String customKey,@Param("platformId")Integer platformId);

    List<OrganizationNode> queryCurrentAndChildrenNodesLevelOne(@Param("levelCode") String levelCode, @Param("contentLevel") int contentLevel, @Param("platformId") Integer platformId);

    List<OrganizationNode> queryNodesChildLevelOne(@Param("levelCode") String levelCode, @Param("contentLevel") int contentLevel,@Param("platformId") Integer platformId);

    List<OrganizationNode> queryNodesCurrentAndChildren(@Param("levelCode")String levelCode,@Param("platformId")Integer platformId);

    List<OrganizationNode> listAll(@Param("platformId")Integer platformId);

    int insertGroup(OrganizationNode node);

    int updateNodeById(OrganizationNode newNode);

    List<OrganizationNode> queryNodeByParam(Map<String, Object> params);

    int enable(String customkey);

    int removeById(@Param("id")Integer id,@Param("levelCode")String levelCode);

    /**
     * 通过customkey查询levelcode
     * @param newCustomkey
     * @return
     */
    List<String> getLevelCodeByCustomkey(@Param("newCustomkey") String newCustomkey);

    /**
     * 查询 商户唯一标识 通过leveCode
     * @param code
     * @return
     */
    List<String> getCustomkeyByLeveCode(@Param("levelCode") String code);

    /**
     * 通过customkey查询group
     * @param customkey
     * @return
     */
    OrganizationNode getGroupIdByCustomkey(String customkey);

    int updateNodeParentIdById(OrganizationNode newNode);

	OrganizationNode getNodeByIdCustomkey(@Param("nodeId") int nodeId, @Param("customkey") String customkey);

    OrganizationNode getCustomGroupByCustomkey(String customKey);
}
