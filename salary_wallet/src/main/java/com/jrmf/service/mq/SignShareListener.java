package com.jrmf.service.mq;

import com.jrmf.controller.constant.*;
import com.jrmf.domain.*;
import com.jrmf.service.UsersAgreementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class SignShareListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(SignShareListener.class);

    public static final String PROCESS = "process";

    @Autowired
    private UsersAgreementService usersAgreementService;

    @Override
    public void onMessage(Message message) {

        String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        MDC.put(PROCESS, processId);

        ShareSignRequest request;
        try {
            request = (ShareSignRequest) ((ObjectMessage) message).getObject();
            logger.info("签约共享mq的信息为{}", request.toString());

            User user = request.getUser();
            int signSubmitType = request.getSignSubmitType();
            AgreementTemplate agreementTemplate = request.getAgreementTemplate();
            //共享签约成功的签约记录
            UsersAgreement sourceAgreement = request.getUsersAgreement();
            UsersAgreement agreement = usersAgreementService.getUsersAgreement(agreementTemplate.getId(), user.getId());

            logger.info("签约共享获取当前模板下用户是否存在签约记录 agreement：{}", agreement);

            boolean thirdSign = String.valueOf(AgreementTemplateSignType.THIRD_SIGN.getCode()).equals(agreementTemplate.getAgreementType());


            //未创建签约记录
            if (agreement == null || agreement.getId() == 0) {
                agreement = usersAgreementService.generateUsersAgreement(agreementTemplate, user.getId(), agreementTemplate.getOriginalId(), user.getUserName(), user.getCertId(), Integer.parseInt(user.getDocumentType()), "");
                usersAgreementService.createOrUpdateAgreement(agreement);
            }
            //获取成功签约记录的照片
            agreement.setImageURLA(sourceAgreement.getImageURLA());
            agreement.setImageURLB(sourceAgreement.getImageURLB());
            agreement.setCheckLevel(sourceAgreement.getCheckLevel());
            agreement.setCheckByPhoto(sourceAgreement.getCheckByPhoto());

            //本地签约直接更改为成功记录
            if (!thirdSign) {
                agreement.setSignStep(SignStep.SIGN_SUCCESS.getCode());
                agreement.setSignStatus(UsersAgreementSignType.SIGN_SUCCESS.getCode());
                agreement.setSignStatusDes(UsersAgreementSignType.SIGN_SUCCESS.getDesc());
                agreement.setAgreementURL(agreementTemplate.getAgreementTemplateURL());
                agreement.setSignSubmitType(signSubmitType);

                logger.info("执行本地签约方法 agreement：{} ", agreement);
                usersAgreementService.updateUsersAgreement(agreement);
            } else {
                logger.info("执行第三方签约方法 agreement：{} ", agreement);
                boolean isCheckedByPhoto=sourceAgreement.getCheckByPhoto()==CheckByPhoto.YES.getCode();
                usersAgreementService.signAgreement(agreementTemplate, agreement, signSubmitType, "", user.getMobilePhone(), thirdSign,isCheckedByPhoto, null);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            MDC.remove(PROCESS);
        }

    }
}
