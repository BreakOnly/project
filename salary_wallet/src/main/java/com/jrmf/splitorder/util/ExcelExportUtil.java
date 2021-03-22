package com.jrmf.splitorder.util;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.CertType;
import com.jrmf.domain.UserCommission;
import com.jrmf.splitorder.domain.BaseOrderInfo;
import com.jrmf.splitorder.domain.SplitFailOrder;
import com.jrmf.splitorder.domain.SplitLaveOrder;
import com.jrmf.splitorder.domain.SplitSuccessOrder;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.StringUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class ExcelExportUtil {
    private static final Logger logger = LoggerFactory.getLogger(ExcelExportUtil.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    // 该方法返回的模板要从 row(2) 开始写数据
    public static XSSFWorkbook getTemplate(Integer templateNo) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("拆单数据");
        sheet.setColumnWidth(0, 130 * 40);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
        XSSFRow row = sheet.createRow(0);
        XSSFCell cell = row.createCell(0);
        // 初始化字体 并加粗
        XSSFFont fontStyle = workbook.createFont();
        fontStyle.setBold(true);
        // 初始化 样式
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        // 将字体设置 加入到样式中
        cellStyle.setFont(fontStyle);
        cell.setCellStyle(cellStyle);
        // 加入文字
        XSSFCell cell1, cell2, cell3, cell4, cell5, cell6, cell7, cell8, cell9;
        XSSFRow row1;
        switch (templateNo) {
            case 1:
                cell.setCellValue("银行卡批量打款模板(单个批次文件最大支持2000条订单，证件类型 1 身份证  2 港澳台通行证 3 护照  4 军官证)");

                row1 = sheet.createRow(1);

                cell1 = row1.createCell(0);
                cell1.setCellValue("姓名（必填）");

                cell2 = row1.createCell(1);
                cell2.setCellValue("银行卡号（必填）");

                cell3 = row1.createCell(2);
                cell3.setCellValue("银行卡验证(选填)");

                cell4 = row1.createCell(3);
                cell4.setCellValue("身份证号（必填）");

                cell5 = row1.createCell(4);
                cell5.setCellValue("手机号（必填）");

                cell6 = row1.createCell(5);
                cell6.setCellValue("金额（必填）");

                cell7 = row1.createCell(6);
                cell7.setCellValue("所属银行（选填）");

                cell8 = row1.createCell(7);
                cell8.setCellValue("证件类型（必填）");

                cell9 = row1.createCell(8);
                cell9.setCellValue("备注（选填）");
                break;
            case 2:
                cell.setCellValue("支付宝批量打款模板(单个批次文件最大支持2000条订单)");

                row1 = sheet.createRow(1);

                cell1 = row1.createCell(0);
                cell1.setCellValue("姓名（必填）");

                cell2 = row1.createCell(1);
                cell2.setCellValue("金额（必填）");

                cell3 = row1.createCell(2);
                cell3.setCellValue("支付宝账号（必填）");

                cell4 = row1.createCell(3);
                cell4.setCellValue("手机号（必填）");

                cell5 = row1.createCell(4);
                cell5.setCellValue("身份证号（必填）");

                cell6 = row1.createCell(5);
                cell6.setCellValue("证件类型（必填）");

                cell7 = row1.createCell(6);
                cell7.setCellValue("备注（选填）");
                break;
        }
        return workbook;
    }

    public static String outPrintExcel(String path, String filename, Workbook workbook) {
        try {

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            byte[] content = os.toByteArray();
            InputStream is = new ByteArrayInputStream(content);

//            FileOutputStream output = new FileOutputStream(CommonString.EXECLPATH + "/" + name + ".xlsx");
//            workbook.write(output);
//            output.flush();
//
//            String uploadPath = "/splitOrder/" + splitOrderNo + "/" + customKey;
            boolean state = FtpTool.uploadFile(path, filename, is);
            if (state) {
                logger.info("导出成功 文件名:{}.xlax", filename);
            }
            return path + filename;
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public static String exportSuccess(SplitSuccessOrder successOrder, String splitOrderNo, Integer templateNo) {

        XSSFWorkbook template = ExcelExportUtil.getTemplate(templateNo);
        XSSFSheet sheet = template.getSheetAt(0);
        // 填充数据
        if (successOrder.getData().size() == 0) {
            logger.info("success excel 无数据可以用于填充。{}", successOrder);
            return null;
        }
        initExcelCell(successOrder, sheet);

        String filePath = "/splitOrder/" + splitOrderNo + "/" + successOrder.getCustomKey() + "/";
        String fileName = successOrder.getCustomName() + "_" + successOrder.getCompanyName() + ".xlsx";
        return outPrintExcel(filePath, fileName, template);
    }

    private static void initExcelCell(BaseOrderInfo baseOrderInfo, XSSFSheet sheet) {
        List<UserCommission> data = baseOrderInfo.getData();
        for (int i = 2; i < data.size() + 2; i++) {
            UserCommission commission = data.get(i - 2);
            XSSFRow row = sheet.createRow(i);
            setRowValue(commission, row);
        }
    }

    public static String exportFail(SplitFailOrder failOrder, String splitOrderNo, Integer templateNo) {
        XSSFWorkbook template = ExcelExportUtil.getTemplate(templateNo);
        XSSFSheet sheet = template.getSheetAt(0);
        // 填充数据
        if (failOrder.getData().size() == 0) {
            logger.info("fail excel 无数据可以用于填充。{}", failOrder);
            return null;
        }
        initExcelCell(failOrder, sheet);
        String filePath = "/splitOrder/" + splitOrderNo + "/" + failOrder.getCustomKey() + "/";
        String fileName = "未拆单失败文件.xlsx";
        return outPrintExcel(filePath, fileName, template);
    }

    public static String exportlave(SplitLaveOrder laveOrder, String splitOrderNo, Integer templateNo) {
        XSSFWorkbook template = ExcelExportUtil.getTemplate(templateNo);
        XSSFSheet sheet = template.getSheetAt(0);
        // 填充数据
        if (laveOrder.getData().size() == 0) {
            logger.info("lave excel 无数据可以用于填充。{}", laveOrder);
            return null;
        }
        initExcelCell(laveOrder, sheet);
        String filePath = "/splitOrder/" + splitOrderNo + "/" + laveOrder.getCustomKey() + "/";
        String fileName = "拆单部分失败文件.xlsx";
        return outPrintExcel(filePath, fileName, template);
    }

    private static void setRowValue(UserCommission commission, XSSFRow row) {
        XSSFCell cell1, cell2, cell3, cell4, cell5, cell6, cell7, cell8, cell9;
        switch (commission.getPayType()) {
            case 2:
                cell1 = row.createCell(0);
                cell1.setCellValue(commission.getUserName());

                cell2 = row.createCell(1);
                cell2.setCellValue(commission.getAmount());

                cell3 = row.createCell(2);
                cell3.setCellValue(commission.getAccount());

                cell4 = row.createCell(3);
                cell4.setCellValue(commission.getPhoneNo());

                cell5 = row.createCell(4);
                cell5.setCellValue(commission.getCertId());

                cell6 = row.createCell(5);
                if (CertType.codeOfDefault(commission.getDocumentType()) != null) {
                    cell6.setCellValue(CertType.codeOfDefault(commission.getDocumentType()).getDesc());
                }

                cell7 = row.createCell(6);
                cell7.setCellValue(commission.getRemark());
                break;
            case 4:
                cell1 = row.createCell(0);
                cell1.setCellValue(commission.getUserName());

                cell2 = row.createCell(1);
                cell2.setCellValue(commission.getAccount());

                cell3 = row.createCell(2);
                cell3.setCellValue("");

                cell4 = row.createCell(3);
                cell4.setCellValue(commission.getCertId());

                cell5 = row.createCell(4);
                cell5.setCellValue(commission.getPhoneNo());

                cell6 = row.createCell(5);
                cell6.setCellValue(commission.getAmount());

                cell7 = row.createCell(6);
                cell7.setCellValue("");

                cell8 = row.createCell(7);
                if (CertType.codeOfDefault(commission.getDocumentType()) != null) {
                    cell8.setCellValue(CertType.codeOfDefault(commission.getDocumentType()).getDesc());
                }

                cell9 = row.createCell(8);
                cell9.setCellValue(commission.getRemark());
                break;
        }

    }

}
