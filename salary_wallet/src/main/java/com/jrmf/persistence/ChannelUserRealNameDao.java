package com.jrmf.persistence;


import com.jrmf.domain.ChannelUserRealName;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChannelUserRealNameDao {
    int deleteById(Integer id);

    int insert(ChannelUserRealName userRealName);

    int insertSelective(ChannelUserRealName userRealName);

    int updateByPrimaryKeySelective(ChannelUserRealName userRealName);

    int updateByPrimaryKey(ChannelUserRealName userRealName);

    List<ChannelUserRealName> selectAll(Map<String, Object> paramMap);

    int deleteByUserId(int userId);

    ChannelUserRealName  selectByCertId(String certId);
}