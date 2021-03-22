package com.jrmf.utils.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class InputAliBatchData extends InputBatchData {

  @ExcelProperty(index = 0)
  private String userName;
  @ExcelProperty(index = 1)
  private String amount;
  @ExcelProperty(index = 2)
  private String aliAccount;
  @ExcelProperty(index = 3)
  private String phoneNo;
  @ExcelProperty(index = 4)
  private String certId;
  @ExcelProperty(index = 5)
  private String certType;
  @ExcelProperty(index = 6)
  private String remark;


  @Override
  public String toValueString() {
    return userName + amount + aliAccount + phoneNo + certId + certType + remark;
  }
}
