package com.jrmf.service;

import com.jrmf.domain.LinkageTransferRecord;

import java.util.List;

public interface LinkageTransferRecordService {

    int insert(LinkageTransferRecord record);

    void addRechargeLinkageTransfer(String orderNo);

    int updateStatus(LinkageTransferRecord record);

    List<LinkageTransferRecord> getPayingList();

	List<LinkageTransferRecord> checkIsExistRecord(String customKey);

	List<LinkageTransferRecord> checkIsExistRecordByConfigId(Integer id);

    List<LinkageTransferRecord> getList(LinkageTransferRecord record);

}
