package com.jrmf.persistence;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import com.jrmf.domain.LinkAccountTrans;
import com.jrmf.domain.Page;

@Mapper
public interface LinkAccountTransDao {

    int insert(LinkAccountTrans record);
    
	List<Map<String, Object>> getLinkAccountTransList(Page page);
	
	int getLinkAccountTransListCount(Page page);
	
	List<Map<String, String>> getLinkAccountTransListNoPage(Page page);
}