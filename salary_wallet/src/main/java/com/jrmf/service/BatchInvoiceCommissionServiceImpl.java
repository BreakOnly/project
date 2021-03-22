package com.jrmf.service;

import com.jrmf.domain.BatchInvoiceCommission;
import com.jrmf.domain.dto.BatchInvoiceCommissionDTO;
import com.jrmf.persistence.BatchInvoiceCommissionDao;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("batchInvoiceCommissionService")
public class BatchInvoiceCommissionServiceImpl implements BatchInvoiceCommissionService{

  @Autowired
  private BatchInvoiceCommissionDao batchInvoiceCommissionDao;

  @Override
  public void insert(BatchInvoiceCommission batchInvoiceCommission) {
    batchInvoiceCommissionDao.insert(batchInvoiceCommission);
  }

  @Override
  public List<BatchInvoiceCommission> listBatchInvoiceCommission(
      BatchInvoiceCommissionDTO batchInvoiceCommissionDTO) {
    return batchInvoiceCommissionDao.listBatchInvoiceCommission(batchInvoiceCommissionDTO);
  }

  @Override
  public void updateByOrderNo(BatchInvoiceCommission batchInvoiceCommission) {
    batchInvoiceCommissionDao.updateByOrderNo(batchInvoiceCommission);
  }

  @Override
  public BatchInvoiceCommission getBatchInvoiceCommissionByOrderNo(String orderNo) {
    return batchInvoiceCommissionDao.getBatchInvoiceCommissionByOrderNo(orderNo);
  }
}
