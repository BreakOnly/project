package com.jrmf.taxsettlement.api.service.yuncr;

import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.service.CommonRetCodes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * @author: YJY
 * @date: 2020/11/6 11:30
 * @description:
 */
public class CheckData {

  //3M
  private static final int videoFileSize = 3145728;
  //500k
  private static final int otherFileSize = 512000;

  private static final String videoType = "3";

  private static final String code = "600";

  private static final String msg = "上传文件大小超过系统限制";

  public static void checkNull(String parameter) {

    if (StringUtils.isEmpty(parameter)) {
      throw new APIDockingException(CommonRetCodes.INVAILD_PARAMS.getCode(),
          CommonRetCodes.INVAILD_PARAMS.getDesc());
    }
  }


  public static void checkFile(String base64, String type) {

    if ((videoType.equals(type) && base64FileSize(base64) > videoFileSize) ||
        (!videoType.equals(type) && base64FileSize(base64) > otherFileSize)) {
      throw new APIDockingException(code, msg);
    }

  }

  /**
   * 验证是否是URL
   *
   * @param url
   * @return
   */
  public static void verifyUrl(String url) {

    try {

      String regEx = "[a-zA-z]+://[^\\s]*";
      Pattern pattern = Pattern.compile(regEx);
      Matcher matcher = pattern.matcher(url);
      if (!matcher.matches()) {
        throw new APIDockingException(CommonRetCodes.INVAILD_PARAMS.getCode(),
            CommonRetCodes.INVAILD_PARAMS.getDesc());
      }
    } catch (Exception e) {
      throw new APIDockingException(CommonRetCodes.INVAILD_PARAMS.getCode(),
          CommonRetCodes.INVAILD_PARAMS.getDesc());
    }
  }


  /**
   * 精确计算base64字符串文件大小（单位：B）
   *
   * @param base64String
   * @return
   */
  public static double base64FileSize(String base64String) {
    /**检测是否含有base64,文件头)*/
    if (base64String.lastIndexOf(",") > 0) {
      base64String = base64String.substring(base64String.lastIndexOf(",") + 1);
    }
    /** 获取base64字符串长度(不含data:audio/wav;base64,文件头) */
    int size0 = base64String.length();
    /** 获取字符串的尾巴的最后10个字符，用于判断尾巴是否有等号，正常生成的base64文件'等号'不会超过4个 */
    String tail = base64String.substring(size0 - 10);
    /** 找到等号，把等号也去掉,(等号其实是空的意思,不能算在文件大小里面) */
    int equalIndex = tail.indexOf("=");
    if (equalIndex > 0) {
      size0 = size0 - (10 - equalIndex);
    }
    /** 计算后得到的文件流大小，单位为字节 */
    return size0 - ((double) size0 / 8) * 2;
  }


}
