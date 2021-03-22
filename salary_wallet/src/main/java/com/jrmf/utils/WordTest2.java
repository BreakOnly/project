package com.jrmf.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

/**
 * @author: YJY
 * @date: 2021/1/4 17:36
 * @description:
 */
public class WordTest2 {

  /**
   * 替换段落里面的变量
   *
   * @param doc    要替换的文档
   * @param params 参数
   */
  public static void replaceInPara(XWPFDocument doc, Map<String, Object> params) {
    Iterator<XWPFParagraph> iterator = doc.getParagraphsIterator();
    XWPFParagraph para;
    while (iterator.hasNext()) {
      para = iterator.next();
      replaceInPara(para, params);
    }
  }

  /**
   * 替换段落里面的变量
   *
   * @param para   要替换的段落
   * @param params 参数
   */
  public static void replaceInPara(XWPFParagraph para, Map<String, Object> params) {
    List<XWPFRun> runs;
    Matcher matcher;
    if (matcher(para.getParagraphText()).find()) {
      runs = para.getRuns();

      int start = -1;
      int end = -1;
      String str = "";
      for (int i = 0; i < runs.size(); i++) {
        XWPFRun run = runs.get(i);
        String runText = run.toString();
        System.out.println("------>>>>>>>>>" + runText);
        if ('$' == runText.charAt(0)&&'{' == runText.charAt(1)) {
          start = i;
        }
        if ((start != -1)) {
          str += runText;
        }
        if ('}' == runText.charAt(runText.length() - 1)) {
          if (start != -1) {
            end = i;
            break;
          }
        }
      }
      System.out.println("start--->"+start);
      System.out.println("end--->"+end);

      System.out.println("str---->>>" + str);

      for (int i = start; i <= end; i++) {
        para.removeRun(i);
        i--;
        end--;
        System.out.println("remove i="+i);
      }

      for (String key : params.keySet()) {
        if (str.equals(key)) {
          para.createRun().setText((String) params.get(key));
          break;
        }
      }


    }
  }

  /**
   * 替换表格里面的变量
   *
   * @param doc    要替换的文档
   * @param params 参数
   */
  public static void replaceInTable(XWPFDocument doc, Map<String, Object> params) {
    Iterator<XWPFTable> iterator = doc.getTablesIterator();
    XWPFTable table;
    List<XWPFTableRow> rows;
    List<XWPFTableCell> cells;
    List<XWPFParagraph> paras;
    while (iterator.hasNext()) {
      table = iterator.next();
      rows = table.getRows();
      for (XWPFTableRow row : rows) {
        cells = row.getTableCells();
        for (XWPFTableCell cell : cells) {
          paras = cell.getParagraphs();
          for (XWPFParagraph para : paras) {
            replaceInPara(para, params);
          }
        }
      }
    }
  }

  /**
   * 正则匹配字符串
   *
   * @param str
   * @return
   */
  private static Matcher matcher(String str) {
    Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(str);
    return matcher;
  }

  /**
   * 关闭输入流
   *
   * @param is
   */
  public static void close(InputStream is) {
    if (is != null) {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 关闭输出流
   *
   * @param os
   */
  public static void close(OutputStream os) {
    if (os != null) {
      try {
        os.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


  public static void main(String[] args) {
     

    Map<String, Object> params = new HashMap<String, Object>();

    params.put("${position}", "职位");

    params.put("${name}", "测试");

    XWPFDocument doc = null;
    InputStream is = null;
    try {
      URL url = new URL("https://ms-wallet.jrmf360.com/yuncr/agreement_template.docx");
      HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
      httpConn.connect();
      is = httpConn.getInputStream();
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      doc = new XWPFDocument(is);
    } catch (IOException e) {
      e.printStackTrace();
    }

    replaceInPara(doc, params);
    //替换表格里面的变量
     replaceInTable(doc, params);

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();//二进制OutputStream
      doc.write(baos);//文档写入流
      ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());//OutputStream写入InputStream二进制流
      boolean flag = FtpTool.uploadFile("/yuncr/word","20",in);
      System.out.println(flag);
      System.out.println(flag);
      System.out.println(flag);
      System.out.println(flag);
      System.out.println(flag);
    } catch (IOException e) {
      e.printStackTrace();
    }

    close(is);
  }

}
