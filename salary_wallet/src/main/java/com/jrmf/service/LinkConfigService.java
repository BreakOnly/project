package com.jrmf.service;

import java.util.List;
import java.util.Map;

import com.jrmf.domain.LinkageBaseConfig;
import com.jrmf.domain.Page;

public interface LinkConfigService {

	List<Map<String, Object>> getLinkConfigList(Page page);
	
	int getLinkConfigListCount(Page page);
	
    int deleteByPrimaryKey(Integer id);

    int insert(LinkageBaseConfig record);
    
    int update(LinkageBaseConfig record);

	LinkageBaseConfig getLinkConfigById(Integer id);

	List<Map<String, String>> getPathInfo();
}
