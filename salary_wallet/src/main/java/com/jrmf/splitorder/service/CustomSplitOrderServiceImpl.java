package com.jrmf.splitorder.service;

import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.persistence.CustomDao;
import com.jrmf.persistence.CustomSplitOrderDao;
import com.jrmf.splitorder.domain.Custom;
import com.jrmf.splitorder.domain.CustomSplitOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CustomSplitOrderServiceImpl implements CustomSplitOrderService {

    @Autowired
    private CustomSplitOrderDao customSplitOrderDao;

    @Override
    public int insert(CustomSplitOrder record) {
        return customSplitOrderDao.insert(record);
    }

    @Override
    public int insertSelective(CustomSplitOrder record) {
        return customSplitOrderDao.insertSelective(record);
    }

    @Override
    public CustomSplitOrder selectBySplitOrderNo(String splitOrderNo) {
        return customSplitOrderDao.selectBySplitOrderNo(splitOrderNo);
    }

    @Override
    public int updateByPrimaryKeySelective(CustomSplitOrder record) {
        return customSplitOrderDao.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateBySplitOrderNo(CustomSplitOrder record) {
        return customSplitOrderDao.updateBySplitOrderNo(record);
    }

    @Override
    public String selectToDayAmountByCustomKey(String customKey) {
        return customSplitOrderDao.selectToDayAmountByCustomKey(customKey);
    }

    @Override
    public List<CustomSplitOrder> selectSplitOrder(Map<String, Object> params) {
        return customSplitOrderDao.selectSplitOrder(params);
    }
}
