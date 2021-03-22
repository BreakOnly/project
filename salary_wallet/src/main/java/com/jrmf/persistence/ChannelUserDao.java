package com.jrmf.persistence;

import com.jrmf.domain.ChannelUser;
import com.jrmf.domain.UserCommission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChannelUserDao {

    int insert(ChannelUser user);

    ChannelUser selectByPhoneNo(String phoneNo);

    int updateByPrimaryKey(ChannelUser user);

    List<String> selectCustomList(String certIds);

    List<Map<String,String>> selectCompanyList(String certIds);

    List<UserCommission> selectUserCommissionList(Map<String, Object> paramMap);

    Map<String,Object> selectUserCommissionCount(Map<String, Object> paramMap);

    void updatePassword(int id, String password);

    ChannelUser selectByCertId(String certId);

    List<Map<String,Object>> getAllList(Map<String, Object> paramMap);

    ChannelUser selectByUserId(Integer id);

    int deleteByUserId(int userId);

}