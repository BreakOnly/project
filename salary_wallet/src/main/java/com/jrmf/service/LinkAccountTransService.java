package com.jrmf.service;

import java.util.List;
import java.util.Map;
import com.jrmf.domain.LinkAccountTrans;
import com.jrmf.domain.Page;

public interface LinkAccountTransService {
	
    int insert(LinkAccountTrans record);
    
	List<Map<String, Object>> getLinkAccountTransList(Page page);
	
	int getLinkAccountTransListCount(Page page);
	
	List<Map<String, String>> getLinkAccountTransListNoPage(Page page);

}
