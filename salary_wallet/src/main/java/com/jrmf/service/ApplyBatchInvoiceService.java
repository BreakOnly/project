package com.jrmf.service;

import com.github.pagehelper.PageInfo;
import com.jrmf.domain.ApplyBatchInvoice;
import com.jrmf.domain.ApplyBatchInvoiceAmount;
import com.jrmf.domain.BatchInvoiceCommission;
import com.jrmf.domain.dto.ApplyBatchInvoiceDTO;
import com.jrmf.domain.dto.BatchInvoiceCommissionDTO;
import com.jrmf.domain.dto.InvoiceCommissionDTO;
import com.jrmf.domain.dto.StatisticalBatchInvoiceDTO;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Param;

/**
 * @author: YJY
 * @date: 2021/1/5 16:27
 * @description:
 */
public interface ApplyBatchInvoiceService {


  boolean batchInsert(InvoiceCommissionDTO invoiceCommissionDTO);

  PageInfo<ApplyBatchInvoice> findByCondition(InvoiceCommissionDTO invoiceCommissionDTO);

  List<ApplyBatchInvoice> findListByCondition(InvoiceCommissionDTO invoiceCommissionDTO);

  StatisticalBatchInvoiceDTO findStatisticalByCondition(InvoiceCommissionDTO invoiceCommissionDTO);

  List<ApplyBatchInvoiceAmount> statisticalUserAmountByMonth(Set certIds,Set tradeMonths);

    List<ApplyBatchInvoice> getInvoiceList(ApplyBatchInvoiceDTO applyBatchInvoiceDTO);

  List<BatchInvoiceCommission> findCommissionList(InvoiceCommissionDTO invoiceCommissionDTO);

  ApplyBatchInvoice findById(String id);

    void batchInvoiceSuccess(ApplyBatchInvoice applyBatchInvoice);

  int updateBatchInvoice(ApplyBatchInvoice applyBatchInvoice);

  void setInvoiceFail(ApplyBatchInvoice applyBatchInvoice);
  int checkCustom(String companyName);

  List<HashMap> findTaskList(String companyName);

  HashMap findTaskDetail(Integer id);

  int findCountByCondition(InvoiceCommissionDTO invoiceCommissionDTO);

  List<ApplyBatchInvoice> findALLByCondition(InvoiceCommissionDTO invoiceCommissionDTO);

}
