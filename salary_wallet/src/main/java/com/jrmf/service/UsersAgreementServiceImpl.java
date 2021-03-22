package com.jrmf.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.jrmf.common.CommonString;
import com.jrmf.common.Constant;
import com.jrmf.controller.constant.*;
import com.jrmf.domain.*;
import com.jrmf.domain.dto.UsersAgreementDTO;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.openapi.param.econtract.ConstantsEnum;
import com.jrmf.payment.openapi.utils.FileUtils;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.persistence.QbInvoiceBaseDao;
import com.jrmf.persistence.SignShareDao;
import com.jrmf.persistence.UserDao;
import com.jrmf.persistence.UserRelatedDao;
import com.jrmf.persistence.UsersAgreementDao;
import com.jrmf.signContract.SignContractChannel;
import com.jrmf.signContract.SignContractChannelFactory;
import com.jrmf.taxsettlement.api.APIDockingManager;
import com.jrmf.taxsettlement.api.MerchantAPIDockingConfig;
import com.jrmf.taxsettlement.api.gateway.restful.APIDefinitionConstants;
import com.jrmf.taxsettlement.api.security.sign.SignWorker;
import com.jrmf.taxsettlement.api.security.sign.SignWorkers;
import com.jrmf.taxsettlement.api.util.HttpPostUtil;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.*;
import com.jrmf.utils.exception.CheckUserNameCertIdCountException;
import com.jrmf.utils.ocr.OCRUtil;
import com.jrmf.utils.threadpool.ThreadUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author 种路路
 * @create 2018-11-13 15:27
 * @desc
 **/
@Service("usersAgreementService")
public class UsersAgreementServiceImpl implements UsersAgreementService {

  private static Logger logger = LoggerFactory.getLogger(UsersAgreementServiceImpl.class);

  private final String THIRD_PART_SIGN = "2";

  public final static String PROCESS = "process";

  private static final String MONTH_CUSTOM_USERNAME_CERTID_CHECK_COUNT = "MCUCC";
  private static final String DAY_CUSTOM_USERNAME_CERTID_CHECK_COUNT = "DCUCC";
  private static final String APPEND = "_";
  private static final String DAY_ERROR_MESSAGE = "超过日验证次数限额";
  private static final String MONTH_ERROR_MESSAGE = "超过月验证次数限额";

  @Value("${alibaba.appcode}")
  String appCode;

  @Autowired
  private UserRelatedDao userRelatedDao;
  @Autowired
  private UsersAgreementDao usersAgreementDao;
  @Autowired
  private OrderNoUtil orderNoUtil;
  @Autowired
  private BaseInfo baseInfo;
  @Autowired
  private BestSignConfig bestSignConfig;
  @Autowired
  private CallBackInfoService callBackInfoService;
  @Autowired
  private APIDockingManager apiDockingManager;
  @Autowired
  private SignWorkers signWorkers;
  @Autowired
  private AgreementTemplateService agreementTemplateService;
  @Autowired
  private CompanyService companyService;
  @Autowired
  private UtilCacheManager cacheManager;
  @Autowired
  private UserDao userDao;
  @Autowired
  private ChannelCustomService channelCustomService;
  @Autowired
  private UserNameCertIdCheckService userNameCertIdCheckService;
  @Autowired
  private SignShareDao signShareDao;
  @Autowired
  private CustomThirdPaymentConfigServiceImpl customThirdPaymentConfigService;
  @Autowired
  private QbInvoiceBaseDao qbInvoiceBaseDao;
  @Autowired
  private ChannelRelatedDao channelRelatedDao;


  /**
   * 根据条件查询用户所以签约的记录
   *
   * @return 签约记录list
   */
  @Override
  public List<UsersAgreement> getUsersAgreementsByParams(Map<String, Object> map) {
    return usersAgreementDao.getUsersAgreementsByParams(map);
  }

  @Override
  public List<UsersAgreement> selectUsersAgreementsByParams(Map<String, Object> map) {
    return usersAgreementDao.selectUsersAgreementsByParams(map);
  }

  /**
   * 根据条件修改用户签约记录
   */
  @Override
  public void updateUsersAgreement(UsersAgreement usersAgreement) {
    usersAgreementDao.updateUsersAgreement(usersAgreement);
  }

  /**
   * 根据userId修改ImageURL
   */
  @Override
  public void updateUsersAgreementImageURL(Map<String, Object> hashMap) {
    usersAgreementDao.updateUsersAgreementImageURL(hashMap);
  }

  @Override
  public void createOrUpdateAgreement(UsersAgreement agreement) {
    String certId = agreement.getCertId();
    String userName = agreement.getUserName();
    String agreementTemplateId = agreement.getAgreementTemplateId();
    Map<String, Object> map = new HashMap<>(6);
    map.put("certId", certId);
    map.put("userName", userName);
    map.put("agreementTemplateId", agreementTemplateId);
    logger.info("再次查询签约记录是否存在...");
    List<UsersAgreement> usersAgreementList = getUsersAgreementsByParams(map);
    if (usersAgreementList.isEmpty()) {
      usersAgreementDao.createAgreement(agreement);
    } else {
      if (agreement.getSignStatus() == UsersAgreementSignType.SIGN_FORBIDDEN.getCode()) {
        agreement.setSignStatus(UsersAgreementSignType.SIGN_SUCCESS.getCode());
        agreement.setSignStatusDes(UsersAgreementSignType.SIGN_SUCCESS.getDesc());
        usersAgreementDao.updateUsersAgreement(agreement);
      } else {
        logger.info("创建签约明细失败，原协议id是；{}", agreement.getId());
      }
    }
  }

  /**
   * 删除用户协议
   *
   * @param usersAgreement 签约协议
   */
  @Override
  public void deleteUsersAgreement(UsersAgreement usersAgreement) {
    usersAgreementDao.deleteUsersAgreement(usersAgreement);
  }


  @Override
  public List<Map<String, Object>> getAgreementsByParams(Map<String, Object> params) {

    return usersAgreementDao.getAgreementsByParams(params);

  }

  /**
   * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
   * @Author YJY
   * @Description 用户协议查看 新增统计业务
   * @Date 2020/11/9
   * @Param [params]
   **/
  @Override
  public HashMap getAgreementsByParamsAndStatistical(Map<String, Object> params) {

    HashMap hashMap = new HashMap();
    /**
     * @Description allUserNumber 用户总量 signUsers:签约用户  notSignUsers:未签约用户
     **/
    Set allUserNumber = new HashSet();
    Set signUsers = new HashSet();
    /**
     * @Description notSinAgreements未签约协议数量  successUsers 下发成功用户数量
     **/
    int notSinAgreements = 0;
    int successUsers = 0;

    /**
     * @Description 查询下发数据所需字段
     **/
    StringBuilder originalIds = new StringBuilder();
    StringBuilder companyIds = new StringBuilder();
    StringBuilder userIds = new StringBuilder();

    List<UsersAgreement> AllList = usersAgreementDao.getAgreementStatistical(params);
    /**
     * @Description 对结果进行统计
     **/
    if (!CollectionUtils.isEmpty(AllList)) {

      for (UsersAgreement map : AllList) {
        if (StringUtils.isNotBlank(map.getCertId())) {
          allUserNumber.add(map.getCertId());
        }
        if (5 == map.getSignStatus()) {
          if (StringUtils.isNotBlank(map.getOriginalId())
              && originalIds.indexOf(map.getOriginalId()) == -1) {
            originalIds.append(map.getOriginalId() + ",");
          }
          if (StringUtils.isNotBlank(map.getCompanyId())
              && companyIds.indexOf(map.getCompanyId()) == -1) {
            companyIds.append(map.getCompanyId() + ",");
          }

          if (StringUtils.isNotBlank(map.getCertId())) {
            signUsers.add(map.getCertId());
          }

        } else {
          notSinAgreements++;
        }
      }
    }

    if (ObjectUtils.isEmpty(params.get("signStatus")) || "5"
        .equals(params.get("signStatus") + "")) {

      Map<String, Object> commission = new HashMap<>(3);
      commission.put("originalIds", originalIds.toString());
      commission.put("companyIds", companyIds.toString());
      commission.put("startDate", params.get("signDateStart"));
      commission.put("endDate", params.get("signDateEnd"));
      successUsers = usersAgreementDao.findCommissionCount(commission);
    }
    hashMap.put("allUserNumber", allUserNumber.size());
    hashMap.put("signUsers", signUsers.size());
    hashMap.put("notSignUsers", allUserNumber.size() - signUsers.size());
    hashMap.put("notSinAgreements", notSinAgreements);
    hashMap.put("successUsers", successUsers);
    return hashMap;
  }


  @Override
  public List<Map<String, Object>> getAgreementsForPlatform(Map<String, Object> params) {
    return usersAgreementDao.getAgreementsForPlatform(params);
  }

  @Override
  public void updateUsersAgreementSignStep(UsersAgreement usersAgreement) {
    usersAgreementDao.updateUsersAgreementSignStep(usersAgreement);
  }

  @Override
  public void updateUsersAgreementDocumentStep(UsersAgreement usersAgreement) {
    usersAgreementDao.updateUsersAgreementDocumentStep(usersAgreement);
  }

  @Override
  public List<Map<String, Object>> getUserAgreementsForPayCompanyByParam(Map<String, Object> map) {
    return usersAgreementDao.getUserAgreementsForPayCompanyByParam(map);
  }

  /**
   * 添加签约记录，如果存在就跳过
   */
  @Override
  public void addUserAgreement(AgreementTemplate agreementTemplate, int userId, String originalId,
      String userName, String certId, int documentType, String remark,
      SignSubmitType signSubmitType) {
    Map<String, Object> param = new HashMap<>(3);
    param.put("agreementTemplateId", agreementTemplate.getId());
    param.put("userId", userId + "");
    param.put("userName", userName);
    param.put("certId", certId);
    logger.info("查询签约记录是否存在...");
    List<UsersAgreement> agreements = usersAgreementDao.getUsersAgreementsByParams(param);
    try {
      if (agreements.isEmpty()) {
        logger.info("签约记录不存在，添加签约记录...");
        UsersAgreement agreement = generateUsersAgreement(agreementTemplate, userId, originalId,
            userName, certId, documentType, remark);
        if (signSubmitType != null) {
          agreement.setSignSubmitType(signSubmitType.getCode());
        }
        createOrUpdateAgreement(agreement);
      } else {
        for (UsersAgreement usersAgreement : agreements) {
          createOrUpdateAgreement(usersAgreement);
        }
      }

    } catch (Exception e) {
      logger.error("添加异常{}", e.getMessage());
    }
  }

  @Override
  public UsersAgreement generateUsersAgreement(AgreementTemplate agreementTemplate, int userId,
      String originalId, String userName, String certId, int documentType, String remark) {
    UsersAgreement agreement = new UsersAgreement();
    agreement.setSignStatus(UsersAgreementSignType.SIGN_CREATE.getCode());
    agreement.setSignStatusDes(UsersAgreementSignType.SIGN_CREATE.getDesc());
    agreement.setDocumentStep(DocumentStep.DOCUMENT_CREATE.getCode());
    agreement.setSignStep(SignStep.SIGN_CREATE.getCode());
    agreement.setAgreementName(agreementTemplate.getAgreementName());
    agreement.setAgreementTemplateURL(agreementTemplate.getAgreementTemplateURL());
    agreement.setAgreementNo(
        agreementTemplate.getId() + "_" + userId + "_" + orderNoUtil.getChannelSerialno());
    agreement.setAgreementTemplateId(agreementTemplate.getId() + "");
    agreement.setThirdTemplateId(agreementTemplate.getThirdTemplateId());
    agreement.setAgreementType(agreementTemplate.getAgreementType());
    agreement.setOrderNo(orderNoUtil.getChannelSerialno());
    agreement.setOriginalId(originalId);
    agreement.setUserId(userId + "");
    agreement.setUserName(userName.trim());
    agreement.setCertId(certId.trim());
    agreement.setDocumentType(documentType + "");
    agreement.setCompanyId(agreementTemplate.getCompanyId());
    agreement.setThirdMerchId(agreementTemplate.getThirdMerchId());
    agreement.setPreparedA(remark);
    return agreement;
  }

  /**
   * 批量审核通过
   *
   * @param ids 编号
   */
  @Override
  public int updateUsersAgreementByBatch(String ids) {
    int count = usersAgreementDao.updateUsersAgreementByBatch(ids);
    String[] idList = ids.split(",");
    for (String id : idList) {
      Map<String, Object> map = new HashMap<>(2);
      map.put("id", id);
      List<UsersAgreement> agreements = usersAgreementDao.getUsersAgreementsByParams(map);
      String thirdNo = agreements.get(0).getThirdNo();
      if (!StringUtil.isEmpty(thirdNo)) {
        CallBackInfo callBackInfo = callBackInfoService.getCallBackInfoBySerialNo(thirdNo);
        callBackInfo.setNotifyCount(0);
        callBackInfo.setThirdNo(thirdNo);
        callBackInfo.setStatus(0);
        callBackInfoService.addCallBackInfo(callBackInfo);
      }
    }
    return count;
  }

  /**
   * @param params 参数
   * @return 协议导出成功或者失败
   */
  @Override
  public Map<String, Object> exportAgreementByType(Map<String, Object> params) {
    Map<String, Object> model = new HashMap<>(4);
    int state = RespCode.success;

    List<Map<String, Object>> userAgreements = usersAgreementDao.getUserAgreementsByParam(params);
    if (userAgreements.isEmpty()) {
      state = RespCode.DO_NOT_HAVE_MATCHING_RESULTS;
    } else {
      String processId = MDC.get(PROCESS);
      ThreadUtil.pdfThreadPool.execute(() -> {
        MDC.put(PROCESS, processId);
        String type = (String) params.get("type");
        String email = (String) params.get("email");
        String domainName = baseInfo.getDomainName();
        String serverName = params.get("serverName").toString();

        String path = "/data/server/salaryboot/temp/agreement/" + System.currentTimeMillis() + "/";
        File dir = new File(path);
        if (!dir.exists()) {
          boolean mkdirs = dir.mkdirs();
          logger.info("创建文件夹：" + path + "。结果：" + mkdirs);
        }
        switch (type) {
          case "1":
            exportAgreementByPdf(userAgreements, email, path, domainName, serverName);
            MDC.remove(PROCESS);
            break;
          case "2":
            exportAgreementByWord(userAgreements, email, path, domainName, serverName);
            MDC.remove(PROCESS);
            break;
          default:
            exportAgreementByPicture(userAgreements, email, path, domainName, serverName);
            MDC.remove(PROCESS);
        }
      });
    }
    model.put(RespCode.RESP_STAT, state);
    model.put(RespCode.RESP_MSG, RespCode.codeMaps.get(state));
    return model;
  }

  private void exportAgreementByPicture(List<Map<String, Object>> userAgreements, String email,
      String path, String domainName, String serverName) {
    String processId = MDC.get(PROCESS);
    MDC.put(PROCESS, processId);
    for (Map<String, Object> userAgreement : userAgreements) {
      String imageURLA = (String) userAgreement.get("imageURLA");
      String imageURLB = (String) userAgreement.get("imageURLB");
      String userName = (String) userAgreement.get("userName");
      String certId = (String) userAgreement.get("certId");
      String front = path + userName + "_" + certId + "_front.jpg";
      String back = path + userName + "_" + certId + "_back.jpg";
      try {
        PicUtils.getPic(front, domainName + imageURLA);
        PicUtils.getPic(back, domainName + imageURLB);
      } catch (IOException e) {
        logger.error("生成图片异常：", e);
        return;
      }
    }
    zip(email, path, serverName);
  }

  private void exportAgreementByWord(List<Map<String, Object>> userAgreements, String email,
      String path, String domainName, String serverName) {
    for (Map<String, Object> userAgreementMap : userAgreements) {
      UsersAgreement usersAgreement = new UsersAgreement();
      usersAgreement.map2Object(userAgreementMap);
      String serviceTypeNames = qbInvoiceBaseDao
          .getServiceTypeNamesByCustomKeyAndCompanyId(usersAgreement.getCustomkey(),
              usersAgreement.getCompanyId());
      usersAgreement.setServiceTypeNames(serviceTypeNames == null ? "" : serviceTypeNames);
      usersAgreement.setDomainName(domainName);

      //为兼容历史业务数据，再查一下平台和服务公司的编码
      if (!usersAgreement.getHtmlTemplate().endsWith(Constant.COMPANY_AGREEMENT_FILE_SUFFIX)) {
        ChannelRelated channelRelated = channelRelatedDao
            .getRelatedByCompAndOrig(usersAgreement.getCustomkey(), usersAgreement.getCompanyId());
        Company company = companyService
            .getCompanyByUserId(Integer.valueOf(usersAgreement.getCompanyId()));
        if (company == null || company.getCompanyKey() == null) {
          logger.info("服务公司：" + usersAgreement.getCompanyId() + "未配置key");
          continue;
        }
        String companyAgreementMappingKey =
            channelRelated.getMerchantId() + "_" + company.getCompanyKey();
        String companyAgreementMappingValue = agreementTemplateService
            .selectAgreementTemplate(companyAgreementMappingKey);
        if (companyAgreementMappingValue == null || "".equals(companyAgreementMappingValue)) {
          logger.info("服务公司：" + usersAgreement.getCompanyId() + "未配置协议使用映射规则");
          continue;
        }
        usersAgreement
            .setHtmlTemplate(companyAgreementMappingValue + Constant.COMPANY_AGREEMENT_FILE_SUFFIX);
      }
      try {
        String html = HtmlUtil.replace(usersAgreement);
        String docFile =
            path + usersAgreement.getUserName() + "_" + usersAgreement.getCertId() + ".doc";
        HtmlUtil.parseHTML2WordFile(docFile, html);
      } catch (Exception e) {
        logger.error("生成word异常：", e);
        return;
      }
    }
    zip(email, path, serverName);
  }

  private void exportAgreementByPdf(List<Map<String, Object>> userAgreements, String email,
      String path, String domainName, String serverName) {
    for (Map<String, Object> userAgreementMap : userAgreements) {
      UsersAgreement usersAgreement = new UsersAgreement();
      usersAgreement.map2Object(userAgreementMap);
      String serviceTypeNames = qbInvoiceBaseDao
          .getServiceTypeNamesByCustomKeyAndCompanyId(usersAgreement.getCustomkey(),
              usersAgreement.getCompanyId());
      usersAgreement.setServiceTypeNames(serviceTypeNames == null ? "" : serviceTypeNames);
      usersAgreement.setDomainName(domainName);

      //为兼容历史业务数据，再查一下平台和服务公司的编码
      if (!usersAgreement.getHtmlTemplate().endsWith(Constant.COMPANY_AGREEMENT_FILE_SUFFIX)) {
        ChannelRelated channelRelated = channelRelatedDao
            .getRelatedByCompAndOrig(usersAgreement.getCustomkey(), usersAgreement.getCompanyId());
        Company company = companyService
            .getCompanyByUserId(Integer.valueOf(usersAgreement.getCompanyId()));
        if (company == null || company.getCompanyKey() == null) {
          logger.info("服务公司：" + usersAgreement.getCompanyId() + "未配置key");
          continue;
        }
        String companyAgreementMappingKey =
            channelRelated.getMerchantId() + "_" + company.getCompanyKey();
        String companyAgreementMappingValue = agreementTemplateService
            .selectAgreementTemplate(companyAgreementMappingKey);
        if (companyAgreementMappingValue == null || "".equals(companyAgreementMappingValue)) {
          logger.info("服务公司：" + usersAgreement.getCompanyId() + "未配置协议使用映射规则");
          continue;
        }
        usersAgreement
            .setHtmlTemplate(companyAgreementMappingValue + Constant.COMPANY_AGREEMENT_FILE_SUFFIX);
      }
      try {
        String html = HtmlUtil.replace(usersAgreement);
        String pdfFile =
            path + usersAgreement.getUserName() + "_" + usersAgreement.getCertId() + ".pdf";
        HtmlUtil.parseHTML2PDFFile(pdfFile, html);
      } catch (Exception e) {
        logger.error("生成pdf异常：", e);
        return;
      }
    }
    zip(email, path, serverName);
  }

  private void zip(String email, String path, String serverName) {
    try {
      String zip = "/data/server/salaryboot/temp/agreement/agreement.zip";
      ZipUtils.doCompress(path, zip);
      ThreadUtil.pdfThreadPool.execute(() -> send(email, new File(zip), serverName));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    } finally {
      MDC.remove(PROCESS);
    }
  }

  private void send(String receiver, File file, String serverName) {
    String url = "zstservice@jrmf360.com";
    String password = "Jrmf#2019";
    String host = "smtp.jrmf360.com";
    String context = "请查收";
    String[] receivers = {receiver};
    String title = "签约协议下载";
    try {
      FileInputStream inputStream = new FileInputStream(file);
      String path = "/download/receipt/";
      String filename = System.currentTimeMillis() + ".zip";
      FtpTool.uploadFile(path, filename, inputStream);
      String download = serverName + path + filename;
      context = context + "\n下载地址：" + download;
    } catch (Exception e) {
      logger.error("发送邮件异常[{}]", e.getMessage());
      context = "文件生成异常，请联系客服人员";
    }
    try {
      EmailUtil
          .send(url, password, host, receivers, title, context, null, "text/html;charset=GB2312");
    } catch (Exception e) {
      logger.error("发送邮件异常[{}]", e.getMessage());
    }
  }


  /**
   * 多线程改单线程。
   *
   * @param usersAgreements 签约
   * @param frontFileBytes 身份证
   * @param backFileBytes 身份证
   * @param mobileNo 手机号
   * @param channelSerialno @throws IOException 文件处理异常
   */
  @Override
  public void singleSign(List<UsersAgreement> usersAgreements, byte[] frontFileBytes,
      byte[] backFileBytes, String mobileNo, int signSubmitType, String channelSerialno,
      boolean uploadIdCard, String bankCardNo) throws IOException {
    String frontFileName = "front.jpg";
    String backFileName = "back.jpg";
    String dir = "/data/server/salaryboot/temp/";
    String filePath = dir + System.currentTimeMillis() + "/";
    String frontUrl = "";
    String backUrl = "";
    File frontPic = null;
    File backPic = null;

    logger.info("进入线程，开始签约前的准备...");
    // api签约如果签约模板配置为不上传身份证uploadIdCard = false
    // 但如果商户上送了身份证图片，也将上传到服务器
    if (uploadIdCard || (frontFileBytes != null && frontFileBytes.length != 0)) {
      logger.info("上传身份证人像面...");
      frontPic = createFile(filePath, frontFileBytes, frontFileName);
      frontUrl = uploadIdentityCard2(frontFileBytes, frontFileName, usersAgreements);
    }

    if (uploadIdCard || (backFileBytes != null && backFileBytes.length != 0)) {
      logger.info("上传身份证国徽面...");
      backPic = createFile(filePath, backFileBytes, backFileName);
      backUrl = uploadIdentityCard2(backFileBytes, backFileName, usersAgreements);
    }

    for (UsersAgreement usersAgreement : usersAgreements) {
      if (usersAgreement.getSignStatus() == UsersAgreementSignType.SIGN_SUCCESS.getCode()) {
        logger.info("已有签约成功的记录, 签约协议id为:{}", usersAgreement.getId());
        usersAgreement.setThirdNo(channelSerialno);
        usersAgreementDao.updateUsersAgreement(usersAgreement);
        continue;
      }

      usersAgreement.setImageURLA(frontUrl);
      usersAgreement.setImageURLB(backUrl);
      String agreementType = usersAgreement.getAgreementType();
      boolean thirdSign = THIRD_PART_SIGN.equals(agreementType);
      logger.info("是否为第三方签约:{}", thirdSign);
      AgreementTemplate agreementTemplate = agreementTemplateService
          .getAgreementTemplateById(usersAgreement.getAgreementTemplateId());

      if (agreementTemplate == null) {
        logger.error("用户{}签约模板{}不存在", usersAgreement.getUserName(),
            usersAgreement.getAgreementTemplateId());
        continue;
      }

      signAgreement(agreementTemplate, usersAgreement, signSubmitType, channelSerialno, mobileNo,
          thirdSign, true, bankCardNo);

      if (!thirdSign && !uploadIdCard && usersAgreement.getSignStatus() != UsersAgreementSignType.SIGN_FAIL.getCode()) {
        logger.info("本地签约且不用上传证件照信息且签约不为失败,则签约成功...");
        usersAgreement.setDocumentStep(DocumentStep.DOCUMENT_CREATE.getCode());
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_SUCCESS.getCode());
        usersAgreement.setSignStatusDes("成功");
        usersAgreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
        updateUsersAgreement(usersAgreement);
      }

      if (usersAgreement.getSignStatus() != UsersAgreementSignType.SIGN_FAIL.getCode()
          && uploadIdCard) {
        logger.info("上传图片----");
        uploadPic(agreementTemplate, usersAgreement, frontUrl, backUrl, thirdSign, backPic,
            frontPic);
      }

      if (usersAgreement.getSignStatus() != UsersAgreementSignType.SIGN_SUCCESS.getCode()) {
        logger.info("签约状态为非成功，则开始不成功的后续处理-----");
        checkSignStatus(usersAgreement);
      }
    }
  }

  private Map<String, String> uploadIdentityCard(byte[] frontFileBytes, byte[] backFileBytes,
      String frontFileName, String backFileName, List<UsersAgreement> usersAgreements) {
    Calendar cal = Calendar.getInstance();
    Date date = cal.getTime();
    String time = "" + date.getTime();
    String ftpURL = bestSignConfig.getFtpURL();
    String userName = bestSignConfig.getUsername();
    String password = bestSignConfig.getPassword();
    String pathName =
        "certPicture_" + usersAgreements.get(0).getOriginalId() + "_" + usersAgreements.get(0)
            .getUserId();
    String frontUrl = Base64Img
        .multipartImage(userName, password, ftpURL, frontFileBytes, pathName, time, frontFileName);
    String backUrl = Base64Img
        .multipartImage(userName, password, ftpURL, backFileBytes, pathName, time, backFileName);
    Map<String, String> map = new HashMap<>();
    map.put("frontUrl", frontUrl);
    map.put("backUrl", backUrl);
    return map;
  }

  private String uploadIdentityCard2(byte[] fileBytes, String fileName,
      List<UsersAgreement> usersAgreements) {
    Calendar cal = Calendar.getInstance();
    Date date = cal.getTime();
    String time = "" + date.getTime();
    String ftpURL = bestSignConfig.getFtpURL();
    String userName = bestSignConfig.getUsername();
    String password = bestSignConfig.getPassword();
    String pathName =
        "certPicture_" + usersAgreements.get(0).getOriginalId() + "_" + usersAgreements.get(0)
            .getUserId();
    String url = Base64Img
        .multipartImage(userName, password, ftpURL, fileBytes, pathName, time, fileName);
    return url;
  }

  /**
   * 签约
   */
  @Override
  public void signAgreement(AgreementTemplate agreementTemplate, UsersAgreement usersAgreement,
      int signSubmitType, String channelSerialno, String mobileNo, boolean thirdSign,
      boolean isCheckedByPhoto, String bankCardNo) {
    Map<String,Object> checkResult ;
    try {
      checkResult = checkUserNameAndCertIdInternal(usersAgreement.getUserName(),
          usersAgreement.getCertId(), usersAgreement.getCompanyId(),
          usersAgreement.getOriginalId());
      if ((int)checkResult.get("code") == 0) {
        //TODO 签约二要素信息返回补全
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
        usersAgreement.setSignStatusDes(String.valueOf(checkResult.get("msg")));
        usersAgreement.setMobilePhone(mobileNo);
        usersAgreement.setSignSubmitType(signSubmitType);
        usersAgreement.setThirdNo(channelSerialno);
        usersAgreement.setBankCardNo(bankCardNo);
        updateUsersAgreement(usersAgreement);
        return;
      }
    } catch (CheckUserNameCertIdCountException e) {
      logger.error(e.getMessage(), e);
      usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
      usersAgreement.setSignStatusDes(e.getRespmsg());
      usersAgreement.setMobilePhone(mobileNo);
      usersAgreement.setSignSubmitType(signSubmitType);
      usersAgreement.setThirdNo(channelSerialno);
      usersAgreement.setBankCardNo(bankCardNo);
      updateUsersAgreement(usersAgreement);
      return;
    }

    int signStep = usersAgreement.getSignStep();
    usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_PROCESSING.getCode());
    usersAgreement.setSignStatusDes("签约进行中");
    usersAgreement.setMobilePhone(mobileNo);
    usersAgreement.setBankCardNo(bankCardNo);
    usersAgreement.setSignSubmitType(signSubmitType);
    usersAgreement.setThirdNo(channelSerialno);
    usersAgreement.setCheckLevel(CheckLevel.L0.name());//初始未认证
    if (String.valueOf(AgreementTemplateSignType.LOCAL_SIGN.getCode())
        .equals(agreementTemplate.getAgreementType())) {
      usersAgreement.setCheckLevel(CheckLevel.L1.name());//本地认证
    }
    if ((int)checkResult.get("code") == 1) {
      usersAgreement.setCheckLevel(CheckLevel.L2.name());//二要素认证
    }
    usersAgreement
        .setCheckByPhoto(isCheckedByPhoto ? CheckByPhoto.YES.getCode() : CheckByPhoto.NO.getCode());
    logger.info("更新签约状态为处理中---");
    updateUsersAgreement(usersAgreement);
    upgradeUserCheckLevelIfHigher(usersAgreement);

    String extrOrderId = "bestSign" + usersAgreement.getOrderNo();

    if (thirdSign) {
      logger.info("调用第三方签约");
      if (signStep != 1) {

        logger.info("签约开始");
        String notifyUrl = bestSignConfig.getServerNameUrl() + BestSignType.RETURN_URL.getDesc()
            + "?usersAgreementId=" + usersAgreement.getId();
        eContractSingleSubmit(agreementTemplate, notifyUrl, extrOrderId, usersAgreement,
            bankCardNo);
      }
    } else {
      usersAgreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
      usersAgreement.setSignStatusDes("签约合同步骤完成");
      usersAgreement.setLastUpdateTime("now");
      usersAgreement.setAgreementURL(usersAgreement.getAgreementTemplateURL());
      updateUsersAgreementSignStep(usersAgreement);
    }
  }

  @Override
  public void uploadPic(AgreementTemplate agreementTemplate, UsersAgreement usersAgreement,
      String frontUrl, String backUrl, Boolean thirdSign, File backPic, File frontPic) {
    int documentStep = usersAgreement.getDocumentStep();
    logger.info("上传身份证状态：" + documentStep);

    if (documentStep != 1) {

      logger.info("上传身份证开始");

      Map<String, Object> hashMap = new HashMap<>(6);
      hashMap.put("id", usersAgreement.getId());
      hashMap.put("imageURLA", frontUrl);
      hashMap.put("imageURLB", backUrl);
      updateUsersAgreementImageURL(hashMap);

      logger.info("开始OCR识别认证---");
      Map<String, Object> check = checkCertPictureByOCR(usersAgreement, backPic, frontPic);
      Object errorMsg = check.get("error_msg");
      documentStep = DocumentStep.DOCUMENT_SUCCESS.getCode();

      if (errorMsg != null) {
        String errorMassage = check.get("error_msg").toString();
        logger.info("身份证审核失败原因：" + errorMassage);
        documentStep = DocumentStep.DOCUMENT_FAIL.getCode();
        usersAgreement.setSignStatusDes(errorMassage);
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
      } else {

        if (thirdSign) {
          usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_PROCESSING.getCode());

          logger.info("调用第三方签约证件信息上传");
          String notifyUrl = bestSignConfig.getServerNameUrl()
              + "/wallet/subscriber/bestsign/receive/picture_check/result.do?usersAgreementId="
              + usersAgreement.getId();
          //调用通道证件上传
          identityUpload(agreementTemplate, usersAgreement, notifyUrl, backPic, frontPic);
        } else {
          usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_SUCCESS.getCode());
          usersAgreement.setSignStatusDes("成功");
        }

        usersAgreement.setDocumentStep(documentStep);
        usersAgreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
      }
      updateUsersAgreement(usersAgreement);
    }
  }

  private Map<String, Object> checkCertPictureByOCR(UsersAgreement usersAgreement, File backPic,
      File frontPic) {
    String userName = usersAgreement.getUserName();
    String certId = usersAgreement.getCertId();
    Company company = companyService
        .getCompanyByUserId(Integer.parseInt(usersAgreement.getCompanyId()));
    String message = StringUtil.checkAge(certId, company.getMinAge(), company.getMaxAge());
    Map<String, Object> map = new HashMap<>(2);
    if (!StringUtil.isEmpty(message)) {
      map.put("error_msg", message);
      return map;
    }
    Map<String, Object> result = OCRUtil
        .getIdCardResult(FileUtils.getByteByFile(backPic), OCRUtil.IDCARD_SIDE_BACK);
    Object errorMsg = result.get("error_msg");
    if (errorMsg != null) {
      return result;
    }
    if (result.get("expiredAt") == null) {
      logger.error("身份证识别：获取身份证有效期失败:{}", certId);
      result.put("error_msg", "获取身份证有效期失败");
      return result;
    }
    String expiredAt = result.get("expiredAt").toString();
    String longTime = "长期";
    if (!longTime.equals(expiredAt)) {
      DateFormat df = new SimpleDateFormat("yyyyMMdd");
      try {
        Date date = df.parse(expiredAt);
        if (date.compareTo(new Date()) < 0) {
          logger.error("身份证识别：获取日期 {}", expiredAt);
          result.put("error_msg", "身份证已过期");
          return result;
        }
      } catch (ParseException e) {
        logger.error("身份证识别：解析日期：{}", expiredAt);
        result.put("error_msg", "日期错误，无法获取");
        return result;
      }
    }
    result = OCRUtil.getIdCardResult(FileUtils.getByteByFile(frontPic), OCRUtil.IDCARD_SIDE_FRONT);
    errorMsg = result.get("error_msg");
    if (errorMsg != null) {
      return result;
    }
    if (result.get("name") == null) {
      logger.error("身份证识别：获取姓名失败 {}", userName);
      result.put("error_msg", "获取姓名失败");
      return result;
    }
    String name = result.get("name").toString();
    if (!name.equals(userName)) {
      logger.error("身份证识别：获取姓名：{}", name);
      result.put("error_msg", "姓名不匹配");
      return result;
    }
    if (result.get("idNumber") == null) {
      logger.error("身份证识别：获取身份证号失败 {}", certId);
      result.put("error_msg", "获取身份证号失败");
      return result;
    }
    String idNumber = result.get("idNumber").toString();
    if (!idNumber.equalsIgnoreCase(certId)) {
      logger.error("身份证识别：获取身份证号：{}", idNumber);
      result.put("error_msg", "身份证不匹配");
      return result;
    }
    return result;
  }

  private File createFile(String filePath, byte[] fileBytes, String fileName) throws IOException {

    File fileParent = new File(filePath);
    if (!fileParent.exists()) {
      boolean mkDirs = fileParent.mkdirs();
      logger.info("创建文件夹状态：" + mkDirs);
    }
    File frontPic = new File(filePath, fileName);
    if (!frontPic.exists()) {
      boolean newFile = frontPic.createNewFile();
      logger.info("创建文件：" + newFile);
    }
    byte[] frontBytes = PicUtils.compressPic(fileBytes, 3000, 0.8);
    if (frontBytes == null) {
      throw new FileNotFoundException();
    }
    FileOutputStream frontStream = new FileOutputStream(frontPic);
    frontStream.write(frontBytes);
    frontStream.flush();
    frontStream.close();
    return frontPic;
  }


  /**
   * 单笔签约
   */
  @Override
  public void eContractSingleSubmit(AgreementTemplate agreementTemplate, String notifyUrl,
      String extrOrderId, UsersAgreement usersAgreement, String bankCardNo) {

    JSONObject jsonObject = getJsonParam(usersAgreement, notifyUrl, extrOrderId, bankCardNo,
        agreementTemplate);

    if (agreementTemplate.getChannelType() == 1) {
      agreementTemplate.setReqUrl(bestSignConfig.getBestSignURL());
      //爱员工私钥信息从属性文件获取
      agreementTemplate.setPrivateKey(bestSignConfig.getSeckey().replaceAll("\n", ""));
    }

    PaymentConfig paymentConfig = followSignTypeGetPaymentConfig(usersAgreement, agreementTemplate);

    //路由第三方签约实现类
    SignContractChannel signContractChannel = SignContractChannelFactory
        .createChannel(agreementTemplate, paymentConfig);
    Map<String, String> respMap = signContractChannel.signContract(jsonObject.toJSONString());
    logger.info("签约返回结果是：" + respMap);
    respMapIfFailUpdateUsersAgreement(respMap, usersAgreement, agreementTemplate);
  }

  private JSONObject getJsonParam(UsersAgreement usersAgreement, String notifyUrl,
      String extrOrderId, String bankCardNo, AgreementTemplate agreementTemplate) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("userName", usersAgreement.getUserName());
    jsonObject.put("certId", usersAgreement.getCertId());
    jsonObject.put("notifyUrl", notifyUrl);
    jsonObject.put("extrOrderId", extrOrderId);
    jsonObject.put("mobilePhone", usersAgreement.getMobilePhone());
    jsonObject.put("backPicUrl", usersAgreement.getImageURLA());
    jsonObject.put("frontPicUrl", usersAgreement.getImageURLB());
    jsonObject.put("bankCardNo", bankCardNo);
    jsonObject.put("uploadFlag", agreementTemplate.getUploadIdCard());
    Company company = companyService
        .getCompanyByUserId(Integer.parseInt(usersAgreement.getCompanyId()));
    jsonObject.put("serviceCompanyId", company.getServiceCompanyId());
    return jsonObject;
  }

  private PaymentConfig followSignTypeGetPaymentConfig(UsersAgreement usersAgreement,
      AgreementTemplate agreementTemplate) {
    PaymentConfig paymentConfig = new PaymentConfig();
    if (usersAgreement.getSignSubmitType() == SignSubmitType.SERVICE_COMPANY.getCode()) {
      logger.info("开始转包公司签约...");
      if (agreementTemplate.getChannelType() == ChannelTypeEnum.AI_YUAN_GONG.getCode()) {
        paymentConfig.setPathKeyType(PathKeyTypeEnum.COMPANY.getCode());
        paymentConfig.setPathNo(PaymentFactory.HMZFTD);
      } else if (agreementTemplate.getChannelType() == ChannelTypeEnum.YI_MEI.getCode()) {
        paymentConfig.setPathKeyType(PathKeyTypeEnum.CUSTOM.getCode());
        paymentConfig.setPathNo(PaymentFactory.YMFWSPAY);
      }

      paymentConfig = customThirdPaymentConfigService
          .getConfigBySubcontract(null, usersAgreement.getOriginalId(),
              usersAgreement.getCompanyId(), paymentConfig);
      if (paymentConfig == null) {
        // 000014通道如果未查询到配置信息，则使用000018通道配置信息
        if (agreementTemplate.getChannelType() == ChannelTypeEnum.YI_MEI.getCode()) {
          paymentConfig = new PaymentConfig();
          paymentConfig.setPathKeyType(PathKeyTypeEnum.CUSTOM.getCode());
          paymentConfig.setPathNo(PaymentFactory.YFSH);
          paymentConfig = customThirdPaymentConfigService
              .getConfigBySubcontract(null, usersAgreement.getOriginalId(),
                  usersAgreement.getCompanyId(), paymentConfig);
        }
        if (paymentConfig == null) {
          paymentConfig = new PaymentConfig();
          logger.info("路由未配置，使用模板配置信息:{}", agreementTemplate);
          this.agreementTemplateInToPaymentConfig(paymentConfig, agreementTemplate);
        }
      }
      if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())) {
        paymentConfig.setAppIdAyg(agreementTemplate.getThirdMerchId());
      }
    } else {
      String pathNo = "";
      if (agreementTemplate.getChannelType() == ChannelTypeEnum.AI_YUAN_GONG.getCode()) {
        pathNo = PaymentFactory.HMZFTD;
      } else if (agreementTemplate.getChannelType() == ChannelTypeEnum.YI_MEI.getCode()) {
        pathNo = PaymentFactory.YMFWSPAY;
      }

      paymentConfig = this.getPaymentConfigInfo(usersAgreement, pathNo);
      if (paymentConfig == null) {
        // 000014通道如果未查询到配置信息，则使用000018通道配置信息
        if (agreementTemplate.getChannelType() == ChannelTypeEnum.YI_MEI.getCode()) {
          pathNo = PaymentFactory.YFSH;
          paymentConfig = this.getPaymentConfigInfo(usersAgreement, pathNo);
        }
        if (paymentConfig == null) {
          logger.info("路由未配置，使用模板配置信息:{}", agreementTemplate);
          paymentConfig = new PaymentConfig();
          this.agreementTemplateInToPaymentConfig(paymentConfig, agreementTemplate);
        }
      }
      if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())) {
        paymentConfig.setAppIdAyg(agreementTemplate.getThirdMerchId());
      }
    }
    return paymentConfig;
  }

  private PaymentConfig getPaymentConfigInfo(UsersAgreement usersAgreement, String pathNo) {
    PaymentConfig paymentConfig = companyService
        .getPaymentConfigInfoPlus(String.valueOf(PayType.PINGAN_BANK.getCode()),
            usersAgreement.getOriginalId(), usersAgreement.getCompanyId(),
            usersAgreement.getCompanyId(), pathNo);
    logger.info("路由配置信息:{}, 通道编号：{}，商户:{}, 服务公司:{}", paymentConfig, pathNo,
        usersAgreement.getOriginalId(), usersAgreement.getCompanyId());
    return paymentConfig;
  }

  private void agreementTemplateInToPaymentConfig(PaymentConfig paymentConfig,
      AgreementTemplate agreementTemplate) {
    paymentConfig.setAppIdAyg(agreementTemplate.getThirdMerchId());
    paymentConfig.setApiKey(agreementTemplate.getApiKey());
    paymentConfig.setPreHost(agreementTemplate.getReqUrl());
    paymentConfig.setPayPrivateKey(agreementTemplate.getPrivateKey());
    paymentConfig.setPayPublicKey(agreementTemplate.getPublicKey());
  }

  private void respMapIfFailUpdateUsersAgreement(Map<String, String> respMap,
      UsersAgreement usersAgreement, AgreementTemplate agreementTemplate) {
    if (!respMap.get("code").equals("0000")) {
      if (agreementTemplate.getChannelType() != null && agreementTemplate.getChannelType() == 2
          && respMap.get("code").equals("1014")) {
        //溢美优付已存在签约成功记录所以更新签约成功
        usersAgreement.setSignStatus(5);
        usersAgreement.setSignStep(1);
        usersAgreement.setSignStatusDes("成功");
        usersAgreement.setLastUpdateTime(DateUtils.getNowDate());
        usersAgreementDao.updateUsersAgreement(usersAgreement);
      } else {
        String msg = respMap.get("msg");
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
        usersAgreement.setSignStep(SignStep.SIGN_FAIL.getCode());
        usersAgreement.setSignStatusDes(msg);
        usersAgreement.setLastUpdateTime("now");
        updateUsersAgreementSignStep(usersAgreement);
      }
    }
  }

  /**
   * 单笔提交身份证异步
   */
  @Override
  public void identityUpload(AgreementTemplate agreementTemplate, UsersAgreement usersAgreement,
      String notifyUrl, File backFile, File frontFile) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("userName", usersAgreement.getUserName());
    jsonObject.put("certId", usersAgreement.getCertId());
    jsonObject.put("notifyUrl", notifyUrl);
    //爱员工则需要进行证件信息上传
    if (agreementTemplate.getChannelType() == 1) {
      //爱员工私钥信息从属性文件获取
      agreementTemplate.setPrivateKey(bestSignConfig.getSeckey().replaceAll("\n", ""));
      agreementTemplate.setReqUrl(bestSignConfig.getBestSignURL());

      PaymentConfig paymentConfig = this
          .getPaymentConfigInfo(usersAgreement, PaymentFactory.HMZFTD);
      if (paymentConfig == null) {
        logger.info("路由未配置，使用模板配置信息:{}", agreementTemplate);
        paymentConfig = new PaymentConfig();
        this.agreementTemplateInToPaymentConfig(paymentConfig, agreementTemplate);
      }
      if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())) {
        paymentConfig.setAppIdAyg(agreementTemplate.getThirdMerchId());
      }

      //路由第三方签约实现类
      SignContractChannel signContractChannel = SignContractChannelFactory
          .createChannel(agreementTemplate, paymentConfig);
      //发起身份证上传请求
      Map<String, String> respMap = signContractChannel
          .uploadPicInfo(jsonObject.toJSONString(), backFile, frontFile);
      logger.info("上传身份证返回结果：" + respMap);
      if (respMap.get("code").equals("0000")) {
        String msg = jsonObject.getString("msg");
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
        usersAgreement.setDocumentStep(DocumentStep.DOCUMENT_FAIL.getCode());
        usersAgreement.setSignStatusDes(msg);
        Map<String, Object> hashMap = new HashMap<>(6);
        hashMap.put("id", usersAgreement.getId());
        hashMap.put("imageURLA", "");
        hashMap.put("imageURLB", "");
        updateUsersAgreementImageURL(hashMap);
        usersAgreement.setLastUpdateTime("now");
        updateUsersAgreementDocumentStep(usersAgreement);
      }
    } else {
      logger.info("溢美优付签约不需要上传证件信息....");
    }
  }


  @Override
  public void checkSignStatus(UsersAgreement usersAgreement) {
    Map<String, Object> hashMap = new HashMap<>(2);
    hashMap.put("id", usersAgreement.getId());
    List<UsersAgreement> usersAgreements = getUsersAgreementsByParams(hashMap);
    if (usersAgreements.isEmpty()) {
      logger.error("查询不到这个签约信息");
    } else {
      usersAgreement = usersAgreements.get(0);
      if (usersAgreement.getSignStep() == SignStep.SIGN_SUCCESS.getCode()
          && usersAgreement.getDocumentStep() == DocumentStep.DOCUMENT_SUCCESS.getCode()) {
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_SUCCESS.getCode());
        usersAgreement.setSignStatusDes("SUCCESS");
        usersAgreement.setLastUpdateTime("now");
        updateUsersAgreement(usersAgreement);
        if (!StringUtil.isEmpty(usersAgreement.getThirdNo())) {
          afterSignProcess(1, usersAgreement.getThirdNo());
        }

      }
    }
  }

  /**
   * 签约后的  通知处理
   *
   * @param status 状态  1，成功   0，失败
   * @param serialNo 序列号
   */
  @Override
  public void afterSignProcess(int status, String serialNo) {
    HashMap<String, Object> params = new HashMap<>(2);
    params.put("thirdNo", serialNo);
    List<UsersAgreement> agreements = getUsersAgreementsByParams(params);
    if (agreements.isEmpty()) {
      return;
    }
    for (UsersAgreement agreement : agreements) {
      if (SignSubmitType.API.getCode() == agreement.getSignSubmitType()) {
        agreement.setWhiteList(status);
        updateWhiteList(agreement);
      }
    }
    UsersAgreement usersAgreement = agreements.get(0);
    String originalId = usersAgreement.getOriginalId();
    CallBackInfo callBackInfo = callBackInfoService.getCallBackInfoBySerialNo(serialNo);
    if (callBackInfo == null) {
      logger.error("未查询到回调信息，流水号：" + serialNo);
      return;
    }
    String signStatus = "F";
    String signStatusDesc = usersAgreement.getSignStatusDes();
    if (status == 1) {
      signStatus = "S";
      signStatusDesc = "成功";
    }
    int notifyCount = callBackInfo.getNotifyCount();
    int maxCount = 10;
    if ((notifyCount < maxCount) && (callBackInfo.getStatus() != 1)) {
      notifyProcess(originalId, callBackInfo, signStatus, signStatusDesc);
    } else {
      logger.info("已通知过：" + callBackInfo.toString());
    }

  }

  @Override
  public void notifyProcess(String originalId, CallBackInfo callBackInfo, String signStatus,
      String signStatusDesc) {
    MerchantAPIDockingConfig dockingConfig = apiDockingManager
        .getMerchantAPIDockingConfig(originalId);
    String signType = dockingConfig.getSignType();
    Map<String, Object> outData = new HashMap<>(16);
    outData.put("serial_no", callBackInfo.getThirdNo());
    outData.put("deal_no", callBackInfo.getSerialNo());
    outData = getOutData(signStatus, signStatusDesc, dockingConfig, signType, outData);
    Map<String, Object> result = HttpPostUtil.httpPost(callBackInfo.getNotifyUrl(), outData);
    callBackInfo.setNotifyContent(JSON.toJSONString(outData));
    callBackInfo.setNotifyCount(callBackInfo.getNotifyCount() + 1);
    Integer code = (Integer) result.get("code");
    callBackInfo.setHttpStatus(code + "");
    String message = (String) result.get("message");
    int longestLength = 1000;
    if ((!StringUtil.isEmpty(message)) && message.length() > longestLength) {
      message = message.substring(0, 999);
    }
    callBackInfo.setHttpResult(message);
    if (ConstantsEnum.NOTIFY_RESULTCODE_SUCCESS.getCode().equals(message)) {
      callBackInfo.setStatus(1);
    }
    callBackInfoService.updateCallBackInfo(callBackInfo);
  }

  @Override
  public Map<String, Object> getOutData(String signStatus, String signStatusDesc,
      MerchantAPIDockingConfig dockingConfig, String signType, Map<String, Object> outData) {
    outData.put(APIDefinitionConstants.CFN_RET_CODE, signStatus);
    outData.put(APIDefinitionConstants.CFN_RET_MSG, signStatusDesc);
    outData.put(APIDefinitionConstants.CFN_SIGN_TYPE, signType);
    outData.put(APIDefinitionConstants.CFN_TIMESTAMP,
        new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));

    SignWorker generator = signWorkers.get(signType);
    String sign = "";
    try {
      sign = generator.generateSign(outData, dockingConfig.getSignGenerationKey());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    outData.put(APIDefinitionConstants.CFN_SIGN, sign);
    return outData;
  }

  /**
   * 更改白名单
   *
   * @param agreement 入参
   */
  private void updateWhiteList(UsersAgreement agreement) {
    usersAgreementDao.updateWhiteList(agreement);
  }

  /**
   * 修改api签约的  信息提交 提交方式 回调地址 回调次数 流水号
   *
   * @param agreement 协议
   */
  @Override
  public void updateApiSignDetail(UsersAgreement agreement) {
    usersAgreementDao.updateApiSignDetail(agreement);
  }

  @Override
  public int getWhiteListCount(String customKey, String companyId, String certId) {
    return usersAgreementDao.getWhiteListCount(customKey, companyId, certId);
  }

  /**
   * 查询符合条件的id
   *
   * @param params 入参
   * @return id
   */
  @Override
  public List<String> getUserAgreementsIdForPayCompanyByParam(Map<String, Object> params) {
    return usersAgreementDao.getUserAgreementsIdForPayCompanyByParam(params);
  }

  /**
   * 查询符合条件的 结果条数
   *
   * @param hashMap 查询条件
   * @return 条数
   */
  @Override
  public int getUsersAgreementsCountByParams(Map<String, Object> hashMap) {
    return usersAgreementDao.getUsersAgreementsCountByParams(hashMap);
  }

  @Override
  public void copyAgreementsByTemplateId(String fromTemplateId, String toTemplateId) {
    ThreadUtil.pdfThreadPool.execute(() -> {
      Map<String, Object> hashMap = new HashMap<>(2);
      hashMap.put("signStatus", UsersAgreementSignType.SIGN_SUCCESS.getCode());
      hashMap.put("agreementTemplateId", fromTemplateId);
      List<UsersAgreement> usersAgreementsByParams = usersAgreementDao
          .getUsersAgreementsByParams(hashMap);
      hashMap.clear();
      hashMap.put("id", toTemplateId);
      AgreementTemplate toAgreementTemplate = agreementTemplateService
          .getAgreementTemplateByParam(hashMap).get(0);
      for (UsersAgreement usersAgreement : usersAgreementsByParams) {
        UserRelated userRelated = new UserRelated();
        userRelated.setOriginalId(toAgreementTemplate.getOriginalId());
        userRelated.setUserId(Integer.parseInt(usersAgreement.getUserId()));
        userRelated.setUserNo("");
        userRelated.setCompanyId(toAgreementTemplate.getCompanyId());
        userRelated.setCreateTime(DateUtils.getNowDate());
        try {
          userRelatedDao.createUserRelated(userRelated);
        } catch (Exception e) {
          logger.error("添加异常，{}", e.getMessage());
        }
        if (usersAgreement.getSignStatus() == UsersAgreementSignType.SIGN_SUCCESS.getCode()) {
          try {
            copyAgreement(usersAgreement, toAgreementTemplate);
          } catch (Exception e) {
            logger.error("签约异常，{}", e.getMessage());
          }
        }
      }
    });

  }

  @Override
  public int getAgreementsByParamsCount(Map<String, Object> params) {
    return usersAgreementDao.getAgreementsByParamsCount(params);
  }

  @Override
  public int getUserAgreementsIdForPayCompanyByParamCount(Map<String, Object> params) {
    return usersAgreementDao.getUserAgreementsIdForPayCompanyByParamCount(params);
  }

  @Override
  public void copyAgreement(UsersAgreement usersAgreement, AgreementTemplate agreementTemplate)
      throws Exception {

    Map<String, Object> hashMap = new HashMap<>(4);
    String agreementType = agreementTemplate.getAgreementType();
    boolean thirdSign = THIRD_PART_SIGN.equals(agreementType);
    String certId = usersAgreement.getCertId();
    hashMap.put("certId", certId);
    hashMap.put("agreementTemplateId", agreementTemplate.getId());
    List<UsersAgreement> agreements = usersAgreementDao.getUsersAgreementsByParams(hashMap);
    UsersAgreement agreement;
    if (agreements.isEmpty()) {
      agreement = generateUsersAgreement(agreementTemplate,
          Integer.parseInt(usersAgreement.getUserId()), agreementTemplate.getOriginalId(),
          usersAgreement.getUserName(), certId, Integer.parseInt(usersAgreement.getDocumentType()),
          "");
      agreement.setMobilePhone(usersAgreement.getMobilePhone());
      createOrUpdateAgreement(agreement);
    } else {
      agreement = agreements.get(0);
    }
    agreement.setImageURLA(usersAgreement.getImageURLA());
    agreement.setImageURLB(usersAgreement.getImageURLB());
    agreement.setMobilePhone(usersAgreement.getMobilePhone());

    boolean isCheckByPhoto = StringUtils.isNotBlank(agreement.getImageURLA()) && StringUtils
        .isNotBlank(agreement.getImageURLB());
    agreement
        .setCheckByPhoto(isCheckByPhoto ? CheckByPhoto.YES.getCode() : CheckByPhoto.NO.getCode());
    agreement.setCheckLevel(usersAgreement.getCheckLevel());
    agreement.setSignSubmitType(SignSubmitType.MIGRATION.getCode());
    // 如果签约模板商户为服务公司，签约类型改为转包签约
    ChannelCustom channelCustom = channelCustomService
        .getCustomByCustomkey(agreementTemplate.getOriginalId());
    if (channelCustom.getCustomType() == CustomType.COMPANY.getCode()) {
      agreement.setSignSubmitType(SignSubmitType.SERVICE_COMPANY.getCode());
    }
    usersAgreementDao.updateUsersAgreement(agreement);
    if (thirdSign) {
      if (agreement.getSignStep() != 1) {
        agreement.setSignStatus(UsersAgreementSignType.SIGN_PROCESSING.getCode());
        agreement.setSignStatusDes("签约进行中");
        usersAgreementDao.updateUsersAgreement(agreement);
        logger.info("签约开始");
        String notifyUrl = bestSignConfig.getServerNameUrl() + BestSignType.RETURN_URL.getDesc()
            + "?usersAgreementId=" + agreement.getId();
        String extrOrderId = "bestSign" + agreement.getOrderNo();
        eContractSingleSubmit(agreementTemplate, notifyUrl, extrOrderId, agreement, null);
      }
      if (agreement.getDocumentStep() != 1) {
        // 如果1327行，eContractSingleSubmit方法，把调用第三方签约失败，签约状态修改为签约失败，但这里又会修改为签约进行中
        agreement.setSignStatus(UsersAgreementSignType.SIGN_PROCESSING.getCode());
        agreement.setSignStatusDes("签约进行中");
        usersAgreementDao.updateUsersAgreement(agreement);
        logger.info("调用第三方签约");
        String serverNameUrl = bestSignConfig.getServerNameUrl();
        String notifyPictureUrl = serverNameUrl
            + "/wallet/subscriber/bestsign/receive/picture_check/result.do?usersAgreementId="
            + agreement.getId();
        //  后期要动态路由到不同的签约平台
        String frontFileName = "front.jpg";
        String backFileName = "back.jpg";
        String frontFileUrl = serverNameUrl + usersAgreement.getImageURLA();
        String backFileUrl = serverNameUrl + usersAgreement.getImageURLB();
        agreement.setImageURLA(usersAgreement.getImageURLA());
        agreement.setImageURLB(usersAgreement.getImageURLB());
        byte[] frontFileBytes = getImageByte(frontFileUrl);
        byte[] backFileBytes = getImageByte(backFileUrl);

        String dir = "/data/server/salaryboot/temp/";
        String filePath = dir + System.currentTimeMillis() + "/";
        File frontPic = createFile(filePath, frontFileBytes, frontFileName);
        File backPic = createFile(filePath, backFileBytes, backFileName);

        identityUpload(agreementTemplate, agreement, notifyPictureUrl, backPic, frontPic);
      }
    } else {
      localSignSuccess(usersAgreement, agreement);
    }

  }

  private void localSignSuccess(UsersAgreement usersAgreement, UsersAgreement agreement) {
    agreement.setSignStatus(UsersAgreementSignType.SIGN_SUCCESS.getCode());
    agreement.setSignStatusDes(UsersAgreementSignType.SIGN_SUCCESS.getDesc());
    agreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
    agreement.setDocumentStep(DocumentStep.DOCUMENT_SUCCESS.getCode());
    agreement.setImageURLA(usersAgreement.getImageURLA());
    agreement.setImageURLB(usersAgreement.getImageURLB());
    agreement.setAgreementURL(usersAgreement.getAgreementTemplateURL());
    usersAgreementDao.updateUsersAgreement(agreement);
  }

  private byte[] getImageByte(String fileUrl) throws Exception {
    URL u = new URL(fileUrl);
    BufferedImage image = ImageIO.read(u);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "jpg", byteArrayOutputStream);
    byteArrayOutputStream.flush();
    return byteArrayOutputStream.toByteArray();
  }


  @Override
  public List<UsersAgreement> getUsersAgreementsByChannelType(Map<String, Object> params) {
    return usersAgreementDao.getUsersAgreementsByChannelType(params);
  }

  /**
   * 二要素校验
   *
   * @return 未实际校验返回-1，实际校验成功返回 1，实际校验失败返回 0
   */
  private Map<String,Object> checkUserNameAndCertIdInternal(String userName, String certId, String companyId,
      String originalId) {

    logger.info("二要素校验：姓名>>>{}，身份证号>>>{}，公司>>>{}，商户>>>{}", userName, certId, companyId, originalId);

    Map<String,Object> result = new HashMap<>();
    int checkUse = useUserNameCertIdCheckService(companyId, originalId);
    logger.info("二要素验证次数验证返回：{}", checkUse);
    if (checkUse <= 0) {
      result.put("code",-1);
      result.put("msg","二要素验证次数不足");
      return result;
    }

    Map<String,String> checkResult = checkTwoElements(userName, certId);
    String checkStatus =checkResult.get("status");


    Map<String, Object> map = new HashMap<>();
    map.put("userName", userName);
    map.put("certId", certId);
    List<User> users = userDao.getUserByParam(map);

    boolean checkSuccess = "success".equals(checkStatus);
    result.put("code",checkSuccess ? 1 : 0);
    result.put("msg",checkResult.get("msg"));


    if (!users.isEmpty()) {
      User user = users.get(0);
      user.setCheckTruth(checkSuccess ? 1 : 2);
      userDao.updateUserInfo(user);
    }

    UserNameCertIdCheck userNameCertIdCheck = new UserNameCertIdCheck();
    userNameCertIdCheck.setCertId(certId);
    userNameCertIdCheck.setCustomkey(originalId);
    userNameCertIdCheck.setUserName(userName);
    userNameCertIdCheck.setResult(checkSuccess ? 1 : 0);
    userNameCertIdCheck.setResultMessage(checkResult.get("msg"));
    userNameCertIdCheckService.addUserNameCertIdCheck(userNameCertIdCheck);
    int monthOfYear = LocalDate.now().getMonthValue();
    int dayOfMonth = LocalDate.now().getDayOfMonth();
    int lengthOfMonth = LocalDate.now().lengthOfMonth();
    String key = originalId;
    if (checkUse == 2) {
      ChannelCustom custom = channelCustomService.getCustomByCustomkey(originalId);
      key = custom.getAgentId();
    }
    if (checkUse == 3) {
      key = companyId;
    }
    if (checkUse == 4) {
      key = "ALL";
    }
    String monthKey =
        MONTH_CUSTOM_USERNAME_CERTID_CHECK_COUNT + APPEND + monthOfYear + APPEND + key;
    putAndIncrease(monthKey, lengthOfMonth);
    String dayKey = DAY_CUSTOM_USERNAME_CERTID_CHECK_COUNT + APPEND + dayOfMonth + APPEND + key;
    putAndIncrease(dayKey, 1);

    return result;
  }

  /**
   * 二要素对外暴露接口
   *
   * @param userName 姓名
   * @param certId 身份证号
   * @param companyId 下发公司
   * @param originalId 商户id
   * @return true 成功 false  失败
   */
  @Override
  public Map<String,Object> checkUserNameAndCertId(String userName, String certId, String companyId,
      String originalId) {
    return checkUserNameAndCertIdInternal(userName, certId, companyId, originalId);
  }

  /**
   * 根据签约记录升级用户的认证等级和是否证照校验（比原有等级低不升级）
   */
  private void upgradeUserCheckLevelIfHigher(UsersAgreement usersAgreement) {
    User user = userDao.getUserByUserNo(Integer.parseInt(usersAgreement.getUserId()));

    boolean needUpdate = false;
    if (StringUtils.isNotBlank(user.getCheckLevel())
        && usersAgreement.getCheckLevel().compareTo(user.getCheckLevel()) > 0) {
      user.setCheckLevel(usersAgreement.getCheckLevel());
      needUpdate = true;
    }

    if (usersAgreement.getCheckByPhoto() != null && user.getCheckByPhoto() != null
        && usersAgreement.getCheckByPhoto() > user.getCheckByPhoto()) {
      user.setCheckByPhoto(usersAgreement.getCheckByPhoto());
      needUpdate = true;
    }
    if (needUpdate) {
      logger.info("升级用户认证等级及是否证照认证,", user);
      userDao.updateUserInfo(user);
    }
  }


  private int useUserNameCertIdCheckService(String companyId, String customkey) {

    logger.info("二要素前置校验条件公司>>{}，商户>>{}", companyId, customkey);

//      黑名单
    UserNameCertIdCheckBaseConfig userNameCertIdCheckBaseConfig = userNameCertIdCheckService
        .getUserNameCertIdCheckBaseConfigByCheckType(1);
    if (userNameCertIdCheckBaseConfig != null) {
      UserNameCertIdWhiteBlackConfig config = userNameCertIdCheckService
          .getUserNameCertIdWhiteAndBlackConfigByCustomkeyAndType(customkey, 1);
      if (config != null) {
        logger.info("黑名单不校验");
        return 0;
      }
    }
//      白名单
    userNameCertIdCheckBaseConfig = userNameCertIdCheckService
        .getUserNameCertIdCheckBaseConfigByCheckType(2);
    if (userNameCertIdCheckBaseConfig != null) {
      UserNameCertIdWhiteBlackConfig config = userNameCertIdCheckService
          .getUserNameCertIdWhiteAndBlackConfigByCustomkeyAndType(customkey, 2);
      if (config != null) {
        logger.info("白名单验证次数");
        checkCount(customkey, config);
        return 1;
      }
    }
//      部分验证
    userNameCertIdCheckBaseConfig = userNameCertIdCheckService
        .getUserNameCertIdCheckBaseConfigByCheckType(3);
    if (userNameCertIdCheckBaseConfig != null) {
      ChannelCustom custom = channelCustomService.getCustomByCustomkey(customkey);
      if (!ObjectUtils.isEmpty(custom)) {
        String proxyCustomKey = custom.getAgentId();
        if (!StringUtil.isEmpty(proxyCustomKey)) {
          UserNameCertIdCheckBaseConfig proxyBaseConfig = userNameCertIdCheckService
              .getUserNameCertIdCheckBaseConfigByNotAllCheckCustomkey(proxyCustomKey);
          if (proxyBaseConfig != null) {
            logger.info("部分验证代理商>>>{}验证次数", proxyCustomKey);
            checkCount(proxyCustomKey, proxyBaseConfig);
            return 2;
          }
        }
      }
      UserNameCertIdCheckBaseConfig companyBaseConfig = userNameCertIdCheckService
          .getUserNameCertIdCheckBaseConfigByNotAllCheckCustomkey(companyId);
      if (companyBaseConfig != null) {
        logger.info("部分验证下发公司>>>{}验证次数", companyId);
        checkCount(companyId, companyBaseConfig);
        return 3;
      }
      logger.info("部分验证不校验");
      return 0;
    }
//      全部验证
    userNameCertIdCheckBaseConfig = userNameCertIdCheckService
        .getUserNameCertIdCheckBaseConfigByCheckType(4);
    if (userNameCertIdCheckBaseConfig != null) {
      logger.info("全部验证 {}验证次数", "ALL");
      checkCount("ALL", userNameCertIdCheckBaseConfig);
      return 4;
    }
    logger.info("没有配置不需要校验");
    return 0;

  }

  private void putAndIncrease(String key, int length) {
    Object countObject = cacheManager.get(key);
    if (countObject == null) {
      cacheManager.put(key, 1 + "", 3600 * 24 * length);
    } else {
      cacheManager.put(key, Integer.parseInt(countObject.toString()) + 1 + "", 3600 * 24 * length);
    }
  }

  private void checkCount(String key, UserNameCertIdConfig config) {
    int monthOfYear = LocalDate.now().getMonthValue();
    int dayOfMonth = LocalDate.now().getDayOfMonth();
    String dayKey = DAY_CUSTOM_USERNAME_CERTID_CHECK_COUNT + APPEND + dayOfMonth + APPEND + key;
    String monthKey =
        MONTH_CUSTOM_USERNAME_CERTID_CHECK_COUNT + APPEND + monthOfYear + APPEND + key;
    compareLimitAndCount(config.getDayLimit(), getCount(dayKey), DAY_ERROR_MESSAGE);
    compareLimitAndCount(config.getMonthLimit(), getCount(monthKey), MONTH_ERROR_MESSAGE);
  }

  /**
   * 获取redis数值
   *
   * @param key redis key
   * @return 数值
   */
  private int getCount(String key) {
    Object countObject = cacheManager.get(key);
    return countObject == null ? 0 : Integer.parseInt(countObject.toString());
  }

  private void compareLimitAndCount(int limit, int count, String message) {
    if (limit <= count) {
      logger.info("限制次数---{}，已用次数---{}", limit, count);
      throw new CheckUserNameCertIdCountException(message + "_" + limit);
    }
  }

  /**
   * 二要素验证
   *
   * @return String   success   成功 其余      失败
   */
  @Override
  public Map<String, String> checkTwoElements(String userName, String certId) {
    logger.info("start checkTwoElements...");
    String host = "https://idcert.market.alicloudapi.com";
    String path = "/idcard";
    Map<String, String> headers = new HashMap<>(2);
    //注意中间是英文空格
    headers.put("Authorization", "APPCODE " + appCode);
    Map<String, String> query = new HashMap<>(4);
    query.put("idCard", certId);
    query.put("name", userName);
    Map<String, String> result = new HashMap<>();
    try {
      HttpResponse httpResponse = HttpGetUtil.doGet(host, path, headers, query);
      StatusLine statusLine = httpResponse.getStatusLine();
//          状态码: 200 正常；400 URL无效；401 appCode错误； 403 次数用完； 500 API网管错误
      int statusCode = statusLine.getStatusCode();
      if (HttpStatus.SC_OK == statusCode) {
        JSONObject jsonObject = HttpGetUtil.getJson(httpResponse);
        String status = jsonObject.get("status").toString();
        String msg = jsonObject.get("msg").toString();
        if ("01".equals(status)) {
          result.put("status", "success");
        } else if ("202".equals(status)){
          result.put("status", "fail");
          result.put("msg", "二要素验证失败[库无]："+msg);
        } else {
          result.put("status","fail");
          result.put("msg","二要素验证失败:"+msg);
        }
      } else {
        result.put("status", "fail");
        result.put("msg", "二要素验证接口响应:" + statusCode);
      }

    } catch (Exception e) {
      logger.error("二要素验证接口异常：{}", e.getMessage());
      result.put("status", "fail");
      result.put("msg", "二要素验证接口异常");
    }
    logger.info("checkTwoElements return:"+result.toString());
    return result;
  }

  @Override
  public UsersAgreement getCompanySignShare(String companyId, Integer userId) {
    return usersAgreementDao.getCompanySignShare(companyId, userId);
  }

  @Override
  public UsersAgreement getCustomSignShare(String customKeys, int userId, String signLevel,
      int papersRequire) {
    return usersAgreementDao.getCustomSignShare(customKeys, userId, signLevel, papersRequire);
  }


  @Override
  public UsersAgreement getUsersAgreement(int agreementTemplateId, int userId) {
    return usersAgreementDao.getUsersAgreement(agreementTemplateId, userId);
  }

  @Override
  public UsersAgreement getAgreementsSignSuccess(String customkey, String recCustomkey, int id) {
    return usersAgreementDao.getAgreementsSignSuccess(customkey, recCustomkey, id);
  }

  @Override
  public void singleSignTwo(UsersAgreement usersAgreement,
      UsersAgreementDTO usersAgreementDTO) throws IOException {
    logger.info("执行签约...协议id:{}", usersAgreement.getId());
    Map<String, File> fileMap = new HashMap<>();
    Map<String, String> identityCardMap = new HashMap<>();

    if (usersAgreementDTO.isUploadIdCard()) {
      logger.info("上传身份证...");
      fileMap = uploadIdCard(usersAgreementDTO);
      usersAgreementDTO.setFrontFile(fileMap.get("frontPic"));
      usersAgreementDTO.setBackFile(fileMap.get("backPic"));
      identityCardMap = getFileUrl(usersAgreementDTO, usersAgreement);
    }

    if (usersAgreement.getSignStatus() == UsersAgreementSignType.SIGN_SUCCESS.getCode()) {
      logger.info("签约协议id{}:签约成功，修改状态..", usersAgreement.getId());
      usersAgreement.setThirdNo(usersAgreementDTO.getChannelSerialno());
      usersAgreementDao.updateUsersAgreement(usersAgreement);
      return;
    }

    try {
      setLicenseInfo(usersAgreement, identityCardMap);
      logger.info("签约类型:{}, 签约模板id:{}", usersAgreement.getAgreementType(),
          usersAgreement.getAgreementTemplateId());
      AgreementTemplate agreementTemplate = agreementTemplateService
          .getAgreementTemplateById(usersAgreement.getAgreementTemplateId());
      signAgreementTwo(agreementTemplate, usersAgreement, usersAgreementDTO);
      checkSignStatusUpdateUsersAgreement(agreementTemplate, usersAgreement, usersAgreementDTO);
      logger.info("签约执行结束...协议id:{}", usersAgreement.getId());
    } catch (Exception e) {
      logger.error("签约异常:{}", e);
      usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
      usersAgreement.setSignStatusDes(e.getMessage());
      updateUsersAgreement(usersAgreement);
      return;
    }
  }

  @Override
  public Map<String,Object> getlinkageSignProcessingCount(String batchId, String customKey, String companyId) {
    return usersAgreementDao.getlinkageSignProcessingCount(batchId, customKey, companyId);
  }

  private Map<String, File> uploadIdCard(UsersAgreementDTO usersAgreementDTO)
      throws IOException {
    Map<String, File> resultMap = new HashMap<>(4);
    String frontFileName = "front.jpg";
    String backFileName = "back.jpg";
    String dir = "/data/server/salaryboot/temp/";
    String filePath = dir + System.currentTimeMillis() + "/";
    File frontPic = createFile(filePath, usersAgreementDTO.getFrontFileByte(), frontFileName);
    File backPic = createFile(filePath, usersAgreementDTO.getBackFileByte(), backFileName);
    resultMap.put("frontPic", frontPic);
    resultMap.put("backPic", backPic);
    return resultMap;
  }

  private Map<String, String> getFileUrl(UsersAgreementDTO usersAgreementDTO,
      UsersAgreement usersAgreement) {
    return uploadIdentityCardTwo(usersAgreementDTO.getFrontFileByte(),
        usersAgreementDTO.getBackFileByte(),
        "front.jpg", "back.jpg", usersAgreement);
  }

  private Map<String, String> uploadIdentityCardTwo(byte[] frontFileBytes, byte[] backFileBytes,
      String frontFileName, String backFileName, UsersAgreement usersAgreement) {
    Calendar cal = Calendar.getInstance();
    Date date = cal.getTime();
    String time = "" + date.getTime();
    String ftpURL = bestSignConfig.getFtpURL();
    String userName = bestSignConfig.getUsername();
    String password = bestSignConfig.getPassword();
    String pathName =
        "certPicture_" + usersAgreement.getOriginalId() + "_" + usersAgreement.getUserId();
    String frontUrl = Base64Img
        .multipartImage(userName, password, ftpURL, frontFileBytes, pathName, time, frontFileName);
    String backUrl = Base64Img
        .multipartImage(userName, password, ftpURL, backFileBytes, pathName, time, backFileName);
    Map<String, String> map = new HashMap<>();
    map.put("frontUrl", frontUrl);
    map.put("backUrl", backUrl);
    return map;
  }

  private void updateUsersAgreementSuccess(UsersAgreement usersAgreement) {
    usersAgreement.setDocumentStep(DocumentStep.DOCUMENT_CREATE.getCode());
    usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_SUCCESS.getCode());
    usersAgreement.setSignStatusDes("成功");
    usersAgreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
    updateUsersAgreement(usersAgreement);
  }

  private void setLicenseInfo(UsersAgreement usersAgreement,
      Map<String, String> identityCardMap) {
    String frontUrl = identityCardMap.get("frontUrl");
    String backUrl = identityCardMap.get("backUrl");
    usersAgreement.setImageURLA(frontUrl);
    usersAgreement.setImageURLB(backUrl);
    usersAgreement.setCheckByPhoto(CheckByPhoto.YES.getCode());
  }

  private void signAgreementTwo(AgreementTemplate agreementTemplate,
      UsersAgreement usersAgreement, UsersAgreementDTO usersAgreementDTO) {

    //二要素认证校验，若已认证的情况不会重复认证
    logger.info("二要素认证---- 用户:{}", usersAgreement.getUserId());
    boolean elementFlag = elementCertification(agreementTemplate, usersAgreement,
        usersAgreementDTO);
    if (!elementFlag) {
      return;
    }

    updateUsersAgreementIsUnderway(usersAgreement, usersAgreementDTO);
    upgradeUserCheckLevelIfHigher(usersAgreement);

    if (AgreementTemplateSignType.THIRD_SIGN.getCode() == Integer
        .parseInt(usersAgreement.getAgreementType())) {
      logger.info("调用第三方签约...");
      if (usersAgreement.getSignStep() != 1) {
        logger.info("签约开始...");
        String extrOrderId = "bestSign" + usersAgreement.getOrderNo();
        String notifyUrl = bestSignConfig.getServerNameUrl() + BestSignType.RETURN_URL.getDesc()
            + "?usersAgreementId=" + usersAgreement.getId();
        eContractSingleSubmit(agreementTemplate, notifyUrl, extrOrderId, usersAgreement, null);
      }
    } else {
      usersAgreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
      usersAgreement.setSignStatusDes("签约合同步骤完成");
      usersAgreement.setLastUpdateTime("now");
      usersAgreement.setAgreementURL(usersAgreement.getAgreementTemplateURL());
      updateUsersAgreementSignStep(usersAgreement);
    }
  }

  private boolean elementCertification(AgreementTemplate agreementTemplate,
      UsersAgreement usersAgreement, UsersAgreementDTO usersAgreementDTO) {

    Map<String,Object> checkResult ;
    try {
      checkResult = checkUserNameAndCertIdInternal(usersAgreement.getUserName(),
          usersAgreement.getCertId(), usersAgreement.getCompanyId(),
          usersAgreement.getOriginalId());
      if ((int)checkResult.get("code") == 0) {
        //TODO 签约二要素信息返回补全
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
        usersAgreement.setSignStatusDes(String.valueOf(checkResult.get("msg")));
        usersAgreement.setMobilePhone(usersAgreementDTO.getMobilePhone());
        usersAgreement.setSignSubmitType(usersAgreementDTO.getSignSubmitType());
        usersAgreement.setThirdNo(usersAgreementDTO.getChannelSerialno());
        updateUsersAgreement(usersAgreement);
        return false;
      }
    } catch (CheckUserNameCertIdCountException e) {
      logger.error(e.getMessage(), e);
      usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
      usersAgreement.setSignStatusDes(e.getRespmsg());
      usersAgreement.setMobilePhone(usersAgreementDTO.getMobilePhone());
      usersAgreement.setSignSubmitType(usersAgreementDTO.getSignSubmitType());
      usersAgreement.setThirdNo(usersAgreementDTO.getChannelSerialno());
      updateUsersAgreement(usersAgreement);
      return false;
    }
    logger.info("修改认证等级...");
    updateCheckLevel(usersAgreement, agreementTemplate.getAgreementType(), (int)checkResult.get("code"));
    return true;
  }

  private void updateCheckLevel(UsersAgreement usersAgreement, String agreementType,
      Integer checkResult) {
    usersAgreement.setCheckLevel(CheckLevel.L0.name());
    if (String.valueOf(AgreementTemplateSignType.LOCAL_SIGN.getCode())
        .equals(agreementType)) {
      usersAgreement.setCheckLevel(CheckLevel.L1.name());
    }
    if (checkResult == 1) {
      usersAgreement.setCheckLevel(CheckLevel.L2.name());
    }
    updateUsersAgreement(usersAgreement);
  }

  private void updateUsersAgreementIsUnderway(UsersAgreement usersAgreement,
      UsersAgreementDTO usersAgreementDTO) {
    usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_PROCESSING.getCode());
    usersAgreement.setSignStatusDes("签约进行中");
    usersAgreement.setMobilePhone(usersAgreementDTO.getMobilePhone());
    usersAgreement.setSignSubmitType(usersAgreementDTO.getSignSubmitType());
    usersAgreement.setThirdNo(usersAgreementDTO.getChannelSerialno());
    logger.info("更新签约状态为进行中...");
    updateUsersAgreement(usersAgreement);
  }

  private void checkSignStatusUpdateUsersAgreement(AgreementTemplate agreementTemplate,
      UsersAgreement usersAgreement, UsersAgreementDTO usersAgreementDTO) {

    if (usersAgreement.getSignStatus() != UsersAgreementSignType.SIGN_FAIL.getCode()) {
      if (AgreementTemplateSignType.THIRD_SIGN.getCode() != Integer
          .parseInt(usersAgreement.getAgreementType())
          && !usersAgreementDTO.isUploadIdCard()) {
        logger.info("本地签约且签约模板配置不用上传证件照，修改为签约成功...");
        updateUsersAgreementSuccess(usersAgreement);
      }

      if (usersAgreementDTO.isUploadIdCard()) {
        logger.info("上传图片...");
        uploadPicTwo(agreementTemplate, usersAgreement, usersAgreementDTO);
      }
    }

    if (usersAgreement.getSignStatus() != UsersAgreementSignType.SIGN_SUCCESS.getCode()) {
      logger.info("签约状态不等于成功的后续处理...");
      checkSignStatus(usersAgreement);
    }
  }

  private void uploadPicTwo(AgreementTemplate agreementTemplate, UsersAgreement usersAgreement,
      UsersAgreementDTO usersAgreementDTO) {
    int documentStep = usersAgreement.getDocumentStep();
    logger.info("上传身份证状态：" + documentStep);
    if (documentStep != 1) {
      logger.info("上传身份证开始...");
      Map<String, Object> hashMap = new HashMap<>(6);
      hashMap.put("id", usersAgreement.getId());
      hashMap.put("imageURLA", usersAgreement.getImageURLA());
      hashMap.put("imageURLB", usersAgreement.getImageURLB());
      updateUsersAgreementImageURL(hashMap);

      logger.info("开始OCR识别认证...");
      Map<String, Object> check = checkCertPictureByOCR(usersAgreement,
          usersAgreementDTO.getBackFile(), usersAgreementDTO.getFrontFile());
      ocrIfSuccessUploadIdentity(check, usersAgreement, agreementTemplate, usersAgreementDTO);
    }
  }

  private void ocrIfSuccessUploadIdentity(Map<String, Object> check,
      UsersAgreement usersAgreement, AgreementTemplate agreementTemplate,
      UsersAgreementDTO usersAgreementDTO) {
    Object errorMsg = check.get("error_msg");
    int documentStep = DocumentStep.DOCUMENT_SUCCESS.getCode();

    if (errorMsg != null) {
      String errorMassage = check.get("error_msg").toString();
      logger.info("身份证审核失败原因：" + errorMassage);
      documentStep = DocumentStep.DOCUMENT_FAIL.getCode();
      usersAgreement.setSignStatusDes(errorMassage);
      usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
    } else {
      if (Integer.parseInt(usersAgreement.getAgreementType())
          == AgreementTemplateSignType.THIRD_SIGN.getCode()) {
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_PROCESSING.getCode());
        logger.info("调用第三方签约证件信息上传");
        String notifyUrl = bestSignConfig.getServerNameUrl()
            + "/wallet/subscriber/bestsign/receive/picture_check/result.do?usersAgreementId="
            + usersAgreement.getId();
        //调用通道证件上传
        identityUploadTwo(agreementTemplate, usersAgreement, notifyUrl, usersAgreementDTO);
      } else {
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_SUCCESS.getCode());
        usersAgreement.setSignStatusDes("成功");
      }
      usersAgreement.setDocumentStep(documentStep);
      usersAgreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
    }
    updateUsersAgreement(usersAgreement);
  }

  private void identityUploadTwo(AgreementTemplate agreementTemplate,
      UsersAgreement usersAgreement, String notifyUrl, UsersAgreementDTO usersAgreementDTO) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("userName", usersAgreement.getUserName());
    jsonObject.put("certId", usersAgreement.getCertId());
    jsonObject.put("notifyUrl", notifyUrl);
    //爱员工则需要进行证件信息上传
    if (agreementTemplate.getChannelType() == 1) {
      //爱员工私钥信息从属性文件获取
      agreementTemplate.setPrivateKey(bestSignConfig.getSeckey().replaceAll("\n", ""));
      agreementTemplate.setReqUrl(bestSignConfig.getBestSignURL());

      PaymentConfig paymentConfig = this
          .getPaymentConfigInfo(usersAgreement, PaymentFactory.HMZFTD);
      if (paymentConfig == null) {
        logger.info("路由未配置，使用模板配置信息:{}", agreementTemplate);
        paymentConfig = new PaymentConfig();
        this.agreementTemplateInToPaymentConfig(paymentConfig, agreementTemplate);
      }
      if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())) {
        paymentConfig.setAppIdAyg(agreementTemplate.getThirdMerchId());
      }

      //路由第三方签约实现类
      SignContractChannel signContractChannel = SignContractChannelFactory
          .createChannel(agreementTemplate, paymentConfig);
      //发起身份证上传请求
      Map<String, String> respMap = signContractChannel
          .uploadPicInfo(jsonObject.toJSONString(), usersAgreementDTO.getBackFile(),
              usersAgreementDTO.getFrontFile());
      logger.info("上传身份证返回结果：" + respMap);
      identityUploadIfSuccessUpdateUsersAgreement(respMap, usersAgreement, jsonObject);
    } else {
      logger.info("溢美优付签约不需要上传证件信息....");
    }
  }

  private void identityUploadIfSuccessUpdateUsersAgreement(Map<String, String> respMap,
      UsersAgreement usersAgreement, JSONObject jsonObject) {
    if (respMap.get("code").equals("0000")) {
      String msg = jsonObject.getString("msg");
      usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
      usersAgreement.setDocumentStep(DocumentStep.DOCUMENT_FAIL.getCode());
      usersAgreement.setSignStatusDes(msg);
      Map<String, Object> hashMap = new HashMap<>(6);
      hashMap.put("id", usersAgreement.getId());
      hashMap.put("imageURLA", "");
      hashMap.put("imageURLB", "");
      updateUsersAgreementImageURL(hashMap);
      usersAgreement.setLastUpdateTime("now");
      updateUsersAgreementDocumentStep(usersAgreement);
    }
  }
}
