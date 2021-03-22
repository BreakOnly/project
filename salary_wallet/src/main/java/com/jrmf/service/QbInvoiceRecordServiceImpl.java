package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoiceRecord;
import com.jrmf.persistence.QbInvoiceRecordDao;

@Service
public class QbInvoiceRecordServiceImpl implements QbInvoiceRecordService{

	@Autowired
	private QbInvoiceRecordDao qbInvoiceRecordDao;
	
	@Override
	public int deleteByPrimaryKey(Integer id) {
		return qbInvoiceRecordDao.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(QbInvoiceRecord record) {
		return qbInvoiceRecordDao.insert(record);
	}

	@Override
	public QbInvoiceRecord selectByPrimaryKey(Integer id) {
		return qbInvoiceRecordDao.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKeySelective(QbInvoiceRecord record) {
		return qbInvoiceRecordDao.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(QbInvoiceRecord record) {
		return qbInvoiceRecordDao.updateByPrimaryKey(record);
	}

	@Override
	public int queryRecordListCount(Page page) {
		return qbInvoiceRecordDao.queryRecordListCount(page);
	}

	@Override
	public List<Map<String, Object>> queryRecordList(Page page) {
		return qbInvoiceRecordDao.queryRecordList(page);
	}

	@Override
	public List<Map<String, Object>> queryRecordListNoPage(Page page) {
		return qbInvoiceRecordDao.queryRecordListNoPage(page);
	}

	@Override
	public List<Integer> groupBillingClassByOrderNo(String orderNo) {
		return qbInvoiceRecordDao.groupBillingClassByOrderNo(orderNo);
	}

	@Override
	public QbInvoiceRecord getByInvoiceSerialNo(String invoiceSerialNo) {
		return qbInvoiceRecordDao.getByInvoiceSerialNo(invoiceSerialNo);
	}

	@Override
	public String getRecentAddress(String customkey) {
		return qbInvoiceRecordDao.getRecentAddress(customkey);
	}

    /**
     * 查询开票历史记录，包含图片
     */
    @Override
    public List<Map<String, Object>> queryRecordWithPicList(Page page) {
        return qbInvoiceRecordDao.queryRecordWithPicList(page);
    }

    /**
     * 查询开票历史记录条数，包含图片
     */
    @Override
    public List<Integer> queryRecordListWithPicCount(Page page) {
        return qbInvoiceRecordDao.queryRecordListWithPicCount(page);
    }

	@Override
	public List<QbInvoiceRecord> selectByPrimaryKeys(String ids) {
		return qbInvoiceRecordDao.selectByPrimaryKeys(ids);
	}

	@Override
	public QbInvoiceRecord getAdvanceInvoice(String customKey, String companyId) {
		return qbInvoiceRecordDao.getAdvanceInvoice(customKey,companyId);
	}


	@Override
	public int updateByRecharge(QbInvoiceRecord record) {
		return qbInvoiceRecordDao.updateByRecharge(record);
	}

	@Override
	public List<QbInvoiceRecord> getCommissionInvoiceList(Map<String, Object> params) {
		return qbInvoiceRecordDao.getCommissionInvoiceList(params);
	}
}
