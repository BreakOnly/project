package com.jrmf.service;

import com.jrmf.domain.LinkageBaseConfig;
import com.jrmf.domain.LinkageCustomConfig;
import com.jrmf.domain.Page;
import com.jrmf.persistence.LinkageCustomConfigDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LinkageCustomConfigServiceImpl implements LinkageCustomConfigService{

	@Autowired
	private LinkageCustomConfigDao linkageCustomConfigDao;

	@Override
	public List<Map<String, Object>> getCustomLinkConfigList(Page page) {
		return linkageCustomConfigDao.getCustomLinkConfigList(page);
	}

	@Override
	public int getCustomLinkConfigListCount(Page page) {
		return linkageCustomConfigDao.getCustomLinkConfigListCount(page);
	}

	@Override
	public int deleteByPrimaryKey(Integer id) {
		return linkageCustomConfigDao.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(LinkageCustomConfig record) {
		return linkageCustomConfigDao.insert(record);
	}

	@Override
	public LinkageBaseConfig getConfigByCustomKey(String customKey, Integer linkageType) {
		return linkageCustomConfigDao.getConfigByCustomKey(customKey, linkageType);
	}

	@Override
	public List<LinkageCustomConfig> getLinkConfigByConfigId(String configId) {
		return linkageCustomConfigDao.getLinkConfigByConfigId(configId);
	}

	@Override
	public List<LinkageCustomConfig> getCustomConfigByLinkType(
			LinkageCustomConfig linkageCustomConfig) {
		return linkageCustomConfigDao.getCustomConfigByLinkType(linkageCustomConfig);
	}

	/**
	 * 查询资金联动账户账户实时机构
	 * @param paramMap
	 * @return
	 */
	@Override
	public List<LinkageBaseConfig> getMoneyLinkageByParam(Map<String, String> paramMap) {
		return linkageCustomConfigDao.getMoneyLinkageByParam(paramMap);
	}

	@Override
	public LinkageBaseConfig getConfigById(String id) {
		return linkageCustomConfigDao.getConfigById(id);
	}

	@Override
	public LinkageCustomConfig getCustomConfigById(Integer id) {
		return linkageCustomConfigDao.getCustomConfigById(id);
	}

    @Override
    public LinkageBaseConfig getLinkageConfigByCustomKey(String customkey) {
        return linkageCustomConfigDao.getLinkageConfigByCustomKey(customkey);
    }
}
