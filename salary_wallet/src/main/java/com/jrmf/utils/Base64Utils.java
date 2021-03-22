package com.jrmf.utils;

import static com.jrmf.controller.scanlogin.PaymentHelperCommon.readInputStream;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Base64Utils {

  public static void main(String[] args) {
    //String result = localFileToBase64("C:\\googleload\\1-1红包显示领取.xlsx");
    //System.out.println("----------------> :"+result);
    //System.out.println("================》:"+remoteFileToBase64("https://bx-static.jrmf360.com/insurance_static/img/1-1红包显示领取.xlsx"));

    Byte b =new Byte("2");
    System.out.println(b.byteValue()==2);
  }

  public static byte[] getDataFromNetByUrl(String strUrl) {
    try {
      URL url = new URL(strUrl);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(5 * 1000);
      InputStream inStream = conn.getInputStream();//通过输入流获取图片数据
      byte[] btImg = readInputStream(inStream);//得到图片的二进制数据
      return btImg;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static byte[] readInputStream(InputStream inStream) throws Exception {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len = 0;
    while ((len = inStream.read(buffer)) != -1) {
      outStream.write(buffer, 0, len);
    }
    byte[] data = outStream.toByteArray();//网页的二进制数据
    outStream.close();
    inStream.close();
    return data;
  }

  public static String remoteFileToBase64(String url) {
    byte[] data = null;
    try {
      data = getDataFromNetByUrl(url);
    } catch (Exception e) {
      e.printStackTrace();
    }
    BASE64Encoder encoder = new BASE64Encoder();
    return encoder.encode(data);
  }


  public static String localFileToBase64(String filePath) {
    if (filePath == null) {
      return null;
    }
    try {
      byte[] b = Files.readAllBytes(Paths.get(filePath));
      return java.util.Base64.getEncoder().encodeToString(b);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 对字节数组字符串进行Base64解码并生成图片
   */
  public static boolean generateImage(String imgStr, String desFilePath) {
    if (StringUtil.isEmpty(imgStr)) {//图像数据为空
      return false;
    }
    BASE64Decoder decoder = new BASE64Decoder();
    try {
      // Base64解码
      byte[] bytes = decoder.decodeBuffer(imgStr);
      for (int i = 0; i < bytes.length; ++i) {
        if (bytes[i] < 0) {// 调整异常数据
          bytes[i] += 256;
        }
      }
      // 生成图片
      OutputStream out = new FileOutputStream(desFilePath);
      out.write(bytes);
      out.flush();
      out.close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
