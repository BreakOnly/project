package com.jrmf.service.mq;

import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.ReceiptStatus;
import com.jrmf.domain.AutoImportReceipt;
import com.jrmf.domain.ReceiptBatch;
import com.jrmf.service.ReceiptService;
import com.jrmf.splitorder.util.FileUtil;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.ZipUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.springframework.beans.factory.annotation.Value;

/**
 * @description: <br/>
 * @author: <br/>
 * @create：2020年05⽉21⽇<br/>
 */
@Slf4j
public class AutoImportReceiptListener implements MessageListener {

  public static final String PROCESS = "process";

  @Autowired
  private ReceiptService receiptService;

  @Autowired
  private BestSignConfig bestSignConfig;

  @Value("${receipt.tmp.path}")
  private String receiptTmpPath;

  @Override
  public void onMessage(Message message) {
    String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    MDC.put(PROCESS, processId);
    log.info("------------------------自动导入回单mq开始工作------------------------");
    AutoImportReceipt receipt;

    try {
      receipt = (AutoImportReceipt) ((ObjectMessage) message).getObject();
      log.info("--------------------自动导入回单mq的信息为{}----------------{}", receipt.toString());

      // download回单
      log.info("FTPURL：{}", bestSignConfig.getFtpURL());

      byte[] fileData;
      if (receipt.getBestSignConfig() == null) {
        fileData = FtpTool
            .downloadFtpFile(bestSignConfig.getFtpURL(), bestSignConfig.getUsername(),
                bestSignConfig.getPassword(), 21, "/pingan/kf", receipt.getFileName());
      } else {
        BestSignConfig ftpConfig = receipt.getBestSignConfig();
        fileData = FtpTool
            .downloadFtpsFile(ftpConfig.getFtpURL(), ftpConfig.getUsername(),
                ftpConfig.getPassword(), Integer.parseInt(ftpConfig.getFtpPort()),
                ftpConfig.getFtpPath(), receipt.getFileName());
      }


      if (fileData == null || fileData.length == 0) {
        ReceiptBatch receiptBatch = receiptService.getReceiptBatchById(
            Integer.valueOf(receipt.getBatchId()));//非处理状态
        receiptBatch.setStatus(ReceiptStatus.ALLFAIL.getCode());
        receiptService.updateReceiptBatch(receiptBatch);
        log.error("mq自动导入回单结束,未找到指定文件");
        return;
      }

      if (receipt.getFileName().contains(".zip")) {

        FileUtil.mkdirs(receiptTmpPath);
        String fileUrl = receiptTmpPath + File.separator + receipt.getFileName();
        //保存ftp上的文件压缩包至本地解压
        Files.write(Paths.get(fileUrl), fileData);

        //解压目标文件夹
        String tempPath =
            receiptTmpPath + File.separator + System.currentTimeMillis() + File.separator;

        ZipUtils.unzip(new File(fileUrl), tempPath);

        Path root = Paths.get(tempPath);
        //页数技术器
        AtomicInteger atomicInteger = new AtomicInteger();
        //条数计数器
        AtomicInteger count = new AtomicInteger();

        ReceiptBatch receiptBatch = receiptService.getReceiptBatchById(Integer.valueOf(receipt.getBatchId()));

        int pageSize = (int) Files.list(root).count();
        Files.list(root).forEach(path -> {
          try {
//            Files.readAllBytes(path);
            receiptService
                .pdfSplit(Files.readAllBytes(path), atomicInteger, count, pageSize,
                    receipt.getBatchId(), receipt.getPathPdfDir(), path.getFileName().toString(),receiptBatch.getReceiptImportType());
          } catch (Exception e) {
            log.error("mq自动导入回单异常", e);
            atomicInteger.incrementAndGet();
          }
        });

      } else {

//          测试坏境FTP
//          byte[] fileData = FtpTool.downloadFtpFile("192.168.1.15", "test", "test2020", 21,"/pingan", receipt.getFileName());
        // 服务上传并解析分片pdf文件
        receiptService.partitionPdfFile(fileData, receipt.getBatchId(), receipt.getPathPdfDir());
      }

    } catch (Exception e) {
      log.error("mq自动导入回单异常", e);
    } finally {
      MDC.remove(PROCESS);
    }

  }
}
