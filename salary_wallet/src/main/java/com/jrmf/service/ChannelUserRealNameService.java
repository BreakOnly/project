package com.jrmf.service;

import com.jrmf.domain.ChannelUser;
import com.jrmf.domain.ChannelUserRealName;

import java.util.List;
import java.util.Map;


public interface ChannelUserRealNameService {

    int deleteById(Integer id);

    int insert(ChannelUserRealName userRealName);

    int insertSelective(ChannelUserRealName userRealName);

    int updateByPrimaryKeySelective(ChannelUserRealName userRealName);

    int updateByPrimaryKey(ChannelUserRealName userRealName);

    List<ChannelUserRealName> selectAll(Map<String, Object> paramMap);

    Integer addOrUpdateUserRealName(ChannelUserRealName userRealName);

    int deleteByUserId(int userId);

    ChannelUserRealName  selectByCertId(String certId);
}
