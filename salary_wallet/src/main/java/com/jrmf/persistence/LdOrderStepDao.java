package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.LdOrderStep;
import com.jrmf.domain.Page;

@Mapper
public interface LdOrderStepDao {

	void insert(LdOrderStep ldOrderStep);

	void update(LdOrderStep ldOrderStep);

	List<LdOrderStep> getList(String orderNo);

	int getCountByOrderNo(String orderno);

	int getCountSuccessByOrderNo(String orderno);

	int getCountFailByOrderNo(String orderno);

	int queryLdStepOrderDetailListCount(Page page);

	List<Map<String, Object>> queryLdStepOrderDetailList(Page page);

	LdOrderStep getOrderStep(String stepOrderNo);

	void updateById(LdOrderStep ldOrderStepDetail);

	LdOrderStep getPreStepOrder(Map<String, Object> params);

}