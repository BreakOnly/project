package com.jrmf.controller.systemrole.merchant.user;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.jrmf.common.CommonString;
import com.jrmf.common.Constant;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.CertType;
import com.jrmf.controller.constant.ChannelTypeEnum;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.DocumentStep;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.controller.constant.SignStep;
import com.jrmf.controller.constant.UsersAgreementSignType;
import com.jrmf.domain.*;
import com.jrmf.service.*;
import com.jrmf.utils.*;
import com.jrmf.utils.exception.ImportException;
import com.jrmf.utils.exception.LoginException;
import com.jrmf.utils.exception.ParamErrorException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author chonglulu 用户签约管理，注意子账户的概念，根据customkey匹配
 */
@Api(tags = "用户管理")
@Slf4j
@Controller
@RequestMapping("/merchant/user/management")
public class UserSignManagementController extends BaseController {

  private static Logger logger = LoggerFactory.getLogger(UserSignManagementController.class);

  private final UserSerivce userSerivce;

  private final UsersAgreementService usersAgreementService;

  private final AgreementTemplateService agreementTemplateService;

  private final UserRelatedService userRelatedService;

  private final ChannelCustomService channelCustomService;

  private final BestSignConfig bestSignConfig;

  private final OrganizationTreeService organizationTreeService;

  private final ChannelRelatedService channelRelatedService;

  private final CallBackInfoService callBackInfoService;

  private final ChannelHistoryService channelHistoryService;
  @Autowired
  UsersAgreementSmsRemindService usersAgreementSmsRemindService;

  private final String hygCompany = "huiyonggong";
  private final String xiaohuangfengCompany = "xiaohuangfeng";

  @Value("${company.agreement.file.domain}")
  private String companyAgreementFileDomain;


  @Autowired
  public UserSignManagementController(UserSerivce userSerivce,
      UsersAgreementService usersAgreementService,
      AgreementTemplateService agreementTemplateService, UserRelatedService userRelatedService,
      ChannelCustomService channelCustomService1, BestSignConfig bestSignConfig,
      OrganizationTreeService organizationTreeService, ChannelRelatedService channelRelatedService,
      CallBackInfoService callBackInfoService, ChannelHistoryService channelHistoryService) {
    this.userSerivce = userSerivce;
    this.usersAgreementService = usersAgreementService;
    this.agreementTemplateService = agreementTemplateService;
    this.userRelatedService = userRelatedService;
    this.channelCustomService = channelCustomService1;
    this.bestSignConfig = bestSignConfig;
    this.organizationTreeService = organizationTreeService;
    this.channelRelatedService = channelRelatedService;
    this.callBackInfoService = callBackInfoService;
    this.channelHistoryService = channelHistoryService;
  }

  /**
   * 用户列表
   */
  @RequestMapping(value = "/users", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> users(@RequestParam(value = "userName", required = false) String userName,
      @RequestParam(value = "startTime", required = false) String startTime,
      @RequestParam(value = "endTime", required = false) String endTime,
      @RequestParam(value = "certId", required = false) String certId,
      @RequestParam(value = "userNo", required = false) String userNo,
      @RequestParam(required = false, defaultValue = "1") int pageNo,
      @RequestParam(required = false, defaultValue = "10") int pageSize,
      @RequestParam(value = "customkey") String customkey) {
    Map<String, Object> paramMap = new HashMap<>(16);
    paramMap.put("userName", userName);
    paramMap.put("certId", certId);
    paramMap.put("userNo", userNo);
    paramMap.put("startTime", startTime);
    paramMap.put("endTime", endTime);
    paramMap.put("originalId", customkey);
    PageHelper.startPage(pageNo, pageSize);
    List<User> userList = userSerivce.getUserForMerchantByParams(paramMap);
    PageInfo<User> pageInfo = new PageInfo(userList);
    Map<String, Object> resultMap = new HashMap<>(4);

    resultMap.put("total", pageInfo.getTotal());
    resultMap.put("userList", pageInfo.getList());
    return returnSuccess(resultMap);

  }

  /**
   * 用户导出
   */
  @RequestMapping(value = "/info/export")
  public @ResponseBody
  void infoExport(HttpServletResponse response,
      @RequestParam(value = "userName", required = false) String userName,
      @RequestParam(value = "startTime", required = false) String startTime,
      @RequestParam(value = "endTime", required = false) String endTime,
      @RequestParam(value = "certId", required = false) String certId,
      @RequestParam(value = "userNo", required = false) String userNo,
      @RequestParam(value = "customkey") String customkey) throws Exception {
    Map<String, Object> paramMap = new HashMap<>(12);
    paramMap.put("userName", userName);
    paramMap.put("certId", certId);
    paramMap.put("userNo", userNo);
    paramMap.put("startTime", startTime);
    paramMap.put("endTime", endTime);
    paramMap.put("originalId", customkey);
    List<User> userList = userSerivce.getUserForMerchantByParams(paramMap);
    if (userList.isEmpty()) {
      return;
    } 

    String today = DateUtils.getNowDay();
    ArrayList<String> dataStr = new ArrayList<>();
    for (User user : userList) {
      StringBuilder strBuff = new StringBuilder();
      int code = Integer.parseInt(user.getDocumentType());
      String desc = CertType.codeOf(code).getDesc();
      strBuff.append(user.getUserNo() == null ? "" : user.getUserNo()).append(",")
          .append(user.getUserName() == null ? "" : user.getUserName()).append(",")
          .append(desc == null ? "" : desc).append(",")
          .append(user.getCertId() == null ? "" : user.getCertId()).append(",")
          .append(user.getCreateTime() == null ? "" : user.getCreateTime()).append(",");
      dataStr.add(strBuff.toString());
    }
    ArrayList<String> fieldName = new ArrayList<>();
    fieldName.add("商户用户编号");
    fieldName.add("用户名称");
    fieldName.add("证件类型");
    fieldName.add("证件号");
    fieldName.add("用户创建时间");
    String filename = today + "用户信息";
    ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
  }

  /**
   * 用户信息导入
   *
   * @return 结果集  hasError  true  有错误  返回 errorList
   */
  @RequestMapping(value = "/info/import")
  public @ResponseBody
  Map<String, Object> infoImport(@RequestParam("file") MultipartFile file,
      @RequestParam(value = "customkey") String customkey) throws IOException {
    HashMap<String, Object> paramMap = new HashMap<>(3);
    InputStream is;
    Workbook workbook = null;
    ByteArrayOutputStream bytesOut;
    if (file != null) {
      is = file.getInputStream();

      int readLen;
      byte[] byteBuffer = new byte[1024];
      bytesOut = new ByteArrayOutputStream();

      while ((readLen = is.read(byteBuffer)) > -1) {
        bytesOut.write(byteBuffer, 0, readLen);
      }
      byte[] fileData = bytesOut.toByteArray();

      try {
        workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData));
      } catch (Exception ex) {
        workbook = new HSSFWorkbook(new ByteArrayInputStream(fileData));
      }
    }
    int num = 0;
    Sheet sheet = workbook.getSheetAt(0);
    for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {
      XSSFRow row = (XSSFRow) sheet.getRow(j);
      if (row == null) {
        continue;
      }
      ++num;
      if (num > 2000 || num < 0) {
        throw new ImportException(RespCode.codeMaps.get(RespCode.IMPORT_NUMBER_ERROR));
      }
    }
    List<User> users = new ArrayList<>();
    getUserList(sheet, users);
    logger.info("用户信息aaa:"+users);

    paramMap.put("users", users);
    paramMap.put("customkey", customkey);
    Map<String, Object> result = userSerivce.addUserBatchByExcel(paramMap);
    return returnSuccess(result);
  }

  /**
   * 删除用户 删除规则：用户没有签约成功的，才能删
   *
   * @return 结果集  hasError  true  有错误  返回 errorList
   */
  @RequestMapping(value = "/info/delete", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> infoDelete(@RequestParam("ids") String ids,
      @RequestParam(value = "customkey") String customkey) {
    if (StringUtil.isEmpty(ids)) {
      throw new ParamErrorException(RespCode.codeMaps.get(RespCode.ParamNotFound));
    }
    Map<String, Object> map = new HashMap<>(8);
    map.put("signStatus", UsersAgreementSignType.SIGN_PROCESSING.getCode());
    map.put("userIds", ids);
    map.put("originalId", customkey);
    List<UsersAgreement> agreements = usersAgreementService.getUsersAgreementsByParams(map);
    if (!agreements.isEmpty()) {
      StringBuilder errorMsg = new StringBuilder();
      for (UsersAgreement agreement : agreements) {
        errorMsg.append(agreement.getUserName()).append(":").append(agreement.getCertId())
            .append(";").toString();
      }
      int code = RespCode.error134;
      return returnFail(code, errorMsg.toString() + "用户签约处理中，不允许删除");
    }
    String[] split = ids.split(",");
    List<String> stringList = Arrays.asList(split);

    Map<String, Object> paramMap = new HashMap<>(3);
    paramMap.put("customkey", customkey);
    paramMap.put("ids", stringList);
    userSerivce.deleteUnSignUser(paramMap);
    return returnSuccess(null);
  }

  /**
   * 修改用户
   */
  @RequestMapping(value = "/info/update", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> infoUpdate(@RequestParam("userName") String userName,
      @RequestParam("userNo") String userNo,
      @RequestParam("mobileNo") String mobileNo,
      @RequestParam("customkey") String customkey,
      @RequestParam("id") String id) {
    if (!StringUtil.isNumeric(id)) {
      throw new ParamErrorException(RespCode.codeMaps.get(RespCode.ParamNotFound));
    }
//    HashMap<String, Object> paramMap = new HashMap<>(3);
//    paramMap.put("originalId", customkey);
//    List<UserRelated> userRelatedList = userRelatedService.getRelatedByParam(paramMap);
//    boolean updateInfo;
//    if (userRelatedList.isEmpty()) {
//      updateInfo = true;
//    } else {
//      if (userRelatedList.size() > 1) {
//        updateInfo = false;
//      } else {
//        int userId = userRelatedList.get(0).getUserId();
//        updateInfo = (userId + "").equals(id);
//      }
//    }

//    if (updateInfo) {
//
//    } else {
//      int code = RespCode.USERNO_ALREADY_EXIST;
//      return returnFail(0, RespCode.codeMaps.get(code));
//    }

    User user = userSerivce.getUserByUserId(Integer.parseInt(id));
    if (!user.getUserName().equals(userName)) {
      user.setCheckTruth(0);
    }
    user.setUserName(userName);
    if (!StringUtil.isEmpty(userNo)){
      user.setUserNo(userNo);
    }
    userSerivce.updateUserInfo(user);

    UserRelated userRelated = new UserRelated();
    userRelated.setUserNo(userNo);
    userRelated.setUserId(Integer.parseInt(id));
    userRelated.setOriginalId(customkey);
    userRelated.setMobileNo(mobileNo);
    userRelatedService.updateUserRelatedUserNo(userRelated);
    return returnSuccess(null);
  }

  /**
   * Author Nicholas-Ning Description //用户签约协议查看（商户超管通用） Date 15:16 2018/11/23 Param [request,
   * userName, certId, signDateStart, signDateEnd, userNo, companyId, signStatus, pageSize, pageNo,
   * customName] return java.util.Map<java.lang.String,java.lang.Object>
   **/
  @ApiOperation("获取用户签约协议列表")
  @RequestMapping(value = "/sign/list/user/agreements", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> listUserAgreements(HttpServletRequest request,
      @RequestParam(required = false) String userName,
      @RequestParam(required = false) String certId,
      @RequestParam(required = false) String signDateStart,
      @RequestParam(required = false) String signDateEnd,
      @RequestParam(required = false) String userNo,
      @RequestParam(required = false) String userType,
      @RequestParam(required = false) String remark,
      @RequestParam(required = false) String companyId,
      @RequestParam(required = false) String signStatus,
      @RequestParam(defaultValue = "10", required = false) int pageSize,
      int pageNo, @RequestParam(required = false) String customName) {
    Map<String, Object> result = new HashMap<>(4);
    Map<String, Object> params = new HashMap<>(15);
    String customkey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
    if (!CommonString.ROOT.equals(customkey)) {
      params.put("originalId", customkey);
    }
    ChannelCustom channelCustom = channelCustomService.getCustomByCustomkey(customkey);
    if (channelCustom.getCustomType() == CustomType.GROUP.getCode()) {
      int id = organizationTreeService.queryNodeIdByCustomKey(channelCustom.getCustomkey());
      List<String> stringList = organizationTreeService
          .queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, id);
      String originalIds = Joiner.on(",").join(stringList);
      params.put("originalId", originalIds);
    }
    if (channelCustom.getCustomType() == CustomType.COMPANY.getCode()) {
      params.put("originalId", null);
      companyId = channelCustom.getCustomkey();
    }
    params.put("userName", userName);
    params.put("customName", customName);
    params.put("certId", certId);
    params.put("signDateStart", signDateStart);
    params.put("signDateEnd", signDateEnd);
    params.put("userNo", userNo);
    params.put("companyId", companyId);
    params.put("signStatus", signStatus);
    params.put("remark", remark);
    params.put("userType", userType);
    int total = usersAgreementService.getAgreementsByParamsCount(params);
    params.put("start", (pageNo - 1) * pageSize);
    params.put("limit", pageSize);
    logger.info("用户签约协议查看 params{}", params);
    List<Map<String, Object>> agreements = usersAgreementService.getAgreementsByParams(params);
    result.put("agreements", agreements);
    result.put("total", total);
    return returnSuccess(result);
  }


  /**
   * Author Nicholas-Ning Description //用户签约协议统计查看（商户超管通用） Date 15:16 2018/11/23 Param [request,
   * userName, certId, signDateStart, signDateEnd, userNo, companyId, signStatus, pageSize, pageNo,
   * customName] return java.util.Map<java.lang.String,java.lang.Object>
   **/
  @RequestMapping(value = "/sign/list/user/agreements/statistical", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> listUserAgreementStatistical(HttpServletRequest request,
      @RequestParam(required = false) String userName,
      @RequestParam(required = false) String certId,
      @RequestParam(required = false) String signDateStart,
      @RequestParam(required = false) String signDateEnd,
      @RequestParam(required = false) String userNo,
      @RequestParam(required = false) String userType,
      @RequestParam(required = false) String remark,
      @RequestParam(required = false) String companyId,
      @RequestParam(required = false) String signStatus,
      @RequestParam(required = false) String customName) {
    Map<String, Object> result = new HashMap<>(4);
    Map<String, Object> params = new HashMap<>(15);
    String customkey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
    if (!CommonString.ROOT.equals(customkey)) {
      params.put("originalId", customkey);
    }
    ChannelCustom channelCustom = channelCustomService.getCustomByCustomkey(customkey);
    if (channelCustom.getCustomType() == CustomType.GROUP.getCode()) {
      int id = organizationTreeService.queryNodeIdByCustomKey(channelCustom.getCustomkey());
      List<String> stringList = organizationTreeService
          .queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, id);
      String originalIds = Joiner.on(",").join(stringList);
      params.put("originalId", originalIds);
    }
    if (channelCustom.getCustomType() == CustomType.COMPANY.getCode()) {
      params.put("originalId", null);
      companyId = channelCustom.getCustomkey();
    }
    params.put("userName", userName);
    params.put("customName", customName);
    params.put("certId", certId);
    params.put("signDateStart", signDateStart);
    params.put("signDateEnd", signDateEnd);
    params.put("userNo", userNo);
    params.put("companyId", companyId);
    params.put("signStatus", signStatus);
    params.put("remark", remark);
    params.put("userType", userType);

    logger.info("用户签约协议统计查看 params{}", params);
    HashMap agreements = usersAgreementService.getAgreementsByParamsAndStatistical(params);
    result.put("agreements", agreements);
    return returnSuccess(result);
  }


  /**
   * @return java.util.Map<java.lang.String, java.lang.Object>
   * @Author YJY
   * @Description 发送短信通知前 获取前置数据
   * @Date 2020/10/29
   * @Param [request, userIds]
   **/
  @RequestMapping(value = "/sign/list/user/sms/param", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> smsParam(HttpServletRequest request,
      @RequestParam(required = false) String ids) {
    Map<String, Object> result = new HashMap<>(1);
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if (CommonString.ROOT.equals(customLogin.getCustomkey()) ||
        CommonString.ROOT.equals(customLogin.getMasterCustom())) {
      return returnFail(ResponseCodeMapping.ERR_529.getCode(),
          ResponseCodeMapping.ERR_529.getMessage());
    }
    String domainName = request.getServerName();
    log.info("获取到的域名为"+domainName);
    int customType = customLogin.getCustomType();
    /**
     * @Description 获取主商户信息
     **/
    if(customType == 4){

      ChannelCustom master = customService.getCustomByCustomkey(customLogin.getMasterCustom());
      customType = master.getCustomType();
    }

    if (customType != CustomType.CUSTOM.getCode()
        && customType != CustomType.COMPANY.getCode()
        && customType != CustomType.GROUP.getCode()) {
      return returnFail(ResponseCodeMapping.ERR_529.getCode(),
          ResponseCodeMapping.ERR_529.getMessage());
    }

    if (ObjectUtils.isEmpty(ids)) {
      return returnFail(ResponseCodeMapping.ERR_5009.getCode(),
          ResponseCodeMapping.ERR_5009.getMessage());
    }

    JSONObject jsonObject = usersAgreementSmsRemindService
        .checkUserId(Arrays.asList(ids.split(",")));

    if (ObjectUtils.isEmpty(jsonObject)) {
      return returnFail(ResponseCodeMapping.ERR_5009.getCode(),
          ResponseCodeMapping.ERR_5009.getMessage());
    }
    if ("no".equals(jsonObject.getString("isOne"))) {

      return returnFail(ResponseCodeMapping.ERR_530.getCode(),
          ResponseCodeMapping.ERR_530.getMessage());
    }
    Map<String, Object> map = usersAgreementSmsRemindService
        .findSmsTemplate(domainName, jsonObject.getString("customKey"), ids);
    map.put("companyName",jsonObject.getString("companyName"));
    result.put("data", map);
    return returnSuccess(result);
  }

  /**
   * @return java.util.Map<java.lang.String, java.lang.Object>
   * @Author YJY
   * @Description 发送未签约短信提醒
   * @Date 2020/10/29
   * @Param [request, userIds, smsSign, content]
   **/
  @RequestMapping(value = "/sign/list/user/send/sms", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> sendSms(HttpServletRequest request, String ids, String smsSign, String content) {
    Map<String, Object> result = new HashMap<>(1);
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if (CommonString.ROOT.equals(customLogin.getCustomkey()) ||
        CommonString.ROOT.equals(customLogin.getMasterCustom())) {
      return returnFail(ResponseCodeMapping.ERR_529.getCode(),
          ResponseCodeMapping.ERR_529.getMessage());
    }
    int customType = customLogin.getCustomType();
    /**
     * @Description 获取主商户信息
     **/
    if(customType == 4){

      ChannelCustom master = customService.getCustomByCustomkey(customLogin.getMasterCustom());
      customType = master.getCustomType();
    }

    if (customType != CustomType.CUSTOM.getCode()
        && customType != CustomType.COMPANY.getCode()
        && customType != CustomType.GROUP.getCode()) {
      return returnFail(ResponseCodeMapping.ERR_529.getCode(),
          ResponseCodeMapping.ERR_529.getMessage());
    }
    JSONObject jsonObject = usersAgreementSmsRemindService
        .checkUserId(Arrays.asList(ids.split(",")));

    if (ObjectUtils.isEmpty(jsonObject)) {
      return returnFail(ResponseCodeMapping.ERR_5009.getCode(),
          ResponseCodeMapping.ERR_5009.getMessage());
    }
    if ("no".equals(jsonObject.getString("isOne"))) {

      return returnFail(ResponseCodeMapping.ERR_530.getCode(),
          ResponseCodeMapping.ERR_530.getMessage());
    }

    boolean flag = usersAgreementSmsRemindService
        .sendSms(jsonObject.getString("customKey"), smsSign, content, ids);
    result.put("data", flag);
    if (flag) {
      return returnSuccess(result);
    }
    return returnFail(ResponseCodeMapping.ERR_531.getCode(),
        ResponseCodeMapping.ERR_531.getMessage());
  }

  /**
   * 查看用户签约协议
   *
   * @return 结果集  hasError  true  有错误  返回 errorList
   */
  @RequestMapping(value = "/agreements", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> agreements(HttpServletRequest request,
      @RequestParam(value = "userName", required = false) String userName,
      @RequestParam(value = "startTime", required = false) String startTime,
      @RequestParam(value = "endTime", required = false) String endTime,
      @RequestParam(value = "certId", required = false) String certId,
      @RequestParam(value = "companyName", required = false) String companyName,
      @RequestParam(value = "companyId", required = false) String companyId,
      @RequestParam(value = "id", required = false) String id,
      @RequestParam(value = "signStatus", required = false) String signStatus,
      @RequestParam(value = "templateName", required = false) String templateName,
      @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
      @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
    Map<String, Object> params = new HashMap<>(15);
    String customKey = (String) request.getSession().getAttribute("customkey");
    if (!CommonString.ROOT.equals(customKey)) {
      companyId = customKey;
    }

    params.put("companyId", companyId);
    params.put("userName", userName);
    params.put("startTime", startTime);
    params.put("endTime", endTime);
    params.put("certId", certId);
    params.put("id", id);
    params.put("companyName", companyName);
    params.put("signStatus", signStatus);
    params.put("templateName", templateName);
    List<String> agreementList = usersAgreementService
        .getUserAgreementsIdForPayCompanyByParam(params);
//		 int total = usersAgreementService.getUserAgreementsIdForPayCompanyByParamCount(params);
//
//		 params.put("start", (pageNo - 1) * pageSize);
//		 params.put("limit", pageSize);

    PageHelper.startPage(pageNo, pageSize);
    List<Map<String, Object>> agreements = usersAgreementService
        .getUserAgreementsForPayCompanyByParam(params);
    PageInfo page = new PageInfo(agreements);

    Map<String, Object> result = new HashMap<>(5);
    result.put("agreements", page.getList());
    result.put("total", page.getTotal());
    result.put("arrayList", agreementList);
    return returnSuccess(result);
  }

  /**
   * 导出用户签约协议
   */
  @RequestMapping(value = "/agreements/export", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> agreementsExport(HttpServletRequest request,
      @RequestParam(value = "userName", required = false) String userName,
      @RequestParam(value = "startTime") String startTime,
      @RequestParam(value = "endTime") String endTime,
      @RequestParam(value = "certId", required = false) String certId,
      @RequestParam(value = "companyName", required = false) String companyName,
      @RequestParam(value = "companyId", required = false) String companyId,
      @RequestParam(value = "email", required = false) String email,
      @RequestParam(value = "type", required = false) String type,
      @RequestParam(value = "templateName", required = false) String templateName) {
    Map<String, Object> params = new HashMap<>(15);
    String customKey = (String) request.getSession().getAttribute("customkey");
    if (!CommonString.ROOT.equals(customKey)) {
      companyId = customKey;
    }
    params.put("companyId", companyId);
    params.put("userName", userName);
    params.put("startTime", startTime);
    params.put("endTime", endTime);
    params.put("certId", certId);
    params.put("companyName", companyName);
    params.put("templateName", templateName);
    params.put("signStatus", 5);
    params.put("email", email);
    params.put("type", type);
    params.put("serverName", request.getServerName());
    return usersAgreementService.exportAgreementByType(params);
  }

  /**
   * 查看用户签约协议导出excel
   */
  @RequestMapping(value = "/agreements/excel")
  public void agreementsExcel(HttpServletRequest request, HttpServletResponse response,
      @RequestParam(value = "userName", required = false) String userName,
      @RequestParam(value = "startTime", required = false) String startTime,
      @RequestParam(value = "endTime", required = false) String endTime,
      @RequestParam(value = "certId", required = false) String certId,
      @RequestParam(value = "companyName", required = false) String companyName,
      @RequestParam(value = "companyId", required = false) String companyId,
      @RequestParam(value = "signStatus", required = false) String signStatus,
      @RequestParam(value = "templateName", required = false) String templateName) {
    Map<String, Object> params = new HashMap<>(15);
    String customKey = (String) request.getSession().getAttribute("customkey");
    if (!CommonString.ROOT.equals(customKey)) {
      companyId = customKey;
    }
    params.put("companyId", companyId);
    params.put("userName", userName);
    params.put("startTime", startTime);
    params.put("endTime", endTime);
    params.put("certId", certId);
    params.put("companyName", companyName);
    params.put("signStatus", signStatus);
    params.put("templateName", templateName);
    List<Map<String, Object>> agreements = usersAgreementService
        .getUserAgreementsForPayCompanyByParam(params);
    if (agreements.isEmpty()) {
      return;
    }
    int maxExportNum = 2000;
    if (agreements.size() > maxExportNum) {
      throw new LoginException("不支持导出数据大于2000条");
    }
    String today = DateUtils.getNowDay();
    List<Map<String, Object>> data = new ArrayList<>();
    String[] columnName = new String[]{"商户名称", "签约协议号", "用户名称", "证件类型", "证件号", "签约状态", "签名状态描述",
        "签约模板名称", "签约模板描述", "联系手机号", "签约时间"};
    for (Map<String, Object> dataResult : agreements) {
      Map<String, Object> dataMap = new HashMap<>(20);
      dataMap.put("1", dataResult.get("companyName"));
      dataMap.put("2", dataResult.get("agreementNo"));
      dataMap.put("3", dataResult.get("userName"));
      int documentType = (Integer) dataResult.get("documentType");
      String desc = CertType.codeOfDefault(documentType).getDesc();
      dataMap.put("4", desc);
      dataMap.put("5", dataResult.get("certId"));
      int status = (Integer) dataResult.get("signStatus");
      UsersAgreementSignType agreementSignType = UsersAgreementSignType.codeOf(status);
      dataMap.put("6", agreementSignType.getDesc());
      dataMap.put("7", dataResult.get("signStatusDes"));
      dataMap.put("8", dataResult.get("templateName"));
      dataMap.put("9", dataResult.get("thirdTemplateDes"));
      dataMap.put("10", dataResult.get("mobilePhone"));
      dataMap.put("11", dataResult.get("last_update_time"));
      data.add(sortMapByKey(dataMap));
    }
    String filename = today + "用户签约协议";
    ExcelFileGenerator.ExcelExport(response, columnName, filename, data);
  }

  /**
   * 审核
   */
  @RequestMapping(value = "/agreements/action/approval", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> approval(HttpServletRequest request, @RequestParam("id") String id,
      @RequestParam("signStatus") String signStatus,
      @RequestParam("signStatusDes") String signStatusDes) {
    Map<String, Object> params = new HashMap<>(15);
    String companyId = (String) request.getSession().getAttribute("customkey");
    params.put("companyId", companyId);
    params.put("id", id);
    List<UsersAgreement> agreements = usersAgreementService.getUsersAgreementsByParams(params);
    if (agreements.isEmpty()) {
      int code = RespCode.AGREEMENT_NOT_FOUND;
      return returnFail(code, RespCode.codeMaps.get(code));
    }
    UsersAgreement agreement = agreements.get(0);
    agreement.setSignStatus(Integer.parseInt(signStatus));
    agreement.setSignStatusDes(signStatusDes);
    ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
    agreement.setApprover(loginUser.getUsername());
    if (UsersAgreementSignType.SIGN_SUCCESS.getCode() == Integer.parseInt(signStatus)) {
      agreement.setDocumentStep(DocumentStep.DOCUMENT_SUCCESS.getCode());
      agreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
      usersAgreementService.updateUsersAgreementDocumentStep(agreement);
    }
    usersAgreementService.updateUsersAgreement(agreement);

    String thirdNo = agreement.getThirdNo();
    if (!StringUtil.isEmpty(thirdNo)) {
      CallBackInfo callBackInfo = callBackInfoService.getCallBackInfoBySerialNo(thirdNo);
      if (callBackInfo != null) {
        callBackInfo.setNotifyCount(0);
        callBackInfo.setStatus(0);
        callBackInfoService.updateCallBackInfo(callBackInfo);
      }
    }

    return returnSuccess(null);
  }

  /**
   * 批量审核通过
   */
  @RequestMapping(value = "/agreements/action/batch/approval/success", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> batchApprovalSuccess(@RequestParam("ids") String ids) {
    int count = usersAgreementService.updateUsersAgreementByBatch(ids);
    Map<String, Object> result = new HashMap<>(2);
    result.put("count", count);
    return returnSuccess(result);
  }

  /**
   * Author Nicholas-Ning Description  用户签约协议模板查看 Date 16:41 2018/11/23 Param [request, customName,
   * templateName, createTimeStart, createTimeEnd, agreementName, agreementPayment, companyId,
   * pageSize, pageNo] return java.util.Map<java.lang.String,java.lang.Object>
   **/
  @RequestMapping(value = "/sign/list/user/templates", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> listUserAgreementTemplates(HttpServletRequest request,
      @RequestParam(required = false) String customName,
      @RequestParam(required = false) String templateName,
      @RequestParam(required = false) String createTimeStart,
      @RequestParam(required = false) String createTimeEnd,
      @RequestParam(required = false) String agreementName,
      @RequestParam(required = false) String agreementPayment,
      @RequestParam(required = false) String companyId,
      @RequestParam(defaultValue = "10", required = false) int pageSize, int pageNo) {
    Map<String, Object> result = new HashMap<>(2);
    Map<String, Object> params = new HashMap<>(15);
    String customKey = (String) request.getSession().getAttribute("customkey");
    if (!CommonString.ROOT.equals(customKey)) {
      params.put("originalId", customKey);
    }
    String originalId = request.getParameter("originalId");
    if (!StringUtil.isEmpty(originalId)) {
      params.put("originalId", originalId);
    }

    ChannelCustom custom = channelCustomService.getCustomByCustomkey(customKey);
    if (custom.getCustomType() == CustomType.GROUP.getCode()) {
      int id = organizationTreeService.queryNodeIdByCustomKey(custom.getCustomkey());
      List<String> stringList = organizationTreeService
          .queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, id);
      String originalIds = Joiner.on(",").join(stringList);
      params.put("originalId", originalIds);
    }

    if (custom.getCustomType() == CustomType.COMPANY.getCode()) {
      companyId = custom.getCustomkey();
      params.put("originalId", "");
    }
    params.put("companyId", companyId);
    params.put("customName", customName);
    params.put("agreementName", agreementName);
    params.put("templateName", templateName);
    params.put("createTimeStart", createTimeStart);
    params.put("createTimeEnd", createTimeEnd);
    params.put("agreementPayment", agreementPayment);
    int total = agreementTemplateService.listUserAgreementTemplatesCount(params);
    params.put("start", (pageNo - 1) * pageSize);
    params.put("limit", pageSize);
    logger.info("用户签约协议模板查看 params{}", params);
    List<AgreementTemplate> templates = agreementTemplateService.listUserAgreementTemplates(params);
    result.put("total", total);
    result.put("templates", templates);
    return returnSuccess(result);
  }

  /**
   * 添加协议模版
   */
  @RequestMapping(value = "/agreement/template", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> template(
      @RequestParam("templateName") String templateName,
      @RequestParam("agreementName") String agreementName,
      @RequestParam("originalId") String originalId,
      @RequestParam("companyId") String companyId,
      @RequestParam("thirdMerchId") String thirdMerchId,
      @RequestParam("thirdTemplateId") String thirdTemplateId,
      @RequestParam("thirdTemplateDes") String thirdTemplateDes,
      @RequestParam(value = "regType", required = false, defaultValue = "1") int regType,
      @RequestParam("agreementPayment") String agreementPayment,
      @RequestParam(value = "agreementType") Integer agreementType,
      @RequestParam(value = "channelType") Integer channelType,
      @RequestParam(value = "uploadIdCard") Integer uploadIdCard) throws Exception {

    ChannelRelated channelRelated = channelRelatedService
        .getRelatedByCompAndOrig(originalId, companyId);
    if (channelRelated == null) {
      int code = RespCode.COMPANY_NOT_FOUND;
      return returnFail(code, RespCode.codeMaps.get(code));
    }

    Company company = companyService.getCompanyByUserId(Integer.valueOf(companyId));
    if (company == null || company.getCompanyKey() == null) {
      int code = RespCode.COMPANY_KEY_NOT_SETTING;
      return returnFail(code, RespCode.codeMaps.get(code));
    }

    String merchantId = channelRelated.getMerchantId();
    String companyAgreementMappingKey = merchantId + "_" + company.getCompanyKey();
    String companyAgreementMappingValue = agreementTemplateService
        .selectAgreementTemplate(companyAgreementMappingKey);

    if (companyAgreementMappingValue == null || "".equals(companyAgreementMappingValue)) {
      int code = RespCode.COMPANY_AGREEMENT_NOT_SETTING;
      return returnFail(code, RespCode.codeMaps.get(code));
    }
    String agreementTemplateURL = companyAgreementMappingValue
        + Constant.COMPANY_AGREEMENT_FILE_SUFFIX;

    AgreementTemplate agreementTemplate = new AgreementTemplate();
    agreementTemplate.setChannelType(channelType);
    agreementTemplate.setAgreementName(agreementName);
    agreementTemplate.setAgreementPayment(agreementPayment);
    agreementTemplate.setAgreementTemplateURL(agreementTemplateURL);
    agreementTemplate.setAgreementType(String.valueOf(agreementType));
    agreementTemplate.setCompanyId(companyId);
    agreementTemplate.setOriginalId(originalId);
    agreementTemplate.setMerchantId(merchantId);
    agreementTemplate.setThirdMerchId(thirdMerchId);
    agreementTemplate.setThirdTemplateId(thirdTemplateId);
    agreementTemplate.setThirdTemplateDes(thirdTemplateDes);
    agreementTemplate.setRegType(regType);
    agreementTemplate.setTemplateName(templateName);
    agreementTemplate.setHtmlTemplate(agreementTemplateURL);
    agreementTemplate.setUploadIdCard(uploadIdCard);
    agreementTemplateService.addAgreementTemplate(agreementTemplate);
    return returnSuccess(null);
  }

  /**
   * 修改
   */
  @RequestMapping(value = "/agreement/template/update", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> updateTemplate(
      @RequestParam("id") String id,
      @RequestParam("companyId") String companyId,
      @RequestParam("thirdMerchId") String thirdMerchId,
      @RequestParam("thirdTemplateId") String thirdTemplateId,
      @RequestParam("templateName") String templateName,
      @RequestParam("thirdTemplateDes") String thirdTemplateDes,
      @RequestParam("agreementName") String agreementName,
      @RequestParam("agreementPayment") String agreementPayment,
      @RequestParam(value = "agreementType") Integer agreementType,
      @RequestParam(value = "channelType") Integer channelType,
      @RequestParam(value = "uploadIdCard") Integer uploadIdCard) throws Exception {

    AgreementTemplate agreementTemplate = agreementTemplateService.getAgreementTemplateById(id);
    if (agreementTemplate == null) {
      int code = RespCode.AGREEMENT_TEMPLATE_NOT_FOUND;
      return returnFail(code, RespCode.codeMaps.get(code));
    }
    ChannelRelated channelRelated = channelRelatedService
        .getRelatedByCompAndOrig(agreementTemplate.getOriginalId(), companyId);
    if (channelRelated == null) {
      int code = RespCode.COMPANY_NOT_FOUND;
      return returnFail(code, RespCode.codeMaps.get(code));
    }
    Company company = companyService.getCompanyByUserId(Integer.valueOf(companyId));
    if (company == null || company.getCompanyKey() == null) {
      int code = RespCode.COMPANY_KEY_NOT_SETTING;
      return returnFail(code, RespCode.codeMaps.get(code));
    }
    String merchantId = channelRelated.getMerchantId();
    String companyAgreementMappingKey = merchantId + "_" + company.getCompanyKey();
    String companyAgreementMappingValue = agreementTemplateService
        .selectAgreementTemplate(companyAgreementMappingKey);
    if (companyAgreementMappingValue == null || "".equals(companyAgreementMappingValue)) {
      int code = RespCode.COMPANY_AGREEMENT_NOT_SETTING;
      return returnFail(code, RespCode.codeMaps.get(code));
    }
    String agreementTemplateURL = companyAgreementMappingValue
        + Constant.COMPANY_AGREEMENT_FILE_SUFFIX;

    agreementTemplate
        .build(templateName, agreementName, merchantId, companyId, thirdMerchId, thirdTemplateId,
            thirdTemplateDes, String.valueOf(agreementType), agreementPayment, agreementTemplateURL,
            channelType, uploadIdCard, agreementTemplateURL);

    agreementTemplateService.updateAgreementTemplate(agreementTemplate);
    return returnSuccess(null);
  }


  /**
   * 删除
   */
  @RequestMapping(value = "/agreement/template/delete", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> deleteTemplate(
      @RequestParam("id") String id) {
    Map<String, Object> params = new HashMap<>(4);
    params.put("id", id);
    List<AgreementTemplate> templates = agreementTemplateService
        .getAgreementTemplateByParam(params);
    if (templates.isEmpty()) {
      int code = RespCode.AGREEMENT_TEMPLATE_NOT_FOUND;
      return returnFail(code, RespCode.codeMaps.get(code));
    }
    agreementTemplateService.deleteAgreementTemplate(id);
    return returnSuccess(null);
  }

  /**
   * 获取服务公司列表
   */
  @RequestMapping(value = "/companies", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> companies(
      @RequestParam("originalId") String originalId) {
    List<Map<String, Object>> paymentList = channelCustomService
        .getPaymentListByOriginalId(originalId);
    return returnSuccess(paymentList);
  }

  /**
   * 修改用户协议模板限制接口
   */
  @RequestMapping(value = "/sign/list/user/saveTemplates", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> updateUserAgreementTemplates(HttpServletRequest request,
      @RequestParam("id") String id, @RequestParam("agreementPayment") String agreementPayment) {
    int respstat = RespCode.success;
    HashMap<String, Object> result = new HashMap<String, Object>();
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));

    String customkey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
    ChannelCustom channelCustom = customService.getCustomByCustomkey(customkey);
    if (QueryType.COMPANY == channelCustom.getCustomType()) {
      AgreementTemplate agreementTemplate = agreementTemplateService.getAgreementTemplateById(id);
      agreementTemplate.setAgreementPayment(agreementPayment);
      agreementTemplateService.updateAgreementTemplate(agreementTemplate);
    } else {
      return returnFail(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT, "权限不足！");
    }
    return result;
  }

  /**
   * 用户下发匹配签约信息
   */
  @RequestMapping("/queryUserAgreementMatch")
  @ResponseBody
  public Map<String, Object> queryUserAgreementMatch(HttpServletRequest request) {
    int respstat = RespCode.success;
    HashMap<String, Object> result = new HashMap<String, Object>();
    Page page = new Page(request);
    //校验是否有权限
    boolean checkFlag = true;
    //获取登陆信息
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    Integer[] allowCustomType = new Integer[]{CustomType.COMPANY.getCode(), 6};
    checkFlag = channelCustomService
        .getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
    if (checkFlag) {
      Map<String, Object> params = new HashMap<String, Object>();
      if (page.getParams().containsKey("startTime") && !StringUtil
          .isEmpty(page.getParams().get("startTime"))) {
        params.put("startTime", page.getParams().get("startTime"));
      }
      if (page.getParams().containsKey("endTime") && !StringUtil
          .isEmpty(page.getParams().get("endTime"))) {
        params.put("endTime", page.getParams().get("endTime"));
      }
      List<Map<String, String>> agreementMatchList = channelHistoryService
          .queryUserAgreementMatch(page);
      int total = channelHistoryService.queryUserAgreementMatchCount(page);
      for (Map<String, String> agreementMatch : agreementMatchList) {
        params.put("originalId", agreementMatch.get("originalId"));
        params.put("companyId", agreementMatch.get("companyId"));
        List<Map<String, String>> noAgreementList = channelHistoryService.noAgreementCount(params);
        agreementMatch.put("noAgreementCount",
            String.valueOf(noAgreementList == null ? 0 : noAgreementList.size()));
        int count = 0;
        for (Map<String, String> certMap : noAgreementList) {
          certMap.put("originalId", agreementMatch.get("originalId"));
          int agreementOtherCompanyCount = channelHistoryService
              .agreementOtherCompanyCount(certMap);
          count += agreementOtherCompanyCount;
        }
        agreementMatch.put("agreementOtherCompanyCount", String.valueOf(count));
      }
      result.put("total", total);
      result.put("relationList", agreementMatchList);
    } else {
      respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
    }
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    return result;
  }

  /**
   * 用户下发匹配签约-统计数据导出
   */
  @RequestMapping("/export")
  @ResponseBody
  public void export(HttpServletRequest request, HttpServletResponse response) {
    // 标题
    String[] headers = new String[]{"商户名称", "服务公司", "下发用户个数", "本服务公司未签约用户数", "未签约在其他服务公司签约成功数",
        "所属平台", "所属代理商"};
    String filename = "用户下发匹配签约-统计数据";
    Page page = new Page(request);
    //校验是否有权限
    boolean checkFlag = true;
    //获取登陆信息
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    Integer[] allowCustomType = new Integer[]{CustomType.COMPANY.getCode(), 6};
    checkFlag = channelCustomService
        .getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
    if (checkFlag) {
      Map<String, Object> params = new HashMap<String, Object>();
      if (page.getParams().containsKey("startTime") && !StringUtil
          .isEmpty(page.getParams().get("startTime"))) {
        params.put("startTime", page.getParams().get("startTime"));
      }
      if (page.getParams().containsKey("endTime") && !StringUtil
          .isEmpty(page.getParams().get("endTime"))) {
        params.put("endTime", page.getParams().get("endTime"));
      }
      List<Map<String, String>> agreementMatchList = channelHistoryService
          .queryUserAgreementMatchNoPage(page);
      List<Map<String, Object>> data = new ArrayList<>();
      for (Map<String, String> agreementMatch : agreementMatchList) {
        params.put("originalId", agreementMatch.get("originalId"));
        params.put("companyId", agreementMatch.get("companyId"));
        List<Map<String, String>> noAgreementList = channelHistoryService.noAgreementCount(params);
        agreementMatch.put("noAgreementCount",
            String.valueOf(noAgreementList == null ? 0 : noAgreementList.size()));
        int count = 0;
        for (Map<String, String> certMap : noAgreementList) {
          certMap.put("originalId", agreementMatch.get("originalId"));
          int agreementOtherCompanyCount = channelHistoryService
              .agreementOtherCompanyCount(certMap);
          count += agreementOtherCompanyCount;
        }
        agreementMatch.put("agreementOtherCompanyCount", String.valueOf(count));
        Map<String, Object> dataMap = new HashMap<>(20);
        dataMap.put("1", agreementMatch.get("merchantName"));
        dataMap.put("2", agreementMatch.get("companyName"));
        dataMap.put("3", agreementMatch.get("count"));
        dataMap.put("4", agreementMatch.get("noAgreementCount"));
        dataMap.put("5", agreementMatch.get("agreementOtherCompanyCount"));
        dataMap.put("6", agreementMatch.get("businessPlatform"));
        dataMap.put("7", agreementMatch.get("businessChannel"));
        data.add(sortMapByKey(dataMap));
      }
      ExcelFileGenerator.ExcelExport(response, headers, filename, data);
    } else {
      logger.info("权限不足");
    }
  }

  /**
   * 导出全部未签约数据
   */
  @RequestMapping("/exportPayUsers")
  @ResponseBody
  public void exportPayUsers(HttpServletRequest request, HttpServletResponse response) {
    // 标题
    String[] headers = new String[]{"姓名", "证件号", "商户名称", "服务公司", "所属平台", "所属代理商"};
    String filename = "下发用户数";
    Page page = new Page(request);
    //校验是否有权限
    boolean checkFlag = true;
    //获取登陆信息
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    Integer[] allowCustomType = new Integer[]{CustomType.COMPANY.getCode(), 6};
    checkFlag = channelCustomService
        .getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
    if (checkFlag) {
      Map<String, Object> params = new HashMap<String, Object>();
      if (page.getParams().containsKey("startTime") && !StringUtil
          .isEmpty(page.getParams().get("startTime"))) {
        params.put("startTime", page.getParams().get("startTime"));
      }
      if (page.getParams().containsKey("endTime") && !StringUtil
          .isEmpty(page.getParams().get("endTime"))) {
        params.put("endTime", page.getParams().get("endTime"));
      }
      List<Map<String, String>> agreementMatchList = channelHistoryService
          .queryUserAgreementMatchNoPage(page);
      List<Map<String, Object>> data = new ArrayList<>();
      for (Map<String, String> agreementMatch : agreementMatchList) {
        params.put("customkey", agreementMatch.get("originalId"));
        params.put("companyId", agreementMatch.get("companyId"));
        List<Map<String, String>> payUsers = channelHistoryService.payUsers(params);
        for (Map<String, String> payUser : payUsers) {
          Map<String, Object> dataMap = new HashMap<>(20);
          dataMap.put("1", payUser.get("userName"));
          dataMap.put("2", payUser.get("certId"));
          dataMap.put("3", agreementMatch.get("merchantName"));
          dataMap.put("4", agreementMatch.get("companyName"));
          dataMap.put("5", agreementMatch.get("businessPlatform"));
          dataMap.put("6", agreementMatch.get("businessChannel"));
          data.add(sortMapByKey(dataMap));
        }
      }
      ExcelFileGenerator.ExcelExport(response, headers, filename, data);
    } else {
      logger.info("权限不足");
    }
  }

  /**
   * 导出全部未签约数据
   */
  @RequestMapping("/exportNoAgreementCertIds")
  @ResponseBody
  public void exportNoAgreementCertIds(HttpServletRequest request, HttpServletResponse response) {
    // 标题
    String[] headers = new String[]{"姓名", "证件号", "商户名称", "服务公司", "所属平台", "所属代理商"};
    String filename = "用户下发匹配签约-未签约数据";
    Page page = new Page(request);
    //校验是否有权限
    boolean checkFlag = true;
    //获取登陆信息
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    Integer[] allowCustomType = new Integer[]{CustomType.COMPANY.getCode(), 6};
    checkFlag = channelCustomService
        .getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
    if (checkFlag) {
      Map<String, Object> params = new HashMap<String, Object>();
      if (page.getParams().containsKey("startTime") && !StringUtil
          .isEmpty(page.getParams().get("startTime"))) {
        params.put("startTime", page.getParams().get("startTime"));
      }
      if (page.getParams().containsKey("endTime") && !StringUtil
          .isEmpty(page.getParams().get("endTime"))) {
        params.put("endTime", page.getParams().get("endTime"));
      }
      List<Map<String, String>> agreementMatchList = channelHistoryService
          .queryUserAgreementMatchNoPage(page);
      List<Map<String, Object>> data = new ArrayList<>();
      for (Map<String, String> agreementMatch : agreementMatchList) {
        params.put("originalId", agreementMatch.get("originalId"));
        params.put("companyId", agreementMatch.get("companyId"));
        List<Map<String, String>> noAgreementList = channelHistoryService.noAgreementCount(params);
        for (Map<String, String> noAgreement : noAgreementList) {
          Map<String, Object> dataMap = new HashMap<>(20);
          dataMap.put("1", noAgreement.get("userName"));
          dataMap.put("2", noAgreement.get("certId"));
          dataMap.put("3", agreementMatch.get("merchantName"));
          dataMap.put("4", agreementMatch.get("companyName"));
          dataMap.put("5", agreementMatch.get("businessPlatform"));
          dataMap.put("6", agreementMatch.get("businessChannel"));
          data.add(sortMapByKey(dataMap));
        }

      }
      ExcelFileGenerator.ExcelExport(response, headers, filename, data);
    } else {
      logger.info("权限不足");
    }
  }

  /**
   * 导出其他签约数据
   */
  @RequestMapping("/exportOtherAgreementCertIds")
  @ResponseBody
  public void exportOtherAgreementCertIds(HttpServletRequest request,
      HttpServletResponse response) {
    // 标题
    String[] headers = new String[]{"姓名", "证件号", "商户名称", "服务公司", "所属平台", "所属代理商", "其他服务公司签约"};
    String filename = "用户下发匹配签约-其他签约数据";
    Page page = new Page(request);
    //校验是否有权限
    boolean checkFlag = true;
    //获取登陆信息
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    Integer[] allowCustomType = new Integer[]{CustomType.COMPANY.getCode(), 6};
    checkFlag = channelCustomService
        .getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
    if (checkFlag) {
      Map<String, Object> params = new HashMap<String, Object>();
      if (page.getParams().containsKey("startTime") && !StringUtil
          .isEmpty(page.getParams().get("startTime"))) {
        params.put("startTime", page.getParams().get("startTime"));
      }
      if (page.getParams().containsKey("endTime") && !StringUtil
          .isEmpty(page.getParams().get("endTime"))) {
        params.put("endTime", page.getParams().get("endTime"));
      }
      List<Map<String, String>> agreementMatchList = channelHistoryService
          .queryUserAgreementMatchNoPage(page);
      List<Map<String, Object>> data = new ArrayList<>();
      List<Map<String, String>> otherCompanySignCertIdList = new ArrayList<Map<String, String>>();
      for (Map<String, String> agreementMatch : agreementMatchList) {
        params.put("originalId", agreementMatch.get("originalId"));
        params.put("companyId", agreementMatch.get("companyId"));
        List<Map<String, String>> noAgreementList = channelHistoryService.noAgreementCount(params);
        for (Map<String, String> noAgreement : noAgreementList) {
          noAgreement.put("originalId", agreementMatch.get("originalId"));
          String companyNames = channelHistoryService.agreementOtherCompanyNames(noAgreement);
          if (!StringUtil.isEmpty(companyNames)) {
            noAgreement.put("merchantName", agreementMatch.get("merchantName"));
            noAgreement.put("businessChannel", agreementMatch.get("businessChannel"));
            noAgreement.put("companyName", agreementMatch.get("companyName"));
            noAgreement.put("businessPlatform", agreementMatch.get("businessPlatform"));
            noAgreement.put("otherCompanyNames", companyNames);
            otherCompanySignCertIdList.add(noAgreement);
          }
        }
      }
      for (Map<String, String> otherCompanyCertId : otherCompanySignCertIdList) {
        Map<String, Object> dataMap = new HashMap<>(20);
        dataMap.put("1", otherCompanyCertId.get("userName"));
        dataMap.put("2", otherCompanyCertId.get("certId"));
        dataMap.put("3", otherCompanyCertId.get("merchantName"));
        dataMap.put("4", otherCompanyCertId.get("companyName"));
        dataMap.put("5", otherCompanyCertId.get("businessPlatform"));
        dataMap.put("6", otherCompanyCertId.get("businessChannel"));
        dataMap.put("7", otherCompanyCertId.get("otherCompanyNames"));
        data.add(sortMapByKey(dataMap));
      }
      ExcelFileGenerator.ExcelExport(response, headers, filename, data);
    } else {
      logger.info("权限不足");
    }
  }


}
