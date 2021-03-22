package com.jrmf.utils.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class InputBankBatchData extends InputBatchData {

  @ExcelProperty(index = 0)
  private String userName;
  @ExcelProperty(index = 1)
  private String bankAccount;
  @ExcelProperty(index = 2)
  private String validateBank;
  @ExcelProperty(index = 3)
  private String certId;
  @ExcelProperty(index = 4)
  private String phoneNo;
  @ExcelProperty(index = 5)
  private String amount;
  @ExcelProperty(index = 6)
  private String bankName;
  @ExcelProperty(index = 7)
  private String certType;
  @ExcelProperty(index = 8)
  private String remark;

  @Override
  public String toValueString() {
    return userName + bankAccount + validateBank + certId + phoneNo + amount + bankName + certType
        + remark;
  }
}
