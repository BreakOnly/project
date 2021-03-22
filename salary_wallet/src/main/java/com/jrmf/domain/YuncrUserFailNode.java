package com.jrmf.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

/**
 * @author: YJY
 * @date: 2020/12/14 15:43
 * @description:
 */
@Data
@ToString
public class YuncrUserFailNode implements Serializable {

  private int id;

  private int authenticationId;

  private String auditNode;

  private int status;

  private String remark;

}
