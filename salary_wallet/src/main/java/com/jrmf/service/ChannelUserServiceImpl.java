package com.jrmf.service;

import com.jrmf.domain.ChannelUser;
import com.jrmf.domain.UserCommission;
import com.jrmf.persistence.ChannelUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("channelUserService")
public class ChannelUserServiceImpl implements ChannelUserService {

    @Autowired
    private ChannelUserDao channelUserDao;

    @Override
    public int insert(ChannelUser user) {
        return channelUserDao.insert(user);
    }

    @Override
    public ChannelUser selectByPhoneNo(String phoneNo) {
        return channelUserDao.selectByPhoneNo(phoneNo);
    }

    @Override
    public ChannelUser selectByUserId(Integer id) {
        return channelUserDao.selectByUserId(id);
    }

    @Override
    public int updateByPrimaryKey(ChannelUser user) {
        return channelUserDao.updateByPrimaryKey(user);
    }

    @Override
    public List<String> selectCustomList(String certIds) {
        return channelUserDao.selectCustomList(certIds);
    }

    @Override
    public List<Map<String, String>> selectCompanyList(String certIds) {
        return channelUserDao.selectCompanyList(certIds);
    }

    @Override
    public List<UserCommission> selectUserCommissionList(Map<String, Object> paramMap) {
        return channelUserDao.selectUserCommissionList(paramMap);
    }

    @Override
    public Map<String, Object> selectUserCommissionCount(Map<String, Object> paramMap) {
        return channelUserDao.selectUserCommissionCount(paramMap);
    }

    @Override
    public void updatePassword(int id, String password) {
        channelUserDao.updatePassword(id, password);
    }

    @Override
    public ChannelUser selectByCertId(String certId) {
        return channelUserDao.selectByCertId(certId);
    }

    @Override
    public ChannelUser selectByCertIdAndPhoneNo(String str) {
        ChannelUser user = selectByPhoneNo(str);
        if (user == null) {
            user = selectByCertId(str);
        }
        return user;
    }

    @Override
    public List<Map<String, Object>> getAllList(Map<String, Object> paramMap) {
        return channelUserDao.getAllList(paramMap);
    }

    @Override
    public Integer addOrUpdateUser(ChannelUser user) {
        if (user.getId() != 0) {
            updateByPrimaryKey(user);
            return user.getId();
        } else {
            return insert(user);
        }
    }

    @Override
    public int deleteByUserId(int userId) {
        return channelUserDao.deleteByUserId(userId);
    }

}
