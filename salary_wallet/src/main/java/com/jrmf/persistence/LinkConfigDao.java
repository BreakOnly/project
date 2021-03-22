package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.LinkageBaseConfig;
import com.jrmf.domain.Page;

@Mapper
public interface LinkConfigDao {
	
	List<Map<String, Object>> getLinkConfigList(Page page);
	
	int getLinkConfigListCount(Page page);
	
    int deleteByPrimaryKey(Integer id);

    int insert(LinkageBaseConfig record);

	LinkageBaseConfig getLinkConfigById(Integer id);

	int update(LinkageBaseConfig record);

	List<Map<String, String>> getPathInfo();
    

}
