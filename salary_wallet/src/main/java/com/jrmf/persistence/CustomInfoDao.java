package com.jrmf.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.CustomInfo;
import org.springframework.stereotype.Repository;


/**
* @author 种路路 
* @version 创建时间：2017年9月2日 下午3:33:27 
* 类说明 
*/

@Mapper
public interface CustomInfoDao {
	public CustomInfo searchCustomInfoByKey(@Param("customkey") String customkey);

	public void updateLoginKey(@Param("customkey") String merchantId,@Param("loginKey") String loginKey);

	public void creatCustomInfo(CustomInfo customInfo);

	public void updateCustomInfo(CustomInfo customInfo);

	public List<CustomInfo> getAllActiveCustom();
	
	public List<CustomInfo> getCustomByName(@Param("customname")String customname);
}
 