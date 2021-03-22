package com.jrmf.service;

import java.util.List;
import java.util.Map;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoiceBase;

public interface QbInvoiceBaseService {

    int deleteByPrimaryKey(Integer id);

    int insert(QbInvoiceBase record);

    int insertSelective(QbInvoiceBase record);
    
    QbInvoiceBase selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(QbInvoiceBase record);

    int updateByPrimaryKey(QbInvoiceBase record);

	int queryInvoiceBaseListCount(Page page);

	List<Map<String, Object>> queryInvoiceBaseList(Page page);

	int queryInvoiceClassInfoListCount(Page page);

	List<Map<String, Object>> queryInvoiceClassInfoList(Page page);

	String queryServiceContent(Integer id);

	List<Map<String, Object>> queryInvoiceBaseListNoPage(Page page);
	
	List<QbInvoiceBase> getMerInfo(String customkey);

	List<QbInvoiceBase> getMerInfoByInvoice(Map<String, Object> params);

	int getMerInvoiceBaseByRecordCount(Page page);

	List<Map<String, Object>> getMerInvoiceBaseByRecord(Page page);

	void updateTaxPicUrl(Map<String, Object> params);

    /**
     * 根据id查询是否存在
     */
    int queryInvoiceClassCount(String billingClass);

	String getServiceTypeNamesByCustomKeyAndCompanyId(String customKey, String companyId);
}