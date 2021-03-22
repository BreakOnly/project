package com.jrmf.utils;

import com.deepoove.poi.XWPFTemplate;
import com.jrmf.domain.ZhipaiSignTemplate;
import java.io.FileOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WordUtil {


  public static boolean wordTemplateFill(String src, String dest, Object content) {
    boolean rs = false;
    XWPFTemplate template = XWPFTemplate.compile(src).render(content);
    try {
      FileOutputStream out = new FileOutputStream(dest);
      template.write(out);
      out.flush();
      out.close();
      template.close();
      rs = true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return rs;
  }

  public static void main(String[] args) {

    ZhipaiSignTemplate zhipaiSignTemplate =  new ZhipaiSignTemplate();
    zhipaiSignTemplate.setAddress("中国北京123");
    zhipaiSignTemplate.setContactInfo("15313321336");
    zhipaiSignTemplate.setContacts("哈哈哈");
    zhipaiSignTemplate.setUserName("测试哈哈");
    zhipaiSignTemplate.setTaxpayerId("123");
    zhipaiSignTemplate.setDate("2021年1月7日");

    boolean rs =wordTemplateFill("C:\\Users\\86153\\Desktop\\江西智派C端协议模板.docx","C:\\Users\\86153\\Desktop\\江西智派C端协议模板out.docx",zhipaiSignTemplate);
  }

}
