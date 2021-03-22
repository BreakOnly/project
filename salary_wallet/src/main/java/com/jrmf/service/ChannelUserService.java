package com.jrmf.service;

import com.jrmf.domain.ChannelUser;
import com.jrmf.domain.UserCommission;

import java.util.List;
import java.util.Map;

public interface ChannelUserService {

    int insert(ChannelUser user);

    ChannelUser selectByPhoneNo(String phoneNo);

    ChannelUser selectByUserId(Integer id);

    int updateByPrimaryKey(ChannelUser user);

    List<String> selectCustomList(String certIds);

    List<Map<String,String>> selectCompanyList(String certIds);

    List<UserCommission> selectUserCommissionList(Map<String, Object> paramMap);

    Map<String, Object> selectUserCommissionCount(Map<String, Object> paramMap);

    void updatePassword(int id, String password);

    ChannelUser selectByCertId(String certId);

    ChannelUser selectByCertIdAndPhoneNo(String str);

    List<Map<String,Object>> getAllList(Map<String, Object> paramMap);

    Integer addOrUpdateUser(ChannelUser user);

    int deleteByUserId(int userId);


}
