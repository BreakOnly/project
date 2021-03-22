package com.jrmf.service;

import com.jrmf.domain.LdOrderCorrect;
import com.jrmf.domain.Page;
import java.util.List;
import java.util.Map;

public interface LdOrderCorrectService {

    int insert(LdOrderCorrect record);

    int updateByPrimaryKeySelective(LdOrderCorrect record);

    int updateByPrimaryKey(LdOrderCorrect record);

	int queryLdCorrectOrderDetailListCount(Page page);

	List<Map<String, Object>> queryLdCorrectOrderDetailList(Page page);

	List<LdOrderCorrect> getLdCorrectListByTypeAndStatus();

    List<LdOrderCorrect> getLdCorrectListByTypeAndStatusOnJob();
}