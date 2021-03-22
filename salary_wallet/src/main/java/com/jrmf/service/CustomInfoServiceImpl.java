package com.jrmf.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jrmf.domain.CustomInfo;
import com.jrmf.persistence.CustomInfoDao;


/**
 * 
* @author chonglulu  
*
 */
@Service("customInfoService")
public class CustomInfoServiceImpl implements CustomInfoService {
	
	@Autowired
	private CustomInfoDao customInfoDao;

	@Override
	public CustomInfo searchCustomInfoByKey(String customkey) {
		return customInfoDao.searchCustomInfoByKey(customkey);
	}

	@Override
	public void updateLoginKey(String merchantId, String loginKey) {
		customInfoDao.updateLoginKey( merchantId, loginKey);
	}

	@Override
	public void creatCustomInfo(CustomInfo customInfo) {
		customInfoDao.creatCustomInfo( customInfo);
	}

	@Override
	public void updateCustomInfo(CustomInfo customInfo) {
		customInfoDao.updateCustomInfo( customInfo);
	}

	@Override
	public List<CustomInfo> getAllActiveCustom() {
		return customInfoDao.getAllActiveCustom();
	}

	@Override
	public List<CustomInfo> getCustomByName(String customName) {
		return customInfoDao.getCustomByName(customName);
	}

}
