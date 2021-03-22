package com.jrmf.domain;

import java.io.Serializable;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2020/10/28 15:30
 * @description:  未签约用户短信提醒
 */
@Data
public class SendSmsHistoryRecord implements Serializable {

  private int id;

  /**
  * @Description '接受方用户信息表ID'
  **/
  private String receiveUserId;
  /**
   * @Description '接收方手机号'
   **/
  private String receivePhone;

  /**
   * @Description '发送方'
   **/
  private String customKey;

  /**
   * @Description '发送类型 1:未签约用户短信提醒'
   **/
  private String sendType;

  /**
   * @Description '发送内容'
   **/
  private String sendContent;

  /**
   * @Description '是否发送成功'
   **/
  private String isSuccess;
}
