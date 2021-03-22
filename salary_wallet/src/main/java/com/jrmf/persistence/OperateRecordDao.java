package com.jrmf.persistence;


import com.jrmf.domain.OperateRecord;
import com.jrmf.domain.OperateURL;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface OperateRecordDao {

    /**
     * 保存记录
     * @param operateRecord 记录
     */
    void addOperateRecord(OperateRecord operateRecord);

    /**
     *
     * @param params
     * @return
     */
    List<OperateURL> getURLList(Map<String, Object> params);
}