package com.jrmf.service;

import com.jrmf.domain.BatchInvoiceAssociation;

import java.util.List;

public interface BatchInvoiceAssociationService {
    int insert(BatchInvoiceAssociation invoiceAssociation);
    List<BatchInvoiceAssociation> findBatchInvoiceAssociationList(BatchInvoiceAssociation invoiceAssociation);
    List<BatchInvoiceAssociation> findInvoiceCustomAssociationList(BatchInvoiceAssociation invoiceAssociation);
    void batchPush(Integer id);
    List<BatchInvoiceAssociation> findInvoiceCustomList(BatchInvoiceAssociation invoiceAssociation);
}
