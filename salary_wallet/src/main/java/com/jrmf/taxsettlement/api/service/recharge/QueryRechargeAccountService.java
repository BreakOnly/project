package com.jrmf.taxsettlement.api.service.recharge;

import com.jrmf.domain.Company;
import com.jrmf.service.CompanyService;
import com.jrmf.service.CustomReceiveConfigService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019年8月22日16:08:53
 * @desc 查询充值账户信息
 **/
@ActionConfig(name = "查询充值账户信息")
public class QueryRechargeAccountService
        implements Action<QueryRechargeAccountServiceParams, QueryRechargeAccountServiceAttachment> {

    private static final Logger logger = LoggerFactory.getLogger(QueryRechargeAccountService.class);

    @Autowired
    private CustomReceiveConfigService customReceiveConfigService;
    @Autowired
    private CompanyService companyService;

    @Override
    public String getActionType() {
        return APIDefinition.RECHARGE_QUERY_ACCOUNT.name();
    }

    @Override
    public ActionResult<QueryRechargeAccountServiceAttachment> execute(
            QueryRechargeAccountServiceParams actionParams) {
        String transferCorpId = actionParams.getTransferCorpId();
        Company company = companyService.getCompanyByUserId(Integer.parseInt(transferCorpId));
        Integer status = company.getStatus();
        if(status != null && status == 2){
            throw new APIDockingException(APIDockingRetCodes.COMPANY_RECHARGE_DISABLED.getCode(),APIDockingRetCodes.COMPANY_RECHARGE_DISABLED.getDesc() );
        }
        Map<String, Object> params = new HashMap<>(2);
        params.put("customkey", actionParams.getMerchantId());
        params.put("channelId", actionParams.getTransferCorpId());
        params.put("companyId", actionParams.getTransferCorpId());
        params.put("payType", actionParams.getPayType());
        Map<String, Object> rechargeInfo = customReceiveConfigService.getRechargeInfo(params);
        if(rechargeInfo == null){
            logger.error("未找到充值配置，请求参数是{}",params);
            throw new APIDockingException(APIDockingRetCodes.RECHARGE_INFO_NOT_FOUND.getCode(), APIDockingRetCodes.RECHARGE_INFO_NOT_FOUND.getDesc());
        }

        QueryRechargeAccountServiceAttachment attachment = new QueryRechargeAccountServiceAttachment();
        attachment.setReceiveAccount(rechargeInfo.get("inAccountNo")+"");
        attachment.setReceiveAccountName(rechargeInfo.get("inAccountBankName")+"");
        attachment.setReceiveMerchantName(rechargeInfo.get("inAccountName")+"");
        attachment.setTransferCorpId(actionParams.getTransferCorpId());
        return new ActionResult<>(attachment);
    }

}
