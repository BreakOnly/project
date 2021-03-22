package com.jrmf.domain.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class YuncrPushProjectDTO implements Serializable {

  private static final long serialVersionUID = -8720375088053567518L;
  /**
   * 项目文件,base64
   */
  
  private String fileBase;

  /**
   * 项目名称
   */
  
  private String bidTitle;

  /**
   * 发票类型 1增值税票普通发票2增值税专用发票
   */
  
  private String invoiceType;

  /**
   * 开票信息
   */
  
  private String invoiceInfo;

  /**
   * 经济分类编号(2级)
   */
  
  private String categoryId;

  /**
   * 项目描述
   */
  private String loanDesc;

  /**
   * 发包方编号
   */
  
  private String firmId;

  /**
   * 项目文件类型(目前只支持PDF/xls/xlsx)
   */
  
  private String fileType;

  /**
   * 项目文件名称
   */
  
  private String fileName;
}
