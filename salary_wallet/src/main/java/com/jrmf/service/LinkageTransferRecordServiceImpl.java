package com.jrmf.service;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.*;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.*;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.persistence.LinkageTransferRecordDao;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LinkageTransferRecordServiceImpl implements LinkageTransferRecordService {

    private static Logger logger = LoggerFactory.getLogger(LinkageTransferRecordServiceImpl.class);

    @Autowired
    private LinkageTransferRecordDao linkageTransferRecordDao;
    @Autowired
    private ChannelHistoryService channelHistoryService;
    @Autowired
    private LinkageCustomConfigService linkageCustomConfigService;
    @Autowired
    private UtilCacheManager utilCacheManager;

    @Override
    public int insert(LinkageTransferRecord record) {
        return linkageTransferRecordDao.insert(record);
    }


    @Override
    public void addRechargeLinkageTransfer(String orderNo) {

        logger.info("---------------充值联动调账开始,orderNo:{}---------------", orderNo);

        try {

            ChannelHistory rechargeInfo = channelHistoryService.getChannelHistoryByOrderno(orderNo);

            LinkageBaseConfig baseConfig = linkageCustomConfigService.getConfigByCustomKey(rechargeInfo.getCustomkey(), LinkageType.RECHARGENO.getCode());

            LinkageTransferRecord linkageTransferRecord = new LinkageTransferRecord(rechargeInfo, baseConfig);
            linkageTransferRecord.setTranType(LinkageTranType.MAINACCOUNT.getCode());

            linkageTransferRecord.setStatus(LinkageTranStatus.UNKNOW.getCode());

            //暂时写死服务费字段
            linkageTransferRecord.setTranRemark("服务费");
            linkageTransferRecordDao.insert(linkageTransferRecord);


            if (baseConfig != null && !StringUtil.isEmpty(baseConfig.getPathNo())) {
                PaymentConfig paymentConfig = new PaymentConfig(baseConfig);
                //调用支付通道工厂模式
                Payment payment = PaymentFactory.paymentEntity(paymentConfig);
                //不使用交易方法，不传递UtilCacheManager
                PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, utilCacheManager);
                Payment proxy = paymentProxy.getProxy();

                PaymentReturn<String> paymentReturn = proxy.linkageTransfer(linkageTransferRecord);

                logger.error("----------充值联动通道返回参数:{}--------------", paymentReturn);

                if (PayRespCode.RESP_SUCCESS.equals(paymentReturn.getRetCode())
                        || PayRespCode.RESP_UNKNOWN.equals(paymentReturn.getRetCode())) {
                    String bankOrderNo = paymentReturn.getAttachment();
                    linkageTransferRecord.setSelOrderNo(bankOrderNo);
                    linkageTransferRecord.setStatus(LinkageTranStatus.PAYING.getCode());
                    rechargeInfo.setStatus(RechargeStatusType.RECHARGEING.getCode());
                } else {
                    linkageTransferRecord.setStatus(LinkageTranStatus.FAILURE.getCode());
                    linkageTransferRecord.setStatusDesc(paymentReturn.getFailMessage());
                    rechargeInfo.setStatus(RechargeStatusType.RECHARGEFAILURE.getCode());
                }

            } else {

                logger.error("----------联动交易基础配置不存在,orderNo:{}--------------", orderNo);

                linkageTransferRecord.setStatus(LinkageTranStatus.FAILURE.getCode());
                linkageTransferRecord.setStatusDesc(RespCode.NOT_OPEN_LINKAGE);

                rechargeInfo.setStatus(RechargeStatusType.NORECHARGE.getCode());
            }

            updateStatus(linkageTransferRecord);
            channelHistoryService.updateRechargeStatus(rechargeInfo);


        } catch (Exception e) {
            logger.error("----------充值联动调账失败,orderNo:{}--------------", orderNo);
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public int updateStatus(LinkageTransferRecord record) {
        return linkageTransferRecordDao.updateStatus(record);
    }

    @Override
    public List<LinkageTransferRecord> getPayingList() {
        return linkageTransferRecordDao.getPayingList();
    }

    @Override
    public List<LinkageTransferRecord> getList(LinkageTransferRecord record) {
        return linkageTransferRecordDao.getList(record);
    }


	@Override
	public List<LinkageTransferRecord> checkIsExistRecord(String customKey) {
		return linkageTransferRecordDao.checkIsExistRecord(customKey);
	}


	@Override
	public List<LinkageTransferRecord> checkIsExistRecordByConfigId(Integer id) {
		return linkageTransferRecordDao.checkIsExistRecordByConfigId(id);
	}


}
