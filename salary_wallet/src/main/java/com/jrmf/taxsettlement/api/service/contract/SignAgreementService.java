package com.jrmf.taxsettlement.api.service.contract;

import com.jrmf.controller.constant.AgreementPayment;
import com.jrmf.controller.constant.CompanyType;
import com.jrmf.controller.constant.SignSubmitType;
import com.jrmf.controller.constant.UsersAgreementSignType;
import com.jrmf.domain.AgreementTemplate;
import com.jrmf.domain.CallBackInfo;
import com.jrmf.domain.Company;
import com.jrmf.domain.User;
import com.jrmf.domain.UsersAgreement;
import com.jrmf.service.AgreementTemplateService;
import com.jrmf.service.CallBackInfoService;
import com.jrmf.service.CompanyService;
import com.jrmf.service.UserSerivce;
import com.jrmf.service.UsersAgreementService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.util.HexStringUtil;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chonglulu
 */
@ActionConfig(name = "签约协议")
public class SignAgreementService
		implements Action<SignAgreementServiceParams, SignAgreementServiceAttachment> {

    public final static String PROCESS = "process";
    private static final Logger logger = LoggerFactory.getLogger(SignAgreementService.class);
    private final UserSerivce userSerivce;
    private final AgreementTemplateService agreementTemplateService;
    private final UsersAgreementService usersAgreementService;
    private final CallBackInfoService callBackInfoService;
    private final OrderNoUtil orderNoUtil;

    @Autowired
    private CompanyService companyService;

    @Autowired
    public SignAgreementService(UserSerivce userSerivce, AgreementTemplateService agreementTemplateService, UsersAgreementService usersAgreementService, CallBackInfoService callBackInfoService, OrderNoUtil orderNoUtil) {
        this.userSerivce = userSerivce;
        this.agreementTemplateService = agreementTemplateService;
        this.usersAgreementService = usersAgreementService;
        this.callBackInfoService = callBackInfoService;
        this.orderNoUtil = orderNoUtil;
    }

    @Override
	public String getActionType() {
		return APIDefinition.SIGN_AGREEMENT.name();
	}

	@Override
	public ActionResult<SignAgreementServiceAttachment> execute(
            SignAgreementServiceParams actionParams) {
      logger.info("api签约开始...姓名:" + actionParams.getName() + ", 身份证号:" + actionParams.getCertificateNo() + ", 手机号:"
           + actionParams.getMobileNo());
        actionParams.setCertificateNo(actionParams.getCertificateNo().toUpperCase());
        String thirdNo = actionParams.getSerialNo();
        Map<String, Object> paramMap = new HashMap<>(8);
        paramMap.put("customkey",actionParams.getMerchantId());
        paramMap.put("thirdNo",thirdNo);
        List<CallBackInfo> list = callBackInfoService.getCallBackInfoByParams(paramMap);
        if(!list.isEmpty()){
            throw new APIDockingException(APIDockingRetCodes.SERIAL_NO_EXISTED.getCode(), APIDockingRetCodes.SERIAL_NO_EXISTED.getDesc());
        }

        String certificateImage = actionParams.getCertificateImage();
        String certificateImageBackground = actionParams.getCertificateImageBackground();
        byte[] front = HexStringUtil.hexStringToBytes(certificateImage);
        byte[] back = HexStringUtil.hexStringToBytes(certificateImageBackground);


        paramMap.clear();
        paramMap.put("userName", actionParams.getName());
        paramMap.put("certId", actionParams.getCertificateNo());
        paramMap.put("documentType", actionParams.getCertificateType());
        paramMap.put("active", "true");
        User user = userSerivce.getUsersCountByCard(paramMap);
        if(user == null){
            throw new APIDockingException(APIDockingRetCodes.USER_NOT_FOUND.getCode(), APIDockingRetCodes.USER_NOT_FOUND.getDesc());
        }
        //插入用户协议表
        paramMap.clear();
        //平台id
        paramMap.put("originalId", actionParams.getMerchantId());
        String allSignType = "ALL";
        if(!allSignType.equals(actionParams.getSignAgreementType())){
            paramMap.put("companyId", actionParams.getTransferCorpId());
        }

        List<AgreementTemplate> agreementTemplateList = agreementTemplateService.getAgreementTemplateByParam(paramMap);
        if(agreementTemplateList.isEmpty()){
            throw new APIDockingException(APIDockingRetCodes.AGREEMENT_TEMPLATE_NOT_FOUND.getCode(), APIDockingRetCodes.AGREEMENT_TEMPLATE_NOT_FOUND.getDesc());
        }

        boolean uploadIdCard = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (AgreementTemplate agreementTemplate : agreementTemplateList) {
          if (agreementTemplate.getUploadIdCard() == 1) {
            uploadIdCard = true;
          }
          logger.info("模板:" + agreementTemplate.getId() + ", 是否上传证件照:" + uploadIdCard);
            stringBuilder.append(agreementTemplate.getId()).append(",");
            usersAgreementService.addUserAgreement(agreementTemplate,user.getId(),actionParams.getMerchantId(),actionParams.getName(),actionParams.getCertificateNo(),Integer.parseInt(actionParams.getCertificateType()), "",SignSubmitType.API);
        }
        paramMap.put("userId", user.getId() + "");
        String agreementTemplateIds = stringBuilder.toString();
        paramMap.put("agreementTemplateIds", agreementTemplateIds.substring(0,agreementTemplateIds.length()-1));
        logger.info("查询用户所有签约协议记录， 签约模板为:{}，用户为:{}", agreementTemplateIds, user.getId() );
        List<UsersAgreement> agreements = usersAgreementService.getUsersAgreementsByParams(paramMap);
        if(agreements.isEmpty()){
            throw new APIDockingException(APIDockingRetCodes.AGREEMENT_TEMPLATE_NOT_FOUND.getCode(), APIDockingRetCodes.AGREEMENT_TEMPLATE_NOT_FOUND.getDesc());
        }
        int processingCode = UsersAgreementSignType.SIGN_PROCESSING.getCode();
        int preReviewCode = UsersAgreementSignType.SIGN_PRE_REVIEW.getCode();
        for (UsersAgreement agreement : agreements) {
            if (processingCode == agreement.getSignStatus() || preReviewCode == agreement.getSignStatus()) {
                throw new APIDockingException(APIDockingRetCodes.AGREEMENT_IS_SIGNING_OR_PRE_REVIEW.getCode(), APIDockingRetCodes.AGREEMENT_IS_SIGNING_OR_PRE_REVIEW.getDesc());
            }
        }
        int signSubmitType = SignSubmitType.API.getCode();
        String channelSerialno = orderNoUtil.getChannelSerialno();
        String notifyUrl = actionParams.getNotifyUrl();
        CallBackInfo callBackInfoBySerialNo = callBackInfoService.getCallBackInfoBySerialNo(channelSerialno);
        if(callBackInfoBySerialNo != null){
            throw new APIDockingException(APIDockingRetCodes.SERIAL_NO_EXISTED.getCode(), APIDockingRetCodes.SERIAL_NO_EXISTED.getDesc());
        }
        //创建回调表
        CallBackInfo callBackInfo = new CallBackInfo();
        callBackInfo.setNotifyUrl(notifyUrl);
        callBackInfo.setNotifyCount(0);
        callBackInfo.setSerialNo(channelSerialno);
        callBackInfo.setThirdNo(thirdNo);
        callBackInfo.setStatus(0);
        callBackInfo.setCustomkey(actionParams.getMerchantId());
        logger.info("新增到回调表...");
        callBackInfoService.addCallBackInfo(callBackInfo);
        String processId = MDC.get(PROCESS);

        boolean finalUploadIdCard = uploadIdCard;
        logger.info("多线程异步签约准备...");
        ThreadUtil.cashThreadPool.execute(() -> {
            try {
                MDC.put(PROCESS,processId);
                usersAgreementService.singleSign(agreements,front,back,actionParams.getMobileNo(),signSubmitType,channelSerialno, finalUploadIdCard, actionParams.getCardNo());
            } catch (IOException e) {
                logger.info("签约图片异常");
                logger.error(e.getMessage(), e);
            } finally {
                MDC.remove(PROCESS);
            }

          try {
            MDC.put(PROCESS,processId);
            logger.info("签约完成... 查询服务公司信息..");
            // 循环所有模板 是为了获取所有的服务公司
            // 如果是实际下发公司 则不用签约，如果是转包服务公司并且不是自己转包自己 则进行转包签约
            Optional.ofNullable(agreementTemplateList)
                .ifPresent(agreementTemplates -> {
                  agreementTemplates.forEach(agreementTemplate -> {
                    // 查询服务公司所属的实际服务公司
                    Company company = companyService
                        .getCompanyByUserId(Integer.parseInt(agreementTemplate.getCompanyId()));
                    logger.info("服务公司id:" + company.getUserId() + ", 真实服务公司id:" + company.getRealCompanyId() + ", 服务公司类型:"
                        + company.getCompanyType());
                    Optional.ofNullable(company)
                        .ifPresent(c -> {
                          Integer companyType = c.getCompanyType();
                          Integer companyId = Integer.parseInt(c.getRealCompanyId());
                          if (companyType == CompanyType.SUBCONTRACT.getCode()
                              && c.getUserId() != companyId) {
                            // 服务公司类型为转包类型，并且不是自己转包自己
                            user.setMobileNo(actionParams.getMobileNo());
                            subcontractCompanySign(c, user, actionParams.getMerchantId(), front, back, actionParams.getCardNo());
                          }
                        });
                  });
                });
          } catch (Exception e) {
            logger.info("API转包签约异常...", e);
          } finally {
            MDC.remove(PROCESS);
          }
        });

        SignAgreementServiceAttachment attachment = new SignAgreementServiceAttachment();
        attachment.setDealNo(channelSerialno);
        attachment.setSerialNo(thirdNo);
        return new ActionResult<>(attachment);

	}

  private void subcontractCompanySign(Company company, User user, String originalId, byte[] front, byte[] back, String bankCardNo) {
    Map<String, Object> paramMap = new HashMap<>(8);
    paramMap.put("originalId", company.getUserId());
    paramMap.put("companyId", company.getRealCompanyId());
    List<AgreementTemplate> agreementTemplateList = agreementTemplateService
        .getAgreementTemplateByParam(paramMap);
    Optional.ofNullable(agreementTemplateList)
        .ifPresent(templates -> {
          templates.forEach(template -> {
            boolean uploadIdCard = false;
            if (template.getUploadIdCard() == 1) {
              uploadIdCard = true;
            }
            logger.info("模板id：" + template.getId() + ",签约类型为:" + template.getAgreementPayment() + ", 是否上传证件照:" + uploadIdCard);
            if (String.valueOf(AgreementPayment.SIGN_FIRST.getCode())
                .equals(template.getAgreementPayment())) {
              logger.info("开始签约...");
              insertUsersAgreementAndSign(template, user, front, back, uploadIdCard, company.getUserId(), bankCardNo);
            }
          });
        });
    logger.info("-------------------转包签约完成---------------------------");
  }

  private void insertUsersAgreementAndSign(AgreementTemplate agreementTemplate, User user, byte[] front,
      byte[] back, boolean uploadIdCard, Integer companyId, String bankCardNo) {

    String channelSerialno = orderNoUtil.getChannelSerialno();
    logger.info("转包签约- 用户:{}, 订单号:{}", user.getCertId(), channelSerialno);
    usersAgreementService.addUserAgreement(agreementTemplate, user.getId(), String.valueOf(companyId),
            user.getUserName(), user.getCertId(), 1, "",
            SignSubmitType.SERVICE_COMPANY);
    Map<String, Object> paramMap = new HashMap<>(8);
    paramMap.put("userId", user.getId());
    paramMap.put("agreementTemplateIds", agreementTemplate.getId());
    List<UsersAgreement> usersAgreements = new ArrayList<>();
    List<UsersAgreement> agreements = usersAgreementService.getUsersAgreementsByParams(paramMap);
    if (!agreements.isEmpty()) {
      for (UsersAgreement agreement : agreements) {
        if (UsersAgreementSignType.SIGN_PROCESSING.getCode() == agreement.getSignStatus()
            || UsersAgreementSignType.SIGN_PRE_REVIEW.getCode() == agreement.getSignStatus()) {
          logger.error("协议签署中或者正在审核中,签约协议为:{}", agreement.getId());
          continue;
        }
        usersAgreements.add(agreement);
      }
    }
    try {
      usersAgreementService.singleSign(usersAgreements, front, back, user.getMobileNo(),
          SignSubmitType.SERVICE_COMPANY.getCode(), channelSerialno, uploadIdCard, bankCardNo);
    } catch (IOException e) {
      logger.info("签约图片异常");
      logger.error(e.getMessage(), e);
    }

  }
}
