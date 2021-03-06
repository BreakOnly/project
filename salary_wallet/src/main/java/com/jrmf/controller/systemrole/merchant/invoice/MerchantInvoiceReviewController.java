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
  //????????????service
  @Autowired
  private QbInvoiceReserveService qbInvoiceReserveService;
  //??????????????????
  @Autowired
  private QbInvoiceVoucherService qbInvoiceVoucherService;
  //????????????service
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
   * ??????????????????
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
        //??????
      } else if (masterChannelCustom.getCustomType() == CustomType.CUSTOM.getCode()) {
        //????????????
        page.getParams().put("loginCustomer", masterChannelCustom.getCustomkey());
      } else if (masterChannelCustom.getCustomType() == CustomType.GROUP.getCode()) {
        //????????????
        int nodeId = organizationTreeService
            .queryNodeIdByCustomKey(masterChannelCustom.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(masterChannelCustom.getCustomType(),
                QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
        page.getParams().put("loginCustomer", String.join(",", customKeys));
      } else if (masterChannelCustom.getCustomType() == CustomType.COMPANY.getCode()) {
        //????????????
        page.getParams().put("companyId", masterChannelCustom.getCustomkey());
        page.getParams().put("originalIds", masterChannelCustom.getCustomkey());
      } else if (masterChannelCustom.getCustomType() == CustomType.PROXY.getCode()) {
        //?????????
        //?????????????????????????????????
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
        //??????
      } else if (customLogin.getCustomType() == CustomType.CUSTOM.getCode()) {
        //????????????
        page.getParams().put("loginCustomer", customLogin.getCustomkey());
      } else if (customLogin.getCustomType() == CustomType.GROUP.getCode()) {
        //????????????
        int nodeId = organizationTreeService.queryNodeIdByCustomKey(customLogin.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(customLogin.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                nodeId);
        page.getParams().put("loginCustomer", String.join(",", customKeys));
      } else if (customLogin.getCustomType() == CustomType.COMPANY.getCode()) {
        //????????????
        page.getParams().put("companyId", customLogin.getCustomkey());
        page.getParams().put("originalIds", customLogin.getCustomkey());
      } else if (customLogin.getCustomType() == CustomType.PROXY.getCode()) {
        //?????????
        //?????????????????????????????????
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
   * ??????excel
   */
  @RequestMapping("/export")
  public void export(HttpServletResponse response, HttpServletRequest request) {
    // ??????
    String[] headers = new String[]{"????????????", "?????????????????????", "????????????(????????????)", "???????????????", "???????????????", "??????????????????",
        "????????????", "??????", "??????", "????????????", "????????????", "????????????", "???????????????", "????????????", "????????????", "???????????????????????????", "????????????",
        "????????????????????????", "????????????", "????????????", "????????????",
        "????????????", "????????????", "????????????", "??????"};
    String filename = "??????????????????";
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
        //??????
      } else if (masterChannelCustom.getCustomType() == CustomType.CUSTOM.getCode()) {
        //????????????
        page.getParams().put("loginCustomer", masterChannelCustom.getCustomkey());
      } else if (masterChannelCustom.getCustomType() == CustomType.GROUP.getCode()) {
        //????????????
        int nodeId = organizationTreeService
            .queryNodeIdByCustomKey(masterChannelCustom.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(masterChannelCustom.getCustomType(),
                QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
        page.getParams().put("loginCustomer", String.join(",", customKeys));
      } else if (masterChannelCustom.getCustomType() == CustomType.COMPANY.getCode()) {
        //????????????
        page.getParams().put("companyId", masterChannelCustom.getCustomkey());
        page.getParams().put("originalIds", masterChannelCustom.getCustomkey());
      } else if (masterChannelCustom.getCustomType() == CustomType.PROXY.getCode()) {
        //?????????
        //?????????????????????????????????
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
        //??????
      } else if (customLogin.getCustomType() == CustomType.CUSTOM.getCode()) {
        //????????????
        page.getParams().put("loginCustomer", customLogin.getCustomkey());
      } else if (customLogin.getCustomType() == CustomType.GROUP.getCode()) {
        //????????????
        int nodeId = organizationTreeService.queryNodeIdByCustomKey(customLogin.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(customLogin.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                nodeId);
        page.getParams().put("loginCustomer", String.join(",", customKeys));
      } else if (customLogin.getCustomType() == CustomType.COMPANY.getCode()) {
        //????????????
        page.getParams().put("companyId", customLogin.getCustomkey());
        page.getParams().put("originalIds", customLogin.getCustomkey());
      } else if (customLogin.getCustomType() == CustomType.PROXY.getCode()) {
        //?????????
        //?????????????????????????????????
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
        dataMap.put("24", "????????????");
      } else {
        dataMap.put("24", invioceBase.get("rejectionReason"));
      }
      if (CollectionUtils.isNotEmpty(listDetails)) {
        //??????????????????
        dataMap.put("25", invioceBase.get("invoiceMethod"));
        dataMap.put("26", invioceBase.get("remark"));
      } else {
        dataMap.put("25", invioceBase.get("remark"));
      }
      data.add(sortMapByKey(dataMap));
    }

    if (CollectionUtils.isNotEmpty(listDetails)) {
      String[] newHeaders = Arrays.copyOf(headers, 31);

      newHeaders[25] = "??????????????????";
      newHeaders[26] = "???????????????";
      newHeaders[27] = "????????????";
      newHeaders[28] = "????????????";
      newHeaders[29] = "????????????";
      newHeaders[30] = "??????????????????";
      ExcelFileGenerator
          .ExcelExportAndMergedRegion(response, newHeaders, filename, data, listDetails);
    } else {
      ExcelFileGenerator.ExcelExport(response, headers, filename, data);
    }
  }

  /**
   * ??????????????????
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
    //????????????
    String uploadPath = "/invoiceFile/";
    //????????????
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
          //?????????????????????
          review(invoiceRecord, file, invoiceVoucherList, respstat, result, uploadPath, customLogin, invoiceRecordOld);
        } else {
          //??????????????????????????????
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
   * ??????????????????????????????
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
    //????????????????????????????????????
    invoiceRecordOld.setInvoiceTime(invoiceRecord.getInvoiceTime());
    QbInvoiceReserve invoiceReserve = qbInvoiceReserveService.getReserveByParams(invoiceRecordOld);
    List<QbInvoiceVoucher> invoiceVoucherList = qbInvoiceVoucherService.getInvoiceVoucherBySerialNo(invoiceRecordOld.getInvoiceSerialNo());
    List<Integer> invoiceVoucherIds = invoiceVoucherList.stream().map(QbInvoiceVoucher::getId).collect(Collectors.toList());
    List<QbInvoiceVoucher> insertInvoiceVoucherList = new ArrayList<QbInvoiceVoucher>();
    List<QbInvoiceVoucher> updateInvoiceVoucherList = new ArrayList<QbInvoiceVoucher>();
    for (QbInvoiceVoucher invoiceVoucher : invoiceVouchers) {
      if (invoiceVoucher.getId() == null) {
        //?????????
        insertInvoiceVoucherList.add(invoiceVoucher);
      } else {
        //?????????
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
        //??????????????????
        invoicePics = qbInvoicePicService.getPicListBySerialNo(invoiceRecordOld.getInvoiceSerialNo());
        insertInvoiceVoucher(file, insertInvoiceVoucherList, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,1);
      }else{
        checkFlag = false;
        result.put(RespCode.RESP_STAT, RespCode.INVOICENUM_NOT_ENOUGH);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICENUM_NOT_ENOUGH));
      }
    } else {
      //??????????????????
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
   * ?????????????????????
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
    //1.??????????????????2.???????????????????????????
    String[] orderArray = invoiceRecordOld.getOrderNo().split(",");
    boolean checkFlag = true;
    if (orderArray.length > 1) {
      invoiceRecordOld.setInvoiceTime(invoiceRecord.getInvoiceTime());
      List<QbInvoicePic> invoicePics = qbInvoicePicService.getPicListBySerialNo(invoiceRecordOld.getInvoiceSerialNo());
      //??????????????????
      if (invoiceRecord.getStatus() == 3) {
        //??????
        for (String orderNo : orderArray) {
          ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(orderNo);
          channelHistory.setInvoiceStatus(0);
          channelHistory.setUnInvoiceAmount(channelHistory.getInvoiceingAmount());
          channelHistory.setInvoiceingAmount("0");
          channelHistoryService.updateChannelHistory(channelHistory);
        }
      } else if (invoiceRecord.getStatus() == 4) {
        //??????
        QbInvoiceReserve invoiceReserve = qbInvoiceReserveService.getReserveByParams(invoiceRecordOld);
        if (invoiceReserve != null) {
          int alreadyCount = qbInvoiceVoucherService.getAlreadyInvoiceCount(invoiceReserve);
          if (invoiceReserve.getInvoiceTotalNum() > 0
              && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) > 0
                  && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) >= invoiceVouchers.size()) {
            //??????????????????
            insertInvoiceVoucher(file, invoiceVouchers, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,1);
          } else {
            checkFlag = false;
            //?????????????????????????????????????????????
            respstat = RespCode.INVOICENUM_NOT_ENOUGH;
          }
        } else {
          //??????????????????
          insertInvoiceVoucher(file, invoiceVouchers, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,0);
        }
        if (checkFlag){//????????????????????????
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
      //??????
      if (invoiceRecord.getStatus() == 3) {
        //??????
        if (invoiceRecordOld.getStatus() != invoiceRecord.getStatus()) {
          Integer invoiceMethod = invoiceRecordOld.getInvoiceMethod();
          if (1 == invoiceMethod || 2 == invoiceMethod) {//??????????????????
            ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(invoiceRecordOld.getOrderNo());
            BigDecimal InvoiceAmountDec = new BigDecimal(StringUtil.isEmpty(invoiceRecordOld.getInvoiceAmount()) ? "0" : invoiceRecordOld.getInvoiceAmount());
            BigDecimal invoiceingAmountDec = new BigDecimal(StringUtil.isEmpty(channelHistory.getInvoiceingAmount()) ? "0" : channelHistory.getInvoiceingAmount());
            logger.info("????????????" + channelHistory.getOrderno() + "??????????????????" + invoiceingAmountDec);
            BigDecimal unInvoiceAmountDec = new BigDecimal(StringUtil.isEmpty(channelHistory.getUnInvoiceAmount()) ? "0" : channelHistory.getUnInvoiceAmount());
            logger.info("????????????" + channelHistory.getOrderno() + "???????????????" + unInvoiceAmountDec);
            BigDecimal unInvoiceAmountDecNew = unInvoiceAmountDec.add(InvoiceAmountDec);
            logger.info("????????????" + channelHistory.getOrderno() + "????????????????????????" + unInvoiceAmountDecNew.toString());
            channelHistory.setUnInvoiceAmount(unInvoiceAmountDecNew.toString());
            BigDecimal invoiceingAmountDecNew = invoiceingAmountDec.subtract(InvoiceAmountDec);
            channelHistory.setInvoiceingAmount(invoiceingAmountDecNew.toString());
            logger.info("????????????" + channelHistory.getOrderno() + "???????????????????????????" + invoiceingAmountDecNew.toString());
            logger.info("????????????" + channelHistory.getOrderno() + "???????????????????????????" + channelHistory.getUnInvoiceAmount() + "????????????????????????" + channelHistory.getRealRechargeAmount());
            if (new BigDecimal(channelHistory.getUnInvoiceAmount()).compareTo(new BigDecimal(channelHistory.getRealRechargeAmount())) == 0) {
              logger.info("????????????" + channelHistory.getOrderno() + "?????????????????????");
              channelHistory.setInvoiceStatus(0);
            } else if (new BigDecimal(channelHistory.getUnInvoiceAmount()).compareTo(new BigDecimal(channelHistory.getRealRechargeAmount())) == -1
                && new BigDecimal(channelHistory.getUnInvoiceAmount()).compareTo(new BigDecimal(0)) != 0) {
              channelHistory.setInvoiceStatus(1);
            }
            channelHistoryService.updateChannelHistory(channelHistory);
          } else {//??????????????????
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
            if (status == 3) {//??????????????????????????????????????????????????????????????????
              Map<String, Object> updateParams = new HashMap<>();
              updateParams.put("invoiceSerialNo", leftInvoiceRecord.getOrderNo());
              updateParams.put("invoiceStatus2", InvoiceOrderStatus2.FAIL_TYPE.getCode());
              updateParams.put("individualTax", "0");
              updateParams.put("taxRate", "0");
              userCommission2Dao.updateCommissionInvoiceInfoByInvoiceSerialNo(updateParams);
              //????????????????????????????????????????????????0
              updateParams.clear();
              updateParams.put("invoiceSerialNo2", leftInvoiceRecord.getOrderNo());
              updateParams.put("individualBackTax", "0");
              userCommission2Dao.updateCommissionInvoiceInfoByInvoiceSerialNo(updateParams);
            }
            else if (status == 4) {//???????????????????????????
              checkFlag = false;
              respstat = RespCode.NOT_SUPPORT_STATUS;
            }
          }
        }
      } else if (invoiceRecord.getStatus() == 4) {
        if (!invoiceRecordOld.getStatus().equals(invoiceRecord.getStatus())) {
          Integer invoiceMethod = invoiceRecordOld.getInvoiceMethod();
          if (1 == invoiceMethod || 2 == invoiceMethod) {//??????????????????
            QbInvoiceReserve invoiceReserve = qbInvoiceReserveService.getReserveByParams(invoiceRecordOld);
            if (invoiceReserve != null) {
              int alreadyCount = qbInvoiceVoucherService.getAlreadyInvoiceCount(invoiceReserve);
              if (invoiceReserve.getInvoiceTotalNum() > 0
                      && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) > 0
                      && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) >= invoiceVouchers.size()) {
                //??????????????????
                insertInvoiceVoucher(file, invoiceVouchers, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,1);
              } else {
                checkFlag = false;
                respstat = RespCode.INVOICENUM_NOT_ENOUGH;
              }
            } else {
              //??????????????????
              insertInvoiceVoucher(file, invoiceVouchers, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,0);
            }
            if (checkFlag){//??????????????????????????? ??????
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
          } else {//??????????????????
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
              //?????????????????????
              QbInvoiceReserve invoiceReserve = qbInvoiceReserveService.getReserveByParams(invoiceRecordOld);
              if (invoiceReserve != null) {
                int alreadyCount = qbInvoiceVoucherService.getAlreadyInvoiceCount(invoiceReserve);
                if (invoiceReserve.getInvoiceTotalNum() > 0
                        && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) > 0
                        && (invoiceReserve.getInvoiceTotalNum() - alreadyCount) >= invoiceVouchers.size()) {
                  //??????????????????
                  insertInvoiceVoucher(file, invoiceVouchers, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,1);
                  //??????????????????
                  Map<String, Object> updateParams = new HashMap<>();
                  updateParams.put("invoiceSerialNo", leftInvoiceRecord.getOrderNo());
                  updateParams.put("invoiceStatus2", InvoiceOrderStatus2.FINISH_TYPE.getCode());
                  userCommission2Dao.updateCommissionInvoiceInfoByInvoiceSerialNo(updateParams);
                } else {
                  checkFlag = false;
                  respstat = RespCode.INVOICENUM_NOT_ENOUGH;
                }
              } else {
                //??????????????????
                insertInvoiceVoucher(file, invoiceVouchers, uploadPath, customLogin, invoiceRecordOld, invoiceReserve,0);
                //??????????????????
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
          //??????UUID???????????????
          String name = UUID.randomUUID().toString().replaceAll("-", "");
          //?????????????????????
          String ext = FilenameUtils.getExtension(mf.getOriginalFilename());
          //????????????????????????
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
   * ????????????????????????
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
            //1.??????????????????2.???????????????????????????
            String[] orderArray = invoiceRecordOld.getOrderNo().split(",");
            if (orderArray.length > 1) {
              //??????????????????
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
              //??????-?????????????????????????????????
              Integer invoiceMethod = invoiceRecordOld.getInvoiceMethod();
              if (1 == invoiceMethod || 2 == invoiceMethod){//??????????????????
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
              }else{ //??????????????????
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
                if (status == 4) {//??????????????????????????????????????????????????????????????????
                  Map<String, Object> updateParams = new HashMap<>();
                  updateParams.put("invoiceSerialNo", leftInvoiceRecord.getOrderNo());
                  updateParams.put("invoiceStatus2", InvoiceOrderStatus2.FINISH_TYPE.getCode());
                  userCommission2Dao.updateCommissionInvoiceInfoByInvoiceSerialNo(updateParams);
                } else if (status == 3) {//???????????????????????????
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
   * ????????????????????????
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
    //root ?????? ??? ????????????
    if (customLogin.getCustomType() == 4 &&  !StringUtil.isEmpty(customLogin.getMasterCustom())){//????????????
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
   * ??????????????????
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
      //???????????????????????????????????????
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
   * ????????????
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
    fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// ??????????????????
    headers.add("Content-Disposition", "attachment;filename=" + fileName);
    headers.setContentDispositionFormData("attachment", fileName);
    return new ResponseEntity<byte[]>(bytes,
        headers, HttpStatus.OK);
  }

  /**
   * ????????????
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
      //????????????????????????????????????
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
        //????????????????????????
        qbInvoicePicService.deleteByPrimaryKey(invoiceRecord.getId());
        invoiceRecordOld.setInvoiceNum(invoiceRecordOld.getInvoiceNum() - 1);
        //??????????????????????????????????????????
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
   * ??????????????????????????????
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
   * @Description ??????????????????????????????????????????
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
        //??????????????????????????????????????????
        userCommissionListRate = personalIncomeTaxRateService.selectByListInvoiceSerialNo(listSer);
        //????????????????????????????????????????????????
        userCommissionListSum = personalIncomeTaxRateService
            .selectSumByListInvoiceSerialNo(listSer);
        //????????????????????????????????????????????????
        userCommissionListBack = personalIncomeTaxRateService
            .findIndividualBackTax(listSer);
        //???????????????????????????????????????
        allCommission = integrationData(userCommissionListRate, userCommissionListSum,userCommissionListBack);
      }
    }

    return allCommission;
  }


  /**
   * @return java.util.List<java.lang.Object>
   * @Author YJY
   * @Description ??????????????? ?????? ??????
   * @Date 2020/7/20
   * @Param [listUserCommission]
   **/
  public List<UserCommission> integrationData(List<UserCommission> listUserCommissionRate,
      List<UserCommission> listUserCommissionSum,List<UserCommission> listBackRate) {

    //sum??????
    for (UserCommission sum : listUserCommissionSum) {
      sum.setIndividualBackTax("0");
      //??????
      for (UserCommission rate : listUserCommissionRate) {

        if (StringUtils.isNotBlank(rate.getCalculationRates())
            && sum.getAccountDate().equals(rate.getAccountDate())
            && (!sum.getCalculationRates().contains(rate.getCalculationRates()))
        ) {


            sum.setCalculationRates(sum.getCalculationRates() +"%-"+ rate.getCalculationRates());


        }
        //???????????? ????????? ???????????? ?????????
        if (StringUtils.isNotBlank(rate.getTaxRate())
            && sum.getAccountDate().equals(rate.getAccountDate())
            && (!sum.getTaxRate().contains(rate.getTaxRate()))
        ) {

          sum.setTaxRate(sum.getTaxRate() + "%-"+ rate.getTaxRate());

        }
      }

      if(CollectionUtils.isNotEmpty(listBackRate)) {
        //????????????
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
