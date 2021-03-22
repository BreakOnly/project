package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.Page;
import com.jrmf.domain.WhiteUser;

@Mapper
public interface WhiteUserDao {

	List<Map<String, Object>> getWhiteUsersByPage(Page page);

	int getWhiteUsersCount(Page page);

	List<Map<String, Object>> getWhiteUsersNoPage(Page page);

	int insert(WhiteUser whiteUser);

	int update(WhiteUser whiteUser);

	int checkIsExists(WhiteUser whiteUser);

	int deleteWhiteUserById(Integer id);

	WhiteUser getOne(Integer id);

	int updateStatusById(WhiteUser whiteUser);

	Integer checkIsWhiteUser(WhiteUser whiteUser);

}
