package com.jrmf.payment.mybankapi.common.util;


import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.Map;


/**
 * @author guyihui Action 中用到的一些帮助方法
 */
public class ActionUtil {

  public static final String CODE_SUCCESS = "S";
  public static final String CODE_FAIL = "F";
  public static final String CODE = "code";
  public static final String MESSAGE = "msg";

  public static String buildMsg(String code, String msg) {
    Map<String, String> message = new HashMap<String, String>();
    message.put(CODE, code);
    message.put(MESSAGE, msg);
    return JSONObject.toJSONString(message);
  }

  public static String buildMsg(String code, String... msgs) {
    Map<String, String> message = new HashMap<String, String>();
    message.put(CODE, code);
    for (int i = 0; i < msgs.length; i++) {
      message.put(MESSAGE + i, msgs[i]);
    }

    return JSONObject.toJSONString(message);
  }
}
