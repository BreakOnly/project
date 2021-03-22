package com.jrmf.taxsettlement.api.service.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.domain.AgreementTemplate;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.User;
import com.jrmf.persistence.TransferBankDao;
import com.jrmf.service.AgreementTemplateService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.util.ChannelRelatedCache;

@ActionConfig(name = "商户用户签约查询")
public class QueryMerchantUserContractService
		implements Action<QueryMerchantUserContractServiceParams, QueryMerchantUserContractServiceAttachment> {

	public static final String ANY_MATCH_SIGNAL = "*";

	private static final Logger logger = LoggerFactory.getLogger(QueryMerchantUserContractService.class);

	@Autowired
	private TransferBankDao transferBankDao;
	
	@Autowired
	private ChannelRelatedCache channelRelatedCache;
	
	@Autowired
	private AgreementTemplateService agreementTemplateService;
	
	@Override
	public String getActionType() {
		return APIDefinition.QUERY_MERCHANT_USER_CONTRACT.name();
	}

	@Override
	public ActionResult<QueryMerchantUserContractServiceAttachment> execute(QueryMerchantUserContractServiceParams actionParams) {

		String certificateNo = actionParams.getCertificateNo();
		String userName = actionParams.getName();
		
		List<User> userData = transferBankDao.getUserByCertId(certificateNo);
		if (userData != null && userData.size() > 0) {
			if (!userName.equals(userData.get(0).getUserName())) {
				throw new APIDockingException(APIDockingRetCodes.REAL_NAME_VERIFY_FAILED.getCode(),
						new StringBuilder(userName).append("-").append(certificateNo).toString());
			}
		}
		
		QueryMerchantUserContractServiceAttachment attachment = new QueryMerchantUserContractServiceAttachment();
		
		ChannelRelated channelWithContract = channelRelatedCache.getChannelRelated(actionParams.getMerchantId(),
				actionParams.getTransferCorpId());
		
		Map<String, Object> paramMap = new HashMap<>(12);
		paramMap.put("companyId", channelWithContract.getCompanyId());
		paramMap.put("originalId", channelWithContract.getOriginalId());
		paramMap.put("agreementPayment", "1");
		List<AgreementTemplate> agreementTemplateList = agreementTemplateService
				.getAgreementTemplateByParam(paramMap);
		int templateCount = agreementTemplateList == null ? 0 : agreementTemplateList.size();
		attachment.setContractCount(templateCount);
		if (templateCount > 0) {
			paramMap.clear();
			paramMap.put("certId", certificateNo);
			paramMap.put("signStatus", "5");
            paramMap.put("userName", userName);
			String agreementTemplateId = "";
			if (agreementTemplateList != null && agreementTemplateList.size() > 0) {
				for (int ai = 0; ai < agreementTemplateList.size(); ai++) {
					agreementTemplateId = agreementTemplateList.get(ai).getId() + "," + agreementTemplateId;
				}
				agreementTemplateId = agreementTemplateId.substring(0, agreementTemplateId.lastIndexOf(","));
			}

			paramMap.put("agreementTemplateId", agreementTemplateId);
			int userAgreeCount = agreementTemplateService.getUserAgreementCountByParam(paramMap);
			attachment.setSignStatus(userAgreeCount != templateCount ? "N" : "Y");
			attachment.setSignCount(userAgreeCount);
		} else {
			attachment.setSignStatus("N");
			attachment.setSignCount(0);
		}
		
		return new ActionResult<QueryMerchantUserContractServiceAttachment>(attachment);
	}

}
