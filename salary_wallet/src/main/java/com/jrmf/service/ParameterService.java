package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jrmf.domain.Parameter;

/**
 * filename：com.jrmf.service.ParameterService.java
 * 
 * @author: zhangyong
 * @time: 2013-10-11下午2:59:40
 */
@Service
public interface ParameterService {
	/**
	 * 添加
	 * 
	 * @param parameter
	 */
	public void saveParameter(Parameter parameter);

	/**
	 * 验证手机号,验证码十分钟有效
	 * 
	 * @param paramName
	 * @param paramValue
	 * @param paramFlag
	 * @return
	 */
	public Parameter valiMobiletelno(String paramName, String paramValue,
			String paramFlag);

	/**
	 * 变更验证次数
	 * 
	 * @param paramName
	 * @param paramFlag
	 * @param paramDate
	 */
	public void updateVailCount(String paramName, String paramFlag,
			String paramDate);




}
