package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.Page;
import com.jrmf.domain.WhiteUser;
import com.jrmf.persistence.WhiteUserDao;

@Service
public class WhiteUserServiceImpl implements WhiteUserService{

	@Autowired
	private WhiteUserDao whiteUserDao;

	@Override
	public List<Map<String, Object>> getWhiteUsersByPage(Page page) {
		return whiteUserDao.getWhiteUsersByPage(page);
	}

	@Override
	public int getWhiteUsersCount(Page page) {
		return whiteUserDao.getWhiteUsersCount(page);
	}

	@Override
	public List<Map<String, Object>> getWhiteUsersNoPage(Page page) {
		return whiteUserDao.getWhiteUsersNoPage(page);
	}

	@Override
	public int insert(WhiteUser whiteUser) {
		return whiteUserDao.insert(whiteUser);
	}

	@Override
	public int update(WhiteUser whiteUser) {
		return whiteUserDao.update(whiteUser);
	}

	@Override
	public int checkIsExists(WhiteUser whiteUser) {
		return whiteUserDao.checkIsExists(whiteUser);
	}

	@Override
	public int deleteWhiteUserById(Integer id) {
		return whiteUserDao.deleteWhiteUserById(id);
	}

	@Override
	public WhiteUser getOne(Integer id) {
		return whiteUserDao.getOne(id);
	}

	@Override
	public int updateStatusById(WhiteUser whiteUser) {
		return whiteUserDao.updateStatusById(whiteUser);
	}

	@Override
	public Integer checkIsWhiteUser(WhiteUser whiteUser) {
		return whiteUserDao.checkIsWhiteUser(whiteUser);
	}
	
}
