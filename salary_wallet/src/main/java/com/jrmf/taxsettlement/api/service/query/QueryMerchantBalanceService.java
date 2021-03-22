package com.jrmf.taxsettlement.api.service.query;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.service.CustomBalanceService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;

@ActionConfig(name = "商户余额查询")
public class QueryMerchantBalanceService
		implements Action<QueryMerchantBalanceServiceParams, QueryMerchantBalanceServiceAttachment> {

	public static final String ANY_MATCH_SIGNAL = "*";

	private static final Logger logger = LoggerFactory.getLogger(QueryMerchantBalanceService.class);

	@Autowired
	private CustomBalanceService customBalanceService;

	@Override
	public String getActionType() {
		return APIDefinition.QUERY_MERCHANT_BALANCE.name();
	}

	@Override
	public ActionResult<QueryMerchantBalanceServiceAttachment> execute(QueryMerchantBalanceServiceParams actionParams) {

		QueryMerchantBalanceServiceAttachment attachment = new QueryMerchantBalanceServiceAttachment();

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("customkey", actionParams.getMerchantId());

		String transferCorpId = actionParams.getTransferCorpId();
		if (transferCorpId != null && !"".equals(transferCorpId) && !ANY_MATCH_SIGNAL.equals(transferCorpId)) {
			params.put("companyId", transferCorpId);
			attachment.setTransferCorpId(transferCorpId);
		} else {
			attachment.setTransferCorpId(ANY_MATCH_SIGNAL);
		}

		String payType = actionParams.getPayType();
		if (payType != null && !"".equals(payType) && !ANY_MATCH_SIGNAL.equals(payType)) {
			params.put("payType", payType);
			attachment.setPayType(payType);
		} else {
			attachment.setPayType(ANY_MATCH_SIGNAL);
		}

		Integer balance = customBalanceService.queryBalance(params);
		if (balance == null) {
			throw new APIDockingException(APIDockingRetCodes.NO_CONTRACT_WITH_AGENT.getCode(),
					new StringBuilder(actionParams.getMerchantId()).append("-").append(transferCorpId).toString());
		}
		StringBuilder balanceStr = new StringBuilder(String.valueOf(balance));
		int strLength = balanceStr.length();
		if (strLength == 1) {
			balanceStr.insert(0, "0.0");
		} else if (strLength == 2) {
			balanceStr.insert(0, "0.");
		} else {
			balanceStr.insert(strLength - 2, ".");
		}
		attachment.setBalance(balanceStr.toString());

		return new ActionResult<QueryMerchantBalanceServiceAttachment>(attachment);
	}

}
