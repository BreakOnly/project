package com.jrmf.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.CustomNotice;
import com.jrmf.persistence.CustomNoticeDao;

@Service
public class CustomNoticeServiceImpl implements CustomNoticeService{

	@Autowired
	private CustomNoticeDao customNoticeDao;
	
	@Override
	public void insertCustomNotice(CustomNotice customNotice) {
		customNoticeDao.insertCustomNotice(customNotice);
	}
	

}
