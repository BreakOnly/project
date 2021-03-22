package com.jrmf.service;

import org.springframework.stereotype.Service;

import com.jrmf.domain.ReturnUrl;

/** 
* @author 种路路 
* @version 创建时间：2017年9月3日 上午11:25:33 
* 类说明 
*/
@Service
public interface ReturnUrlService {

	void addPasswordSetReturnUrl(ReturnUrl returnUrl);

	ReturnUrl getUrlReturn(int id);

	void addResetUrl(ReturnUrl returnUrl);

	void updateReturnUrl(ReturnUrl returnurl);

}
 