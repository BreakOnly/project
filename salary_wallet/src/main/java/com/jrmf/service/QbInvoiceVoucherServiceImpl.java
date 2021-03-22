package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoiceReserve;
import com.jrmf.domain.QbInvoiceVoucher;
import com.jrmf.persistence.QbInvoiceVoucherDao;

@Service
public class QbInvoiceVoucherServiceImpl implements QbInvoiceVoucherService{
	
	@Autowired
	private QbInvoiceVoucherDao qbInvoiceVoucherDao;

	@Override
	public int deleteByPrimaryKey(Integer id) {
		return qbInvoiceVoucherDao.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(QbInvoiceVoucher record) {
		return qbInvoiceVoucherDao.insert(record);
	}

	@Override
	public QbInvoiceVoucher selectByPrimaryKey(Integer id) {
		return qbInvoiceVoucherDao.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKey(QbInvoiceVoucher record) {
		return qbInvoiceVoucherDao.updateByPrimaryKey(record);
	}

	@Override
	public int getAlreadyInvoiceCount(QbInvoiceReserve invoiceReserve) {
		return qbInvoiceVoucherDao.getAlreadyInvoiceCount(invoiceReserve);
	}

	@Override
	public List<Map<String, Object>> getInvoiceVoucherByPage(Page page) {
		return qbInvoiceVoucherDao.getInvoiceVoucherByPage(page);
	}

	@Override
	public int getInvoiceVoucherCount(Page page) {
		return qbInvoiceVoucherDao.getInvoiceVoucherCount(page);
	}

	@Override
	public List<QbInvoiceVoucher> getInvoiceVoucherBySerialNo(String serialNo) {
		return qbInvoiceVoucherDao.getInvoiceVoucherBySerialNo(serialNo);
	}

	@Override
	public void updateVoucher(QbInvoiceVoucher qbInvoiceVoucherUpdate) {
		qbInvoiceVoucherDao.updateVoucher(qbInvoiceVoucherUpdate);
	}

	@Override
	public void updateVoucherIsDelete(Map<String, Object> deleteParams) {
		qbInvoiceVoucherDao.updateVoucherIsDelete(deleteParams);
	}

}
