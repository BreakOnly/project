package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoiceRecord;
import com.jrmf.domain.QbInvoiceReserve;
@Mapper
public interface QbInvoiceReserveDao {

    int deleteByPrimaryKey(Integer id);

    int insert(QbInvoiceReserve record);

    QbInvoiceReserve selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(QbInvoiceReserve record);

    int updateByPrimaryKey(QbInvoiceReserve record);

	List<Map<String, Object>> getInvoiceReserveByPage(Page page);

	int getInvoiceReserveCount(Page page);

	int checkIsExist(QbInvoiceReserve invoiceReserve);

	QbInvoiceReserve getReserveByParams(QbInvoiceRecord invoiceRecord);
}