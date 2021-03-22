/**
 * 
 */
package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.ChannelCustomCatalog;
import com.jrmf.domain.ChannelPermission;
import com.jrmf.domain.Page;
import com.jrmf.persistence.CustomPermissionDao;

/**
 * @author: liangyu
 * @date: 2016-3-18
 * @description:
 */
@Service("customPermissionService")
public class CustomPermissionServiceImpl implements CustomPermissionService {
	
	@Autowired
	CustomPermissionDao customPermissionDao;
	
	@Override
	public List<ChannelCustomCatalog> getCustomContentByCustomId(int id,
			String level) {
		return customPermissionDao.getCustomContentByCustomId(id,level);
	}

	@Override
	public List<ChannelPermission> getAllPermission() {
		return customPermissionDao.getAllPermission();
	}

	@Override
	public List<ChannelPermission> getCustomPermission(int id) {
		return customPermissionDao.getCustomPermission(id);
	}

	@Override
	public void deleteCustomPermission(String customId) {
		customPermissionDao.deleteCustomPermission(customId);
	}

	@Override
	public void saveCustomPermission(String customId,String[] ids) {
		customPermissionDao.saveCustomPermission(customId,ids);
	}

	@Override
	public List<String> getPermissionUrlByCustomId(int id) {
		return customPermissionDao.getPermissionUrlByCustomId(id);
	}

	@Override
	public ChannelPermission getPermissionDetailById(String id) {
		return customPermissionDao.getPermissionDetailById(id);
	}
	
	@Override
	public void savePermission(ChannelPermission channelPermission) {
		customPermissionDao.savePermission(channelPermission);
	}

	@Override
	public void updatePermission(ChannelPermission channelPermission) {
		customPermissionDao.updatePermission(channelPermission);
	}

	@Override
	public List<ChannelPermission> getCustomPermissionReal(int parseInt) {
		return customPermissionDao.getCustomPermissionReal(parseInt);
	}
	
	@Override
	public void saveCustomPermissionByTempId(String customId, Integer tempId,Integer type) {
		customPermissionDao.saveCustomPermissionByTempId(customId,tempId,type);
	}

	@Override
	public Map<String, Object> getTempTypeByCustomId(int parseInt) {
		return customPermissionDao.getTempTypeByCustomId(parseInt);
	}

	@Override
	public List<String> getCustomPermissionTemp(int id) {
		return customPermissionDao.getCustomPermissionTemp(id);
	}

	@Override
	public List<ChannelPermission> getCustomPermissionByIds(String menuIds) {
		return customPermissionDao.getCustomPermissionByIds(menuIds);
	}

	@Override
	public List<Map<String, Object>> getPermissionTempMapping(Page page) {
		return customPermissionDao.getPermissionTempMapping(page);
	}

	@Override
	public List<Map<String, Object>> getCustomMenuIdsByLevel(Map<String, Object> paramsMap) {
		return customPermissionDao.getCustomMenuIdsByLevel(paramsMap);
	}

	@Override
	public List<String> getCustomPerissionMapping(Map<String, Object> paramsMap) {
		return customPermissionDao.getCustomPerissionMapping(paramsMap);
	}

	@Override
	public List<Map<String, Object>> getCustomMenuIdsTempByLevel(Map<String, Object> paramsMap) {
		return customPermissionDao.getCustomMenuIdsTempByLevel(paramsMap);
	}

	@Override
	public int checkIsHaveChild(int id) {
		return customPermissionDao.checkIsHaveChild(id);
	}

	@Override
	public int getPermissionTempMappingCount(Page page) {
		return customPermissionDao.getPermissionTempMappingCount(page);
	}
}
