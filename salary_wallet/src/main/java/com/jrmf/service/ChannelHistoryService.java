package com.jrmf.service;

import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.ChannelHistoryPic;
import com.jrmf.domain.CompanyEsignContractTemplate;
import com.jrmf.domain.MerchantTransaction;
import com.jrmf.domain.Page;

import java.io.InputStream;
import com.jrmf.taxsettlement.api.service.recharge.RechargeRecordListServiceAttachment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhangzehui
 * @version 创建时间：2017年12月16日
 */
@Service
public interface ChannelHistoryService {

    public int addChannelHistory(ChannelHistory history);

    public List<ChannelHistory> getChannelHistoryByParam(Map<String, Object> param);

    List<ChannelHistory> getChannelHistoryByParamOnJob(Map<String, Object> param);

    public List<ChannelHistory> getChannelHistoryBySubmit(Map<String, Object> param);

    public List<ChannelHistory> getChannelHistoryList(Map<String, Object> param);

    public int updateChannelHistory(ChannelHistory history);

    public void deleteById(int id);

    public String getBalance(String customkey, String companyId, String payType);

    public ChannelHistory getChannelHistoryById(String id);

    public List<ChannelHistory> getChannelHistoryByCompany(Map<String, Object> param);

    public void deleteByOrderno(String orderno);

    public ChannelHistory getChannelHistoryByOrderno(String orderno);

    public List<Map<String, Object>> batchResultQuery(Map<String, Object> param);

    public List<Map<String, Object>> batchResultQueryByCompany(Map<String, Object> param);

    List<Map<String, Object>> geCustomChargeDetail(Map<String, Object> paramMap);

    List<ChannelHistory> getHistoryList(Map<String, Object> param);

    List<MerchantTransaction> selectTransactionList(Map<String, Object> param);

    List<MerchantTransaction> selectTransactionListByProxy(Map<String, Object> param);

    public int querybillingListCount(Page page);

    public List<Map<String, Object>> billingList(Page page);

    public String getTotalAmountByOrderNo(String orderNo);

    public List<Map<String, Object>> checkCommonCompanyAndCustom(String orderNo);

    public List<Map<String, Object>> queryBillingListNoPage(Page page);

    List<ChannelHistory> getAutoConfirmList();

    void rechargeCallback(ChannelHistory history);

    int getToBeConfirmedCount(String customKey, String companyId, Integer payType);

    ChannelHistory getRechargeInfoById(@Param("id")String id);

    /**
     * 获取与代理商绑定关系的商户信息
     * @param paramMap
     * @return
     */
    List<Map<String, Object>> getProxyCustomCompanyDetail(Map<String, Object> paramMap);

    List<ChannelHistory> getWarningRechargeList(Integer minute);

    void updateSendStatus(int id);

	public List<Map<String, String>> queryUserAgreementMatch(Page page);

	public List<Map<String, String>> noAgreementCount(Map<String, Object> params);

	public int agreementOtherCompanyCount(Map<String, String> certMap);

	public int queryUserAgreementMatchCount(Page page);

	public List<Map<String, String>> queryUserAgreementMatchNoPage(Page page);

	public String agreementOtherCompanyNames(Map<String, String> noAgreement);

	public List<Map<String, String>> payUsers(Map<String, Object> params);

	void approvalInvoice(ChannelHistory history);

    int selectTransactionListCount(Map<String, Object> model);

    int selectTransactionListByProxyCount(Map<String, Object> model);

    int batchResultQueryByCompanyCount(Map<String, Object> param);

    ChannelHistory getByOriginalBeachNo(String batchId);

	public int updateChannelHistorySummary(ChannelHistory history);

    int updateRechargeStatus(ChannelHistory history);

  String geCustomChargeAmount(Map<String, Object> paramMap);

    List<Map<String, Object>> getPicListByOrderNo(String orderNo);

    void insertChannelHistoryPic(ChannelHistoryPic channelHistoryPic);

    void deleteRechargeFileById(String id);

    ChannelHistoryPic getChannelHistoryPicById(String id);

    void updateChannelHistoryFileNumAddByOrderNo(String orderNo);

    void updateChannelHistoryFileNumMinusByOrderNo(String id);

  List<RechargeRecordListServiceAttachment> apiGetChannelHistoryList(Map<String, Object> paramMap);

  String getTotalUninvoicedAmountByCompanyId(Integer companyId, String customKey);

  int selectCountByLetterStatus(int id);

  boolean uploadLetter(int id, InputStream file, String fileName, String ftpUrl, String ftpUserName,
      String ftpPassword, String fileUrl);

  CompanyEsignContractTemplate getCompanyEsignContractTemplateByParams(int companyId,Byte bizType);

}
