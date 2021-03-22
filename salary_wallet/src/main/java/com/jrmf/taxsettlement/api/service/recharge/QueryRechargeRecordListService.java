package com.jrmf.taxsettlement.api.service.recharge;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.controller.constant.SortType;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.taxsettlement.api.APIDefinition;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;
import com.jrmf.taxsettlement.api.service.Action;
import com.jrmf.taxsettlement.api.service.ActionConfig;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@ActionConfig(name = "充值记录列表查询")
public class QueryRechargeRecordListService
    implements Action<QueryRechargeRecordListServiceParams, QueryRechargeRecordListServiceAttachment> {

  private static final Logger logger = LoggerFactory.getLogger(QueryRechargeRecordListService.class);

  @Autowired
  private ChannelHistoryService channelHistoryService;

  @Override
  public String getActionType() {
    return APIDefinition.QUERY_RECHARGE_LIST_RECORD.name();
  }

  @Override
  public ActionResult<QueryRechargeRecordListServiceAttachment> execute(
      QueryRechargeRecordListServiceParams actionParams) {
    logger.info("充值记录列表查询request...");
    String customKey = actionParams.getMerchantId();
    String companyIds = actionParams.getTransferCorpId();
    String pageNo = actionParams.getPage();
    String size = actionParams.getSize();
    String startDate = actionParams.getStartDate();
    String endDate = actionParams.getEndDate();
    String sort = actionParams.getSort();

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

    if (!isInteger(pageNo) || !isInteger(size)) {
      throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), APIDockingRetCodes.FIELD_FORMAT_ERROR.getDesc());
    }
    
    if (StringUtil.isEmpty(sort)) {
      sort = SortType.DESCENDING.getCode() + "";
    }
    
    if (!StringUtil.isNumber(sort)) {
      throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), APIDockingRetCodes.FIELD_FORMAT_ERROR.getDesc());
    }

    if (!sort.equals(SortType.DESCENDING.getCode()+"") && !sort.equals(SortType.ASCENDING.getCode()+"")) {
      throw new APIDockingException(APIDockingRetCodes.FIELD_FORMAT_ERROR.getCode(), APIDockingRetCodes.FIELD_FORMAT_ERROR.getDesc());
    }

    setSize(size);
    Map<String, Object> paramMap = new HashMap<>(8);
    paramMap.put("customKey", customKey);
    paramMap.put("companyIds", companyIds);
    paramMap.put("startDate", startDate);
    paramMap.put("endDate", endDate);
    paramMap.put("sort", sort);
    List<RechargeRecordListServiceAttachment> channelHistoryList = null;
    PageInfo<RechargeRecordListServiceAttachment> pageInfo = null;
    try {
      PageHelper.startPage(Integer.parseInt(pageNo), Integer.parseInt(size));
      channelHistoryList = channelHistoryService.apiGetChannelHistoryList(paramMap);
      pageInfo = new PageInfo<>(channelHistoryList);
    } catch (Exception e) {
      logger.error("充值记录列表查询Exception..", e);
      throw new APIDockingException(APIDockingRetCodes.ILLEGAL_ACCESS.getCode(), APIDockingRetCodes.ILLEGAL_ACCESS.getDesc());
    }
    QueryRechargeRecordListServiceAttachment attachment = new QueryRechargeRecordListServiceAttachment();
    attachment.setList(pageInfo.getList());
    attachment.setTotal(pageInfo.getTotal() + "");
    return new ActionResult<>(attachment);
  }

  public static boolean isInteger(String str) {
    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
    return pattern.matcher(str).matches();
  }

  private void setSize(String size) {
    int sizeMax = 100;
    if(Integer.parseInt(size) > sizeMax){
      size = "100";
    }
  }
}
