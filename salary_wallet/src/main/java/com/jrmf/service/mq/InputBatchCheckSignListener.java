package com.jrmf.service.mq;

import com.google.code.yanf4j.util.ConcurrentHashSet;
import com.jrmf.controller.constant.InterimBatchStatus;
import com.jrmf.controller.constant.TempStatus;
import com.jrmf.domain.AgreementTemplate;
import com.jrmf.domain.ChannelInterimBatch;
import com.jrmf.domain.CommissionTemporary;
import com.jrmf.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class InputBatchCheckSignListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(InputBatchCheckSignListener.class);

    public static final String PROCESS = "process";

    @Autowired
    private ChannelInterimBatchService2 interimBatchService2;
    @Autowired
    private ChannelInterimBatchService2Impl channelInterimBatchService2;
    @Autowired
    private AgreementTemplateService agreementTemplateService;
    @Autowired
    private UserCommissionService userCommissionService;
    @Autowired
    private CustomLimitConfService customLimitConfService;

    @Override
    public void onMessage(Message message) {

        String processId = java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        MDC.put(PROCESS, processId);


        TextMessage noticeMessage = (TextMessage) message;

        try {
            String batchId = noticeMessage.getText();
            logger.info("签约共享批次落地mq的信息为 batchId:{}", batchId);

            ChannelInterimBatch batch = interimBatchService2.getChannelInterimBatchByOrderno(batchId, "");
            //防止出现重复校验情况
            if (batch != null && InterimBatchStatus.TOBECONTINUED.getCode() == batch.getStatus()) {

                AgreementTemplate template = agreementTemplateService.getAgreementPaymentTemplate(batch.getCustomkey(), batch.getRecCustomkey());

                List<CommissionTemporary> temporaryList = channelInterimBatchService2.getSuccessCommissionList(batchId);
                List<CommissionTemporary> signFailureList = new ArrayList<>();
                for (CommissionTemporary commissionTemporary : temporaryList) {

                    HashMap paramMap = new HashMap();
                    paramMap.put("certId", commissionTemporary.getIdCard());
                    paramMap.put("signStatus", "5");
                    paramMap.put("userName", commissionTemporary.getUserName());
                    paramMap.put("agreementTemplateId", template.getId());
                    //是否成功签约
                    int userAgreeCount = agreementTemplateService.getUserAgreementCountByParam(paramMap);

                    logger.info("校验用户是否签约 userName:{} certId:{} userAgreeCount:{}", commissionTemporary.getUserName(), commissionTemporary.getIdCard(), userAgreeCount);


                    if (userAgreeCount < 1) {
                        commissionTemporary.setStatus(TempStatus.FAILURE.getCode());
                        commissionTemporary.setStatusDesc("签约校验未通过，用户未创建或未签约");
                        signFailureList.add(commissionTemporary);
                    }

                }

                if (signFailureList.size() > 0) {
                    logger.info("存在签约共享失败的明细:{}", signFailureList);
                    channelInterimBatchService2.updateCommissionTemporarys(signFailureList);
                }

                boolean autoSupplement = customLimitConfService.autoSupplement(batch.getRecCustomkey(), batch.getCustomkey());
                Set<String> validateSet = new ConcurrentHashSet<>();
                userCommissionService.updateTemporaryBatchData(batch.getOrderno(), batch.getCustomkey(), batch.getRecCustomkey(), autoSupplement, validateSet);


                logger.info("-------------------mq批次信息生成结束 batchId：{}--------------------------", batchId);
            }


        } catch (Exception e) {
            logger.error("mq批次信息落地异常", e);
        } finally {
            MDC.remove(PROCESS);
        }

    }
}
