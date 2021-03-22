package com.jrmf.domain.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractDTO {

  private Integer errorCode;
  private String errMsg;
  private String signature;
  private String body;
  private String contractUrl;
  private String contractNo;

}
