package com.jrmf.controller.systemrole.merchant.invoice;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.domain.UserCommission;
import com.jrmf.service.PersonalIncomeTaxRateService;
import com.jrmf.controller.constant.InvoiceOrderStatus2;
import com.jrmf.persistence.UserCommission2Dao;
import com.jrmf.service.UserCommissionService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Joiner;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.InvoiceApprovalStatus;
import com.jrmf.controller.constant.InvoiceMethodType;
import com.jrmf.controller.constant.InvoiceRealStatus;
import com.jrmf.controller.constant.InvoiceType;
import com.jrmf.controller.constant.LoginRole;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.controller.constant.TaxpayerType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.OrganizationNode;
import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoicePic;
import com.jrmf.domain.QbInvoiceRecord;
import com.jrmf.domain.QbInvoiceReserve;
import com.jrmf.domain.QbInvoiceVoucher;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.service.QbInvoicePicService;
import com.jrmf.service.QbInvoiceRecordService;
import com.jrmf.service.QbInvoiceReserveService;
import com.jrmf.service.QbInvoiceVoucherService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;

@Controller
@RequestMapping("/invoiceReview")
public class MerchantInvoiceReviewController extends BaseController {

  @Autowired
  private OrganizationTreeService organizationTreeService;
  @Autowired
  private QbInvoiceRecordService qbInvoiceRecordService;
  @Autowired
  private QbInvoicePicService qbInvoicePicService;
  @Autowired
  private ChannelHistoryService channelHistoryService;
  @Autowired
  private BaseInfo baseInfo;
  @Autowired
  private BestSignConfig bestSignConfig;
  @Autowired
  private ChannelCustomService customService;
  @Autowired
  private CustomProxyDao customProxyDao;
  //发票库存service
  @Autowired
  private QbInvoiceReserveService qbInvoiceReserveService;
  //发票凭证服务
  @Autowired
  private QbInvoiceVoucherService qbInvoiceVoucherService;
  //商户信息service
  @Autowired
  private ChannelCustomService channelCustomService;
  @Autowired
  PersonalIncomeTaxRateService personalIncomeTaxRateService;
  @Autowired
  UserCommissionService userCommissionService;
  @Autowired
  UserCommission2Dao userCommission2Dao;

  private static Logger logger = LoggerFactory.getLogger(MerchantInvoiceReviewController.class);

  /**
   * 开票记录列表
   *
   * @param request
   * @return
   */
  @RequestMapping("/list")
  @ResponseBody
  public Map<String, Object> list(HttpServletRequest request) {
    int respstat = RespCode.success;
    HashMap<String, Object> result = new HashMap<>();
    Page page = new Page(request);
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    String invoiceCode = page.getParams().get("invoiceCode");
    String invoiceNo = page.getParams().get("invoiceNo");
    if (!StringUtil.isEmpty(invoiceCode) || !StringUtil.isEmpty(invoiceNo)) {
      page.getParams().put("joinVou", "1");
    }
    ;
    if (customLogin.getCustomType() == 4 && customLogin.getMasterCustom() != null) {
      ChannelCustom masterChannelCustom = customService
          .getCustomByCustomkey(customLogin.getMasterCustom());
      if ((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (
          CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT
              .equals(masterChannelCustom.getMasterCustom())
              && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))) {
        //超管
      } else if (masterChannelCustom.getCustomType() == CustomType.CUSTOM.getCode()) {
        //普通商户
        page.getParams().put("loginCustomer", masterChannelCustom.getCustomkey());
      } else if (masterChannelCustom.getCustomType() == CustomType.GROUP.getCode()) {
        //集团商户
        int nodeId = organizationTreeService
            .queryNodeIdByCustomKey(masterChannelCustom.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(masterChannelCustom.getCustomType(),
                QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
        page.getParams().put("loginCustomer", String.join(",", customKeys));
      } else if (masterChannelCustom.getCustomType() == CustomType.COMPANY.getCode()) {
        //服务公司
        page.getParams().put("companyId", masterChannelCustom.getCustomkey());
        page.getParams().put("originalIds", masterChannelCustom.getCustomkey());
      } else if (masterChannelCustom.getCustomType() == CustomType.PROXY.getCode()) {
        //代理商
        //判断是不是关联性代理商
        if (masterChannelCustom.getProxyType() == 1) {
          OrganizationNode node = customProxyDao
              .getProxyChildenNodeByCustomKey(masterChannelCustom.getCustomkey(),null);
          List<String> stringList = organizationTreeService
              .queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(),
                  QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
          if (stringList != null && stringList.size() > 0) {
            List<String> customStringList = new ArrayList<String>();
            for (String customKey : stringList) {
              OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey,null);
              List<String> customKeyList = organizationTreeService
                  .queryNodeCusotmKey(CustomType.PROXY.getCode(),
                      QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
              customStringList.addAll(customKeyList);
            }
            String customKeys = Joiner.on(",").join(customStringList);
            page.getParams().put("loginCustomer", String.join(",", customKeys));
          }
        } else {
          OrganizationNode node = customProxyDao
              .getNodeByCustomKey(masterChannelCustom.getCustomkey(),null);
          List<String> stringList = organizationTreeService
              .queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                  node.getId());
          String customKeys = Joiner.on(",").join(stringList);
          page.getParams().put("loginCustomer", String.join(",", customKeys));
        }
      }
    } else {
      if ((CommonString.ROOT.equals(customLogin.getCustomkey()) || (
          CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT
              .equals(customLogin.getMasterCustom())
              && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
        //超管
      } else if (customLogin.getCustomType() == CustomType.CUSTOM.getCode()) {
        //普通商户
        page.getParams().put("loginCustomer", customLogin.getCustomkey());
      } else if (customLogin.getCustomType() == CustomType.GROUP.getCode()) {
        //集团商户
        int nodeId = organizationTreeService.queryNodeIdByCustomKey(customLogin.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(customLogin.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                nodeId);
        page.getParams().put("loginCustomer", String.join(",", customKeys));
      } else if (customLogin.getCustomType() == CustomType.COMPANY.getCode()) {
        //服务公司
        page.getParams().put("companyId", customLogin.getCustomkey());
        page.getParams().put("originalIds", customLogin.getCustomkey());
      } else if (customLogin.getCustomType() == CustomType.PROXY.getCode()) {
        //代理商
        //判断是不是关联性代理商
        if (customLogin.getProxyType() == 1) {
          OrganizationNode node = customProxyDao
              .getProxyChildenNodeByCustomKey(customLogin.getCustomkey(),null);
          List<String> stringList = organizationTreeService
              .queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(),
                  QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
          if (stringList != null && stringList.size() > 0) {
            List<String> customStringList = new ArrayList<String>();
            for (String customKey : stringList) {
              OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey,null);
              List<String> customKeyList = organizationTreeService
                  .queryNodeCusotmKey(CustomType.PROXY.getCode(),
                      QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
              customStringList.addAll(customKeyList);
            }
            String customKeys = Joiner.on(",").join(customStringList);
            page.getParams().put("loginCustomer", String.join(",", customKeys));
          }
        } else {
          OrganizationNode node = customProxyDao.getNodeByCustomKey(customLogin.getCustomkey(),null);
          List<String> stringList = organizationTreeService
              .queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                  node.getId());
          String customKeys = Joiner.on(",").join(stringList);
          page.getParams().put("loginCustomer", String.join(",", customKeys));
        }
      }
    }
    PageHelper.startPage(page.getPageNo(),page.getPageSize());
    List<Map<String, Object>> relationList = qbInvoiceRecordService.queryRecordList(page);
    PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(relationList);
    result.put("total", pageInfo.getTotal());
    result.put("relationList", relationList);
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    return result;
  }

  /**
   * 导出excel
   */
  @RequestMapping("/export")
  public void export(HttpServletResponse response, HttpServletRequest request) {
    // 标题
    String[] headers = new String[]{"商户名称", "申请开票流水号", "公司名称(发票抬头)", "纳税人类型", "税务登记号", "开户银行名称",
        "开户账号", "地址", "电话", "开票类目", "开票金额", "开票类型", "收递人信息", "快递单号", "开票日期", "服务公司（开票方）", "服务类型",
        "发票对应充值流水", "申请时间", "开票状态", "开票方式",
        "核销状态", "核销金额", "驳回原因", "备注"};
    String filename = "商户开票记录";
    Page page = new Page(request);
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    String invoiceCode = page.getParams().get("invoiceCode");
    String invoiceNo = page.getParams().get("invoiceNo");
    if (!StringUtil.isEmpty(invoiceCode) || !StringUtil.isEmpty(invoiceNo)) {
      page.getParams().put("joinVou", "1");
    }
    ;
    if (customLogin.getCustomType() == 4 && customLogin.getMasterCustom() != null) {
      ChannelCustom masterChannelCustom = customService
          .getCustomByCustomkey(customLogin.getMasterCustom());
      if ((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (
          CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT
              .equals(masterChannelCustom.getMasterCustom())
              && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))) {
        //超管
      } else if (masterChannelCustom.getCustomType() == CustomType.CUSTOM.getCode()) {
        //普通商户
        page.getParams().put("loginCustomer", masterChannelCustom.getCustomkey());
      } else if (masterChannelCustom.getCustomType() == CustomType.GROUP.getCode()) {
        //集团商户
        int nodeId = organizationTreeService
            .queryNodeIdByCustomKey(masterChannelCustom.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(masterChannelCustom.getCustomType(),
                QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
        page.getParams().put("loginCustomer", String.join(",", customKeys));
      } else if (masterChannelCustom.getCustomType() == CustomType.COMPANY.getCode()) {
        //服务公司
        page.getParams().put("companyId", masterChannelCustom.getCustomkey());
        page.getParams().put("originalIds", masterChannelCustom.getCustomkey());
      } else if (masterChannelCustom.getCustomType() == CustomType.PROXY.getCode()) {
        //代理商
        //判断是不是关联性代理商
        if (masterChannelCustom.getProxyType() == 1) {
          OrganizationNode node = customProxyDao
              .getProxyChildenNodeByCustomKey(masterChannelCustom.getCustomkey(),null);
          List<String> stringList = organizationTreeService
              .queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(),
                  QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
          if (stringList != null && stringList.size() > 0) {
            List<String> customStringList = new ArrayList<String>();
            for (String customKey : stringList) {
              OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey,null);
              List<String> customKeyList = organizationTreeService
                  .queryNodeCusotmKey(CustomType.PROXY.getCode(),
                      QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
              customStringList.addAll(customKeyList);
            }
            String customKeys = Joiner.on(",").join(customStringList);
            page.getParams().put("loginCustomer", String.join(",", customKeys));
          }
        } else {
          OrganizationNode node = customProxyDao
              .getNodeByCustomKey(masterChannelCustom.getCustomkey(),null);
          List<String> stringList = organizationTreeService
              .queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                  node.getId());
          String customKeys = Joiner.on(",").join(stringList);
          page.getParams().put("loginCustomer", String.join(",", customKeys));
        }
      }
    } else {
      if ((CommonString.ROOT.equals(customLogin.getCustomkey()) || (
          CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT
              .equals(customLogin.getMasterCustom())
              && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
        //超管
      } else if (customLogin.getCustomType() == CustomType.CUSTOM.getCode()) {
        //普通商户
        page.getParams().put("loginCustomer", customLogin.getCustomkey());
      } else if (customLogin.getCustomType() == CustomType.GROUP.getCode()) {
        //集团商户
        int nodeId = organizationTreeService.queryNodeIdByCustomKey(customLogin.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(customLogin.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                nodeId);
        page.getParams().put("loginCustomer", String.join(",", customKeys));
      } else if (customLogin.getCustomType() == CustomType.COMPANY.getCode()) {
        //服务公司
        page.getParams().put("companyId", customLogin.getCustomkey());
        page.getParams().put("originalIds", customLogin.getCustomkey());
      } else if (customLogin.getCustomType() == CustomType.PROXY.getCode()) {
        //代理商
        //判断是不是关联性代理商
        if (customLogin.getProxyType() == 1) {
          OrganizationNode node = customProxyDao
              .getProxyChildenNodeByCustomKey(customLogin.getCustomkey(),null);
          List<String> stringList = organizationTreeService
              .queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(),
                  QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
          if (stringList != null && stringList.size() > 0) {
            List<String> customStringList = new ArrayList<String>();
            for (String customKey : stringList) {
              OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey,null);
              List<String> customKeyList = organizationTreeService
                  .queryNodeCusotmKey(CustomType.PROXY.getCode(),
                      QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
              customStringList.addAll(customKeyList);
            }
            String customKeys = Joiner.on(",").join(customStringList);
            page.getParams().put("loginCustomer", String.join(",", customKeys));
          }
        } else {
          OrganizationNode node = customProxyDao.getNodeByCustomKey(customLogin.getCustomkey(),null);
          List<String> stringList = organizationTreeService
              .queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                  node.getId());
          String customKeys = Joiner.on(",").join(stringList);
          page.getParams().put("loginCustomer", String.join(",", customKeys));
        }
      }
    }

    List<Map<String, Object>> relationList = qbInvoiceRecordService.queryRecordListNoPage(page);
    List<UserCommission> listDetails = checkInvoice(relationList);
    List<Map<String, Object>> data = new ArrayList<>();
    for (Map<String, Object> invioceBase : relationList) {
      Map<String, Object> dataMap = new HashMap<>(20);
      dataMap.put("1", invioceBase.get("merchantName"));
      dataMap.put("2", invioceBase.get("invoiceSerialNo"));
      dataMap.put("3", invioceBase.get("companyName"));
      dataMap.put("4",
          TaxpayerType.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("taxpayerType"))))
              .getDesc());
      dataMap.put("5", invioceBase.get("taxRegistrationNumber"));
      dataMap.put("6", invioceBase.get("accountBankName"));
      dataMap.put("7", invioceBase.get("accountNo"));
      dataMap.put("8", invioceBase.get("address"));
      dataMap.put("9", invioceBase.get("phone"));
      dataMap.put("10", invioceBase.get("billingClassName"));
      dataMap.put("11", invioceBase.get("invoiceAmount"));
      dataMap.put("12",
          InvoiceType.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("invoiceType"))))
              .getDesc());
      dataMap.put("13", String.valueOf(invioceBase.get("receiveUser")).replaceAll(",", "|"));
      dataMap.put("14", invioceBase.get("expressNo"));
      dataMap.put("15", invioceBase.get("invoiceTime"));
      dataMap.put("16", invioceBase.get("serviceName"));
      dataMap.put("17", invioceBase.get("serviceTypeName"));
      dataMap.put("18", String.valueOf(invioceBase.get("orderNo")).replaceAll(",", "|"));
      dataMap.put("19", invioceBase.get("createTime"));
      dataMap.put("20",
          InvoiceRealStatus.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("status"))))
              .getDesc());
      String invoiceMethod =
          StringUtil.isEmpty(String.valueOf(invioceBase.get("invoiceMethod"))) ? "1"
              : String.valueOf(invioceBase.get("invoiceMethod"));
      dataMap.put("21", InvoiceMethodType.codeOf(Integer.parseInt(invoiceMethod)).getDesc());
      String approval = StringUtil.isEmpty(String.valueOf(invioceBase.get("approval"))) ? "0"
          : String.valueOf(invioceBase.get("approval"));
      dataMap.put("22", InvoiceApprovalStatus.codeOf(Integer.parseInt(approval)).getDesc());
      String invoiceAmount =
          StringUtil.isEmpty(String.valueOf(invioceBase.get("invoiceAmount"))) ? "0"
              : String.valueOf(invioceBase.get("invoiceAmount"));
      dataMap.put("23", invoiceAmount);
      if (ObjectUtils.isEmpty(invioceBase.get("rejectionReason"))) {
        dataMap.put("24", "暂无原因");
      } else {
        dataMap.put("24", invioceBase.get("rejectionReason"));
      }
      if (CollectionUtils.isNotEmpty(listDetails)) {
        //放入发票类型
        dataMap.put("25", invioceBase.get("invoiceMethod"));
        dataMap.put("26", invioceBase.get("remark"));
      } else {
        dataMap.put("25", invioceBase.get("remark"));
      }
      data.add(sortMapByKey(dataMap));
    }

    if (CollectionUtils.isNotEmpty(listDetails)) {
      String[] newHeaders = Arrays.copyOf(headers, 31);

      newHeaders[25] = "开票明细日期";
      newHeaders[26] = "实发总金额";
      newHeaders[27] = "服务费率";
      newHeaders[28] = "个税税率";
      newHeaders[29] = "个税税额";
      newHeaders[30] = "补充个税税额";
      ExcelFileGenerator
          .ExcelExportAndMergedRegion(response, newHeaders, filename, data, listDetails);
    } else {
      ExcelFileGenerator.ExcelExport(response, headers, filename, data);
    }
  }

  /**
   * 审核发票信息
   *
   * @param invoiceRecord
   * @param file
   * @param request
   * @return
   */
  @RequestMapping("/reviewInvoice")
  @ResponseBody
  public Map<String, Object> reviewInvoice(QbInvoiceRecord invoiceRecord, MultipartFile[] file,
      HttpServletRequest request, String invoiceVouchers) {
    int respstat = RespCode.success;
    HashMap<String, Object> result = new HashMap<>();
    //保存路径
    String uploadPath = "/invoiceFile/";
    //服务域名
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    Integer[] allowCustomType = new Integer[]{CustomType.COMPANY.getCode(), 6};
    boolean flag = channelCustomService.getCustomKeysByType(new HashMap<String, String>(), allowCustomType, customLogin);
    List<QbInvoiceVoucher> invoiceVoucherList = new ArrayList<QbInvoiceVoucher>();
    if (!StringUtil.isEmpty(invoiceVouchers)) {
      invoiceVoucherList = JSONArray.parseArray(invoiceVouchers, QbInvoiceVoucher.class);
    }
    if (!flag){
      result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT));
      return result;
    }
    if (invoiceRecord.getId() == null){
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
      return result;
    }
    try {
      QbInvoiceRecord invoiceRecordOld = qbInvoiceRecordService.selectByPrimaryKey(invoiceRecord.getId());
      if (invoiceRecordOld != null) {
        if (invoiceRecordOld.getStatus() == 1 || invoiceRecordOld.getStatus() == 2 || invoiceRecordOld.getStatus() == 5) {
          //未完成进行审核
          review(invoiceRecord, file, invoiceVoucherList, respstat, result, uploadPath, customLogin, invoiceRecordOld);
        } else {
          //已经审核完成进行修改
          hadReviewUpdate(invoiceRecord, file, invoiceVoucherList, respstat, result, uploadPath, customLogin, invoiceRecordOld);
        }
      } else {
        result.put(RespCode.RESP_STAT, RespCode.INVOICE_RECORD_NOTEXIST);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_RECORD_NOTEXIST));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      result.put(RespCode.RESP_STAT, RespCode.INVOICE_EXCEPTION);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_EXCEPTION));
    }
    return result;
  }

  /**
   * 已经审核完成进行修改
   *
   * @param invoiceRecord
   * @param file
   * @param invoiceVouchers
   * @param respstat
   * @param result
   * @param uploadPath
   * @param customLogin
   * @param invoiceRecordOld
   * @throws IOException
   */
  private void hadReviewUpdate(QbInvoiceRecord invoiceRecord, MultipartFile[] file,
      List<QbInvoiceVoucher> invoiceVouchers, int respstat, HashMap<String, Object> result,
      String uploadPath, ChannelCustom customLogin, QbInvoiceRecord invoiceRecordOld)
      throws IOException {
    //审核完成进行部分内容修改
    invoiceRecordOld.setInvoiceTime(invoiceRecord.getInvoiceTime());
    QbInvoiceReserve invoiceReserve = qbInvoiceReserveService.getReserveByParams(invoiceRecordOld);
    List<QbInvoiceVoucher> invoiceVoucherList = qbInvoiceVoucherService.getInvoiceVoucherBySerialNo(invoiceRecordOld.getInvoiceSerialNo());
    List<Integer> invoiceVoucherIds = invoiceVoucherList.stream().map(QbInvoiceVoucher::getId).collect(Collectors.toList());
    List<QbInvoiceVoucher> insertInvoiceVoucherList = new ArrayList<QbInvoiceVoucher>();
    List<QbInvoiceVoucher> updateInvoiceVoucherList = new ArrayList<QbInvoiceVoucher>();
    for (QbInvoiceVoucher invoiceVoucher : invoiceVouchers) {
      if (invoiceVoucher.getId() == null) {
        //添加的
        insertInvoiceVoucherList.add(invoiceVoucher);
      } else {
        //修改的
        updateInvoiceVoucherList.add(invoiceVoucher);
        invoiceVoucherIds.remove(invoiceVoucher.getId());
      }
    }
    boolean checkFlag = true;
    List<QbInvoicePic> invoicePics = null;
    if (invoiceReserve != null) {
      int alreadyCount = qbInvoiceVoucherService.getAlreadyInvoiceCount(invoiceReserve);
      if ((invoiceReserve.getInvoiceTotalNum() > 0
          && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) > 0
          && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) >= insertInvoiceVoucherList.size())
              || insertInvoiceVoucherList.size() == 0) {
        //插入发票信息
        invoicePics = qbInvoicePicService.getPicListBySerialNo(invoiceRecordOld.getInvoiceSerialNo());
        insertInvoiceVoucher(file, insertInvoiceVoucherList, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,1);
      }else{
        checkFlag = false;
        result.put(RespCode.RESP_STAT, RespCode.INVOICENUM_NOT_ENOUGH);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICENUM_NOT_ENOUGH));
      }
    } else {
      //插入发票信息
      invoicePics = qbInvoicePicService.getPicListBySerialNo(invoiceRecordOld.getInvoiceSerialNo());
      insertInvoiceVoucher(file, insertInvoiceVoucherList, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,0);
    }
    if (checkFlag) {
      for (QbInvoiceVoucher qbInvoiceVoucherUpdate : updateInvoiceVoucherList) {
        qbInvoiceVoucherUpdate.setUpdateTime(DateUtils.getNowDate());
        qbInvoiceVoucherService.updateVoucher(qbInvoiceVoucherUpdate);
      }
      for (Integer invoiceVoucherId : invoiceVoucherIds) {
        Map<String, Object> deleteParams = new HashMap<String, Object>();
        deleteParams.put("id", invoiceVoucherId);
        deleteParams.put("deleteUser", customLogin.getUsername());
        deleteParams.put("updateTime", DateUtils.getNowDate());
        qbInvoiceVoucherService.updateVoucherIsDelete(deleteParams);
      }
      if (invoicePics != null && invoicePics.size() > 0) {
        Integer fileSize = invoicePics.size() + file.length;
        invoiceRecordOld.setInvoiceNum(fileSize);
      } else {
        invoiceRecordOld.setInvoiceNum(file.length);
      }
      invoiceRecordOld.setIsDiscard(invoiceRecord.getIsDiscard());
      invoiceRecordOld.setRejectionReason(invoiceRecord.getRejectionReason());
      invoiceRecordOld.setExpressNo(invoiceRecord.getExpressNo());
      invoiceRecordOld.setUpdateTime(DateUtils.getNowDate());
      qbInvoiceRecordService.updateByPrimaryKey(invoiceRecordOld);
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    }
  }

  /**
   * 未完成进行审核
   *
   * @param invoiceRecord
   * @param file
   * @param invoiceVouchers
   * @param respstat
   * @param result
   * @param uploadPath
   * @param customLogin
   * @param invoiceRecordOld
   * @throws IOException
   */
  private void review(QbInvoiceRecord invoiceRecord, MultipartFile[] file,
      List<QbInvoiceVoucher> invoiceVouchers, int respstat,
      HashMap<String, Object> result, String uploadPath,
      ChannelCustom customLogin, QbInvoiceRecord invoiceRecordOld)
      throws IOException {
    //1.申请待处理，2.申请已受理允许操作
    String[] orderArray = invoiceRecordOld.getOrderNo().split(",");
    boolean checkFlag = true;
    if (orderArray.length > 1) {
      invoiceRecordOld.setInvoiceTime(invoiceRecord.getInvoiceTime());
      List<QbInvoicePic> invoicePics = qbInvoicePicService.getPicListBySerialNo(invoiceRecordOld.getInvoiceSerialNo());
      //多笔复合订单
      if (invoiceRecord.getStatus() == 3) {
        //驳回
        for (String orderNo : orderArray) {
          ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(orderNo);
          channelHistory.setInvoiceStatus(0);
          channelHistory.setUnInvoiceAmount(channelHistory.getInvoiceingAmount());
          channelHistory.setInvoiceingAmount("0");
          channelHistoryService.updateChannelHistory(channelHistory);
        }
      } else if (invoiceRecord.getStatus() == 4) {
        //成功
        QbInvoiceReserve invoiceReserve = qbInvoiceReserveService.getReserveByParams(invoiceRecordOld);
        if (invoiceReserve != null) {
          int alreadyCount = qbInvoiceVoucherService.getAlreadyInvoiceCount(invoiceReserve);
          if (invoiceReserve.getInvoiceTotalNum() > 0
              && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) > 0
                  && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) >= invoiceVouchers.size()) {
            //插入发票信息
            insertInvoiceVoucher(file, invoiceVouchers, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,1);
          } else {
            checkFlag = false;
            //提示发票量信息不存在或票量不足
            respstat = RespCode.INVOICENUM_NOT_ENOUGH;
          }
        } else {
          //插入发票信息
          insertInvoiceVoucher(file, invoiceVouchers, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,0);
        }
        if (checkFlag){//插入开票信息成功
          for (String orderNo : orderArray) {
            ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(orderNo);
            channelHistory.setInvoiceStatus(2);
            channelHistory.setInvoiceAmount(channelHistory.getInvoiceingAmount());
            channelHistory.setInvoiceingAmount("0");
            channelHistoryService.updateChannelHistory(channelHistory);
          }
        }
      }
      if (checkFlag) {
        if (invoicePics != null && invoicePics.size() > 0) {
          Integer fileSize = invoicePics.size() + file.length;
          invoiceRecordOld.setInvoiceNum(fileSize);
        } else {
          invoiceRecordOld.setInvoiceNum(file.length);
        }
        invoiceRecordOld.setStatus(invoiceRecord.getStatus());
        invoiceRecordOld.setIsDiscard(invoiceRecord.getIsDiscard());
        invoiceRecordOld.setRejectionReason(invoiceRecord.getRejectionReason());
        invoiceRecordOld.setExpressNo(invoiceRecord.getExpressNo());
        invoiceRecordOld.setUpdateTime(DateUtils.getNowDate());
        qbInvoiceRecordService.updateByPrimaryKey(invoiceRecordOld);
      }
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    } else {
      invoiceRecordOld.setInvoiceTime(invoiceRecord.getInvoiceTime());
      List<QbInvoicePic> invoicePics = qbInvoicePicService.getPicListBySerialNo(invoiceRecordOld.getInvoiceSerialNo());
      //单笔
      if (invoiceRecord.getStatus() == 3) {
        //驳回
        if (invoiceRecordOld.getStatus() != invoiceRecord.getStatus()) {
          Integer invoiceMethod = invoiceRecordOld.getInvoiceMethod();
          if (1 == invoiceMethod || 2 == invoiceMethod) {//按照充值开票
            ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(invoiceRecordOld.getOrderNo());
            BigDecimal InvoiceAmountDec = new BigDecimal(StringUtil.isEmpty(invoiceRecordOld.getInvoiceAmount()) ? "0" : invoiceRecordOld.getInvoiceAmount());
            BigDecimal invoiceingAmountDec = new BigDecimal(StringUtil.isEmpty(channelHistory.getInvoiceingAmount()) ? "0" : channelHistory.getInvoiceingAmount());
            logger.info("订单号：" + channelHistory.getOrderno() + "开票处理金额" + invoiceingAmountDec);
            BigDecimal unInvoiceAmountDec = new BigDecimal(StringUtil.isEmpty(channelHistory.getUnInvoiceAmount()) ? "0" : channelHistory.getUnInvoiceAmount());
            logger.info("订单号：" + channelHistory.getOrderno() + "待开票金额" + unInvoiceAmountDec);
            BigDecimal unInvoiceAmountDecNew = unInvoiceAmountDec.add(InvoiceAmountDec);
            logger.info("订单号：" + channelHistory.getOrderno() + "驳回后待开票金额" + unInvoiceAmountDecNew.toString());
            channelHistory.setUnInvoiceAmount(unInvoiceAmountDecNew.toString());
            BigDecimal invoiceingAmountDecNew = invoiceingAmountDec.subtract(InvoiceAmountDec);
            channelHistory.setInvoiceingAmount(invoiceingAmountDecNew.toString());
            logger.info("订单号：" + channelHistory.getOrderno() + "驳回后开票处理金额" + invoiceingAmountDecNew.toString());
            logger.info("订单号：" + channelHistory.getOrderno() + "驳回后待开票金额为" + channelHistory.getUnInvoiceAmount() + "原实际打款金额为" + channelHistory.getRealRechargeAmount());
            if (new BigDecimal(channelHistory.getUnInvoiceAmount()).compareTo(new BigDecimal(channelHistory.getRealRechargeAmount())) == 0) {
              logger.info("订单号：" + channelHistory.getOrderno() + "恢复未开票状态");
              channelHistory.setInvoiceStatus(0);
            } else if (new BigDecimal(channelHistory.getUnInvoiceAmount()).compareTo(new BigDecimal(channelHistory.getRealRechargeAmount())) == -1
                && new BigDecimal(channelHistory.getUnInvoiceAmount()).compareTo(new BigDecimal(0)) != 0) {
              channelHistory.setInvoiceStatus(1);
            }
            channelHistoryService.updateChannelHistory(channelHistory);
          } else {//按照下发开票
            String invoiceSerialNo = invoiceRecordOld.getInvoiceSerialNo();
            String leftInvoiceSerialNo;
            if (invoiceSerialNo.endsWith("_1")){
              leftInvoiceSerialNo = invoiceSerialNo.replace("_1","_2");
            }else{
              leftInvoiceSerialNo = invoiceSerialNo.replace("_2","_1");
            }
            Map<String, Object> params = new HashMap<>();
            params.put("invoiceSerialNo", leftInvoiceSerialNo);
            List<QbInvoiceRecord> invoiceRecords = qbInvoiceRecordService.getCommissionInvoiceList(params);
            QbInvoiceRecord leftInvoiceRecord = invoiceRecords.get(0);
            Integer status = leftInvoiceRecord.getStatus();
            if (status == 3) {//另外一张发票也是驳回状态，更改下发记录为失败
              Map<String, Object> updateParams = new HashMap<>();
              updateParams.put("invoiceSerialNo", leftInvoiceRecord.getOrderNo());
              updateParams.put("invoiceStatus2", InvoiceOrderStatus2.FAIL_TYPE.getCode());
              updateParams.put("individualTax", "0");
              updateParams.put("taxRate", "0");
              userCommission2Dao.updateCommissionInvoiceInfoByInvoiceSerialNo(updateParams);
              //更改补个税的下发记录补个税金额为0
              updateParams.clear();
              updateParams.put("invoiceSerialNo2", leftInvoiceRecord.getOrderNo());
              updateParams.put("individualBackTax", "0");
              userCommission2Dao.updateCommissionInvoiceInfoByInvoiceSerialNo(updateParams);
            }
            else if (status == 4) {//另外一张发票为成功
              checkFlag = false;
              respstat = RespCode.NOT_SUPPORT_STATUS;
            }
          }
        }
      } else if (invoiceRecord.getStatus() == 4) {
        if (!invoiceRecordOld.getStatus().equals(invoiceRecord.getStatus())) {
          Integer invoiceMethod = invoiceRecordOld.getInvoiceMethod();
          if (1 == invoiceMethod || 2 == invoiceMethod) {//按照充值开票
            QbInvoiceReserve invoiceReserve = qbInvoiceReserveService.getReserveByParams(invoiceRecordOld);
            if (invoiceReserve != null) {
              int alreadyCount = qbInvoiceVoucherService.getAlreadyInvoiceCount(invoiceReserve);
              if (invoiceReserve.getInvoiceTotalNum() > 0
                      && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) > 0
                      && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) >= invoiceVouchers.size()) {
                //插入开票信息
                insertInvoiceVoucher(file, invoiceVouchers, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,1);
              } else {
                checkFlag = false;
                respstat = RespCode.INVOICENUM_NOT_ENOUGH;
              }
            } else {
              //插入开票信息
              insertInvoiceVoucher(file, invoiceVouchers, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,0);
            }
            if (checkFlag){//发票状态可以修改为 完成
              ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(invoiceRecordOld.getOrderNo());
              BigDecimal InvoiceAmountDec = new BigDecimal(StringUtil.isEmpty(invoiceRecordOld.getInvoiceAmount()) ? "0" : invoiceRecordOld.getInvoiceAmount());
              BigDecimal hasInvoiceAmountDec = new BigDecimal(StringUtil.isEmpty(channelHistory.getInvoiceAmount()) ? "0" : channelHistory.getInvoiceAmount());
              hasInvoiceAmountDec = hasInvoiceAmountDec.add(InvoiceAmountDec);
              channelHistory.setInvoiceAmount(hasInvoiceAmountDec.toString());
              BigDecimal invoiceingAmountDec = new BigDecimal(StringUtil.isEmpty(channelHistory.getInvoiceingAmount()) ? "0" : channelHistory.getInvoiceingAmount());
              invoiceingAmountDec = invoiceingAmountDec.subtract(InvoiceAmountDec);
              channelHistory.setInvoiceingAmount(invoiceingAmountDec.toString());
              BigDecimal hasInvoiceAmountNew = new BigDecimal(StringUtil.isEmpty(channelHistory.getInvoiceAmount()) ? "0" : channelHistory.getInvoiceAmount());
              BigDecimal rechanrgeAmountDec = new BigDecimal(channelHistory.getRealRechargeAmount());
              if (hasInvoiceAmountNew.compareTo(rechanrgeAmountDec) == 0) {
                channelHistory.setInvoiceStatus(2);
              } else {
                channelHistory.setInvoiceStatus(1);
              }
              channelHistoryService.updateChannelHistory(channelHistory);
            }
          } else {//按照实发开票
            String invoiceSerialNo = invoiceRecordOld.getInvoiceSerialNo();
            String leftInvoiceSerialNo;
            if (invoiceSerialNo.endsWith("_1")){
              leftInvoiceSerialNo = invoiceSerialNo.replace("_1","_2");
            }else{
              leftInvoiceSerialNo = invoiceSerialNo.replace("_2","_1");
            }
            Map<String, Object> params = new HashMap<>();
            params.put("invoiceSerialNo", leftInvoiceSerialNo);
            List<QbInvoiceRecord> invoiceRecords = qbInvoiceRecordService.getCommissionInvoiceList(params);
            QbInvoiceRecord leftInvoiceRecord = invoiceRecords.get(0);
            Integer status = leftInvoiceRecord.getStatus();
            if (status == 3){
              checkFlag = false;
              respstat = RespCode.NOT_SUPPORT_STATUS;
            }
            if (checkFlag){
              //先插入发票信息
              QbInvoiceReserve invoiceReserve = qbInvoiceReserveService.getReserveByParams(invoiceRecordOld);
              if (invoiceReserve != null) {
                int alreadyCount = qbInvoiceVoucherService.getAlreadyInvoiceCount(invoiceReserve);
                if (invoiceReserve.getInvoiceTotalNum() > 0
                        && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) > 0
                        && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) >= invoiceVouchers.size()) {
                  //插入开票信息
                  insertInvoiceVoucher(file, invoiceVouchers, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,1);
                  //更改下发状态
                  Map<String, Object> updateParams = new HashMap<>();
                  updateParams.put("invoiceSerialNo", leftInvoiceRecord.getOrderNo());
                  updateParams.put("invoiceStatus2", InvoiceOrderStatus2.FINISH_TYPE.getCode());
                  userCommission2Dao.updateCommissionInvoiceInfoByInvoiceSerialNo(updateParams);
                } else {
                  checkFlag = false;
                  respstat = RespCode.INVOICENUM_NOT_ENOUGH;
                }
              } else {
                //插入开票信息
                insertInvoiceVoucher(file, invoiceVouchers, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,0);
                //更改下发状态
                Map<String, Object> updateParams = new HashMap<>();
                updateParams.put("invoiceSerialNo", leftInvoiceRecord.getOrderNo());
                updateParams.put("invoiceStatus2", InvoiceOrderStatus2.FINISH_TYPE.getCode());
                userCommission2Dao.updateCommissionInvoiceInfoByInvoiceSerialNo(updateParams);
              }
            }
          }
        }
      }
      if (checkFlag) {
        if (invoicePics != null && invoicePics.size() > 0) {
          Integer fileSize = invoicePics.size() + file.length;
          invoiceRecordOld.setInvoiceNum(fileSize);
        } else {
          invoiceRecordOld.setInvoiceNum(file.length);
        }
        invoiceRecordOld.setStatus(invoiceRecord.getStatus());
        invoiceRecordOld.setIsDiscard(invoiceRecord.getIsDiscard());
        invoiceRecordOld.setRejectionReason(invoiceRecord.getRejectionReason());
        invoiceRecordOld.setExpressNo(invoiceRecord.getExpressNo());
        invoiceRecordOld.setUpdateTime(DateUtils.getNowDate());
        qbInvoiceRecordService.updateByPrimaryKey(invoiceRecordOld);
      }
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    }
  }

  private void insertInvoiceVoucher(MultipartFile[] file, List<QbInvoiceVoucher> invoiceVouchers, String uploadPath,
                                    ChannelCustom customLogin, QbInvoiceRecord invoiceRecordOld,
                                    QbInvoiceReserve invoiceReserve,Integer hasCount) throws IOException {
    for (QbInvoiceVoucher invoiceVoucher : invoiceVouchers) {
      invoiceVoucher.setInvoiceType(invoiceRecordOld.getInvoiceType());
      invoiceVoucher.setCompanyId(invoiceRecordOld.getCompanyId());
      invoiceVoucher.setCustomkey(invoiceRecordOld.getCustomkey());
      invoiceVoucher.setInvoiceDate(invoiceRecordOld.getInvoiceTime());
      if (invoiceReserve != null){
        invoiceVoucher.setInvoiceLimitAmout(invoiceReserve.getInvoiceLimitAmout());
      }
      invoiceVoucher.setInvoiceSerialNo(invoiceRecordOld.getInvoiceSerialNo());
      invoiceVoucher.setApprovalFlag(1);
      invoiceVoucher.setAddUser(customLogin.getUsername());
      invoiceVoucher.setCreateTime(DateUtils.getNowDate());
      invoiceVoucher.setHasCount(hasCount);
      int i = qbInvoiceVoucherService.insert(invoiceVoucher);
    }
    String filePath = uploadPath + invoiceRecordOld.getCustomkey() + "/";
    if (file.length > 0) {
      for (MultipartFile mf : file) {
        if (!mf.isEmpty()) {
          //使用UUID图片重命名
          String name = UUID.randomUUID().toString().replaceAll("-", "");
          //获取文件扩展名
          String ext = FilenameUtils.getExtension(mf.getOriginalFilename());
          //设置文件上传路径
          String fileName = name + "." + ext;
          InputStream in = new ByteArrayInputStream(mf.getBytes());
          String uploadFile = FtpTool
              .uploadFile(bestSignConfig.getFtpURL(), 21, filePath, fileName, in,
                  bestSignConfig.getUsername(), bestSignConfig.getPassword());
          QbInvoicePic invoicePic = new QbInvoicePic();
          if (!"error".equals(uploadFile)) {
            invoicePic.setInvoicePicUrl(filePath + fileName);
          }
          invoicePic.setAddUser(customLogin.getUsername());
          invoicePic.setInvoiceSerialNo(invoiceRecordOld.getInvoiceSerialNo());
          invoicePic.setCreateTime(DateUtils.getNowDate());
          qbInvoicePicService.insert(invoicePic);
        }
      }
    }

  }

  /**
   * 批量审核发票信息
   *
   * @return
   */
  @RequestMapping("/reviewBatchInvoice")
  @ResponseBody
  public Map<String, Object> reviewBatchInvoice(String ids, HttpServletRequest request) {
    int respstat = RespCode.success;
    HashMap<String, Object> result = new HashMap<>();
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    boolean flag = false;
    if (customLogin.getCustomType() == 4 && customLogin.getMasterCustom() != null) {
      ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customLogin.getMasterCustom());
      if ((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (
          CustomType.ROOT.getCode() == masterChannelCustom.getCustomType()
                  && CommonString.ROOT.equals(masterChannelCustom.getMasterCustom())
                  && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))) {
        flag = true;
      }
      if (masterChannelCustom.getCustomType() == 2) {
        flag = true;
      }
    } else {
      if ((CommonString.ROOT.equals(customLogin.getCustomkey())
              || (CustomType.ROOT.getCode() == customLogin.getCustomType()
              && CommonString.ROOT.equals(customLogin.getMasterCustom())
              && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
        flag = true;
      }
      if (customLogin.getCustomType() == 2) {
        flag = true;
      }
    }
    if (!flag){
      result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT));
      return result;
    }
    if (StringUtil.isEmpty(ids)){
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
      return result;
    }
    try {
      List<QbInvoiceRecord> invoiceRecords = qbInvoiceRecordService.selectByPrimaryKeys(ids);
      if (invoiceRecords.size() <= 0) {
        String[] idsArray = ids.split(",");
        for (String id : idsArray) {
          QbInvoiceRecord invoiceRecordOld = qbInvoiceRecordService.selectByPrimaryKey(Integer.parseInt(id));
          if (invoiceRecordOld != null) {
            //1.申请待处理，2.申请已受理允许操作
            String[] orderArray = invoiceRecordOld.getOrderNo().split(",");
            if (orderArray.length > 1) {
              //多笔复合订单
              for (String orderNo : orderArray) {
                ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(orderNo);
                channelHistory.setInvoiceStatus(2);
                channelHistory.setInvoiceAmount(channelHistory.getInvoiceingAmount());
                channelHistory.setInvoiceingAmount("0");
                channelHistoryService.updateChannelHistory(channelHistory);
              }
              invoiceRecordOld.setInvoiceNum(0);
              invoiceRecordOld.setStatus(4);
              invoiceRecordOld.setUpdateTime(DateUtils.getNowDate());
              qbInvoiceRecordService.updateByPrimaryKey(invoiceRecordOld);
              result.put(RespCode.RESP_STAT, respstat);
              result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
            } else {
              //单笔-包括充值开票和下发开票
              Integer invoiceMethod = invoiceRecordOld.getInvoiceMethod();
              if (1 == invoiceMethod || 2 == invoiceMethod){//按照充值开票
                ChannelHistory channelHistory = channelHistoryService
                    .getChannelHistoryByOrderno(invoiceRecordOld.getOrderNo());
                BigDecimal InvoiceAmountDec = new BigDecimal(
                    StringUtil.isEmpty(invoiceRecordOld.getInvoiceAmount()) ? "0"
                        : invoiceRecordOld.getInvoiceAmount());
                BigDecimal hasInvoiceAmountDec = new BigDecimal(
                    StringUtil.isEmpty(channelHistory.getInvoiceAmount()) ? "0"
                        : channelHistory.getInvoiceAmount());
                hasInvoiceAmountDec = hasInvoiceAmountDec.add(InvoiceAmountDec);
                channelHistory.setInvoiceAmount(hasInvoiceAmountDec.toString());
                BigDecimal invoiceingAmountDec = new BigDecimal(
                    StringUtil.isEmpty(channelHistory.getInvoiceingAmount()) ? "0"
                        : channelHistory.getInvoiceingAmount());
                invoiceingAmountDec = invoiceingAmountDec.subtract(InvoiceAmountDec);
                channelHistory.setInvoiceingAmount(invoiceingAmountDec.toString());
                BigDecimal hasInvoiceAmountNew = new BigDecimal(
                    StringUtil.isEmpty(channelHistory.getInvoiceAmount()) ? "0"
                        : channelHistory.getInvoiceAmount());
                BigDecimal rechanrgeAmountDec = new BigDecimal(
                    channelHistory.getRealRechargeAmount());
                if (hasInvoiceAmountNew.compareTo(rechanrgeAmountDec) == 0) {
                  channelHistory.setInvoiceStatus(2);
                } else {
                  channelHistory.setInvoiceStatus(1);
                }
                channelHistoryService.updateChannelHistory(channelHistory);
              }else{ //按照下发开票
                String invoiceSerialNo = invoiceRecordOld.getInvoiceSerialNo();
                String leftInvoiceSerialNo;
                if (invoiceSerialNo.endsWith("_1")){
                  leftInvoiceSerialNo = invoiceSerialNo.replace("_1","_2");
                }else{
                  leftInvoiceSerialNo = invoiceSerialNo.replace("_2","_1");
                }
                Map<String, Object> params = new HashMap<>();
                params.put("invoiceSerialNo", leftInvoiceSerialNo);
                List<QbInvoiceRecord> commissionInvoiceRecords = qbInvoiceRecordService.getCommissionInvoiceList(params);
                QbInvoiceRecord leftInvoiceRecord = commissionInvoiceRecords.get(0);
                Integer status = leftInvoiceRecord.getStatus();
                if (status == 4) {//另外一张发票也是成功状态，更改下发记录为成功
                  Map<String, Object> updateParams = new HashMap<>();
                  updateParams.put("invoiceSerialNo", leftInvoiceRecord.getOrderNo());
                  updateParams.put("invoiceStatus2", InvoiceOrderStatus2.FINISH_TYPE.getCode());
                  userCommission2Dao.updateCommissionInvoiceInfoByInvoiceSerialNo(updateParams);
                } else if (status == 3) {//另外一张发票为驳回
                  flag = false;
                  result.put(RespCode.RESP_STAT, RespCode.NOT_SUPPORT_STATUS);
                  result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.NOT_SUPPORT_STATUS));
                }
              }
              if (flag){
                invoiceRecordOld.setInvoiceNum(0);
                invoiceRecordOld.setStatus(4);
                invoiceRecordOld.setUpdateTime(DateUtils.getNowDate());
                qbInvoiceRecordService.updateByPrimaryKey(invoiceRecordOld);
                result.put(RespCode.RESP_STAT, respstat);
                result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
              }
            }
          } else {
            result.put(RespCode.RESP_STAT, RespCode.INVOICE_RECORD_NOTEXIST);
            result.put(RespCode.RESP_MSG,
                RespCode.codeMaps.get(RespCode.INVOICE_RECORD_NOTEXIST));
          }
        }
      } else {
        result.put(RespCode.RESP_STAT, RespCode.CURRENT_STATUS_REFUSE);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.CURRENT_STATUS_REFUSE));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      result.put(RespCode.RESP_STAT, RespCode.INVOICE_EXCEPTION);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_EXCEPTION));
    }
    return result;
  }


  /**
   * 批量审核发票信息
   *
   * @return
   */
  @RequestMapping("/acceptBatchInvoice")
  @ResponseBody
  public Map<String, Object> acceptBatchInvoice(String ids, HttpServletRequest request) {
    int respstat = RespCode.success;
    HashMap<String, Object> result = new HashMap<>();
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    boolean flag = false;
    //root 账户 和 服务公司
    if (customLogin.getCustomType() == 4 &&  !StringUtil.isEmpty(customLogin.getMasterCustom())){//机构账户
      ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customLogin.getMasterCustom());
      if ((CommonString.ROOT.equals(masterChannelCustom.getCustomkey())
              || (CustomType.ROOT.getCode() == masterChannelCustom.getCustomType()
                      && CommonString.ROOT.equals(masterChannelCustom.getMasterCustom())
                      && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole())
              || masterChannelCustom.getCustomType() == 2)) {
        flag = true;
      }
    }else{
      if ((CommonString.ROOT.equals(customLogin.getCustomkey())
              || (CustomType.ROOT.getCode() == customLogin.getCustomType()
              && CommonString.ROOT.equals(customLogin.getMasterCustom())
              && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole())
              || customLogin.getCustomType() == 2)) {
        flag = true;
      }
    }
    if (!flag){
      result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT));
      return result;
    }
    if (StringUtil.isEmpty(ids)){
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
      return result;
    }
    try {
      List<QbInvoiceRecord> invoiceRecords = qbInvoiceRecordService.selectByPrimaryKeys(ids);
      if (invoiceRecords.size() > 0){
        result.put(RespCode.RESP_STAT, RespCode.CURRENT_STATUS_REFUSE);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.CURRENT_STATUS_REFUSE));
        return result;
      }
      String[] idsArray = ids.split(",");
      for (String id : idsArray) {
        QbInvoiceRecord invoiceRecordOld = qbInvoiceRecordService.selectByPrimaryKey(Integer.parseInt(id));
        if (invoiceRecordOld.getStatus() == 1){
          invoiceRecordOld.setStatus(2);
          invoiceRecordOld.setIsDiscard(0);
          invoiceRecordOld.setUpdateTime(DateUtils.getNowDate());
          qbInvoiceRecordService.updateByPrimaryKey(invoiceRecordOld);
        }
      }
    }catch (Exception e){
      logger.error(e.getMessage(), e);
      result.put(RespCode.RESP_STAT, RespCode.INVOICE_EXCEPTION);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_EXCEPTION));
    }
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    return result;
  }


  /**
   * 发票凭证查看
   *
   * @param invoiceRecord
   * @return
   */
  @RequestMapping("/picList")
  @ResponseBody
  public Map<String, Object> invoicePicList(QbInvoiceRecord invoiceRecord) {
    int respstat = RespCode.success;
    HashMap<String, Object> result = new HashMap<>();
    if (!StringUtil.isEmpty(invoiceRecord.getInvoiceSerialNo())) {
      //根据发票流水号查看发票凭证
      List<QbInvoicePic> picList = qbInvoicePicService
          .getPicListBySerialNo(invoiceRecord.getInvoiceSerialNo());
      result.put("relationList", picList);
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    } else {
      result.put(RespCode.RESP_STAT, RespCode.error101);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
    }
    return result;
  }

  /**
   * 图片下载
   *
   * @param invoiceRecord
   * @param response
   * @throws IOException
   */
  @RequestMapping("/downloadPic")
  public ResponseEntity<byte[]> downloadPic(QbInvoiceRecord invoiceRecord,
      HttpServletResponse response) throws IOException {
    QbInvoicePic pic = qbInvoicePicService.selectByPrimaryKey(invoiceRecord.getId());
    String fileName = pic.getInvoicePicUrl().substring(pic.getInvoicePicUrl().lastIndexOf("/") + 1);
    String filePath = pic.getInvoicePicUrl();
    byte[] bytes = FtpTool
        .downloadFtpFile(filePath.substring(0, filePath.lastIndexOf("/")), fileName);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// 防止中文乱码
    headers.add("Content-Disposition", "attachment;filename=" + fileName);
    headers.setContentDispositionFormData("attachment", fileName);
    return new ResponseEntity<byte[]>(bytes,
        headers, HttpStatus.OK);
  }

  /**
   * 图片删除
   *
   * @param invoiceRecord
   * @param response
   */
  @RequestMapping("/deletePic")
  @ResponseBody
  public Map<String, Object> deletePic(QbInvoiceRecord invoiceRecord, HttpServletResponse response,
      HttpServletRequest request) {
    int respstat = RespCode.success;
    HashMap<String, Object> result = new HashMap<>();
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    boolean flag = false;
    if (customLogin.getCustomType() == 4 && customLogin.getMasterCustom() != null) {
      ChannelCustom masterChannelCustom = customService
          .getCustomByCustomkey(customLogin.getMasterCustom());
      if ((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (
          CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT
              .equals(masterChannelCustom.getMasterCustom())
              && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))) {
        flag = true;
      }
      if (masterChannelCustom.getCustomType() == 2) {
        flag = true;
      }
    } else {
      if ((CommonString.ROOT.equals(customLogin.getCustomkey()) || (
          CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT
              .equals(customLogin.getMasterCustom())
              && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
        flag = true;
      }
      if (customLogin.getCustomType() == 2) {
        flag = true;
      }
    }
    if (flag) {
      //根据发票记录获取发票凭证
      QbInvoicePic pic = qbInvoicePicService.selectByPrimaryKey(invoiceRecord.getId());
      FTPClient ftpClient = new FTPClient();
      ftpClient.setControlEncoding("utf-8");
      String fileName = pic.getInvoicePicUrl()
          .substring(pic.getInvoicePicUrl().lastIndexOf("/") + 1);
      try {
        ftpClient.connect(bestSignConfig.getFtpURL(), 21);
        ftpClient.login(bestSignConfig.getUsername(), bestSignConfig.getPassword());
        ftpClient.dele(fileName);
        ftpClient.logout();
        QbInvoiceRecord invoiceRecordOld = qbInvoiceRecordService
            .getByInvoiceSerialNo(pic.getInvoiceSerialNo());
        //删除发票凭证图片
        qbInvoicePicService.deleteByPrimaryKey(invoiceRecord.getId());
        invoiceRecordOld.setInvoiceNum(invoiceRecordOld.getInvoiceNum() - 1);
        //更新发票记录发票凭证图片数量
        qbInvoiceRecordService.updateByPrimaryKey(invoiceRecordOld);
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        result.put(RespCode.RESP_STAT, RespCode.DELETE_PIC_EXCEPTION);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DELETE_PIC_EXCEPTION));
      } finally {
        if (ftpClient.isConnected()) {
          try {
            ftpClient.disconnect();
          } catch (Exception e) {
            logger.error(e.getMessage(),e);
          }
        }
      }
    } else {
      result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT));
    }

    return result;
  }

  /**
   * 是否显示发票审核按钮
   *
   * @param request
   * @return
   */
  @RequestMapping("/isShowReviewInvoiceButton")
  @ResponseBody
  public Map<String, Object> isShowReviewInvoiceButton(HttpServletRequest request) {
    int respstat = RespCode.success;
    HashMap<String, Object> result = new HashMap<>();
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    boolean flag = false;
    if (customLogin.getCustomType() == 4 && customLogin.getMasterCustom() != null) {
      ChannelCustom masterChannelCustom = customService
          .getCustomByCustomkey(customLogin.getMasterCustom());
      if ((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (
          CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT
              .equals(masterChannelCustom.getMasterCustom())
              && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))) {
        flag = true;
      }
      if (masterChannelCustom.getCustomType() == 2) {
        flag = true;
      }
    } else {
      if ((CommonString.ROOT.equals(customLogin.getCustomkey()) || (
          CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT
              .equals(customLogin.getMasterCustom())
              && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
        flag = true;
      }
      if (customLogin.getCustomType() == 2) {
        flag = true;
      }
    }
    result.put("isShow", flag);
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    return result;
  }


  /**
   * @return java.util.List<java.util.HashMap < java.lang.String, java.lang.Object>>
   * @Author YJY
   * @Description 查询出所有符合条件的开票金额
   * @Date 2020/7/20
   * @Param [relationList]
   **/
  public List<UserCommission> checkInvoice(List<Map<String, Object>> relationList) {

    List<UserCommission> allCommission = new ArrayList<>();

    List<UserCommission> userCommissionListRate = new ArrayList<>();
    List<UserCommission> userCommissionListSum = new ArrayList<>();
    List<UserCommission> userCommissionListBack = new ArrayList<>();
    String invoiceMethod = "";
    List listSer = new ArrayList();
    if (CollectionUtils.isNotEmpty(relationList)) {

      for (Map<String, Object> map : relationList) {
        invoiceMethod = StringUtil.isEmpty(String.valueOf(map.get("invoiceMethod"))) ? "1"
            : String.valueOf(map.get("invoiceMethod"));

        if ("3".equals(invoiceMethod) || "4".equals(invoiceMethod)) {

          listSer.add(map.get("orderNo"));

        }

      }
      if (CollectionUtils.isNotEmpty(listSer)) {
        //获取所符合条件的发票流水费率
        userCommissionListRate = personalIncomeTaxRateService.selectByListInvoiceSerialNo(listSer);
        //获取所符合条件的发票流水钱数总计
        userCommissionListSum = personalIncomeTaxRateService
            .selectSumByListInvoiceSerialNo(listSer);
        //获取所符合条件的发票补充个税总计
        userCommissionListBack = personalIncomeTaxRateService
            .findIndividualBackTax(listSer);
        //按照发票流水号统计明细数据
        allCommission = integrationData(userCommissionListRate, userCommissionListSum,userCommissionListBack);
      }
    }

    return allCommission;
  }


  /**
   * @return java.util.List<java.lang.Object>
   * @Author YJY
   * @Description 按照流水号 月份 统计
   * @Date 2020/7/20
   * @Param [listUserCommission]
   **/
  public List<UserCommission> integrationData(List<UserCommission> listUserCommissionRate,
      List<UserCommission> listUserCommissionSum,List<UserCommission> listBackRate) {

    //sum数据
    for (UserCommission sum : listUserCommissionSum) {
      sum.setIndividualBackTax("0");
      //税率
      for (UserCommission rate : listUserCommissionRate) {

        if (StringUtils.isNotBlank(rate.getCalculationRates())
            && sum.getAccountDate().equals(rate.getAccountDate())
            && (!sum.getCalculationRates().contains(rate.getCalculationRates()))
        ) {


            sum.setCalculationRates(sum.getCalculationRates() +"%-"+ rate.getCalculationRates());


        }
        //个税税率 不为空 日期相等 不包含
        if (StringUtils.isNotBlank(rate.getTaxRate())
            && sum.getAccountDate().equals(rate.getAccountDate())
            && (!sum.getTaxRate().contains(rate.getTaxRate()))
        ) {

          sum.setTaxRate(sum.getTaxRate() + "%-"+ rate.getTaxRate());

        }
      }

      if(CollectionUtils.isNotEmpty(listBackRate)) {
        //补充个税
        for (UserCommission back : listBackRate) {

          if (sum.getAccountDate().equals(back.getAccountDate())
           && sum.getInvoiceSerialNo().equals(back.getInvoiceSerialNo())) {

            sum.setIndividualBackTax(back.getIndividualBackTax());
          }
        }
      }
      if(!sum.getTaxRate().endsWith("%")){

        sum.setTaxRate(sum.getTaxRate()+"%");
      }

      if(!sum.getCalculationRates().endsWith("%")){

        sum.setCalculationRates(sum.getCalculationRates()+"%");
      }
    }

    return listUserCommissionSum;
  }


}
