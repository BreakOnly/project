package com.jrmf.payment.service;

import com.jrmf.domain.ChannelInterimBatch;
import com.jrmf.utils.dto.InputBatchData;
import java.util.List;
import java.util.Map;

public interface ConfirmGrantService2 {

    public Map<String, Object> grantTransfer(String originalId, String companyId, String batchId, String remark, String operatorName, String realCompanyId);

    public Map<String, Object> inputCommissionData(int payType, List<InputBatchData> inputBatchData, Map<String, String> batchData);

    public boolean checkRepeatCommission(String batchId, String originalId, String companyId);

//    Map<String, Object> ymGrantTransferBefore(String originalId, String companyId, String batchId, String remark, String operatorName, String phone,String realCompanyId);

    Map<String, Object> ymGrantTransferAfter(ChannelInterimBatch batch, String code, String operatorName, String customName, String companyName);

    void unlockBatch(ChannelInterimBatch batch);

    String syncBatch(ChannelInterimBatch batch, String orderNo, String customName, String companyName, String operatorName, String processId);

    void verifyCompanyAndSendMq(String batchId);

    String lockCommissionUsers(String batchId);

    void unLockCommissionUsers(String batchId);
}
