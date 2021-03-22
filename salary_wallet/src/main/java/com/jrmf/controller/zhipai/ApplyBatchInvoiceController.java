package com.jrmf.controller.zhipai;

import static com.jrmf.common.Constant.INVOICE_RATE;

import com.github.pagehelper.PageInfo;
import com.jrmf.common.APIResponse;
import com.jrmf.common.BatchInvoiceStatus;
import com.jrmf.common.CommonString;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ApplyBatchInvoice;
import com.jrmf.domain.ApplyBatchInvoiceAmount;
import com.jrmf.domain.BatchInvoiceCommission;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.dto.BatchInvoiceCommissionDTO;
import com.jrmf.domain.dto.InvoiceCommissionDTO;
import com.jrmf.domain.dto.StatisticalBatchInvoiceDTO;
import com.jrmf.service.ApplyBatchInvoiceService;
import com.jrmf.service.BatchInvoiceAssociationService;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.ThreadPoolUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: YJY
 * @date: 2021/1/5 16:24
 * @description:
 */
@Api(value = "申请批次开票页面", tags = {"申请批次开票页面-弹框相关接口"})
@RestController
@RequestMapping("zhipai/")
public class ApplyBatchInvoiceController extends BaseController {

  @Autowired
  ApplyBatchInvoiceService applyBatchInvoiceService;

  @Autowired
  BatchInvoiceAssociationService batchInvoiceAssociationService;

  @ApiOperation("申请批次开票弹框")
  @PostMapping("apply/batch/invoice")
  public APIResponse<ApplyBatchInvoice> findByCondition(HttpServletRequest request,
      InvoiceCommissionDTO invoiceCommissionDTO) {
    HashMap hashMap = new HashMap();
    ChannelCustom customLogin = (ChannelCustom) request.getSession()
        .getAttribute(CommonString.CUSTOMLOGIN);
    if (!isMFKJAccount(customLogin) && !isCompany(customLogin)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_529);
    }

    //查询包含不符合开票状态的数据直接返回
    if (null != invoiceCommissionDTO.getReceiptStatus()
        && invoiceCommissionDTO.getReceiptStatus() == 0) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_560);
    }

    StatisticalBatchInvoiceDTO dto = applyBatchInvoiceService
        .findStatisticalByCondition(invoiceCommissionDTO);
    //判断批次包含的发包商
    if (ObjectUtils.isEmpty(dto) || ObjectUtils.isEmpty(dto.getContractCompanyName())) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_557);
    }
    if (dto.getContractCompanyNameCount() > 1) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_562);
    }
    //判断此发包商是否存在
    int count = applyBatchInvoiceService.checkCustom(dto.getContractCompanyName());
    if (count > 1) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_558);
    }
    if (count == 0) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_563);
    }
    //项目
    List<HashMap> task = applyBatchInvoiceService.findTaskList(dto.getContractCompanyName());
    if (CollectionUtils.isEmpty(task)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_561);
    }
    //下发笔数
    int tradeNumber = applyBatchInvoiceService.findCountByCondition(invoiceCommissionDTO);
    dto.setTradeNumber(tradeNumber);
    hashMap.put("total", tradeNumber);
    hashMap.put("statistical", dto);
    hashMap.put("taskList", task);

    return APIResponse.successResponse(hashMap);
  }

  @ApiOperation("申请批次开票弹框-列表分页")
  @PostMapping("apply/batch/invoice/list")
  public APIResponse findListByCondition(HttpServletRequest request,
      InvoiceCommissionDTO invoiceCommissionDTO) {
    ChannelCustom customLogin = (ChannelCustom) request.getSession()
        .getAttribute(CommonString.CUSTOMLOGIN);
    if (!isMFKJAccount(customLogin) && !isCompany(customLogin)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_529);
    }
    return APIResponse
        .successResponse(applyBatchInvoiceService.findByCondition(invoiceCommissionDTO));
  }

  @ApiOperation("申请批次开票弹框-提交")
  @PostMapping("apply/batch/invoice/submit")
  public APIResponse submit(HttpServletRequest request, InvoiceCommissionDTO invoiceCommissionDTO) {

    try {
    boolean insert = applyBatchInvoiceService.batchInsert(invoiceCommissionDTO);

    if (insert) {
      //推送云控
      ThreadPoolUtils.getThread().execute(() -> {
        batchInvoiceAssociationService.batchPush(null);
      });
      return APIResponse.successResponse();
    }
    return APIResponse.errorResponse(ResponseCodeMapping.ERR_564);
    }catch (RuntimeException e){
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_500.getCode(),e.getMessage());
    }
  }

  @ApiOperation("申请批次开票弹框-获取项目详情")
  @GetMapping("apply/batch/task/detail")
  public APIResponse detail(int id) {

    return APIResponse.successResponse(applyBatchInvoiceService.findTaskDetail(id));

  }

  @ApiOperation("申请批次开票弹框-导出")
  @GetMapping("apply/batch/invoice/export")
  public void export(HttpServletRequest request, HttpServletResponse response,
      InvoiceCommissionDTO invoiceCommissionDTO) {

    List<ApplyBatchInvoice> list = applyBatchInvoiceService
        .findALLByCondition(invoiceCommissionDTO);
    if (CollectionUtils.isEmpty(list)) {
      return;
    }
    String filename = "申请批次按月统计表";
    String[] columnName = new String[]{"交易月份", "个体户名称", "交易金额", "结算金额", "交易笔数", "证件号", "银行卡"};
    List<Map<String, Object>> data = new ArrayList<>();

    for (ApplyBatchInvoice batchInvoice : list) {
      Map<String, Object> dataMap = new HashMap<>(7);
      dataMap.put("1", batchInvoice.getTradeMonth());
      dataMap.put("2", batchInvoice.getIndividualName());
      dataMap.put("3", batchInvoice.getTradeMoney());
      dataMap.put("4", batchInvoice.getInvoiceMoney());
      dataMap.put("5", batchInvoice.getTradeNumber());
      dataMap.put("6", batchInvoice.getIdCard());
      dataMap.put("7", batchInvoice.getInAccountNo());
      data.add(dataMap);
    }
    ExcelFileGenerator.ExcelExport(response, columnName, filename, data);
  }


  @ApiOperation("申请批次开票弹框-全选检查数据")
  @PostMapping("apply/batch/check/count")
  public APIResponse checkCount(HttpServletRequest request,
      InvoiceCommissionDTO invoiceCommissionDTO) {
    ChannelCustom customLogin = (ChannelCustom) request.getSession()
        .getAttribute(CommonString.CUSTOMLOGIN);
    if (!isMFKJAccount(customLogin) && !isCompany(customLogin)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_529);
    }
    //查询包含不符合开票状态的数据直接返回
    if (null != invoiceCommissionDTO.getReceiptStatus()
        && invoiceCommissionDTO.getReceiptStatus() == 0) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_560);
    }
    return APIResponse
        .successResponse(applyBatchInvoiceService.findCountByCondition(invoiceCommissionDTO));

  }


  public static void main(String[] args) {

    String invoiceStatus = "1,2";

    if (invoiceStatus.contains(BatchInvoiceStatus.IN_INVOICE.getNode())
        || invoiceStatus.contains(BatchInvoiceStatus.SUCCESS_INVOICE.getNode())) {

      invoiceStatus = invoiceStatus.replaceAll(
          BatchInvoiceStatus.IN_INVOICE.getNode() + "|" + BatchInvoiceStatus.SUCCESS_INVOICE
              .getNode(), "");
      invoiceStatus = invoiceStatus.replaceAll(",,", "");
      if (invoiceStatus.startsWith(",")) {
        invoiceStatus = invoiceStatus.substring(1, invoiceStatus.length());
      }
      if (invoiceStatus.endsWith(",")) {
        invoiceStatus = invoiceStatus.substring(0, invoiceStatus.length() - 1);
      }

    }
    if (!StringUtils.isEmpty(invoiceStatus)) {
      List list = Arrays.asList(invoiceStatus.split(","));
      System.out.println(list.size());
    }
    System.out.println(invoiceStatus);

  }

}
