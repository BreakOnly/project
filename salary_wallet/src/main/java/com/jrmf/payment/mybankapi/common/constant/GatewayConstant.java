/**
 *
 */
package com.jrmf.payment.mybankapi.common.constant;

/**
 * <p>注释</p>
 * @author fjl
 * @version $Id: GatewayConstant.java, v 0.1 2013-11-13 下午4:01:07 fjl Exp $
 */
public interface GatewayConstant {

  /*
   * 连接符
   */
  String and = "&";
  String eq = "=";
  String empty = "";
  /*
   * 字符集
   */
  String charset_iso_latin = "ISO-8859-1";
  String charset_utf_8 = "UTF-8";
  String charset_gbk = "GBK";
  String charset_gb2312 = "GB2312";

  String EXPANDED_NAME_TMP = ".TMP";

  /**
   * 与配置文件中keyStoreName保持一致
   */
  String KEY_STORE_NAME = "apiKeyStore";
}
