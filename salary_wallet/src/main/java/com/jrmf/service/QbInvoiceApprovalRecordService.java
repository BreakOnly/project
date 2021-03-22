package com.jrmf.service;


import com.jrmf.domain.QbInvoiceApprovalRecord;

public interface QbInvoiceApprovalRecordService {
    int deleteByPrimaryKey(Integer id);

    int insert(QbInvoiceApprovalRecord record);

    int insertSelective(QbInvoiceApprovalRecord record);

    QbInvoiceApprovalRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(QbInvoiceApprovalRecord record);

    int updateByPrimaryKey(QbInvoiceApprovalRecord record);
}