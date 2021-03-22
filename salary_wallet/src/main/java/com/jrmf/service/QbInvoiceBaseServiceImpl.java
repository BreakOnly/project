package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoiceBase;
import com.jrmf.persistence.QbInvoiceBaseDao;
@Service
public class QbInvoiceBaseServiceImpl implements QbInvoiceBaseService{

	@Autowired
	private QbInvoiceBaseDao qbInvoiceBaseDao;
	
	@Override
	public int deleteByPrimaryKey(Integer id) {
		return qbInvoiceBaseDao.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(QbInvoiceBase record) {
		return qbInvoiceBaseDao.insert(record);
	}

	@Override
	public int insertSelective(QbInvoiceBase record) {
		return qbInvoiceBaseDao.insertSelective(record);
	}

	@Override
	public QbInvoiceBase selectByPrimaryKey(Integer id) {
		return qbInvoiceBaseDao.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKeySelective(QbInvoiceBase record) {
		return qbInvoiceBaseDao.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(QbInvoiceBase record) {
		return qbInvoiceBaseDao.updateByPrimaryKey(record);
	}

	@Override
	public int queryInvoiceBaseListCount(Page page) {
		return qbInvoiceBaseDao.queryInvoiceBaseListCount(page);
	}

	@Override
	public List<Map<String, Object>> queryInvoiceBaseList(Page page) {
		return qbInvoiceBaseDao.queryInvoiceBaseList(page);
	}

	@Override
	public int queryInvoiceClassInfoListCount(Page page) {
		return qbInvoiceBaseDao.queryInvoiceClassInfoListCount(page);
	}

	@Override
	public List<Map<String, Object>> queryInvoiceClassInfoList(Page page) {
		return qbInvoiceBaseDao.queryInvoiceClassInfoList(page);
	}

	@Override
	public String queryServiceContent(Integer id) {
		return qbInvoiceBaseDao.queryServiceContent(id);
	}

	@Override
	public List<Map<String, Object>> queryInvoiceBaseListNoPage(Page page) {
		return qbInvoiceBaseDao.queryInvoiceBaseListNoPage(page);
	}

	@Override
	public List<QbInvoiceBase> getMerInfoByInvoice(Map<String, Object> params) {
		return qbInvoiceBaseDao.getMerInfoByInvoice(params);
	}

	@Override
	public List<QbInvoiceBase> getMerInfo(String customkey) {
		return qbInvoiceBaseDao.getMerInfo(customkey);
	}

	@Override
	public int getMerInvoiceBaseByRecordCount(Page page) {
		return qbInvoiceBaseDao.getMerInvoiceBaseByRecordCount(page);
	}

	@Override
	public List<Map<String, Object>> getMerInvoiceBaseByRecord(Page page) {
		return qbInvoiceBaseDao.getMerInvoiceBaseByRecord(page);
	}

	@Override
	public void updateTaxPicUrl(Map<String, Object> params) {
		qbInvoiceBaseDao.updateTaxPicUrl(params);
	}

    /**
     * 根据id查询是否存在
     */
    @Override
    public int queryInvoiceClassCount(String billingClass) {
        return qbInvoiceBaseDao.queryInvoiceClassCount(billingClass);
    }

	@Override
	public String getServiceTypeNamesByCustomKeyAndCompanyId(String customKey, String companyId) {
		return qbInvoiceBaseDao.getServiceTypeNamesByCustomKeyAndCompanyId(customKey, companyId);
	}

}
