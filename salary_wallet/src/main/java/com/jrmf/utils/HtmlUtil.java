package com.jrmf.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.jrmf.domain.UsersAgreement;
import com.jrmf.domain.ZhipaiSignTemplate;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author 种路路
 * @create 2019-05-06 16:07
 * @desc 网页转 pdf，word  工具类
 **/
@Slf4j
public class HtmlUtil {

  public static String replace(UsersAgreement usersAgreement) throws Exception {
    //读取文件
    File file = new File(
        "/data/server/salaryboot/static/template/agreement/" + usersAgreement.getHtmlTemplate()
            + ".html");
    /*File file = new File(
        "C:\\Users\\86153\\Desktop\\协议\\" + usersAgreement.getHtmlTemplate() + ".html");*/
    BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    StringBuilder stringBuilder = new StringBuilder();
    String line;
    //以行为单位进行遍历
    while ((line = br.readLine()) != null) {
      //替换每一行中符合被替换字符条件的字符串
      line = line.replaceAll("\\{serviceTypeNames\\}", usersAgreement.getServiceTypeNames());
      line = line.replaceAll("\\{companyName\\}", usersAgreement.getCompanyName());
      line = line.replaceAll("\\{customName\\}", usersAgreement.getCustomName());
      line = line.replaceAll("\\{userName\\}", usersAgreement.getUserName());
      line = line.replaceAll("\\{certId\\}", usersAgreement.getCertId());
      line = line.replaceAll("\\{mobilePhone\\}", usersAgreement.getMobilePhone());
      line = line.replaceAll("certImg_front",
          usersAgreement.getDomainName() + usersAgreement.getImageURLA());
      line = line.replaceAll("certImg_back",
          usersAgreement.getDomainName() + usersAgreement.getImageURLB());
      line = line.replaceAll("【2000】年【98】月【79】日",
          "【" + usersAgreement.getLastUpdateTime().substring(0, 4) + "】年【" + usersAgreement
              .getLastUpdateTime().substring(5, 7) + "】月【"
              + usersAgreement.getLastUpdateTime().substring(8, 10)
              + "】日");
      stringBuilder.append(line);
      stringBuilder.append(System.getProperty("line.separator"));
    }
    //关闭输入流
    br.close();
    return stringBuilder.toString();

  }


  public static String templateFill(String templateFile, ZhipaiSignTemplate zhipaiSignTemplate)
      throws Exception {
    log.info("开始替换合同字段");
    File file = new File(templateFile);
    BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    StringBuilder stringBuilder = new StringBuilder();
    String line;
    log.info("以行为单位进行遍历");
    //以行为单位进行遍历
    while ((line = br.readLine()) != null) {
      //替换每一行中符合被替换字符条件的字符串
      line = line.replaceAll("\\{userName\\}", zhipaiSignTemplate.getUserName());
      line = line.replaceAll("\\{taxpayerId\\}", zhipaiSignTemplate.getTaxpayerId());
      line = line.replaceAll("\\{contacts\\}", zhipaiSignTemplate.getContacts());
      line = line.replaceAll("\\{contactInfo\\}", zhipaiSignTemplate.getContactInfo());
      line = line.replaceAll("\\{address\\}", zhipaiSignTemplate.getAddress());
      line = line.replaceAll("\\{date\\}", zhipaiSignTemplate.getDate());
      line = line.replaceAll("\\{services\\}", zhipaiSignTemplate.getTaskName());
      line = line.replaceAll("\\{taskName\\}", zhipaiSignTemplate.getTaskName());
      line = line.replaceAll("\\{taskDesc\\}", zhipaiSignTemplate.getTaskDesc());
      line = line.replaceAll("\\{invoiceMoney\\}", zhipaiSignTemplate.getInvoiceMoney());
      stringBuilder.append(line);
      stringBuilder.append(System.getProperty("line.separator"));
    }
    br.close();
    log.info("替换成功");
    return stringBuilder.toString();
  }


  public static void parseHTML2PDFFile(String pdfFile, String html)
      throws IOException, DocumentException {
    Document document = new Document();
    PdfWriter writer = PdfWriter.getInstance(document,
        new FileOutputStream(pdfFile));
    document.open();
    XMLWorkerHelper.getInstance().parseXHtml(writer, document,
        new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8,
        new FontProviderUtil());
    document.close();
  }

  public static void parseHTML2WordFile(String wordFile, String html) throws IOException {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
        html.getBytes(StandardCharsets.UTF_8));
    POIFSFileSystem poi = new POIFSFileSystem();
    DirectoryEntry directory = poi.getRoot();
    directory.createDocument("WordDocument", byteArrayInputStream);
    FileOutputStream outputStream = new FileOutputStream(wordFile);
    poi.writeFilesystem(outputStream);
    byteArrayInputStream.close();
    outputStream.close();
  }

  public static void main(String[] args) throws IOException, DocumentException {

    ZhipaiSignTemplate zhipaiSignTemplate = new ZhipaiSignTemplate();
    zhipaiSignTemplate.setAddress("中国北京");
    zhipaiSignTemplate.setContactInfo("15313321336");
    zhipaiSignTemplate.setContacts("吴福进");
    zhipaiSignTemplate.setUserName("测试");
    zhipaiSignTemplate.setTaxpayerId("123");
    zhipaiSignTemplate.setDate("2021年1月7日");
    zhipaiSignTemplate.setTaskName("123");
    zhipaiSignTemplate.setTaskDesc("哈哈");
    zhipaiSignTemplate.setInvoiceMoney("12");

    try {
      String htmlLocalSavePath ="C:\\Users\\86153\\Desktop\\zhipai-123.html";
      String pdfLocalSavePath ="C:\\Users\\86153\\Desktop\\zhipai-123.pdf";

      String content = templateFill("C:\\Users\\86153\\Desktop\\index.html", zhipaiSignTemplate);
      OutputStream fos = new FileOutputStream(htmlLocalSavePath);
      fos.write(content.getBytes("UTF-8"));
      fos.flush();
      fos.close();

      PdUtil.html2pdf(htmlLocalSavePath, pdfLocalSavePath, "C:/Windows/Fonts/simsun.ttc");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
