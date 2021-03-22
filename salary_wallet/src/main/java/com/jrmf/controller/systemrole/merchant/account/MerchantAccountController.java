package com.jrmf.controller.systemrole.merchant.account;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.APIResponse;
import com.jrmf.common.CommonString;
import com.jrmf.common.Constant;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.common.ServiceResponse;
import com.jrmf.common.UserServiceFeignClient;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.FundModelType;
import com.jrmf.controller.constant.LinkageType;
import com.jrmf.controller.constant.PayType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.controller.constant.RechargeLetterStatusType;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.ChannelConfig;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.Company;
import com.jrmf.domain.CompanyEsignContractTemplate;
import com.jrmf.domain.Contract;
import com.jrmf.domain.CustomCompanyRateConf;
import com.jrmf.domain.CustomReceiveConfig;
import com.jrmf.domain.LinkageBaseConfig;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.dto.ContractDTO;
import com.jrmf.domain.dto.EsignContractDTO;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.CompanyService;
import com.jrmf.service.CustomCompanyRateConfService;
import com.jrmf.service.CustomReceiveConfigService;
import com.jrmf.service.LinkageCustomConfigService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import io.swagger.annotations.ApiOperation;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/merchant/account")
public class MerchantAccountController extends BaseController {

  private static Logger logger = LoggerFactory.getLogger(MerchantAccountController.class);
  @Autowired
  private ChannelHistoryService channelHistoryService;
  @Autowired
  private ChannelRelatedService channelRelatedService;
  @Autowired
  private ChannelCustomService customService;
  @Autowired
  private CompanyService companyService;
  @Autowired
  private OrganizationTreeService organizationTreeService;
  @Autowired
  private CustomReceiveConfigService receiveConfigService;
  @Autowired
  private CustomCompanyRateConfService rateConfService;
  @Autowired
  private LinkageCustomConfigService linkageCustomConfigService;
  @Autowired
  ChannelCustomService channelCustomService;
  @Autowired
  UserServiceFeignClient userServiceFeignClient;

  /**
   * 服务公司余额查询
   */
  @RequestMapping(value = "/custom/account", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> companyAccount(HttpServletRequest request, HttpServletResponse response) {
    // 商户标识
    String originalId = (String) request.getSession().getAttribute("customkey");
    int respstat = RespCode.success;
    Map<String, Object> model = new HashMap<>(10);
    logger.info("/company/account方法  传参： originalId=" + originalId);
    if (StringUtil.isEmpty(originalId)) {
      respstat = RespCode.error101;
      model.put(RespCode.RESP_STAT, respstat);
      model.put(RespCode.RESP_MSG, "请求参数不全");
      return model;
    } else {
      try {
        ChannelRelated hsBankAccount = new ChannelRelated();
        // 徽商电子户
        String balance = channelHistoryService.getBalance("", originalId, "1");
        hsBankAccount.setBalance(balance);
        hsBankAccount.setCompanyName("银行电子户");

        ChannelRelated aLiAccount = new ChannelRelated();
        // 支付宝账户
        String aliBalance = channelHistoryService.getBalance("", originalId, "2");
        aLiAccount.setBalance(aliBalance);
        aLiAccount.setCompanyName("支付宝账户");

        ChannelRelated bankAccount = new ChannelRelated();
        // 银企直连账户
        String bankBalance = channelHistoryService.getBalance("", originalId, "4");
        bankAccount.setBalance(bankBalance);
        bankAccount.setCompanyName("银行卡账户");
        model.put("hsBankAccount", hsBankAccount);
        model.put("aLiAccount", aLiAccount);
        model.put("bankAccount", bankAccount);
      } catch (Exception e) {
        respstat = RespCode.error107;
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
        logger.error(e.getMessage(), e);
        return model;
      }
    }
    model.put(RespCode.RESP_STAT, respstat);
    model.put(RespCode.RESP_MSG, "成功");
    logger.info("返回结果：" + model);
    return model;
  }

  /**
   * Author Nicholas-Ning Description //TODO 商户余额列表查询 重构要求：处理customkey得到用逗号拼接的customkey字符串。 --> 分页查询
   * Date 15:02 2019/1/4 Param [customKey] return java.util.Map<java.lang.String,java.lang.Object>
   **/
  @RequestMapping(value = "/custom/account/balanceList", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> customAccount(Integer customType, Integer nodeId,
      @RequestParam(required = false, defaultValue = "1") Integer pageNo,
      @RequestParam(required = false) String customName,
      @RequestParam(required = false) String companyId,
      @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
    List<String> customKeys = organizationTreeService
        .queryNodeCusotmKey(customType, QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
    StringBuffer customKeyS = new StringBuffer();
    for (String customKey : customKeys) {
      customKeyS.append(customKey + ",");
    }
    List<Map<String, Object>> result = new ArrayList<>();
    int total;
    Map<String, Object> params1 = new HashMap<>(5);
    params1.put(CommonString.CUSTOMKEY, customKeyS.toString());
    params1.put("customName", customName);
    params1.put("companyId", companyId);

    PageHelper.startPage(pageNo, pageSize);
    List<ChannelRelated> relateds = channelRelatedService.queryRelatedList(params1);
    PageInfo<ChannelRelated> pageInfo = new PageInfo(relateds);
    total = (int) pageInfo.getTotal();
    for (ChannelRelated related : pageInfo.getList()) {
      ChannelCustom custom = customService.getCustomByCustomkey(related.getOriginalId());
      ChannelCustom company = customService.getCustomByCustomkey(related.getCompanyId());
      Map<String, Object> data = new HashMap<>(15);
      data.put("customName", custom.getCompanyName());
      data.put("companyName", company.getCompanyName());
      PayType[] values = PayType.values();
      for (PayType payType : values) {
        String balance = channelHistoryService
            .getBalance(related.getOriginalId(), related.getCompanyId(),
                String.valueOf(payType.getCode()));
        data.put(payType.getEnglishDesc() + "balance", balance);
      }
//            for (PayType payType : PayType.values()) {
//                String balance = channelHistoryService.getBalance(related.getOriginalId(), related.getCompanyId(), String.valueOf(payType.getCode()));
//                data.put(payType.getEnglishDesc() + "balance", balance);
//            }
      //商户在某个服务公司下发总余额
      data.put("balanceSum",
          channelHistoryService.getBalance(related.getOriginalId(), related.getCompanyId(), ""));
      data.put("customId", custom.getId());
      data.put("companyId", company.getCustomkey());
      //查询商户待确认充值金额
      Map<String, Object> params = new HashMap<>(10);
      params.put(CommonString.CUSTOMKEY, related.getOriginalId());
      params.put("recCustomkey", related.getCompanyId());
      params.put(CommonString.TRANSFERTYPE, "1");
      params.put(CommonString.STATUS, "0");
      List<ChannelHistory> channelHistorys = channelHistoryService.getChannelHistoryByParam(params);
      String waitConfirmedBalance = "0.00";
      for (ChannelHistory channelHistory : channelHistorys) {
        waitConfirmedBalance = ArithmeticUtil
            .addStr(waitConfirmedBalance, channelHistory.getAmount());
      }
      data.put("waitConfirmedBalance", waitConfirmedBalance);

      Map<String, Object> customRateConf = rateConfService
          .getCustomRateConf(related.getOriginalId(), related.getCompanyId());
      if (customRateConf != null) {
        data.putAll(customRateConf);
      }

      data.put("state", 0);

      if (!(customRateConf == null
          && ArithmeticUtil.compareTod(String.valueOf(data.get("balanceSum")), "0") <= 0
          && ArithmeticUtil.compareTod(String.valueOf(data.get("waitConfirmedBalance")), "0")
          <= 0)) {
        //增加状态避免余额为0，并且没有档位分页不对的问题
        data.put("state", 1);
      }

      result.add(data);

    }
    return returnSuccess(result, total);
  }

  /**
   * 账户信息
   */
  @RequestMapping(value = "/custom/accountMessage")
  public @ResponseBody
  Map<String, Object> details(HttpServletRequest request) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>(10);
    String payType = request.getParameter("payType");
    String companyId = request.getParameter("companyId");
    if (StringUtils.isEmpty(payType)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      return result;
    }
    try {
      String customkey = (String) request.getSession().getAttribute("customkey");
      ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(customkey, companyId);
      ChannelCustom channelCustom = customService.getCustomByCustomkey(related.getCompanyId());

      Map<String, Object> param = new HashMap<>(5);
      param.put("payType", payType);
      param.put("channelId", channelCustom.getId());
      ChannelConfig channelConfig = customService.getChannelConfigByParam(param);
      if (channelConfig == null) {
        respstat = RespCode.error107;
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "未开通该下发通道！");
        return result;
      }
      result.put("bankcardno", channelConfig.getAccountNum());
      result.put("bankname", channelConfig.getBankName());
      result.put("companyName", channelCustom.getCompanyName());
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "成功");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      respstat = RespCode.error107;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
      return result;
    }
    return result;
  }

  /**
   * 付款方账户信息
   */
  @RequestMapping(value = "/custom/originalData")
  public @ResponseBody
  Map<String, Object> originalData(HttpServletRequest request) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>(5);
    String payType = request.getParameter("payType");
    if (StringUtils.isEmpty(payType)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      return result;
    }
    try {
      String customkey = (String) request.getSession().getAttribute("customkey");
      ChannelCustom channelCustom = customService.getCustomByCustomkey(customkey);

      result.put("bankcardno", channelCustom.getBankcardno());
      result.put("bankname", channelCustom.getBankname());
      result.put("companyName", channelCustom.getCompanyName());
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "成功");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      respstat = RespCode.error107;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
      return result;
    }
    return result;
  }

  /**
   * 商户充值信息弹窗
   */
  @RequestMapping(value = "/custom/chargeInfo", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> getCusotmChargeInfo(Integer customId, Integer companyId, Integer payType) {

    ChannelCustom custom = customService.getCustomById(customId);
    Company company = companyService.getCompanyByUserId(companyId);

    if (custom == null || company == null) {
      return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
    }

    if (company.getStatus() == 2) {
      //下发公司废弃，状态不可用
      return returnFail(RespCode.error101, "服务公司充值限制，请联系客户经理");
    }
    Map<String, Object> result = new HashMap<>(10);
    result.put("payAccountName", custom.getCompanyName());
    result.put("payAccountNo", custom.getBankcardno());
    result.put("payAccountBankName", custom.getBankname());
    result.put("companyName", company.getCompanyName());
    result.put("customKey", custom.getCustomkey());
    result.put("companyId", company.getUserId());

    Map<String, Object> params = new HashMap<>(2);
    params.put("customkey", custom.getCustomkey());
    params.put("channelId", companyId);
    params.put("companyId", companyId);
    params.put("payType", payType);
    params.put("status", 1);

    Map<String, Object> data = new HashMap<>(10);
    List<CustomReceiveConfig> receiveConfigList = receiveConfigService
        .queryRechargeAccountListNoPape(params);
    if (receiveConfigList != null && receiveConfigList.size() > 0) {
      CustomReceiveConfig receiveConfig = receiveConfigList.get(0);
      data.put("inAccountNo", receiveConfig.getReceiveAccount());
      data.put("inAccountBankName", receiveConfig.getReceiveBank());
      data.put("inAccountName", receiveConfig.getReceiveUser());
      data.put("rechargeConfirmType", receiveConfig.getRechargeConfirmType());
    } else {
      List<ChannelConfig> channelConfigList = customService.getChannelConfigListByParam(params);
      if (channelConfigList != null && channelConfigList.size() > 0) {
        ChannelConfig channelConfig = channelConfigList.get(0);
        data.put("inAccountNo", channelConfig.getAccountNum());
        data.put("inAccountBankName", channelConfig.getBankName());
        data.put("inAccountName", channelConfig.getAccountName());
        data.put("rechargeConfirmType", channelConfig.getRechargeConfirmType());
      }
    }
    result.put("data", data);
    return returnSuccess(result);
  }

  /**
   * 商户充值明细
   */
  @RequestMapping(value = "/custom/accountList", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> companyAccountList(@RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime,
      @RequestParam(required = false, defaultValue = "1") Integer pageNo,
      @RequestParam(required = false, defaultValue = "10") Integer pageSize,
      @RequestParam(required = false) String rechargeAmount,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") Integer nodeId,
      @RequestParam(defaultValue = "0") Integer customType,
      @RequestParam(required = false) String payType,
      @RequestParam(required = false) String companyId,
      @RequestParam(required = false) String customName,
      @RequestParam(required = false) String orderNo,
      @RequestParam(required = false) Integer rechargeType) {
    // 商户标识
    List<String> originalIds = organizationTreeService
        .queryNodeCusotmKey(customType, QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
    StringBuilder originalIdStr = new StringBuilder();
    for (String originalId : originalIds) {
      originalIdStr.append(originalId).append(",");
    }

    if (customType == QueryType.COMPANY) {
      ChannelCustom custom = channelCustomService.getCustomById(nodeId);
      String customkey = custom.getCustomkey();
      originalIdStr.append(customkey).append(",");
    }

    Map<String, Object> result = new HashMap<>(5);
    Map<String, Object> paramMap = new HashMap<>(10);
    try {
      paramMap.put("customkey", originalIdStr.toString());
      paramMap.put("startTime", startTime);
      paramMap.put("endTime", endTime);
      paramMap.put("rechargeAmount", rechargeAmount);
      paramMap.put("status", status);
      paramMap.put("payType", payType);
      paramMap.put("companyId", companyId);
      paramMap.put("customName", customName);
      paramMap.put("orderNo", orderNo);
      paramMap.put("rechargeType", rechargeType);

      PageHelper.startPage(pageNo, pageSize);
      List<Map<String, Object>> list = channelHistoryService.geCustomChargeDetail(paramMap);
      PageInfo page = new PageInfo(list);
      result.put("total", page.getTotal());
      result.put("list", page.getList());

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.codeMaps.get(RespCode.error107));
    }
    return returnSuccess(result);
  }

  /**
   * 账户信息导出
   */
  @RequestMapping(value = "/custom/exportCustomData")
  public void exportCustomManage(ModelMap model, String startTime, String endTime, String amount,
      String payType,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    String customkey = (String) request.getSession().getAttribute("customkey");// 渠道名称
    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("customkey", customkey);
    paramMap.put("startTime", startTime);
    paramMap.put("endTime", endTime);
    paramMap.put("amount", amount);
    paramMap.put("payType", payType);
    List<ChannelHistory> list = channelHistoryService.getChannelHistoryBySubmit(paramMap);

    String today = DateUtils.getNowDay();
    ArrayList<String> dataStr = new ArrayList<String>();
    for (int i = 0; i < list.size(); i++) {
      ChannelHistory channelHistory = list.get(i);
      StringBuffer strBuff = new StringBuffer();
      String role = "";
      String payStatus = "";
      int pay = channelHistory.getPayType();
      if (pay == 1) {
        payStatus = "银行电子户";
      } else if (pay == 2) {
        payStatus = "支付宝";
      } else if (pay == 4) {
        payStatus = "银行卡";
      }

      int type = channelHistory.getTransfertype();
      if (type == 1) {
        role = "充值";
      }
      strBuff.append(role).append(",")
          .append(channelHistory.getAmount() == null ? "" : channelHistory.getAmount()).append(",")
          .append(payStatus).append(",")
          .append(channelHistory.getCreatetime() == null ? "" : channelHistory.getCreatetime());

      dataStr.add(strBuff.toString());
    }
    ArrayList<String> fieldName = new ArrayList<String>();
    fieldName.add("时间");
    fieldName.add("类别");
    fieldName.add("预存款账户");
    fieldName.add("金额");
    String filename = today + "账户列表";
    ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
  }

  /**
   * 商户充值信息弹窗下拉档位
   */
  @RequestMapping(value = "/custom/rateInfo", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> rateInfo(Integer customId, Integer companyId) {

    if (customId == null || companyId == null) {
      return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
    }

    Map<String, Object> result = new HashMap<>(5);

    try {
      ChannelCustom custom = customService.getCustomById(customId);
      Company company = companyService.getCompanyByUserId(companyId);
      if (custom == null || company == null) {
        return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
      }

      List<Map<String, Object>> rateConfList = rateConfService
          .getCustomRateConfList(custom.getCustomkey(), String.valueOf(company.getUserId()));
      if (rateConfList == null || rateConfList.size() == 0) {
        return returnFail(RespCode.error107, "未配置手续费费率档位");
      }

      result.put("serviceFeeType", rateConfList.get(0).get("serviceFeeType"));
      result.put("rateConfList", rateConfList);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }

    return returnSuccess(result);
  }

  /**
   * 计算充值可用金额
   */
  @RequestMapping(value = "/custom/countAmount", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> countAmount(Integer rateId, String amount) {

    if (rateId == null || StringUtil.isEmpty(amount)) {
      return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
    }

    if (ArithmeticUtil.compareTod(amount, "0") <= 0) {
      return returnFail(RespCode.error124, RespCode.AMOUNT_ERROR);
    }

    Map<String, Object> result = new HashMap<>(5);

    try {
      CustomCompanyRateConf rateConf = rateConfService.getById(rateId);
      if (rateConf == null) {
        return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
      }

      BigDecimal b1 = new BigDecimal(ArithmeticUtil.getFormatDouble(amount)); //可用金额
      BigDecimal b2 = new BigDecimal(ArithmeticUtil.getFormatDouble(rateConf.getCustomRate())); //费率
      NumberFormat nf = NumberFormat.getInstance();
      nf.setGroupingUsed(false);

      String serviceFee;

      //服务费计算规则为1  服务费=交易金额*服务费率
      if (rateConf.getFeeRuleType() == 1) {

        serviceFee = nf.format(b1.multiply(b2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

      } else if (rateConf.getFeeRuleType() == 2) {  //服务费计算规则为2 服务费=交易金额/(1-服务费率)*服务费率

        serviceFee = nf.format(
            b1.divide(new BigDecimal(1).subtract(b2), 2, BigDecimal.ROUND_HALF_UP).multiply(b2)
                .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

      } else {
        return returnFail(RespCode.error101, RespCode.FEE_RULE_TYEP_ERROR);
      }

      result.put("serviceFeeRate", ArithmeticUtil.mulStr(rateConf.getCustomRate(), "100", 2));
      result.put("serviceFee", serviceFee);
      result.put("rechargeAmount", ArithmeticUtil.addStr(amount, serviceFee));


    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }

    return returnSuccess(result);
  }


  /**
   * 商户充值信息实时查询第三方账户余额
   */
  @RequestMapping(value = "/custom/currentBalance", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> currentBalance(Integer customId, String payType) {

    if (customId == null) {
      return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
    }

    Map<String, Object> result = new HashMap<>(5);

    try {
      ChannelCustom custom = customService.getCustomById(customId);
      if (custom == null) {
        return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
      }

      LinkageBaseConfig baseConfig = linkageCustomConfigService
          .getConfigByCustomKey(custom.getCustomkey(), LinkageType.RECHARGENO.getCode());
      if (FundModelType.RECHARGE.getCode() == custom.getFundModelType() && baseConfig != null
          && !StringUtil.isEmpty(baseConfig.getPathNo())) {
        PaymentConfig paymentConfig = new PaymentConfig(baseConfig);
        //调用支付通道工厂模式
        Payment payment = PaymentFactory.paymentEntity(paymentConfig);
        //不使用交易方法，不传递UtilCacheManager
        PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, null);
        Payment proxy = paymentProxy.getProxy();

        String balance = String.valueOf(proxy.queryBalanceResult(payType).getAttachment());
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put("currentBalance", balance);
        result.put("pathNo", baseConfig.getPathNo());
//                result.put("fundModelType", custom.get());

        logger.info("-------查询联动充值商户customKey:{},pathNo:{},amount:{}------", custom.getCustomkey(),
            baseConfig.getPathNo(), balance);

        return returnSuccess(result);
      } else {
        result.put(RespCode.RESP_STAT, RespCode.error101);
        result.put(RespCode.RESP_MSG, RespCode.NOT_OPEN_LINKAGE);
        return returnSuccess(result);
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }

  }

  @ApiOperation("生成电子签名充值确认函")
  @RequestMapping(value = "/rechargeletter/esign", method = RequestMethod.POST)
  @ResponseBody
  public APIResponse generateRechargeLetter(@RequestParam String chanelHistoryId,
      @RequestParam Integer companyId) {

    ChannelHistory channelHistory = channelHistoryService.getChannelHistoryById(chanelHistoryId);
    if (channelHistory != null && channelHistory.getId() > 0) {
      if (RechargeLetterStatusType.PROCESSING.getCode()
          .equals(channelHistory.getRechargeLetterStatus())) {

        if (channelHistory.getRemark() != null && !"".equals(channelHistory.getRemark())) {
          String remark = channelHistory.getRemark();
          int start = remark.indexOf("本次支付共计人民币");
          int end = remark.indexOf("元");
          if (start != -1) {
            String amount = remark.substring(start + 9, end).trim();
            if (new BigDecimal(channelHistory.getRechargeAmount()).compareTo(new BigDecimal(amount))
                == 0) {
              CompanyEsignContractTemplate companyEsignContractTemplate = channelHistoryService
                  .getCompanyEsignContractTemplateByParams(companyId, new Byte("1"));
              if (companyEsignContractTemplate != null) {
                EsignContractDTO esignContractDTO = new EsignContractDTO();
                esignContractDTO.setAccountId(companyEsignContractTemplate.getAccountId());
                esignContractDTO.setProxyFlag(true);
                esignContractDTO.setMsgFlag(true);
                esignContractDTO.setProjectCode("wallet-sign");
                esignContractDTO.setSignLocation("Signature2");
                esignContractDTO.setEsignPlatform(companyEsignContractTemplate.getEsignPlatform());

                Map<String, String> textValueInfo = new HashMap<>();
                String date = null;
                try {
                  date = DateUtils
                      .formartDateStr(channelHistory.getCreatetime(), "yyyy-MM-dd", "yyyy年MM月dd");
                } catch (ParseException e) {
                  e.printStackTrace();
                }
                textValueInfo.put("remark", StringUtil.insertSubString(remark,"\r\n",34));
                textValueInfo.put("date", date);

                esignContractDTO.setTextValueInfo(textValueInfo);
                esignContractDTO.setTemplateId(companyEsignContractTemplate.getTemplateId());
                esignContractDTO.setContractName("服务费确认函");
                esignContractDTO.setSealId(companyEsignContractTemplate.getSealId());

                ServiceResponse serviceResponse = userServiceFeignClient
                    .createContract(esignContractDTO);
                if (serviceResponse != null && Constant.SERVICE_RESPONSE_CODE_SUCCESS
                    .equals(serviceResponse.getCode())) {
                  ContractDTO contract = JSONObject
                      .parseObject(JSONObject.toJSONString(serviceResponse.getData()),
                          ContractDTO.class);
                  if (contract != null && contract.getErrorCode() == 0) {
                    ChannelHistory history = new ChannelHistory();
                    history.setId(channelHistory.getId());
                    history.setRechargeLetterUrl(contract.getContractUrl());
                    history.setRechargeLetterStatus(new Byte("2"));
                    history.setRechargeLetterType(new Byte("2"));
                    channelHistoryService.updateChannelHistory(history);
                    return APIResponse.successResponse();
                  }
                  return APIResponse.errorResponse(ResponseCodeMapping.ERR_500);
                }
                return APIResponse.errorResponse(ResponseCodeMapping.ERR_500);
              }
              return APIResponse.errorResponse(ResponseCodeMapping.ERR_553);
            }
            return APIResponse.errorResponse(ResponseCodeMapping.ERR_556);
          }
          return APIResponse.errorResponse(ResponseCodeMapping.ERR_555);
        }
        return APIResponse.errorResponse(ResponseCodeMapping.ERR_555);
      }
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_552);
    }
    return APIResponse.errorResponse(ResponseCodeMapping.ERR_551);
  }




}
