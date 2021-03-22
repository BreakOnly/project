package com.jrmf.controller.receipt;

import static com.jrmf.payment.PaymentFactory.MYBANK;
import static com.jrmf.payment.PaymentFactory.PAKHKF;
import static com.jrmf.payment.PaymentFactory.PAYQZL;

import com.jrmf.bankapi.ReceiptFileResult;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.*;
import com.jrmf.domain.*;
import com.jrmf.payment.entity.PingAnBankYqzl;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.model.request.deliver.ReceiptRequestParam;
import com.jrmf.payment.openapi.model.response.BaseResponseResult;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.payment.util.ClientMapUtil;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.service.*;
import com.jrmf.utils.*;
import com.jrmf.utils.ftp.FTPClientUtil;
import com.jrmf.utils.threadpool.ThreadUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2019/1/22 15:40
 * Version:1.0
 */
@Controller
@RequestMapping("/receipt")
public class ReceiptController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ReceiptController.class);

    private BaseInfo baseInfo;
    private OrderNoUtil orderNoUtil;
    private ReceiptService receiptService;
    private ChannelCustomService customService;
    private OrganizationTreeService organizationTreeService;
    private final FTPClientUtil ftpClientUtil;
    private ChannelRelatedDao channelRelatedDao;
    private UserCommissionService userCommissionService;
    private BestSignConfig bestSignConfig;
    private CompanyService companyService;
    private OemConfigService oemConfigService;


    @Autowired
    public ReceiptController(ReceiptService receiptService, ChannelCustomService customService, OrganizationTreeService organizationTreeService, BaseInfo baseInfo, OrderNoUtil orderNoUtil, FTPClientUtil ftpClientUtil, ChannelRelatedDao channelRelatedDao, UserCommissionService userCommissionService, BestSignConfig bestSignConfig, CompanyService companyService,OemConfigService oemConfigService) {
        this.baseInfo = baseInfo;
        this.orderNoUtil = orderNoUtil;
        this.customService = customService;
        this.receiptService = receiptService;
        this.ftpClientUtil = ftpClientUtil;
        this.channelRelatedDao = channelRelatedDao;
        this.userCommissionService = userCommissionService;
        this.organizationTreeService = organizationTreeService;
        this.bestSignConfig = bestSignConfig;
        this.companyService = companyService;
        this.oemConfigService = oemConfigService;
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 交易电子回单页面，回单明细查询。
     * Date 15:55 2019/1/22
     * Param [session, customName, contentName, timeStart, timeEnd, companyId, batchName, batchDesc, amountStart, amountEnd, payType, pageNo, pageSize, userName, certId, account]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @RequestMapping("/list")
    @ResponseBody
    public Map<String, Object> receiptList(HttpSession session,
                                           String companyName,
                                           String contentName,
                                           String timeStart,
                                           String timeEnd,
                                           String batchName,
                                           String batchDesc,
                                           @RequestParam(defaultValue = "0") Integer companyId,
                                           @RequestParam(defaultValue = "0") Integer payType,
                                           @RequestParam(defaultValue = "0") Integer customType,
                                           @RequestParam(defaultValue = "0") Integer nodeId,
                                           @RequestParam(defaultValue = "1") Integer pageNo,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           String amountStart,
                                           String amountEnd,
                                           String userName,
                                           String certId,
                                           String account) {
        Map<String, Object> params = new HashMap<>(20);
        ChannelCustom custom = customService.getCustomByCustomkey((String) session.getAttribute(CommonString.CUSTOMKEY));
        String customKey = (String)session.getAttribute(CommonString.CUSTOMKEY);
        ChannelCustom byCustomkey = customService.getCustomByCustomkey(customKey);
        if (!CommonString.ROOT.equals(customKey)) {
            if(CustomType.PROXY.getCode() == byCustomkey.getCustomType()){
                customType = CustomType.PROXY.getCode();
            }
            List<String> strings = organizationTreeService.queryNodeCusotmKey(customType, "G", nodeId);
            StringBuilder customKeys = new StringBuilder();
            for (String str : strings) {
                customKeys.append(str).append(",");
            }
            params.put("customKey", customKeys.toString());
        }
        params.put("customName", companyName);
        params.put("contentName", contentName);
        params.put("timeStart", timeStart);
        params.put("timeEnd", timeEnd);
        params.put("companyId", companyId);
        if (custom.getCustomType() == CustomType.COMPANY.getCode()) {
            params.put("companyId", custom.getCustomkey());
        }
        params.put("batchName", batchName);
        params.put("batchDesc", batchDesc);
        params.put("amountStart", amountStart);
        params.put("payType", payType);
        params.put("amountEnd", amountEnd);
        params.put("userName", userName);
        params.put("certId", certId);
        params.put("account", account);
        int total = receiptService.listReceiptCommissionCount(params);
        params.put("start", (pageNo - 1) * pageSize);
        params.put("limit", pageSize);
        List<ReceiptCommission> list = receiptService.listReceiptCommission(params);
        return returnSuccess(list, total);
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 批量下在回单记录。
     * Date 11:39 2019/1/23
     * Param [receivingMail, customName, status, pageNo, pageSize, orgAccount, timeStart, timeEnd]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @RequestMapping("/download/history")
    @ResponseBody
    public Map<String, Object> receiptBatchList(HttpSession session,
                                                String receivingMail,
                                                String orgName,
                                                @RequestParam(defaultValue = "0") Integer status,
                                                @RequestParam(defaultValue = "1") Integer pageNo,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                String orgAccount,
                                                String timeStart,
                                                String timeEnd) {
        ChannelCustom loginUser = (ChannelCustom) session.getAttribute(CommonString.CUSTOMLOGIN);
        Map<String, Object> params = new HashMap<>(10);
        params.put("receivingMail", receivingMail);
        params.put("orgName", orgName);
        params.put("status", status);
        params.put("orgAccount", orgAccount);
        params.put("timeStart", timeStart);
        params.put("timeEnd", timeEnd);
        if (!CommonString.ROOT.equals(session.getAttribute(CommonString.CUSTOMKEY))) {
            params.put("orgAccount", loginUser.getUsername());
        }
        int total = receiptService.listDownloadHistory(params).size();
        params.put("start", (pageNo - 1) * pageSize);
        params.put("limit", pageSize);
        List<ReceiptDownLoad> list = receiptService.listDownloadHistory(params);
        return returnSuccess(list, total);
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 交易电子回单页面，回单明细查询导出。
     * Date 9:48 2019/1/23
     * Param [customName, contentName, timeStart, timeEnd, batchName, batchDesc, companyId, payType, customType, nodeId, pageNo, pageSize, amountStart, amountEnd, userName, certId, account]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @RequestMapping("/list/excel")
    public void receiptListExcel(HttpSession session,
                                 HttpServletResponse response,
                                 String companyName,
                                 String contentName,
                                 String timeStart,
                                 String timeEnd,
                                 String batchName,
                                 String batchDesc,
                                 @RequestParam(defaultValue = "0") Integer companyId,
                                 @RequestParam(defaultValue = "0") Integer payType,
                                 @RequestParam(defaultValue = "0") Integer customType,
                                 @RequestParam(defaultValue = "0") Integer nodeId,
                                 String amountStart,
                                 String amountEnd,
                                 String userName,
                                 String certId,
                                 String account) {
        Map<String, Object> params = new HashMap<>(20);
        if (!CommonString.ROOT.equals(session.getAttribute(CommonString.CUSTOMKEY))) {
            List<String> strings = organizationTreeService.queryNodeCusotmKey(customType, "G", nodeId);
            StringBuilder customKeys = new StringBuilder();
            for (String str : strings) {
                customKeys.append(str).append(",");
            }
            params.put("customKey", customKeys.toString());
        }
        params.put("customName", companyName);
        params.put("contentName", contentName);
        params.put("timeStart", timeStart);
        params.put("timeEnd", timeEnd);
        params.put("companyId", companyId);
        ChannelCustom custom = customService.getCustomByCustomkey((String) session.getAttribute(CommonString.CUSTOMKEY));
        if (custom.getCustomType() == CustomType.COMPANY.getCode()) {
            params.put("companyId", custom.getCustomkey());
        }
        params.put("batchName", batchName);
        params.put("batchDesc", batchDesc);
        params.put("amountStart", amountStart);
        params.put("payType", payType);
        params.put("amountEnd", amountEnd);
        params.put("userName", userName);
        params.put("certId", certId);
        params.put("account", account);
        List<ReceiptCommission> list = receiptService.listReceiptCommission(params);
        String[] colunmName = new String[]{"商户名称", "项目名称", "订单ID", "收款人姓名",
                "证件类型", "证件号", "收款账号", "交易金额", "交易时间",
                "订单状态", "订单状态描述", "订单备注", "下发通道", "服务公司",
                "账号所属金融机构", "批次名称", "批次说明", "最后更新时间"};
        String filename = "交易电子回单统计表";
        List<Map<String, Object>> data = new ArrayList<>();
        for (ReceiptCommission commission : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", commission.getCustomName());
            dataMap.put("2", commission.getContentName());
            dataMap.put("3", commission.getOrderNo());
            dataMap.put("4", commission.getUserName());
            dataMap.put("5", CertType.codeOf(commission.getDocumentType()).getDesc());
            dataMap.put("6", commission.getCertId());
            dataMap.put("7", commission.getAccount());
            dataMap.put("8", commission.getAmount());
            dataMap.put("9", commission.getPaymentTime());
            dataMap.put("10", CommissionStatus.codeOf(commission.getStatus()).getDesc());
            dataMap.put("11", commission.getStatusDesc());
            dataMap.put("12", commission.getRemark());
            dataMap.put("13", PayType.codeOf(commission.getPayType()).getDesc());
            dataMap.put("14", commission.getCompanyName());
            dataMap.put("15", commission.getBankName());
            dataMap.put("16", commission.getBatchName());
            dataMap.put("17", commission.getBatchDesc());
            dataMap.put("18", commission.getUpdatetime());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 回单处理任务查询
     * zh
     * 2019-01-2310:41
     */
    @PostMapping(value = "listReceiptBatch")
    @ResponseBody
    public Map<String, Object> listReceiptBatch(HttpServletRequest request,
                                                String companyId,
                                                String payType,
                                                String receiptOrgType,
                                                String receiptOrgName,
                                                String status,
                                                String receiptTimeStart,
                                                String receiptTimeEnd,
                                                String createtimeStart,
                                                String createtimeEnd,
                                                @RequestParam(defaultValue = "1") Integer pageNo,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {

        String customkey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
        logger.info("customkey:" + customkey);
        List<ReceiptBatch> list = null;
        int total = 0;
        if ("mfkj".equals(customkey)) {
            Map<String, Object> params = new HashMap<>(20);
            params.put("companyId", companyId);
            params.put("payType", payType);
            params.put("receiptOrgType", receiptOrgType);
            params.put("receiptOrgName", receiptOrgName);
            params.put("status", status);
            params.put("receiptTimeStart", receiptTimeStart);
            params.put("receiptTimeEnd", receiptTimeEnd);
            params.put("createtimeStart", createtimeStart);
            params.put("createtimeEnd", createtimeEnd);

            total = receiptService.listReceiptBatch(params).size();
            params.put("start", (pageNo - 1) * pageSize);
            params.put("limit", pageSize);

            list = receiptService.listReceiptBatch(params);
        } else {
            Map<String, Object> params = new HashMap<>(20);
            params.put("companyId", customkey);
            params.put("payType", payType);
            params.put("receiptOrgType", receiptOrgType);
            params.put("receiptOrgName", receiptOrgName);
            params.put("status", status);
            params.put("receiptTimeStart", receiptTimeStart);
            params.put("receiptTimeEnd", receiptTimeEnd);
            params.put("createtimeStart", createtimeStart);
            params.put("createtimeEnd", createtimeEnd);

            total = receiptService.listReceiptBatch(params).size();
            params.put("start", (pageNo - 1) * pageSize);
            params.put("limit", pageSize);

            list = receiptService.listReceiptBatch(params);
        }
        return returnSuccess(list, total);
    }

    /**
     * Author zhagnhuan
     * Description //TODO 回单处理--手工导入
     * Date 14:29 2019/1/23
     * Param [request, file]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @PostMapping(value = "/inputReceiptBatch")
    @ResponseBody
    public Map<String, Object> inputReceiptBatch(HttpServletRequest request, MultipartFile file) {

        InputStream is = null;
        ByteArrayOutputStream bytesOut = null;
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>();

        try {
            String payType = request.getParameter("payType");
            String companyId = request.getParameter("companyId");
            String receiptTime = request.getParameter("receiptTime");
            String receiptOrgType = request.getParameter("receiptOrgType");
            String receiptOrgName = request.getParameter("receiptOrgName");
            String receiptImportType = request.getParameter("receiptImportType");

            if (StringUtil.isEmpty(payType)
                    && StringUtil.isEmpty(companyId)
                    && StringUtil.isEmpty(receiptTime)
                    && StringUtil.isEmpty(receiptOrgType)
                    && StringUtil.isEmpty(receiptOrgName)) {
                respstat = RespCode.error101;
                result.put(RespCode.RESP_STAT, respstat);
                result.put(RespCode.RESP_MSG, "请补全回单处理信息！");
                return result;
            }

            if (file == null) {
                respstat = RespCode.error101;
                result.put(RespCode.RESP_STAT, respstat);
                result.put(RespCode.RESP_MSG, "上传文件不能为空！");
                return result;
            } else {

                Map<String, Object> params = new HashMap<>(20);
                params.put("payType", payType);
                params.put("companyId", companyId);
                params.put("receiptTime", receiptTime);
                params.put("receiptOrgType", receiptOrgType);
                params.put("receiptImportType", receiptImportType);

                ReceiptBatch receiptBatch;

                List<ReceiptBatch> listReceiptBatch = receiptService.listReceiptBatch(params);
                params.clear();
                if (listReceiptBatch == null || listReceiptBatch.size() <= 0) {
                    respstat = RespCode.error101;
                    result.put(RespCode.RESP_STAT, respstat);
                    result.put(RespCode.RESP_MSG, "无对应回单处理信息！");
                    return result;
                } else {
                    receiptBatch = listReceiptBatch.get(0);
                }

                //回单明细全部更新待勾对，回单批次为处理中
                receiptBatch.setStatus(2);//更新批次状态---处理中
                if (!StringUtil.isEmpty(receiptImportType)) {
                    receiptBatch.setReceiptImportType(Integer.valueOf(receiptImportType));
                }
                receiptService.updateReceiptBatch(receiptBatch);

                //更新明细状态---待勾对
                String yearMon = receiptBatch.getReceiptTime().substring(0, 7);

                Company company = companyService.getCompanyByUserId(receiptBatch.getCompanyId());
                //真实下发公司id
                String realCompanyId = company.getRealCompanyId();
                if (StringUtil.isEmpty(realCompanyId)) {
                    realCompanyId = String.valueOf(company.getUserId());
                }

                String pathPdfDir = "/" + realCompanyId + "/" + receiptBatch.getPayType() + "_" + receiptBatch.getReceiptOrgName() + "/" + yearMon;
                params.put("payType", payType);
                params.put("companyId", companyId);
                params.put("merchantId", receiptBatch.getMerchantId());
                params.put("receiptTime", receiptTime);
                params.put("receiptChecked", "0");//回单明细全部更新待勾对
                receiptService.updateReceiptCommission(params);

                is = file.getInputStream();
                int readLen;
                byte[] byteBuffer = new byte[1024];
                bytesOut = new ByteArrayOutputStream();

                while ((readLen = is.read(byteBuffer)) > -1) {
                    bytesOut.write(byteBuffer, 0, readLen);
                }
                byte[] fileData = bytesOut.toByteArray();

                //调用服务上传并解析分片pdf文件
                String batchId = receiptBatch.getId() + "";
                ThreadUtil.cashThreadPool.execute(() -> receiptService.partitionPdfFile(fileData, batchId, pathPdfDir));
            }
        } catch (Exception e) {
            logger.error("", e);
            respstat = RespCode.error101;
            logger.error(e.getMessage());
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "手工导入失败");
            return result;
        } finally {
            try {
                if (bytesOut != null) {
                    bytesOut.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
        result.put(RespCode.RESP_STAT, respstat);
        logger.info("返回结果：" + result);
        return result;
    }

    @PostMapping(value = "/excuteAgain")
    @ResponseBody
    public Map<String, Object> excuteAgain(HttpServletRequest request,
                                           HttpServletResponse response) {

        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>();

        String id = request.getParameter("id");
        ReceiptBatch receiptBatch = receiptService.getReceiptBatchById(Integer.valueOf(id));//非处理状态
        if(receiptBatch.getStatus() == 2){
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "回单任务处理中！");
            return result;
        }

        String priKeyString = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCcdJ+nqtInOaHn/fZTbxdj5oTMFU6AJKD3DWmoYqUGcMCebtpdKhT/BbAb/t8Rsp1QLv1r4h+nf7s98LZkV9J6dSa5iHDIqCGT12byaTftKrYZlf3Hr8SFDkzBPTSBz56LRnYCRaYlrMQgZgY9U/T5UIwKVhJEordPvwbH2MqueKxUdk9vD4S8Dj7rEgor31Ifc3E/qRivKcRCncWBpiAtOTNSPLtsx0TIxizPXYicjL8Tr2+eLnuUxjKhWnFxvpRMC2ghLVrsWiVU0oW/0a6Qbv+TtB4/xR1JWV+pn4fqQhiJAssMHN4kEn7JxPpXB+/MzWUfrrGsYnSlNwpxPojzAgMBAAECggEAYJNEsfSpwHi8zj1fveTHJW1375n/WO5DRfzLiZtKjo0u+R0oQXXme/0A1mcfPwdoP8ShveRY8cXQyM07aPkk/V4vRztHkzTldSLzcxMr6IQC4AxMGOUQg6luC6JCNRb5oLMfyQtBIeRhNDaGB3k5sGPd7ctvf1qJmPorr1TM16C9vW8MdF4YjOfxpvm2g5AuaFHB3O9jTd6+tN3oAiusC3TwjtvhfQDmnPqfJOdp2x2NZAf47luU7J/eiEvyEPkjfmwSIQKvXfzIw0uu2rLX+52OmtJlJ8DYF8Ep9iCSJJMLiHhxYkSkNuwyB5/5oVZmRGj8mZVNdfIXyCaFXnEw0QKBgQDzZFAYCg+QRQZx9fdWwM4mSeHRCeZpQ9QBGjwNN0yWF+XioNtnMIr/pZYV/25biVVF5Pc/20r/8ZSBCShgR3u1ePuoaxzU907lT3R7tBoqaIfvjEBaZuOQcaGJqvNNJnySmhP1PnsjD5x9wWwer7nJLvl8UqmgbED0gKoClfWopwKBgQCkj2ypjvo6fHg19huw96y7CaYQjljWslY6GsWzE1WqSVy0xJb6JEzARZe38XhXPiAeg9mhlyUfkum8LJaYGe1JA5a2c9qhUOPfFeGM6PblGcF1jlMZYLv8shhxkjeWQA/kJngbW6lCHQBbc2G/W7irUC+PbkZkknAcYkYlQE3a1QKBgD9fVxtrQzIlRtBVYtlLymFdy1ZKZZvy9Th0RD6Mr3xFLK4dhAMSOJ7n1nRT1cAvuexA+b++sYCCvk/6unCXLDbMEXqAqTkqS3iZf5LWChoQrZRJyFfBgm8RpyXZRRBJfRYO2DN62UT/w5dazXQP/SfM+1jLjS8gAKmo9ptFwHjxAoGAK3y3g4uENwaDogb6xGZ/YCIpn4Bum7YfMVW33x4B6nFerWqyV0JWgg0iDfsjCTMiu82uKpTNu61QVWkXFvTrDvuCzY6KPU0qGt8mbt11uY9334AQF8nHg/zwlrrEM9GUIX/FB73OWeleGczBDRfJEoSrPOUwdw130RhrXxbCPE0CgYEAvXFClSExFaNvOz5eowwn09mOtgWEJEU6TFBEJNVoCaqavQlpelIyLGMA/+KP6Xg3ZvbfPTf1iTQhX3nLhOBEV4DanUo+9SH+96j0petltFgzkogf7vw3NHDf/bAuYAvyecrhRXAnRSy/YIAqF/p3nZDird3vSVgvdkjKtAUGrsc=";

        Map<String, Object> params = new HashMap<String, Object>();
        params.clear();
        params.put("companyId", receiptBatch.getCompanyId());
        params.put("receiptTime", receiptBatch.getReceiptTime());
        params.put("payType", receiptBatch.getPayType());
        params.put("merchantId", receiptBatch.getMerchantId());

        OpenApiClient client = null;
        List<ReceiptCommission> listReceipt = receiptService.listReceiptCommission(params);
        for (ReceiptCommission commission : listReceipt) {

            ChannelRelated related = channelRelatedDao.getRelatedByCompAndOrig(commission.getOriginalId(), commission.getCompanyId());
            String aygAppId = related.getAppIdAyg();
            logger.info("----回单-----爱员工appid------------:" + aygAppId);
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
            OpenApiBaseResponse<BaseResponseResult<String>> response2 = client.execute(param);

            String aygReceiptStatus;
            String aygCode = response2.getCode();
            if(OpenApiBaseResponse.SUCCESS_CODE.equals(aygCode)){
                aygReceiptStatus = "1";
            }else if("2002".equals(aygCode) || "2101".equals(aygCode)){
                aygReceiptStatus = "2";
            }else{
                aygReceiptStatus = "3";
            }

            params.clear();
            params.put("aygRreceiptStatus", aygReceiptStatus);
            params.put("accountDate", commission.getAccountDate());
            params.put("receiptNo", commission.getReceiptNo());
            receiptService.updateReceiptCommissionByReceiptNo(params);

        }

        receiptBatch.setStatus(2);//更新批次状态---处理中
        receiptService.updateReceiptBatch(receiptBatch);

        ThreadUtil.pdfThreadPool.execute(new Runnable(){
            @Override
            public void run(){
                try {
                    logger.info("----回单文件重新执行---批次更新等待");
                    Thread.sleep(3600000);

                    logger.info("----回单文件重新执行---批次更新开始");
                    Map<String, Object> params = new HashMap<>(20);
                    params.put("payType", receiptBatch.getPayType());
                    params.put("status", "1");
                    params.put("companyId", receiptBatch.getCompanyId());
                    params.put("merchantId", receiptBatch.getMerchantId());
                    params.put("receiptTime", receiptBatch.getReceiptTime());

                    List<ReceiptBatch> receiptBatchList = receiptService.listReceiptBatchGroup(params);
                    for(ReceiptBatch batch : receiptBatchList){//根据支付通道、服务公司分组插入

                        params.clear();
                        params.put("companyId", batch.getCompanyId());
                        params.put("receiptTime", batch.getReceiptTime());
                        params.put("payType", batch.getPayType());
                        params.put("merchantId", batch.getMerchantId());
                        List<ReceiptCommission> listReceipt = receiptService.listReceiptCommission(params);

                        int receiptMatchNum = 0;
                        Map<String, Object> receiptCommissions = new HashMap<String, Object>();
                        for(ReceiptCommission commission : listReceipt){

                            String yearMon = commission.getAccountDate().substring(0,7);
                            String relativePath =  "/" + commission.getCompanyId() + "/"+ commission.getPayType() + "_" + batch.getReceiptOrgName() + "/" + yearMon + "/" + commission.getAccountDate() + "_" + commission.getReceiptNo() + ".pdf";
                            String receiptFile = "/receipt" + relativePath;
                            logger.info("----回单文件路径：" + receiptFile);

                            receiptCommissions.put("id", commission.getId());
                            boolean isFileExists = ftpClientUtil.isExists(receiptFile);
                            logger.info("----回单文件路径存在判断isFileExists：" + isFileExists);
                            if(isFileExists){
                                receiptMatchNum ++;
                                receiptCommissions.put("receiptUrl", relativePath);
                                receiptCommissions.put("receiptChecked", "1");
                            }else{
                                receiptCommissions.put("receiptChecked", "2");
                                logger.info("----回单文件不存在：" + receiptFile + "----勾兑失败----");
                            }
                            receiptService.updateReceiptCommissionById(receiptCommissions);
                            receiptCommissions.clear();
                        }

                        batch.setReceiptMatchNum(receiptMatchNum + "");
                        if(listReceipt.size() == receiptMatchNum && receiptMatchNum !=0){
                            batch.setStatus(1);//全部成功
                        }else if(receiptMatchNum > 0 && receiptMatchNum != listReceipt.size()){
                            batch.setStatus(3);//部分成功
                        }else if(receiptMatchNum == 0){
                            batch.setStatus(4);//全部失败
                        }
                        receiptService.updateReceiptBatch(batch);

                    }
                    logger.info("----回单文件重新执行---批次更新结束");
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(),e);
                }
            }
        });

        result.put(RespCode.RESP_STAT, respstat);
        logger.info("返回结果：" + result);
        return result;
    }

    @RequestMapping(value = "/batchReceiptExport")
    @ResponseBody
    public void batchReceiptExport(HttpSession session, HttpServletResponse response,
                                   @RequestParam(defaultValue = "0") Integer payType,
                                   @RequestParam(defaultValue = "0") Integer customType,
                                   @RequestParam(defaultValue = "0") Integer nodeId,
                                   String receiptTime,
                                   String status,
                                   String companyName,
                                   String receiptOrgType,
                                   String receiptOrgName,
                                   String commissionNum,
                                   String receiptNum,
                                   String receiptMatchNum,
                                   String receiptType,
                                   String createTime,
                                   String updateTime) {
        Map<String, Object> params = new HashMap<>();
//        if (!CommonString.ROOT.equals(session.getAttribute(CommonString.CUSTOMKEY))) {
//            List<String> strings = organizationTreeService.queryNodeCusotmKey(customType, "G", nodeId);
//            StringBuilder customKeys = new StringBuilder();
//            for (String str : strings) {
//                customKeys.append(str).append(",");
//            }
//            params.put("customKey", customKeys.toString());
//        }
        ChannelCustom custom = customService.getCustomByCustomkey((String) session.getAttribute(CommonString.CUSTOMKEY));
        if (custom.getCustomType() == CustomType.COMPANY.getCode()) {
            params.put("companyId", custom.getCustomkey());
        }
        params.put("receiptTime", receiptTime);
        params.put("status", status);
        params.put("companyName", companyName);
        params.put("payType", payType);
        params.put("receiptOrgType", receiptOrgType);
        params.put("receiptOrgName", receiptOrgName);
        params.put("commissionNum", commissionNum);
        params.put("receiptNum", receiptNum);
        params.put("receiptMatchNum", receiptMatchNum);
        params.put("receiptType", receiptType);
        params.put("createTime", createTime);
        params.put("updateTime", updateTime);
        List<ReceiptBatch> list = receiptService.listReceiptBatch(params);
        String[] colunmName = new String[]{"回单日期", "处理状态", "服务公司", "下发通道",
                "回单机构凭证类型", "回单凭证机构名称", "成功交易笔数", "回单文件笔数", "回单匹配笔数",
                "回单处理方式", "创建时间", "最后一次更新时间"};
        String filename = "交易回单处理表";
        List<Map<String, Object>> data = new ArrayList<>();
        for (ReceiptBatch receiptBatch : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", receiptBatch.getReceiptTime());
            dataMap.put("2", ReceiptStatus.codeOf(Integer.parseInt(String.valueOf(receiptBatch.getStatus()))).getDesc());
            dataMap.put("3", receiptBatch.getCompanyName());
            dataMap.put("4", PayType.codeOf(Integer.parseInt(String.valueOf(receiptBatch.getPayType()))).getDesc());
            dataMap.put("5", ReceiptOrganizationType.codeOf(Integer.parseInt(receiptBatch.getReceiptOrgType())).getDesc());
            dataMap.put("6", receiptBatch.getReceiptOrgName());
            dataMap.put("7", receiptBatch.getCommissionNum());
            dataMap.put("8", receiptBatch.getReceiptNum());
            dataMap.put("9", receiptBatch.getReceiptMatchNum());
            dataMap.put("10", ReceiptType.codeOf(Integer.parseInt(String.valueOf(receiptBatch.getReceiptType()))).getDesc());
            dataMap.put("11", receiptBatch.getCreateTime());
            dataMap.put("12", receiptBatch.getUpdateTime());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    @RequestMapping("/batch/receipt/download")
    @ResponseBody
    public Map<String, Object> batchReceiptDownload(HttpSession session,
                                                    HttpServletRequest request,
                                                    String companyName,
                                                    String contentName,
                                                    String timeStart,
                                                    String timeEnd,
                                                    String batchName,
                                                    String batchDesc,
                                                    @RequestParam(defaultValue = "0") Integer companyId,
                                                    @RequestParam(defaultValue = "4") Integer payType,
                                                    @RequestParam(defaultValue = "0") Integer customType,
                                                    @RequestParam(defaultValue = "0") Integer nodeId,
                                                    String amountStart,
                                                    String amountEnd,
                                                    String userName,
                                                    String certId,
                                                    String account,
                                                    String email) {
        Map<String, Object> params = new HashMap<>(20);
        params.put("customName", companyName);
        params.put("contentName", contentName);
        params.put("timeStart", timeStart);
        params.put("timeEnd", timeEnd);
        params.put("companyId", companyId);
        ChannelCustom custom = customService.getCustomByCustomkey((String) session.getAttribute(CommonString.CUSTOMKEY));
        if (custom.getCustomType() == CustomType.COMPANY.getCode()) {
            params.put("companyId", custom.getCustomkey());
        }
        params.put("batchName", batchName);
        params.put("batchDesc", batchDesc);
        params.put("amountStart", amountStart);
        params.put("payType", payType);
        params.put("amountEnd", amountEnd);
        params.put("userName", userName);
        params.put("certId", certId);
        params.put("account", account);

        Map<String, Object> map = new HashMap<>(2);
        map.put("portalDomain", request.getServerName());
        OemConfig oemConfig = oemConfigService.getOemByParam(map);

        String title = "【" + oemConfig.getOemName() + "】批量下载回单文件";

        ThreadUtil.cashThreadPool.execute(new InitEmailSendInfo(new String[]{email},
                organizationTreeService,
                params,
                customType,
                nodeId,
                timeStart,
                timeEnd,
                email,
                session,
                customService,
                receiptService,request.getServerName(),title));
        return returnSuccess();
    }



    @RequestMapping("/test")
    public void test() {
        String file = "/receipt/997/4_ayg/2019-04/2019-04-22_000346.pdf";
        boolean exists = ftpClientUtil.isExists(file);
        logger.error(exists+"");
        return;

    }


    @PostMapping(value = "/pingan/excuteAgain")
    @ResponseBody
    public Map<String, Object> pinganExcuteAgain(Integer id) {

        ReceiptBatch receiptBatch = receiptService.getReceiptBatchById(id);//非处理状态
        if (ReceiptStatus.INHAND.getCode() == receiptBatch.getStatus()) {
            return returnFail(RespCode.error101, "回单任务处理中！");
        }

        if (ReceiptStatus.SUCCESS.getCode() == receiptBatch.getStatus()) {
            return returnFail(RespCode.error101, "回单状态为成功，无需重新生成！");
        }

        try {

            List<PaymentConfig> paymentConfigs = companyService.getSubAccountPaymentConfig();
            if (paymentConfigs != null && paymentConfigs.size() > 0) {
                String fileName = "";
                String date = DateUtils
                    .formartDateStr(receiptBatch.getReceiptTime(), "yyyy-MM-dd", "yyyyMMdd");

                for (PaymentConfig paymentConfig : paymentConfigs) {
                    if (paymentConfig.getCompanyId() == receiptBatch.getCompanyId()) {

                        BestSignConfig ftpConfig = null;

                        switch (paymentConfig.getPathNo()) {
                            case PAKHKF:
                            case PAYQZL:
                                PingAnBankYqzl pingAnBank = new PingAnBankYqzl(paymentConfig);
                                List<ReceiptFileResult> receiptFileResultList = pingAnBank
                                    .queryTransHistoryFile(date);
                                if (receiptFileResultList != null && receiptFileResultList.size() > 0) {
                                    logger.info("-------------------------调用平安通道开始下载-------------------------------");
                                    for (ReceiptFileResult result : receiptFileResultList) {
                                        if (result.getFileName().trim().equals(
                                            "RECPDF_" + paymentConfig.getCorporationAccount() + "_" + date + ".zip")) {
                                            fileName = result.getFileName();
                                        }
                                        logger.info("-----------resultFileName------------------:" + result.getFileName()
                                            + ",FilePath" + result.getFilePath());
                                        pingAnBank.DowloadQueryTransHistoryFile(
                                            "YQT" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()),
                                            result.getFileName(), result.getRandomPwd());
                                    }
                                    logger.info("--------------------调用平安通道下载完毕---------------------------");

                                }

                                receiptBatch.setReceiptImportType(ReceiptImportType.PINGANBANKONE.getCode());
                                break;
                            case MYBANK:
                                String ftpPath = File.separator + paymentConfig.getParameter8() + File.separator + date;
                                ftpConfig = new BestSignConfig(paymentConfig.getParameter4(),
                                    paymentConfig.getParameter5(), ftpPath,
                                    paymentConfig.getParameter6(),
                                    paymentConfig.getParameter7());
                                fileName = paymentConfig.getParameter8() + "+" + date + "+000001.zip";
                                receiptBatch.setReceiptImportType(ReceiptImportType.MYBANKONE.getCode());
                                break;
                        }

                        receiptService.autoImportReceipt(receiptBatch, fileName,ftpConfig);

                        break;
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return returnSuccess();

    }

}