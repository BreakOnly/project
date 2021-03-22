package com.jrmf.service;

import com.jrmf.domain.CustomTransferRecord;
import com.jrmf.domain.Page;

import java.util.List;
import java.util.Map;


public interface CustomTransferRecordService {
    int deleteByPrimaryKey(Integer id);

    int insert(CustomTransferRecord record);

    int insertWithPathNo(CustomTransferRecord record);

    CustomTransferRecord selectByPrimaryKey(Integer id);

    List<CustomTransferRecord> selectByPrimaryKeys(String selectByPrimaryKeys);

    int updateByPrimaryKey(CustomTransferRecord record);

    List<CustomTransferRecord> getToBeConfirmedRecord();

    List<CustomTransferRecord> getSyncBalanceList();

    int updateState(String bizFlowNo,Integer state);

	List<Map<String, Object>> getSubTransRecordListByPage(Page page);

	Integer getSubTransRecordListCount(Page page);

	List<Map<String, Object>> getSubTransRecordListNoPage(Page page);

    List<CustomTransferRecord> getCustomTransferRecordByParam(Map<String, Object> param);

}