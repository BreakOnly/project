package com.jrmf.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jrmf.domain.LinkAccountTrans;
import com.jrmf.domain.Page;
import com.jrmf.persistence.LinkAccountTransDao;

@Service
public class LinkAccountTransServiceImpl implements LinkAccountTransService{
	
	@Autowired
	private LinkAccountTransDao linkAccountTransDao;

	@Override
	public int insert(LinkAccountTrans record) {
		return linkAccountTransDao.insert(record);
	}

	@Override
	public List<Map<String, Object>> getLinkAccountTransList(Page page) {
		return linkAccountTransDao.getLinkAccountTransList(page);
	}

	@Override
	public int getLinkAccountTransListCount(Page page) {
		return linkAccountTransDao.getLinkAccountTransListCount(page);
	}

	@Override
	public List<Map<String, String>> getLinkAccountTransListNoPage(Page page) {
		return linkAccountTransDao.getLinkAccountTransListNoPage(page);
	}

}
