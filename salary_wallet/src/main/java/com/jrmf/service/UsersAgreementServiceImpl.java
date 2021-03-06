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
 * @author ?????????
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
  private static final String DAY_ERROR_MESSAGE = "???????????????????????????";
  private static final String MONTH_ERROR_MESSAGE = "???????????????????????????";

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
   * ?????????????????????????????????????????????
   *
   * @return ????????????list
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
   * ????????????????????????????????????
   */
  @Override
  public void updateUsersAgreement(UsersAgreement usersAgreement) {
    usersAgreementDao.updateUsersAgreement(usersAgreement);
  }

  /**
   * ??????userId??????ImageURL
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
    logger.info("????????????????????????????????????...");
    List<UsersAgreement> usersAgreementList = getUsersAgreementsByParams(map);
    if (usersAgreementList.isEmpty()) {
      usersAgreementDao.createAgreement(agreement);
    } else {
      if (agreement.getSignStatus() == UsersAgreementSignType.SIGN_FORBIDDEN.getCode()) {
        agreement.setSignStatus(UsersAgreementSignType.SIGN_SUCCESS.getCode());
        agreement.setSignStatusDes(UsersAgreementSignType.SIGN_SUCCESS.getDesc());
        usersAgreementDao.updateUsersAgreement(agreement);
      } else {
        logger.info("????????????????????????????????????id??????{}", agreement.getId());
      }
    }
  }

  /**
   * ??????????????????
   *
   * @param usersAgreement ????????????
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
   * @Description ?????????????????? ??????????????????
   * @Date 2020/11/9
   * @Param [params]
   **/
  @Override
  public HashMap getAgreementsByParamsAndStatistical(Map<String, Object> params) {

    HashMap hashMap = new HashMap();
    /**
     * @Description allUserNumber ???????????? signUsers:????????????  notSignUsers:???????????????
     **/
    Set allUserNumber = new HashSet();
    Set signUsers = new HashSet();
    /**
     * @Description notSinAgreements?????????????????????  successUsers ????????????????????????
     **/
    int notSinAgreements = 0;
    int successUsers = 0;

    /**
     * @Description ??????????????????????????????
     **/
    StringBuilder originalIds = new StringBuilder();
    StringBuilder companyIds = new StringBuilder();
    StringBuilder userIds = new StringBuilder();

    List<UsersAgreement> AllList = usersAgreementDao.getAgreementStatistical(params);
    /**
     * @Description ?????????????????????
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
   * ??????????????????????????????????????????
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
    logger.info("??????????????????????????????...");
    List<UsersAgreement> agreements = usersAgreementDao.getUsersAgreementsByParams(param);
    try {
      if (agreements.isEmpty()) {
        logger.info("??????????????????????????????????????????...");
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
      logger.error("????????????{}", e.getMessage());
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
   * ??????????????????
   *
   * @param ids ??????
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
   * @param params ??????
   * @return ??????????????????????????????
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
          logger.info("??????????????????" + path + "????????????" + mkdirs);
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
        logger.error("?????????????????????", e);
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

      //????????????????????????????????????????????????????????????????????????
      if (!usersAgreement.getHtmlTemplate().endsWith(Constant.COMPANY_AGREEMENT_FILE_SUFFIX)) {
        ChannelRelated channelRelated = channelRelatedDao
            .getRelatedByCompAndOrig(usersAgreement.getCustomkey(), usersAgreement.getCompanyId());
        Company company = companyService
            .getCompanyByUserId(Integer.valueOf(usersAgreement.getCompanyId()));
        if (company == null || company.getCompanyKey() == null) {
          logger.info("???????????????" + usersAgreement.getCompanyId() + "?????????key");
          continue;
        }
        String companyAgreementMappingKey =
            channelRelated.getMerchantId() + "_" + company.getCompanyKey();
        String companyAgreementMappingValue = agreementTemplateService
            .selectAgreementTemplate(companyAgreementMappingKey);
        if (companyAgreementMappingValue == null || "".equals(companyAgreementMappingValue)) {
          logger.info("???????????????" + usersAgreement.getCompanyId() + "?????????????????????????????????");
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
        logger.error("??????word?????????", e);
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

      //????????????????????????????????????????????????????????????????????????
      if (!usersAgreement.getHtmlTemplate().endsWith(Constant.COMPANY_AGREEMENT_FILE_SUFFIX)) {
        ChannelRelated channelRelated = channelRelatedDao
            .getRelatedByCompAndOrig(usersAgreement.getCustomkey(), usersAgreement.getCompanyId());
        Company company = companyService
            .getCompanyByUserId(Integer.valueOf(usersAgreement.getCompanyId()));
        if (company == null || company.getCompanyKey() == null) {
          logger.info("???????????????" + usersAgreement.getCompanyId() + "?????????key");
          continue;
        }
        String companyAgreementMappingKey =
            channelRelated.getMerchantId() + "_" + company.getCompanyKey();
        String companyAgreementMappingValue = agreementTemplateService
            .selectAgreementTemplate(companyAgreementMappingKey);
        if (companyAgreementMappingValue == null || "".equals(companyAgreementMappingValue)) {
          logger.info("???????????????" + usersAgreement.getCompanyId() + "?????????????????????????????????");
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
        logger.error("??????pdf?????????", e);
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
    String context = "?????????";
    String[] receivers = {receiver};
    String title = "??????????????????";
    try {
      FileInputStream inputStream = new FileInputStream(file);
      String path = "/download/receipt/";
      String filename = System.currentTimeMillis() + ".zip";
      FtpTool.uploadFile(path, filename, inputStream);
      String download = serverName + path + filename;
      context = context + "\n???????????????" + download;
    } catch (Exception e) {
      logger.error("??????????????????[{}]", e.getMessage());
      context = "??????????????????????????????????????????";
    }
    try {
      EmailUtil
          .send(url, password, host, receivers, title, context, null, "text/html;charset=GB2312");
    } catch (Exception e) {
      logger.error("??????????????????[{}]", e.getMessage());
    }
  }


  /**
   * ????????????????????????
   *
   * @param usersAgreements ??????
   * @param frontFileBytes ?????????
   * @param backFileBytes ?????????
   * @param mobileNo ?????????
   * @param channelSerialno @throws IOException ??????????????????
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

    logger.info("???????????????????????????????????????...");
    // api???????????????????????????????????????????????????uploadIdCard = false
    // ??????????????????????????????????????????????????????????????????
    if (uploadIdCard || (frontFileBytes != null && frontFileBytes.length != 0)) {
      logger.info("????????????????????????...");
      frontPic = createFile(filePath, frontFileBytes, frontFileName);
      frontUrl = uploadIdentityCard2(frontFileBytes, frontFileName, usersAgreements);
    }

    if (uploadIdCard || (backFileBytes != null && backFileBytes.length != 0)) {
      logger.info("????????????????????????...");
      backPic = createFile(filePath, backFileBytes, backFileName);
      backUrl = uploadIdentityCard2(backFileBytes, backFileName, usersAgreements);
    }

    for (UsersAgreement usersAgreement : usersAgreements) {
      if (usersAgreement.getSignStatus() == UsersAgreementSignType.SIGN_SUCCESS.getCode()) {
        logger.info("???????????????????????????, ????????????id???:{}", usersAgreement.getId());
        usersAgreement.setThirdNo(channelSerialno);
        usersAgreementDao.updateUsersAgreement(usersAgreement);
        continue;
      }

      usersAgreement.setImageURLA(frontUrl);
      usersAgreement.setImageURLB(backUrl);
      String agreementType = usersAgreement.getAgreementType();
      boolean thirdSign = THIRD_PART_SIGN.equals(agreementType);
      logger.info("????????????????????????:{}", thirdSign);
      AgreementTemplate agreementTemplate = agreementTemplateService
          .getAgreementTemplateById(usersAgreement.getAgreementTemplateId());

      if (agreementTemplate == null) {
        logger.error("??????{}????????????{}?????????", usersAgreement.getUserName(),
            usersAgreement.getAgreementTemplateId());
        continue;
      }

      signAgreement(agreementTemplate, usersAgreement, signSubmitType, channelSerialno, mobileNo,
          thirdSign, true, bankCardNo);

      if (!thirdSign && !uploadIdCard && usersAgreement.getSignStatus() != UsersAgreementSignType.SIGN_FAIL.getCode()) {
        logger.info("???????????????????????????????????????????????????????????????,???????????????...");
        usersAgreement.setDocumentStep(DocumentStep.DOCUMENT_CREATE.getCode());
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_SUCCESS.getCode());
        usersAgreement.setSignStatusDes("??????");
        usersAgreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
        updateUsersAgreement(usersAgreement);
      }

      if (usersAgreement.getSignStatus() != UsersAgreementSignType.SIGN_FAIL.getCode()
          && uploadIdCard) {
        logger.info("????????????----");
        uploadPic(agreementTemplate, usersAgreement, frontUrl, backUrl, thirdSign, backPic,
            frontPic);
      }

      if (usersAgreement.getSignStatus() != UsersAgreementSignType.SIGN_SUCCESS.getCode()) {
        logger.info("????????????????????????????????????????????????????????????-----");
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
   * ??????
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
        //TODO ?????????????????????????????????
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
    usersAgreement.setSignStatusDes("???????????????");
    usersAgreement.setMobilePhone(mobileNo);
    usersAgreement.setBankCardNo(bankCardNo);
    usersAgreement.setSignSubmitType(signSubmitType);
    usersAgreement.setThirdNo(channelSerialno);
    usersAgreement.setCheckLevel(CheckLevel.L0.name());//???????????????
    if (String.valueOf(AgreementTemplateSignType.LOCAL_SIGN.getCode())
        .equals(agreementTemplate.getAgreementType())) {
      usersAgreement.setCheckLevel(CheckLevel.L1.name());//????????????
    }
    if ((int)checkResult.get("code") == 1) {
      usersAgreement.setCheckLevel(CheckLevel.L2.name());//???????????????
    }
    usersAgreement
        .setCheckByPhoto(isCheckedByPhoto ? CheckByPhoto.YES.getCode() : CheckByPhoto.NO.getCode());
    logger.info("??????????????????????????????---");
    updateUsersAgreement(usersAgreement);
    upgradeUserCheckLevelIfHigher(usersAgreement);

    String extrOrderId = "bestSign" + usersAgreement.getOrderNo();

    if (thirdSign) {
      logger.info("?????????????????????");
      if (signStep != 1) {

        logger.info("????????????");
        String notifyUrl = bestSignConfig.getServerNameUrl() + BestSignType.RETURN_URL.getDesc()
            + "?usersAgreementId=" + usersAgreement.getId();
        eContractSingleSubmit(agreementTemplate, notifyUrl, extrOrderId, usersAgreement,
            bankCardNo);
      }
    } else {
      usersAgreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
      usersAgreement.setSignStatusDes("????????????????????????");
      usersAgreement.setLastUpdateTime("now");
      usersAgreement.setAgreementURL(usersAgreement.getAgreementTemplateURL());
      updateUsersAgreementSignStep(usersAgreement);
    }
  }

  @Override
  public void uploadPic(AgreementTemplate agreementTemplate, UsersAgreement usersAgreement,
      String frontUrl, String backUrl, Boolean thirdSign, File backPic, File frontPic) {
    int documentStep = usersAgreement.getDocumentStep();
    logger.info("????????????????????????" + documentStep);

    if (documentStep != 1) {

      logger.info("?????????????????????");

      Map<String, Object> hashMap = new HashMap<>(6);
      hashMap.put("id", usersAgreement.getId());
      hashMap.put("imageURLA", frontUrl);
      hashMap.put("imageURLB", backUrl);
      updateUsersAgreementImageURL(hashMap);

      logger.info("??????OCR????????????---");
      Map<String, Object> check = checkCertPictureByOCR(usersAgreement, backPic, frontPic);
      Object errorMsg = check.get("error_msg");
      documentStep = DocumentStep.DOCUMENT_SUCCESS.getCode();

      if (errorMsg != null) {
        String errorMassage = check.get("error_msg").toString();
        logger.info("??????????????????????????????" + errorMassage);
        documentStep = DocumentStep.DOCUMENT_FAIL.getCode();
        usersAgreement.setSignStatusDes(errorMassage);
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
      } else {

        if (thirdSign) {
          usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_PROCESSING.getCode());

          logger.info("???????????????????????????????????????");
          String notifyUrl = bestSignConfig.getServerNameUrl()
              + "/wallet/subscriber/bestsign/receive/picture_check/result.do?usersAgreementId="
              + usersAgreement.getId();
          //????????????????????????
          identityUpload(agreementTemplate, usersAgreement, notifyUrl, backPic, frontPic);
        } else {
          usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_SUCCESS.getCode());
          usersAgreement.setSignStatusDes("??????");
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
      logger.error("????????????????????????????????????????????????:{}", certId);
      result.put("error_msg", "??????????????????????????????");
      return result;
    }
    String expiredAt = result.get("expiredAt").toString();
    String longTime = "??????";
    if (!longTime.equals(expiredAt)) {
      DateFormat df = new SimpleDateFormat("yyyyMMdd");
      try {
        Date date = df.parse(expiredAt);
        if (date.compareTo(new Date()) < 0) {
          logger.error("?????????????????????????????? {}", expiredAt);
          result.put("error_msg", "??????????????????");
          return result;
        }
      } catch (ParseException e) {
        logger.error("?????????????????????????????????{}", expiredAt);
        result.put("error_msg", "???????????????????????????");
        return result;
      }
    }
    result = OCRUtil.getIdCardResult(FileUtils.getByteByFile(frontPic), OCRUtil.IDCARD_SIDE_FRONT);
    errorMsg = result.get("error_msg");
    if (errorMsg != null) {
      return result;
    }
    if (result.get("name") == null) {
      logger.error("???????????????????????????????????? {}", userName);
      result.put("error_msg", "??????????????????");
      return result;
    }
    String name = result.get("name").toString();
    if (!name.equals(userName)) {
      logger.error("?????????????????????????????????{}", name);
      result.put("error_msg", "???????????????");
      return result;
    }
    if (result.get("idNumber") == null) {
      logger.error("?????????????????????????????????????????? {}", certId);
      result.put("error_msg", "????????????????????????");
      return result;
    }
    String idNumber = result.get("idNumber").toString();
    if (!idNumber.equalsIgnoreCase(certId)) {
      logger.error("???????????????????????????????????????{}", idNumber);
      result.put("error_msg", "??????????????????");
      return result;
    }
    return result;
  }

  private File createFile(String filePath, byte[] fileBytes, String fileName) throws IOException {

    File fileParent = new File(filePath);
    if (!fileParent.exists()) {
      boolean mkDirs = fileParent.mkdirs();
      logger.info("????????????????????????" + mkDirs);
    }
    File frontPic = new File(filePath, fileName);
    if (!frontPic.exists()) {
      boolean newFile = frontPic.createNewFile();
      logger.info("???????????????" + newFile);
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
   * ????????????
   */
  @Override
  public void eContractSingleSubmit(AgreementTemplate agreementTemplate, String notifyUrl,
      String extrOrderId, UsersAgreement usersAgreement, String bankCardNo) {

    JSONObject jsonObject = getJsonParam(usersAgreement, notifyUrl, extrOrderId, bankCardNo,
        agreementTemplate);

    if (agreementTemplate.getChannelType() == 1) {
      agreementTemplate.setReqUrl(bestSignConfig.getBestSignURL());
      //??????????????????????????????????????????
      agreementTemplate.setPrivateKey(bestSignConfig.getSeckey().replaceAll("\n", ""));
    }

    PaymentConfig paymentConfig = followSignTypeGetPaymentConfig(usersAgreement, agreementTemplate);

    //??????????????????????????????
    SignContractChannel signContractChannel = SignContractChannelFactory
        .createChannel(agreementTemplate, paymentConfig);
    Map<String, String> respMap = signContractChannel.signContract(jsonObject.toJSONString());
    logger.info("????????????????????????" + respMap);
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
      logger.info("????????????????????????...");
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
        // 000014????????????????????????????????????????????????000018??????????????????
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
          logger.info("??????????????????????????????????????????:{}", agreementTemplate);
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
        // 000014????????????????????????????????????????????????000018??????????????????
        if (agreementTemplate.getChannelType() == ChannelTypeEnum.YI_MEI.getCode()) {
          pathNo = PaymentFactory.YFSH;
          paymentConfig = this.getPaymentConfigInfo(usersAgreement, pathNo);
        }
        if (paymentConfig == null) {
          logger.info("??????????????????????????????????????????:{}", agreementTemplate);
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
    logger.info("??????????????????:{}, ???????????????{}?????????:{}, ????????????:{}", paymentConfig, pathNo,
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
        //???????????????????????????????????????????????????????????????
        usersAgreement.setSignStatus(5);
        usersAgreement.setSignStep(1);
        usersAgreement.setSignStatusDes("??????");
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
   * ???????????????????????????
   */
  @Override
  public void identityUpload(AgreementTemplate agreementTemplate, UsersAgreement usersAgreement,
      String notifyUrl, File backFile, File frontFile) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("userName", usersAgreement.getUserName());
    jsonObject.put("certId", usersAgreement.getCertId());
    jsonObject.put("notifyUrl", notifyUrl);
    //??????????????????????????????????????????
    if (agreementTemplate.getChannelType() == 1) {
      //??????????????????????????????????????????
      agreementTemplate.setPrivateKey(bestSignConfig.getSeckey().replaceAll("\n", ""));
      agreementTemplate.setReqUrl(bestSignConfig.getBestSignURL());

      PaymentConfig paymentConfig = this
          .getPaymentConfigInfo(usersAgreement, PaymentFactory.HMZFTD);
      if (paymentConfig == null) {
        logger.info("??????????????????????????????????????????:{}", agreementTemplate);
        paymentConfig = new PaymentConfig();
        this.agreementTemplateInToPaymentConfig(paymentConfig, agreementTemplate);
      }
      if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())) {
        paymentConfig.setAppIdAyg(agreementTemplate.getThirdMerchId());
      }

      //??????????????????????????????
      SignContractChannel signContractChannel = SignContractChannelFactory
          .createChannel(agreementTemplate, paymentConfig);
      //???????????????????????????
      Map<String, String> respMap = signContractChannel
          .uploadPicInfo(jsonObject.toJSONString(), backFile, frontFile);
      logger.info("??????????????????????????????" + respMap);
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
      logger.info("?????????????????????????????????????????????....");
    }
  }


  @Override
  public void checkSignStatus(UsersAgreement usersAgreement) {
    Map<String, Object> hashMap = new HashMap<>(2);
    hashMap.put("id", usersAgreement.getId());
    List<UsersAgreement> usersAgreements = getUsersAgreementsByParams(hashMap);
    if (usersAgreements.isEmpty()) {
      logger.error("??????????????????????????????");
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
   * ????????????  ????????????
   *
   * @param status ??????  1?????????   0?????????
   * @param serialNo ?????????
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
      logger.error("???????????????????????????????????????" + serialNo);
      return;
    }
    String signStatus = "F";
    String signStatusDesc = usersAgreement.getSignStatusDes();
    if (status == 1) {
      signStatus = "S";
      signStatusDesc = "??????";
    }
    int notifyCount = callBackInfo.getNotifyCount();
    int maxCount = 10;
    if ((notifyCount < maxCount) && (callBackInfo.getStatus() != 1)) {
      notifyProcess(originalId, callBackInfo, signStatus, signStatusDesc);
    } else {
      logger.info("???????????????" + callBackInfo.toString());
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
   * ???????????????
   *
   * @param agreement ??????
   */
  private void updateWhiteList(UsersAgreement agreement) {
    usersAgreementDao.updateWhiteList(agreement);
  }

  /**
   * ??????api?????????  ???????????? ???????????? ???????????? ???????????? ?????????
   *
   * @param agreement ??????
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
   * ?????????????????????id
   *
   * @param params ??????
   * @return id
   */
  @Override
  public List<String> getUserAgreementsIdForPayCompanyByParam(Map<String, Object> params) {
    return usersAgreementDao.getUserAgreementsIdForPayCompanyByParam(params);
  }

  /**
   * ????????????????????? ????????????
   *
   * @param hashMap ????????????
   * @return ??????
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
          logger.error("???????????????{}", e.getMessage());
        }
        if (usersAgreement.getSignStatus() == UsersAgreementSignType.SIGN_SUCCESS.getCode()) {
          try {
            copyAgreement(usersAgreement, toAgreementTemplate);
          } catch (Exception e) {
            logger.error("???????????????{}", e.getMessage());
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
    // ????????????????????????????????????????????????????????????????????????
    ChannelCustom channelCustom = channelCustomService
        .getCustomByCustomkey(agreementTemplate.getOriginalId());
    if (channelCustom.getCustomType() == CustomType.COMPANY.getCode()) {
      agreement.setSignSubmitType(SignSubmitType.SERVICE_COMPANY.getCode());
    }
    usersAgreementDao.updateUsersAgreement(agreement);
    if (thirdSign) {
      if (agreement.getSignStep() != 1) {
        agreement.setSignStatus(UsersAgreementSignType.SIGN_PROCESSING.getCode());
        agreement.setSignStatusDes("???????????????");
        usersAgreementDao.updateUsersAgreement(agreement);
        logger.info("????????????");
        String notifyUrl = bestSignConfig.getServerNameUrl() + BestSignType.RETURN_URL.getDesc()
            + "?usersAgreementId=" + agreement.getId();
        String extrOrderId = "bestSign" + agreement.getOrderNo();
        eContractSingleSubmit(agreementTemplate, notifyUrl, extrOrderId, agreement, null);
      }
      if (agreement.getDocumentStep() != 1) {
        // ??????1327??????eContractSingleSubmit?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        agreement.setSignStatus(UsersAgreementSignType.SIGN_PROCESSING.getCode());
        agreement.setSignStatusDes("???????????????");
        usersAgreementDao.updateUsersAgreement(agreement);
        logger.info("?????????????????????");
        String serverNameUrl = bestSignConfig.getServerNameUrl();
        String notifyPictureUrl = serverNameUrl
            + "/wallet/subscriber/bestsign/receive/picture_check/result.do?usersAgreementId="
            + agreement.getId();
        //  ?????????????????????????????????????????????
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
   * ???????????????
   *
   * @return ?????????????????????-1??????????????????????????? 1??????????????????????????? 0
   */
  private Map<String,Object> checkUserNameAndCertIdInternal(String userName, String certId, String companyId,
      String originalId) {

    logger.info("????????????????????????>>>{}???????????????>>>{}?????????>>>{}?????????>>>{}", userName, certId, companyId, originalId);

    Map<String,Object> result = new HashMap<>();
    int checkUse = useUserNameCertIdCheckService(companyId, originalId);
    logger.info("????????????????????????????????????{}", checkUse);
    if (checkUse <= 0) {
      result.put("code",-1);
      result.put("msg","???????????????????????????");
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
   * ???????????????????????????
   *
   * @param userName ??????
   * @param certId ????????????
   * @param companyId ????????????
   * @param originalId ??????id
   * @return true ?????? false  ??????
   */
  @Override
  public Map<String,Object> checkUserNameAndCertId(String userName, String certId, String companyId,
      String originalId) {
    return checkUserNameAndCertIdInternal(userName, certId, companyId, originalId);
  }

  /**
   * ???????????????????????????????????????????????????????????????????????????????????????????????????
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
      logger.info("?????????????????????????????????????????????,", user);
      userDao.updateUserInfo(user);
    }
  }


  private int useUserNameCertIdCheckService(String companyId, String customkey) {

    logger.info("?????????????????????????????????>>{}?????????>>{}", companyId, customkey);

//      ?????????
    UserNameCertIdCheckBaseConfig userNameCertIdCheckBaseConfig = userNameCertIdCheckService
        .getUserNameCertIdCheckBaseConfigByCheckType(1);
    if (userNameCertIdCheckBaseConfig != null) {
      UserNameCertIdWhiteBlackConfig config = userNameCertIdCheckService
          .getUserNameCertIdWhiteAndBlackConfigByCustomkeyAndType(customkey, 1);
      if (config != null) {
        logger.info("??????????????????");
        return 0;
      }
    }
//      ?????????
    userNameCertIdCheckBaseConfig = userNameCertIdCheckService
        .getUserNameCertIdCheckBaseConfigByCheckType(2);
    if (userNameCertIdCheckBaseConfig != null) {
      UserNameCertIdWhiteBlackConfig config = userNameCertIdCheckService
          .getUserNameCertIdWhiteAndBlackConfigByCustomkeyAndType(customkey, 2);
      if (config != null) {
        logger.info("?????????????????????");
        checkCount(customkey, config);
        return 1;
      }
    }
//      ????????????
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
            logger.info("?????????????????????>>>{}????????????", proxyCustomKey);
            checkCount(proxyCustomKey, proxyBaseConfig);
            return 2;
          }
        }
      }
      UserNameCertIdCheckBaseConfig companyBaseConfig = userNameCertIdCheckService
          .getUserNameCertIdCheckBaseConfigByNotAllCheckCustomkey(companyId);
      if (companyBaseConfig != null) {
        logger.info("????????????????????????>>>{}????????????", companyId);
        checkCount(companyId, companyBaseConfig);
        return 3;
      }
      logger.info("?????????????????????");
      return 0;
    }
//      ????????????
    userNameCertIdCheckBaseConfig = userNameCertIdCheckService
        .getUserNameCertIdCheckBaseConfigByCheckType(4);
    if (userNameCertIdCheckBaseConfig != null) {
      logger.info("???????????? {}????????????", "ALL");
      checkCount("ALL", userNameCertIdCheckBaseConfig);
      return 4;
    }
    logger.info("???????????????????????????");
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
   * ??????redis??????
   *
   * @param key redis key
   * @return ??????
   */
  private int getCount(String key) {
    Object countObject = cacheManager.get(key);
    return countObject == null ? 0 : Integer.parseInt(countObject.toString());
  }

  private void compareLimitAndCount(int limit, int count, String message) {
    if (limit <= count) {
      logger.info("????????????---{}???????????????---{}", limit, count);
      throw new CheckUserNameCertIdCountException(message + "_" + limit);
    }
  }

  /**
   * ???????????????
   *
   * @return String   success   ?????? ??????      ??????
   */
  @Override
  public Map<String, String> checkTwoElements(String userName, String certId) {
    logger.info("start checkTwoElements...");
    String host = "https://idcert.market.alicloudapi.com";
    String path = "/idcard";
    Map<String, String> headers = new HashMap<>(2);
    //???????????????????????????
    headers.put("Authorization", "APPCODE " + appCode);
    Map<String, String> query = new HashMap<>(4);
    query.put("idCard", certId);
    query.put("name", userName);
    Map<String, String> result = new HashMap<>();
    try {
      HttpResponse httpResponse = HttpGetUtil.doGet(host, path, headers, query);
      StatusLine statusLine = httpResponse.getStatusLine();
//          ?????????: 200 ?????????400 URL?????????401 appCode????????? 403 ??????????????? 500 API????????????
      int statusCode = statusLine.getStatusCode();
      if (HttpStatus.SC_OK == statusCode) {
        JSONObject jsonObject = HttpGetUtil.getJson(httpResponse);
        String status = jsonObject.get("status").toString();
        String msg = jsonObject.get("msg").toString();
        if ("01".equals(status)) {
          result.put("status", "success");
        } else if ("202".equals(status)){
          result.put("status", "fail");
          result.put("msg", "?????????????????????[??????]???"+msg);
        } else {
          result.put("status","fail");
          result.put("msg","?????????????????????:"+msg);
        }
      } else {
        result.put("status", "fail");
        result.put("msg", "???????????????????????????:" + statusCode);
      }

    } catch (Exception e) {
      logger.error("??????????????????????????????{}", e.getMessage());
      result.put("status", "fail");
      result.put("msg", "???????????????????????????");
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
    logger.info("????????????...??????id:{}", usersAgreement.getId());
    Map<String, File> fileMap = new HashMap<>();
    Map<String, String> identityCardMap = new HashMap<>();

    if (usersAgreementDTO.isUploadIdCard()) {
      logger.info("???????????????...");
      fileMap = uploadIdCard(usersAgreementDTO);
      usersAgreementDTO.setFrontFile(fileMap.get("frontPic"));
      usersAgreementDTO.setBackFile(fileMap.get("backPic"));
      identityCardMap = getFileUrl(usersAgreementDTO, usersAgreement);
    }

    if (usersAgreement.getSignStatus() == UsersAgreementSignType.SIGN_SUCCESS.getCode()) {
      logger.info("????????????id{}:???????????????????????????..", usersAgreement.getId());
      usersAgreement.setThirdNo(usersAgreementDTO.getChannelSerialno());
      usersAgreementDao.updateUsersAgreement(usersAgreement);
      return;
    }

    try {
      setLicenseInfo(usersAgreement, identityCardMap);
      logger.info("????????????:{}, ????????????id:{}", usersAgreement.getAgreementType(),
          usersAgreement.getAgreementTemplateId());
      AgreementTemplate agreementTemplate = agreementTemplateService
          .getAgreementTemplateById(usersAgreement.getAgreementTemplateId());
      signAgreementTwo(agreementTemplate, usersAgreement, usersAgreementDTO);
      checkSignStatusUpdateUsersAgreement(agreementTemplate, usersAgreement, usersAgreementDTO);
      logger.info("??????????????????...??????id:{}", usersAgreement.getId());
    } catch (Exception e) {
      logger.error("????????????:{}", e);
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
    usersAgreement.setSignStatusDes("??????");
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

    //???????????????????????????????????????????????????????????????
    logger.info("???????????????---- ??????:{}", usersAgreement.getUserId());
    boolean elementFlag = elementCertification(agreementTemplate, usersAgreement,
        usersAgreementDTO);
    if (!elementFlag) {
      return;
    }

    updateUsersAgreementIsUnderway(usersAgreement, usersAgreementDTO);
    upgradeUserCheckLevelIfHigher(usersAgreement);

    if (AgreementTemplateSignType.THIRD_SIGN.getCode() == Integer
        .parseInt(usersAgreement.getAgreementType())) {
      logger.info("?????????????????????...");
      if (usersAgreement.getSignStep() != 1) {
        logger.info("????????????...");
        String extrOrderId = "bestSign" + usersAgreement.getOrderNo();
        String notifyUrl = bestSignConfig.getServerNameUrl() + BestSignType.RETURN_URL.getDesc()
            + "?usersAgreementId=" + usersAgreement.getId();
        eContractSingleSubmit(agreementTemplate, notifyUrl, extrOrderId, usersAgreement, null);
      }
    } else {
      usersAgreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
      usersAgreement.setSignStatusDes("????????????????????????");
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
        //TODO ?????????????????????????????????
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
    logger.info("??????????????????...");
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
    usersAgreement.setSignStatusDes("???????????????");
    usersAgreement.setMobilePhone(usersAgreementDTO.getMobilePhone());
    usersAgreement.setSignSubmitType(usersAgreementDTO.getSignSubmitType());
    usersAgreement.setThirdNo(usersAgreementDTO.getChannelSerialno());
    logger.info("??????????????????????????????...");
    updateUsersAgreement(usersAgreement);
  }

  private void checkSignStatusUpdateUsersAgreement(AgreementTemplate agreementTemplate,
      UsersAgreement usersAgreement, UsersAgreementDTO usersAgreementDTO) {

    if (usersAgreement.getSignStatus() != UsersAgreementSignType.SIGN_FAIL.getCode()) {
      if (AgreementTemplateSignType.THIRD_SIGN.getCode() != Integer
          .parseInt(usersAgreement.getAgreementType())
          && !usersAgreementDTO.isUploadIdCard()) {
        logger.info("??????????????????????????????????????????????????????????????????????????????...");
        updateUsersAgreementSuccess(usersAgreement);
      }

      if (usersAgreementDTO.isUploadIdCard()) {
        logger.info("????????????...");
        uploadPicTwo(agreementTemplate, usersAgreement, usersAgreementDTO);
      }
    }

    if (usersAgreement.getSignStatus() != UsersAgreementSignType.SIGN_SUCCESS.getCode()) {
      logger.info("??????????????????????????????????????????...");
      checkSignStatus(usersAgreement);
    }
  }

  private void uploadPicTwo(AgreementTemplate agreementTemplate, UsersAgreement usersAgreement,
      UsersAgreementDTO usersAgreementDTO) {
    int documentStep = usersAgreement.getDocumentStep();
    logger.info("????????????????????????" + documentStep);
    if (documentStep != 1) {
      logger.info("?????????????????????...");
      Map<String, Object> hashMap = new HashMap<>(6);
      hashMap.put("id", usersAgreement.getId());
      hashMap.put("imageURLA", usersAgreement.getImageURLA());
      hashMap.put("imageURLB", usersAgreement.getImageURLB());
      updateUsersAgreementImageURL(hashMap);

      logger.info("??????OCR????????????...");
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
      logger.info("??????????????????????????????" + errorMassage);
      documentStep = DocumentStep.DOCUMENT_FAIL.getCode();
      usersAgreement.setSignStatusDes(errorMassage);
      usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_FAIL.getCode());
    } else {
      if (Integer.parseInt(usersAgreement.getAgreementType())
          == AgreementTemplateSignType.THIRD_SIGN.getCode()) {
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_PROCESSING.getCode());
        logger.info("???????????????????????????????????????");
        String notifyUrl = bestSignConfig.getServerNameUrl()
            + "/wallet/subscriber/bestsign/receive/picture_check/result.do?usersAgreementId="
            + usersAgreement.getId();
        //????????????????????????
        identityUploadTwo(agreementTemplate, usersAgreement, notifyUrl, usersAgreementDTO);
      } else {
        usersAgreement.setSignStatus(UsersAgreementSignType.SIGN_SUCCESS.getCode());
        usersAgreement.setSignStatusDes("??????");
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
    //??????????????????????????????????????????
    if (agreementTemplate.getChannelType() == 1) {
      //??????????????????????????????????????????
      agreementTemplate.setPrivateKey(bestSignConfig.getSeckey().replaceAll("\n", ""));
      agreementTemplate.setReqUrl(bestSignConfig.getBestSignURL());

      PaymentConfig paymentConfig = this
          .getPaymentConfigInfo(usersAgreement, PaymentFactory.HMZFTD);
      if (paymentConfig == null) {
        logger.info("??????????????????????????????????????????:{}", agreementTemplate);
        paymentConfig = new PaymentConfig();
        this.agreementTemplateInToPaymentConfig(paymentConfig, agreementTemplate);
      }
      if (StringUtil.isEmpty(paymentConfig.getAppIdAyg())) {
        paymentConfig.setAppIdAyg(agreementTemplate.getThirdMerchId());
      }

      //??????????????????????????????
      SignContractChannel signContractChannel = SignContractChannelFactory
          .createChannel(agreementTemplate, paymentConfig);
      //???????????????????????????
      Map<String, String> respMap = signContractChannel
          .uploadPicInfo(jsonObject.toJSONString(), usersAgreementDTO.getBackFile(),
              usersAgreementDTO.getFrontFile());
      logger.info("??????????????????????????????" + respMap);
      identityUploadIfSuccessUpdateUsersAgreement(respMap, usersAgreement, jsonObject);
    } else {
      logger.info("?????????????????????????????????????????????....");
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
