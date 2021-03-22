package com.jrmf.service;

import java.util.List;
import com.jrmf.domain.QbInvoicePic;


public interface QbInvoicePicService {

    int deleteByPrimaryKey(Integer id);

    int insert(QbInvoicePic record);

    QbInvoicePic selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(QbInvoicePic record);

    int updateByPrimaryKey(QbInvoicePic record);

	List<QbInvoicePic> getPicListBySerialNo(String invoiceSerialNo);
}