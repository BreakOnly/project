package com.jrmf.utils;

import java.net.HttpURLConnection;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;

/**
 * filename：com.jrmf.utils.PicUtils.java
 *
 * @author: 种路路
 * @time: 2018年10月8日17:40:39
 */
public class PicUtils {

    /**
     * 根据指定大小和指定精度压缩图片
     *
     * @param imageData                     源图片
     * @param desFileSize             指定图片大小，单位kb
     * @param accuracy                精度，递归压缩的比率，建议小于0.9
     * @return
     */
    public static byte[] compressPic(byte[] imageData, long desFileSize, double accuracy) {


        try {
            // 1、先转换成jpg
            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(imageData)).scale(1f).toOutputStream(outBytes);
            // 递归压缩，直到目标文件大小小于desFileSize
            return compressPicCycle(outBytes.toByteArray(), desFileSize, accuracy);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] compressPicCycle(byte[] data, long desFileSize, double accuracy) throws IOException {

        // 2、判断大小，如果小于500kb，不压缩；如果大于等于500kb，压缩
        if (data.length <= desFileSize * 1024) {
            return data;
        }

        // 计算宽高
        BufferedImage bim = ImageIO.read(new ByteArrayInputStream(data));
        int srcWdith = bim.getWidth();
        int srcHeigth = bim.getHeight();
        int desWidth = new BigDecimal(srcWdith).multiply(new BigDecimal(accuracy)).intValue();
        int desHeight = new BigDecimal(srcHeigth).multiply(new BigDecimal(accuracy)).intValue();

        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(data)).size(desWidth, desHeight).outputQuality(accuracy).toOutputStream(tempOut);
        return compressPicCycle(tempOut.toByteArray(), desFileSize, accuracy);
    }

    public static void getPic(String fileName, String httpUrl) throws IOException {
        URL url = new URL(httpUrl);
        BufferedInputStream in = new BufferedInputStream(url.openStream());
        File file1 = new File(fileName);
        boolean newFile = file1.createNewFile();
        if(newFile){
            FileOutputStream outputStream = new FileOutputStream(file1);
            int t;
            while ((t = in.read()) != -1) {
                outputStream.write(t);
            }
            outputStream.close();
            in.close();
        }
    }


    /**
     * @Author YJY
     * @Description  将图片转化为base64
     * @Date  2020/9/10
     * @Param [filePath]
     * @return java.lang.String
     **/
    public static String encryptToBase64(String filePath) {
        if (filePath == null) {
            return null;
        }
        try {
            byte[] bytes = getFileStream(filePath);
            return java.util.Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encryptToBase64(byte[] bytes){
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }


    /**
     * 得到文件流
     * @param url
     * @return
     */
    public static byte[] getFileStream(String url){
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)httpUrl.openConnection();
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

    /**
     * 从输入流中获取数据
     * @param inStream 输入流
     * @return
     * @throws Exception
     */
    public static byte[] readInputStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1 ){
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

}
