package com.jrmf.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.ReturnUrl;
import org.springframework.stereotype.Repository;

/** 
* @author 种路路 
* @version 创建时间：2017年9月3日 上午11:30:45 
* 类说明 
*/
@Mapper
public interface ReturnUrlDao {

	void addPasswordSetReturnUrl(ReturnUrl returnUrl);

	ReturnUrl getUrlReturn(@Param("id") int id);

	void addResetUrl(ReturnUrl returnUrl);

	void updateReturnUrl(ReturnUrl returnurl);

}
 