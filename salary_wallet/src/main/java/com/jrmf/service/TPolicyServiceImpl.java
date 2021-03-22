package com.jrmf.service;

import com.jrmf.domain.ChannelAreas;
import com.jrmf.domain.TPolicy;
import com.jrmf.domain.TPolicyGroup;
import com.jrmf.persistence.TPolicyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TPolicyServiceImpl implements TPolicyService {

    @Autowired
    private TPolicyDao tPolicyDao;


    @Override
    public int deleteByPrimaryKey(Integer id) {
        return tPolicyDao.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(TPolicy record) {
        return tPolicyDao.insert(record);
    }

    @Override
    public int insertSelective(TPolicy record) {
        return tPolicyDao.insertSelective(record);
    }

    @Override
    public List<Map<String, Object>> selectByExample(TPolicy record) {
        return tPolicyDao.selectByExample(record);
    }


    @Override
    public TPolicy selectByPrimaryKey(Integer id) {
        return tPolicyDao.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(TPolicy record) {
        return tPolicyDao.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(TPolicy record) {
        return tPolicyDao.updateByPrimaryKey(record);
    }

    @Override
    public List<ChannelAreas> selectAreaByRootCode(String parentCode) {
        return tPolicyDao.selectAreaByRootCode(parentCode);
    }

    @Override
    public List<Map<String, Object>> selectPolicyTypeByCode(String parentCode) {
        return tPolicyDao.selectPolicyTypeByCode(parentCode);
    }

    @Override
    public List<Map<String, Object>> selectH5ListByType(Integer type, String keyword) {
        return tPolicyDao.selectH5ListByType(type, keyword);
    }

    @Override
    public List<TPolicyGroup> selectH5ListByArea(String keyword, String areaCode, Integer parentId) {
        return tPolicyDao.selectH5ListByArea(keyword, areaCode, parentId);
    }

    @Override
    public int updateVisitsCount(Integer id) {
        return tPolicyDao.updateVisitsCount(id);
    }

    @Override
    public List<String> selectPolicyTypeStr(Integer parentCode) {
        return tPolicyDao.selectPolicyTypeStr(parentCode);
    }
}
 