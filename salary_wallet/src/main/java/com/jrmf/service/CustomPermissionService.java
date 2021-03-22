/**
 * 
 */
package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jrmf.domain.ChannelCustomCatalog;
import com.jrmf.domain.ChannelPermission;
import com.jrmf.domain.Page;

/**
 * @author: liangyu
 * @date: 2016-3-18
 * @description:
 */
@Service
public interface CustomPermissionService {

	/**
	 * @param id
	 * @param string
	 * @return
	 */
	List<ChannelCustomCatalog> getCustomContentByCustomId(int id, String level);

	List<ChannelPermission> getAllPermission();

	List<ChannelPermission> getCustomPermission(int id);

	public void deleteCustomPermission(String customId);

	public void saveCustomPermission(String customId, String[] ids);

	List<String> getPermissionUrlByCustomId(int id);

	ChannelPermission getPermissionDetailById(String id);
	
	public void savePermission(ChannelPermission channelPermission);
	
	public void updatePermission(ChannelPermission channelPermission);

	List<ChannelPermission> getCustomPermissionReal(int parseInt);
	
	void saveCustomPermissionByTempId(String customId, Integer tempId,Integer type);

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
