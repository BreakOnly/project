package com.jrmf.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.jrmf.domain.CustomInfo;


/** 
* @author 种路路 
* @version 创建时间：2017年9月2日 下午3:31:20 
* 类说明 
*/
@Component
public interface CustomInfoService {

	public CustomInfo searchCustomInfoByKey(String customkey);

	public void updateLoginKey(String merchantId, String loginKey);

	public void creatCustomInfo(CustomInfo customInfo);

	public void updateCustomInfo(CustomInfo customInfo);

	public List<CustomInfo> getAllActiveCustom();
	
	public List<CustomInfo> getCustomByName(String customName);


}
 