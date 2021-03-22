package com.jrmf.controller.constant;

/*** 
 * @Description: 通道类型
 * @Auther: wsheng
 * @Version: 1.0
 * @create 2020/8/4 13:56 
 */
public enum ChannelTypeEnum {

  AI_YUAN_GONG(1,"爱员工"),

  YI_MEI(2,"溢美");

  private final int code;
  private final String desc;

  ChannelTypeEnum(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }
}
