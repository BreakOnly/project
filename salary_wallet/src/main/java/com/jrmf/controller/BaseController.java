package com.jrmf.controller;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.LoginRole;
import com.jrmf.domain.*;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Company;
import com.jrmf.domain.Parameter;
import com.jrmf.domain.SMSChannelConfig;
import com.jrmf.domain.User;
import com.jrmf.service.CompanyService;
import com.jrmf.service.CustomInfoService;
import com.jrmf.service.ParameterService;
import com.jrmf.splitorder.domain.ReturnCode;
import com.jrmf.splitorder.service.CustomService;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.service.CommonRetCodes;
import com.jrmf.utils.ClientUtils;
import com.jrmf.utils.MapUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.sms.channel.SMSChannel;
import com.jrmf.utils.sms.channel.SMSChannelFactory;
import com.jrmf.utils.threadpool.ThreadUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import org.springframework.util.ObjectUtils;

/**
 * filename：com.jrmf.controller.BaseController.java
 *
 * @author: zhangyong
 * @time: 2013-11-23下午2:52:47
 */
@Controller
public class BaseController {

  @Autowired
  protected CustomInfoService customInfoService;
  @Autowired
  protected ParameterService parameterService;
  @Autowired
  protected CustomService customService;
  @Autowired
  protected CompanyService companyService;


  public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static Logger logger = LoggerFactory.getLogger(BaseController.class);
  public final static String PROCESS = "process";

  protected Map<String, Object> retModel(int respstat, Map<String, Object> result) {
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    logger.info("返回信息" + result);
    return result;
  }

  protected Map<String, Object> retModelMsg(int respstat, String msg, Map<String, Object> model) {
    model.put(RespCode.RESP_STAT, respstat);
    model.put(RespCode.RESP_MSG, msg);
    return model;
  }

  protected <T> List<List<T>> averageAssign(List<T> source, int n) {
    List<List<T>> result = new ArrayList<List<T>>();
    int remaider = source.size() % n; // (先计算出余数)
    int number = source.size() / n; // 然后是商
    int offset = 0;// 偏移量
    for (int i = 0; i < n; i++) {
      List<T> value = null;
      if (remaider > 0) {
        value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
        remaider--;
        offset++;
      } else {
        value = source.subList(i * number + offset, (i + 1) * number + offset);
      }
      result.add(value);
    }
    return result;
  }


  protected int getPageSize(String pageSize) {
    return StringUtil.isNumber(pageSize) ? Integer.parseInt(pageSize) : 10;
  }

  private int getPage(String pageNo) {
    return (StringUtil.isNumber(pageNo) && (Integer.parseInt(pageNo) > 0)) ? Integer
        .parseInt(pageNo) : 1;
  }

  protected int getFirst(String pageNo, String pageSize) {
    return (getPage(pageNo) - 1) * getPageSize(pageSize);
  }


  /**
   * 返回成功结果
   *
   * @param obj
   * @return
   */
  public Map<String, Object> returnSuccess(Object... obj) {
    int state = RespCode.success;
    Map<String, Object> model = new HashMap<>(4);
    model.put(RespCode.RESP_STAT, state);
    if (obj != null) {
      for (int i = 0; i < obj.length; i++) {
        if (i > 0) {
          model.put("attachment_" + i, obj[i]);
        } else {
          model.put(RespCode.RESULT, obj[i]);
        }
      }
    }
    logger.info("model{}", model);
    return model;
  }

  /**
   * 返回失败结果
   *
   * @param state
   * @param message
   * @return
   */
  public Map<String, Object> returnFail(int state, String message) {
    Map<String, Object> model = new HashMap<>(4);
    model.put(RespCode.RESP_STAT, state);
    if (StringUtil.isEmpty(message)) {
      message = "error";
    }
    model.put(RespCode.RESP_MSG, message);
    logger.info("model{}", model);
    return model;
  }

  /**
   * 发短信
   *
   * @param request
   * @param paramFlag
   * @param code
   * @param content
   */
  public void sendCode(HttpServletRequest request, String paramFlag, String code,
      String[] mobilesArray, String content, String signName, String templateCode,
      String templateParam) {
    Parameter param = new Parameter();
    try {
      String mobiles = MapUtil.strArrayToStr(mobilesArray);
      param.setParamName(mobiles);
      param.setParamValue(code);
      param.setFromip(ClientUtils.getClientip(request));
      param.setParamStatus(1);
      param.setParamDate(new Date());
      param.setParamFlag(paramFlag);
      param.setIsVoice(0);
      parameterService.saveParameter(param);
      SMSChannelConfig channelConfig = companyService.getSmsConfig();
      SMSChannel smsChannel = SMSChannelFactory.createChannel(channelConfig);
      if (content != null) {
        ThreadUtil.pdfThreadPool.execute(new Thread(() -> {
          boolean flag = smsChannel
              .sendSMS(mobilesArray, content, signName, templateCode, templateParam);
          if (!flag) {
            throw new APIDockingException(CommonRetCodes.SEND_SMS_ERROR.getCode(),
                CommonRetCodes.UNEXPECT_ERROR.getDesc());
          }
        }));
      }
    } catch (Exception e) {
      logger.info("短信发送异常" + e);
      throw new APIDockingException(CommonRetCodes.SEND_SMS_ERROR.getCode(),
          CommonRetCodes.UNEXPECT_ERROR.getDesc());
    }
  }

  /**
   * 发短信
   */
  public void sendContent(String[] mobilesArray, String content, String signName,
      String templateCode, String templateParam) {
    Parameter param = new Parameter();
    try {
      String mobiles = MapUtil.strArrayToStr(mobilesArray);
      param.setParamName(mobiles);
      param.setParamValue(content);
      param.setFromip(ClientUtils.getLocalIP());
      param.setParamStatus(1);
      param.setParamDate(new Date());
      param.setParamFlag("smsContent");
      param.setIsVoice(0);
      parameterService.saveParameter(param);
      SMSChannelConfig channelConfig = companyService.getSmsConfig();
      SMSChannel smsChannel = SMSChannelFactory.createChannel(channelConfig);
      if (content != null) {
        ThreadUtil.pdfThreadPool.execute(new Thread(() -> {
          boolean flag = smsChannel
              .sendSMS(mobilesArray, content, signName, templateCode, templateParam);
          if (!flag) {
            throw new APIDockingException(CommonRetCodes.SEND_SMS_ERROR.getCode(),
                CommonRetCodes.UNEXPECT_ERROR.getDesc());
          }
        }));
      }
    } catch (Exception e) {
      logger.info("短信发送异常" + e);
      throw new APIDockingException(CommonRetCodes.SEND_SMS_ERROR.getCode(),
          CommonRetCodes.UNEXPECT_ERROR.getDesc());
    }
  }

  /**
   * 使用 Map按key进行排序
   *
   * @param dataMap
   * @return
   */
  public Map<String, Object> sortMapByKey(Map<String, Object> dataMap) {
    if (dataMap == null || dataMap.isEmpty()) {
      return null;
    }
    Map<String, Object> sortMap = new TreeMap<>(new MapKeyComparator());
    sortMap.putAll(dataMap);
    return sortMap;
  }

  public Map<String, Object> getReturnMap(ReturnCode returnCode, Object... objects) {
    Map<String, Object> result = new HashMap<>();
    result.put("state", returnCode.getState());
    result.put("msg", returnCode.getMsg());
    if (objects != null && objects.length > 0) {
      for (int i = 0; i < objects.length; i++) {
        result.put("attachment_" + i, objects[i]);
      }
    }
    return result;
  }

  public boolean isRootAdmin(ChannelCustom custom) {
    if (CommonString.ROOT.equals(custom.getCustomkey())) {
      return true;
    }
    if (CustomType.ROOT.getCode() == custom.getCustomType() && CommonString.ROOT
        .equals(custom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == custom
        .getLoginRole()) {
      return true;
    }
    return false;
  }

  public boolean isMFKJAccount(ChannelCustom custom) {
    if (CommonString.ROOT.equals(custom.getCustomkey())) {
      return true;
    }
    if (CustomType.ROOT.getCode() == custom.getCustomType() && CommonString.ROOT
        .equals(custom.getMasterCustom())) {
      return true;
    }
    return false;
  }

  public boolean isCompany(ChannelCustom custom) {
    if (CustomType.ROOT.getCode() == custom.getCustomType() && !StringUtil
        .isEmpty(custom.getMasterCustom())) {
      ChannelCustom masterCustom = customService.getCustomByCustomkey(custom.getMasterCustom());
      return CustomType.COMPANY.getCode() == masterCustom.getCustomType();
    }
    return CustomType.COMPANY.getCode() == custom.getCustomType();
  }

  public boolean isForwardCompany(ChannelCustom custom) {
    if (CustomType.ROOT.getCode() == custom.getCustomType() && !StringUtil.isEmpty(custom.getMasterCustom())) {
      ChannelCustom masterCustom = customService.getCustomByCustomkey(custom.getMasterCustom());
      if (CustomType.COMPANY.getCode() == masterCustom.getCustomType()) {
        Company companyByUserId = companyService.getCompanyByUserId(Integer.parseInt(masterCustom.getCustomkey()));
        return 1 == companyByUserId.getCompanyType();
      }
    }
    if (CustomType.COMPANY.getCode() == custom.getCustomType()) {
      Company companyByUserId = companyService.getCompanyByUserId(Integer.parseInt(custom.getCustomkey()));
      return 1 == companyByUserId.getCompanyType();
    }
    return false;
  }
  public boolean isRootAdmin(HttpServletRequest request) {
    ChannelCustom custom = (ChannelCustom) request.getSession().getAttribute("customLogin");
    return isRootAdmin(custom);
  }
  /**
  * @Description 获取转包服务公司的key 如果不是转包返回 null
  **/
  public Integer returnSubcontractCompanyId(HttpServletRequest request) {
    ChannelCustom custom = (ChannelCustom) request.getSession().getAttribute("customLogin");
    try {
     /**
     * @Description 机构登陆账户
     **/
    if (CustomType.ROOT.getCode() == custom.getCustomType() && !StringUtil
        .isEmpty(custom.getMasterCustom())) {
      ChannelCustom masterCustom = customService.getCustomByCustomkey(custom.getMasterCustom());
      if(CustomType.COMPANY.getCode() == masterCustom.getCustomType()) {
       int userId =  Integer.parseInt(masterCustom.getCustomkey());
        Company company = companyService.getCompanyByUserId(userId);
        if(company.getCompanyType() == 1) {
          return company.getUserId();
        }
      }
    }
    /**
    * @Description 服务公司
    **/
    if(CustomType.COMPANY.getCode() == custom.getCustomType()){
      int userId =  Integer.parseInt(custom.getCustomkey());
      Company company = companyService.getCompanyByUserId(userId);
      if(company.getCompanyType() == 1) {
        return company.getUserId();
      }
    }
    }catch (Exception e){
      logger.info("判断转包服务公司类型异常"+e);
    }
    return null;
  }

  public boolean isPlatformAdminAccount(ChannelCustom custom) {
    if (CustomType.ROOT.getCode() == custom.getCustomType()
            && !StringUtil.isEmpty(custom.getMasterCustom())) {
      ChannelCustom masterCustom = customService.getCustomByCustomkey(custom.getMasterCustom());
      return CustomType.PLATFORM.getCode() == masterCustom.getCustomType()
              && LoginRole.ADMIN_ACCOUNT.getCode() == masterCustom.getLoginRole();
    }
    return CustomType.PLATFORM.getCode() == custom.getCustomType();
  }

  public boolean isPlatformAccount(ChannelCustom custom) {
    if (CustomType.ROOT.getCode() == custom.getCustomType() && !StringUtil
        .isEmpty(custom.getMasterCustom())) {
      ChannelCustom masterCustom = customService.getCustomByCustomkey(custom.getMasterCustom());
      return CustomType.PLATFORM.getCode() == masterCustom.getCustomType();
    }
    return CustomType.PLATFORM.getCode() == custom.getCustomType();
  }

  public boolean isAdmin(ChannelCustom custom){
    return LoginRole.ADMIN_ACCOUNT.getCode() == custom.getLoginRole();
  }

  /**
   * 获取excel文件中的 用户信息
   *
   * @param sheet
   * @param users
   */
  public void getUserList(Sheet sheet, List<User> users) {
    for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {
      User user = new User();
      XSSFRow row = (XSSFRow) sheet.getRow(j);
      if (row == null) {
        continue;
      }
      String userName = StringUtil.getXSSFCell(row.getCell(0));
      String documentType = StringUtil.getXSSFCell(row.getCell(1));
      String certId = StringUtil.getXSSFCell(row.getCell(2));
//      String userNo = StringUtil.getXSSFCell(row.getCell(3));
      String mobile = StringUtil.getXSSFCell(row.getCell(4));

      //增加中英文判断,替换掉中文中间的空格
      if (StringUtil.isChinese(userName.trim())) {
        user.setUserName(StringUtil.replaceSpecialStr(userName));
      } else {
        user.setUserName(userName.trim());
      }
      user.setDocumentType(documentType.trim());
      user.setCertId(certId.trim());
      user.setUserNo(certId.trim());
      if (!StringUtil.isEmpty(mobile)) {
        if (StringUtil.isMobileNO(mobile.trim())) {
          user.setMobilePhone(mobile.trim());
        }
      }
      users.add(user);
    }
  }

  /**
   * @Description 检查所属平台
   **/
  public Integer checkCustom(ChannelCustom channelCustom) {

    Integer platformId = null;
    if (ObjectUtils.isEmpty(channelCustom)) {

      return null;
    }
    /**
     * @Description 平台类型账号
     **/
    if (channelCustom.getCustomType() == CustomType.PLATFORM.getCode()) {
      platformId = channelCustom.getId();
    }
    /**
     * @Description 机构账号
     **/
    if (channelCustom.getCustomType() == CustomType.ROOT.getCode()) {

      /**
       * @Description 获取主商户信息
       **/
      ChannelCustom master = customService.getCustomByCustomkey(channelCustom.getMasterCustom());

      if (ObjectUtils.isEmpty(master)) {

        return null;
      }

      if (master.getCustomType() == CustomType.PLATFORM.getCode()) {

        platformId = master.getId();
      }
    }

    if ( ObjectUtils.isEmpty(platformId) || platformId < 4) {
      return null;
    }
    return platformId;
  }

}

class MapKeyComparator implements Comparator<String> {

  @Override
  public int compare(String str1, String str2) {
    Integer parseInt = Integer.parseInt(str1);
    Integer parseInt2 = Integer.parseInt(str2);
    return parseInt.compareTo(parseInt2);
  }
}
