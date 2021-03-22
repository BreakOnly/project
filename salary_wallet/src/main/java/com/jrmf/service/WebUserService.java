package com.jrmf.service;

import org.springframework.stereotype.Service;

import com.jrmf.domain.WebUser;
@Service
public interface WebUserService {
	public boolean valiUserByColumn(String param);

	public void createUser(WebUser user);

	public WebUser getUserByColumn(String param);

	public void updateUserInfo(WebUser user);

	public WebUser getUserByName(String param);

	public WebUser getUserByEmail(String param);

	public void updateLastlogin(int userId);

}
