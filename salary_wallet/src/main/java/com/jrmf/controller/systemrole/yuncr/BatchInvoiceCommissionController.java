package com.jrmf.controller.systemrole.yuncr;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.CertType;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.InvoiceStatusEnum;
import com.jrmf.controller.constant.ReceiptStatusEnum;
import com.jrmf.domain.BatchInvoiceCommission;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Company;
import com.jrmf.domain.dto.BatchInvoiceCommissionDTO;
import com.jrmf.service.BatchInvoiceCommissionService;
import com.jrmf.service.ReceiptService;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/apply/batch/invoice")
public class BatchInvoiceCommissionController extends BaseController {

  @Autowired
  private BatchInvoiceCommissionService batchInvoiceCommissionService;

  @Autowired
  private ReceiptService receiptService;

  @Autowired
  private BestSignConfig bestSignConfig;

  @Value("${companyId}")
  private String companyId;

  @RequestMapping(value = "/query")
  public Map<String, Object> queryTransactionDetail(
      BatchInvoiceCommissionDTO batchInvoiceCommissionDTO,
      @RequestParam(required = false) Integer pageNo,
      @RequestParam(required = false) Integer pageSize,
      HttpServletRequest request) {
    ChannelCustom customLogin = getCustomLogin(request);

    if (!CommonString.ROOT.equals(customLogin.getCustomkey()) && customLogin.getCustomType() != CustomType.COMPANY.getCode()) {
      return returnFail(RespCode.error101, "权限不足");
    }

    PageHelper.startPage(pageNo, pageSize);
    List<BatchInvoiceCommission> list = batchInvoiceCommissionService.listBatchInvoiceCommission(
        batchInvoiceCommissionDTO);
    PageInfo page = new PageInfo(list);
    Map<String, Object> result = new HashMap<>(4);
    result.put("list", page.getList());
    result.put("total", page.getTotal());
    return returnSuccess(result);
  }

  private ChannelCustom getCustomLogin(HttpServletRequest request) {
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if (CustomType.ROOT.getCode() == customLogin.getCustomType() && !StringUtil
        .isEmpty(customLogin.getMasterCustom())) {
      customLogin = customService
          .getCustomByCustomkey(customLogin.getMasterCustom());
    }
    return customLogin;
  }

  @GetMapping(value = "/export")
  public void exportTransactionDetail(HttpServletResponse response, BatchInvoiceCommissionDTO batchInvoiceCommissionDTO) {
    List<BatchInvoiceCommission> list = batchInvoiceCommissionService.listBatchInvoiceCommission(
        batchInvoiceCommissionDTO);
    String[] colunmName = new String[]{"商户名称", "回单状态", "开票状态", "个体户姓名", "到账时间", "交易金额",
        "服务费", "收款账号", "账号所属机构", "发包商（公司名称）", "实发服务公司", "证件类型", "证件号", "订单备注",
        "查看回单"};
    String filename = "下发交易申请批次开票表";
    List<Map<String, Object>> data = new ArrayList<>();
    for (BatchInvoiceCommission batchInvoiceCommission : list) {
      Map<String, Object> dataMap = new HashMap<>();
      dataMap.put("1", batchInvoiceCommission.getCompanyName());
      dataMap.put("2", ReceiptStatusEnum.codeOf(batchInvoiceCommission.getReceiptStatus()));
      dataMap.put("3", InvoiceStatusEnum.codeOf(batchInvoiceCommission.getInvoiceStatus()));
      dataMap.put("4", batchInvoiceCommission.getIndividualName());
      dataMap.put("5", batchInvoiceCommission.getAccountTime());
      dataMap.put("6", batchInvoiceCommission.getAmount());
      dataMap.put("7", batchInvoiceCommission.getFee());
      dataMap.put("8", batchInvoiceCommission.getInAccountNo());
      dataMap.put("9", batchInvoiceCommission.getInAccountName());
      dataMap.put("10", batchInvoiceCommission.getContractCompanyName());
      dataMap.put("11", batchInvoiceCommission.getServiceCompanyName());
      dataMap.put("12", CertType.codeOfTwo(batchInvoiceCommission.getDocumentType()));
      dataMap.put("13", batchInvoiceCommission.getCertId());
      dataMap.put("14", batchInvoiceCommission.getRemark());
      dataMap.put("15", bestSignConfig.getServerNameUrl() + "/receipt" + batchInvoiceCommission.getReceiptUrl());
      data.add(sortMapByKey(dataMap));
    }
    ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
  }

  @RequestMapping(value = "/replenishReceipt")
  public Map<String, Object> replenishReceipt(@RequestParam String orderNo) {
    String receiptUrl = receiptService.getReceiptCommissionByOrderNo(orderNo);
    if (!StringUtil.isEmpty(receiptUrl)) {
      BatchInvoiceCommission batchInvoiceCommission = new BatchInvoiceCommission();
      batchInvoiceCommission.setOrderNo(orderNo);
      batchInvoiceCommission.setReceiptStatus(ReceiptStatusEnum.EXIST_RECEIPT.getCode());
      batchInvoiceCommission.setReceiptUrl(receiptUrl);
      batchInvoiceCommissionService.updateByOrderNo(batchInvoiceCommission);
    }
    return returnSuccess();
  }

  @RequestMapping(value = "/getCompanyInfo")
  private Map<String, Object> getCompanyInfo() {
    List<Company> companys = companyService.getCompanyByUserIds(companyId);
    return returnSuccess(companys);
  }

}
