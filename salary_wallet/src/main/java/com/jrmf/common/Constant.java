package com.jrmf.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Constant {

  public static final String COMMISSION_INVOICE_START_DAY = "2020-08-01";//实发开票开始日期
  public static final String COMMISSION_INVOICE_START_MONTH = "2020-08";//实发开票开始月份
  public static final String VARIABLE_SERVICE_RESPONSE_CODE = "code";

  public static final String VARIABLE_SERVICE_RESPONSE_MESSAGE = "msg";

  public static final String VARIABLE_SERVICE_RESPONSE_DATA = "data";

  public static final String VARIABLE_SERVICE_RESPONSE_PAGE = "page";

  public static final String PLATFORM_SERVICE_TYPE = "paltform_service_type";

  public static final int LOGIN_COOKIE_MAXAGE = 7200;

  public static final String SERVICE_RESPONSE_CODE_SUCCESS = "00000";

  public static final String ENV_DEV = "dev";
  public static final String ENV_TEST = "test";
  public static final String ENV_PROD = "prod";
  public static final String WX_LOGIN_CACHE = "wx_login_cache:";

  public static final String OPEN_USER_DEFAULT_PLATFORM = "WX";

  public static final Long TASK_SELECT_DEFAULT_PAGE_NO = 1L;
  public static final Long TASK_SELECT_DEFAULT_PAGE_SIZE = 1000L;

  public static final String USER_SERVICE_PAGE_PAGE_NO = "pageNo";
  public static final String USER_SERVICE_PAGE_PAGE_SIZE = "pageSize";
  public static final String USER_SERVICE_PAGE_TOTAL_NUM = "totalNum";
  public static final String USER_SERVICE_PAGE_TOTAL_PAGE = "totalPage";

  public static final String OPEN_USER_PLATFORM_SOURCE_WX = "WX";
  public static final String OPEN_USER_PRODUCT_SOURCE_XHF = "XHF";
  public static final Byte CHANNEL_USER_REGISTER_TYPE_WX = 1;
  public static final Byte CHANNEL_USER_REGISTER_TYPE_MOBILE = 2;
  public static final Byte CHANNEL_USER_REGISTER_TYPE_ID = 3;

  public static final String COMPANY_AGREEMENT_FILE_DOMAIN = "https://jrmf360.com/";
  public static final String COMPANY_AGREEMENT_FILE_SUFFIX = "_agreement";

  public static final Byte OPEN_USER_TYPE_PROMOTE = 1;
  public static final Byte OPEN_USER_TYPE_VISITOR = 2;
  public static final Byte OPEN_USER_TYPE_GTGSH = 3;

  public static final Map<String, String> COMPANY_AGREEMENT_MAPPING = new HashMap<>();

  /**
   * @Description 审核中
   **/
  public static final  int  IN_AUDIT = 0;
  /**
   * @Description 审核成功
   **/
  public static final  int  AUDIT_SUCCESS = 1;

  /**
   * @Description 审核失败
   **/
  public static final  int  AUDIT_FAILED = 2;

  /**
   * @Description 工商公示人员同名
   **/
  public static final  String  SAME_NAME = "工商公示人员同名";

  /**
   * @Description 未注册个体工商户
   **/
  public static final  String  UN_AUDIT = "未注册个体工商户";

  /**
   * @Description 工商户未绑定银行卡
   **/
  public static final  String  UN_BIND_BANK = "该个体工商户未绑定银行卡";

  /**
   * @Description 回调成功
   **/
  public static final  int  CALLBACK_STATUS_SUCCESS = 1;

  /**
   * @Description 回调失败
   **/
  public static final  int  CALLBACK_STATUS_FAILED = 2;


  public static final  String  CALL_BACK_SUCCESS = "SUCCESS";

  //发票费率
  public static final String INVOICE_RATE = "0.01";

  static {
    COMPANY_AGREEMENT_MAPPING.put("platform1_company1", "platform1_company1");
    COMPANY_AGREEMENT_MAPPING.put("platform1_company2", "platform1_company1");
  }

  public static final Map<String, String> LOGIN_USER = new HashMap<>();

  static {
    LOGIN_USER.put("sz1", "sz123456");
  }

  public static final List<String> LIST_CHANNEL_CUSTOM = new ArrayList();
  public static final List<Integer> LIST_COMPANY = new ArrayList();
  static {
    LIST_CHANNEL_CUSTOM.add("fF5O6113yh2a55255F9f");

    LIST_COMPANY.add(1003);
  }

  public static final String CHANNEL_CUSTOMS = new String("fF5O6113yh2a55255F9f,29954,V9986iM9Uodi2nw4U7zy,J4Ep84013ih5VQ76oGPJ,5753OqVL15f8kg29p23d,pI92yBzo15gZn693480i");
  public static final String COMPANYS = new String("1003,994,102,101,994,997,27152,29734,29733");


}
