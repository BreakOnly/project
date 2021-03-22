package com.jrmf.controller.subscriber;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.*;
import com.jrmf.controller.constant.sms.SmsTemplateCodeEnum;
import com.jrmf.domain.*;
import com.jrmf.domain.dto.UsersAgreementDTO;
import com.jrmf.service.*;
import com.jrmf.utils.*;
import com.jrmf.utils.bestSign.ContractUserCertExtrResult;
import com.jrmf.utils.exception.LoginException;
import com.jrmf.utils.exception.SessionDestroyedException;
import com.jrmf.utils.threadpool.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author chonglulu
 */
@Controller
@RequestMapping("/wallet/subscriber/bestsign")
public class BestSignController extends BaseController {

  private static Logger logger = LoggerFactory.getLogger(BestSignController.class);

  private final UserSerivce userSerivce;

  private final UsersAgreementService usersAgreementService;

  private final UserRelatedService userRelatedService;

  private final ChannelCustomService channelCustomService;

  private final AgreementTemplateService agreementTemplateService;

  private final OemConfigService oemConfigService;

  private final QbInvoiceBaseService qbInvoiceBaseService;

  private final String flag = SMSSendUtils.VALI_MOBILE;

  @Autowired
  private BaseInfo baseInfo;

  @Autowired
  public BestSignController(UserSerivce userSerivce, UsersAgreementService usersAgreementService,
      UserRelatedService userRelatedService, ChannelCustomService channelCustomService,
      AgreementTemplateService agreementTemplateService, OemConfigService oemConfigService,
      QbInvoiceBaseService qbInvoiceBaseService) {
    this.userSerivce = userSerivce;
    this.usersAgreementService = usersAgreementService;
    this.userRelatedService = userRelatedService;
    this.channelCustomService = channelCustomService;
    this.agreementTemplateService = agreementTemplateService;
    this.oemConfigService = oemConfigService;
    this.qbInvoiceBaseService = qbInvoiceBaseService;
  }

  /**
   * 登录
   *
   * @return state = 1 成功 state ！= 1 失败 根据status 做路由 1-创建 2-签约处理中 3-签约待审核 4-签约失败 5-签约成功 6-属于多家商户
   */
  @RequestMapping(value = "/login", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> login(HttpServletRequest request, @RequestParam("userName") String userName,
      @RequestParam("certId") String certId, @RequestParam("documentType") String documentType,
      @RequestParam(value = "originalId", required = false) String originalId) {
    Map<String, Object> paramMap = new HashMap<>(8);

    boolean chineseName = StringUtil.isChinese(userName);
    if (chineseName) {
      userName = userName.replaceAll(" ", "");
    } else {
      userName = userName.trim();
    }
    certId = certId.replaceAll(" ", "");
    logger.info("用户签约---信息：userName:" + userName + "-certId:" + certId);

    paramMap.put("userName", userName);
    paramMap.put("certId", certId);
    paramMap.put("documentType", documentType);
    paramMap.put("active", "true");
    User user = userSerivce.getUsersCountByCard(paramMap);
    if (user == null) {
      logger.error("用户不存在");
      throw new LoginException(RespCode.codeMaps.get(RespCode.USER_NOT_FOUND));
    }
    request.getSession().setAttribute("user", user);
    request.getSession().setAttribute("customLogin", user);
    Map<String, Object> map = new HashMap<>(2);
    map.put("userId", user.getId());
    List<UserRelated> userRelateds = userRelatedService.getRelatedByParam(map);

    if (userRelateds.isEmpty()) {
      logger.error("用户未找到所属商户");
      throw new LoginException(RespCode.codeMaps.get(RespCode.ORIGIN_NOT_FOUND));
    }
    String status = "status";
    Map<String, Object> resultMap = new HashMap<>(2);

    Set<String> originalIdSet = new HashSet<>();
    for (UserRelated userRelated : userRelateds) {
      originalIdSet.add(userRelated.getOriginalId());
    }
    if (originalIdSet.size() > 1) {
      logger.info("用户同属于多家商户");
      if (StringUtil.isEmpty(originalId)) {
        logger.info("需要返回前端重新选择所属商户");
        Set<ChannelCustom> channelCustomList = new HashSet<>();
        for (UserRelated userRelated : userRelateds) {
          ChannelCustom channelCustom = channelCustomService
              .getCustomByCustomkey(userRelated.getOriginalId());
          channelCustomList.add(channelCustom);
        }
        resultMap.put(status, UsersAgreementSignType.SIGN_CHOOSE_CUSTOM.getCode());
        resultMap.put("channelCustomList", channelCustomList);
        return returnSuccess(resultMap);
      } else {
        map.put("originalId", originalId);
        UserRelated userRelated = userRelatedService.getRelatedByParam(map).get(0);
        request.getSession().setAttribute("userRelated", userRelated);
      }
    } else {
      request.getSession().setAttribute("userRelated", userRelateds.get(0));
    }
    UserRelated userRelated = (UserRelated) request.getSession().getAttribute("userRelated");
    //插入用户协议表
    paramMap.clear();
    //平台id
    paramMap.put("originalId", userRelated.getOriginalId());
    List<AgreementTemplate> agreementTemplateList = agreementTemplateService
        .getAgreementTemplateByParam(paramMap);
    for (AgreementTemplate agreementTemplate : agreementTemplateList) {
      usersAgreementService
          .addUserAgreement(agreementTemplate, user.getId(), userRelated.getOriginalId(), userName,
              certId, Integer.parseInt(documentType), "", SignSubmitType.H5);
    }

    paramMap.clear();
    paramMap.put("userId", user.getId() + "");
    paramMap.put("originalId", userRelated.getOriginalId());
    List<UsersAgreement> agreements = usersAgreementService.getUsersAgreementsByParams(paramMap);
    int create = 0;
    int createCode = UsersAgreementSignType.SIGN_CREATE.getCode();
    int failCode = UsersAgreementSignType.SIGN_FAIL.getCode();
    int processingCode = UsersAgreementSignType.SIGN_PROCESSING.getCode();
    int preReviewCode = UsersAgreementSignType.SIGN_PRE_REVIEW.getCode();
    for (UsersAgreement agreement : agreements) {
      if (createCode == agreement.getSignStatus()) {
        create++;
        continue;
      }
      if (processingCode == agreement.getSignStatus()) {
        resultMap.put(status, processingCode);
        resultMap.put("message", UsersAgreementSignType.SIGN_PROCESSING.getDesc());

        return returnSuccess(resultMap);
      }
      if (preReviewCode == agreement.getSignStatus()) {
        resultMap.put(status, preReviewCode);
        resultMap.put("message", UsersAgreementSignType.SIGN_PRE_REVIEW.getDesc());

        return returnSuccess(resultMap);
      }
      if (failCode == agreement.getSignStatus()) {
        resultMap.put(status, failCode);
        return returnSuccess(resultMap);
      }
    }
    if (create > 0) {
      resultMap.put(status, UsersAgreementSignType.SIGN_CREATE.getCode());
    } else {
      resultMap.put(status, UsersAgreementSignType.SIGN_SUCCESS.getCode());
    }
    return returnSuccess(resultMap);
  }

  /**
   * 选择商户
   */
  @RequestMapping(value = "/list", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> originList(HttpServletRequest request,
      @RequestParam("userName") String userName, @RequestParam("certId") String certId,
      @RequestParam("documentType") String documentType) {
    Map<String, Object> paramMap = new HashMap<>(4);

    boolean chineseName = StringUtil.isChinese(userName);
    if (chineseName) {
      userName = userName.replaceAll(" ", "");
    } else {
      userName = userName.trim();
    }
    certId = certId.replaceAll(" ", "");
    logger.info("用户签约-选择商户--信息：userName:" + userName + "-certId:" + certId);

    paramMap.put("userName", userName);
    paramMap.put("certId", certId);
    paramMap.put("documentType", documentType);
    paramMap.put("active", "true");
    User user = userSerivce.getUsersCountByCard(paramMap);
    if (user == null) {
      throw new LoginException(RespCode.codeMaps.get(RespCode.USER_NOT_FOUND));
    }
    Map<String, Object> map = new HashMap<>(2);
    map.put("userId", user.getId());
    List<UserRelated> userRelateds = userRelatedService.getRelatedByParam(map);
    if (userRelateds.isEmpty()) {
      logger.error("用户未找到所属商户");
      throw new LoginException(RespCode.codeMaps.get(RespCode.USER_NOT_FOUND));
    }
    Set<ChannelCustom> channelCustomList = new HashSet<>();
    for (UserRelated userRelated : userRelateds) {
      ChannelCustom channelCustom = channelCustomService
          .getCustomByCustomkey(userRelated.getOriginalId());
      if (!StringUtil.isEmpty(channelCustom.getOemCUrl()) && !request.getServerName()
          .equals(channelCustom.getOemCUrl())) {
        continue;
      }
      channelCustomList.add(channelCustom);
    }
    Map<String, Object> resultMap = new HashMap<>(2);
    resultMap.put("channelCustomList", channelCustomList);
    return returnSuccess(resultMap);
  }

  /**
   * 签约信息页面
   *
   * @return state = 1 成功 state ！= 1 失败
   */
  @RequestMapping(value = "/signPage", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> signPage(HttpServletRequest request) {
    User user = (User) request.getSession().getAttribute("user");
    UserRelated userRelated = (UserRelated) request.getSession().getAttribute("userRelated");
    if (user == null || userRelated == null) {
      throw new SessionDestroyedException(RespCode.codeMaps.get(RespCode.SESSION_DESTROYED));
    }
    Map<String, Object> paramMap = new HashMap<>(4);
    paramMap.put("userId", user.getId() + "");
    String originalId = userRelated.getOriginalId();
    paramMap.put("originalId", originalId);
    List<UsersAgreement> usersAgreementList = usersAgreementService
        .getUsersAgreementsByParams(paramMap);
    Map<String, Object> resultMap = new HashMap<>(4);
    resultMap.put("usersAgreementList", usersAgreementList);
    resultMap.put("userName", user.getUserName());
    resultMap.put("certId", user.getCertId());
    ChannelCustom channelCustom = channelCustomService.getCustomByCustomkey(originalId);
    if (channelCustom == null) {
      logger.error("未找到商户信息");
      throw new SessionDestroyedException(RespCode.codeMaps.get(RespCode.SESSION_DESTROYED));
    }
    resultMap.put("channelCustomName", channelCustom.getCompanyName());
    return returnSuccess(resultMap);
  }

  /**
   * 发短信
   *
   * @return state = 1 成功 state ！= 1 失败
   */
  @RequestMapping(value = "/sendCode", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> sendCode(HttpServletRequest request,
      @RequestParam("mobileNo") String mobileNo) {
    User user = (User) request.getSession().getAttribute("user");
    if (user == null) {
      throw new SessionDestroyedException(RespCode.codeMaps.get(RespCode.SESSION_DESTROYED));
    }
    Map<String, Object> map = new HashMap<>(4);
    map.put("portalDomain", request.getServerName());
    OemConfig oemConfig = oemConfigService.getOemByParam(map);
    String smsSignature = oemConfig.getSmsSignature();
    String code = StringUtil.GetRandomNumberStr6();
    final String content = "【" + smsSignature + "】签约验证码 " + code + "。感谢您使用我司为自由职业从业者提供的云结算服务。";
    String[] mobileArray = new String[]{mobileNo};
    final String templateParam = "{\"code\":\"" + code + "\"}";
    sendCode(request, flag, code, mobileArray, content, smsSignature,
        SmsTemplateCodeEnum.SIGN.getCode(), templateParam);
    return returnSuccess();
  }

  /**
   * 验证手机动态码，成功就提交，失败，返回错误信息
   */
  @RequestMapping(value = "/single/submit")
  public @ResponseBody
  Map<String, Object> singleSubmit(HttpServletRequest request,
      @RequestParam("mobileNo") String mobileNo, @RequestParam("code") String code,
      @RequestParam("frontFile") MultipartFile frontFile,
      @RequestParam("backFile") MultipartFile backFile) throws IOException {

    User user = (User) request.getSession().getAttribute("user");
    UserRelated userRelated = (UserRelated) request.getSession().getAttribute("userRelated");
    if (user == null || userRelated == null) {
      logger.error("登录超时");
      int state = RespCode.error306;
      return returnFail(state, RespCode.codeMaps.get(state));
    }

    code = code.trim();
    Parameter param = parameterService.valiMobiletelno(mobileNo, code, flag);
    if (param == null) {
      int state = RespCode.CODE_VILID_ERROR;
      return returnFail(state, RespCode.codeMaps.get(state));
    }

    HashMap<String, Object> map = new HashMap<>(4);
    map.put("userId", user.getId() + "");
    map.put("originalId", userRelated.getOriginalId());
    List<UsersAgreement> usersAgreementList = usersAgreementService.getUsersAgreementsByParams(map);
    if (usersAgreementList.isEmpty()) {
      return returnSuccess();
    }

    InputStream frontFileInputStream = frontFile.getInputStream();
    byte[] frontBytes = new byte[1024];
    int frontReadLen;
    ByteArrayOutputStream frontOut = new ByteArrayOutputStream();
    while ((frontReadLen = frontFileInputStream.read(frontBytes)) > -1) {
      frontOut.write(frontBytes, 0, frontReadLen);
    }
    final byte[] frontFileBytes = frontOut.toByteArray();

    InputStream backFileInputStream = backFile.getInputStream();
    byte[] backBytes = new byte[1024];
    int backReadLen;
    ByteArrayOutputStream backOut = new ByteArrayOutputStream();
    while ((backReadLen = backFileInputStream.read(backBytes)) > -1) {
      backOut.write(backBytes, 0, backReadLen);
    }
    final byte[] backFileBytes = backOut.toByteArray();

    final List<UsersAgreement> usersAgreements = usersAgreementList;
    //起一个新线程
    String processId = MDC.get(PROCESS);

    ThreadUtil.cashThreadPool.execute(() -> {
      try {
        MDC.put(PROCESS, processId);
        usersAgreementService.singleSign(usersAgreements, frontFileBytes, backFileBytes, mobileNo,
            SignSubmitType.H5.getCode(), "", true, null);
      } catch (IOException e) {
        logger.error("签约图片异常");
        logger.error(e.getMessage(), e);
      } finally {
        MDC.remove(PROCESS);
      }
    });
    return returnSuccess();
  }

  /**
   * 异步通知地址
   *
   * @return success 成功 其他 失败
   * @throws IOException io异常
   */
  @RequestMapping(value = "/signSuccess", method = RequestMethod.POST)
  public @ResponseBody
  String signSuccess(HttpServletRequest req) throws IOException {
    ServletInputStream ris = req.getInputStream();
    StringBuilder content = new StringBuilder();
    byte[] b = new byte[1024];
    int lens;
    while ((lens = ris.read(b)) > 0) {
      content.append(new String(b, 0, lens));
    }
    String strcont = content.toString();
    logger.info("签名后台通知返回结果信息" + strcont);
    JSONObject jsonObj = JSON.parseObject(strcont);
    String subState = jsonObj.getString("subState");
    String state = jsonObj.getString("state");
    char charAt = subState.charAt(1);
    String usersAgreementId = req.getParameter("usersAgreementId");
    Map<String, Object> map = new HashMap<>(2);
    map.put("id", usersAgreementId);
    List<UsersAgreement> usersAgreementList = usersAgreementService.getUsersAgreementsByParams(map);
    if (usersAgreementList.isEmpty()) {
      logger.info("没有找到这个签约协议");
      return "fail";
    }
    UsersAgreement usersAgreement = usersAgreementList.get(0);
    if ("CLOSED".equals(state) && "220".equals(subState)) {
      String urls = (String) jsonObj.get("outerDownloadUrl");
      String[] split = urls.split("\\|");
      String url = split[2];
      usersAgreement.setAgreementURL(url);
      usersAgreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
      usersAgreement.setLastUpdateTime("now");
    }
    if (charAt != '2') {
      logger.info("签约失败，修改用户的签约状态");
      usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
      usersAgreement.setSignStep(SignStep.SIGN_FAIL.getCode());
      usersAgreement.setSignStatusDes(jsonObj.getString("stateDesc"));
      usersAgreement.setLastUpdateTime("now");
    }
    usersAgreementService.updateUsersAgreementSignStep(usersAgreement);
    usersAgreementService.checkSignStatus(usersAgreement);
    return "SUCCESS";
  }


  /**
   * 接收图片验证结果
   *
   * @throws IOException io异常
   */
  @RequestMapping(value = "receive/picture_check/result", method = RequestMethod.POST)
  public @ResponseBody
  String pictureCheckResult(@RequestBody ContractUserCertExtrResult param,
      HttpServletRequest request) throws IOException {
    logger.info("上传身份证后台通知返回结果：" + param.toString());
    String resultCode = param.getResultCode();
    String usersAgreementId = request.getParameter("usersAgreementId");
    Map<String, Object> paramMap = new HashMap<>(2);
    paramMap.put("id", usersAgreementId);
    List<UsersAgreement> usersAgreements = usersAgreementService
        .getUsersAgreementsByParams(paramMap);
    if (usersAgreements.isEmpty()) {
      logger.error("上传身份证失败，查询记录失败");
      return "fail";
    }
    UsersAgreement usersAgreement = usersAgreements.get(0);
    if ("FAIL".equals(resultCode)) {
      logger.info("上传失败");
      usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
      usersAgreement.setDocumentStep(DocumentStep.DOCUMENT_FAIL.getCode());
      usersAgreement.setSignStatusDes(param.getResultMessage());
      Map<String, Object> hashMap = new HashMap<>(6);
      hashMap.put("id", usersAgreement.getId());
      hashMap.put("imageURLA", "");
      hashMap.put("imageURLB", "");
      usersAgreementService.updateUsersAgreementImageURL(hashMap);
      usersAgreement.setLastUpdateTime("now");
    } else {
      logger.info("上传成功");
      usersAgreement.setDocumentStep(DocumentStep.DOCUMENT_SUCCESS.getCode());
      usersAgreement.setLastUpdateTime("now");
    }
    usersAgreementService.updateUsersAgreementDocumentStep(usersAgreement);
    usersAgreementService.checkSignStatus(usersAgreement);
    return "SUCCESS";
  }

  /**
   * 获取长连接
   */
  private String longUrl(String url) {
    HttpURLConnection conn;
    try {
      conn = (HttpURLConnection) new URL(
          "http://api.t.sina.com.cn/short_url/expand.json?source=2815391962&url_short=" + url)
          .openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Content-Type", "application/json");
      InputStream in = conn.getInputStream();
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int retLen;
      while ((retLen = in.read(buffer)) > -1) {
        bytes.write(buffer, 0, retLen);
      }

      return new String(bytes.toByteArray(), "utf-8");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return url;
    }
  }

  /**
   * 验证手机动态码，成功就提交，失败，返回错误信息
   */
  @RequestMapping(value = "/single/submit/Two")
  @ResponseBody
  public Map<String, Object> singleSubmitTwo(HttpServletRequest request,
      @RequestParam("mobileNo") String mobileNo, @RequestParam("code") String code,
      @RequestParam("frontFile") MultipartFile frontFile,
      @RequestParam("backFile") MultipartFile backFile) throws IOException {
    User user = (User) request.getSession().getAttribute("user");
    UserRelated userRelated = (UserRelated) request.getSession().getAttribute("userRelated");

    Map<String, Object> checkResultMap = checkBaseSign(user, userRelated, code, mobileNo);
    if (!checkResultMap.isEmpty()) {
      return checkResultMap;
    }

    List<UsersAgreement> usersAgreementList = getUsersAgreement(user.getId() + "",
        userRelated.getOriginalId());
    if (usersAgreementList.isEmpty()) {
      return returnSuccess();
    }

    byte[] frontFileBytes = getFileByte(frontFile);
    byte[] backFileBytes = getFileByte(backFile);

    UsersAgreementDTO usersAgreementDTO = this
        .getTransferTemplate(frontFileBytes, backFileBytes, mobileNo);
    startSingleThreadSignTwo(usersAgreementList, usersAgreementDTO);
    return returnSuccess();
  }

  /**
   * 签约信息页面
   *
   * @return state = 1 成功 state ！= 1 失败
   */
  @RequestMapping(value = "/agreementTemplateParamInfo", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> signPage(Integer agreementTemplateId, Integer userId) {

    UsersAgreement usersAgreement = usersAgreementService
        .getUsersAgreement(agreementTemplateId, userId);

    Map<String, Object> resultMap = new HashMap<>(4);
    if (usersAgreement != null) {
      resultMap.put("customName", usersAgreement.getCustomName());
      resultMap.put("companyName", usersAgreement.getCompanyName());
      resultMap.put("userName", usersAgreement.getUserName());
      resultMap.put("certId", usersAgreement.getCertId());
      if (usersAgreement.getImageURLA() != null && !"".equals(usersAgreement.getImageURLA())) {
        resultMap.put("imageURLA", baseInfo.getDomainName() + usersAgreement.getImageURLA());
      }
      if (usersAgreement.getImageURLB() != null && !"".equals(usersAgreement.getImageURLB())){
        resultMap.put("imageURLB", baseInfo.getDomainName() + usersAgreement.getImageURLB());
      }
      resultMap.put("createTime", usersAgreement.getCreateTime());

      String serviceTypeNames = qbInvoiceBaseService
          .getServiceTypeNamesByCustomKeyAndCompanyId(usersAgreement.getOriginalId(),
              usersAgreement.getCompanyId());
      resultMap.put("serviceTypeNames", serviceTypeNames);
    }

    return returnSuccess(resultMap);
  }

  private Map<String, Object> checkBaseSign(User user, UserRelated userRelated, String code,
      String mobileNo) {
    if (user == null || userRelated == null) {
      Integer state = RespCode.error306;
      return returnFail(state, RespCode.codeMaps.get(state));
    }

    boolean checkCode = checkCode(code, mobileNo);
    if (checkCode) {
      Integer state = RespCode.CODE_VILID_ERROR;
      return returnFail(state, RespCode.codeMaps.get(state));
    }
    return new HashMap<>();
  }

  private boolean checkCode(String code, String mobileNo) {
    boolean check = false;
    code.trim();
    Parameter param = parameterService.valiMobiletelno(mobileNo, code, flag);
    if (param == null) {
      check = true;
    }
    return check;
  }

  private List<UsersAgreement> getUsersAgreement(String userId, String originalId) {
    HashMap<String, Object> map = new HashMap<>(4);
    map.put("userId", userId);
    map.put("originalId", originalId);
    List<UsersAgreement> usersAgreementList = usersAgreementService
        .getUsersAgreementsByParams(map);
    return usersAgreementList;
  }

  private byte[] getFileByte(MultipartFile file) throws IOException {
    InputStream fileInputStream = file.getInputStream();
    byte[] bytes = new byte[1024];
    int readLen;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    while ((readLen = fileInputStream.read(bytes)) > -1) {
      out.write(bytes, 0, readLen);
    }
    return out.toByteArray();
  }

  private UsersAgreementDTO getTransferTemplate(byte[] frontFileBytes, byte[] backFileBytes,
      String mobileNo) {
    UsersAgreementDTO usersAgreementDTO = new UsersAgreementDTO();
    usersAgreementDTO.setSignSubmitType(SignSubmitType.H5.getCode());
    usersAgreementDTO.setMobilePhone(mobileNo);
    usersAgreementDTO.setUploadIdCard(true);
    usersAgreementDTO.setFrontFileByte(frontFileBytes);
    usersAgreementDTO.setBackFileByte(backFileBytes);
    usersAgreementDTO.setChannelSerialno("");
    return usersAgreementDTO;
  }

  private void startSingleThreadSignTwo(List<UsersAgreement> usersAgreementList,
      UsersAgreementDTO usersAgreementDTO) {
    String processId = MDC.get(PROCESS);
    ThreadUtil.cashThreadPool.execute(() -> {
      try {
        MDC.put(PROCESS, processId);
        for (UsersAgreement usersAgreement : usersAgreementList) {
          usersAgreementService.singleSignTwo(usersAgreement, usersAgreementDTO);
        }
      } catch (IOException e) {
        logger.error("签约图片异常...");
        logger.error(e.getMessage(), e);
      } finally {
        MDC.remove(PROCESS);
      }
    });
  }
}
