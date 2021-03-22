package com.jrmf.controller.merchant;

import com.github.pagehelper.PageInfo;
import com.jrmf.common.APIResponse;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.TradeType;
import com.jrmf.domain.ForwardCompanyAccountHistory;
import com.jrmf.domain.dto.ForwardCompanyAccountHistoryRequestDTO;
import com.jrmf.service.ForwardCompanyAccountHistoryService;
import com.jrmf.utils.ExcelFileGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: YJY
 * @date: 2020/11/20 13:45
 * @description: 转包商记账户交易记录
 */
@Api(value = "转包记账户记录查询", tags = {"转包记账户记录查询"})
@RestController
@RequestMapping("forward/company/account/history")
public class CompanyAccountHistoryController extends BaseController {


  @Autowired
  ForwardCompanyAccountHistoryService forwardCompanyAccountHistoryService;

  /**
   * @Description 列表搜索
   **/
  @ApiOperation("转包记账户记录查询 - 列表搜索")
  @PostMapping("search")
  public APIResponse<ForwardCompanyAccountHistory> search(ForwardCompanyAccountHistoryRequestDTO txt,
      HttpServletRequest request) {
    ForwardCompanyAccountHistoryRequestDTO requestDTO= getSearchData(request);
    Integer companyId = returnSubcontractCompanyId(request);
    if (!isRootAdmin(request) && ObjectUtils.isEmpty(companyId)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_529);
    }
    if (!ObjectUtils.isEmpty(companyId)) {
      requestDTO.setCompanyId(companyId + "");
    }
      return APIResponse.successResponse(forwardCompanyAccountHistoryService.findByCondition(requestDTO));


  }
  @ApiOperation("转包记账户记录查询 - 新增历史记录")
  @PostMapping("insert")
  public APIResponse insert(@RequestBody ForwardCompanyAccountHistory requestDTO) {

    return APIResponse.successResponse(forwardCompanyAccountHistoryService.insert(requestDTO));
  }

  @ApiOperation("转包记账户记录查询 - 导出")
  @GetMapping("export")
  public void export(HttpServletRequest request, HttpServletResponse response) {

    try {

    String[] headers = new String[] {"商户名称", "转包服务公司","完税服务公司","交易类型","交易金额","交易笔数","交易发生后余额",
        "备注","交易时间"};
    String filename = "转包记账户记录";
    ForwardCompanyAccountHistoryRequestDTO requestDTO= getSearchData(request);
    requestDTO.setPageNo(1);
    requestDTO.setPageSize(5000);
    Integer companyId = returnSubcontractCompanyId(request);
    if (!isRootAdmin(request) && ObjectUtils.isEmpty(companyId)) {
      return ;
    }
    if (!ObjectUtils.isEmpty(companyId)) {
      requestDTO.setCompanyId(companyId + "");
    }
    PageInfo<ForwardCompanyAccountHistory> pageInfo = forwardCompanyAccountHistoryService.findByCondition(requestDTO);
    if(ObjectUtils.isEmpty(pageInfo) || CollectionUtils.isEmpty(pageInfo.getList())){
      return ;
    }
    List<Map<String, Object>> data = new ArrayList<>();
    List<ForwardCompanyAccountHistory> list = pageInfo.getList();
    for(ForwardCompanyAccountHistory history:list){
      Map<String, Object> dataMap = new HashMap<>(9);
      dataMap.put("1",history.getMerchantName());
      dataMap.put("2",history.getCompanyName());
      dataMap.put("3",history.getRealCompanyName());
      try {
        dataMap.put("4", TradeType.codeOf(history.getTradeType()).getDesc());
      }catch (Exception e){
        dataMap.put("4", "未知");
      }

      dataMap.put("5",history.getTradeMoney());
      dataMap.put("6",history.getAmount());
      dataMap.put("7",history.getAfterTradeMoney());
      dataMap.put("8",history.getRemark());
      dataMap.put("9",history.getCreateTIme());
      data.add(dataMap);
    }
    ExcelFileGenerator.ExcelExport(response, headers, filename, data);
    }catch (Exception e){

    }
  }



  public ForwardCompanyAccountHistoryRequestDTO getSearchData(HttpServletRequest request){
    ForwardCompanyAccountHistoryRequestDTO data = new ForwardCompanyAccountHistoryRequestDTO();
    data.setStartDate( request.getParameter("startDate") );
    data.setEndDate( request.getParameter("endDate") );
    data.setCompanyId( request.getParameter("companyId"));
    data.setRealCompanyId( request.getParameter("realCompanyId"));
    data.setCustomKey( request.getParameter("customKey"));
    data.setMerchantName(request.getParameter("merchantName"));

    try {
      data.setAccountId( Integer.parseInt(request.getParameter("accountId")));
    }catch (Exception e){}

    try {
      data.setTradeType( Integer.parseInt(request.getParameter("tradeType")));
    }catch (Exception e){}
    try {
    data.setMinBalance( Integer.parseInt(request.getParameter("minBalance")));
    }catch (Exception e){}
    try {
      data.setMaxBalance( Integer.parseInt(request.getParameter("maxBalance")));
    }catch (Exception e){}
    try {
      data.setPageSize(Integer.parseInt(request.getParameter("pageSize")));
    }catch (Exception e){}

    try {
      data.setPageNo(Integer.parseInt(request.getParameter("pageNo")));
    }catch (Exception e){}





    return  data;

  }
}
