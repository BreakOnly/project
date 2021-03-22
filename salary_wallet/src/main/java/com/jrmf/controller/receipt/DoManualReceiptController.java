package com.jrmf.controller.receipt;

import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.ReceiptBatch;
import com.jrmf.domain.ReceiptCommission;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.model.request.deliver.ReceiptRequestParam;
import com.jrmf.payment.openapi.model.response.BaseResponseResult;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.util.ClientMapUtil;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.service.ReceiptService;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.ftp.FTPClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/domanual/receipt")
public class DoManualReceiptController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(DoManualReceiptController.class);
    @Autowired
    private BaseInfo baseInfo;
    @Autowired
    private ReceiptService receiptService;
    @Autowired
    private ChannelRelatedDao channelRelatedDao;
    @Autowired
    private OrderNoUtil orderNoUtil;
    @Autowired
    private FTPClientUtil ftpClientUtil;

    //回单处理--创建处理任务信息
    @RequestMapping(value = "/receiptBatchCreate")
    @ResponseBody
    public Map<String, Object> receiptBatchCreate(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>();

        try {

            String payType = request.getParameter("payType");
            String companyId = request.getParameter("companyId");
            String merchantId = request.getParameter("merchantId");
            String receiptTime = request.getParameter("receiptTime");

            Map<String, Object> params = new HashMap<>(20);
            params.put("payType", payType);
            params.put("companyId", companyId);
            params.put("merchantId", merchantId);
            params.put("receiptTime", receiptTime);
            params.put("status", "1");

            List<ReceiptBatch> receiptBatchList = null;
            receiptBatchList = receiptService.listReceiptBatchGroup(params);
            for (ReceiptBatch batch : receiptBatchList) {//根据支付通道、服务公司分组插入

                ReceiptBatch batchNew = new ReceiptBatch();
                batchNew.setStatus(0);
                batchNew.setCommissionNum(batch.getCommissionNum());
                batchNew.setCompanyId(batch.getCompanyId());
                batchNew.setPayType(batch.getPayType());
                batchNew.setReceiptTime(batch.getReceiptTime());

                String orgType = "1";
                String orgTryName = "pa";
                String receiptType = "1";//手动
                if ("aiyuangong".equals(merchantId)) {
                    orgType = "3";
                    orgTryName = "ayg";
                    receiptType = "0";
                }

                batchNew.setReceiptType(receiptType);
                batchNew.setReceiptOrgType(orgType);
                batchNew.setReceiptOrgName(orgTryName);

                batchNew.setMerchantId(batch.getMerchantId());

                receiptService.saveReceiptBatch(batchNew);
            }

            result.put(RespCode.RESP_STAT, respstat);

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, e.getMessage());
        }

        return result;
    }

    //回单处理--更批次任务信息
    @RequestMapping(value = "/recepitBatchUpdate")
    @ResponseBody
    public Map<String, Object> aygRecepitBatchJob(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>();

        try {

            String companyId = request.getParameter("companyId");
            String payType = request.getParameter("payType");
            String merchantId = request.getParameter("merchantId");
            String receiptTime = request.getParameter("receiptTime");

            Map<String, Object> params = new HashMap<>(20);
            params.put("companyId", companyId);
            params.put("payType", payType);
            params.put("merchantId", merchantId);
            params.put("receiptTime", receiptTime);

            List<ReceiptBatch> receiptBatchList = receiptService.listReceiptBatch(params);
            for (ReceiptBatch batch : receiptBatchList) {//根据支付通道、服务公司分组插入

                params.clear();
                params.put("companyId", batch.getCompanyId());
                params.put("receiptTime", batch.getReceiptTime());
                params.put("payType", batch.getPayType());
                params.put("merchantId", batch.getMerchantId());
                List<ReceiptCommission> listReceipt = receiptService.listReceiptCommission(params);

                int receiptMatchNum = 0;
                Map<String, Object> receiptCommissions = new HashMap<String, Object>();
                for (ReceiptCommission commission : listReceipt) {

                    String fileName = commission.getAccountDate() + "_" + commission.getReceiptNo() + ".pdf";
                    String yearMon = commission.getAccountDate().substring(0, 7);
                    String pathPdfDir = "/" + commission.getCompanyId() + "/" + commission.getPayType() + "_" +batch.getReceiptOrgName() + "/" + yearMon;
                    String relativePath = pathPdfDir + "/" + fileName;
                    String receiptFile = "/receipt" + relativePath;
                    logger.info("----手工----回单文件路径：" + receiptFile);
                    
                    receiptCommissions.put("id", commission.getId());
                    
                    boolean isFileExists = ftpClientUtil.isExists(receiptFile);
                    if (isFileExists) {
                        receiptMatchNum++;
                        receiptCommissions.put("receiptUrl", relativePath);
                        receiptCommissions.put("receiptChecked", "1");
                    } else {
                        receiptCommissions.put("receiptChecked", "2");
                        logger.info("---手工----回单文件不存在：" + receiptFile + "----勾兑失败----");
                    }
                    receiptService.updateReceiptCommissionById(receiptCommissions);
                    receiptCommissions.clear();
                }

                batch.setReceiptMatchNum(receiptMatchNum + "");
                if (listReceipt.size() == receiptMatchNum && receiptMatchNum != 0) {
                    batch.setStatus(1);//全部成功
                } else if (receiptMatchNum != 0 && receiptMatchNum != listReceipt.size()) {
                    batch.setStatus(3);//部分成功
                } else if (receiptMatchNum == 0) {
                    batch.setStatus(4);//全部失败
                }

                receiptService.updateReceiptBatch(batch);
            }

            result.put(RespCode.RESP_STAT, respstat);

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, e.getMessage());
        }

        return result;
    }

    //回单明细--创建回单明细
    @RequestMapping(value = "/receiptCommissionCreate")
    @ResponseBody
    public Map<String, Object> receiptCommissionCreateJob(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>();

        try {
            String companyId = request.getParameter("companyId");
            String payType = request.getParameter("payType");
            String merchantId = request.getParameter("merchantId");
            String receiptTime = request.getParameter("receiptTime");

            Map<String, Object> params = new HashMap<>(20);
            params.put("merchantId", merchantId);
            params.put("payType", payType);
            params.put("status", "1");
            params.put("receiptTime", receiptTime);
            params.put("companyId", companyId);
            receiptService.addReceipt(params);

            result.put(RespCode.RESP_STAT, respstat);

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, e.getMessage());
        }
        return result;
    }

    //回单明细--更新回单明细
    @RequestMapping(value = "/receiptCommissionUpdate")
    @ResponseBody
    public Map<String, Object> receiptCommissionUpdate(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>();
        
    	try{
    		
        	String payType = request.getParameter("payType");
            String companyId = request.getParameter("companyId");
            String receiptTime = request.getParameter("receiptTime");
            Map<String, Object> params = new HashMap<String, Object>();
        	
            String priKeyString = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCcdJ+nqtInOaHn/fZTbxdj5oTMFU6AJKD3DWmoYqUGcMCebtpdKhT/BbAb/t8Rsp1QLv1r4h+nf7s98LZkV9J6dSa5iHDIqCGT12byaTftKrYZlf3Hr8SFDkzBPTSBz56LRnYCRaYlrMQgZgY9U/T5UIwKVhJEordPvwbH2MqueKxUdk9vD4S8Dj7rEgor31Ifc3E/qRivKcRCncWBpiAtOTNSPLtsx0TIxizPXYicjL8Tr2+eLnuUxjKhWnFxvpRMC2ghLVrsWiVU0oW/0a6Qbv+TtB4/xR1JWV+pn4fqQhiJAssMHN4kEn7JxPpXB+/MzWUfrrGsYnSlNwpxPojzAgMBAAECggEAYJNEsfSpwHi8zj1fveTHJW1375n/WO5DRfzLiZtKjo0u+R0oQXXme/0A1mcfPwdoP8ShveRY8cXQyM07aPkk/V4vRztHkzTldSLzcxMr6IQC4AxMGOUQg6luC6JCNRb5oLMfyQtBIeRhNDaGB3k5sGPd7ctvf1qJmPorr1TM16C9vW8MdF4YjOfxpvm2g5AuaFHB3O9jTd6+tN3oAiusC3TwjtvhfQDmnPqfJOdp2x2NZAf47luU7J/eiEvyEPkjfmwSIQKvXfzIw0uu2rLX+52OmtJlJ8DYF8Ep9iCSJJMLiHhxYkSkNuwyB5/5oVZmRGj8mZVNdfIXyCaFXnEw0QKBgQDzZFAYCg+QRQZx9fdWwM4mSeHRCeZpQ9QBGjwNN0yWF+XioNtnMIr/pZYV/25biVVF5Pc/20r/8ZSBCShgR3u1ePuoaxzU907lT3R7tBoqaIfvjEBaZuOQcaGJqvNNJnySmhP1PnsjD5x9wWwer7nJLvl8UqmgbED0gKoClfWopwKBgQCkj2ypjvo6fHg19huw96y7CaYQjljWslY6GsWzE1WqSVy0xJb6JEzARZe38XhXPiAeg9mhlyUfkum8LJaYGe1JA5a2c9qhUOPfFeGM6PblGcF1jlMZYLv8shhxkjeWQA/kJngbW6lCHQBbc2G/W7irUC+PbkZkknAcYkYlQE3a1QKBgD9fVxtrQzIlRtBVYtlLymFdy1ZKZZvy9Th0RD6Mr3xFLK4dhAMSOJ7n1nRT1cAvuexA+b++sYCCvk/6unCXLDbMEXqAqTkqS3iZf5LWChoQrZRJyFfBgm8RpyXZRRBJfRYO2DN62UT/w5dazXQP/SfM+1jLjS8gAKmo9ptFwHjxAoGAK3y3g4uENwaDogb6xGZ/YCIpn4Bum7YfMVW33x4B6nFerWqyV0JWgg0iDfsjCTMiu82uKpTNu61QVWkXFvTrDvuCzY6KPU0qGt8mbt11uY9334AQF8nHg/zwlrrEM9GUIX/FB73OWeleGczBDRfJEoSrPOUwdw130RhrXxbCPE0CgYEAvXFClSExFaNvOz5eowwn09mOtgWEJEU6TFBEJNVoCaqavQlpelIyLGMA/+KP6Xg3ZvbfPTf1iTQhX3nLhOBEV4DanUo+9SH+96j0petltFgzkogf7vw3NHDf/bAuYAvyecrhRXAnRSy/YIAqF/p3nZDird3vSVgvdkjKtAUGrsc=";

            params.put("payType", payType);
            params.put("companyId", companyId);
            params.put("receiptTime", receiptTime);
            params.put("merchantId", "aiyuangong");

            OpenApiClient client = null;
            List<ReceiptCommission> listReceipt = receiptService.listReceiptCommission(params);
            for (ReceiptCommission commission : listReceipt) {
                
            	ChannelRelated related = channelRelatedDao.getRelatedByCompAndOrig(commission.getOriginalId(), commission.getCompanyId());
            	String aygAppId = related.getAppIdAyg();
        		logger.info("-----手工----回单-----爱员工appid------------:" + aygAppId);
        		client = ClientMapUtil.httpClient.get(aygAppId);

        		if(client == null){
        			synchronized(ClientMapUtil.httpClient){
        				client = ClientMapUtil.httpClient.get(aygAppId);
        				if(client == null){
        					client = new OpenApiClient.Builder().appId(aygAppId).privateKey(priKeyString).build();
        					ClientMapUtil.httpClient.putIfAbsent(aygAppId, client);
        				}
        			}
        		}
            	
                ReceiptRequestParam param = new ReceiptRequestParam();
                param.setReqNo(orderNoUtil.getChannelSerialno());
                param.setNotifyUrl(baseInfo.getDomainName() + "/receipt/receiptAsyncNotify.do");
                param.setOutOrderNo(commission.getOrderNo());
                param.setAttach("回单");
                OpenApiBaseResponse<BaseResponseResult<String>> response = client.execute(param);
                logger.info("-----手工----回单-----爱员工response------------:" + response);
                String aygReceiptStatus;
                String aygCode = response.getCode();
                if (OpenApiBaseResponse.SUCCESS_CODE.equals(aygCode)) {
                    aygReceiptStatus = "1";
                } else if ("2002".equals(aygCode) || "2101".equals(aygCode)) {
                    aygReceiptStatus = "2";
                } else {
                    aygReceiptStatus = "3";
                }

                params.clear();
                params.put("aygRreceiptStatus", aygReceiptStatus);
                params.put("accountDate", commission.getAccountDate());
                params.put("receiptNo", commission.getReceiptNo());
                receiptService.updateReceiptCommissionByReceiptNo(params);

            }
            result.put(RespCode.RESP_STAT, respstat);
    	}catch(Exception e){
          logger.error(e.getMessage(),e);
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, e.getMessage());
    	}
    	
    	return result;
    }

    /**
     * 触发jobservice  aygRecepitBatchJob  回单处理--爱员工批次
     */
    @RequestMapping(value = "/aygReceiptBatchJob")
    @ResponseBody
    public Map<String, Object> receiptCommissionUpdate() {
        receiptService.initAygRecepitBatchJob();
        return returnSuccess();
    }


    public static void main(String[] args) throws UnsupportedEncodingException {
        logger.info("");
    }
}
