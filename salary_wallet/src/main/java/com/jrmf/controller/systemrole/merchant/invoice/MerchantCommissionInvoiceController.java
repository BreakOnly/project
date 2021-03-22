package com.jrmf.controller.systemrole.merchant.invoice;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.Constant;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.InvoiceMethodType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.CommissionInvoice;
import com.jrmf.domain.Company;
import com.jrmf.domain.PersonalIncomeTaxRate;
import com.jrmf.domain.QbInvoiceRecord;
import com.jrmf.domain.UserCommission;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.CompanyService;
import com.jrmf.service.PersonalIncomeTaxRateService;
import com.jrmf.service.QbInvoiceRecordService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.taxsettlement.util.cache.RedisCluster;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auth honglin
 * @time 2020/7/15 下午6:43
 * @desc Simple describe the file functionality
 */
@RestController
@RequestMapping("/commissionInvoice")
public class MerchantCommissionInvoiceController extends BaseController {
  private static Logger logger = LoggerFactory.getLogger(MerchantCommissionInvoiceController.class);

  @Autowired
  UserCommissionService userCommissionService;
  @Autowired
  QbInvoiceRecordService qbInvoiceRecordService;
  @Autowired
  PersonalIncomeTaxRateService personalIncomeTaxRateService;
  @Autowired
  ChannelCustomService channelCustomService;
  @Autowired
  CompanyService companyService;
  @Autowired
  RedisCluster redisCluster;


  @PostMapping("/list")
  public Map<String, Object> commissionInvoiceList(CommissionInvoice commissionInvoice,
      HttpServletRequest request){
    Map<String,Object> resultMap = new HashMap<>();
    resultMap.put(RespCode.RESP_STAT, RespCode.success);
    resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
    List<CommissionInvoice> resultDataList = new ArrayList<>();
    resultMap.put("customInvoiceList",resultDataList);
    //检查商户权限
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    logger.info("实发开票查询记录开始，当前登录商户{}",customLogin == null ? "未登录":customLogin.getCompanyName());
    if (customLogin != null){
      if (isCompany(customLogin)) {
        resultMap.put(RespCode.RESP_STAT, RespCode.error101);
        resultMap.put(RespCode.RESP_MSG, RespCode.PERMISSION_ERROR);
        return resultMap;
      }
      //获取该登录商户下可以访问的customKey
      String currOriginalIds = channelCustomService.getCustomKeysByLoginMerchant(customLogin);
      //获取支持下发开票的服务公司
      Map<String,Object> companyParam = new HashMap<>();
      companyParam.put("invoiceCategory",2);
      List<Company> companyList = companyService.getCompanyListByParam(companyParam);
      if (companyList == null || companyList.isEmpty()){
        resultMap.put(RespCode.RESP_STAT, RespCode.COMPANY_NOT_FOUND);
        resultMap.put(RespCode.RESP_MSG, RespCode.CUSTOM_COMPANY_NOT_RELATED);
        return resultMap;
      }
      StringBuilder sb = new StringBuilder();
      companyList.forEach(company -> sb.append(company.getUserId()).append(","));
      String companyIds = sb.toString();
      if (commissionInvoice.getPageNo() == null || commissionInvoice.getPageSize() == null){
        commissionInvoice.setPageNo(1);
        commissionInvoice.setPageSize(10);
      }
      commissionInvoice.setOriginalId(currOriginalIds);
      commissionInvoice.setCompanyId(companyIds);
    }

    //1 分页获取月份，商户、服务公司
    //如果开始的月份小于2020年8月设置成2020-08
    if (commissionInvoice.getStartMonth() == null ||
        commissionInvoice.getStartMonth().compareTo(Constant.COMMISSION_INVOICE_START_MONTH) < 0){
      commissionInvoice.setStartMonth(Constant.COMMISSION_INVOICE_START_MONTH);
    }
    PageHelper.startPage(commissionInvoice.getPageNo(),commissionInvoice.getPageSize(),true);
    List<Map<String, Object>> invoiceCustomInfos = userCommissionService.getInvoiceCustomInfos(commissionInvoice);
    PageInfo page = new PageInfo(invoiceCustomInfos);
    resultMap.put("totalRecord", page.getTotal());
    resultMap.put("totalPage", page.getPages());
    if (invoiceCustomInfos == null || invoiceCustomInfos.isEmpty()) {
      return resultMap;
    }
    Map<String,List<PersonalIncomeTaxRate>> personalTaxRatesMap = new HashMap<>();
    //遍历分页查询出来的商户数据，获取下发记录详情
    for (Map<String, Object> stringObjectMap : invoiceCustomInfos) {
      CommissionInvoice invoiceItem = new CommissionInvoice();
      String month = (String) stringObjectMap.get("month");
      String originalId = (String) stringObjectMap.get("originalId");
      String customName = (String) stringObjectMap.get("customName");
      String companyId = (String) stringObjectMap.get("companyId");
      String companyName = (String) stringObjectMap.get("companyName");
      String invoiceStatus2s = (String) stringObjectMap.get("invoiceStatus2");
      //获取服务公司个税档位列表
      List<PersonalIncomeTaxRate> taxRateList = personalIncomeTaxRateService.getCompanyTaxRateList(companyId);
      if (taxRateList == null || taxRateList.isEmpty()){
        resultMap.put(RespCode.RESP_STAT, RespCode.BUSINESS_TYPE_NOT);
        resultMap.put(RespCode.RESP_MSG, String.format(RespCode.COMPANY_NOT_TAX_RAX,companyName));
        return resultMap;
      }
      if (!personalTaxRatesMap.containsKey(companyId)) {
        personalTaxRatesMap.put(companyId,taxRateList);
      }
      //开票状态 0 未开票，1 开票处理中，2 开票完成 3 开票失败 4 部分开票-计算出来的状态 5 可开票-包括开票失败和未开票 9 临时状态-开票已计算
      //开票处理中：当待开票金额为0时，已开票总额中有发票处理中的订单，开票状态为开票处理中；当待开票金额不为0时，无论是否有开票处理中的订单，开票状态都为部分开票。
      if ((invoiceStatus2s.contains("0") || invoiceStatus2s.contains("3"))
          && (invoiceStatus2s.contains("1") || invoiceStatus2s.contains("2"))){
        //部分开票
        invoiceItem.setInvoiceStatus("4");
      } else if (invoiceStatus2s.contains("1") && !invoiceStatus2s.contains("0") && !invoiceStatus2s.contains("3")){
        //开票处理中
        invoiceItem.setInvoiceStatus("1");
      }else{
        //未开票
        invoiceItem.setInvoiceStatus("0");
      }
      //获取下发明细
      Map<String,Object> params = new HashMap<>();
      params.put("originalId",originalId);
      params.put("companyId",companyId);
      params.put("startTime", DateUtils.monthToStartTime(month + "-01"));
      params.put("endTime", DateUtils.monthToEndTime(month + "-01"));
      invoiceItem.setMonth(month);
      invoiceItem.setCustomName(customName);
      invoiceItem.setOriginalId(originalId);
      invoiceItem.setCompanyId(companyId);
      invoiceItem.setCompanyName(companyName);
      List<UserCommission> userCommissionList = userCommissionService.getUserCommissionInvoiceRecord(params);
      for (int i = 0; i < userCommissionList.size(); i++) {
        UserCommission userCommission = userCommissionList.get(i);
        //交易总金额
        String commissionAmount = invoiceItem.getCommissionAmount();
        invoiceItem.setCommissionAmount(ArithmeticUtil.addStr(commissionAmount,userCommission.getAmount(),2));
        //服务费率
        String serviceRate = invoiceItem.getServiceRate();
        if (StringUtil.isEmpty(serviceRate)){
          invoiceItem.setServiceRate(ArithmeticUtil.mulStr(userCommission.getCalculationRates(),"100",2,BigDecimal.ROUND_HALF_UP));
        }else{
          if (!serviceRate.contains(ArithmeticUtil.mulStr(userCommission.getCalculationRates(),"100",2,BigDecimal.ROUND_HALF_UP))) {
            invoiceItem.setServiceRate(serviceRate + "," + ArithmeticUtil.mulStr(userCommission.getCalculationRates(),"100",2,BigDecimal.ROUND_HALF_UP));
          }
        }
        //服务费
        String serviceCharge = invoiceItem.getServiceCharge();
        invoiceItem.setServiceCharge(ArithmeticUtil.addStr(serviceCharge,userCommission.getSumFee(),2));
        //操作者
        String operator = invoiceItem.getOperator();
        String operatorName = userCommission.getOperatorName();
        if (!StringUtil.isEmpty(operatorName) && StringUtil.isEmpty(operator)){
          invoiceItem.setOperator(operatorName);
        }else{
          if (!StringUtil.isEmpty(operatorName) && !operator.contains(operatorName)){
            invoiceItem.setOperator(operator + "," + operatorName);
          }
        }
        //计算个税相关
        int invoiceStatus2 = userCommission.getInvoiceStatus2();
        if (9 != invoiceStatus2){
          calculateTax(i,userCommissionList.size(),userCommission.getUserId(), companyId,personalTaxRatesMap,userCommissionList,invoiceItem);
        }
      }
      //待开服务费票
      String totalInvoiceAmount = invoiceItem.getTotalInvoiceAmount();
      String taxInvoiceAmount = invoiceItem.getTaxInvoiceAmount();
      invoiceItem.setServiceChargeInvoiceAmount(ArithmeticUtil.subStr2(totalInvoiceAmount,taxInvoiceAmount));
      //已开服务费票
      String totalInvoicedAmount = invoiceItem.getTotalInvoicedAmount();
      String taxInvoicedAmount = invoiceItem.getTaxInvoicedAmount();
      invoiceItem.setServiceChargeInvoicedAmount(ArithmeticUtil.subStr2(totalInvoicedAmount,taxInvoicedAmount));
      //发票类型
      invoiceItem.setInvoiceType("实发申请");
      resultDataList.add(invoiceItem);
    }
    return resultMap;
  }

  @GetMapping("/export")
  public void exportCommissionInvoiceList(HttpServletRequest request, HttpServletResponse response){
    Map<String,Object> resultMap = new HashMap<>();
    resultMap.put(RespCode.RESP_STAT, RespCode.success);
    resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
    List<CommissionInvoice> resultDataList = new ArrayList<>();
    resultMap.put("customInvoiceList",resultDataList);
    //获取获取请求参数
    CommissionInvoice commissionInvoice = new CommissionInvoice();
    commissionInvoice.setStartMonth(request.getParameter("startMonth"));
    commissionInvoice.setEndMonth(request.getParameter("endMonth"));
    commissionInvoice.setCompanyName(request.getParameter("companyName"));
    commissionInvoice.setCustomName(request.getParameter("customName"));
    commissionInvoice.setInvoiceStatus(request.getParameter("invoiceStatus"));

    //检查商户权限
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    logger.info("实发开票导出记录开始，当前登录商户{}",customLogin == null ? "未登录":customLogin.getCompanyName());
    if (customLogin != null){
      if (isCompany(customLogin)) {
        resultMap.put(RespCode.RESP_STAT, RespCode.error101);
        resultMap.put(RespCode.RESP_MSG, RespCode.PERMISSION_ERROR);
        return;
      }
      //获取该登录商户下可以访问的customKey
      String currOriginalIds = channelCustomService.getCustomKeysByLoginMerchant(customLogin);
      //获取支持下发开票的服务公司
      Map<String,Object> companyParam = new HashMap<>();
      companyParam.put("invoiceCategory",2);
      List<Company> companyList = companyService.getCompanyListByParam(companyParam);
      if (companyList == null || companyList.isEmpty()){
        resultMap.put(RespCode.RESP_STAT, RespCode.COMPANY_NOT_FOUND);
        resultMap.put(RespCode.RESP_MSG, RespCode.CUSTOM_COMPANY_NOT_RELATED);
        return;
      }
      StringBuilder sb = new StringBuilder();
      companyList.forEach(company -> sb.append(company.getUserId()).append(","));
      String companyIds = sb.toString();
      commissionInvoice.setOriginalId(currOriginalIds);
      commissionInvoice.setCompanyId(companyIds);
    }
    //1 分页获取月份，商户、服务公司
    //如果开始的月份小于2020年8月,设置为2020-08
    if (commissionInvoice.getStartMonth() == null ||
        commissionInvoice.getStartMonth().compareTo(Constant.COMMISSION_INVOICE_START_MONTH) < 0){
      commissionInvoice.setStartMonth(Constant.COMMISSION_INVOICE_START_MONTH);
    }
    List<Map<String, Object>> invoiceCustomInfos = userCommissionService.getInvoiceCustomInfos(commissionInvoice);
    if (invoiceCustomInfos == null || invoiceCustomInfos.isEmpty()) {
      return;
    }
    Map<String,List<PersonalIncomeTaxRate>> personalTaxRatesMap = new HashMap<>();
    //遍历分页查询出来的商户数据，获取下发记录详情
    for (Map<String, Object> stringObjectMap : invoiceCustomInfos) {
      CommissionInvoice invoiceItem = new CommissionInvoice();
      String month = (String) stringObjectMap.get("month");
      String originalId = (String) stringObjectMap.get("originalId");
      String customName = (String) stringObjectMap.get("customName");
      String companyId = (String) stringObjectMap.get("companyId");
      String companyName = (String) stringObjectMap.get("companyName");
      String invoiceStatus2s = (String) stringObjectMap.get("invoiceStatus2");
      //获取服务公司个税档位列表
      List<PersonalIncomeTaxRate> taxRateList = personalIncomeTaxRateService.getCompanyTaxRateList(companyId);
      if (taxRateList == null || taxRateList.isEmpty()){
        resultMap.put(RespCode.RESP_STAT, RespCode.BUSINESS_TYPE_NOT);
        resultMap.put(RespCode.RESP_MSG, String.format(RespCode.COMPANY_NOT_TAX_RAX,companyName));
        return;
      }
      if (!personalTaxRatesMap.containsKey(companyId)) {
        personalTaxRatesMap.put(companyId,taxRateList);
      }
      //开票状态 0 未开票，1 开票处理中，2 开票完成 3 开票失败 4 部分开票-计算出来的状态 9 临时状态-开票已计算
      //开票处理中：当待开票金额为0时，已开票总额中有发票处理中的订单，开票状态为开票处理中；当待开票金额不为0时，无论是否有开票处理中的订单，开票状态都为部分开票。
      if ((invoiceStatus2s.contains("0") || invoiceStatus2s.contains("3"))
          && (invoiceStatus2s.contains("1") || invoiceStatus2s.contains("2"))){
        //部分开票
        invoiceItem.setInvoiceStatus("4");
      } else if (invoiceStatus2s.contains("1") && !invoiceStatus2s.contains("0") && !invoiceStatus2s.contains("3")){
        //开票处理中
        invoiceItem.setInvoiceStatus("1");
      }else{
        //未开票
        invoiceItem.setInvoiceStatus("0");
      }
      //获取下发明细
      Map<String,Object> params = new HashMap<>();
      params.put("originalId",originalId);
      params.put("companyId",companyId);
      params.put("startTime", DateUtils.monthToStartTime(month + "-01"));
      params.put("endTime", DateUtils.monthToEndTime(month + "-01"));
      invoiceItem.setMonth(month);
      invoiceItem.setCustomName(customName);
      invoiceItem.setOriginalId(originalId);
      invoiceItem.setCompanyId(companyId);
      invoiceItem.setCompanyName(companyName);
      List<UserCommission> userCommissionList = userCommissionService.getUserCommissionInvoiceRecord(params);
      for (int i = 0; i < userCommissionList.size(); i++) {
        UserCommission userCommission = userCommissionList.get(i);
        //交易总金额
        String commissionAmount = invoiceItem.getCommissionAmount();
        invoiceItem.setCommissionAmount(ArithmeticUtil.addStr(commissionAmount,userCommission.getAmount(),2));
        //服务费率
        String serviceRate = invoiceItem.getServiceRate();
        if (StringUtil.isEmpty(serviceRate)){
          invoiceItem.setServiceRate(ArithmeticUtil.mulStr(userCommission.getCalculationRates(),"100",2,BigDecimal.ROUND_HALF_UP));
        }else{
          if (!serviceRate.contains(ArithmeticUtil.mulStr(userCommission.getCalculationRates(),"100",2,BigDecimal.ROUND_HALF_UP))) {
            invoiceItem.setServiceRate(serviceRate + "," + ArithmeticUtil.mulStr(userCommission.getCalculationRates(),"100",2,BigDecimal.ROUND_HALF_UP));
          }
        }
        //服务费
        String serviceCharge = invoiceItem.getServiceCharge();
        invoiceItem.setServiceCharge(ArithmeticUtil.addStr(serviceCharge,userCommission.getSumFee(),2));
        //操作者
        String operator = invoiceItem.getOperator();
        String operatorName = userCommission.getOperatorName();
        if (!StringUtil.isEmpty(operatorName) && StringUtil.isEmpty(operator)){
          invoiceItem.setOperator(operatorName);
        }else{
          if (!StringUtil.isEmpty(operatorName) && !operator.contains(operatorName)){
            invoiceItem.setOperator(operator + "," + operatorName);
          }
        }
        //计算个税相关
        int invoiceStatus2 = userCommission.getInvoiceStatus2();
        if (9 != invoiceStatus2){
          calculateTax(i,userCommissionList.size(),userCommission.getUserId(), companyId,personalTaxRatesMap,userCommissionList,invoiceItem);
        }
      }
      //待开服务费票
      String totalInvoiceAmount = invoiceItem.getTotalInvoiceAmount();
      String taxInvoiceAmount = invoiceItem.getTaxInvoiceAmount();
      invoiceItem.setServiceChargeInvoiceAmount(ArithmeticUtil.subStr2(totalInvoiceAmount,taxInvoiceAmount));
      //已开服务费票
      String totalInvoicedAmount = invoiceItem.getTotalInvoicedAmount();
      String taxInvoicedAmount = invoiceItem.getTaxInvoicedAmount();
      invoiceItem.setServiceChargeInvoicedAmount(ArithmeticUtil.subStr2(totalInvoicedAmount,taxInvoicedAmount));
      //发票类型
      invoiceItem.setInvoiceType("实发申请");
      resultDataList.add(invoiceItem);
    }
    //导出数据
    List<Map<String, Object>> data = new ArrayList<>();
    String filename = "实发开票记录";
    String[] headers = new String[] { "月份", "商户名称", "服务公司","商户交易总金额","服务费率","服务费","个税税率","个税税额","开票状态","待开票总额",
        "待开实发个税票","待开服务费票","已开票总额","已开实发个税票","已开服务费票","类别","操作账号"};
    for (CommissionInvoice invoice : resultDataList) {
      Map<String, Object> dataMap = new HashMap<>(18);
      dataMap.put("1",invoice.getMonth());
      dataMap.put("2",invoice.getCustomName());
      dataMap.put("3",invoice.getCompanyName());
      dataMap.put("4",invoice.getCommissionAmount());
      String serviceRate = invoice.getServiceRate();
      if (!StringUtil.isEmpty(serviceRate)){
        String[] serviceRateArray = serviceRate.split(",");
        StringBuilder sbServiceRate = new StringBuilder();
        for (String s : serviceRateArray) {
          sbServiceRate.append(s).append("%").append(",");
        }
        sbServiceRate.deleteCharAt(sbServiceRate.length() - 1);
        serviceRate = sbServiceRate.toString();
      }
      dataMap.put("5",serviceRate);
      dataMap.put("6",invoice.getServiceCharge());
      String taxRate = invoice.getTaxRate();
      if (!StringUtil.isEmpty(taxRate)){
        String[] taxRateArray = taxRate.split(",");
        StringBuilder sbTaxRate = new StringBuilder();
        for (String s : taxRateArray) {
          sbTaxRate.append(s).append("%").append(",");
        }
        sbTaxRate.deleteCharAt(sbTaxRate.length() - 1);
        taxRate = sbTaxRate.toString();
      }
      dataMap.put("7",taxRate);
      dataMap.put("8",invoice.getTaxAmount());
      //开票状态 0 未开票，1 开票处理中，2 开票完成 3 开票失败 4 部分开票-计算出来的状态 9 临时状态-开票已计算
      String invoiceStatus = invoice.getInvoiceStatus();
      if ("4".equals(invoiceStatus)){
        invoiceStatus = "可开票(部分开票)";
      }else if ("1".equals(invoiceStatus)){
        invoiceStatus = "开票处理中";
      }else{
        invoiceStatus = "可开票（未开票）";
      }
      dataMap.put("9",invoiceStatus);
      dataMap.put("10",invoice.getTotalInvoiceAmount());
      dataMap.put("11",invoice.getTaxInvoiceAmount());
      dataMap.put("12",invoice.getServiceChargeInvoiceAmount());
      dataMap.put("13",invoice.getTotalInvoicedAmount());
      dataMap.put("14",invoice.getTaxInvoicedAmount());
      dataMap.put("15",invoice.getServiceChargeInvoicedAmount());
      dataMap.put("16",invoice.getInvoiceType());
      dataMap.put("17",invoice.getOperator());
      data.add(sortMapByKey(dataMap));
    }
    ExcelFileGenerator.ExcelExport(response, headers, filename, data);
  }

  /**所有下发记录计算*/
  private void calculateTax(int firstIndex,int endIndex,String userId,String companyId,
      Map<String,List<PersonalIncomeTaxRate>> personalTaxRatesMap,
      List<UserCommission> userCommissionList,CommissionInvoice invoiceItem){
    //该用户当月下发总金额
    String userTotalAmount = "0";
    for (int i = firstIndex; i < endIndex; i++) {
      UserCommission userCommission = userCommissionList.get(i);
      if (!userId.equals(userCommission.getUserId())){
        break;
      }
      String amount = userCommission.getAmount();
      userTotalAmount = ArithmeticUtil.addStr(userTotalAmount,amount,2);

      String sumFee = userCommission.getSumFee();
      //开票金额 = 下发金额 + 服务费
      String totalInvoiceAmount = ArithmeticUtil.addStr(amount,sumFee,2);
      //待开票总金额
      if (userCommission.getInvoiceStatus2() == 0 || userCommission.getInvoiceStatus2() == 3){
        String existTotalInvoiceAmount = invoiceItem.getTotalInvoiceAmount();
        invoiceItem.setTotalInvoiceAmount(ArithmeticUtil.addStr(existTotalInvoiceAmount,totalInvoiceAmount,2));
      }
      //已开票总金额
      if (userCommission.getInvoiceStatus2() == 1 || userCommission.getInvoiceStatus2() == 2){
        //已开票总金额
        String existInvoicedAmount = invoiceItem.getTotalInvoicedAmount();
        invoiceItem.setTotalInvoicedAmount(ArithmeticUtil.addStr(existInvoicedAmount,totalInvoiceAmount,2));
      }
    }
    //获得个税档位
    String taxRate = userCommissionService.getTaxRate(userTotalAmount,companyId,personalTaxRatesMap);
    //个税税率
    String existTaxRate = invoiceItem.getTaxRate();
    if (StringUtil.isEmpty(existTaxRate)){
      invoiceItem.setTaxRate(ArithmeticUtil.mulStr(taxRate,"100",2,BigDecimal.ROUND_HALF_UP));
    }else{
      String[] taxRates = existTaxRate.split(",");
      boolean flag = true;
      for (String rate : taxRates) {
        if (ArithmeticUtil.compareTod(ArithmeticUtil.mulStr(taxRate,"100",2,BigDecimal.ROUND_HALF_UP),rate) == 0) {
          flag = false;
        }
      }
      if (flag){
        invoiceItem.setTaxRate(existTaxRate +"," + ArithmeticUtil.mulStr(taxRate,"100",2,BigDecimal.ROUND_HALF_UP));
      }
    }
    if (ArithmeticUtil.compareTod(taxRate,"0") <= 0){//个税档位小于0
      //个税税额
      String existTaxAmount = invoiceItem.getTaxAmount();
      if (StringUtil.isEmpty(existTaxAmount)){
        invoiceItem.setTaxAmount("0.00");
      }
      //更改状态
      for (int i = firstIndex; i < endIndex; i++){
        UserCommission userCommission = userCommissionList.get(i);
        if (!userId.equals(userCommission.getUserId())){
          break;
        }
        if (userCommission.getInvoiceStatus2() == 0 || userCommission.getInvoiceStatus2() == 3){
          //待开实发个税票
          String existTaxInvoiceAmount = invoiceItem.getTaxInvoiceAmount();
          invoiceItem.setTaxInvoiceAmount(ArithmeticUtil.addStr(existTaxInvoiceAmount,userCommission.getAmount(),2));
          //更新下发记录、开票流水、个税金额-查询接口不需要更新
        }
        if (userCommission.getInvoiceStatus2() == 1 || userCommission.getInvoiceStatus2() == 2){
          //已开实发个税票=下发金额+已开个税票
          String existTaxInvoicedAmount = invoiceItem.getTaxInvoicedAmount();
          String individualTax = userCommission.getIndividualTax();
          String individualBackTax = userCommission.getIndividualBackTax();
          String individualSum = ArithmeticUtil.addStr(individualTax, individualBackTax, 2);

          String amount = userCommission.getAmount();
          String taxInvoicedAmount = ArithmeticUtil
              .addStr(ArithmeticUtil.addStr(existTaxInvoicedAmount, individualSum,2), amount,2);
          invoiceItem.setTaxInvoicedAmount(taxInvoicedAmount);
        }
        userCommission.setInvoiceStatus2(9);
      }
    }else{//个税档位大于0
      //个税税额
      String existTaxAmount = invoiceItem.getTaxAmount();
      invoiceItem.setTaxAmount(ArithmeticUtil.addStr(existTaxAmount,ArithmeticUtil.mulStr(userTotalAmount,taxRate,2,BigDecimal.ROUND_HALF_UP),2));
      //遍历下发记录，计算已开、待开票 金额
      for (int i = firstIndex; i < endIndex; i++) {
        UserCommission userCommission = userCommissionList.get(i);
        if (!userId.equals(userCommission.getUserId())){
          break;
        }
        // 0 未开票，1 开票处理中，2 开票完成 3 开票失败 4 部分开票 9 临时状态-开票已计算
        //当用用户a，在商户A下发，第一次提交发票时，实发金额1<个税税率范围，第二次提交发票时，实发金额2+实发金额1>个税税率范围，两次的计算公式分别为：
        //第一次提交：实发个税票=实发金额1      服务费票=实发金额*服务费率
        //第二次提交：实发个税票=实发金额2+（实发金额1+实发金额2）*个税税率     服务费票=实发金额2*服务费率-（实发金额1+实发金额2）*个税税率
        String amount = userCommission.getAmount();
        //待开实发个税票 计算该笔下发记录实际个税和已经开过的个税的差额
        String existTaxInvoiceAmount = invoiceItem.getTaxInvoiceAmount();
        String taxAmount = ArithmeticUtil.mulStr(amount,taxRate,2,BigDecimal.ROUND_HALF_UP);
        String individualTax = userCommission.getIndividualTax();
        String individualBackTax = userCommission.getIndividualBackTax();
        String diffTaxAmount = ArithmeticUtil
            .subStr2(ArithmeticUtil.subStr2(taxAmount, individualTax), individualBackTax);
        String taxInvoiceAmount = diffTaxAmount;
        if (userCommission.getInvoiceStatus2() == 0 || userCommission.getInvoiceStatus2() == 3){
          //如果是未开票状态还需要加上下发金额
          taxInvoiceAmount = ArithmeticUtil.addStr(amount,diffTaxAmount,2);
          //更新下发记录、开票流水、个税金额-查询接口不需要更新
        }
        invoiceItem.setTaxInvoiceAmount(ArithmeticUtil.addStr(existTaxInvoiceAmount,taxInvoiceAmount,2));
        //待开服务费票-通过计算所得

        if (userCommission.getInvoiceStatus2() == 1 || userCommission.getInvoiceStatus2() == 2){
          //已开实发个税票
          String existTaxInvoicedAmount = invoiceItem.getTaxInvoicedAmount();
          String individualSum = ArithmeticUtil.addStr(individualTax, individualBackTax, 2);
          String taxInvoicedAmount = ArithmeticUtil
              .addStr(ArithmeticUtil.addStr(existTaxInvoicedAmount, individualSum,2), amount,2);
          invoiceItem.setTaxInvoicedAmount(taxInvoicedAmount);
          //已开服务费票-通过计算所得
        }
        userCommission.setInvoiceStatus2(9);
      }
    }
  }

  @PostMapping("/submit")
  public Map<String, Object> commissionInvoiceSubmit(QbInvoiceRecord invoiceRecord,HttpServletRequest request){
    Map<String,Object> resultMap = new HashMap<>();
    resultMap.put(RespCode.RESP_STAT, RespCode.success);
    resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
    //检查商户权限
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    logger.info("实发开票提交开始，当前登录商户{}",customLogin == null ? "未登录":customLogin.getCompanyName());
    if (customLogin != null){
      if (isCompany(customLogin)) {
        resultMap.put(RespCode.RESP_STAT, RespCode.error101);
        resultMap.put(RespCode.RESP_MSG, RespCode.PERMISSION_ERROR);
        return resultMap;
      }
    }
    String months = invoiceRecord.getMonths();
    String companyId = invoiceRecord.getCompanyId();
    String customkey = invoiceRecord.getCustomkey();
    String invoiceTypes = invoiceRecord.getInvoiceTypes();
    if (StringUtil.isEmpty(months) || StringUtil.isEmpty(customkey) || StringUtil.isEmpty(companyId)
        || StringUtil.isEmpty(invoiceTypes)){
      resultMap.put(RespCode.RESP_STAT, RespCode.error101);
      resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
      return resultMap;
    }
    //获取服务公司个税档位
    Map<String,List<PersonalIncomeTaxRate>> personalTaxRatesMap = new HashMap<>();
    List<PersonalIncomeTaxRate> taxRateList = personalIncomeTaxRateService.getCompanyTaxRateList(companyId);
    if (taxRateList == null || taxRateList.isEmpty()){
      resultMap.put(RespCode.RESP_STAT, RespCode.BUSINESS_TYPE_NOT);
      resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.BUSINESS_TYPE_NOT));
      return resultMap;
    }
    personalTaxRatesMap.put(companyId,taxRateList);

    String[] monthArray = months.split(",");
    Map<String,Object> params = new HashMap<>();
    List<UserCommission> allUserCommissionList = new ArrayList<>();
    for (String month : monthArray) {
      // 0 未开票，1 开票处理中，2 开票完成 3 开票失败 4 部分开票 9 临时状态-开票已计算
      params.put("originalId",customkey);
      params.put("companyId",companyId);
      params.put("startTime", DateUtils.monthToStartTime(month + "-01"));
      params.put("endTime", DateUtils.monthToEndTime(month + "-01"));
      List<UserCommission> userCommissionList = userCommissionService.getUserCommissionInvoiceRecord(params);
      allUserCommissionList.addAll(userCommissionList);
      userCommissionList.clear();
    }
    //循环获取商户信息，计算开票金额、添加开票流水号、更改开票状态
    //生成开票流水号
    String invoiceSerialNo = "P"+ OrderNoUtil.getOrderNo();
    if (customLogin != null){
      invoiceRecord.setAddUser(customLogin.getUsername());
    }
    invoiceRecord.setStatus(1);
    invoiceRecord.setIsDiscard(0);
    invoiceRecord.setCreateTime(DateUtils.getNowDate());
    invoiceRecord.setIsDiscard(0);

    CommissionInvoice invoiceItem = new CommissionInvoice();
    invoiceItem.setInvoiceSerialNo(invoiceSerialNo);
    //校验同一个商户同一个服务公司不能并发开票
    String invoiceSubmitKey = "commission_invoice_" + customkey + "_" + companyId;
    boolean notExist = redisCluster.putIfAbsent(invoiceSubmitKey, 1, 60);
    if (!notExist){
      resultMap.put(RespCode.RESP_STAT, RespCode.COMMISSION_INVOICE_PROCESSING);
      resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.COMMISSION_INVOICE_PROCESSING));
      return resultMap;
    }
    //计算开票金额-待开实发个税票、待开服务费票
    for (int i = 0; i < allUserCommissionList.size(); i++) {
      UserCommission userCommission = allUserCommissionList.get(i);
      try{
        //计算个税相关
        int invoiceStatus2 = userCommission.getInvoiceStatus2();
        if (9 != invoiceStatus2){
          userCommissionService.calculateInvoiceAmountAndUpdateCommission(i,allUserCommissionList.size(),userCommission.getUserId(),
              userCommission.getCompanyId(),personalTaxRatesMap,allUserCommissionList,invoiceItem,invoiceRecord);
        }
      }catch (Exception e){
        resultMap.put(RespCode.RESP_STAT, RespCode.error000);
        resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error000));
        return resultMap;
      }finally {
        redisCluster.remove(invoiceSubmitKey);
      }
    }
    //金额计算完成、下发记录状态修改完成、生成发票信息.包括实发个税票和服务费票
    invoiceRecord.setOrderNo(invoiceSerialNo);
    invoiceRecord.setInvoiceSerialNo(invoiceSerialNo + "_" + 1);
    String[] invoiceTypeArray = invoiceTypes.split(",");
    String taxInvoiceAmount = invoiceItem.getTaxInvoiceAmount();
    if (ArithmeticUtil.compareTod(taxInvoiceAmount,"0") <= 0){
      resultMap.put(RespCode.RESP_STAT, RespCode.error000);
      resultMap.put(RespCode.RESP_MSG, RespCode.DEDUCT_AMOUNT_ERROR);
      return resultMap;
    }
    invoiceRecord.setInvoiceMethod(InvoiceMethodType.COMMISSION_TAX_INVOICE_TYPE.getCode());
    invoiceRecord.setInvoiceType(Integer.parseInt(invoiceTypeArray[0]));
    invoiceRecord.setInvoiceAmount(taxInvoiceAmount);
    qbInvoiceRecordService.insert(invoiceRecord);

    //服务费票
    invoiceRecord.setInvoiceSerialNo(invoiceSerialNo + "_" + 2);
    invoiceRecord.setInvoiceMethod(InvoiceMethodType.SERVICE_FEE_INVOICE_TYPE.getCode());
    invoiceRecord.setInvoiceType(Integer.parseInt(invoiceTypeArray[1]));
    String totalInvoiceAmount = invoiceItem.getTotalInvoiceAmount();
    invoiceRecord.setInvoiceAmount(ArithmeticUtil.subStr2(totalInvoiceAmount,taxInvoiceAmount));
    qbInvoiceRecordService.insert(invoiceRecord);
    return resultMap;
  }

  @PostMapping("/batchList")
  public Map<String, Object> commissionInvoicedList(CommissionInvoice commissionInvoice){
    Map<String,Object> resultMap = new HashMap<>();
    List<Map<String,Object>> commissionInvoiceList = new ArrayList<>();
    resultMap.put(RespCode.RESP_STAT, RespCode.success);
    resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
    resultMap.put("invoiceList",commissionInvoiceList);

    String month = commissionInvoice.getMonth();
    String originalId = commissionInvoice.getOriginalId();
    String companyId = commissionInvoice.getCompanyId();
    if (StringUtil.isEmpty(month) || StringUtil.isEmpty(originalId) || StringUtil.isEmpty(companyId)){
      resultMap.put(RespCode.RESP_STAT, RespCode.error101);
      resultMap.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
      return resultMap;
    }
    Map<String,Object> params = new HashMap<>();
    params.put("originalId",originalId);
    params.put("companyId",companyId);
    params.put("startTime", DateUtils.monthToStartTime(month + "-01"));
    params.put("endTime", DateUtils.monthToEndTime(month + "-01"));
    //获取当月该商户该服务公司下的开票批次号
    List<String> invoiceSerialNoList = userCommissionService.getCommissionInvoiceSerialNos(params);
    if (invoiceSerialNoList == null || invoiceSerialNoList.isEmpty()){
      return resultMap;
    }
    Map<String,Object> invoiceRecordParam = new HashMap<>();
    //遍历批次号，获得已开票的下发明细列表
    for (String invoiceSerialNo : invoiceSerialNoList) {
      params.put("invoiceSerialNo2",null);
      params.put("invoiceSerialNo",invoiceSerialNo);
      List<UserCommission> userCommissionList = userCommissionService.getUserCommissionInvoiceRecord(params);
      String commissionAmount = "0";
      String individualTax = "0";
      String individualBackTax = "0";
      String sumFee = "0";
      Integer invoiceStatus = null;
      for (int i = 0; i < userCommissionList.size(); i++) {
        UserCommission userCommission = userCommissionList.get(i);
        if (i == 0){
          invoiceStatus = userCommission.getInvoiceStatus2();
        }
        commissionAmount = ArithmeticUtil.addStr(commissionAmount,userCommission.getAmount(),2);
        individualTax = ArithmeticUtil.addStr(individualTax,userCommission.getIndividualTax(),2);
        sumFee = ArithmeticUtil.addStr(sumFee,userCommission.getSumFee(),2);
      }
      //获得补个税的下发列表
      params.put("invoiceSerialNo",null);
      params.put("invoiceSerialNo2",invoiceSerialNo);
      List<UserCommission> individualBackUserCommissionList = userCommissionService.getUserCommissionInvoiceRecord(params);
      if (individualBackUserCommissionList != null && individualBackUserCommissionList.size() > 0){
        for (UserCommission userCommission : individualBackUserCommissionList) {
          individualBackTax = ArithmeticUtil.addStr(individualBackTax,userCommission.getIndividualBackTax(),2);
        }
      }
      //获取开票记录
      invoiceRecordParam.put("orderNo",invoiceSerialNo);
      List<QbInvoiceRecord> invoiceRecords = qbInvoiceRecordService.getCommissionInvoiceList(invoiceRecordParam);
      Map<String,Object> result = new HashMap<>();
      result.put("month",month);
      result.put("totalInvoicedAmount",ArithmeticUtil.addStr(commissionAmount,sumFee,2));
      String totalTaxAmount = ArithmeticUtil.addStr(individualBackTax,individualTax,2);
      result.put("taxInvoicedAmount",ArithmeticUtil.addStr(commissionAmount,totalTaxAmount,2));
      result.put("serviceChargeInvoicedAmount",ArithmeticUtil.subStr2(sumFee,totalTaxAmount));
      result.put("invoiceStatus",invoiceStatus);
      if (invoiceRecords != null && invoiceRecords.size() > 0){
        QbInvoiceRecord qbInvoiceRecord = invoiceRecords.get(0);
        result.put("createTime",qbInvoiceRecord.getCreateTime());
        result.put("operator",qbInvoiceRecord.getAddUser());
      }
      commissionInvoiceList.add(result);
    }
    resultMap.put("invoiceList",commissionInvoiceList);
    return resultMap;
  }

  @PostMapping("/individualTaxList")
  public Map<String, Object> individualTaxInvoicedList(CommissionInvoice commissionInvoice){
    Map<String,Object> resultMap = new HashMap<>();
    resultMap.put(RespCode.RESP_STAT, RespCode.success);
    resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
    String month = commissionInvoice.getMonth();
    String originalId = commissionInvoice.getOriginalId();
    String companyId = commissionInvoice.getCompanyId();
    if (StringUtil.isEmpty(month) || StringUtil.isEmpty(originalId) || StringUtil.isEmpty(companyId)){
      resultMap.put(RespCode.RESP_STAT, RespCode.PARAMS_ERROR);
      resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PARAMS_ERROR));
      return resultMap;
    }
    //获取服务公司个税档位列表
    Map<String,List<PersonalIncomeTaxRate>> personalTaxRatesMap = new HashMap<>();
    List<PersonalIncomeTaxRate> taxRateList = personalIncomeTaxRateService.getCompanyTaxRateList(companyId);
    personalTaxRatesMap.put(companyId,taxRateList);

    Map<String,Object> params = new HashMap<>();
    params.put("originalId",originalId);
    params.put("companyId",companyId);
    params.put("invoiceStatus2","1,2");
    params.put("startTime", DateUtils.monthToStartTime(month + "-01"));
    params.put("endTime", DateUtils.monthToEndTime(month + "-01"));
    PageHelper.startPage(commissionInvoice.getPageNo(),commissionInvoice.getPageSize(),true);
    List<UserCommission> userCommissionInvoiceRecord = userCommissionService.groupUserCommissionInvoiceRecord(params);
    PageInfo pageInfo = new PageInfo(userCommissionInvoiceRecord);
    resultMap.put("totalRecord", pageInfo.getTotal());
    resultMap.put("totalPage", pageInfo.getPages());
    List<Map<String,Object>> userInvoiceList = new ArrayList<>();
    for (UserCommission userCommission : userCommissionInvoiceRecord) {
      Map<String,Object> userInvoice = new HashMap<>();
      userInvoice.put("month",month);
      userInvoice.put("userName",userCommission.getUserName());
      userInvoice.put("certId",userCommission.getCertId());
      userInvoice.put("commissionAmount",userCommission.getAmount());
      String individualTax = ArithmeticUtil
          .addStr(userCommission.getIndividualTax(), userCommission.getIndividualBackTax(),2);
      userInvoice.put("individualTax",individualTax);
      userInvoice.put("individualBackTax",userCommission.getIndividualBackTax());
      //个税税率
      String taxRate = userCommissionService.getTaxRate(userCommission.getAmount(),companyId,personalTaxRatesMap);
      userInvoice.put("taxRate",ArithmeticUtil.mulStr(taxRate,"100",2, BigDecimal.ROUND_HALF_UP));
      userInvoiceList.add(userInvoice);
    }
    resultMap.put("userInvoiceList",userInvoiceList);
    return resultMap;
  }
}





















