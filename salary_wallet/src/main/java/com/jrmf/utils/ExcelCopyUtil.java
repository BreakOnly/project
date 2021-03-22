package com.jrmf.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelCopyUtil {

	public static void copyCell(HSSFCell srcCell, HSSFCell distCell) {
		distCell.setCellStyle(srcCell.getCellStyle());
		if(srcCell.getCellComment() != null){
			distCell.setCellComment(srcCell.getCellComment());
		}
		int srcCellType = srcCell.getCellType();
		distCell.setCellType(srcCellType);
		if(srcCellType == HSSFCell.CELL_TYPE_NUMERIC){
			if(HSSFDateUtil.isCellDateFormatted(srcCell)){
				distCell.setCellValue(srcCell.getDateCellValue());
			}else{
				distCell.setCellValue(srcCell.getNumericCellValue());
			}
		}else if(srcCellType == HSSFCell.CELL_TYPE_STRING){
			distCell.setCellValue(srcCell.getRichStringCellValue());
		}else if(srcCellType == HSSFCell.CELL_TYPE_BLANK){
			// nothing21
		}else if(srcCellType == HSSFCell.CELL_TYPE_BOOLEAN){
			distCell.setCellValue(srcCell.getBooleanCellValue());
		}else if(srcCellType == HSSFCell.CELL_TYPE_ERROR){
			distCell.setCellErrorValue(srcCell.getErrorCellValue());
		}else if(srcCellType == HSSFCell.CELL_TYPE_FORMULA){
			distCell.setCellFormula(srcCell.getCellFormula());
		}else{
			// nothing29
		}
	}

}
