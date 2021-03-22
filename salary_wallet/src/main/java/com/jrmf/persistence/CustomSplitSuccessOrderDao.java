package com.jrmf.persistence;


import com.jrmf.splitorder.domain.CustomSplitSuccessOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CustomSplitSuccessOrderDao {
    int insert(CustomSplitSuccessOrder record);

    int insertSelective(CustomSplitSuccessOrder record);

    CustomSplitSuccessOrder selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CustomSplitSuccessOrder record);

    int updateByPrimaryKey(CustomSplitSuccessOrder record);

    List<CustomSplitSuccessOrder> selectBySplitOrderNo(String splitOrderNo);

    List<CustomSplitSuccessOrder> selectAll(Map<String, Object> params);

    CustomSplitSuccessOrder selectBySplitOrderNoAndCompanyId(String splitOrderNo,String companyId);
}