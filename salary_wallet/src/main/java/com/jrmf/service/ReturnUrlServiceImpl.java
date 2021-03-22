package com.jrmf.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.ReturnUrl;
import com.jrmf.persistence.ReturnUrlDao;

/** 
* @author 种路路 
* @version 创建时间：2017年9月3日 上午11:29:31 
* 类说明 
*/
@Service("ReturnUrlService")
public class ReturnUrlServiceImpl implements ReturnUrlService {
	@Autowired
	private ReturnUrlDao returnUrlDao;
	@Override
	public void addPasswordSetReturnUrl(ReturnUrl returnUrl) {
		// TODO Auto-generated method stub
		returnUrlDao.addPasswordSetReturnUrl(returnUrl);
	}
	@Override
	public ReturnUrl getUrlReturn(int id) {
		// TODO Auto-generated method stub
		return returnUrlDao.getUrlReturn(id);
	}
	@Override
	public void addResetUrl(ReturnUrl returnUrl) {
		// TODO Auto-generated method stub
		returnUrlDao.addResetUrl( returnUrl);
	}
	@Override
	public void updateReturnUrl(ReturnUrl returnurl) {
		// TODO Auto-generated method stub
		returnUrlDao.updateReturnUrl( returnurl);
	}

}
 