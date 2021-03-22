package com.jrmf.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author chonglulu
 */
public class ZipUtils {

    private static Logger logger = LoggerFactory.getLogger(ZipUtils.class);
    private ZipUtils(){
    }
    
    public static void doCompress(String srcFile, String zipFile) throws IOException {
        doCompress(new File(srcFile), new File(zipFile));
    }
    
    /**
     * 文件压缩
     * @param srcFile 目录或者单个文件
     * @param zipFile 压缩后的ZIP文件
     */
    private static void doCompress(File srcFile, File zipFile) throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {
            doCompress(srcFile, out);
        }
    }
    
    private static void doCompress(File file, ZipOutputStream out) throws IOException{
        doCompress(file, out, "");
    }
    
    private static void doCompress(File inFile, ZipOutputStream out, String dir) throws IOException {
        if ( inFile.isDirectory() ) {
            File[] files = inFile.listFiles();
            if (files!=null && files.length>0) {
                for (File file : files) {
                    String name = inFile.getName();
                    if (!"".equals(dir)) {
                        name = dir + "/" + name;
                    }
                    doCompress(file, out, name);
                }
            }
        } else {
             doZip(inFile, out, dir);
        }
    }
    
    private static void doZip(File inFile, ZipOutputStream out, String dir) throws IOException {
        String entryName ;
        if (!"".equals(dir)) {
            entryName = dir + "/" + inFile.getName();
        } else {
            entryName = inFile.getName();
        }
        ZipEntry entry = new ZipEntry(entryName);
        out.putNextEntry(entry);
        
        int len ;
        byte[] buffer = new byte[1024];
        FileInputStream fis = new FileInputStream(inFile);
        while ((len = fis.read(buffer)) > 0) {
            out.write(buffer, 0, len);
            out.flush();
        }
        out.closeEntry();
        fis.close();
    }

    public static void unzip(File zipFile, String unZipPath, String rename) {

        ZipFile zip ;
        try {
            //指定编码，否则压缩包里面不能有中文目录
            zip = new ZipFile(zipFile, Charset.forName("GBK"));

            for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry;
                try {
                    entry = (ZipEntry) entries.nextElement();
                } catch (Exception e) {
                    logger.error("解压失败：{}",e.getMessage());
                    return;
                }

                String zipEntryName = entry.getName();
                InputStream in = zip.getInputStream(entry);

                String substring;
                if (zipEntryName.contains("/")) {
                    substring = zipEntryName.substring(0, zipEntryName.indexOf("/"));
                    zipEntryName = zipEntryName.replace(substring, rename);
                } else {
                    zipEntryName = rename + "/" + zipEntryName;
                }
                //解压重命名
                String outPath = (unZipPath + zipEntryName).replace("/", File.separator);

                //判断路径是否存在,不存在则创建文件路径
                File outfilePath = new File(outPath.substring(0, outPath.lastIndexOf(File.separator)));
                if (!outfilePath.exists()) {
                    boolean mkdirs = outfilePath.mkdirs();
                    if(!mkdirs){
                        logger.info("创建文件夹失败");
                    }
                }
                //判断文件全路径是否为文件夹
                if (new File(outPath).isDirectory()) {
                    continue;
                }
                OutputStream out = new FileOutputStream(outPath);
                byte[] buf1 = new byte[2048];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
                in.close();
                out.close();
            }
            //必须关闭，否则无法删除该zip文件
            zip.close();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        logger.info("解压成功");

    }


    public static void unzip(File zipFile, String unZipPath) {

        ZipFile zip ;
        try {
            //指定编码，否则压缩包里面不能有中文目录
            zip = new ZipFile(zipFile, Charset.forName("GBK"));

            for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry;
                try {
                    entry = (ZipEntry) entries.nextElement();
                } catch (Exception e) {
                    logger.error("解压失败：{}",e.getMessage());
                    return;
                }

                String zipEntryName = entry.getName();
                InputStream in = zip.getInputStream(entry);

                //解压重命名
                String outPath = (unZipPath + zipEntryName).replace("/", File.separator);

                //判断路径是否存在,不存在则创建文件路径
                File outfilePath = new File(outPath.substring(0, outPath.lastIndexOf(File.separator)));
                if (!outfilePath.exists()) {
                    boolean mkdirs = outfilePath.mkdirs();
                    if(!mkdirs){
                        logger.info("创建文件夹失败");
                    }
                }
                //判断文件全路径是否为文件夹
                if (new File(outPath).isDirectory()) {
                    continue;
                }
                OutputStream out = new FileOutputStream(outPath);
                byte[] buf1 = new byte[2048];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
                in.close();
                out.close();
            }
            //必须关闭，否则无法删除该zip文件
            zip.close();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        logger.info("解压成功");

    }

}