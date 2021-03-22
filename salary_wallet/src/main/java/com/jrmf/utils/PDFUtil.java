package com.jrmf.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 种路路
 * @create 2019-01-09 10:04
 * @desc pdf工具类
 **/
public class PDFUtil {
    private static final Logger logger = LoggerFactory.getLogger(PDFUtil.class);

    public static List<String> getContent(PDDocument pdfDocument) throws Exception {
        StringWriter writer = new StringWriter();
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.writeText(pdfDocument, writer);
        String contents = writer.getBuffer().toString();
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        String line;
        List<String> stringArrayList = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            if (!"".equals(line.trim())) {
                stringArrayList.add(line.trim());
            }
        }
        List<String> contentList = new ArrayList<>();
        int singlePDFLength = 24;
        for (int i = 0; i < stringArrayList.size() / singlePDFLength; i++) {
            String time = stringArrayList.get(10 + singlePDFLength * i);
            String content = stringArrayList.get(16 + singlePDFLength * i);
            contentList.add((time.length() > 10 ? time.substring(0, 10) : time) + "_" + (content.length() < 6 ? content : content.substring(0, 6)));
        }
        stringArrayList.clear();

        return contentList;
    }

    /**
     * pdf按页分割
     *
     * @param fileBytes pdf 原始文件
     * @throws Exception 文件解析异常
     */
    public static List<byte[]> partitionPdfFile(byte[] fileBytes) throws Exception {

        PdfReader reader = new PdfReader(fileBytes);

        int size = reader.getNumberOfPages();
        List<byte[]> arrayList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Document document = new Document(reader.getPageSize(i + 1));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfCopy copy = new PdfCopy(document, out);
            document.open();
            document.newPage();
            PdfImportedPage page = copy.getImportedPage(reader, i + 1);
            copy.addPage(page);
            document.close();

            byte[] bytes = out.toByteArray();
            arrayList.add(bytes);
        }

        return arrayList;

    }

    public static void combinePdf(byte[] abytes, byte[] bbytes, String targetFilename) throws Exception {
        Document doc = new Document();
        PdfCopy pdfCopy = new PdfCopy(doc, new FileOutputStream(targetFilename));
        doc.open();
        PdfReader reader = new PdfReader(abytes);
        int pageCount = reader.getNumberOfPages();
        for (int j = 1; j <= pageCount; ++j) {
            pdfCopy.addPage(pdfCopy.getImportedPage(reader, j));
        }
        reader = new PdfReader(bbytes);
        pageCount = reader.getNumberOfPages();
        for (int j = 1; j <= pageCount; ++j) {
            pdfCopy.addPage(pdfCopy.getImportedPage(reader, j));
        }
        doc.close();
    }

    public static File createFile(String path) {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            boolean mkdirs = file.getParentFile().mkdirs();
            logger.debug("创建文件夹：" + mkdirs);
        }
        try {
            boolean createFile = file.createNewFile();
            logger.debug("创建文件夹：" + createFile);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        return file;
    }

    public static String getPingAnBankOneContent(PDDocument pdfDocument) throws Exception {
        StringWriter writer = new StringWriter();
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.writeText(pdfDocument, writer);
        String contents = writer.getBuffer().toString();
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        String line;
        List<String> stringArrayList = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            if (!"".equals(line.trim())) {
                stringArrayList.add(line.trim());
            }
        }

        String pdfName = null;
        for (int i = 0; i < stringArrayList.size(); i++) {
            String item = stringArrayList.get(i);
            if ("收付款业务回单 回单凭证".equals(item)){
                String time = stringArrayList.get(i + 1);
                time = time.substring(time.indexOf("记账日期") + 6, time.indexOf("回单号")).trim();
                String content = stringArrayList.get(i + 6);

                String receiptNo = (content.length() < 6 ? content : content.substring(0, 6));

                if (content.length() > 6) {
                    if (content.startsWith("备注") && content.contains("(")) {
                        receiptNo = content.substring(content.indexOf("备注") + 3, content.indexOf("备注") + 10).trim();
                    }
                }
                pdfName = (time.length() > 10 ? time.substring(0, 10) : time) + "_" + receiptNo;

                break;
            } else if ("跨行快付批量业务回单".equals(item)) {

                String time = stringArrayList.get(7);
                String content = stringArrayList.get(13);

                String receiptNo = (content.length() < 6 ? content : content.substring(0, 6));
                //兼容子账号下发回单
                if (content.length() > 6) {
                    if (content.startsWith("代") && content.contains("(")) {
                        receiptNo = content.substring(content.lastIndexOf("(") + 1, content.lastIndexOf("(") + 7);
                    } else if (content.startsWith("代") && stringArrayList.get(14).contains("(") && stringArrayList.get(14).length() > 6) {
                        content = stringArrayList.get(14);
                        receiptNo = content.substring(content.lastIndexOf("(") + 1, content.lastIndexOf("(") + 7);
                    }
                }
                pdfName = (time.length() > 10 ? time.substring(0, 10) : time) + "_" + receiptNo;

                break;
            } else if ("跨行快付批量业务回单 回单凭证".equals(item)) {
                String time = stringArrayList.get(1);
                time = time.substring(time.indexOf("记账日期: ") + 6, time.indexOf("回单号:")).trim();
                String content = stringArrayList.get(6);

                String receiptNo = (content.length() < 6 ? content : content.substring(0, 6));
                //兼容子账号下发回单
                if (content.length() > 6) {
                    if (content.startsWith("费项名称: 代") && content.contains("(")) {
                        receiptNo = content.substring(content.lastIndexOf("(") + 1, content.lastIndexOf("(") + 7);
                    } else if (content.startsWith("费项名称: 代")) {
                        content = stringArrayList.get(7);
                        receiptNo = content.substring(content.lastIndexOf("(") + 1, content.lastIndexOf("(") + 7);
                    } else if (content.contains("(委托单位名称):")) {
                        content = stringArrayList.get(6);
                        receiptNo = content.substring(content.lastIndexOf("费项名称: ") + 6, content.lastIndexOf("费项名称: ") + 12);
                    } else if (stringArrayList.get(7).contains("(委托单位名称):")) {
                        content = stringArrayList.get(7);
                        receiptNo = content.substring(content.lastIndexOf("费项名称: ") + 6, content.lastIndexOf("费项名称: ") + 12);
                    } else if (stringArrayList.get(7).contains("费项名称: 代")) {
                        content = stringArrayList.get(8);
                        receiptNo = content.substring(content.lastIndexOf("(") + 1, content.lastIndexOf("(") + 7);
                    }
                }
                pdfName = (time.length() > 10 ? time.substring(0, 10) : time) + "_" + receiptNo;

                break;

            }
        }
        stringArrayList.clear();

        return pdfName;
    }

}
