package com.jrmf.utils.pdf.replace;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 种路路
 * @create 2019-02-18 16:59
 * @desc
 **/
public class TestReplace {
    public static void main(String[] args) throws Exception {
        long time1 = System.currentTimeMillis();
        List<String> content = getText(new FileInputStream("D:/pdf/4715.pdf"));
        int contentLength = 13;
        for (int i=0;i<content.size() / contentLength;i++){
                PdfReplacer textReplacer = new PdfReplacer("D:/pdf/4714.pdf");
                textReplacer.replaceText("2020-03-23", content.get(13 * i));
                textReplacer.replaceText("20032324010100000632", content.get(13 * i +1));
                textReplacer.replaceText("041906", content.get(13 * i +2));
                textReplacer.replaceText("15000099265693", content.get(13 * i +4));
                textReplacer.replaceText("宋艳歌", content.get(13 * i +5));
                textReplacer.replaceText("6217231702000496048", content.get(13 * i +6));
                textReplacer.replaceText("210.00", content.get(13 * i +7));
                String name = content.get(13 * i +8);
                textReplacer.replaceText("000549经营所得", name);
                textReplacer.replaceText("2020-04-09 11:37:16", content.get(13 * i +9));
                textReplacer.replaceText("贰佰壹拾元整", content.get(13 * i +10));
                textReplacer.replaceText("平安银行福州分行营业部", content.get(13 * i +11));
                textReplacer.replaceText("中国工商银行总行清算中心", content.get(13 * i +12));
                textReplacer.toPdf("D:/pdf/1.pdf");
        }
        long time2 = System.currentTimeMillis();
        long time = (time2 - time1) / 1000;
        System.out.println("共处理"+(content.size()/13)+"条，耗时"+time+"秒.!");
    }

    private static List<String> getText(InputStream ips) throws Exception {
        PDDocument pdfDocument = PDDocument.load(ips);
        StringWriter writer = new StringWriter();
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.writeText(pdfDocument, writer);
        pdfDocument.close();
        String contents = writer.getBuffer().toString();
        writer.close();
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contents.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String line;
        List<String> stringArrayList = new ArrayList<>();
        while ( (line = br.readLine()) != null ) {
            if(!isEmpty(line.trim())){
                stringArrayList.add(line.trim());
            }
        }
        br.close();
        int count = stringArrayList.size()/24;
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0 ;i<count;i++){
            String[] split = stringArrayList.get(24*i+10).split(" ");
            for (String s : split) {
                if(!isEmpty(s)){
                    arrayList.add(s);
                }
            }
            arrayList.add(stringArrayList.get(24*i+11));
            arrayList.add(stringArrayList.get(24*i+12));
            arrayList.add(stringArrayList.get(24*i+13));
            arrayList.add(stringArrayList.get(24*i+14));
            arrayList.add(stringArrayList.get(24*i+15));
            arrayList.add(stringArrayList.get(24*i+16));
            arrayList.add(stringArrayList.get(24*i+17));
            arrayList.add(stringArrayList.get(24*i+18));
            String[] split1 = stringArrayList.get(24*i+19).split(" ");
            for (String s : split1) {
                if(!isEmpty(s)){
                    arrayList.add(s);
                }
            }
        }
        return arrayList;
    }

    private static boolean isEmpty(String arg) {
        return (arg == null) || ("".equals(arg.trim()) || "null".equals(arg.trim()) || "NULL".equals(arg.trim()));
    }
}
