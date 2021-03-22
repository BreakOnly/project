package com.jrmf.service;

import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.EmailSendStatus;
import com.jrmf.controller.constant.PayType;
import com.jrmf.controller.constant.ReceiptImportType;
import com.jrmf.controller.constant.ReceiptStatus;
import com.jrmf.controller.constant.ReceiptType;
import com.jrmf.domain.AutoImportReceipt;
import com.jrmf.domain.Company;
import com.jrmf.domain.ReceiptBatch;
import com.jrmf.domain.ReceiptCommission;
import com.jrmf.domain.ReceiptDownLoad;
import com.jrmf.persistence.ReceiptDao;
import com.jrmf.utils.*;
import com.jrmf.utils.ftp.FTPClientUtil;
import com.jrmf.utils.pdf.replace.PdfReplacer;
import com.jrmf.utils.threadpool.ThreadUtil;
import javax.jms.Destination;
import javax.jms.ObjectMessage;
import org.apache.activemq.ScheduledMessage;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2019/1/22 15:51
 * Version:1.0
 */
@Service("receiptService")
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptDao receiptDao;
    private final BestSignConfig bestSignConfig;
    private final FTPClientUtil ftpClientUtil;
    private final CompanyService companyService;
    private static final Logger logger = LoggerFactory.getLogger(ReceiptServiceImpl.class);
    private JmsTemplate providerJmsTemplate;
    private Destination autoImportReceiptDestination;


    @Value("${receipt.wait.time}")
    private String receiptWaitTime;

    @Autowired
    public ReceiptServiceImpl(ReceiptDao receiptDao, BestSignConfig bestSignConfig, FTPClientUtil ftpClientUtil, CompanyService companyService,JmsTemplate providerJmsTemplate,Destination autoImportReceiptDestination) {
        this.receiptDao = receiptDao;
        this.bestSignConfig = bestSignConfig;
        this.ftpClientUtil = ftpClientUtil;
        this.companyService = companyService;
        this.providerJmsTemplate = providerJmsTemplate;
        this.autoImportReceiptDestination = autoImportReceiptDestination;
    }


    @Override
    public List<ReceiptCommission> listReceiptCommission(Map<String, Object> params) {
        return receiptDao.listReceiptCommission(params);
    }

    @Override
    public List<ReceiptBatch> listReceiptBatch(Map<String, Object> params) {
        return receiptDao.listReceiptBatch(params);
    }

    @Override
    public void saveReceiptBatch(ReceiptBatch batch) {
        try {
            receiptDao.saveReceiptBatch(batch);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    @Override
    public void updateReceiptBatch(ReceiptBatch receiptBatch) {
        receiptDao.updateReceiptBatch(receiptBatch);
    }

    @Override
    public List<ReceiptBatch> listReceiptBatchGroup(Map<String, Object> params) {
        return receiptDao.listReceiptBatchGroup(params);
    }

    @Override
    public List<ReceiptDownLoad> listDownloadHistory(Map<String, Object> params) {
        return receiptDao.listDownloadHistory(params);
    }

    @Override
    public int addReceipt(Map<String, Object> params) {
        try {
            return receiptDao.addReceipt(params);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return 0;
    }

    @Override
    public int addReceiptDownload(ReceiptDownLoad receiptDownLoad) {
        return receiptDao.addReceiptDownload(receiptDownLoad);
    }

    /**
     * 触发jobservice  aygRecepitBatchJob  回单处理--爱员工批次
     */
    @Override
    public void initAygRecepitBatchJob() {
        String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        MDC.put("process", processId);
        logger.info("回单处理--爱员工批次---任务开始---");
        Map<String, Object> params = new HashMap<>(20);
        params.put("merchantId", "aiyuangong");
        params.put("payType", "4");
        params.put("receiptTime", DateUtils.getBeforeDayString(2));

        List<ReceiptBatch> receiptBatchList = listReceiptBatch(params);
        //根据支付通道、服务公司分组插入
        for (ReceiptBatch batch : receiptBatchList) {

            params.clear();
            params.put("companyId", batch.getCompanyId());
            params.put("receiptTime", batch.getReceiptTime());
            params.put("payType", batch.getPayType());
            params.put("merchantId", batch.getMerchantId());
            List<ReceiptCommission> listReceipt = listReceiptCommission(params);

            int receiptMatchNum = 0;
            Map<String, Object> receiptCommissions = new HashMap<>();
            for (ReceiptCommission commission : listReceipt) {

                String yearMon = commission.getAccountDate().substring(0, 7);
                String relativePath = "/" + commission.getCompanyId() + "/" + commission.getPayType() + "_" + batch.getReceiptOrgName() + "/" + yearMon + "/" + commission.getAccountDate() + "_" + commission.getReceiptNo() + ".pdf";
                String receiptFile = "/receipt" + relativePath;
                logger.info("----回单文件路径：" + receiptFile);

                receiptCommissions.put("id", commission.getId());
                boolean isFileExists = ftpClientUtil.isExists(receiptFile);
                logger.info("----回单文件路径--存在判断isFileExists：" + isFileExists);
                if (isFileExists) {
                    receiptMatchNum++;
                    receiptCommissions.put("receiptUrl", relativePath);
                    receiptCommissions.put("receiptChecked", "1");
                } else {
                    receiptCommissions.put("receiptChecked", "2");
                    logger.info("----回单文件不存在：" + receiptFile + "----勾兑失败----");
                }

                updateReceiptCommissionById(receiptCommissions);
                receiptCommissions.clear();
            }

            batch.setReceiptMatchNum(receiptMatchNum + "");
            if (listReceipt.size() == receiptMatchNum && receiptMatchNum != 0) {
                batch.setStatus(1);//全部成功
            } else if (receiptMatchNum > 0) {
                batch.setStatus(3);//部分成功
            } else if (receiptMatchNum == 0) {
                batch.setStatus(4);//全部失败
            }

            updateReceiptBatch(batch);

        }
        logger.info("回单处理--批次---爱员工批次---任务结束---");
        MDC.remove("process");
    }

    @Override
    public int listReceiptCommissionCount(Map<String, Object> params) {
        return receiptDao.listReceiptCommissionCount(params);
    }

    @Override
    public int updateStatusReceiptDownloadById(Integer status, String statusDesc, Integer id) {
        return receiptDao.updateStatusReceiptDownloadById(status, statusDesc, id);
    }

    @Override
    public ReceiptBatch getReceiptBatchById(Integer id) {
        return receiptDao.getReceiptBatchById(id);
    }

    /**
     * 根据条件查询下发记录对应的银行流水pdf路径
     *
     * @param param 参数集合
     * @return zip包本地路径  可能为空
     */
    @Override
    public String listPdfPathByParam(Map<String, Object> param, String fileName, Integer id) {
        receiptDao.updateStatusReceiptDownloadById(EmailSendStatus.RUNNING.getCode(), EmailSendStatus.codeOf(EmailSendStatus.RUNNING.getCode()).getDesc(), id);
        String zipFilePath = "/data/server/salaryboot/temp/pdf/" + fileName + ".pdf";
        String zipFile = "/data/server/salaryboot/temp/pdf/" + fileName + ".zip";
        PDFUtil.createFile(zipFile);
        if (PayType.PINGAN_BANK.getCode() == (Integer) param.get("payType")) {
            //查询服务公司列表
            try {
                pdfPathByCompany(param, zipFilePath);
            } catch (Exception e) {
                logger.error("pdf生成异常{}",e.getMessage());
            }
        }

        try {
            ZipUtils.doCompress(zipFilePath, zipFile);
            receiptDao.updateStatusReceiptDownloadById(EmailSendStatus.SUCCESS.getCode(), EmailSendStatus.codeOf(EmailSendStatus.SUCCESS.getCode()).getDesc(), id);
            return zipFile;
        } catch (Exception e) {
            logger.error("pdf压缩异常{}",e.getMessage());
            receiptDao.updateStatusReceiptDownloadById(EmailSendStatus.FAILURE.getCode(), RespCode.CONNECTION_ERROR, id);
            return null;
        }
    }

    private void pdfPathByCompany(Map<String, Object> param, String zipFilePath) throws Exception {
        File file = new File(zipFilePath);
        //获取所有的 数据库  pdf 路径
        List<String> pdfPathList = receiptDao.listReceiptCommissionPath(param);
        for (String str : pdfPathList) {
            String path = "/receipt" + str;
            int i = path.lastIndexOf("/");

            byte[] bytes = FtpTool.downloadFtpFile(bestSignConfig.getFtpURL(), bestSignConfig.getUsername(), bestSignConfig.getPassword(), 21, path.substring(0, i), path.substring(i + 1));
            if (bytes == null) {
                continue;
            }
            //合并pdf
            if (!file.exists()) {
                file = PDFUtil.createFile(zipFilePath);
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(bytes);
                outputStream.close();
            } else {
                FileInputStream inputStream = new FileInputStream(file);
                byte[] tempByte = new byte[1024];
                int n;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                while ((n = inputStream.read(tempByte)) != -1) {
                    outputStream.write(tempByte, 0, n);
                }
                byte[] oldByte = outputStream.toByteArray();
                inputStream.close();
                outputStream.close();

                PDFUtil.combinePdf(oldByte, bytes, zipFilePath);
            }

        }
    }

    /**
     * pdf切割
     *
     * @param integer       条数计数器
     * @param atomicInteger 页数计数器
     * @param pageSize      页码
     * @param id            总数
     * @param path          @throws Exception 文件处理异常
     */
    public void pdfSplit(byte[] bytes, AtomicInteger atomicInteger, AtomicInteger integer,
        int pageSize, String id, String path, String fileName,Integer receiptImportType) throws Exception {
        PDDocument pdDocument = PDDocument.load(new ByteArrayInputStream(bytes));

//        List<String> contentList = PDFUtil.getPingAnBankOneContent(pdDocument);

//        if (ReceiptImportType.PINGANBANKTHREE.getCode() == receiptBatch.getReceiptImportType()) {
//            contentList = PDFUtil.getContent(pdDocument);
//        } else
//
        path = "/receipt/" + path + "/";

        if (ReceiptImportType.PINGANBANKONE.getCode() == receiptImportType) {

            String pdfName = PDFUtil.getPingAnBankOneContent(pdDocument);
            if (!StringUtil.isEmpty(pdfName)) {
                integer.incrementAndGet();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                uploadPDF(path, pdfName, inputStream);
            }
        }else if (ReceiptImportType.MYBANKONE.getCode() == receiptImportType) {

            if (!StringUtil.isEmpty(fileName) && fileName.contains("+")) {
                integer.incrementAndGet();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                //网商银行的文件名带有订单号，不需要根据内容解析备注
                fileName = "mybank" + fileName.split("\\+")[1];
                uploadPDF(path, FilenameUtils.getBaseName(fileName), inputStream);
            }
        } else if (ReceiptImportType.PINGANBANKTHREE.getCode() == receiptImportType) {

            Map<String, Object> result = getText(pdDocument);
            List<String> content = (List<String>) result.get("content");
            Integer templateId = (Integer) result.get("templateId");

            int contentLength = 13;
            if (templateId == 25) {
                contentLength = 14;
            }
            for (int i = 0; i < content.size() / contentLength; i++) {
                integer.incrementAndGet();
                replace(path, content, i, templateId, contentLength);
            }
        }


//        contentList.clear();
        pdDocument.close();
        atomicInteger.incrementAndGet();
        if (pageSize == atomicInteger.intValue()) {
            int receiptNum = integer.intValue();
            logger.error("回单上传完成，共处理" + receiptNum + "条，进行更新操作");
            updateReceiptStatusAndNum(id, receiptNum);
            logger.error("处理完成");
        }
    }

    @Override
    public void autoImportReceipt(ReceiptBatch receiptBatch, String fileName,BestSignConfig bestSignConfig) {
        //回单明细全部更新待勾对，回单批次为处理中
        receiptBatch.setStatus(ReceiptStatus.INHAND.getCode());//更新批次状态---处理中
        receiptBatch.setReceiptType(String.valueOf(ReceiptType.PARTFAIL.getCode()));
        updateReceiptBatch(receiptBatch);

        //更新明细状态---待勾对
        String yearMon = receiptBatch.getReceiptTime().substring(0, 7);

        Company company = companyService
            .getCompanyByUserId(receiptBatch.getCompanyId());
        //真实下发公司id
        String realCompanyId = company.getRealCompanyId();
        if (StringUtil.isEmpty(realCompanyId)) {
            realCompanyId = String.valueOf(company.getUserId());
        }

        String pathPdfDir =
            "/" + realCompanyId + "/" + receiptBatch.getPayType() + "_"
                + receiptBatch
                .getReceiptOrgName() + "/" + yearMon;

        Map<String, Object> params = new HashMap<>(20);

        params.put("payType", 4);
        params.put("companyId", receiptBatch.getCompanyId());
        params.put("merchantId", receiptBatch.getMerchantId());
        params.put("receiptTime", receiptBatch.getReceiptTime());
        params.put("receiptChecked", "0");//回单明细全部更新待勾对
        updateReceiptCommission(params);

        AutoImportReceipt autoImportReceipt = new AutoImportReceipt();
        providerJmsTemplate.send(autoImportReceiptDestination, session -> {
            autoImportReceipt.setFileName(fileName);
            autoImportReceipt.setBatchId(receiptBatch.getId() + "");
            autoImportReceipt.setPathPdfDir(pathPdfDir);
            autoImportReceipt.setBestSignConfig(bestSignConfig);

            logger.info("--------------------------发送mq------------------------ :",
                autoImportReceipt.toString());
            ObjectMessage message = session.createObjectMessage(autoImportReceipt);
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,
                Long.parseLong(receiptWaitTime));
            return message;
        });
    }

    /**
     * 替换文本
     *
     * @param path    路径
     * @param content 原始文本
     * @param i       总数
     * @throws Exception ioException
     */
    private void replace(String path, List<String> content, int i, Integer templateId, int contentLength) throws Exception {
        // 模版文件放在  /data/server/salaryboot/static/4714.pdf
        PdfReplacer textReplacer = null;

        String name = null;
        String date = null;

        if (templateId == 24) {
            textReplacer = new PdfReplacer("/data/server/salaryboot/static/4714.pdf");
            date = content.get(contentLength * i);
            textReplacer.replaceText("2018-12-09", date);
            textReplacer.replaceText("18120903030010014150", content.get(contentLength * i + 1));
            textReplacer.replaceText("056650", content.get(contentLength * i + 2));
            textReplacer.replaceText("天津科企金云科技有限公司", content.get(contentLength * i + 3));
            textReplacer.replaceText("15000094550572", content.get(contentLength * i + 4));
            textReplacer.replaceText("王龙静", content.get(contentLength * i + 5));
            textReplacer.replaceText("6230522460004831378", content.get(contentLength * i + 6));
            textReplacer.replaceText("13.87", content.get(contentLength * i + 7));
            name = content.get(contentLength * i + 8);
            textReplacer.replaceText("银企直联付款(实时转账)", name);
            textReplacer.replaceText("2018-12-10 15:21:06", content.get(contentLength * i + 9));
            textReplacer.replaceText("壹拾叁元捌角柒分", content.get(contentLength * i + 10));
            textReplacer.replaceText("平安银行北京花园路支行", content.get(contentLength * i + 11));
            textReplacer.replaceText("中国农业银行股份有限公司", content.get(contentLength * i + 12));
        } else if (templateId == 25) {
            String unitName = content.get(contentLength * i + 13);
            if (unitName.contains("海南慧用工服务有限公司")) {
                textReplacer = new PdfReplacer("/data/server/salaryboot/static/4716.pdf");
                textReplacer.replaceText("巴马小黄蜂科技服务有限责任公司", content.get(contentLength * i + 3));

            } else if (unitName.contains("开封市薪企云服人力资源服务有限公司")) {
                textReplacer = new PdfReplacer("/data/server/salaryboot/static/4717.pdf");
                textReplacer.replaceText("巴马小黄蜂科技服务有限责任公司", content.get(contentLength * i + 3));
            }  else {
                textReplacer = new PdfReplacer("/data/server/salaryboot/static/4715.pdf");
            }
            date = content.get(contentLength * i);
            textReplacer.replaceText("2019-08-08", date);
            textReplacer.replaceText("19080824010100000024", content.get(contentLength * i + 1));
            textReplacer.replaceText("647224", content.get(contentLength * i + 2));
            textReplacer.replaceText("15000099277864", content.get(contentLength * i + 4));
            textReplacer.replaceText("李丽莉", content.get(contentLength * i + 5));
            textReplacer.replaceText("6230580000168101447", content.get(contentLength * i + 6));
            textReplacer.replaceText("0.01", content.get(contentLength * i + 7));
            name = content.get(contentLength * i + 8);
            textReplacer.replaceText("000608", name);
            textReplacer.replaceText("2019-08-26 14:06:16", content.get(contentLength * i + 9));
            textReplacer.replaceText("壹分", content.get(contentLength * i + 10));
            textReplacer.replaceText("平安银行福州分行营业部", content.get(contentLength * i + 11));
            textReplacer.replaceText("平安银行", content.get(contentLength * i + 12));
        }


        String fileName = name.length() > 6 ? name.substring(0, 6) : name;
        //兼容子账号下发回单
        if (name.length() > 6 && name.startsWith("代") && name.contains("(")) {
            fileName = name.substring(name.lastIndexOf("(") + 1, name.lastIndexOf("(") + 7);
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(textReplacer.toBytes());

        uploadPDF(path, date + "_" + fileName, inputStream);
    }

    /**
     * 获取文本内容
     *
     * @param pdfDocument 原始文件
     * @return 文本数组
     * @throws Exception ioException
     */
    private Map<String, Object> getText(PDDocument pdfDocument) throws Exception {
        StringWriter writer = new StringWriter();
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.writeText(pdfDocument, writer);
        pdfDocument.close();
        String contents = writer.getBuffer().toString();
        writer.close();
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contents.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String line;
        List<String> stringArrayList = new ArrayList<>();

        Integer fieldCount = 24;

        while ((line = br.readLine()) != null) {
            if (!StringUtil.isEmpty(line.trim())) {
                if (fieldCount == 24 && line.trim().contains("代收付回单")) {
                    fieldCount = 25;
                }
                stringArrayList.add(line.trim());
            }
        }
        br.close();


        int count = stringArrayList.size() / fieldCount;
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String[] split = stringArrayList.get(fieldCount * i + 10).split(" ");
            for (String s : split) {
                if (!StringUtil.isEmpty(s)) {
                    arrayList.add(s);
                }
            }
            arrayList.add(stringArrayList.get(fieldCount * i + 11));
            arrayList.add(stringArrayList.get(fieldCount * i + 12));
            arrayList.add(stringArrayList.get(fieldCount * i + 13));
            arrayList.add(stringArrayList.get(fieldCount * i + 14));
            arrayList.add(stringArrayList.get(fieldCount * i + 15));
            arrayList.add(stringArrayList.get(fieldCount * i + 16));
            arrayList.add(stringArrayList.get(fieldCount * i + 17));
            arrayList.add(stringArrayList.get(fieldCount * i + 18));
            String[] split1 = stringArrayList.get(fieldCount * i + 19).split(" ");
            if (split1.length == 1) {
                String str = split1[0];
                split1 = new String[2];
                split1[0] = str;
                split1[1] = "";
            }
            for (String s : split1) {
                if (!StringUtil.isEmpty(s)) {
                    arrayList.add(s);
                }
            }


            arrayList.add(stringArrayList.get(fieldCount * i + 24));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("templateId", fieldCount);
        result.put("content", arrayList);

        return result;
    }

    /**
     * 上传文件
     *
     * @param path        路径
     * @param name        文件名
     * @param inputStream 上传流
     * @throws Exception
     */
    private void uploadPDF(String path, String name, InputStream inputStream) throws Exception {
        boolean storeFile = ftpClientUtil.uploadFile(path, name + ".pdf", inputStream);
        logger.error("上传{}文件服务器返回===={}", name, storeFile);
    }

    /**
     * pdf切割
     *
     * @param fileBytes pdf源文件
     * @param id        pdf源文件数据库id
     * @param path      pdf分解文件路径
     */
    @Override
    public void partitionPdfFile(byte[] fileBytes, String id, String path) {
        List<byte[]> bytes;
        try {
            bytes = PDFUtil.partitionPdfFile(fileBytes);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return;
        }
        int pageSize = bytes.size();
        //页数技术器
        AtomicInteger atomicInteger = new AtomicInteger();
        //条数计数器
        AtomicInteger count = new AtomicInteger();

        ReceiptBatch receiptBatch = getReceiptBatchById(Integer.valueOf(id));

        for (byte[] byteArray : bytes) {
            ThreadUtil.pdfThreadPool.execute(() -> {
                try {
                    pdfSplit(byteArray, atomicInteger, count, pageSize, id, path,null, receiptBatch.getReceiptImportType());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            });
        }
    }

    private void updateReceiptStatusAndNum(String id, Integer receiptNum) {
        //非处理状态
        ReceiptBatch receiptBatch = getReceiptBatchById(Integer.valueOf(id));

        if (receiptBatch == null || receiptBatch.getStatus() != 2) {
            return;
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("companyId", receiptBatch.getCompanyId());
        params.put("receiptTime", receiptBatch.getReceiptTime());
        params.put("payType", receiptBatch.getPayType());
        params.put("merchantId", receiptBatch.getMerchantId());

        String yearMon = receiptBatch.getReceiptTime().substring(0, 7);

        Company company = companyService.getCompanyByUserId(receiptBatch.getCompanyId());
        //真实下发公司id
        String realCompanyId = company.getRealCompanyId();
        if (StringUtil.isEmpty(realCompanyId)) {
            realCompanyId = String.valueOf(company.getUserId());
        }

        int receiptMatchNum = 0;
        List<ReceiptCommission> listReceipt = listReceiptCommission(params);
        Map<String, Object> receiptCommissions = new HashMap<String, Object>();
        for (ReceiptCommission commission : listReceipt) {

            String relativePath;
            String receiptFile;
            if ("mybank".equals(receiptBatch.getMerchantId())){
                relativePath = "/" + realCompanyId + "/" + commission.getPayType() + "_" + receiptBatch.getReceiptOrgName() + "/" + yearMon + "/" + "mybank" + commission.getOrderNo() + ".pdf";
                 receiptFile = "/receipt" + relativePath;
            }else {
                 relativePath = "/" + realCompanyId + "/" + commission.getPayType() + "_" + receiptBatch.getReceiptOrgName() + "/" + yearMon + "/" + receiptBatch.getReceiptTime() + "_" + commission.getReceiptNo() + ".pdf";
                 receiptFile = "/receipt" + relativePath;
            }


            receiptCommissions.put("id", commission.getId());
            logger.info("----回单文件路径：" + receiptFile);
            boolean isFileExists = ftpClientUtil.isExists(receiptFile);
            if (isFileExists) {
                receiptMatchNum++;
                receiptCommissions.put("receiptUrl", relativePath);
                receiptCommissions.put("receiptChecked", "1");
            } else {
                receiptCommissions.put("receiptChecked", "2");
                logger.info("----回单文件不存在：" + receiptFile + "----勾兑失败----");
            }
            updateReceiptCommissionById(receiptCommissions);
            receiptCommissions.clear();
        }

        // 文件切片数量
        receiptBatch.setReceiptNum(receiptNum + "");
        receiptBatch.setReceiptMatchNum(receiptMatchNum + "");
        if (listReceipt.size() == receiptMatchNum && receiptMatchNum != 0) {
            // 全部成功
            receiptBatch.setStatus(1);
        } else if (listReceipt.size() != receiptMatchNum && receiptMatchNum != 0) {
            // 部分成功
            receiptBatch.setStatus(3);
        } else if (receiptMatchNum == 0) {
            // 全部失败
            receiptBatch.setStatus(4);
        }

        updateReceiptBatch(receiptBatch);
    }


    @Override
    public void updateReceiptCommission(Map<String, Object> receiptCommission) {
        receiptDao.updateReceiptCommission(receiptCommission);
    }


    @Override
    public void updateReceiptCommissionById(Map<String, Object> receiptCommission) {
        receiptDao.updateReceiptCommissionById(receiptCommission);
    }

    @Override
    public void updateReceiptCommissionByReceiptNo(Map<String, Object> receiptCommission) {
        receiptDao.updateReceiptCommissionByReceiptNo(receiptCommission);
    }

    @Override
    public ReceiptCommission getReceiptCommissionByReceiptNo(Map<String, Object> params) {
        return receiptDao.getReceiptCommissionByReceiptNo(params);
    }

    @Override
    public String getReceiptCommissionByOrderNo(String orderNo) {
        return receiptDao.getReceiptCommissionByOrderNo(orderNo);
    }

}
