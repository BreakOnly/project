package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.controller.constant.InvoiceOrderStatus;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.CustomInvoiceInfoDO;
import com.jrmf.domain.QbInvoiceBase;
import com.jrmf.domain.QbInvoiceRecord;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.CustomInvoiceService;
import com.jrmf.service.QbInvoiceBaseService;
import com.jrmf.service.QbInvoiceRecordService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author 种路路
 * @create 2019年8月22日16:08:53
 * @desc 查询充值账户信息
 **/
@ActionConfig(name = "发票申请")
public class ApplyInvoiceService
        implements Action<ApplyInvoiceServiceParams, ApplyInvoiceServiceAttachment> {

    @Autowired
    private QbInvoiceBaseService qbInvoiceBaseService;
    @Autowired
    private CustomInvoiceService customInvoiceService;
    @Autowired
    private ChannelHistoryService channelHistoryService;
    @Autowired
    private QbInvoiceRecordService qbInvoiceRecordService;

    @Override
    public String getActionType() {
        return APIDefinition.APPLY_INVOICE.name();
    }

    @Override
    public ActionResult<ApplyInvoiceServiceAttachment> execute(
            ApplyInvoiceServiceParams actionParams) {
        String invoiceAmount = actionParams.getInvoiceAmount();
        invoiceAmount = StringUtil.getFormatResult(invoiceAmount, 2);
        if(!StringUtil.isMoney(invoiceAmount)){
            throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), APIDockingRetCodes.FIELD_FORMAT_ERROR.getDesc());
        }
        String infoId = actionParams.getInfoId();
        QbInvoiceBase qbInvoiceBase = qbInvoiceBaseService.selectByPrimaryKey(Integer.parseInt(infoId));
        if(qbInvoiceBase == null){
            throw new APIDockingException(APIDockingRetCodes.MERCHANT_INVOICE_INFO_NOT_FOUND.getCode(), APIDockingRetCodes.MERCHANT_INVOICE_INFO_NOT_FOUND.getDesc());
        }
        if(qbInvoiceBase.getStatus() != 1){
            throw new APIDockingException(APIDockingRetCodes.MERCHANT_INVOICE_INFO_NOT_AVAILABLE.getCode(), APIDockingRetCodes.MERCHANT_INVOICE_INFO_NOT_AVAILABLE.getDesc());
        }
        String receiveId = actionParams.getReceiveId();
        CustomInvoiceInfoDO customInvoiceInfo = customInvoiceService.getCustomInvoiceInfoDOById(Integer.parseInt(receiveId));
        if(customInvoiceInfo == null){
            throw new APIDockingException(APIDockingRetCodes.CUSTOM_INVOICE_INFO_NOT_FOUND.getCode(), APIDockingRetCodes.CUSTOM_INVOICE_INFO_NOT_FOUND.getDesc());
        }
        String orderNos = actionParams.getOrderNos();


        List<Integer> billingClassList = qbInvoiceRecordService.groupBillingClassByOrderNo(orderNos);
        if(billingClassList.size()>1){
            throw new APIDockingException(APIDockingRetCodes.INVOICE_ClASS_DIFFERENT.getCode(), APIDockingRetCodes.INVOICE_ClASS_DIFFERENT.getDesc());
        }
        if(billingClassList.size() == 1 ){
            Integer billingClassInteger = billingClassList.get(0);
            Integer billingClass = qbInvoiceBase.getBillingClass();
            if(!Objects.equals(billingClassInteger, billingClass)){
                throw new APIDockingException(APIDockingRetCodes.INVOICE_ClASS_DIFFERENT.getCode(), APIDockingRetCodes.INVOICE_ClASS_DIFFERENT.getDesc());
            }
        }

        String[] orderNoArray = orderNos.split(",");
        String zeroString = "0.00";
        if(orderNoArray.length == 1){
            ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(orderNos);
            if(channelHistory == null){
                throw new APIDockingException(APIDockingRetCodes.RECHARGE_ORDERS_NOT_FOUND.getCode(), APIDockingRetCodes.RECHARGE_ORDERS_NOT_FOUND.getDesc()+","+orderNos);
            }
            if(channelHistory.getStatus()!= 1){
                throw new APIDockingException(APIDockingRetCodes.RECHARGE_ORDERS_NOT_SUCCESS.getCode(), APIDockingRetCodes.RECHARGE_ORDERS_NOT_SUCCESS.getDesc()+","+orderNos);
            }
            if(!actionParams.getMerchantId().equals(channelHistory.getCustomkey())){
                throw new APIDockingException(APIDockingRetCodes.RECHARGE_ORDERS_NOT_FOUND.getCode(), APIDockingRetCodes.RECHARGE_ORDERS_NOT_FOUND.getDesc()+","+orderNos);
            }
            if(channelHistory.getInvoiceStatus() == InvoiceOrderStatus.SECTION_TYPE.getCode()){
                throw new APIDockingException(APIDockingRetCodes.APPLY_INVOICE_FINISH.getCode(), APIDockingRetCodes.APPLY_INVOICE_FINISH.getDesc()+","+orderNos);
            }
            if(channelHistory.getInvoiceStatus() == InvoiceOrderStatus.FINISH_TYPE.getCode()){
                throw new APIDockingException(APIDockingRetCodes.APPLY_INVOICE_PROCESSING.getCode(), APIDockingRetCodes.APPLY_INVOICE_PROCESSING.getDesc()+","+orderNos);
            }

            String invoicingAmount = StringUtil.getFormatResult(channelHistory.getInvoiceingAmount(), 2);
            String unInvoiceAmount = StringUtil.getFormatResult(channelHistory.getUnInvoiceAmount(), 2);
            if(zeroString.equals(unInvoiceAmount)){
                throw new APIDockingException(APIDockingRetCodes.NO_INVOICE_AMOUNT.getCode(), APIDockingRetCodes.NO_INVOICE_AMOUNT.getDesc());
            }
            if(ArithmeticUtil.compareTod(unInvoiceAmount, invoiceAmount)<0){
                throw new APIDockingException(APIDockingRetCodes.NO_ENOUGH_INVOICE_AMOUNT.getCode(), APIDockingRetCodes.NO_ENOUGH_INVOICE_AMOUNT.getDesc());
            }
            channelHistory.setInvoiceingAmount(ArithmeticUtil.getScale(ArithmeticUtil.addStr(invoicingAmount,invoiceAmount),2));
            channelHistory.setUnInvoiceAmount(ArithmeticUtil.getScale(ArithmeticUtil.subStr(unInvoiceAmount,invoiceAmount),2));
            channelHistory.setInvoiceStatus(3);
            channelHistoryService.updateChannelHistory(channelHistory);
        }else{
            String sumInvoiceAmount = zeroString;
            String serviceCompanyId = "";
            for (String orderNo : orderNoArray) {
                ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(orderNo);
                if(channelHistory == null){
                    throw new APIDockingException(APIDockingRetCodes.RECHARGE_ORDERS_NOT_FOUND.getCode(), APIDockingRetCodes.RECHARGE_ORDERS_NOT_FOUND.getDesc()+","+orderNo);
                }
                if(!actionParams.getMerchantId().equals(channelHistory.getCustomkey())){
                    throw new APIDockingException(APIDockingRetCodes.RECHARGE_ORDERS_NOT_FOUND.getCode(), APIDockingRetCodes.RECHARGE_ORDERS_NOT_FOUND.getDesc()+","+orderNo);
                }
                if(InvoiceOrderStatus.NO_TYPE.getCode() != channelHistory.getInvoiceStatus()){
                    throw new APIDockingException(APIDockingRetCodes.SINGLE_INVOICE_PROCESS_CAN_NOT_MERGE.getCode(), APIDockingRetCodes.SINGLE_INVOICE_PROCESS_CAN_NOT_MERGE.getDesc()+","+orderNo);
                }
                String unInvoiceAmount = StringUtil.getFormatResult(channelHistory.getUnInvoiceAmount(), 2);
                sumInvoiceAmount = ArithmeticUtil.getScale(ArithmeticUtil.addStr(sumInvoiceAmount,unInvoiceAmount),2);
                if(!StringUtil.isEmpty(serviceCompanyId)){
                    if(!serviceCompanyId.equals(channelHistory.getRecCustomkey())){
                        throw new APIDockingException(APIDockingRetCodes.DIFFERENT_SERVICE_COMPANY_CAN_NOT_MERGER_APPLY_INVOICE.getCode(), APIDockingRetCodes.DIFFERENT_SERVICE_COMPANY_CAN_NOT_MERGER_APPLY_INVOICE.getDesc());
                    }
                }else{
                    serviceCompanyId = channelHistory.getRecCustomkey();
                }
            }
            if(ArithmeticUtil.compareTod(sumInvoiceAmount,invoiceAmount)!= 0){
                throw new APIDockingException(APIDockingRetCodes.INVOICE_AMOUNT_NOT_EQUALS_ORDER_AMOUNT.getCode(), APIDockingRetCodes.INVOICE_AMOUNT_NOT_EQUALS_ORDER_AMOUNT.getDesc());
            }
            for (String orderNo : orderNoArray) {
                ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(orderNo);
                channelHistory.setInvoiceingAmount(channelHistory.getUnInvoiceAmount());
                channelHistory.setUnInvoiceAmount(zeroString);
                channelHistory.setInvoiceStatus(3);
                channelHistoryService.updateChannelHistory(channelHistory);
            }
        }
        QbInvoiceRecord invoiceRecord = new QbInvoiceRecord();
        String invoiceSerialNo = "P"+OrderNoUtil.getOrderNo();
        invoiceRecord.setInvoiceSerialNo(invoiceSerialNo);
        invoiceRecord.setCustomkey(actionParams.getMerchantId());
        invoiceRecord.setCompanyId(qbInvoiceBase.getCompanyId());
        invoiceRecord.setStatus(1);
        invoiceRecord.setInvoiceAmount(invoiceAmount);
        invoiceRecord.setInvoiceType(qbInvoiceBase.getInvoiceType());
        invoiceRecord.setBillingClass(qbInvoiceBase.getBillingClass());
        invoiceRecord.setServiceType(qbInvoiceBase.getBillingClass());
        invoiceRecord.setTaxpayerType(qbInvoiceBase.getTaxpayerType());
        invoiceRecord.setIsDiscard(0);
        invoiceRecord.setOrderNo(orderNos);
        invoiceRecord.setReceiveUser(customInvoiceInfo.getInvoiceUserName()+","+customInvoiceInfo.getInvoiceAddress()+","+customInvoiceInfo.getInvoicePhone());
        invoiceRecord.setCompanyName(qbInvoiceBase.getCompanyName());
        invoiceRecord.setRemark(actionParams.getRemark());
        invoiceRecord.setAccountBankName(qbInvoiceBase.getAccountBankName());
        invoiceRecord.setAccountNo(qbInvoiceBase.getAccountNo());
        invoiceRecord.setAddress(qbInvoiceBase.getAddress());
        invoiceRecord.setPhone(qbInvoiceBase.getPhone());
        invoiceRecord.setCreateTime(DateUtils.getNowDate());
        invoiceRecord.setUpdateTime(DateUtils.getNowDate());
        invoiceRecord.setTaxRegistrationNumber(qbInvoiceBase.getTaxRegistrationNumber());
        qbInvoiceRecordService.insert(invoiceRecord);

        ApplyInvoiceServiceAttachment attachment = new ApplyInvoiceServiceAttachment();
        attachment.setAddTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return new ActionResult<>(attachment);
    }

}
