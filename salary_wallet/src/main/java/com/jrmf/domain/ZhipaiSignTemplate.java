package com.jrmf.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZhipaiSignTemplate {

  private String userName;
  private String taxpayerId;
  private String contacts;
  private String contactInfo;
  private String address;
  private String date;
  private String taskName;
  private String taskDesc;
  private String invoiceMoney;
}
