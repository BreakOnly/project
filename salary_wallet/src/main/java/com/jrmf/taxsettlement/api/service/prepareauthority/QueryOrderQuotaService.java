package com.jrmf.taxsettlement.api.service.prepareauthority;

import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.Company;
import com.jrmf.domain.CustomLimitConf;
import com.jrmf.domain.CustomPaymentTotalAmount;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.CompanyService;
import com.jrmf.service.CustomCompanyRateConfService;
import com.jrmf.service.CustomLimitConfService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.utils.AesUtil;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chonglulu
 */
@ActionConfig(name = "可发放额度查询")
public class QueryOrderQuotaService implements Action<QueryOrderQuotaServiceParams, QueryOrderQuotaServiceAttachment> {


    private static final Logger logger = LoggerFactory.getLogger(QueryOrderQuotaService.class);

    private final ChannelRelatedService channelRelatedService;
    private final CompanyService companyService;
    private final CustomLimitConfService customLimitConfService;
    private final CustomCompanyRateConfService customCompanyRateConfService;

    @Autowired
    public QueryOrderQuotaService(ChannelRelatedService channelRelatedService, CompanyService companyService, CustomLimitConfService customLimitConfService, CustomCompanyRateConfService customCompanyRateConfService) {
        this.channelRelatedService = channelRelatedService;
        this.companyService = companyService;
        this.customLimitConfService = customLimitConfService;
        this.customCompanyRateConfService = customCompanyRateConfService;
    }

    @Override
    public String getActionType() {
        return APIDefinition.QUERY_ORDER_QUOTA.name();
    }

    @Override
    public ActionResult<QueryOrderQuotaServiceAttachment> execute(QueryOrderQuotaServiceParams actionParams) {
        String merchantId = actionParams.getMerchantId();
        List<ChannelRelated> relatedList = channelRelatedService.getRelatedList(merchantId);
        if (relatedList.isEmpty()) {
            throw new APIDockingException(APIDockingRetCodes.TRANSFER_CORPORATION_NOT_EXISTED.getCode(), APIDockingRetCodes.TRANSFER_CORPORATION_NOT_EXISTED.getDesc());
        }
        String certificateNo = actionParams.getCertificateNo();
        String secretKey = "13E80F176EDCA60456220FE8EDCB5772";
        try {
            certificateNo = AesUtil.decrypt(certificateNo, secretKey);
        } catch (Exception e) {
            throw new APIDockingException(APIDockingRetCodes.PARAMETER_ANALYSIS_ERROR.getCode(), APIDockingRetCodes.PARAMETER_ANALYSIS_ERROR.getDesc());
        }
        List<QueryOrderQuotaServiceAttachmentDetail> attachments = new ArrayList<>();
        for (ChannelRelated channelRelated : relatedList) {
            String companyId = channelRelated.getCompanyId();
            Company company = companyService.getCompanyByUserId(Integer.parseInt(companyId));
            int type = company.getCalculateType();
            if (type == 0) {
                logger.info("本地计算");
                //商户限额
                String amount;
                String amountZero = "0.00";
                Map<String, Object> map = new HashMap<>(4);
                map.put("customkey", merchantId);
                map.put("companyId", companyId);
                CustomLimitConf customLimitConf = customLimitConfService.getCustomLimitConf(map);
                QueryOrderQuotaServiceAttachmentDetail detail = new QueryOrderQuotaServiceAttachmentDetail();
                detail.setTransferCorpId(companyId);

                List<String> amountList = new ArrayList<>();

                //1、商户当月最大可发限额
                if (customLimitConf != null && !StringUtil.isEmpty(customLimitConf.getSingleMonthLimit())) {
                    String customMonthLimit = customLimitConf.getSingleMonthLimit();
                    amountList.add(customMonthLimit);
                }
                //2、服务公司当月最大可发限额
                String companyMonthLimit = company.getSingleMonthLimit();
                if (!StringUtil.isEmpty(companyMonthLimit)) {
                    amountList.add(companyMonthLimit);
                }

                //3、档位配置的当月最大可发限额
                String rateMonthLimit = customCompanyRateConfService.getCustomMonthLimit(merchantId, Integer.parseInt(companyId));
                if (StringUtil.isEmpty(rateMonthLimit)) {
                    rateMonthLimit = amountZero;
                }

                amountList.add(rateMonthLimit);

                amountList.sort((s1, s2) -> { //排序ASC
                    Double d1 = Double.valueOf(s1);
                    Double d2 = Double.valueOf(s2);
                    return d1.compareTo(d2);
                });
                amount = amountList.get(0); //获取最小的限额用来计算剩余下发额

                CustomPaymentTotalAmount customPaymentTotalAmount = customLimitConfService.queryCustomPaymentTotalAmount(companyId, merchantId, certificateNo);
                String currentMonthTotal = customPaymentTotalAmount.getCurrentMonthTotalStr();
                String customAmount = ArithmeticUtil.subStr(amount, currentMonthTotal);
                logger.info("商户个人剩余下发额：" + customAmount);
                amount = customAmount;

                if (ArithmeticUtil.compareTod(amount, amountZero) < 0) {
                    amount = amountZero;
                }
                amount = StringUtil.getFormatResult(amount, 2);
                detail.setAmount(amount);
                attachments.add(detail);


            } else {
                throw new APIDockingException(APIDockingRetCodes.CALCULATE_TYPE_NO_EXISTED.getCode(), APIDockingRetCodes.CALCULATE_TYPE_NO_EXISTED.getDesc());
            }
        }
        QueryOrderQuotaServiceAttachment queryOrderQuotaServiceAttachment = new QueryOrderQuotaServiceAttachment();
        queryOrderQuotaServiceAttachment.setCertificateNo(actionParams.getCertificateNo());
        queryOrderQuotaServiceAttachment.setDetails(attachments);
        return new ActionResult<>(queryOrderQuotaServiceAttachment);

    }

}
