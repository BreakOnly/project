package com.jrmf.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.WebUser;
import com.jrmf.persistence.WebUserDao;

@Service("webUserService")
public class WebUserServiceImpl implements WebUserService {

	@Autowired
	private WebUserDao userDao;

	@Override
	public boolean valiUserByColumn(String param) {
		int userNum = this.userDao.countUserNumByParam(param);
		return userNum > 0;
	}


	@Override
	public WebUser getUserByColumn(String param) {
		return this.userDao.getUserByColumn(param);
	}

	@Override
	public void updateUserInfo(WebUser user) {
		this.userDao.updateUserInfo(user);
	}

	@Override
	public WebUser getUserByName(String param) {
		return this.userDao.getUserByName(param);
	}

	@Override
	public void updateLastlogin(int userId) {
		this.userDao.updateLastlogin(userId);
	}

	@Override
	public WebUser getUserByEmail(String param) {
		return this.userDao.getUserByEmail(param);
	}


	@Override
	public void createUser(WebUser user) {
		//初始化账户信息
		Map<String,Object> account = new HashMap<String,Object>();
		account.put("customid", user.getId());
		account.put("customname", user.getUsername());
		account.put("email", user.getEmail());
		this.userDao.insertAccount(account);
	}

}
