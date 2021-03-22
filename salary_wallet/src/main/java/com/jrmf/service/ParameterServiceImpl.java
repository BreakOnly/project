package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.Parameter;
import com.jrmf.persistence.ParameterDao;
import com.jrmf.utils.DateUtils;

/**
 * filename：com.jrmf.service.ParameterServiceImpl.java
 * 
 * @author: zhangyong
 * @time: 2013-10-11下午3:00:22
 */
@Service
public class ParameterServiceImpl implements ParameterService {
	@Autowired
	private ParameterDao parameterDao;

	@Override
    public void saveParameter(Parameter parameter) {
		String firstfrom = parameter.getFirstfrom();
		if (firstfrom != null) {
			// 如果太长进行截取
			int len = firstfrom.length();
			if (len >= 500) {
				len = 500;
			}
			firstfrom = firstfrom.substring(0, len);
			parameter.setFirstfrom(firstfrom);
		}
		parameterDao.saveParameter(parameter);
	}

    /**
	 * 验证手机号,验证码十分钟有效
	 * 
	 * @param paramName
	 * @param paramValue
	 * @param paramFlag
	 * @return
	 */
	@Override
    public Parameter valiMobiletelno(String paramName, String paramValue,
                                     String paramFlag) {
		String paramDate = DateUtils.getBeforeMinuteStr(10);
		updateVailCount(paramName, paramFlag, paramDate);
		return parameterDao.getParameterByValueDate(paramName, paramValue,
				paramFlag, paramDate);
	}

    @Override
	public void updateVailCount(String paramName, String paramFlag,
			String paramDate) {
		parameterDao.updateVailCount(paramName, paramFlag, paramDate);
	}

}
