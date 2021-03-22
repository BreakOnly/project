package com.jrmf.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class ExcelUtil {

	private static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

	//读取excel返回list
	public static List<Map<String,Object>> getExcData(MultipartFile file,String[] cellName){
		if(file == null || cellName == null){
			return null;
		}
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		try {
			InputStream is = file.getInputStream();
			Workbook workbook = null;
			try {
				workbook = new XSSFWorkbook(is);
			} catch (Exception ex) {
				workbook = new HSSFWorkbook(is);
			}
			Sheet sheet = workbook.getSheetAt(0);
			for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {
				XSSFRow row = (XSSFRow) sheet.getRow(j);
				if (row == null) {
					continue;
				}else{
					Map<String,Object> map = new HashMap<String,Object>();
					for (Cell cell : row) {
						//按照String格式获取每一个格子的数据
						String stringCellValue = cell.getStringCellValue();
						map.put("", "");
					}
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
		return data;
	}

	/**
	 * 根据excel模板获取sheet
	 * @param srcXlsPath
	 * @param sheetIndex
	 * @return
	 */
	public static HSSFSheet getSheet(String srcXlsPath,int sheetIndex) { 
		POIFSFileSystem fs = null;
		HSSFWorkbook wb = null;
		HSSFSheet sheet = null;
		try {  
			File fi = new File(srcXlsPath);  
			if (!fi.exists()) {  
				logger.info("模板文件:" + srcXlsPath + "不存在!");  
			}  
			fs = new POIFSFileSystem(new FileInputStream(fi));  
			wb = new HSSFWorkbook(fs);  
			sheet = wb.getSheetAt(sheetIndex);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(),e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
		return sheet;
	}  

	
	public static HSSFSheet getSheet(String srcXlsPath,int sheetIndex,boolean isFtp) { 
		POIFSFileSystem fs = null;
		HSSFWorkbook wb = null;
		HSSFSheet sheet = null;
		try {  
	        byte[] bytes = FtpTool.downloadFtpFile(srcXlsPath.substring(0, srcXlsPath.lastIndexOf("/")), srcXlsPath.substring(srcXlsPath.lastIndexOf("/")+1));
	        InputStream in = new ByteArrayInputStream(bytes);
	        fs = new POIFSFileSystem(in);  
			wb = new HSSFWorkbook(fs);  
			sheet = wb.getSheetAt(sheetIndex);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(),e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
		return sheet;
	}  
	/**
	 * 导出excel
	 * @param response
	 * @param filename
	 * @param wb
	 * @throws Exception
	 */
	public static void exportExcel(HttpServletResponse response,String filename,HSSFWorkbook wb) throws Exception {
		// 获取输出流
		OutputStream os = response.getOutputStream();
		// 重置输出流，这句话可以不加，但是一定要保证response的缓冲区没有其他对象
		response.reset();
		// 设置导出excel的头部信息
		response.setContentType("application/vnd.ms-excel");
		// 导出excel的文件的标题名称
		filename = filename + ".xls";
		filename = new String(filename.getBytes("gbk"), "iso-8859-1");
		response.setHeader("Content-disposition", "attachment; filename=" + filename);
		// 导出excel
		wb.write(os);// 将excel中的数据写到输出流中，用于文件的输出
        os.close();
	}
}
