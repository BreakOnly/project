package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoiceRecord;
import com.jrmf.domain.QbInvoiceReserve;
import com.jrmf.persistence.QbInvoiceReserveDao;

@Service
public class QbInvoiceReserveServiceImpl implements QbInvoiceReserveService{
	
	@Autowired
	private QbInvoiceReserveDao qbInvoiceReserveDao;

	@Override
	public int deleteByPrimaryKey(Integer id) {
		return qbInvoiceReserveDao.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(QbInvoiceReserve record) {
		return qbInvoiceReserveDao.insert(record);
	}

	@Override
	public QbInvoiceReserve selectByPrimaryKey(Integer id) {
		return qbInvoiceReserveDao.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKeySelective(QbInvoiceReserve record) {
		return qbInvoiceReserveDao.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(QbInvoiceReserve record) {
		return qbInvoiceReserveDao.updateByPrimaryKey(record);
	}

	@Override
	public List<Map<String, Object>> getInvoiceReserveByPage(Page page) {
		return qbInvoiceReserveDao.getInvoiceReserveByPage(page);
	}

	@Override
	public int getInvoiceReserveCount(Page page) {
		return qbInvoiceReserveDao.getInvoiceReserveCount(page);
	}

	@Override
	public int checkIsExist(QbInvoiceReserve invoiceReserve) {
		return qbInvoiceReserveDao.checkIsExist(invoiceReserve);
	}

	@Override
	public QbInvoiceReserve getReserveByParams(QbInvoiceRecord invoiceRecord) {
		return qbInvoiceReserveDao.getReserveByParams(invoiceRecord);
	}

}
