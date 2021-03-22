package com.jrmf.persistence;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.CustomLdConfig;
@Mapper
public interface CustomLdConfigDao {

	//获取联动下发配置信息
	public CustomLdConfig getCustomLdConfigByMer(Map<String, Object> params);
}