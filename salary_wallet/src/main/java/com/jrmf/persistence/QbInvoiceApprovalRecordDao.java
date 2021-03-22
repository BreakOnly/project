package com.jrmf.persistence;


import com.jrmf.domain.QbInvoiceApprovalRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QbInvoiceApprovalRecordDao {
    int deleteByPrimaryKey(Integer id);

    int insert(QbInvoiceApprovalRecord record);

    int insertSelective(QbInvoiceApprovalRecord record);

    QbInvoiceApprovalRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(QbInvoiceApprovalRecord record);

    int updateByPrimaryKey(QbInvoiceApprovalRecord record);
}