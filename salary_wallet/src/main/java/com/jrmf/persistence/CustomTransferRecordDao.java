package com.jrmf.persistence;

import com.jrmf.domain.CustomTransferRecord;
import com.jrmf.domain.OemConfig;
import com.jrmf.domain.Page;

import com.jrmf.interceptor.InterceptJobServiceAnnotation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CustomTransferRecordDao {
    int deleteByPrimaryKey(Integer id);

    int insert(CustomTransferRecord record);

    int insertWithPathNo(CustomTransferRecord record);

    CustomTransferRecord selectByPrimaryKey(Integer id);

    List<CustomTransferRecord> selectByPrimaryKeys(String selectByPrimaryKeys);

    int updateByPrimaryKey(CustomTransferRecord record);

    List<CustomTransferRecord> getToBeConfirmedRecord();

    @InterceptJobServiceAnnotation
    List<CustomTransferRecord> getSyncBalanceList();

    int updateState(String bizFlowNo,Integer state);

	List<Map<String, Object>> getSubTransRecordListByPage(Page page);

	Integer getSubTransRecordListCount(Page page);

	List<Map<String, Object>> getSubTransRecordListNoPage(Page page);

    List<CustomTransferRecord> getCustomTransferRecordByParam(Map<String, Object> param);


}