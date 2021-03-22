package com.jrmf.service;

import com.jrmf.domain.CustomTransferRecord;
import com.jrmf.domain.Page;
import com.jrmf.persistence.CustomTransferRecordDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CustomTransferRecordServiceImpl implements CustomTransferRecordService {

    @Autowired
    private CustomTransferRecordDao recordDao;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return recordDao.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(CustomTransferRecord record) {
        return recordDao.insert(record);
    }
    @Override
    public int insertWithPathNo(CustomTransferRecord record) {
        return recordDao.insertWithPathNo(record);
    }

    @Override
    public CustomTransferRecord selectByPrimaryKey(Integer id) {
        return recordDao.selectByPrimaryKey(id);
    }

    @Override
    public List<CustomTransferRecord> selectByPrimaryKeys(String selectByPrimaryKeys) {
        return recordDao.selectByPrimaryKeys(selectByPrimaryKeys);
    }

    @Override
    public int updateByPrimaryKey(CustomTransferRecord record) {
        return recordDao.updateByPrimaryKey(record);
    }

    @Override
    public List<CustomTransferRecord> getToBeConfirmedRecord() {
        return recordDao.getToBeConfirmedRecord();
    }

    @Override
    public List<CustomTransferRecord> getSyncBalanceList() {
        return recordDao.getSyncBalanceList();
    }

    @Override
    public int updateState(String bizFlowNo, Integer state) {
        return recordDao.updateState(bizFlowNo, state);
    }

    @Override
    public List<Map<String, Object>> getSubTransRecordListByPage(Page page) {
        return recordDao.getSubTransRecordListByPage(page);
    }

    @Override
    public Integer getSubTransRecordListCount(Page page) {
        return recordDao.getSubTransRecordListCount(page);
    }

    @Override
    public List<Map<String, Object>> getSubTransRecordListNoPage(Page page) {
        return recordDao.getSubTransRecordListNoPage(page);
    }

    @Override
    public List<CustomTransferRecord> getCustomTransferRecordByParam(Map<String, Object> param) {
        return recordDao.getCustomTransferRecordByParam(param);
    }
}