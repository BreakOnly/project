package com.jrmf.service;

import java.util.List;
import java.util.Map;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoiceReserve;
import com.jrmf.domain.QbInvoiceVoucher;

public interface QbInvoiceVoucherService {
	
    int deleteByPrimaryKey(Integer id);

    int insert(QbInvoiceVoucher record);

    QbInvoiceVoucher selectByPrimaryKey(Integer id);

    int updateByPrimaryKey(QbInvoiceVoucher record);

	int getAlreadyInvoiceCount(QbInvoiceReserve invoiceReserve);

	List<Map<String, Object>> getInvoiceVoucherByPage(Page page);

	int getInvoiceVoucherCount(Page page);

	List<QbInvoiceVoucher> getInvoiceVoucherBySerialNo(String serialNo);

	void updateVoucher(QbInvoiceVoucher qbInvoiceVoucherUpdate);

	void updateVoucherIsDelete(Map<String, Object> deleteParams);
}