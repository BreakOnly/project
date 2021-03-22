package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbBlackUsers;
@Mapper
public interface QbBlackUsersDao {

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

	int checkIsExistsByName(QbBlackUsers qBlackUsers);
}