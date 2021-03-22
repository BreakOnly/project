package com.jrmf.service;

import com.jrmf.domain.LdOrderCorrect;
import com.jrmf.domain.Page;
import com.jrmf.persistence.LdOrderCorrectDao;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class LdOrderCorrectServiceImpl implements LdOrderCorrectService{

	@Autowired
	private LdOrderCorrectDao ldOrderCorrectDao;
	
	@Override
	public int insert(LdOrderCorrect record) {
		return ldOrderCorrectDao.insert(record);
	}

	@Override
	public int updateByPrimaryKeySelective(LdOrderCorrect record) {
		return ldOrderCorrectDao.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(LdOrderCorrect record) {
		return ldOrderCorrectDao.updateByPrimaryKeySelective(record);
	}

	@Override
	public int queryLdCorrectOrderDetailListCount(Page page) {
		return ldOrderCorrectDao.queryLdCorrectOrderDetailListCount(page);
	}

	@Override
	public List<Map<String, Object>> queryLdCorrectOrderDetailList(Page page) {
		return ldOrderCorrectDao.queryLdCorrectOrderDetailList(page);
	}

	@Override
	public List<LdOrderCorrect> getLdCorrectListByTypeAndStatus() {
		return ldOrderCorrectDao.getLdCorrectListByTypeAndStatus();
	}

	@Override
	public List<LdOrderCorrect> getLdCorrectListByTypeAndStatusOnJob() {
		return ldOrderCorrectDao.getLdCorrectListByTypeAndStatusOnJob();
	}

}