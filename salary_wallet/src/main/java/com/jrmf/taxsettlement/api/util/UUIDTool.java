package com.jrmf.taxsettlement.api.util;

import java.util.UUID;

/**
 * @author: YJY
 * @date: 2020/12/1 11:33
 * @description:
 */
public class UUIDTool {


  public static String  getUUID(){

    //生成唯一id
    String id= UUID.randomUUID().toString();
    //替换uuid中的"-"
    id=id.replace("-", "");

    return id;
  }

}
