package com.jrmf.controller.jobtest;

import com.jrmf.bankapi.ReceiptFileResult;
import com.jrmf.domain.AutoImportReceipt;
import com.jrmf.domain.Company;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.ReceiptBatch;
import com.jrmf.payment.entity.PingAnBankYqzl;
import com.jrmf.service.CompanyService;
import com.jrmf.service.ReceiptService;
import com.jrmf.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: <br/>
 * @author: <br/>
 * @create：2020年05⽉28⽇<br/>
 */
@Slf4j
@RestController
@RequestMapping(value = "/Job")
public class JobTest {


    @Autowired
    private JmsTemplate providerJmsTemplate;

    @Autowired
    private Destination autoImportReceiptDestination;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private ReceiptService receiptService;

    public static final String PROCESS = "process";

    @RequestMapping(value = "/receiptFileJob")
    public void receiptFileJob(String receiptTime, String date) {

        InputStream is = null;
        ByteArrayOutputStream bytesOut = null;

//		String date = DateUtils.getBeforeDayString(1).replace("-", "");
//		String receiptTime = DateUtils.getBeforeDayString(1);
//        String date = "20200526";
//        String receiptTime = "2020-05-26";
        String fileName = "";

        try {
            List<PaymentConfig> paymentConfigs = companyService.getSubAccountPaymentConfig();
            log.info("--------------------自动导入回单begin---------------------时间：{}", date);

            if (paymentConfigs != null && paymentConfigs.size() > 0) {
                for (PaymentConfig paymentConfig : paymentConfigs) {
                    log.info("------服务公司id------:{}", paymentConfig.getCompanyId());
                    PingAnBankYqzl pingAnBank = new PingAnBankYqzl(paymentConfig);
                    List<ReceiptFileResult> receiptFileResultList = pingAnBank.queryTransHistoryFile(date);
                    if (receiptFileResultList != null && receiptFileResultList.size() > 0) {
                        log.info("-------------------------调用平安通道开始下载-------------------------------");
                        for (ReceiptFileResult result : receiptFileResultList) {
                            if (result.getFileName().substring(result.getFileName().length() - 16).equals("010_" + date + ".pdf")) {
                                fileName = result.getFileName();
                            }
                            log.info("-----------resultFileName------------------:" + result.getFileName() + ",FilePath" + result.getFilePath());
                            pingAnBank.DowloadQueryTransHistoryFile("YQT" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()), result.getFileName(), result.getRandomPwd());
                        }
                        log.info("--------------------调用平安通道下载完毕---------------------------");


                        Map<String, Object> params = new HashMap<>(20);
                        params.put("payType", 4);
                        params.put("companyId", paymentConfig.getCompanyId());
                        params.put("receiptTime", receiptTime);
                        params.put("receiptOrgName", "pa");

                        ReceiptBatch receiptBatch;

                        List<ReceiptBatch> listReceiptBatch = receiptService.listReceiptBatch(params);
                        params.clear();
                        if (listReceiptBatch == null || listReceiptBatch.size() <= 0) {
                            log.info("无对应回单处理信息！companyId:" + paymentConfig.getCompanyId() + "日期：" + date);
                            continue;
                        }

                        // TODO 这里是批次，多个批次为什么get（0）
                        receiptBatch = listReceiptBatch.get(0);

                        //回单明细全部更新待勾对，回单批次为处理中
                        //更新批次状态---处理中
                        receiptBatch.setStatus(2);
                        receiptBatch.setReceiptImportType(1);
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
                        params.put("payType", 4);
                        params.put("companyId", paymentConfig.getCompanyId());
                        params.put("merchantId", receiptBatch.getMerchantId());
                        params.put("receiptTime", receiptTime);
                        //回单明细全部更新待勾对
                        params.put("receiptChecked", "0");
                        receiptService.updateReceiptCommission(params);

                        AutoImportReceipt autoImportReceipt = new AutoImportReceipt();
                        String finalFileName = fileName;
                        providerJmsTemplate.send(autoImportReceiptDestination, session -> {
                            autoImportReceipt.setFileName(finalFileName);
                            autoImportReceipt.setBatchId(receiptBatch.getId() + "");
                            autoImportReceipt.setPathPdfDir(pathPdfDir);
                            log.info("--------------------------发送mq------------------------ :", autoImportReceipt.toString());
                            return session.createObjectMessage(autoImportReceipt);
                        });

                    }
                }
            }
            log.info("------------------------自动导入回单end------------------------");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("---------------电子回单生产任务异常结束--------------");
        } finally {
            MDC.remove(PROCESS);
        }
    }
}
