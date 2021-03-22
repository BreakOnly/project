package com.jrmf.test;

import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-04-17 18:55
 * @desc
 **/
public class TestExcel {
    public static void main(String[] args) throws IOException {

        File file = new File("D:/a.xlsx");
        InputStream is = new FileInputStream(file);

        int readLen = -1;
        byte[] byteBuffer = new byte[1024];
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

        while ((readLen = is.read(byteBuffer)) > -1) {
            bytesOut.write(byteBuffer, 0, readLen);
        }
        byte[] fileData = bytesOut.toByteArray();
        Workbook workbook = null;

        try {
            workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData));
        } catch (Exception ex) {
            workbook = new HSSFWorkbook(new ByteArrayInputStream(fileData));
        }
        Sheet sheet = workbook.getSheetAt(0);
        for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {
            XSSFRow row = (XSSFRow) sheet.getRow(j);
            if (row == null) {
                continue;
            }
            if(StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0))) &&
                    StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1))) &&
                    StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(2)))){
                continue;
            }

            String userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));// 收款人真实姓名(必要)
            String amount = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(1)));// 金额(必要)
            String alipayAccount = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(2)));
            String phoneNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(3)));// 手机号
            String certId = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(4)));// 身份证号
            String documentType = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(5)));// 证件类型
            String remark = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(6)));//  备注
            System.out.println(userName+","+amount+","+alipayAccount+","+phoneNo+","+certId+","+documentType+","+remark);

        }

    }
}
