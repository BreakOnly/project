package com.jrmf.persistence;

import com.jrmf.domain.BatchInvoiceCommission;
import com.jrmf.domain.dto.BatchInvoiceCommissionDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BatchInvoiceCommissionDao {

  void insert(BatchInvoiceCommission batchInvoiceCommission);

  List<BatchInvoiceCommission> listBatchInvoiceCommission(
      BatchInvoiceCommissionDTO batchInvoiceCommissionDTO);

  void updateByOrderNo(BatchInvoiceCommission batchInvoiceCommission);

  void updateStatusById(BatchInvoiceCommission batchInvoiceCommission);

  BatchInvoiceCommission getBatchInvoiceCommissionByOrderNo(String orderNo);
}
