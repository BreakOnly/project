package com.jrmf.persistence;

import com.jrmf.domain.ReceiptBatch;
import com.jrmf.domain.ReceiptCommission;
import com.jrmf.domain.ReceiptDownLoad;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReceiptDao {

    int addReceipt(Map<String, Object> params);

    List<ReceiptCommission> listReceiptCommission(Map<String, Object> params);

    ReceiptCommission getReceiptCommissionByReceiptNo(Map<String, Object> params);

    List<ReceiptBatch> listReceiptBatch(Map<String, Object> params);

    ReceiptBatch getReceiptBatchById(@Param("id")Integer id);

	List<ReceiptBatch> listReceiptBatchGroup(Map<String, Object> params);

	int saveReceiptBatch(ReceiptBatch receiptBatch);

	int updateReceiptBatch(ReceiptBatch receiptBatch);

	int updateReceiptCommission(Map<String, Object> receiptCommission);

	int updateReceiptCommissionById(Map<String, Object> receiptCommission);

	int updateReceiptCommissionByReceiptNo(Map<String, Object> receiptCommission);

    List<String> listReceiptCommissionPath(Map<String, Object> param);

    List<ReceiptDownLoad> listDownloadHistory(Map<String, Object> params);

    int addReceiptDownload(ReceiptDownLoad receiptDownLoad);

    int updateStatusReceiptDownloadById(@Param("status") Integer status,@Param("statusDesc") String statusDesc,@Param("id") Integer id);

    int listReceiptCommissionCount(Map<String, Object> params);

  String getReceiptCommissionByOrderNo(String orderNo);
}
