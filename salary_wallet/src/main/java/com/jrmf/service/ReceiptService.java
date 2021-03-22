package com.jrmf.service;

import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.domain.ReceiptBatch;
import com.jrmf.domain.ReceiptCommission;
import com.jrmf.domain.ReceiptDownLoad;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2019/1/22 15:51
 * Version:1.0
 */
public interface ReceiptService {

    int updateStatusReceiptDownloadById(Integer status,String statusDesc,Integer id);

    List<ReceiptCommission> listReceiptCommission(Map<String, Object> params);

    List<ReceiptBatch> listReceiptBatch(Map<String, Object> params);

    List<ReceiptBatch> listReceiptBatchGroup(Map<String, Object> params);

    ReceiptCommission getReceiptCommissionByReceiptNo(Map<String, Object> params);

    public void saveReceiptBatch(ReceiptBatch batch);

    public void updateReceiptBatch(ReceiptBatch receiptBatch);

    public void  updateReceiptCommission(Map<String, Object> receiptCommissions);

	public void updateReceiptCommissionById(Map<String, Object> receiptCommission);

	public void updateReceiptCommissionByReceiptNo(Map<String, Object> receiptCommission);

    public ReceiptBatch getReceiptBatchById(Integer id);

    /**
     * 根据条件查询下发记录对应的银行流水pdf路径
     * @param param 参数集合
     * @param customName 登录用户名称
     * @return zip包本地路径  可能为空
     */
    String listPdfPathByParam(Map<String, Object> param,String fileName,Integer id);

    /**
     * pdf切割
     * @param fileBytes pdf源文件
     * @param id pdf源文件数据库id
     * @param path pdf分解文件路径
     */
    void partitionPdfFile(byte[] fileBytes,String id,String path);

    List<ReceiptDownLoad> listDownloadHistory(Map<String, Object> params);

    public int addReceipt(Map<String, Object> params);

    int addReceiptDownload(ReceiptDownLoad receiptDownLoad);

    /**
     * 触发jobservice  aygRecepitBatchJob  回单处理--爱员工批次
     */
    void initAygRecepitBatchJob();

    int listReceiptCommissionCount(Map<String, Object> params);

    void pdfSplit(byte[] bytes, AtomicInteger atomicInteger, AtomicInteger integer, int pageSize, String id, String path, String fileName,Integer receiptImportType)
        throws Exception;

    void autoImportReceipt(ReceiptBatch receiptBatch, String fileName, BestSignConfig bestSignConfig);

  String getReceiptCommissionByOrderNo(String orderNo);
}
