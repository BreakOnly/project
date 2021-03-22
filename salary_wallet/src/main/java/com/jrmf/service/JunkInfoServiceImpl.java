package com.jrmf.service;


import com.jrmf.domain.*;
import com.jrmf.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JunkInfoServiceImpl implements JunkInfoService {

	@Autowired
	private JunkInfoDao junkInfoDao;

	@Override
	public int deleteByPrimaryKey(Integer id) {
		return junkInfoDao.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(JunkInfo junkInfo) {
		return junkInfoDao.insert(junkInfo);
	}

	@Override
	public JunkInfo selectByPrimaryKey(Integer id) {
		return junkInfoDao.selectByPrimaryKey(id);
	}
}
