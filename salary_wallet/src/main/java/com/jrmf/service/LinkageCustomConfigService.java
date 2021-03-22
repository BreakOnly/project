package com.jrmf.service;

import com.jrmf.domain.LinkageBaseConfig;
import com.jrmf.domain.LinkageCustomConfig;
import com.jrmf.domain.Page;

import java.util.List;
import java.util.Map;

public interface LinkageCustomConfigService {

	List<Map<String, Object>> getCustomLinkConfigList(Page page);

	int getCustomLinkConfigListCount(Page page);

    int deleteByPrimaryKey(Integer id);

    LinkageBaseConfig getConfigByCustomKey(String customKey, Integer linkageType);

    int insert(LinkageCustomConfig record);

	List<LinkageCustomConfig> getLinkConfigByConfigId(String id);

	List<LinkageCustomConfig> getCustomConfigByLinkType(
			LinkageCustomConfig linkageCustomConfig);

	/**
	 * 查询资金联动账户账户实时机构
	 * @param paramMap
	 * @return
	 */
    List<LinkageBaseConfig> getMoneyLinkageByParam(Map<String, String> paramMap);

	LinkageBaseConfig getConfigById(String id);

	LinkageCustomConfig getCustomConfigById(Integer id);

    LinkageBaseConfig getLinkageConfigByCustomKey(String customkey);
}
