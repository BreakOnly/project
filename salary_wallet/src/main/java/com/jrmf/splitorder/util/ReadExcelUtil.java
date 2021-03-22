package com.jrmf.splitorder.util;

import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CertType;
import com.jrmf.domain.UserCommission;
import com.jrmf.splitorder.controller.SplitOrderController;
import com.jrmf.splitorder.domain.ReturnCode;
import com.jrmf.splitorder.domain.SplitStatus;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ReadExcelUtil extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SplitOrderController.class);

    public Map<String, Object> readExcelData(Workbook workbook, String customKey, Integer templateNo) {
        Sheet sheet = workbook.getSheetAt(0);
        int num = 0;
        for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {
            XSSFRow row = (XSSFRow) sheet.getRow(j);
            if (row == null || row.getCell(0) == null || row.getCell(0).getCellType() == Cell.CELL_TYPE_BLANK) {
                continue;
            }
            ++num;
        }
        logger.info("excel导入数据 ======> {} 条", num);
        if (num > 2000 || num < 0) {
            return getReturnMap(ReturnCode.DATA_OVERFLOW);
        }
        List<UserCommission> data = getExcelData(sheet, customKey, templateNo);
        return getReturnMap(ReturnCode.SUCCESS, data);
    }

    private List<UserCommission> getExcelData(Sheet sheet, String customKey, Integer templateNo) {
        Set<String> certIds = new HashSet<>();
        List<UserCommission> data = new ArrayList<>();
        String userName, accountNo, certId, phoneNo, amount, documentType, remark;
        Integer payType;
        for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {// 获取每行
            XSSFRow row = (XSSFRow) sheet.getRow(j);
            if (row == null || row.getCell(0) == null || row.getCell(0).getCellType() == Cell.CELL_TYPE_BLANK) {
                continue;
            }
            if (templateNo.equals(1)) {
                userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));// 收款人真实姓名(必要)
                accountNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(1)));// 收款人银行卡号
                certId = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(3)));// 身份证号
                phoneNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(4)));// 手机号
                amount = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(5)));// 金额(必要)
                documentType = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(7)));// 证件类型
                remark = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(8)));// 备注
                payType = 4;
            } else {
                userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));// 收款人真实姓名(必要)
                amount = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(1)));// 金额(必要)
                accountNo = "";
                if (row.getCell(2) != null) {
                    accountNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(2)));
                }
                phoneNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(3)));// 手机号
                certId = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(4)));// 身份证号
                documentType = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(5)));// 证件类型
                remark = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(6)));// 备注
                payType = 2;
            }
            UserCommission userCommission = new UserCommission();
            userCommission.setUserName(userName);
            userCommission.setAccount(accountNo);
            userCommission.setPhoneNo(phoneNo);
            userCommission.setCertId(certId);
            userCommission.setRemark(remark);
            if (!certIds.add(certId)) {
                userCommission.setStatus(SplitStatus.FAIL.getState());
            }
            if (!StringUtil.isEmpty(amount)) {
                userCommission.setAmount(amount);
            } else {
                userCommission.setStatus(SplitStatus.AMOUNT_NOT.getState());
            }
            if (!StringUtil.isEmpty(documentType)) {
                userCommission.setDocumentType(CertType.descOfDefault(documentType).getCode());
            }
            userCommission.setOriginalId(customKey);
//            userCommission.setCompanyId(companyId);
//            userCommission.setMerchantId(merchantId);
            userCommission.setPayType(payType);
            data.add(userCommission);
            logger.info("第 {} 行读取到数据 ======> data={}", j, userCommission);
        }
        certIds.clear();
        return data;
    }

}
