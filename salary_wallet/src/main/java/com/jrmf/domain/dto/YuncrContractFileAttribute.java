package com.jrmf.domain.dto;

import lombok.Data;

@Data
public class YuncrContractFileAttribute {

  private String filePath;

  private String fileName;

  private String fileType;

  private String fileUrl;

  @Override
  public String toString() {
    return "YuncrContractFileAttribute{" +
        "filePath='" + filePath + '\'' +
        ", fileName='" + fileName + '\'' +
        ", fileType='" + fileType + '\'' +
        '}';
  }
}
