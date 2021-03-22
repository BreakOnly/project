package com.jrmf.service;

import java.util.List;
import java.util.Map;

import com.jrmf.domain.CustomPermissionTemplate;
import com.jrmf.domain.Page;

public interface CustomPermissionTemplateService {

	List<Map<String, Object>> getList(Page page);

	void insertPermissionTemplate(CustomPermissionTemplate customPermissionTemplate);

	void updateCustomPermissionTemp(CustomPermissionTemplate customPermissionTemplate);

	CustomPermissionTemplate getPermissionTempDetail(Integer id);

	List<CustomPermissionTemplate> getListByPojo(CustomPermissionTemplate customPermissionTemplate);

	void insertCustomPermissionRelation(Map<String, Object> paramsMap);

	void deleteCustomPermissionRelationOld(Map<String, Object> paramsMap);

	int getCustomPermissionRelationTempCount(Map<String, Object> paramsMap);

	void deleteCustomPermissionRelation(Map<String, Object> paramsMap);

	String getCustomMaster(Integer customId);

	int checkCustomUseTemp(Map<String, Object> paramsMap);

	int getMenuTempCount(Page page);

	int checkCustomOldPermission(Map<String, Object> paramsMap);

}