package com.jrmf.persistence;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.WebUser;

@Mapper
public interface WebUserDao {
	
	public int countUserNumByParam(String param);

	public void insertUser(WebUser user);
	
	public WebUser getUserByColumn(String param);
	
	public void updateUserInfo(WebUser user);
	
	public WebUser getUserByName(String param);
	
	public WebUser getUserByEmail(String param);
	
	public void updateLastlogin(int userId);

	public void insertAccount(Map<String,Object> param);
}
