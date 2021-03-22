package com.jrmf.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.QbInvoicePic;

@Mapper
public interface QbInvoicePicDao {

    int deleteByPrimaryKey(Integer id);

    int insert(QbInvoicePic record);

    QbInvoicePic selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(QbInvoicePic record);

    int updateByPrimaryKey(QbInvoicePic record);

	List<QbInvoicePic> getPicListBySerialNo(String invoiceSerialNo);
}