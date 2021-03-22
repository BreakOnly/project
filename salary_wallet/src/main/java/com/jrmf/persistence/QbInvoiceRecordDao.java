package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoiceRecord;

@Mapper
public interface QbInvoiceRecordDao {

  int deleteByPrimaryKey(Integer id);

  int insert(QbInvoiceRecord record);

  QbInvoiceRecord selectByPrimaryKey(Integer id);

  int updateByPrimaryKeySelective(QbInvoiceRecord record);

  int updateByPrimaryKey(QbInvoiceRecord record);

  int queryRecordListCount(Page page);

  List<Map<String, Object>> queryRecordList(Page page);

  List<Map<String, Object>> queryRecordListNoPage(Page page);

  List<Integer> groupBillingClassByOrderNo(String orderNo);

  QbInvoiceRecord getByInvoiceSerialNo(String invoiceSerialNo);

  String getRecentAddress(String customkey);

  /**
   * 查询开票历史记录，包含图片
   */
  List<Map<String, Object>> queryRecordWithPicList(Page page);

  /**
   * 查询开票历史记录条数，包含图片
   */
  List<Integer> queryRecordListWithPicCount(Page page);

  List<QbInvoiceRecord> selectByPrimaryKeys(String ids);

  QbInvoiceRecord getAdvanceInvoice(String customKey, String companyId);

  int updateByRecharge(QbInvoiceRecord record);

  List<QbInvoiceRecord> getCommissionInvoiceList(Map<String, Object> params);
}