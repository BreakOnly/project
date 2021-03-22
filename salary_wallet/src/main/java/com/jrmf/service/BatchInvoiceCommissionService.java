package com.jrmf.service;

import com.jrmf.domain.BatchInvoiceCommission;
import com.jrmf.domain.dto.BatchInvoiceCommissionDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface BatchInvoiceCommissionService {

  void insert(BatchInvoiceCommission batchInvoiceCommission);

  List<BatchInvoiceCommission> listBatchInvoiceCommission(
      BatchInvoiceCommissionDTO batchInvoiceCommissionDTO);

  void updateByOrderNo(BatchInvoiceCommission batchInvoiceCommission);

  BatchInvoiceCommission getBatchInvoiceCommissionByOrderNo(String orderNo);
}
