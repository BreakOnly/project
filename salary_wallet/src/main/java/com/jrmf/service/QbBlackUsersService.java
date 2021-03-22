package com.jrmf.service;

import java.util.List;
import java.util.Map;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbBlackUsers;
public interface QbBlackUsersService {

    int deleteByPrimaryKey(Integer id);

    int insert(QbBlackUsers record);

    QbBlackUsers selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(QbBlackUsers record);

    int updateByPrimaryKey(QbBlackUsers record);

	int queryBlackUsersCount(Page page);

	List<Map<String, Object>> queryBlackUsers(Page page);

	int checkIsExists(QbBlackUsers qBlackUsers);

	int updateBlackUserById(QbBlackUsers qBlackUsers);

	int updateStatusById(QbBlackUsers qBlackUsers);

	List<Map<String, Object>> queryBlackUsersNoPage(Page page);

	int countExistByCertIdAndName(QbBlackUsers qBlackUsers);
}