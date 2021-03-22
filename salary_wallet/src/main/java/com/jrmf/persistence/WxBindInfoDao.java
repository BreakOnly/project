package com.jrmf.persistence;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.WxBindInfo;
@Mapper
public interface WxBindInfoDao {
	
	int saveWxBindInfo(WxBindInfo wxBindInfo);
	
	int updateWxBindInfo(WxBindInfo wxBindInfo);

	WxBindInfo geWxBindInfo(Map<String, Object> params);
}
