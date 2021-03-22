package com.jrmf.domain.dto;

import lombok.Data;

@Data
public class YuncrPushContractDTO {
  /**
   * 个体户编号或自然人编号
   */
  private String firmId;

  /**
   * 项目流水号
   */
  private String bidPlatsrl;

  /**
   * 合同文件类型
   */
  private String fileType;

  /**
   * 合同文件名称
   */
  private String fileName;

  /**
   * 合同附件
   */
  private String fileBase;
}
