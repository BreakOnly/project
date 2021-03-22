package com.jrmf.persistence;


import com.jrmf.splitorder.domain.CustomSplitOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CustomSplitOrderDao {

    int insert(CustomSplitOrder record);

    int insertSelective(CustomSplitOrder record);

    CustomSplitOrder selectBySplitOrderNo(String splitOrderNo);

    int updateByPrimaryKeySelective(CustomSplitOrder record);

    int updateBySplitOrderNo(CustomSplitOrder record);

    String selectToDayAmountByCustomKey(String customKey);

    List<CustomSplitOrder> selectSplitOrder(Map<String, Object> params);
}