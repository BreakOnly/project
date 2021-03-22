package com.jrmf.service;


import com.jrmf.domain.QbInvoiceApprovalRecord;
import com.jrmf.persistence.QbInvoiceApprovalRecordDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QbInvoiceApprovalRecordServiceImpl implements QbInvoiceApprovalRecordService {

    @Autowired
    private QbInvoiceApprovalRecordDao invoiceApprovalRecordDao;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return invoiceApprovalRecordDao.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(QbInvoiceApprovalRecord record) {
        return invoiceApprovalRecordDao.insert(record);
    }

    @Override
    public int insertSelective(QbInvoiceApprovalRecord record) {
        return invoiceApprovalRecordDao.insertSelective(record);
    }

    @Override
    public QbInvoiceApprovalRecord selectByPrimaryKey(Integer id) {
        return invoiceApprovalRecordDao.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(QbInvoiceApprovalRecord record) {
        return invoiceApprovalRecordDao.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(QbInvoiceApprovalRecord record) {
        return invoiceApprovalRecordDao.updateByPrimaryKey(record);
    }
}