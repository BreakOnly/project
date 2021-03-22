package com.jrmf.controller.merchant;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.jrmf.common.APIResponse;
import com.jrmf.common.CommonString;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.PayType;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.CustomBalanceHistory;
import com.jrmf.domain.ForwardCompanyAccountHistory;
import com.jrmf.domain.dto.ForwardCompanyAccountHistoryRequestDTO;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.CustomBalanceHistoryService;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.exception.LoginException;
import com.jrmf.utils.sms.channel.alibaba.AlibabaSMSChannel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "商户余额记录查询", tags = {"商户余额记录查询"})
@RestController
@RequestMapping("/custom/balance")
public class CustomBalanceHistoryController extends BaseController {

  private static Logger logger = LoggerFactory.getLogger(CustomBalanceHistoryController.class);

  @Autowired
  private CustomBalanceHistoryService customBalanceHistoryService;
  @Autowired
  private ChannelCustomService channelCustomService;


  /**
   * @Description 列表搜索
   **/
  @ApiOperation("商户余额记录查询列表")
  @PostMapping("/historyList")
  public Map<String, Object> historyList(HttpServletRequest request, String customKey, String
      startAmount, String endAmount, String companyId, String startTime, String endTime,
      String tradeType, String customName, Integer payType,String relateOrderNo,
      @RequestParam(required = false, defaultValue = "1") Integer pageNo,
      @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

    ChannelCustom loginCustom = (ChannelCustom) request.getSession()
        .getAttribute(CommonString.CUSTOMLOGIN);

    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("startAmount", startAmount);
    paramMap.put("endAmount", endAmount);
    paramMap.put("startTime", startTime);
    paramMap.put("endTime", endTime);
    paramMap.put("customName", customName);
    paramMap.put("payType", payType);
    paramMap.put("relateOrderNo",relateOrderNo);

    if (CustomType.COMPANY.getCode() == loginCustom.getCustomType()) {
      companyId = loginCustom.getCustomkey();
    } else if (CustomType.COMPANY.getCode() == loginCustom.getMasterCustomType()) {
      companyId = loginCustom.getMasterCustom();
    }

    paramMap.put("companyId", companyId);
    paramMap.put("tradeType", tradeType);

    List<String> customKeys = channelCustomService.getCustomKeysByLoginCustomExtent(loginCustom);
    if (!CommonString.ROOT.equals(customKeys.get(0))) {
      paramMap.put("customKey", Joiner.on(",").join(customKeys));
    }

    paramMap.put("equalCustomKey", customKey);

    PageHelper.startPage(pageNo, pageSize);
    List<CustomBalanceHistory> customBalanceHistoryList = customBalanceHistoryService
        .selectByParamMap(paramMap);
    PageInfo<CustomBalanceHistory> pageInfo = new PageInfo<>(customBalanceHistoryList);

    Map<String, Object> result = new HashMap<>(5);
    result.put("total", pageInfo.getTotal());
    result.put("list", pageInfo.getList());
    return returnSuccess(result);
  }


  @ApiOperation("商户余额记录查询列表 - 导出")
  @GetMapping("/historyList/export")
  public void export(HttpServletRequest request, HttpServletResponse response, String
      startAmount, String endAmount, String customKey, String companyId, String startTime, String endTime,
      String tradeType, String customName, Integer payType) {

    try {

      ChannelCustom loginCustom = (ChannelCustom) request.getSession()
          .getAttribute(CommonString.CUSTOMLOGIN);

      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("startAmount", startAmount);
      paramMap.put("endAmount", endAmount);
      paramMap.put("startTime", startTime);
      paramMap.put("endTime", endTime);
      paramMap.put("customName", customName);
      paramMap.put("payType", payType);

      if (CustomType.COMPANY.getCode() == loginCustom.getCustomType()) {
        companyId = loginCustom.getCustomkey();
      } else if (CustomType.COMPANY.getCode() == loginCustom.getMasterCustomType()) {
        companyId = loginCustom.getMasterCustom();
      }

      paramMap.put("companyId", companyId);
      paramMap.put("tradeType", tradeType);

      List<String> customKeys = channelCustomService.getCustomKeysByLoginCustomExtent(loginCustom);
      if (!CommonString.ROOT.equals(customKeys.get(0))) {
        paramMap.put("customKey", Joiner.on(",").join(customKeys));
      }

      paramMap.put("equalCustomKey", customKey);

      List<Map<String, Object>> data = new ArrayList<>();
      List<CustomBalanceHistory> customBalanceHistoryList = customBalanceHistoryService
          .selectByParamMap(paramMap);

      String[] headers = new String[]{"商户名称", "服务公司", "交易类型", "交易金额", "交易笔数","余额类型", "交易发生后余额",
          "备注", "交易时间"};

      for (CustomBalanceHistory history : customBalanceHistoryList) {
        Map<String, Object> dataMap = new HashMap<>(9);
        dataMap.put("1", history.getCustomName());
        dataMap.put("2", history.getCompanyName());
        dataMap.put("3", TradeType.codeOf(history.getTradeType()).getDesc());
        dataMap.put("4", history.getTradeAmount());
        dataMap.put("5", history.getTradeNumber());
        dataMap.put("6", PayType.codeOf(history.getPayType()).getDesc());
        dataMap.put("7", history.getAfterTradeBalance());
        dataMap.put("8", history.getRemark());
        dataMap.put("9", history.getCreateTime());
        data.add(dataMap);
      }
      ExcelFileGenerator.ExcelExport(response, headers, "商户余额记录", data);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

}
