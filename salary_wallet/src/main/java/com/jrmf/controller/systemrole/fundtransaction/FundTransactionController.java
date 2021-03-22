package com.jrmf.controller.systemrole.fundtransaction;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CertType;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.Company;
import com.jrmf.domain.OrganizationNode;
import com.jrmf.domain.UserCommission;
import com.jrmf.domain.vo.FundSummaryVO;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.persistence.CustomGroupDao;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.FundTransactionService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jrmf.utils.RespCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/fund/transaction")
public class FundTransactionController extends BaseController {

  @Autowired
  private FundTransactionService fundTransactionService;

  @Autowired
  private OrganizationTreeService organizationTreeService;

  @Autowired
  private CustomProxyDao customProxyDao;

  @Autowired
  private ChannelRelatedService channelRelatedService ;

  @Autowired
  private CustomGroupDao customGroupDao;
  @Autowired
  private ChannelCustomService channelCustomService;

  /**
   * 商户资金交易报表
   */
  @RequestMapping(value = "/list")
  public Map<String, Object> listMerchantFundTransaction(HttpServletRequest request,
                                                         @RequestParam(required = false) String startTime,
                                                         @RequestParam(required = false) String endTime,
                                                         @RequestParam(required = false) Integer companyId,
                                                         @RequestParam(required = false) String merchantName,
                                                         @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                                         @RequestParam(required = false, defaultValue = "1") Integer pageNo) {
    Map<String, Object> param = new HashMap<>(6);
    ChannelCustom customLogin = getCustomLogin(request);
    if (customLogin.getCustomType() == CustomType.PROXY.getCode() || isPlatformAccount(customLogin)) {
      return returnFail(RespCode.error101, "权限不足");
    }
    param.put("companyId", companyId);
    getMerchantKeyParam(param, customLogin);
    param.put("merchantName", merchantName);
    param.put("startTime", startTime);
    param.put("endTime", endTime);
    PageHelper.startPage(pageNo,pageSize);
    List<UserCommission> commissionDetailResult = fundTransactionService.listCommissionByCustomKeys(param);
    PageInfo<UserCommission> pageInfo = new PageInfo<>(commissionDetailResult);

    Map<String, Object> result = new HashMap<>(6);
    result.put("total", pageInfo.getTotal());
    result.put("list", pageInfo.getList());

    Map<String, Object> rechargeMap = fundTransactionService.getSumRecharge(param);
    Map<String, Object> transactionMap = fundTransactionService.getSumTransaction(param);
    result.put("recharge", rechargeMap);
    result.put("transaction", transactionMap);
    return returnSuccess(result);
  }


  /**
   * 商户资金交易报表导出
   */
  @RequestMapping(value = "/list/export")
  public void listMerchantFundTransactionExport(HttpServletResponse response,
                                                HttpServletRequest request,
                                                @RequestParam(required = false) String startTime,
                                                @RequestParam(required = false) String endTime,
                                                @RequestParam(required = false) Integer companyId,
                                                @RequestParam(required = false) String merchantName) {

    Map<String, Object> param = new HashMap<>(6);
    ChannelCustom customLogin = getCustomLogin(request);
    if (customLogin.getCustomType() == CustomType.PROXY.getCode() || isPlatformAccount(customLogin)) {
      log.info("资金交易报表-商户端：导出权限不足");
      return;
    }
    param.put("companyId", companyId);
    getMerchantKeyParam(param, customLogin);
    param.put("merchantName", merchantName);
    param.put("startTime", startTime);
    param.put("endTime", endTime);
    List<UserCommission> commissionDetailResult = fundTransactionService
            .listCommissionByCustomKeys(param);

    String[] colunmName = new String[]{"商户名称", "服务公司", "交易类型", "交易时间", "交易金额","到账金额", "服务费",
            "收款人姓名", "证件类型", "证件号", "收款账号", "订单备注", "项目名称", "批次名称", "批次说明"};
    String filename = "商户资金交易统计表";
    List<Map<String, Object>> data = new ArrayList<>();
    for (UserCommission commission : commissionDetailResult) {
      Map<String, Object> dataMap = new HashMap<>(20);
      dataMap.put("1", commission.getMerchantName());
      dataMap.put("2", commission.getCompanyName());
      dataMap.put("3", "下发");
      dataMap.put("4", commission.getCreatetime());
      dataMap.put("5", commission.getSourceAmount());
      dataMap.put("6", commission.getAmount());
      dataMap.put("7", commission.getSumFee());
      dataMap.put("8", commission.getUserName());
      dataMap.put("9", CertType.codeOf(commission.getDocumentType()).getDesc());
      dataMap.put("10", commission.getCertId());
      dataMap.put("11", commission.getAccount());
      dataMap.put("12", commission.getRemark());
      dataMap.put("13", commission.getContentName());
      dataMap.put("14", commission.getBatchName());
      dataMap.put("15", commission.getBatchDesc());
      data.add(sortMapByKey(dataMap));
    }
    ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
  }


  /**
   * 资金交易报表-服务机构端
   */
  @RequestMapping(value = "/platform/list")
  public Map<String, Object> listPlatformFundTransaction(HttpServletRequest request,
                                                         @RequestParam(required = false) String startTime,
                                                         @RequestParam(required = false) String endTime,
                                                         @RequestParam(required = false) Integer companyId,
                                                         @RequestParam(required = false) String merchantName,
                                                         @RequestParam(required = false) String agentName,
                                                         @RequestParam(required = false) String platform,
                                                         @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                                         @RequestParam(required = false, defaultValue = "1") Integer pageNo) {

    Map<String, Object> param = new HashMap<>(6);
    ChannelCustom customLogin = getCustomLogin(request);

    if (!CommonString.ROOT.equals(customLogin.getCustomkey())
        && customLogin.getCustomType() != CustomType.PROXY.getCode() && !isPlatformAccount(
        customLogin)) {
      return returnFail(RespCode.error101, "权限不足");
    }

    getCustomkeys(customLogin, param);
    if (param.containsKey("platformId")) {
      Integer platformId = (Integer) param.get("platformId");
      List<String> customKeys = customService.getCustomKeyByBusinessPlatformId(platformId);
      param.put("originalIds", Joiner.on(",").join(customKeys));
      // 如果用户未在页面选择服务公司查询，则查询平台下所有的关联的服务公司
      if (companyId == null) {
        List<String> companyList = customService.getCompanyUserIdByBusinessPlatformId(platformId);
        if (companyList == null || companyList.isEmpty()) {
          return returnFail(RespCode.error101, "平台尚未配置服务公司");
        }
        param.put("companyIds", Joiner.on(",").join(companyList));
      }
    }
    param.put("merchantName", merchantName);
    param.put("agentName", agentName);
    param.put("platform", platform);
    param.put("companyId", companyId);
    param.put("startTime", startTime);
    param.put("endTime", endTime);
    PageHelper.startPage(pageNo,pageSize);
    List<UserCommission> commissionDetailResult = fundTransactionService.listCommissionByCustomKeys(param);
    PageInfo<UserCommission> pageInfo = new PageInfo<>(commissionDetailResult);
    Map<String, Object> result = new HashMap<>(6);
    result.put("total", pageInfo.getTotal());
    result.put("list", pageInfo.getList());

    Map<String, Object> rechargeMap = fundTransactionService.getSumRecharge(param);
    Map<String, Object> transactionMap = fundTransactionService.getSumTransaction(param);
    result.put("recharge", rechargeMap);
    result.put("transaction", transactionMap);
    return returnSuccess(result);
  }


  /**
   * 资金交易报表导出
   */
  @RequestMapping(value = "/platform/list/export")
  public void listPlatformFundTransactionExport(HttpServletResponse response,
                                                HttpServletRequest request,
                                                @RequestParam(required = false) String startTime,
                                                @RequestParam(required = false) String endTime,
                                                @RequestParam(required = false) Integer companyId,
                                                @RequestParam(required = false) String merchantName,
                                                @RequestParam(required = false) String agentName,
                                                @RequestParam(required = false) String platform) {

    Map<String, Object> param = new HashMap<>(6);
    ChannelCustom customLogin = getCustomLogin(request);
    if (!CommonString.ROOT.equals(customLogin.getCustomkey())
        && customLogin.getCustomType() != CustomType.PROXY.getCode() && !isPlatformAccount(
        customLogin)) {
      log.info("资金交易报表-服务机构端：导出权限不足");
      return;
    }
    getCustomkeys(customLogin, param);
    if (param.containsKey("platformId")) {
      Integer platformId = (Integer) param.get("platformId");
      List<String> customKeys = customService.getCustomKeyByBusinessPlatformId(platformId);
      param.put("originalIds", Joiner.on(",").join(customKeys));
      // 如果用户未在页面选择服务公司查询，则查询平台下所有的关联的服务公司
      if (companyId == null) {
        List<String> companyList = customService.getCompanyUserIdByBusinessPlatformId(platformId);
        if (companyList == null || companyList.isEmpty()) {
          log.info("平台尚未配置服务公司");
          return;
        }
        param.put("companyIds", Joiner.on(",").join(companyList));
      }
    }
    param.put("merchantName", merchantName);
    param.put("agentName", agentName);
    param.put("platform", platform);
    param.put("companyId", companyId);
    param.put("startTime", startTime);
    param.put("endTime", endTime);
    List<UserCommission> commissionDetailResult = fundTransactionService.listCommissionByCustomKeys(param);

    String[] colunmName = new String[]{"商户名称", "服务公司", "代理商", "交易类型", "交易时间",
            "交易金额","到账金额", "服务费", "收款人姓名", "证件类型", "证件号", "收款账号",
            "订单备注", "项目名称", "批次名称", "批次说明"};
    String filename = "资金交易统计表";
    List<Map<String, Object>> data = new ArrayList<>();
    for (UserCommission commission : commissionDetailResult) {
      Map<String, Object> dataMap = new HashMap<>(20);
      dataMap.put("1", commission.getMerchantName());
      dataMap.put("2", commission.getCompanyName());
      dataMap.put("3", commission.getAgentName());
      dataMap.put("4", "下发");
      dataMap.put("5", commission.getCreatetime());
      dataMap.put("6", commission.getSourceAmount());
      dataMap.put("7", commission.getAmount());
      dataMap.put("8", commission.getSumFee());
      dataMap.put("9", commission.getUserName());
      dataMap.put("10", CertType.codeOf(commission.getDocumentType()).getDesc());
      dataMap.put("11", commission.getCertId());
      dataMap.put("12", commission.getAccount());
      dataMap.put("13", commission.getRemark());
      dataMap.put("14", commission.getContentName());
      dataMap.put("15", commission.getBatchName());
      dataMap.put("16", commission.getBatchDesc());
      data.add(sortMapByKey(dataMap));
    }
    ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
  }

  private ChannelCustom getCustomLogin(HttpServletRequest request) {
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if (customLogin.getCustomType() == 4 && customLogin.getMasterCustom() != null) {
      customLogin = customService.getCustomByCustomkey(customLogin.getMasterCustom());
    }
    return customLogin;
  }

  private void getMerchantKeyParam(Map<String, Object> param, ChannelCustom customLogin) {
    if (CommonString.ROOT.equals(customLogin.getCustomkey())) {
      // 超管或运营人员
    } else if (customLogin.getCustomType() == CustomType.CUSTOM.getCode()) {
      // 普通商户
      param.put("originalIds", customLogin.getCustomkey());
    } else if (customLogin.getCustomType() == CustomType.GROUP.getCode()) {
      // 集团商户
      int nodeId = organizationTreeService.queryNodeIdByCustomKey(customLogin.getCustomkey());
      List<String> customKeys = organizationTreeService
          .queryNodeCusotmKey(customLogin.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN,
              nodeId);
      param.put("originalIds", String.join(",", customKeys));
    } else if (customLogin.getCustomType() == CustomType.COMPANY.getCode()) {
      // 服务公司
      List<String> relatedList = channelRelatedService.queryCustomKeysByCompanyId(customLogin.getCustomkey());
      param.put("originalIds", String.join(",", relatedList));
      param.put("companyId", customLogin.getCustomkey());
    }
  }

  private void getCustomkeys(ChannelCustom customLogin, Map<String, Object> param) {
    if (CommonString.ROOT.equals(customLogin.getCustomkey())) {
      // 超管或运营人员
    } else if (customLogin.getCustomType() == CustomType.PROXY.getCode()) {
      //判断是不是关联性代理商
      if (customLogin.getProxyType() == 1) {
        OrganizationNode node = customProxyDao
                .getProxyChildenNodeByCustomKey(customLogin.getCustomkey(), null);
        List<String> stringList = organizationTreeService
                .queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(),
                        QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
        if (stringList != null && !stringList.isEmpty()) {
          List<String> customStringList = new ArrayList<String>();
          for (String customKey : stringList) {
            OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey, null);
            List<String> customKeyList = organizationTreeService
                    .queryNodeCusotmKey(CustomType.PROXY.getCode(),
                            QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
            customStringList.addAll(customKeyList);
          }
          String customKeys = Joiner.on(",").join(customStringList);
          param.put("originalIds", String.join(",", customKeys));
        }
      } else {
        OrganizationNode node = customProxyDao.getNodeByCustomKey(customLogin.getCustomkey(), null);
        List<String> stringList = organizationTreeService
                .queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                        node.getId());
        if (stringList != null && !stringList.isEmpty()) {
          param.put("originalIds", Joiner.on(",").join(stringList));
        }
      }
    } else if (isPlatformAccount(customLogin)) {
      //判断是否是平台商户
      Integer platformId = checkCustom(customLogin);
      param.put("platformId", platformId);
    }
  }

  /**
   * 集团商户资金交易报表汇总信息
   */
  @RequestMapping(value = "/summary")
  public Map<String,Object> summary(
                            HttpServletRequest request,
                            @RequestParam(required = true) String startDate,
                            @RequestParam(required = true) String endDate,
                            @RequestParam(required = false) Integer companyId,
                            @RequestParam(required = false) String merchantName,
                            @RequestParam(required = false) Integer nodeId) {
      if(!checkPermission(request)){
        return returnFail(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT,RespCode.PERMISSION_ERROR);
      }

      Map<String,Object> params=extractFundSummaryParams(request,nodeId);

     //容错：防止未查出customList的情况下，查询出了所有数据
     if(!params.containsKey("customList")){
       return returnFail(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT,RespCode.PERMISSION_ERROR);
     }

      FundSummaryVO summaryVO= fundTransactionService.getFundSummaryInfo(params);
      if(summaryVO==null){
        summaryVO=new FundSummaryVO();
      }
      if(summaryVO.getRechargeAmount()==null){
        summaryVO.setRechargeAmount("0");
      }
      if(summaryVO.getRechargeTimes()==null){
        summaryVO.setRechargeTimes("0");
      }
      if(summaryVO.getCommissionAmount()==null){
        summaryVO.setCommissionAmount("0");
      }
      if(summaryVO.getCommissionTimes()==null){
        summaryVO.setCommissionTimes("0");
      }

      return returnSuccess(summaryVO);
  }

  @GetMapping("list_summary")
  public Map<String,Object> listFundSummary(HttpServletRequest request,
                                            @RequestParam(required = true) String startDate,
                                            @RequestParam(required = true) String endDate,
                                            @RequestParam(required = false) Integer companyId,
                                            @RequestParam(required = false) String merchantName,
                                            @RequestParam(required = false) Integer nodeId,
                                            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false, defaultValue = "1") Integer pageNo){

    if(!checkPermission(request)){
      return returnFail(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT,RespCode.PERMISSION_ERROR);
    }

    Map<String,Object> params=extractFundSummaryParams(request,nodeId);

    //容错：防止未查出customList的情况下，查询出了所有数据
    if(!params.containsKey("customList")){
      return returnFail(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT,RespCode.PERMISSION_ERROR);
    }

    PageHelper.startPage(pageNo,pageSize);
    List<FundSummaryVO> fundSummaryList = fundTransactionService.listFundSummary(params);
    PageInfo<FundSummaryVO> pageInfo = new PageInfo<>(fundSummaryList);

    Map<String, Object> result = new HashMap<>(6);
    result.put("total", pageInfo.getTotal());
    result.put("list", pageInfo.getList());

    return returnSuccess(result);
  }

  private boolean checkPermission(HttpServletRequest request){
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if(customLogin==null){
      return false;
    }

    if(customLogin.getCustomType()==CustomType.GROUP.getCode() || customLogin.getMasterCustomType()==CustomType.GROUP.getCode()){
      return true;
    }
    return false;
  }

  private Map<String, Object> extractFundSummaryParams(HttpServletRequest request,Integer nodeId){
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat mysqlSdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Map<String, Object> param = new HashMap<>();
    //所查数据要小于这个时间，所以天数往后加一

    try {
      param.put("startTime",mysqlSdf.format(sdf.parse(request.getParameter("startDate"))));
      param.put("endTime",mysqlSdf.format(DateUtils.getAfterDay(sdf.parse(request.getParameter("endDate")),1)));
    } catch (ParseException e) {
      log.error(e.getMessage(),e);
    }

    param.put("companyId",request.getParameter("companyId"));
    if(request.getParameter("merchantName")!=null){
      param.put("customName","%"+request.getParameter("merchantName")+"%");
    }

    OrganizationNode organizationNode= customGroupDao.getNodeById(nodeId,null);
    if(organizationNode==null){
      return param;
    }

    ChannelCustom channelCustom= channelCustomService.getCustomByCustomkey(organizationNode.getCustomKey());
    if(channelCustom==null){
      return param;
    }

    List<String> customList= organizationTreeService.queryNodeCusotmKey(channelCustom.getCustomType(),QueryType.QUERY_CURRENT_AND_CHILDREN,nodeId);
    if(customList!=null && !customList.isEmpty()){
      param.put("customList",Joiner.on(",").join(customList));
    }

    return param;
  }

  /**
   * 集团商户资金交易报表导出
   */
  @RequestMapping(value = "/export_summary")
  public void exportSummary(HttpServletResponse response,
                            HttpServletRequest request,
                            @RequestParam(required = true) String startDate,
                            @RequestParam(required = true) String endDate,
                            @RequestParam(required = false) Integer companyId,
                            @RequestParam(required = false) String merchantName,
                            @RequestParam(required = false) Integer nodeId) {

    Map<String,Object> params=extractFundSummaryParams(request,nodeId);

    //容错：防止未查出customList的情况下，查询出了所有数据
    if(!params.containsKey("customList")){
      return;
    }

    List<FundSummaryVO> summaryVOList = fundTransactionService.listFundSummary(params);

    String[] colunmName = new String[]{"商户名称", "服务公司", "充值总金额","充值总次数","下发总金额","下发总笔数","总服务费"};
    String filename = "资金交易报表";
    List<Map<String, Object>> data = new ArrayList<>();
    for (FundSummaryVO summaryVO : summaryVOList) {
      Map<String, Object> dataMap = new LinkedHashMap<>();
      dataMap.put("1", summaryVO.getCustomName());
      dataMap.put("2", summaryVO.getCompanyName());
      dataMap.put("3", summaryVO.getRechargeAmount());
      dataMap.put("4", summaryVO.getCommissionTimes());
      dataMap.put("5", summaryVO.getCommissionAmount());
      dataMap.put("6", summaryVO.getCommissionTimes());
      dataMap.put("7", summaryVO.getFee());
      data.add(dataMap);
    }
    ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
  }

}
