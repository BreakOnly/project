package com.jrmf.persistence;

import com.jrmf.domain.ApplyBatchInvoice;
import com.jrmf.domain.BatchInvoiceAssociation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BatchInvoiceAssociationDao {
    int insert(BatchInvoiceAssociation invoiceAssociation);
    List<BatchInvoiceAssociation> findBatchInvoiceAssociationList(BatchInvoiceAssociation invoiceAssociation);
    List<BatchInvoiceAssociation> findInvoiceCustomAssociationList(BatchInvoiceAssociation invoiceAssociation);
    List<BatchInvoiceAssociation> findInvoiceCustomList(BatchInvoiceAssociation invoiceAssociation);
    int batchInsert(List<BatchInvoiceAssociation> list);
    int batchInsertCustom(List<BatchInvoiceAssociation> list);
    int updateCommissionStatus(List<BatchInvoiceAssociation> list);
}
