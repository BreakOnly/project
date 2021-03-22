package com.jrmf.splitorder.service;


import com.jrmf.domain.SplitOrderConf;
import com.jrmf.splitorder.domain.CustomSplitOrder;

import java.util.List;
import java.util.Map;

public interface CustomSplitOrderService {

    int insert(CustomSplitOrder record);

    int insertSelective(CustomSplitOrder record);

    CustomSplitOrder selectBySplitOrderNo(String splitOrderNo);

    int updateByPrimaryKeySelective(CustomSplitOrder record);

    int updateBySplitOrderNo(CustomSplitOrder record);

    String selectToDayAmountByCustomKey(String customKey);

    List<CustomSplitOrder> selectSplitOrder(Map<String, Object> params);
}