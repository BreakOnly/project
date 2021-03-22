package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbBlackUsers;
import com.jrmf.persistence.QbBlackUsersDao;

@Service
public class QbBlackUsersServiceImpl implements QbBlackUsersService{

	@Autowired
	private QbBlackUsersDao qbBlackUsersDao;
	
	@Override
	public int deleteByPrimaryKey(Integer id) {
		return qbBlackUsersDao.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(QbBlackUsers record) {
		return qbBlackUsersDao.insert(record);
	}

	@Override
	public QbBlackUsers selectByPrimaryKey(Integer id) {
		return qbBlackUsersDao.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKeySelective(QbBlackUsers record) {
		return qbBlackUsersDao.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(QbBlackUsers record) {
		return qbBlackUsersDao.updateByPrimaryKey(record);
	}

	@Override
	public int queryBlackUsersCount(Page page) {
		return qbBlackUsersDao.queryBlackUsersCount(page);
	}

	@Override
	public List<Map<String, Object>> queryBlackUsers(Page page) {
		return qbBlackUsersDao.queryBlackUsers(page);
	}

	@Override
	public int checkIsExists(QbBlackUsers qBlackUsers) {
		return qbBlackUsersDao.checkIsExists(qBlackUsers);
	}

	@Override
	public int updateBlackUserById(QbBlackUsers qBlackUsers) {
		return qbBlackUsersDao.updateBlackUserById(qBlackUsers);
	}

	@Override
	public int updateStatusById(QbBlackUsers qBlackUsers) {
		return qbBlackUsersDao.updateStatusById(qBlackUsers);
	}

	@Override
	public List<Map<String, Object>> queryBlackUsersNoPage(Page page) {
		return qbBlackUsersDao.queryBlackUsersNoPage(page);
	}

	@Override
	public int countExistByCertIdAndName(QbBlackUsers qBlackUsers) {
		int isBlack = qbBlackUsersDao.checkIsExists(qBlackUsers);
		if (isBlack < 1) {
			isBlack = qbBlackUsersDao.checkIsExistsByName(qBlackUsers);
		}
		return isBlack;
	}

}
