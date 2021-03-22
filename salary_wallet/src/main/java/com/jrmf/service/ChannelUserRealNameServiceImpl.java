package com.jrmf.service;

import com.jrmf.domain.ChannelUserRealName;
import com.jrmf.persistence.ChannelUserRealNameDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChannelUserRealNameServiceImpl implements ChannelUserRealNameService {

    @Autowired
    private ChannelUserRealNameDao userRealNameDao;


    @Override
    public int deleteById(Integer id) {
        return userRealNameDao.deleteById(id);
    }

    @Override
    public int insert(ChannelUserRealName userRealName) {
        return userRealNameDao.insert(userRealName);
    }

    @Override
    public int insertSelective(ChannelUserRealName userRealName) {
        return userRealNameDao.insertSelective(userRealName);
    }

    @Override
    public int updateByPrimaryKeySelective(ChannelUserRealName userRealName) {
        return userRealNameDao.updateByPrimaryKeySelective(userRealName);
    }

    @Override
    public int updateByPrimaryKey(ChannelUserRealName userRealName) {
        return userRealNameDao.updateByPrimaryKey(userRealName);
    }

    @Override
    public List<ChannelUserRealName> selectAll(Map<String, Object> paramMap) {
        return userRealNameDao.selectAll(paramMap);
    }

    @Override
    public Integer addOrUpdateUserRealName(ChannelUserRealName userRealName) {
        if (userRealName.getId() == null) {
            return insert(userRealName);
        } else {
            updateByPrimaryKey(userRealName);
            return userRealName.getId();
        }
    }

    @Override
    public int deleteByUserId(int userId) {
        return userRealNameDao.deleteByUserId(userId);
    }

    @Override
    public ChannelUserRealName selectByCertId(String certId) {
        return userRealNameDao.selectByCertId(certId);
    }
}
