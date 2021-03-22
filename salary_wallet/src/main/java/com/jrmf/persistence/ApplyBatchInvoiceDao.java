package com.jrmf.persistence;

import com.jrmf.domain.ApplyBatchInvoice;
import com.jrmf.domain.ApplyBatchInvoiceAmount;
import com.jrmf.domain.BatchInvoiceCommission;
import com.jrmf.domain.PushApplyBatchBean;
import com.jrmf.domain.dto.ApplyBatchInvoiceDTO;
import com.jrmf.domain.dto.BatchInvoiceCommissionDTO;
import com.jrmf.domain.dto.InvoiceCommissionDTO;
import com.jrmf.domain.dto.StatisticalBatchInvoiceDTO;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author: YJY
 * @date: 2020/12/14
 * @description:
 */
@Mapper
public interface ApplyBatchInvoiceDao {

  int batchInsert(List<ApplyBatchInvoice> list);

  List<ApplyBatchInvoice> findCommissionStatisticalByCondition(
      InvoiceCommissionDTO invoiceCommissionDTO);

  List<ApplyBatchInvoice> findByConditionForUpdate(
      InvoiceCommissionDTO invoiceCommissionDTO);

  StatisticalBatchInvoiceDTO findStatisticalByCondition(InvoiceCommissionDTO invoiceCommissionDTO);

  List<ApplyBatchInvoiceAmount> findApplyAmount(@Param("certIds") Set certIds,@Param("tradeMonths")Set tradeMonths);


    List<ApplyBatchInvoice> getInvoiceList(ApplyBatchInvoiceDTO applyBatchInvoiceDTO);

  ApplyBatchInvoice findById(@Param("id")String id);

    int updateBatchInvoice(ApplyBatchInvoice applyBatchInvoice);

  List<BatchInvoiceCommission> findCommissionList(InvoiceCommissionDTO invoiceCommissionDTO);

  int checkCustom(@Param("companyName")String companyName);

  List<HashMap> findTaskList(@Param("companyName")String companyName);

  HashMap findTaskDetail(@Param("id")int id);

  List<PushApplyBatchBean> findApplyBatchListByStatus(@Param("id")Integer id);

  String findLimitMoney(InvoiceCommissionDTO invoiceCommissionDTO);

  List<String> selectReceiptUrl(@Param("id") Integer id);

  int findCountByCondition(InvoiceCommissionDTO invoiceCommissionDTO);
}
