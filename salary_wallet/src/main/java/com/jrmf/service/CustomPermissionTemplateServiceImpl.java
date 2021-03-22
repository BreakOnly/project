package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.CustomPermissionTemplate;
import com.jrmf.domain.Page;
import com.jrmf.persistence.CustomPermissionTemplateDao;

@Service
public class CustomPermissionTemplateServiceImpl implements CustomPermissionTemplateService{

	@Autowired
	private CustomPermissionTemplateDao customPermissionTemplateDao;
	
	@Override
	public List<Map<String, Object>> getList(Page page) {
		return customPermissionTemplateDao.getList(page);
	}

	@Override
	public void insertPermissionTemplate(CustomPermissionTemplate customPermissionTemplate) {
		customPermissionTemplateDao.insertPermissionTemplate(customPermissionTemplate);
	}

	@Override
	public void updateCustomPermissionTemp(CustomPermissionTemplate customPermissionTemplate) {
		customPermissionTemplateDao.updateCustomPermissionTemp(customPermissionTemplate);		
	}

	@Override
	public CustomPermissionTemplate getPermissionTempDetail(Integer id) {
		return customPermissionTemplateDao.getPermissionTempDetail(id);
	}

	@Override
	public List<CustomPermissionTemplate> getListByPojo(CustomPermissionTemplate customPermissionTemplate) {
		return customPermissionTemplateDao.getListByPojo(customPermissionTemplate);
	}

	@Override
	public void insertCustomPermissionRelation(Map<String, Object> paramsMap) {
		customPermissionTemplateDao.insertCustomPermissionRelation(paramsMap);
	}

	@Override
	public void deleteCustomPermissionRelationOld(Map<String, Object> paramsMap) {
		customPermissionTemplateDao.deleteCustomPermissionRelationOld(paramsMap);
	}

	@Override
	public int getCustomPermissionRelationTempCount(Map<String, Object> paramsMap) {
		return customPermissionTemplateDao.getCustomPermissionRelationTempCount(paramsMap);
	}

	@Override
	public void deleteCustomPermissionRelation(Map<String, Object> paramsMap) {
		customPermissionTemplateDao.deleteCustomPermissionRelation(paramsMap);
		
	}

	@Override
	public String getCustomMaster(Integer customId) {
		return customPermissionTemplateDao.getCustomMaster(customId);
	}

	@Override
	public int checkCustomUseTemp(Map<String, Object> paramsMap) {
		return customPermissionTemplateDao.checkCustomUseTemp(paramsMap);
	}

	@Override
	public int getMenuTempCount(Page page) {
		return customPermissionTemplateDao.getMenuTempCount(page);
	}

	@Override
	public int checkCustomOldPermission(Map<String, Object> paramsMap) {
		return customPermissionTemplateDao.checkCustomOldPermission(paramsMap);
	}

}