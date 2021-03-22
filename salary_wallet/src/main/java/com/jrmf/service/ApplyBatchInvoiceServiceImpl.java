package com.jrmf.service;

import static com.jrmf.common.Constant.INVOICE_RATE;
import static com.jrmf.utils.Base64Utils.remoteFileToBase64;
import static com.jrmf.utils.HtmlUtil.parseHTML2PDFFile;
import static com.jrmf.utils.HtmlUtil.templateFill;
import static com.jrmf.utils.PdUtil.mergePdfFiles;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.BatchInvoiceStatus;
import com.jrmf.common.PushYuncrNode;
import com.jrmf.common.PushYuncrStatusNode;
import com.jrmf.common.YuncrServiceFeignClient;
import com.jrmf.controller.constant.BatchInvoiceStatusEnum;
import com.jrmf.controller.constant.BatchInvoiceStepEnum;
import com.jrmf.controller.constant.BatchInvoiceStepStatusEnum;
import com.jrmf.controller.constant.InvoiceStatusEnum;
import com.jrmf.domain.ApplyBatchInvoice;
import com.jrmf.domain.ApplyBatchInvoiceAmount;
import com.jrmf.domain.BatchInvoiceAssociation;
import com.jrmf.domain.BatchInvoiceCommission;
import com.jrmf.domain.PushApplyBatchBean;
import com.jrmf.domain.ZhipaiSignTemplate;
import com.jrmf.domain.dto.ApplyBatchInvoiceDTO;
import com.jrmf.domain.dto.InvoiceCommissionDTO;
import com.jrmf.domain.dto.StatisticalBatchInvoiceDTO;
import com.jrmf.persistence.ApplyBatchInvoiceDao;
import com.jrmf.persistence.BatchInvoiceAssociationDao;
import com.jrmf.persistence.BatchInvoiceCommissionDao;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.ThreadPoolUtils;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author: YJY
 * @date: 2021/1/5 16:29
 * @description:
 */
@Slf4j
@Service
public class ApplyBatchInvoiceServiceImpl implements ApplyBatchInvoiceService {

  @Autowired
  ApplyBatchInvoiceDao applyBatchInvoiceDao;
  @Autowired
  BatchInvoiceCommissionDao batchInvoiceCommissionDao;
  @Autowired
  BatchInvoiceAssociationDao batchInvoiceAssociationDao;


  /**
   * @Author YJY
   * @Description 申请批次开票 --提交
   * @Date  2021/1/13
   * @Param [invoiceCommissionDTO]
   * @return boolean
   **/
  @Transactional(rollbackFor = Exception.class)
  @Override
  public boolean batchInsert(InvoiceCommissionDTO invoiceCommissionDTO) {
    splitData(invoiceCommissionDTO);
    //查询开票记录
    List<ApplyBatchInvoice> list = applyBatchInvoiceDao.findByConditionForUpdate(invoiceCommissionDTO);
    if(CollectionUtils.isEmpty(list)){
      return false;
    }

    //计算开票金额
    getInvoiceAmount(list);
    for (ApplyBatchInvoice applyInsert : list) {
      if(BatchInvoiceStatus.IN_INVOICE.getNode().equals(applyInsert.getInvoiceStatus()+"")
        || BatchInvoiceStatus.SUCCESS_INVOICE.getNode().equals(applyInsert.getInvoiceStatus()+"")){
        throw new RuntimeException("您好，批次有被申请开票的记录，请重新查询选择");
      }
      applyInsert.setApplyBatchRemark(invoiceCommissionDTO.getRemark());
      applyInsert.setChannelTaskId(invoiceCommissionDTO.getTaskId());
    }
    boolean applyInsert = applyBatchInvoiceDao.batchInsert(list) > 0 ? true : false;

    if (!applyInsert) {
      return false;
    }
    //统计批次下发记录 和批次开票表的关联关系
    List<BatchInvoiceCommission> commissionList = findCommissionList(invoiceCommissionDTO);
    List<BatchInvoiceAssociation> associationList = getApplyInvoiceData(commissionList, list);
    if (CollectionUtils.isEmpty(associationList)) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return false;
    }
    //插入批次下发记录和批次开票表的关联关系
    boolean flag = batchInvoiceAssociationDao.batchInsert(associationList) > 0 ? true : false;
    //插入批次开票表和商户的关联关系
    boolean flagCustom = batchInvoiceAssociationDao.batchInsertCustom(associationList) > 0 ? true : false;
    //更新批次下发记录的发票状态
    boolean flagCommission = batchInvoiceAssociationDao.updateCommissionStatus(associationList) > 0 ? true : false;
    if (!flag || !flagCustom || !flagCommission) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return false;
    }

    return true;
  }
  /**
   * @Author YJY
   * @Description  申请批次开票 --列表
   * @Date  2021/1/13
   * @Param [invoiceCommissionDTO]
   * @return com.github.pagehelper.PageInfo<com.jrmf.domain.ApplyBatchInvoice>
   **/
  @Override
  public PageInfo<ApplyBatchInvoice> findByCondition(InvoiceCommissionDTO invoiceCommissionDTO) {

    splitData(invoiceCommissionDTO);
    PageHelper.startPage(invoiceCommissionDTO.getPageNo(), invoiceCommissionDTO.getPageSize());
    PageInfo<ApplyBatchInvoice> pageInfo = new PageInfo<>(
        applyBatchInvoiceDao.findCommissionStatisticalByCondition(invoiceCommissionDTO));
    if(!ObjectUtils.isEmpty(pageInfo) || !CollectionUtils.isEmpty(pageInfo.getList())){
      getInvoiceAmount(pageInfo.getList());
    }
    return pageInfo;
  }

  @Override
  public List<ApplyBatchInvoice> findListByCondition(InvoiceCommissionDTO invoiceCommissionDTO) {
    splitData(invoiceCommissionDTO);
    return applyBatchInvoiceDao.findCommissionStatisticalByCondition(invoiceCommissionDTO);


  }
  /**
   * @Author YJY
   * @Description  申请批次开票 -- 统计数据
   * @Date  2021/1/13
   * @Param [invoiceCommissionDTO]
   * @return com.jrmf.domain.dto.StatisticalBatchInvoiceDTO
   **/
  @Override
  public StatisticalBatchInvoiceDTO findStatisticalByCondition(
      InvoiceCommissionDTO invoiceCommissionDTO) {
    splitData(invoiceCommissionDTO);
    StatisticalBatchInvoiceDTO statisticalBatchInvoiceDTO = applyBatchInvoiceDao
        .findStatisticalByCondition(invoiceCommissionDTO);
    statisticalBatchInvoiceDTO.setTradeMoney(statisticalBatchInvoiceDTO.getInvoiceMoney());
    String limitMoney = applyBatchInvoiceDao.findLimitMoney(invoiceCommissionDTO);
    if (!ObjectUtils.isEmpty(limitMoney)) {
      String invoiceMoney = ArithmeticUtil.addStr(statisticalBatchInvoiceDTO.getInvoiceMoney(),
          ArithmeticUtil.mulStr(limitMoney, INVOICE_RATE));
      statisticalBatchInvoiceDTO.setInvoiceMoney(invoiceMoney);
    }
    return statisticalBatchInvoiceDTO;

  }

  @Override
  public List<ApplyBatchInvoiceAmount> statisticalUserAmountByMonth(Set certIds, Set tradeMonths) {
    return applyBatchInvoiceDao.findApplyAmount(certIds, tradeMonths);
  }

  @Override
  public List<ApplyBatchInvoice> getInvoiceList(ApplyBatchInvoiceDTO applyBatchInvoiceDTO) {
    return applyBatchInvoiceDao.getInvoiceList(applyBatchInvoiceDTO);
  }

  @Override
  public List<BatchInvoiceCommission> findCommissionList(
      InvoiceCommissionDTO invoiceCommissionDTO) {
    return applyBatchInvoiceDao.findCommissionList(invoiceCommissionDTO);
  }

  public void getInvoiceAmount(List<ApplyBatchInvoice> list) {
    Set idCard = new HashSet();
    Set month = new HashSet();
    for (ApplyBatchInvoice batchInvoice : list) {
      idCard.add(batchInvoice.getIdCard());
      month.add(batchInvoice.getTradeMonth());
    }

    List<ApplyBatchInvoiceAmount> userAmountByMonth = statisticalUserAmountByMonth(idCard, month);
    for (ApplyBatchInvoice batchInvoice : list) {
      batchInvoice.setInvoiceMoney(batchInvoice.getTradeMoney());
      for (ApplyBatchInvoiceAmount amount : userAmountByMonth) {
        //超限额
        if (batchInvoice.getIdCard().equals(amount.getIdCard())
            && batchInvoice.getTradeMonth().equals(amount.getTradeMonth())) {
          String tradeMoney = ArithmeticUtil.addStr(batchInvoice.getTradeMoney(),
              ArithmeticUtil.mulStr(batchInvoice.getTradeMoney(), INVOICE_RATE));
          batchInvoice.setInvoiceMoney(tradeMoney);
        }
      }
    }

  }

  public List<BatchInvoiceAssociation> getApplyInvoiceData(
      List<BatchInvoiceCommission> commissionList,
      List<ApplyBatchInvoice> applyBatchInvoices) {

    List<BatchInvoiceAssociation> list = new ArrayList<>();
    for (BatchInvoiceCommission commission : commissionList) {

      for (ApplyBatchInvoice batchInvoice : applyBatchInvoices) {

        if (commission.getCertId().trim().equals(batchInvoice.getIdCard().trim()) &&
            commission.getInAccountNo().trim().equals(batchInvoice.getInAccountNo().trim()) &&
            commission.getAccountDate().trim().equals(batchInvoice.getTradeMonth().trim())) {
          BatchInvoiceAssociation batchInvoiceAssociation = new BatchInvoiceAssociation();
          batchInvoiceAssociation.setApplyBatchInvoiceId(batchInvoice.getId());
          batchInvoiceAssociation.setCommissionId(commission.getId());
          batchInvoiceAssociation.setCustomKey(commission.getCustomKey());
          list.add(batchInvoiceAssociation);
        }
      }
    }

    return list;
  }

  @Override
  public ApplyBatchInvoice findById(String id) {
    return applyBatchInvoiceDao.findById(id);
  }

  @Transactional
  @Override
  public void batchInvoiceSuccess(ApplyBatchInvoice applyBatchInvoice) {
    //更改交易记录状态为成功
    BatchInvoiceAssociation association = new BatchInvoiceAssociation();
    association.setApplyBatchInvoiceId(applyBatchInvoice.getId());
    List<BatchInvoiceAssociation> batchInvoiceAssociationList = batchInvoiceAssociationDao
        .findBatchInvoiceAssociationList(association);
    for (BatchInvoiceAssociation batchInvoiceAssociation : batchInvoiceAssociationList) {
      BatchInvoiceCommission batchInvoiceCommission = new BatchInvoiceCommission();
      batchInvoiceCommission.setId(batchInvoiceAssociation.getCommissionId());
      batchInvoiceCommission.setInvoiceStatus(InvoiceStatusEnum.INVOICE_SUCCESS.getCode());
      batchInvoiceCommissionDao.updateStatusById(batchInvoiceCommission);
    }

    //更改开票记录
    applyBatchInvoice.setStep(BatchInvoiceStepEnum.PUSH_INVOICE.getCode());
    applyBatchInvoice.setStepStatus(BatchInvoiceStepStatusEnum.SUCCESS.getCode());
    applyBatchInvoice.setInvoiceStatus(BatchInvoiceStatusEnum.FINISH.getCode());
    applyBatchInvoice.setInvoiceStatusDescribe((BatchInvoiceStatusEnum.FINISH.getDesc()));
    int row = applyBatchInvoiceDao.updateBatchInvoice(applyBatchInvoice);
  }

  @Override
  public int updateBatchInvoice(ApplyBatchInvoice applyBatchInvoice) {
    return applyBatchInvoiceDao.updateBatchInvoice(applyBatchInvoice);
  }

  @Transactional
  @Override
  public void setInvoiceFail(ApplyBatchInvoice applyBatchInvoice) {
    //设置交易记录状态为开票失败
    BatchInvoiceAssociation association = new BatchInvoiceAssociation();
    association.setApplyBatchInvoiceId(applyBatchInvoice.getId());
    List<BatchInvoiceAssociation> batchInvoiceAssociationList = batchInvoiceAssociationDao
        .findBatchInvoiceAssociationList(association);
    for (BatchInvoiceAssociation batchInvoiceAssociation : batchInvoiceAssociationList) {
      BatchInvoiceCommission batchInvoiceCommission = new BatchInvoiceCommission();
      batchInvoiceCommission.setId(batchInvoiceAssociation.getCommissionId());
      batchInvoiceCommission.setInvoiceStatus(InvoiceStatusEnum.INVOICE_FAIL.getCode());
      batchInvoiceCommissionDao.updateStatusById(batchInvoiceCommission);
    }

    //更改开票记录
    applyBatchInvoice.setStepStatus(BatchInvoiceStepStatusEnum.FAIL.getCode());
    applyBatchInvoice.setInvoiceStatus(BatchInvoiceStatusEnum.FAIL.getCode());
    int row = applyBatchInvoiceDao.updateBatchInvoice(applyBatchInvoice);
  }

  @Override
  public int checkCustom(String companyName) {

    return applyBatchInvoiceDao.checkCustom(companyName);
  }

  @Override
  public List<HashMap> findTaskList(String companyName) {
    return applyBatchInvoiceDao.findTaskList(companyName);
  }

  @Override
  public HashMap findTaskDetail(Integer id) {

    return applyBatchInvoiceDao.findTaskDetail(id);

  }

  @Override
  public int findCountByCondition(InvoiceCommissionDTO invoiceCommissionDTO) {
    splitData(invoiceCommissionDTO);
    return applyBatchInvoiceDao.findCountByCondition(invoiceCommissionDTO);
  }


  @Override
  public List<ApplyBatchInvoice> findALLByCondition(InvoiceCommissionDTO invoiceCommissionDTO) {
    splitData(invoiceCommissionDTO);
    List<ApplyBatchInvoice> list = applyBatchInvoiceDao.findCommissionStatisticalByCondition(invoiceCommissionDTO);
    if(!CollectionUtils.isEmpty(list)){
      getInvoiceAmount(list);
    }
    return list;
  }




 public void splitData(InvoiceCommissionDTO invoiceCommissionDTO){
   if (!StringUtils.isEmpty(invoiceCommissionDTO.getIds())) {
     invoiceCommissionDTO.setListId(Arrays.asList(invoiceCommissionDTO.getIds().split(",")));
   }
   if (!StringUtils.isEmpty(invoiceCommissionDTO.getInvoiceStatus())) {
     String  invoiceStatus = checkInvoiceStatus(invoiceCommissionDTO.getInvoiceStatus());
     if(!StringUtils.isEmpty(invoiceStatus)){
       invoiceCommissionDTO.setInvoiceStatusList(Arrays.asList(invoiceCommissionDTO.getInvoiceStatus().split(",")));
     }
   }
 }

 //筛掉开票中或者开票完成的数据
 public String checkInvoiceStatus(String invoiceStatus){

   if(invoiceStatus.contains(BatchInvoiceStatus.IN_INVOICE.getNode())
       ||invoiceStatus.contains(BatchInvoiceStatus.SUCCESS_INVOICE.getNode())){

     invoiceStatus = invoiceStatus.replaceAll(BatchInvoiceStatus.IN_INVOICE.getNode()+"|"+BatchInvoiceStatus.SUCCESS_INVOICE.getNode(),"");
     invoiceStatus = invoiceStatus.replaceAll(",,","");
     if(invoiceStatus.startsWith(",")){
       invoiceStatus = invoiceStatus.substring(1,invoiceStatus.length());
     }
     if(invoiceStatus.endsWith(",")){
       invoiceStatus = invoiceStatus.substring(0,invoiceStatus.length()-1);
     }

   }

   return invoiceStatus;
 }
}
























