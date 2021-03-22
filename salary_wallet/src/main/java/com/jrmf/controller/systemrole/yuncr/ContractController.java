package com.jrmf.controller.systemrole.yuncr;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.AccountTransStatus;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.CustomerFirmStatusEnum;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Company;
import com.jrmf.domain.Contract;
import com.jrmf.service.CompanyService;
import com.jrmf.service.ContractService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/contract")
public class ContractController extends BaseController {

  @Autowired
  private ContractService contractService;
  @Autowired
  private OrganizationTreeService organizationTreeService;
  @Autowired
  private CompanyService companyService;
  @Autowired
  private BestSignConfig bestSignConfig;

  @PostMapping(value = "/list")
  public Map<String, Object> list(HttpServletRequest request,
      @RequestParam(required = false) String customName,
      @RequestParam(required = false) String userName,
      @RequestParam(required = false) String idCard,
      @RequestParam(required = false) String projectName,
      @RequestParam(required = false) String companyName,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime,
      @RequestParam(required = false) Integer pageNo,
      @RequestParam(required = false) Integer pageSize) {

//    Map<String, Object> map = getCustomerAuthority(request);
//    if (map != null) {
//      return map;
//    }
    String customKey = getCustomKey(request);
    if (StringUtil.isEmpty(customKey)) {
      return returnFail(RespCode.error101, "系统异常");
    }

    Map<String, Object> paramMap = new HashMap<>();
    if (!"mfkj".equals(customKey)) {
      paramMap.put("customKey", customKey);
    }
    paramMap.put("customName", customName);
    paramMap.put("userName", userName);
    paramMap.put("idCard", idCard);
    paramMap.put("projectName", projectName);
    paramMap.put("companyName", companyName);
    paramMap.put("status", status);
    paramMap.put("startTime", startTime);
    paramMap.put("endTime", endTime);

    if (pageNo != null && pageSize != null) {
      PageHelper.startPage(pageNo, pageSize);
    }
    List<Contract> list = contractService.listContract(paramMap);
    PageInfo page = new PageInfo(list);

    Map<String, Object> result = new HashMap<>(4);
    result.put("list", page.getList());
    result.put("total", page.getTotal());
    return returnSuccess(result);
  }

  @PostMapping(value = "/config")
  public Map<String, Object> config(HttpServletRequest request, Contract contract) {

    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
//    if (!CommonString.ROOT.equals(customLogin.getCustomkey()) && !CommonString.ROOT
//        .equals(customLogin.getMasterCustom()) && customLogin.getCustomType() != CustomType.GROUP
//        .getCode() && customLogin.getMasterCustomType() != CustomType.GROUP.getCode()
//        && customLogin.getCustomType() != CustomType.CUSTOM.getCode()
//        && customLogin.getMasterCustomType() != CustomType.CUSTOM.getCode()) {
//      return returnFail(RespCode.error101, "权限不足！");
//    }

    if (contract.getContractFile() != null) {
      double fileSize = (double) contract.getContractFile().getSize() / 1048576;
      log.info("商户：{},文件大小:", contract.getCustomName(), fileSize);
      if (fileSize > 100) {
        return returnFail(RespCode.error101, "附件不得大于100M，请重新选择文件");
      }
    }

    Company company = companyService.getLikeCompanyByCompanyName("江西智派");
    Map<String, Object> result;
    try {
      contract.setCompanyId(company.getUserId());
      contract.setAddUser(customLogin.getUsername());
      result = contractService.configContract(contract);
    } catch (Exception e) {
      log.error("编辑合同失败:", e);
      return returnFail(RespCode.error101, "系统异常");
    }

    return result;
  }

  @PostMapping(value = "/uploadContract")
  public Map<String, Object> uploadContract(Contract contract) {
    String contractFileName = contract.getContractFile().getOriginalFilename();
    String fileType = contractFileName.substring(contractFileName.lastIndexOf(".") + 1);
    String contractName = UUID.randomUUID().toString().replace("-", "");
    String filePath = "/yuncr/contract/";
    String fileName =
        contractFileName.substring(0, contractFileName.indexOf(".")) + contractName + "."
            + fileType;

    if (!"pdf".equals(fileType)) {
      return returnFail(RespCode.error101, "文件格式错误，附件格式需为pdf");
    }

    double fileSize = (double) contract.getContractFile().getSize() / 1048576;
    log.info("商户：{},文件大小:", contract.getCustomName(), fileSize);
    if (fileSize > 100) {
      return returnFail(RespCode.error101, "附件不得大于100M，请重新选择文件");
    }

    try {
      byte[] bytes = contract.getContractFile().getBytes();
      InputStream fileInputStream = new ByteArrayInputStream(bytes);
      boolean backFile = FtpTool.uploadFile(filePath, fileName, fileInputStream);
      if (!backFile) {
        return returnFail(RespCode.error101, "上传合同失败");
      }
    } catch (Exception e) {
      log.error("上传合同失败");
      return returnFail(RespCode.error101, "系统异常");
    }
    contract.setContractUrl(filePath + fileName);
    contractService.updateContract(contract);
    return returnSuccess(contract.getContractUrl());
  }

  @PostMapping(value = "/remove")
  public Map<String, Object> remove(@RequestParam Integer id) {
    Contract contract = contractService.getContractById(id);
    if (contract != null && contract.getStatus() == CustomerFirmStatusEnum.FAIL.getCode()) {
      contractService.updateContractStatusIsDelete(id);
    }
    return returnSuccess();
  }

  @GetMapping(value = "/downloadContract")
  public ResponseEntity<byte[]> downloadContract(@RequestParam Integer id) throws IOException {

    Contract contract = contractService.getContractById(id);
    String filePath = contract.getContractUrl();
    String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

    byte[] bytes = FtpTool
        .downloadFtpFile(filePath.substring(0, filePath.lastIndexOf("/")), fileName);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    // 防止中文乱码
    fileName = new String(fileName.getBytes("gbk"), "iso8859-1");
    headers.add("Content-Disposition", "attachment;filename=" + fileName);
    headers.setContentDispositionFormData("attachment", fileName);
    return new ResponseEntity<byte[]>(bytes,
        headers, HttpStatus.OK);
  }

  @PostMapping(value = "/getProject")
  public Map<String, Object> getProject(@RequestParam String customKey) {
    List<Map<String, Object>> list = contractService.getProjectByCustomKey(customKey);
    return returnSuccess(list);
  }

  @PostMapping(value = "/getYuncrUser")
  public Map<String, Object> getYuncrUser(@RequestParam String customKey) {
    List<Map<String, Object>> list = contractService.getYuncrUser(customKey);
    return returnSuccess(list);
  }

  @PostMapping(value = "/getCustomerFirm")
  public Map<String, Object> getCustomerFirm(HttpServletRequest request) {
//    Map<String, Object> map = getCustomerAuthority(request);
//    if (map != null) {
//      return map;
//    }
    String customKey = getCustomKey(request);
    if (StringUtil.isEmpty(customKey)) {
      return returnFail(RespCode.error101, "系统异常");
    }
    List<Map<String, Object>> list = contractService.getCustomerFirm(customKey);
    return returnSuccess(list);
  }

  @GetMapping(value = "/listExport")
  public void listExport(HttpServletResponse response,
      @RequestParam(required = false) String customName,
      @RequestParam(required = false) String userName,
      @RequestParam(required = false) String idCard,
      @RequestParam(required = false) String projectName,
      @RequestParam(required = false) String companyName,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime,
      HttpServletRequest request) {

    Map<String, Object> map = getCustomerAuthority(request);
//    if (map != null) {
//      return;
//    }
    String customKey = getCustomKey(request);
    if (StringUtil.isEmpty(customKey)) {
      return;
    }

    Map<String, Object> paramMap = new HashMap<>(12);
    if (!"mfkj".equals(customKey)) {
      paramMap.put("customKey", customKey);
    }
    paramMap.put("customName", customName);
    paramMap.put("userName", userName);
    paramMap.put("idCard", idCard);
    paramMap.put("projectName", projectName);
    paramMap.put("companyName", companyName);
    paramMap.put("status", status);
    paramMap.put("startTime", startTime);
    paramMap.put("endTime", endTime);

    List<Contract> list = contractService.listContract(map);
    String[] colunmName = new String[]{"商户名称(发包商)", "个体工商户/自然人名称", "证件号", "项目名称",
        "项目类型", "服务公司", "交易状态", "状态描述", "合同附件",
        "操作账号", "创建时间", "更新时间"};
    String filename = "合同管理表";
    List<Map<String, Object>> data = new ArrayList<>();
    for (Contract contract : list) {
      Map<String, Object> dataMap = new HashMap<>(20);
      dataMap.put("1", contract.getCustomName());
      dataMap.put("2", contract.getUserName());
      dataMap.put("3", contract.getIdCard());
      dataMap.put("4", contract.getTaskName());
      dataMap.put("5", contract.getEcoCateName());
      dataMap.put("6", contract.getCompanyName());
      if (AccountTransStatus.success.getCode() == contract.getStatus()) {
        dataMap.put("7", AccountTransStatus.success.getDesc());
      } else if (AccountTransStatus.fail.getCode() == contract.getStatus()) {
        dataMap.put("7", AccountTransStatus.fail.getDesc());
      } else if (AccountTransStatus.doing.getCode() == contract.getStatus()) {
        dataMap.put("7", AccountTransStatus.doing.getDesc());
      }
      dataMap.put("8", contract.getStatusDesc());
      dataMap.put("9", bestSignConfig.getServerNameUrl() + contract.getContractUrl());
      dataMap.put("10", contract.getAddUser());
      dataMap.put("11", contract.getCreateTime());
      dataMap.put("12", contract.getUpdateTime());
      data.add(sortMapByKey(dataMap));
    }
    ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
  }

  private Map<String, Object> getCustomerAuthority(HttpServletRequest request) {
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if (!CommonString.ROOT.equals(customLogin.getCustomkey()) && !CommonString.ROOT
        .equals(customLogin.getMasterCustom()) && customLogin.getCustomType() != CustomType.GROUP
        .getCode() && customLogin.getMasterCustomType() != CustomType.GROUP.getCode()
        && customLogin.getCustomType() != CustomType.CUSTOM.getCode()
        && customLogin.getMasterCustomType() != CustomType.CUSTOM.getCode()) {
      return returnFail(RespCode.error101, "权限不足！");
    }
    return null;
  }

  private String getCustomKey(HttpServletRequest request) {
    boolean flag = true;
    if (flag){
      return "mfkj";
    }
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if (CustomType.ROOT.getCode() == customLogin.getCustomType() && !StringUtil
        .isEmpty(customLogin.getMasterCustom())) {
      ChannelCustom masterCustom = customService
          .getCustomByCustomkey(customLogin.getMasterCustom());
      if (masterCustom != null) {
        customLogin = masterCustom;
      }
    }

    if (CommonString.ROOT.equals(customLogin.getCustomkey()) || CommonString.ROOT
        .equals(customLogin.getMasterCustom())) {
      return "mfkj";
    } else if (customLogin.getCustomType() == CustomType.CUSTOM.getCode()) {
      return customLogin.getCustomkey();
    } else if (customLogin.getCustomType() == CustomType.GROUP.getCode()) {
      int nodeId = organizationTreeService.queryNodeIdByCustomKey(customLogin.getCustomkey());
      List<String> customKeys = organizationTreeService
          .queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
              nodeId);
      if (customKeys == null || customKeys.size() == 0) {
        return null;
      }
      return String.join(",", customKeys);
    }
    return null;
  }
}
