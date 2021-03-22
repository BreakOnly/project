package com.jrmf.controller.merchant;

import com.github.pagehelper.PageInfo;
import com.jrmf.common.APIResponse;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ForwardCompanyAccount;
import com.jrmf.domain.dto.ForwardCompanyAccountRequestDTO;
import com.jrmf.domain.dto.ForwardCompanyAccountUpdateRequestDTO;
import com.jrmf.domain.vo.CompanyAccountVo;
import com.jrmf.service.CustomBalanceAndAccountService;
import com.jrmf.service.ForwardCompanyAccountService;
import com.jrmf.utils.CipherUtil;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: YJY
 * @date: 2020/11/20 15:21
 * @description: 转包商户记账户余额管理  权限：超管、转包服务公司
 */
@Api(value = "转包服务公司余额查看-转包商户记账户余额管理", tags = {"转包服务公司余额查看-转包商户记账户余额管理"})
@Slf4j
@RestController
@RequestMapping("forward/company/account/")
public class CompanyAccountController extends BaseController {

  @Autowired
  ForwardCompanyAccountService forwardCompanyAccountService;

  @Autowired
  CustomBalanceAndAccountService customBalanceAndAccountService;

  /**
   * @return com.jrmf.common.APIResponse
   * @Author YJY
   * @Description 列表搜索  txt:文档注释
   * @Date 2020/11/23
   * @Param [requestDTO]
   **/
  @ApiOperation("转包商户记账户余额管理 - 列表搜索")
  @PostMapping("find/by/condition")
  public APIResponse<ForwardCompanyAccount> search(ForwardCompanyAccountRequestDTO txt,HttpServletRequest request) {

    ForwardCompanyAccountRequestDTO requestDTO =  getSearchDate(request);
    Integer companyId = returnSubcontractCompanyId(request);
    if (!isRootAdmin(request) && ObjectUtils.isEmpty(companyId)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_529);
    }
    if (!ObjectUtils.isEmpty(companyId)) {
      requestDTO.setCompanyId(companyId + "");
    }
    return APIResponse.successResponse(forwardCompanyAccountService.findByCondition(requestDTO));
  }

  /**
   * @return com.jrmf.common.APIResponse
   * @Author YJY
   * @Description 列表搜索 txt 文档注释
   * @Date 2020/11/23
   * @Param [requestDTO]
   **/
  @ApiOperation("转包服务公司余额查询 - 列表搜索")
  @PostMapping("find/by/company")
  public APIResponse<ForwardCompanyAccount> searchByCompany(ForwardCompanyAccountRequestDTO txt,
      HttpServletRequest request) {
    ForwardCompanyAccountRequestDTO requestDTO =  getSearchDate(request);
    Integer companyId = returnSubcontractCompanyId(request);
    if (!isRootAdmin(request) && ObjectUtils.isEmpty(companyId)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_529);
    }
    if (!ObjectUtils.isEmpty(companyId)) {
      requestDTO.setCompanyId(companyId + "");
    }
    return APIResponse.successResponse(forwardCompanyAccountService.findCompanyList(requestDTO));
  }
  @ApiOperation("转包商户记账户余额管理 - 新增商户转包完税服务公司记账户")
  @PostMapping("insert")
  public APIResponse insert(ForwardCompanyAccount txt,
      HttpServletRequest request) {

    ForwardCompanyAccount requestDTO = getInsertDate(request);
    try {
      Integer companyId = returnSubcontractCompanyId(request);
      if (!isRootAdmin(request) && ObjectUtils.isEmpty(companyId)) {
        return APIResponse.errorResponse(ResponseCodeMapping.ERR_529);
      }

      if (StringUtils.isEmpty(requestDTO) || StringUtils.isEmpty(requestDTO.getCustomKey())
          || StringUtils.isEmpty(requestDTO.getCompanyId())
          || StringUtils.isEmpty(requestDTO.getRealCompanyId())) {

        return APIResponse.errorResponse(ResponseCodeMapping.ERR_5009);
      }

      return forwardCompanyAccountService.insert(requestDTO);
    } catch (Exception e) {
      log.error("新增记账户异常" + e);
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_500);
    }

  }


  /**
   * @return com.jrmf.common.APIResponse
   * @Author YJY
   * @Description 调整余额 切换状态
   * @Date 2020/11/23
   * @Param [requestDTO]
   **/
  @ApiOperation("转包商户记账户余额管理 - 商户转包记账户余额调整")
  @PostMapping("update/by/id")
  public APIResponse updateById(ForwardCompanyAccountUpdateRequestDTO txt,
      HttpServletRequest request) {
    ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
    ForwardCompanyAccountUpdateRequestDTO requestDTO = getUpdate(request);
    Integer companyId = returnSubcontractCompanyId(request);
    if (!isRootAdmin(request) && ObjectUtils.isEmpty(companyId)) {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_529);
    }
    if (!ObjectUtils.isEmpty(requestDTO.getBalance()) && requestDTO.getBalance() != 0) {


      if (StringUtils.isEmpty(requestDTO.getPassword())) {
        return APIResponse.errorResponse(RespCode.error101, RespCode.PASSWORD_ERROR);
      }

      /**
       * 验证交易密码
       */
      if (!loginUser.getTranPassword().equals(CipherUtil.generatePassword(requestDTO.getPassword(), StringUtil.isEmpty(loginUser.getCustomkey()) ? loginUser.getMasterCustom() : loginUser.getCustomkey()))) {
        return APIResponse.errorResponse(RespCode.error101, RespCode.PASSWORD_ERROR);
      }
    }
    requestDTO.setOperator(loginUser.getUsername());
    return forwardCompanyAccountService.updateById(requestDTO);
  }



  @ApiOperation("转包商户记账户余额管理 - 导出")
  @GetMapping("custom/export")
  public void export(HttpServletRequest request, HttpServletResponse response) {

    try {

      String[] headers = new String[] {"序号","商户名称", "转包服务公司","完税服务公司","下挂记账户余额","状态","创建时间","更新时间"};
      String filename = "转包商户记账户余额";
      ForwardCompanyAccountRequestDTO requestDTO= getSearchDate(request);
      requestDTO.setPageNo(1);
      requestDTO.setPageSize(5000);
      Integer companyId = returnSubcontractCompanyId(request);
      if (!isRootAdmin(request) && ObjectUtils.isEmpty(companyId)) {
        return ;
      }
      if (!ObjectUtils.isEmpty(companyId)) {
        requestDTO.setCompanyId(companyId + "");
      }
      PageInfo<ForwardCompanyAccount> pageInfo = forwardCompanyAccountService.findByCondition(requestDTO);
      if(ObjectUtils.isEmpty(pageInfo) || CollectionUtils.isEmpty(pageInfo.getList())){
        return ;
      }
      List<Map<String, Object>> data = new ArrayList<>();
      List<ForwardCompanyAccount> list = pageInfo.getList();
      for(ForwardCompanyAccount account:list){
        Map<String, Object> dataMap = new HashMap<>(9);
        dataMap.put("1",account.getId());
        dataMap.put("2",account.getMerchantName());
        dataMap.put("3",account.getCompanyName());
        dataMap.put("4",account.getRealCompanyName());
        dataMap.put("5",account.getBalanceTwo());
        dataMap.put("6",account.getStatus()==1?"正常":"失效");
        dataMap.put("7",account.getCreateTime());
        dataMap.put("8",account.getUpdateTime());
        data.add(dataMap);
      }
      ExcelFileGenerator.ExcelExport(response, headers, filename, data);
    }catch (Exception e){

    }
  }



  @ApiOperation("转包服务公司余额 - 导出")
  @GetMapping("company/export")
  public void companyExport(HttpServletRequest request, HttpServletResponse response) {

    try {

      String[] headers = new String[] {"序号","转包服务公司","实际服务公司","下挂商户记账户总余额","下挂商户记账户数","转包服务公司Key","实际服务公司Key"};
      String filename = "转包服务公司余额";
      ForwardCompanyAccountRequestDTO requestDTO= getSearchDate(request);
      requestDTO.setPageNo(1);
      requestDTO.setPageSize(5000);
      Integer companyId = returnSubcontractCompanyId(request);
      if (!isRootAdmin(request) && ObjectUtils.isEmpty(companyId)) {
        return ;
      }
      if (!ObjectUtils.isEmpty(companyId)) {
        requestDTO.setCompanyId(companyId + "");
      }
      PageInfo<ForwardCompanyAccount> pageInfo = forwardCompanyAccountService.findCompanyList(requestDTO);
      if(ObjectUtils.isEmpty(pageInfo) || CollectionUtils.isEmpty(pageInfo.getList())){
        return ;
      }
      List<Map<String, Object>> data = new ArrayList<>();
      List<ForwardCompanyAccount> list = pageInfo.getList();
      for(ForwardCompanyAccount account:list){
        Map<String, Object> dataMap = new HashMap<>(7);
        dataMap.put("1",account.getId());
        dataMap.put("2",account.getCompanyName());
        dataMap.put("3",account.getRealCompanyName());
        dataMap.put("4",account.getBalance());
        dataMap.put("5",account.getCustomCount());
        dataMap.put("6",account.getCompanyId());
        dataMap.put("7",account.getRealCompanyId());
        data.add(dataMap);
      }
      ExcelFileGenerator.ExcelExport(response, headers, filename, data);
    }catch (Exception e){

    }
  }


  /**
  * @Description 解析前端传来的参数
  **/

  public ForwardCompanyAccountRequestDTO getSearchDate(HttpServletRequest request){

    ForwardCompanyAccountRequestDTO requestDTO = new ForwardCompanyAccountRequestDTO();
    requestDTO.setStartDate(request.getParameter("startDate"));
    requestDTO.setCompanyId(request.getParameter("companyId"));
    requestDTO.setRealCompanyId(request.getParameter("realCompanyId"));
    requestDTO.setCustomKey(request.getParameter("customKey"));
    requestDTO.setMerchantName(request.getParameter("merchantName"));
    requestDTO.setMerchantName(request.getParameter("merchantName"));
    try {
      requestDTO.setOrder(Integer.parseInt(request.getParameter("order")));
    }catch (Exception e){}
    try {
      requestDTO.setId(Integer.parseInt(request.getParameter("id")));
    }catch (Exception e){}
    try {
      requestDTO.setMaxBalance(Double.parseDouble(request.getParameter("maxBalance"))*100);
    }catch (Exception e){}
    try {
      requestDTO.setMinBalance(Double.parseDouble(request.getParameter("minBalance"))*100);
    }catch (Exception e){}
    try {
      requestDTO.setPageSize(Integer.parseInt(request.getParameter("pageSize")));
    }catch (Exception e){}
    try {
      requestDTO.setStatus(Integer.parseInt(request.getParameter("status")));
    }catch (Exception e){}
    try {
      requestDTO.setPageNo(Integer.parseInt(request.getParameter("pageNo")));
    }catch (Exception e){}


    return requestDTO;
  }


  public ForwardCompanyAccount  getInsertDate (HttpServletRequest request){

    ForwardCompanyAccount  data = new ForwardCompanyAccount();
    data.setCustomKey(request.getParameter("customKey"));
    data.setCompanyId(request.getParameter("companyId"));
    data.setRealCompanyId(request.getParameter("realCompanyId"));
    return  data;
  }

  public ForwardCompanyAccountUpdateRequestDTO getUpdate(HttpServletRequest request){
    ForwardCompanyAccountUpdateRequestDTO data = new ForwardCompanyAccountUpdateRequestDTO();

    try {
      data.setType(Integer.parseInt(request.getParameter("type")));
    }catch (Exception e){}

    try {
      data.setId(Integer.parseInt(request.getParameter("id")));
    }catch (Exception e){}
    try {
      data.setBalance(Double.parseDouble(request.getParameter("balance")));
    }catch (Exception e){}
    try {
      data.setStatus(Integer.parseInt(request.getParameter("status")));
    }catch (Exception e){}

    data.setPassword(request.getParameter("password"));
    data.setRemark(request.getParameter("remark"));
    return  data;

  }


  public CompanyAccountVo getTest(HttpServletRequest request){
    CompanyAccountVo data = new CompanyAccountVo();

    data.setOperator(request.getParameter("operator"));
    data.setRelateOrderNo(request.getParameter("relateOrderNo"));
    data.setCustomKey(request.getParameter("customKey"));
    data.setCompanyId(request.getParameter("companyId"));
    data.setRealCompanyId(request.getParameter("realCompanyId"));
    try {
      data.setId(Integer.parseInt(request.getParameter("type")));
    }catch (Exception e){}
    try {
    data.setOperating(Integer.parseInt(request.getParameter("operating")));
    }catch (Exception e){}
    try {
      data.setTradeType(Integer.parseInt(request.getParameter("tradeType")));
    }catch (Exception e){}
    try {
      data.setBalance(request.getParameter("balance"));
    }catch (Exception e){}


    return  data;

  }
}
