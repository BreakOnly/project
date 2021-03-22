package com.jrmf.test;

import java.io.*;

/**
 * @author 种路路
 * @create 2019-04-17 8:59
 * @desc 导出协议
 **/
public class TestAgreement {
    public static void main(String[] args) throws IOException {
        String userName = "种路路";
        String certId = "123456";
        String time = "123456";
        replace(userName,certId,time);
    }

    private static void replace(String userName, String certId, String time) throws IOException {
        //待替换字符
        String aStr="http";
        //替换字符
        String bStr="https";

        //读取文件
        File file=new File("");

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

        //内存流
        CharArrayWriter caw=new CharArrayWriter();

        //替换
        String line=null;

        //以行为单位进行遍历
        while((line=br.readLine())!=null){
            //替换每一行中符合被替换字符条件的字符串
            line=line.replaceAll(aStr, bStr);
            //将该行写入内存
            caw.write(line);
            //添加换行符，并进入下次循环
            caw.append(System.getProperty("line.separator"));
        }
        //关闭输入流
        br.close();

        //将内存中的流写入源文件
        FileWriter fw=new FileWriter(file);
        caw.writeTo(fw);
        fw.close();
    }

}
