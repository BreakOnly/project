package com.jrmf.payment.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrmf.utils.StringUtil;

public class TestExcel {

	private static Logger logger = LoggerFactory.getLogger(TestExcel.class);

	public static void main(String[] args) {

		File file = new File("D:/用户目录/下载/支付宝下发模板 - 2000.xlsx");
		FileInputStream is = null;
		Workbook workbook = null;
		ByteArrayOutputStream bytesOut = null;
		try {
			
			if (file != null) {
				is = new FileInputStream(file);
				
				int readLen = -1;
				byte[] byteBuffer = new byte[1024];
				bytesOut = new ByteArrayOutputStream();
				
				while((readLen = is.read(byteBuffer)) > -1) {
					bytesOut.write(byteBuffer, 0, readLen);
				}
				byte[] fileData = bytesOut.toByteArray();
				
				try {
					workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData));
				} catch (Exception ex) {
					workbook = new HSSFWorkbook(new ByteArrayInputStream(fileData));
				}
			}
//		
//			
//			
		Sheet sheet =	workbook.getSheetAt(0);
//		
//		XSSFRow row = (XSSFRow) sheet.getRow(1);
//		String alipayTemple = StringUtil.getXSSFCell(row.getCell(0)) 
//		+ StringUtil.getXSSFCell(row.getCell(1))
//		+ StringUtil.getXSSFCell(row.getCell(2))
//		+ StringUtil.getXSSFCell(row.getCell(3))
//		+ StringUtil.getXSSFCell(row.getCell(4))
//		+ StringUtil.getXSSFCell(row.getCell(5));
		
		//数字字符串 
//		String StrBd="1048576.102432445623000000015"; 
//		//构造以字符串内容为值的BigDecimal类型的变量bd 
//		BigDecimal bd=new BigDecimal(StrBd); 
//		//设置小数位数，第一个变量是小数位数，第二个变量是取舍方法(四舍五入) 
////		bd=bd.setScale(2, BigDecimal.ROUND_HALF_UP); 
//		//转化为字符串输出 
//		String OutString=bd.toString(); 
//		
//		logger.info("----："+OutString);
		
//		for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {// 获取每行
//			XSSFRow row = (XSSFRow) sheet.getRow(j);
//			if (row == null) {
//				continue;
//			}
//			if(StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0))) &&
//					StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1))) &&
//					StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(2)))){
//				continue;
//			}
//			
//			String userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));// 收款人真实姓名(必要)
//			String amount = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(1)));// 金额(必要)
//			String alipayAccount = ArithmeticUtil.subZeroAndDot(row.getCell(2).getStringCellValue());// 收款人支付宝账号(必要)
//			String certId = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(3)));// 身份证号
//			String documentType = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(4)));// 证件类型
//			String remark = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(5)));//  备注
//
//        	if("身份证".equals(documentType)){
//        		documentType = "1";
//        	}else if("护照".equals(documentType)){
//        		documentType = "3";
//        	}else if("军官证".equals(documentType)){
//        		documentType = "4";
//        	}else if("港澳台通行证".equals(documentType)){
//        		documentType = "2";
//        	}else{
//        		documentType = "0";
//        	}
//			
//			logger.info("-----------导入---Ali------------userName："+userName
//					+ " alipayAccount=" + alipayAccount
//					+ " certId=" + certId
//					+ " amount=" + amount
//					+ " documentType=" + documentType
//					+ " remark=" + remark
//					+ "---------------------------");
//		}
//	}catch(Exception e){
//		e.printStackTrace();
//	}
		System.out.println(java.net.URLEncoder.encode("https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=2018080660984246&scope=auth_user&redirect_uri=http%3A%2F%2Fwallet-pre.jrmf360.com%2Fchannel%2Fpayment%2FalipayAuth.do"));
//		System.out.println(java.net.URLEncoder.encode("http://wallet-pre.jrmf360.com/channel/payment/alipayAuth.do"));
		
//		int n = 3;
//		int m = 4;
//		loop: for(int i = 0; i<n ; i++){
//			System.out.println("i："+i);
//		    for(int im = 0; im<m ;im++){
//		    	System.out.println("im："+im);
//		        for(int k = 0; k<n; k++){
//		        	System.out.println("k："+k);
//		            continue loop;
//		        }
//		    }
//		    System.out.println("uuuuuuuuuuu：");
//		}
		
		
//		Set<String> validateUserName = new HashSet<String>();
//		System.out.println(validateUserName.add("张33"));
//		System.out.println(validateUserName.add("张32"));
//		System.out.println(validateUserName.add("张30"));
//		System.out.println(validateUserName.add("张33"));
//		
//		System.out.println(validateUserName.contains("张31"));
//		System.out.println(validateUserName.contains("张33"));
//		System.out.println(validateUserName.contains("张32"));
//	
		
//		String agreementTemplateId = "";
//		int n =0;
//    	for(int ai = 0; ai < n; ai++){
//			agreementTemplateId = ai + "," + agreementTemplateId;
//    	}
//    	agreementTemplateId = agreementTemplateId.substring(0, agreementTemplateId.lastIndexOf(","));
//    	System.out.println(agreementTemplateId);
		int num = 0;
		for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {
			XSSFRow row = (XSSFRow) sheet.getRow(j);
			if (row == null) {
				continue;
			}
			
			//支付宝
			if(2 == 2){
				if(StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0))) &&
						StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1))) &&
						StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(2)))){
					continue;
				}
			}
			//银行卡
//			if(payType == 4){
//				if (StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0)))
//						&& StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1)))
//						&& StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(3)))) {
//					sheet.removeRow(row);
//					continue;
//				}
//			}
			++num;
		}
		System.out.println(num);
		
		if (num > 2000 || num < 0) {
			System.out.println(0);
		}
		System.out.println(1);
		}catch(Exception e){
			
		}
	}

}
