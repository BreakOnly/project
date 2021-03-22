package com.jrmf.utils;


import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.jrmf.utils.threadpool.ThreadUtil;
import com.lowagie.text.pdf.BaseFont;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.net.MalformedURLException;

/**
 * Pdf处理工具类
 *
 * @author
 * @create 2017-12-18 21:25
 **/
@Slf4j
public class PdUtil {

  protected static Logger logger = LoggerFactory.getLogger(PdUtil.class);

  /**
   * @param htmlFile html文件存储路径
   * @param pdfFile 生成的pdf文件存储路径
   * @param chineseFontPath 中文字体存储路径
   */
  public static void html2pdf(String htmlFile, String pdfFile, String chineseFontPath) {
    String url;
    OutputStream os = null;
    try {
      url = new File(htmlFile).toURI().toURL().toString();
      os = new FileOutputStream(pdfFile);
      ITextRenderer renderer = new ITextRenderer();
      renderer.setDocument(url);
      // 解决中文不显示问题
      ITextFontResolver fontResolver = renderer.getFontResolver();
      fontResolver.addFont(chineseFontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

      renderer.layout();
      renderer.createPDF(os);
    } catch (MalformedURLException e) {
      logger.warn(e.toString(), e);
    } catch (FileNotFoundException e) {
      logger.warn(e.toString(), e);
    } catch (com.lowagie.text.DocumentException e) {
      logger.warn(e.toString(), e);
    } catch (IOException e) {
      logger.warn(e.toString(), e);
    } finally {
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          logger.warn(e.toString(), e);
        }
      }
    }
  }

  public static void htmlTopdf(String htmlStr, String pdfFile, String chineseFontPath) {
    OutputStream os = null;
    try {
      os = new FileOutputStream(pdfFile);
      ITextRenderer renderer = new ITextRenderer();
      renderer.setDocumentFromString(htmlStr);
      // 解决中文不显示问题
      ITextFontResolver fontResolver = renderer.getFontResolver();
      fontResolver.addFont(chineseFontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

      renderer.layout();
      renderer.createPDF(os);
      renderer.finishPDF();
    } catch (MalformedURLException e) {
      logger.warn(e.toString(), e);
    } catch (FileNotFoundException e) {
      logger.warn(e.toString(), e);
    } catch (com.lowagie.text.DocumentException e) {
      logger.warn(e.toString(), e);
    } catch (IOException e) {
      logger.warn(e.toString(), e);
    } finally {
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          logger.warn(e.toString(), e);
        }
      }
    }
  }

  public static boolean mergeManyPdfFiles(String[] files, String newFile) {
    long s = System.currentTimeMillis();
    boolean rs;
    int total = files.length;
    if (total > 500) {
      //分组merge，这里需要平衡资源 和效率，files不能无限大, 用总数/线程数  得到每组合并的个数

      int pages = Runtime.getRuntime().availableProcessors() * 2;
      int numPerPage = 0;
      int numLastPage = 0;

      if (total % pages == 0) {
        numPerPage = total / pages;
      } else if (total % (pages - 1) == 0) {
        numPerPage = total / (pages - 1);
        pages = pages - 1;
      } else {
        numPerPage = total / (pages - 1);
        numLastPage = total % (pages - 1);
      }
      CountDownLatch countDownLatch = new CountDownLatch(pages);
      String[] rsFiles = new String[pages];

      for (int i = 1; i <= pages; i++) {
        String tmpFile =
            newFile.substring(0, newFile.lastIndexOf("/") + 1) + UUID.randomUUID().toString()
                .replace("-", "") + ".pdf";
        rsFiles[i] = tmpFile;

        if (numLastPage > 0 && i == pages) {
          int start = (i - 1) * numPerPage;
          int end = start + numLastPage;
          ThreadUtil.pdfThreadPool.execute(() -> {
            PdUtil.mergePdfFiles(Arrays.copyOfRange(files, start, end), tmpFile);
            countDownLatch.countDown();
          });
        } else {
          int start = (i - 1) * numPerPage;
          int end = start + numPerPage;
          ThreadUtil.pdfThreadPool.execute(() -> {
            PdUtil.mergePdfFiles(Arrays.copyOfRange(files, start, end), tmpFile);
            countDownLatch.countDown();
          });
        }
      }

      try {
        countDownLatch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      rs = mergePdfFiles(rsFiles, newFile);
      long e = System.currentTimeMillis();
      System.out.println("mergeManyPdfFiles耗时：" + (e - s) + "ms");
      return rs;
    }
    return mergePdfFiles(files, newFile);
  }


  public static boolean mergePdfFiles(String[] files, String newFile) {
    long s = System.currentTimeMillis();
    boolean retValue = false;
    Document document = null;
    try {
      document = new Document(new PdfReader(files[0]).getPageSize(1));
      PdfCopy copy = new PdfCopy(document, new FileOutputStream(newFile));
      document.open();
      for (int i = 0; i < files.length; i++) {
        PdfReader reader = new PdfReader(files[i]);
        int n = reader.getNumberOfPages();
        for (int j = 1; j <= n; j++) {
          document.newPage();
          PdfImportedPage page = copy.getImportedPage(reader, j);
          copy.addPage(page);
        }
      }
      retValue = true;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      document.close();
    }
    long e = System.currentTimeMillis();
    System.out.println("mergePdfFiles耗时：" + (e - s) + "ms");
    return retValue;
  }


  public static void main(String[] args) {
  }
}
