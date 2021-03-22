package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.LinkageBaseConfig;
import com.jrmf.domain.Page;
import com.jrmf.persistence.LinkConfigDao;

@Service
public class LinkConfigServiceImpl implements LinkConfigService{
	
	@Autowired
	private LinkConfigDao linkConfigDao;

	@Override
	public List<Map<String, Object>> getLinkConfigList(Page page) {
		return linkConfigDao.getLinkConfigList(page);
	}

	@Override
	public int getLinkConfigListCount(Page page) {
		return linkConfigDao.getLinkConfigListCount(page);
	}

	@Override
	public int deleteByPrimaryKey(Integer id) {
		return linkConfigDao.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(LinkageBaseConfig record) {
		return linkConfigDao.insert(record);
	}

	@Override
	public LinkageBaseConfig getLinkConfigById(Integer id) {
		return linkConfigDao.getLinkConfigById(id);
	}

	@Override
	public int update(LinkageBaseConfig record) {
		return linkConfigDao.update(record);
	}

	@Override
	public List<Map<String, String>> getPathInfo() {
		return linkConfigDao.getPathInfo();
	}

}
