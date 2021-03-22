/**
 * 
 */
package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.ChannelCustomCatalog;
import com.jrmf.domain.ChannelPermission;
import com.jrmf.domain.Page;

import org.springframework.stereotype.Repository;

/**
 * @author: liangyu
 * @date: 2016-3-18
 * @description:
 */
@Mapper
public interface CustomPermissionDao {

	/**
	 * @param id
	 * @param level
	 * @return
	 */
	List<ChannelCustomCatalog> getCustomContentByCustomId(@Param("id")int id,@Param("level") String level);

	List<ChannelPermission> getAllPermission();

	List<ChannelPermission> getCustomPermission(int id);

	List<ChannelPermission> getCustomPermissionReal(int id);
	
	public void deleteCustomPermission(String customId);

	public void saveCustomPermission(@Param("customId")String customId,@Param("ids") String[] ids);

	List<String> getPermissionUrlByCustomId(int id);

	ChannelPermission getPermissionDetailById(String id);
	
	public void savePermission(ChannelPermission channelPermission);

	public void updatePermission(ChannelPermission channelPermission);
	
	void saveCustomPermissionByTempId(@Param("customId")String customId, @Param("tempId")Integer tempId, @Param("type")Integer type);

	Map<String, Object> getTempTypeByCustomId(int parseInt);

	List<String> getCustomPermissionTemp(int id);

	List<ChannelPermission> getCustomPermissionByIds(String menuIds);

	List<Map<String, Object>> getPermissionTempMapping(Page page);

	List<Map<String, Object>> getCustomMenuIdsByLevel(Map<String, Object> paramsMap);

	List<String> getCustomPerissionMapping(Map<String, Object> paramsMap);

	List<Map<String, Object>> getCustomMenuIdsTempByLevel(Map<String, Object> paramsMap);

	int checkIsHaveChild(int id);

	int getPermissionTempMappingCount(Page page);
}
