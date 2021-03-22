package com.jrmf.taxsettlement.api.service.invoice;

import com.jrmf.controller.constant.InvoiceCategoryType;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.Company;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.CompanyService;
import com.jrmf.service.CustomInvoiceService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@ActionConfig(name = "开票统计信息查询")
public class QueryInvoiceSummaryHistoryService implements Action<QueryInvoiceSummaryHistoryServiceParams, QueryInvoiceSummaryHistoryServiceAttachment> {

  private static final Logger logger = LoggerFactory.getLogger(QueryInvoiceSummaryHistoryService.class);

  @Autowired
  private CustomInvoiceService customInvoiceService;

  @Autowired
  private CompanyService companyService;

  @Autowired
  private ChannelRelatedService channelRelatedService;

  @Autowired
  private ChannelHistoryService channelHistoryService;

  @Autowired
  private UserCommissionService userCommissionService;

  @Override
  public String getActionType() {
    return APIDefinition.QUERY_INVOICE_SUMMARY_HISTORY.name();
  }

  @Override
  public ActionResult<QueryInvoiceSummaryHistoryServiceAttachment> execute(QueryInvoiceSummaryHistoryServiceParams actionParams) {
    logger.info("开票收件人地址查询request...");
    String customKey = actionParams.getMerchantId();
    String companyIds = actionParams.getTransferCorpId();
    String startDate = actionParams.getStartDate();
    String endDate = actionParams.getEndDate();

    if (!DateUtils.checkValidDate(startDate) || !DateUtils.checkValidDate(endDate)) {
      throw new APIDockingException(APIDockingRetCodes.ILLEGALITY_TIMESTAMP_PARAMETER.getCode(), APIDockingRetCodes.ILLEGALITY_TIMESTAMP_PARAMETER.getDesc());
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
    try {
      if (DateUtils.DateCompare(sdf.parse(startDate),sdf.parse(endDate),1)) {
        throw new APIDockingException(APIDockingRetCodes.TIME_NOT_SERVICE_AREA.getCode(), APIDockingRetCodes.TIME_NOT_SERVICE_AREA.getDesc());
      }
    } catch (ParseException e) {
      logger.error("日期转换异常..", e);
      throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), APIDockingRetCodes.FIELD_FORMAT_ERROR.getDesc());
    }

    Map<String, Object> paramMap = new HashMap<>(6);
    paramMap.put("customKey", customKey);
    paramMap.put("companyIds", companyIds);
    paramMap.put("startDate", startDate);
    paramMap.put("endDate", endDate);

    String invoicedAmount = "";
    String totalUninvoicedAmount = "";
    try {
      invoicedAmount = customInvoiceService.getInvoicedAmountByParam(paramMap);
      if (StringUtil.isEmpty(companyIds)) {
        // 查商户所关联的全部服务公司
        companyIds = getCompanyIdByCustomKey(customKey);
      }
      if (!StringUtil.isEmpty(companyIds)) {
        totalUninvoicedAmount = gettotalUninvoicedAmountByCompanyIds(companyIds, customKey);
      }
    } catch (Exception e) {
      logger.error("开票统计信息查询异常:", e);
      throw new APIDockingException(APIDockingRetCodes.ILLEGAL_ACCESS.getCode(), APIDockingRetCodes.ILLEGAL_ACCESS.getDesc());
    }
    QueryInvoiceSummaryHistoryServiceAttachment attachment = new QueryInvoiceSummaryHistoryServiceAttachment();
    attachment.setInvoicedAmount(StringUtil.isEmpty(invoicedAmount)?"0":invoicedAmount);
    attachment.setTotalUninvoicedAmount(StringUtil.isEmpty(totalUninvoicedAmount)?"0":totalUninvoicedAmount);
    return new ActionResult<>(attachment);
  }

  private String getCompanyIdByCustomKey(String customKey) {
    String companyIds = "";
    List<ChannelRelated> relatedList = channelRelatedService.getRelatedList(customKey);
    if (relatedList != null && !relatedList.isEmpty()) {
      for (ChannelRelated related : relatedList) {
        companyIds = companyIds + related.getCompanyId() + ",";
      }
    }
    return companyIds;
  }

  private String gettotalUninvoicedAmountByCompanyIds(String companyIds, String customKey) {
    BigDecimal totalUninvoicedAmount = new BigDecimal("0");
    List<Company> companyList = companyService.getCompanyByUserIds(companyIds);
    if (companyList != null && !companyList.isEmpty()) {
      for (Company company : companyList) {
        if (company.getInvoiceCategory() == InvoiceCategoryType.RECHARGE.getCode()) {
          String amount = channelHistoryService.getTotalUninvoicedAmountByCompanyId(company.getUserId(), customKey);
          if (!StringUtil.isEmpty(amount)) {
            totalUninvoicedAmount = totalUninvoicedAmount.add(new BigDecimal(amount));
          }
        } else if (company.getInvoiceCategory() == InvoiceCategoryType.ISSUE.getCode()) {
          String amount = userCommissionService.getTotalUninvoicedAmountByCompanyId(company.getUserId(), customKey);
          if (!StringUtil.isEmpty(amount)) {
            totalUninvoicedAmount = totalUninvoicedAmount.add(new BigDecimal(amount));
          }
        }
      }
    }
    return totalUninvoicedAmount.toString();
  }
}
