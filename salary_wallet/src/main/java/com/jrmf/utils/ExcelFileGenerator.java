package com.jrmf.utils;

import com.jrmf.domain.UserCommission;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import javax.swing.plaf.synth.Region;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import com.jrmf.common.DataValue;
import org.apache.poi.ss.util.CellRangeAddress;

//系统数据导出Excel 生成器
public class ExcelFileGenerator {

  private final int SPLIT_COUNT = 50000; // Excel每个工作簿的行数

  private ArrayList<String> fieldName = null; // excel标题数据集

  private ArrayList<String> fieldData = null; // excel数据内容

  private HSSFWorkbook workBook = null;

  /**
   * 构造器
   *
   * @param fieldName 结果集的字段名
   */
  public ExcelFileGenerator(ArrayList<String> fieldName, ArrayList<String> fieldData) {

    this.fieldName = fieldName;
    this.fieldData = fieldData;
  }

  /**
   * 创建HSSFWorkbook对象
   *
   * @return HSSFWorkbook
   */
  @SuppressWarnings("deprecation")
  public HSSFWorkbook createWorkbook() {

    workBook = new HSSFWorkbook();// 创建一个工作薄对象
    int rows = fieldData.size();// 总的记录数
    int sheetNum = 0; // 指定sheet的页数

    if (rows % SPLIT_COUNT == 0) {
      sheetNum = rows / SPLIT_COUNT;
      HSSFSheet sheet = workBook.createSheet("Page " + 1);
      setHeader(sheet);
    } else {
      sheetNum = rows / SPLIT_COUNT + 1;
    }

    for (int i = 1; i <= sheetNum; i++) {// 循环2个sheet的值
      HSSFSheet sheet = workBook.createSheet("Page " + i);// 使用workbook对象创建sheet对象
      setHeader(sheet);
      // 分页处理excel的数据，遍历所有的结果
      for (int k = 0; k < (rows < SPLIT_COUNT ? rows : SPLIT_COUNT); k++) {
        if (((i - 1) * SPLIT_COUNT + k) >= rows)// 如果数据超出总的记录数的时候，就退出循环
        {
          break;
        }
        HSSFRow row = sheet.createRow((k + 1));// 创建1行
        // 分页处理，获取每页的结果集，并将数据内容放入excel单元格
        String str = fieldData.get((i - 1) * SPLIT_COUNT + k);
        String[] strArr = str.split(",");
        for (int n = 0; n < strArr.length; n++) {// 遍历某一行的结果
          HSSFCell cell = row.createCell((short) n);// 使用行创建列对象
          if (strArr[n] != null) {
            cell.setCellValue((String) strArr[n]);
          } else {
            cell.setCellValue("");
          }
        }

        /*
         * ArrayList<String> rowList = (ArrayList<String>)
         * fieldData.get((i - 1) * SPLIT_COUNT + k); for (int n = 0; n <
         * rowList.size(); n++) {//遍历某一行的结果 HSSFCell cell =
         * row.createCell( (short) n);//使用行创建列对象 if(rowList.get(n) !=
         * null){ cell.setCellValue((String) rowList.get(n).toString());
         * }else{ cell.setCellValue(""); } }
         */
      }
    }
    return workBook;
  }
  /**
   * 创建HSSFWorkbook对象
   *
   * @return HSSFWorkbook
   */
  @SuppressWarnings("deprecation")
  public HSSFWorkbook createWorkbookAndMergedRegion(List<Map<String, Object>> mapList) {

    workBook = new HSSFWorkbook();// 创建一个工作薄对象
    int rows = fieldData.size();// 总的记录数
    int sheetNum = 0; // 指定sheet的页数

    if (rows % SPLIT_COUNT == 0) {
      sheetNum = rows / SPLIT_COUNT;
      HSSFSheet sheet = workBook.createSheet("Page " + 1);
      setHeader(sheet);
    } else {
      sheetNum = rows / SPLIT_COUNT + 1;
    }

    for (int i = 1; i <= sheetNum; i++) {// 循环2个sheet的值
      HSSFSheet sheet = workBook.createSheet("Page " + i);// 使用workbook对象创建sheet对象
      setHeader(sheet);
      // 分页处理excel的数据，遍历所有的结果
      for (int k = 0; k < (rows < SPLIT_COUNT ? rows : SPLIT_COUNT); k++) {
        if (((i - 1) * SPLIT_COUNT + k) >= rows)// 如果数据超出总的记录数的时候，就退出循环
        {
          break;
        }
        HSSFRow row = sheet.createRow((k + 1));// 创建1行
        // 分页处理，获取每页的结果集，并将数据内容放入excel单元格
        String str = fieldData.get((i - 1) * SPLIT_COUNT + k);
        String[] strArr = str.split(",");
        for (int n = 0; n < strArr.length; n++) {// 遍历某一行的结果
          HSSFCell cell = row.createCell((short) n);// 使用行创建列对象
          if (strArr[n] != null) {
            cell.setCellValue((String) strArr[n]);
          } else {
            cell.setCellValue("");
          }
        }


        /*
         * ArrayList<String> rowList = (ArrayList<String>)
         * fieldData.get((i - 1) * SPLIT_COUNT + k); for (int n = 0; n <
         * rowList.size(); n++) {//遍历某一行的结果 HSSFCell cell =
         * row.createCell( (short) n);//使用行创建列对象 if(rowList.get(n) !=
         * null){ cell.setCellValue((String) rowList.get(n).toString());
         * }else{ cell.setCellValue(""); } }
         */
      }

      //计算之前统计的位置信息 将需要合并的row进行合并
      String invoiceSerialNo = "";
      int minLocation = 0;  int maxLocation = 0;
      int invoiceType = 0;
      if(CollectionUtils.isNotEmpty(mapList)){
        invoiceSerialNo = (String) mapList.get(0).get("invoiceSerialNo");
        minLocation = (int) mapList.get(0).get("location");
        invoiceType = (Integer) mapList.get(0).get("invoiceType");
      }
      for(int m = 0;m<mapList.size();m++){

        if(invoiceSerialNo.equals(mapList.get(m).get("invoiceSerialNo")) &&
            invoiceType==  (Integer) mapList.get(m).get("invoiceType")){

          maxLocation = (int)mapList.get(m).get("location");
        }else{

          if(minLocation<maxLocation){

            for(int mr = 0; mr<24;mr++) {
              sheet.addMergedRegion(new CellRangeAddress(minLocation, maxLocation, mr, mr));
            }

          }


          invoiceSerialNo = (String) mapList.get(m).get("invoiceSerialNo");
          minLocation = (int)mapList.get(m).get("location");
          invoiceType = (Integer) mapList.get(m).get("invoiceType");
          maxLocation = 0;
        }

      }

      if(minLocation<maxLocation){

        for(int mr = 0; mr<24;mr++) {
          sheet.addMergedRegion(new CellRangeAddress(minLocation, maxLocation, mr, mr));
        }
      }

    }
    return workBook;
  }

  private void setHeader(HSSFSheet sheet) {
    HSSFRow headRow = sheet.createRow(0); // 创建行，0表示第一行（本例是excel的标题）
    for (int j = 0; j < fieldName.size(); j++) {// 循环excel的标题
      HSSFCell cell = headRow.createCell((short) j);// 使用行对象创建列对象，0表示第1列
      /************** 对标题添加样式begin ********************/
      /**
       * //设置列的宽度/ sheet.setColumnWidth(j, 6000); HSSFCellStyle
       * cellStyle = workBook.createCellStyle();//创建列的样式对象 HSSFFont
       * font = workBook.createFont();//创建字体对象 //字体加粗
       * font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); //字体颜色变红
       * font.setColor(HSSFColor.RED.index);
       * //如果font中存在设置后的字体，并放置到cellStyle对象中，此时该单元格中就具有了样式字体
       * cellStyle.setFont(font);
       */
      /************** 对标题添加样式end ********************/

      // 添加样式
      cell.setCellType(HSSFCell.CELL_TYPE_STRING);
      if (fieldName.get(j) != null) {
        // 将创建好的样式放置到对应的单元格中
        /** cell.setCellStyle(cellStyle); */
        cell.setCellValue((String) fieldName.get(j));// 为标题中的单元格设置值
      } else {
        cell.setCellValue("-");
      }
    }
  }


  public void expordExcel(OutputStream os) throws Exception {
    workBook = createWorkbook();
    workBook.write(os);// 将excel中的数据写到输出流中，用于文件的输出
    os.close();
  }
  public void expordExcelAndMergedRegion(OutputStream os, List<Map<String, Object>> mapList) throws Exception {
    workBook = createWorkbookAndMergedRegion(mapList);
    workBook.write(os);// 将excel中的数据写到输出流中，用于文件的输出
    os.close();
  }

  public static void exportExcel(HttpServletResponse response, ArrayList<String> fieldName,
      ArrayList<String> dataStr,
      String filename) throws Exception {
    ExcelFileGenerator generator = new ExcelFileGenerator(fieldName, dataStr);
    // 获取输出流
    OutputStream os = response.getOutputStream();
    // 重置输出流，这句话可以不加，但是一定要保证response的缓冲区没有其他对象
    response.reset();
    // 设置导出excel的头部信息
    response.setContentType("application/vnd.ms-excel");
    // 导出excel的文件的标题名称
    filename = filename + ".xls";
    // filename = URLEncoder.encode(filename, "UTF-8");
    filename = new String(filename.getBytes("gbk"), "iso-8859-1");
    response.setHeader("Content-disposition", "attachment; filename=" + filename);
    // 导出excel
    generator.expordExcel(os);
  }
  public static void exportExcelAndMergedRegion(HttpServletResponse response, ArrayList<String> fieldName,
      ArrayList<String> dataStr,
      String filename, List<Map<String, Object>> mapList) throws Exception {
    ExcelFileGenerator generator = new ExcelFileGenerator(fieldName, dataStr);
    // 获取输出流
    OutputStream os = response.getOutputStream();
    // 重置输出流，这句话可以不加，但是一定要保证response的缓冲区没有其他对象
    response.reset();
    // 设置导出excel的头部信息
    response.setContentType("application/vnd.ms-excel");
    // 导出excel的文件的标题名称
    filename = filename + ".xls";
    // filename = URLEncoder.encode(filename, "UTF-8");
    filename = new String(filename.getBytes("gbk"), "iso-8859-1");
    response.setHeader("Content-disposition", "attachment; filename=" + filename);
    // 导出excel
    generator.expordExcelAndMergedRegion(os,mapList);
  }
  /**
   * 利用poi读取Excel文件
   *
   * @param filePath excel文件路径
   * @return DataValue 返回数据集合.
   * @throws IOException
   */
  public static DataValue readExcel(String filePath) throws IOException {
    DataValue dataValueSet = new DataValue();
    FileInputStream fis = new FileInputStream(filePath); // 根据excel文件路径创建文件流
    POIFSFileSystem fs = new POIFSFileSystem(fis); // 利用poi读取excel文件流
    HSSFWorkbook wb = new HSSFWorkbook(fs); // 读取excel工作簿
    HSSFSheet sheet = wb.getSheetAt(0); // 读取excel的sheet，0表示读取第一个、1表示第二个.....

    // 获取sheet中总共有多少行数据sheet.getPhysicalNumberOfRows()
    int rowNumber = sheet.getPhysicalNumberOfRows();
    HSSFRow row = sheet.getRow(0);
    int columnNumber = row.getPhysicalNumberOfCells();

    String[] title = new String[columnNumber];
    List<String[]> valueSet = new ArrayList<String[]>(rowNumber);

    // setting title.
    for (int i = 0; i < columnNumber; i++) {
      title[i] = getStringCellValue(row.getCell(i));
    }
    // setting value.
    for (int i = 1; i < rowNumber; i++) {
      row = sheet.getRow(i); // 取出sheet中的某一行数据
      String[] value = new String[columnNumber];
      for (int j = 0; row != null && j < columnNumber; j++) { // 获取该行中总共有多少列数据row.getLastCellNum()
        value[j] = getStringCellValue(row.getCell(j)); // 获取该行中的一个单元格对象
      }
      valueSet.add(value);
    }
    fis.close();
    dataValueSet.setTitle(title);
    dataValueSet.setValue(valueSet);
    return dataValueSet;
  }

  /**
   * 利用poi读取Excel文件
   * <p>
   * excel文件路径
   *
   * @return DataValue 返回数据集合.
   * @throws IOException
   */
  public static DataValue readExcel(InputStream fis) throws IOException {
    DataValue dataValueSet = new DataValue();
    // FileInputStream fis = new FileInputStream(filePath); //
    // 根据excel文件路径创建文件流
    POIFSFileSystem fs = new POIFSFileSystem(fis); // 利用poi读取excel文件流
    HSSFWorkbook wb = new HSSFWorkbook(fs); // 读取excel工作簿
    HSSFSheet sheet = wb.getSheetAt(0); // 读取excel的sheet，0表示读取第一个、1表示第二个.....

    // 获取sheet中总共有多少行数据sheet.getPhysicalNumberOfRows()
    int rowNumber = sheet.getPhysicalNumberOfRows();
    HSSFRow row = sheet.getRow(0);
    int columnNumber = row.getPhysicalNumberOfCells();

    String[] title = new String[columnNumber];
    List<String[]> valueSet = new ArrayList<String[]>(rowNumber);

    // setting title.
    for (int i = 0; i < columnNumber; i++) {
      title[i] = getStringCellValue(row.getCell(i));
    }
    // setting value.
    for (int i = 1; i < rowNumber; i++) {
      row = sheet.getRow(i); // 取出sheet中的某一行数据
      String[] value = new String[columnNumber];
      for (int j = 0; row != null && j < columnNumber; j++) { // 获取该行中总共有多少列数据row.getLastCellNum()
        value[j] = getStringCellValue(row.getCell(j)); // 获取该行中的一个单元格对象
      }
      valueSet.add(value);
    }
    fis.close();
    dataValueSet.setTitle(title);
    dataValueSet.setValue(valueSet);
    return dataValueSet;
  }

  /**
   * 读取文件样本值.
   *
   * @param filePath
   * @return
   * @throws IOException
   */
  public static List<String> readSampleValueFromExcel(String filePath) throws IOException {
    FileInputStream fis = new FileInputStream(filePath); // 根据excel文件路径创建文件流
    POIFSFileSystem fs = new POIFSFileSystem(fis); // 利用poi读取excel文件流
    HSSFWorkbook wb = new HSSFWorkbook(fs); // 读取excel工作簿
    HSSFSheet sheet = wb.getSheetAt(0); // 读取excel的sheet，0表示读取第一个、1表示第二个.....
    HSSFRow row = sheet.getRow(0);
    int columnNumber = row.getPhysicalNumberOfCells();
    row = sheet.getRow(1);
    String[] value = new String[columnNumber];

    // setting title.
    for (int i = 0; i < columnNumber; i++) {
      value[i] = getStringCellValue(row.getCell(i));
      System.out.print(value[i] + "\t");
    }
    System.out.println();

    fis.close();
    return Arrays.asList(value);
  }

  /**
   * 读取文件Title
   *
   * @param filePath
   * @return
   * @throws IOException
   */
  public static String readTitleFromExcel(String filePath) throws IOException {
    FileInputStream fis = new FileInputStream(filePath); // 根据excel文件路径创建文件流
    POIFSFileSystem fs = new POIFSFileSystem(fis); // 利用poi读取excel文件流
    HSSFWorkbook wb = new HSSFWorkbook(fs); // 读取excel工作簿
    HSSFSheet sheet = wb.getSheetAt(0); // 读取excel的sheet，0表示读取第一个、1表示第二个.....
    HSSFRow row = sheet.getRow(0);
    int columnNumber = row.getPhysicalNumberOfCells();
    StringBuffer sd = new StringBuffer();
    // setting title.
    for (int i = 0; i < columnNumber; i++) {
      sd.append(row.getCell(i));
    }
    fis.close();
    return sd.toString();
  }

  /**
   * 获取单元格数据内容为字符串类型的数据
   *
   * @param cell Excel单元格
   * @return String 单元格数据内容
   */
  public static String getStringCellValue(HSSFCell cell) {
    String strCell = "";
    try {
      switch (cell.getCellType()) {
        case HSSFCell.CELL_TYPE_STRING:
          strCell = cell.getStringCellValue();
          break;
        case HSSFCell.CELL_TYPE_NUMERIC:
          if (HSSFDateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            strCell = DateUtils.formartDate(date, "yyyy-MM-dd");
            return strCell;
          }
          strCell = String.valueOf(Math.round(cell.getNumericCellValue()));
          break;
        case HSSFCell.CELL_TYPE_BOOLEAN:
          strCell = String.valueOf(cell.getBooleanCellValue());
          break;
        case HSSFCell.CELL_TYPE_BLANK:
          strCell = "";
          break;
        default:
          strCell = "";
          break;
      }
    } catch (Exception e) {
      return null;
    }

    if (strCell.equals("") || strCell == null) {
      return null;
    }
    return strCell;
  }

  /**
   * 利用poi读取Excel文件
   *
   * @param filePath excel文件路径
   * @return DataValue 返回数据集合.
   * @throws IOException
   */
  public static DataValue readGSExcel(String filePath) throws IOException {
    DataValue dataValueSet = new DataValue();
    FileInputStream fis = new FileInputStream(filePath); // 根据excel文件路径创建文件流
    POIFSFileSystem fs = new POIFSFileSystem(fis); // 利用poi读取excel文件流
    HSSFWorkbook wb = new HSSFWorkbook(fs); // 读取excel工作簿
    HSSFSheet sheet = wb.getSheetAt(0); // 读取excel的sheet，0表示读取第一个、1表示第二个.....
    FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
    // 获取sheet中总共有多少行数据sheet.getPhysicalNumberOfRows()
    int rowNumber = sheet.getPhysicalNumberOfRows();
    HSSFRow row = sheet.getRow(0);
    int columnNumber = row.getPhysicalNumberOfCells();

    String[] title = new String[columnNumber];
    List<String[]> valueSet = new ArrayList<String[]>(rowNumber);

    // setting title.
    for (int i = 0; i < columnNumber; i++) {
      title[i] = getStringCellValue(row.getCell(i));
    }
    // setting value.
    for (int i = 1; i < rowNumber; i++) {
      row = sheet.getRow(i); // 取出sheet中的某一行数据
      String[] value = new String[columnNumber];
      for (int j = 0; row != null && j < columnNumber; j++) { // 获取该行中总共有多少列数据row.getLastCellNum()
        value[j] = getStringCellValue(evaluator.evaluate(row.getCell(j)),
            row.getCell(j)); // 获取该行中的一个单元格对象
      }
      valueSet.add(value);
    }
    fis.close();
    dataValueSet.setTitle(title);
    dataValueSet.setValue(valueSet);
    return dataValueSet;
  }

  /**
   * 获取单元格数据内容为字符串类型的数据
   *
   * @param cell Excel单元格
   * @return String 单元格数据内容
   */
  public static String getStringCellValue(CellValue cell, HSSFCell cell_unProcess) {
    String strCell = "";
    try {
      switch (cell.getCellType()) {
        case Cell.CELL_TYPE_STRING:
          strCell = cell.getStringValue();
          break;
        case Cell.CELL_TYPE_NUMERIC:
          if (HSSFDateUtil.isCellDateFormatted(cell_unProcess)) {
            Date date = cell_unProcess.getDateCellValue();
            strCell = DateUtils.formartDate(date, "yyyy-MM-dd");
            return strCell;
          }
          strCell = String.valueOf(Math.round(cell.getNumberValue()));
          break;
        case Cell.CELL_TYPE_BOOLEAN:
          strCell = String.valueOf(cell.getBooleanValue());
          break;
        case Cell.CELL_TYPE_BLANK:
          strCell = "";
          break;
        default:
          strCell = "";
          break;
      }
    } catch (Exception e) {
      return null;
    }

    if (strCell.equals("") || strCell == null) {
      return null;
    }
    return strCell;
  }

  /**
   * 公共导出
   */
  public static void ExcelExport(HttpServletResponse response, String[] colunmName, String filename,
      List<Map<String, Object>> data) {
    // 设置入参
    ArrayList<String> dataStr = new ArrayList<>();
    ArrayList<String> fieldName = new ArrayList<>();
    // 组装列名
    fieldName.addAll(Arrays.asList(colunmName));
    // 组装列值
    for (Map<String, Object> map : data) {
      StringBuffer str = new StringBuffer();
      for (Entry<String, Object> entry : map.entrySet()) {
        String value =
            entry.getValue() == null ? "" : entry.getValue().toString().replaceAll(",", "，");
        str.append(value).append(",");
      }
      dataStr.add(str.substring(0, str.length() - 1));
    }
    String today = DateUtils.getNowDay();
    // 设置文件名
    filename = today + filename;
    try {
      ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 合并列
   */
  public static void ExcelExportAndMergedRegion(HttpServletResponse response, String[] columnName,
      String filename,
      List<Map<String, Object>> data, List<UserCommission> listDetails) {

    //记录需要合并的列
    List<Map<String, Object>> mergedRegionData = new ArrayList<>();
    // 设置入参
    ArrayList<String> dataStr = new ArrayList<>();

    ArrayList<String> fieldName = new ArrayList<>();
    // 组装列名
    fieldName.addAll(Arrays.asList(columnName));
    boolean checkFlag = true;
    // 组装列值
    for (Map<String, Object> map : data) {
      StringBuffer str = new StringBuffer();
      int invoiceType = 0;
      for (Entry<String, Object> entry : map.entrySet()) {

        if("25".equals(entry.getKey())){
          invoiceType = (Integer)entry.getValue();
        }else {
          String value =
              entry.getValue() == null ? "" : entry.getValue().toString().replaceAll(",", "，");
          str.append(value).append(",");
        }
      }
      String rowValue = str.substring(0, str.length() - 1);
      //将查询到的统计明细数据 合并到原有的Excel

       Iterator<UserCommission> it = listDetails.iterator();

       while (it.hasNext()){
         UserCommission userCommission = it.next();
         if (rowValue.contains(userCommission.getInvoiceSerialNo())) {
           HashMap hashMap = new HashMap();
           checkFlag = false;
           String rows =
               rowValue +","+ userCommission.getAccountDate() + "," + userCommission.getAmount()
                   + "," +
                   userCommission.getCalculationRates() + "," + userCommission.getTaxRate() + "," +
                   userCommission.getIndividualTax() + "," + userCommission.getIndividualBackTax();

           dataStr.add(rows);
           hashMap.put("invoiceSerialNo",userCommission.getInvoiceSerialNo());
           hashMap.put("invoiceType",invoiceType);
           hashMap.put("location",dataStr.size());
           mergedRegionData.add(hashMap);
//           it.remove();
         }
       }

      if(checkFlag){
        dataStr.add(rowValue);
      }
      checkFlag = true;
    }
    String today = DateUtils.getNowDay();
    // 设置文件名
    filename = today + filename;
    try {
      ExcelFileGenerator.exportExcelAndMergedRegion(response, fieldName, dataStr, filename,mergedRegionData);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
