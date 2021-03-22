package com.jrmf.persistence;

import com.jrmf.domain.LdOrderCorrect;
import com.jrmf.domain.Page;
import java.util.List;
import java.util.Map;

import com.jrmf.interceptor.InterceptJobServiceAnnotation;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface LdOrderCorrectDao {

    int insert(LdOrderCorrect record);

    int updateByPrimaryKeySelective(LdOrderCorrect record);

    int updateByPrimaryKey(LdOrderCorrect record);

	int queryLdCorrectOrderDetailListCount(Page page);

	List<Map<String, Object>> queryLdCorrectOrderDetailList(Page page);

	List<LdOrderCorrect> getLdCorrectListByTypeAndStatus();

    @InterceptJobServiceAnnotation
    List<LdOrderCorrect> getLdCorrectListByTypeAndStatusOnJob();
}