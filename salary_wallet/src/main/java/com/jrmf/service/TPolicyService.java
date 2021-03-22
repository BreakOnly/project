package com.jrmf.service;

import com.jrmf.domain.ChannelAreas;
import com.jrmf.domain.TPolicy;
import com.jrmf.domain.TPolicyGroup;

import java.util.List;
import java.util.Map;

public interface TPolicyService {
    int deleteByPrimaryKey(Integer id);

    int insert(TPolicy record);

    int insertSelective(TPolicy record);

    List<Map<String, Object>> selectByExample(TPolicy record);

    TPolicy selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TPolicy record);

    int updateByPrimaryKey(TPolicy record);

    List<ChannelAreas> selectAreaByRootCode(String parentCode);

    List<Map<String, Object>> selectPolicyTypeByCode(String parentCode);

    List<Map<String, Object>> selectH5ListByType(Integer type, String keyword);

    List<TPolicyGroup> selectH5ListByArea(String keyword, String areaCode, Integer parentId);

    int updateVisitsCount(Integer id);

    List<String> selectPolicyTypeStr(Integer parentCode);

}
