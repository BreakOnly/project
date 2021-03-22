package com.jrmf.taxsettlement.api.service.recharge;

import com.jrmf.domain.ChannelHistory;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019年8月22日16:08:53
 * @desc 充值记录查询
 **/
@ActionConfig(name = "充值记录查询")
public class QueryRechargeRecordService
        implements Action<QueryRechargeRecordServiceParams, QueryRechargeRecordServiceAttachment> {

    private static final Logger logger = LoggerFactory.getLogger(QueryRechargeRecordService.class);

    @Autowired
    private ChannelHistoryService channelHistoryService;

    @Override
    public String getActionType() {
        return APIDefinition.QUERY_RECHARGE_RECORD.name();
    }

    @Override
    public ActionResult<QueryRechargeRecordServiceAttachment> execute(
            QueryRechargeRecordServiceParams actionParams) {

        String customOrderNo = actionParams.getCustomOrderNo();
        String orderNo = actionParams.getOrderNo();
        ChannelHistory channelHistory;
        if(StringUtil.isEmpty(orderNo)){
            if (StringUtil.isEmpty(customOrderNo)) {
                throw new APIDockingException(APIDockingRetCodes.DEAL_NO_NOT_EXISTED.getCode(), "null");
            }
            Map<String, Object> hashMap = new HashMap<>(4);
            hashMap.put("customkey",actionParams.getMerchantId());
            hashMap.put("customOrderNo",actionParams.getCustomOrderNo());
            List<ChannelHistory> channelHistoryList = channelHistoryService.getChannelHistoryList(hashMap);
            if(channelHistoryList.isEmpty()){
                throw new APIDockingException(APIDockingRetCodes.REQUEST_NO_NOT_EXISTED.getCode(), customOrderNo);
            }
            channelHistory = channelHistoryList.get(0);
        }else{
            channelHistory = channelHistoryService.getChannelHistoryByOrderno(orderNo);

            if (channelHistory == null) {
                throw new APIDockingException(APIDockingRetCodes.REQUEST_NO_NOT_EXISTED.getCode(), customOrderNo);
            }
        }
        int status = channelHistory.getStatus();
        logger.info("订单状态："+status);
        APIRechargeStatus apiRechargeStatus = APIRechargeStatus.codeOf(status);
        QueryRechargeRecordServiceAttachment attachment = new QueryRechargeRecordServiceAttachment();
        attachment.setAccountTime(channelHistory.getUpdatetime());
        attachment.setAmount(channelHistory.getRechargeAmount());
        attachment.setBalanceAmount(channelHistory.getAmount());
        attachment.setDealStatus(apiRechargeStatus.getAPIStatus());
        attachment.setDealStatusMsg(apiRechargeStatus.getDesc());
        attachment.setInvoiceAmount(channelHistory.getInvoiceAmount());
        attachment.setInvoiceingAmount(channelHistory.getInvoiceingAmount());
        attachment.setUninvoiceAmount(channelHistory.getUnInvoiceAmount());
        attachment.setInvoiceStatus(channelHistory.getInvoiceStatus()+"");
        if(apiRechargeStatus == APIRechargeStatus.FAILED){
            attachment.setDealStatusMsg(channelHistory.getRemark());
        }
        attachment.setServiceFee(channelHistory.getServiceFee());
        attachment.setRemark(channelHistory.getRemark());
        return new ActionResult<>(attachment);
    }

}
