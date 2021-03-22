package com.jrmf.service;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.jrmf.domain.CustomLdConfig;
import com.jrmf.persistence.CustomLdConfigDao;
@Service
public class CustomLdConfigServiceImpl implements CustomLdConfigService{

	@Resource
	private CustomLdConfigDao customLdConfigDao;
	
	@Override
	public CustomLdConfig getCustomLdConfigByMer(Map<String, Object> params) {
		return customLdConfigDao.getCustomLdConfigByMer(params);
	}

}
