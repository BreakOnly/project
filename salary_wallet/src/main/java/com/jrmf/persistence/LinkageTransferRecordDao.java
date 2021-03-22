package com.jrmf.persistence;

import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.interceptor.InterceptJobServiceAnnotation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LinkageTransferRecordDao {

    int insert(LinkageTransferRecord record);

    int updateStatus(LinkageTransferRecord record);

    @InterceptJobServiceAnnotation
    List<LinkageTransferRecord> getPayingList();

	List<LinkageTransferRecord> checkIsExistRecord(String customKey);

	List<LinkageTransferRecord> checkIsExistRecordByConfigId(Integer id);

    List<LinkageTransferRecord> getList(LinkageTransferRecord record);

}