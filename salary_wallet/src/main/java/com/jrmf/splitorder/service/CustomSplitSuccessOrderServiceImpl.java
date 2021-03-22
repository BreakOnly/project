package com.jrmf.splitorder.service;

import com.jrmf.persistence.CustomSplitSuccessOrderDao;
import com.jrmf.splitorder.domain.CustomSplitSuccessOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CustomSplitSuccessOrderServiceImpl implements CustomSplitSuccessOrderService {

    @Autowired
    private CustomSplitSuccessOrderDao customSplitSuccessOrderDao;


    @Override
    public int insert(CustomSplitSuccessOrder record) {
        return customSplitSuccessOrderDao.insert(record);
    }

    @Override
    public int insertSelective(CustomSplitSuccessOrder record) {
        return customSplitSuccessOrderDao.insertSelective(record);
    }

    @Override
    public CustomSplitSuccessOrder selectByPrimaryKey(Integer id) {
        return customSplitSuccessOrderDao.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(CustomSplitSuccessOrder record) {
        return customSplitSuccessOrderDao.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(CustomSplitSuccessOrder record) {
        return customSplitSuccessOrderDao.updateByPrimaryKey(record);
    }

    @Override
    public List<CustomSplitSuccessOrder> selectBySplitOrderNo(String splitOrderNo) {
        return customSplitSuccessOrderDao.selectBySplitOrderNo(splitOrderNo);
    }

    @Override
    public List<CustomSplitSuccessOrder> selectAll(Map<String, Object> params) {
        return customSplitSuccessOrderDao.selectAll(params);
    }

    @Override
    public CustomSplitSuccessOrder selectBySplitOrderNoAndCompanyId(String splitOrderNo, String companyId) {
        return customSplitSuccessOrderDao.selectBySplitOrderNoAndCompanyId(splitOrderNo, companyId);
    }
}
