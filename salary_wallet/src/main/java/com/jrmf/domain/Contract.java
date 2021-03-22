package com.jrmf.domain;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class Contract {

  private Integer id;

  private String customKey;

  private String customName;

  private Integer userId;

  private String userName;

  private String idCard;

  private Integer channelTaskId;

  private String taskName;

  private Integer ecoCateCode;

  private String ecoCateName;

  private Integer companyId;

  private String companyName;

  private Integer status;

  private String statusDesc;

  private String contractUrl;

  private MultipartFile contractFile;

  private String addUser;

  private String createTime;

  private String updateTime;

  private String fileNo;

  private String platsrl;
}
