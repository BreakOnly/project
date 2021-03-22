package com.jrmf.service;

import com.jrmf.domain.QbInvoicePic;
import com.jrmf.persistence.QbInvoicePicDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class QbInvoicePicServiceImpl implements QbInvoicePicService{

	@Autowired
	private QbInvoicePicDao qbInvoicePicDao;

	@Override
	public int deleteByPrimaryKey(Integer id) {
		return qbInvoicePicDao.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(QbInvoicePic record) {
		return qbInvoicePicDao.insert(record);
	}

	@Override
	public QbInvoicePic selectByPrimaryKey(Integer id) {
		return qbInvoicePicDao.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKeySelective(QbInvoicePic record) {
		return qbInvoicePicDao.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(QbInvoicePic record) {
		return qbInvoicePicDao.updateByPrimaryKey(record);
	}

	@Override
	public List<QbInvoicePic> getPicListBySerialNo(String invoiceSerialNo) {
		return qbInvoicePicDao.getPicListBySerialNo(invoiceSerialNo);
	}

}
