package com.jrmf.domain;

import java.io.Serializable;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/10/26 9:22
 * @description:
 */
@Data
public class ApiRequestData implements Serializable {


  private int id;

  /**
  * @Description 请求ID
  **/
  private String requestId;

  /**
   * @Description 请求数据
   **/
  private String requestBody;


  /**
   * @Description 数据来源
   **/
  private int source;

}
