package com.jrmf.service;

import java.util.Map;
import com.jrmf.domain.CustomLdConfig;

public interface CustomLdConfigService {

	//获取联动下发配置信息
	public CustomLdConfig getCustomLdConfigByMer(Map<String, Object> params);
}