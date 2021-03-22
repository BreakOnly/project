package com.jrmf.splitorder.service;


import com.jrmf.splitorder.domain.CustomSplitOrder;
import com.jrmf.splitorder.domain.CustomSplitSuccessOrder;

import java.util.List;
import java.util.Map;

public interface CustomSplitSuccessOrderService {

    int insert(CustomSplitSuccessOrder record);

    int insertSelective(CustomSplitSuccessOrder record);

    CustomSplitSuccessOrder selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CustomSplitSuccessOrder record);

    int updateByPrimaryKey(CustomSplitSuccessOrder record);

    List<CustomSplitSuccessOrder> selectBySplitOrderNo(String splitOrderNo);

    List<CustomSplitSuccessOrder> selectAll(Map<String, Object> params);

    CustomSplitSuccessOrder selectBySplitOrderNoAndCompanyId(String splitOrderNo,String companyId);
}