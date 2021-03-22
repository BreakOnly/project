package com.jrmf.persistence;

import com.jrmf.domain.LinkageBaseConfig;
import com.jrmf.domain.LinkageCustomConfig;
import com.jrmf.domain.Page;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface LinkageCustomConfigDao {

	List<Map<String, Object>> getCustomLinkConfigList(Page page);

	int getCustomLinkConfigListCount(Page page);

    int deleteByPrimaryKey(Integer id);

    int insert(LinkageBaseConfig record);

    LinkageBaseConfig getConfigByCustomKey(String customKey, Integer linkageType);

    int insert(LinkageCustomConfig record);

	List<LinkageCustomConfig> getLinkConfigByConfigId(String configId);

	List<LinkageCustomConfig> getCustomConfigByLinkType(
			LinkageCustomConfig linkageCustomConfig);

    /**
     * 查询资金联动账户账户实时机构
     * @param paramMap
     * @return
     */
    List<LinkageBaseConfig> getMoneyLinkageByParam(Map<String, String> paramMap);

	LinkageCustomConfig getCustomConfigById(Integer id);

    LinkageBaseConfig getConfigById(String id);

    LinkageBaseConfig getLinkageConfigByCustomKey(String customkey);
}
