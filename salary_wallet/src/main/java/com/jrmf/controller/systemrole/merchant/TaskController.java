package com.jrmf.controller.systemrole.merchant;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.APIResponse;
import com.jrmf.common.CommonString;
import com.jrmf.common.Constant;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.common.ServiceResponse;
import com.jrmf.common.YuncrServiceFeignClient;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.*;
import com.jrmf.domain.ChannelAreas;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelTask;
import com.jrmf.domain.ChannelTaskType;
import com.jrmf.domain.dto.YuncrPushProjectDTO;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelTaskService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.splitorder.domain.ReturnCode;
import com.jrmf.utils.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.jdbc.Null;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/task")
public class TaskController extends BaseController {

  private static Logger logger = LoggerFactory.getLogger(TaskController.class);

  private static List<ChannelAreas> AREAS_LIST;

  private static Map<String, Integer> TASK_TYPE_MAP;

  @Autowired
  private ChannelTaskService taskService;
  @Autowired
  private OrganizationTreeService organizationTreeService;
  @Autowired
  private ChannelCustomService customService;
  @Autowired
  private SalaryConfigUtil conf;
  @Autowired
  private YuncrServiceFeignClient yuncrServiceFeignClient;

  @Value("${platformCompanyId}")
  private String platformCompanyId;


  @PostMapping(value = "/autogenerateTask")
  @ResponseBody
  public Map<String, Object> autogenerateTask(String startTime, String endTime, String customKey,
      String startAmount, String endAmount, String taskIds, String orderNos) {

    if (StringUtil.isEmpty(startTime) || StringUtil.isEmpty(endTime)) {
      return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
    }

    if (!StringUtil.isEmpty(customKey) && customKey.contains(",") && !StringUtil.isEmpty(taskIds)) {
      return returnFail(RespCode.error101, "多商户模式禁止指定taskIds");
    }

    if ((!StringUtil.isEmpty(taskIds) || !StringUtil.isEmpty(orderNos)) && StringUtil
        .isEmpty(customKey)) {
      return returnFail(RespCode.error101, "指定taskIds或orderNos请提供对应商户customKey");
    }

    try {
      taskService.autogenerateTask(customKey, startTime, endTime, startAmount, endAmount, taskIds,
          orderNos);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }

    return returnSuccess();
  }


  @PostMapping(value = "/getTaskList")
  @ResponseBody
  public Map<String, Object> getTaskList(HttpServletRequest request, String userName,
      String startTime, String endTime, String startAmount, String endAmount,
      String customName, String account, Integer payType, String companyId, String certId,
      Integer taskType, Integer taskStatus, String taskName, Integer taskPartition,
      String phoneNo,
      @RequestParam(required = false, defaultValue = "1") Integer pageNo,
      @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

    ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

    if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil
        .isEmpty(loginUser.getMasterCustom())) {
      ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
      if (masterCustom != null) {
        loginUser = masterCustom;
      }
    }

    Map<String, Object> paramMap = new HashMap<>();

    paramMap.put("userName", userName);
    paramMap.put("startTime", startTime);
    paramMap.put("endTime", endTime);
    paramMap.put("payType", payType);
    paramMap.put("customName", customName);
    paramMap.put("account", account);
    paramMap.put("companyId", companyId);
    paramMap.put("certId", certId);
    paramMap.put("startAmount", startAmount);
    paramMap.put("endAmount", endAmount);
    paramMap.put("taskType", taskType);
    paramMap.put("taskStatus", taskStatus);
    paramMap.put("taskName", taskName);
    paramMap.put("taskPartition", taskPartition);
    paramMap.put("getTaskList", "1");
    paramMap.put("phoneNo", phoneNo);

    if (!isRootAdmin(loginUser)) {
      if (loginUser.getCustomType() == CustomType.CUSTOM.getCode()) {
        paramMap.put("customKey", loginUser.getCustomkey());
      } else if (loginUser.getCustomType() == CustomType.GROUP.getCode()) {
        int nodeId = organizationTreeService.queryNodeIdByCustomKey(loginUser.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                nodeId);
        if (customKeys == null || customKeys.size() == 0) {
          return returnFail(RespCode.error101, RespCode.RELATIONSHIP_DOES_NOT_EXIST);
        }
        paramMap.put("customKey", String.join(",", customKeys));
      } else if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
        paramMap.put("companyId", loginUser.getCustomkey());
      } else {
        return returnFail(RespCode.error101, "权限错误");
      }
    }

    try {
//            int total = taskService.selectCustomAllCount(paramMap);
//            paramMap.put("start", (pageNo - 1) * pageSize);
//            paramMap.put("limit", pageSize);

      PageHelper.startPage(pageNo, pageSize);
      List<ChannelTask> taskList = taskService.selectCustomAll(paramMap);
      PageInfo<ChannelTask> pageInfo = new PageInfo<>(taskList);

      paramMap.clear();
      paramMap.put("total", pageInfo.getTotal());
      paramMap.put("list", pageInfo.getList());

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }

    return returnSuccess(paramMap);
  }

  @GetMapping(value = "/getTaskTypeList")
  @ResponseBody
  public Map<String, Object> getTaskTypeList() {

    List<ChannelTaskType> list = taskService.selectAllType();
    return returnSuccess(list);
  }

  @RequestMapping(value = "/getTaskList/export")
  public void transactionListExport(HttpServletRequest request, HttpServletResponse response,
      String userName, String startTime, String endTime, String startAmount, String endAmount,
      String customName, String account, Integer payType, String companyId, String certId,
      Integer taskType, Integer taskPartition, Integer taskStatus, String taskName) {

    ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

    if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil
        .isEmpty(loginUser.getMasterCustom())) {
      ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
      if (masterCustom != null) {
        loginUser = masterCustom;
      }
    }

    Map<String, Object> paramMap = new HashMap<>();

    paramMap.put("userName", userName);
    paramMap.put("startTime", startTime);
    paramMap.put("endTime", endTime);
    paramMap.put("payType", payType);
    paramMap.put("customName", customName);
    paramMap.put("account", account);
    paramMap.put("companyId", companyId);
    paramMap.put("certId", certId);
    paramMap.put("startAmount", startAmount);
    paramMap.put("endAmount", endAmount);
    paramMap.put("taskType", taskType);
    paramMap.put("taskStatus", taskStatus);
    paramMap.put("taskName", taskName);
    paramMap.put("taskPartition", taskPartition);
    paramMap.put("getTaskList", "1");

    if (!isRootAdmin(loginUser)) {
      if (loginUser.getCustomType() == CustomType.CUSTOM.getCode()) {
        paramMap.put("customKey", loginUser.getCustomkey());
      } else if (loginUser.getCustomType() == CustomType.GROUP.getCode()) {
        int nodeId = organizationTreeService.queryNodeIdByCustomKey(loginUser.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                nodeId);
        paramMap.put("customKey", String.join(",", customKeys));
      } else if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
        paramMap.put("companyId", loginUser.getCustomkey());
      }
    }

    List<ChannelTask> taskList = taskService.selectCustomAll(paramMap);

    String[] colunmName = new String[]{"商户名称", "订单ID", "收款人姓名", "证件类型", "证件号", "收款账号", "账号所属金融机构",
        "下发方式", "服务公司（经纪服务公司）", "结算金额", "结算交易时间", "工作量计价", "完成任务量", "绩效费", "其他附加费用", "任务名称", "任务种类",
        "任务类型", "任务状态", "联系手机号", "业务订单号"};
    String filename = "任务结算工作量表";
    List<Map<String, Object>> data = new ArrayList<>();
    for (ChannelTask task : taskList) {
      Map<String, Object> dataMap = new HashMap<>(20);
      dataMap.put("1", task.getCustomName());
      dataMap.put("2", task.getOrderNo());
      dataMap.put("3", task.getUndertakerName());
      dataMap.put("4", CertType.codeOf(CertType.ID_CARD.getCode()).getDesc());
      dataMap.put("5", task.getUndertakerCertId());
      dataMap.put("6", task.getAccount());
      dataMap.put("7", task.getBankName());
//            dataMap.put("8", task.getLinkPhoneNo());
      dataMap.put("8", PayType.codeOf(task.getPayType()).getDesc());
      dataMap.put("9", task.getCompanyName());
      dataMap.put("10", task.getTaskAmount());
      dataMap.put("11", task.getPaymentTime());
      dataMap.put("12", task.getUnitPrice() + "元/" + task.getUnitTab());
      dataMap.put("13", task.getTaskAchievement() + task.getUnitTab());
      dataMap.put("14", task.getAchievementFee());
      dataMap.put("15", task.getOtherFee());
      dataMap.put("16", task.getTaskName());
      dataMap.put("17", TaskPartitionStatus.codeOf(task.getTaskPartition()).getDesc());
      dataMap.put("18", task.getTypeName());
      if (task.getStatus() != null) {
        dataMap.put("19", TaskStatus.codeOf(task.getStatus()).getDesc());
      }
      dataMap.put("20", task.getLinkPhoneNo());
      dataMap.put("21", task.getCustomOrderNo());
      data.add(sortMapByKey(dataMap));
    }
    ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
  }

  @PostMapping(value = "/getResourceTaskList")
  @ResponseBody
  public Map<String, Object> getResourceTaskList(HttpServletRequest request, String startTime,
      String endTime, String startAmount, String endAmount,
      String customName, Integer taskType, String taskName, Integer taskPartition,
      String regionCode, String cityCode, String countyCode,
      @RequestParam(required = false) Byte bizType,
      @RequestParam(required = false, defaultValue = "1") Integer pageNo,
      @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

    ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

    if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil
        .isEmpty(loginUser.getMasterCustom())) {
      ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
      if (masterCustom != null) {
        loginUser = masterCustom;
      }
    }

    Map<String, Object> paramMap = new HashMap<>();

    paramMap.put("startTime", startTime);
    paramMap.put("endTime", endTime);
    paramMap.put("customName", customName);
    paramMap.put("startAmount", startAmount);
    paramMap.put("endAmount", endAmount);
    paramMap.put("taskType", taskType);
    paramMap.put("taskName", taskName);
    paramMap.put("taskPartition", taskPartition);
    paramMap.put("regionCode", regionCode);
    paramMap.put("cityCode", cityCode);
    paramMap.put("countyCode", countyCode);
    paramMap.put("getResourceTaskList", "1");
    paramMap.put("bizType", bizType);
    paramMap.put("deletedFlag", false);

    if (!isRootAdmin(loginUser)) {
      if (loginUser.getCustomType() == CustomType.CUSTOM.getCode()) {
        paramMap.put("customKey", loginUser.getCustomkey());
      } else if (loginUser.getCustomType() == CustomType.GROUP.getCode()) {
        int nodeId = organizationTreeService.queryNodeIdByCustomKey(loginUser.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN,
                nodeId);
        if (customKeys == null || customKeys.size() == 0) {
          return returnFail(RespCode.error101, RespCode.RELATIONSHIP_DOES_NOT_EXIST);
        }
        paramMap.put("customKey", String.join(",", customKeys));
      } else if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
        paramMap.put("companyId", loginUser.getCustomkey());
      } else {
        return returnFail(RespCode.error101, "权限错误");
      }
    }

    try {
      PageHelper.startPage(pageNo, pageSize);
      List<ChannelTask> taskList = taskService.selectResourceAll(paramMap);
      PageInfo<ChannelTask> pageInfo = new PageInfo<>(taskList);

      paramMap.clear();
      paramMap.put("total", pageInfo.getTotal());
      paramMap.put("list", pageInfo.getList());

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }

    return returnSuccess(paramMap);
  }

//    @PostMapping(value = "/addResourceTask")
//    @ResponseBody
//    public Map<String, Object> addResourceTask(HttpServletRequest request, ChannelTask task) {
//
//        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
//
//        try {
//            task.setOperatorName(loginUser.getUsername());
//            task.setCompanyId("1018");
//
//            if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
//                ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
//                if (masterCustom != null) {
//                    loginUser = masterCustom;
//                }
//            }
//
//            if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
//                task.setCompanyId(loginUser.getCustomkey());
//            }
//
//            taskService.insert(task);
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
//        }
//
//        return returnSuccess();
//    }

  @PostMapping(value = "/updateResourceTask")
  @ResponseBody
  public Map<String, Object> updateResourceTask(HttpServletRequest request, ChannelTask task) {
    ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

    try {
      if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil
          .isEmpty(loginUser.getMasterCustom())) {
        ChannelCustom masterCustom = customService
            .getCustomByCustomkey(loginUser.getMasterCustom());
        if (masterCustom != null) {
          loginUser = masterCustom;
        }
      }
//      task.setCustomKey(loginUser.getCustomkey());
      if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
        task.setCompanyId(loginUser.getCustomkey());
      } else {
        task.setCompanyId(task.getCompanyId());
      }
      if (task.getId() != null) {
        taskService.updateByPrimaryKeySelective(task);
      } else {
        task.setOperatorName(loginUser.getUsername());
        if (task.getBizType() != null && task.getBizType().byteValue() == 2) {
          task.setDeletedFlag(true);
        }
        Date date = new Date();
        task.setCreateTime(DateUtils.formartDate(date, "yyyy-MM-dd HH:mm:ss"));
        task.setLastUpdateTime(DateUtils.formartDate(date, "yyyy-MM-dd HH:mm:ss"));
        task.setPublishTime(DateUtils.formartDate(date, "yyyy-MM-dd HH:mm:ss"));
        taskService.insertSelective(task);
        //同步云控
        if (task.getBizType() != null && task.getBizType().byteValue() == 2) {
          YuncrPushProjectDTO yuncrPushProjectDTO = task2YuncrProDto(task);
          ServiceResponse serviceResponse = yuncrServiceFeignClient.openItem(yuncrPushProjectDTO);
          if (!(serviceResponse != null && Constant.SERVICE_RESPONSE_CODE_SUCCESS
              .equals(serviceResponse.getCode()))) {
            return returnFail(RespCode.error600, serviceResponse.getMsg());
          }

          Map<String, String> resultMap = (Map<String, String>) serviceResponse.getData();
          task.setDeletedFlag(false);
          task.setBidno(resultMap.get("bidno"));
          task.setPlatsrl(resultMap.get("platsrl"));
          taskService.updateByPrimaryKeySelective(task);
        }

      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }
    return returnSuccess();
  }

  public YuncrPushProjectDTO task2YuncrProDto(ChannelTask channelTask) {
    YuncrPushProjectDTO yuncrPushProjectDTO = new YuncrPushProjectDTO();
    yuncrPushProjectDTO.setBidTitle(channelTask.getTaskName());
    yuncrPushProjectDTO.setCategoryId(channelTask.getEcoCateCode());
    yuncrPushProjectDTO
        .setFileBase(Base64Utils.remoteFileToBase64(channelTask.getContractFileUrl()));
    String originalFileName = channelTask.getContractFileName();

    String[] fileNameArr = originalFileName.split("\\.");
    yuncrPushProjectDTO.setFileName(fileNameArr[0]);
    yuncrPushProjectDTO.setFileType(fileNameArr[1]);
    yuncrPushProjectDTO.setInvoiceInfo(channelTask.getInvoiceCategoryName());
    yuncrPushProjectDTO.setInvoiceType(String.valueOf(channelTask.getInvoiceType()));
    yuncrPushProjectDTO.setFirmId(channelTask.getFirmId());
    yuncrPushProjectDTO.setLoanDesc(channelTask.getInvoiceDetail());
    return yuncrPushProjectDTO;
  }


  @PostMapping(value = "/deleteTask")
  @ResponseBody
  public Map<String, Object> deleteTask(Integer id) {

    try {
      taskService.deleteByPrimaryKey(id);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }

    return returnSuccess();
  }


  @RequestMapping(value = "/getRegionList")
  @ResponseBody
  public Map<String, Object> getAllRegionList() {

    if (AREAS_LIST != null && AREAS_LIST.size() > 0) {
      return returnSuccess(AREAS_LIST);
    }

    List<ChannelAreas> parentList = taskService.selectByParentCode("0");

    for (ChannelAreas parentArea : parentList) {
      List<ChannelAreas> cityList = taskService.selectByParentCode(parentArea.getCode());
      for (ChannelAreas cityArea : cityList) {
        List<ChannelAreas> countyList = taskService.selectByParentCode(cityArea.getCode());
        cityArea.setChildren(countyList);
      }
      parentArea.setChildren(cityList);
    }

    AREAS_LIST = parentList;
    return returnSuccess(AREAS_LIST);
  }


  @PostMapping(value = "/inputTask")
  @ResponseBody
  public Map<String, Object> inputTask(HttpServletRequest request, MultipartFile file,
      String customKey, Integer payType, String companyId) {

    ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

    logger.info("inputTask请求参数======> customKey={},payType={},companyId={}", customKey, payType,
        companyId);

    InputStream inputStream;
    Workbook workbook;
    ByteArrayOutputStream bytesOut;
    try {
      inputStream = file.getInputStream();
      int readLen;
      byte[] byteBuffer = new byte[1024];
      bytesOut = new ByteArrayOutputStream();
      while ((readLen = inputStream.read(byteBuffer)) > -1) {
        bytesOut.write(byteBuffer, 0, readLen);
      }
      byte[] fileData = bytesOut.toByteArray();
      try {
        workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData));
      } catch (Exception ex) {
        workbook = new HSSFWorkbook(new ByteArrayInputStream(fileData));
      }

      Sheet sheet = workbook.getSheetAt(0);
      XSSFRow row = (XSSFRow) sheet.getRow(0);
      XSSFRow row1 = (XSSFRow) sheet.getRow(1);

      if (row == null || row1 == null) {
        logger.info("数据行不可以为空");
        return returnFail(RespCode.error101, "数据行不可以为空");
      } else {
        String inputTemplate = "";
        for (int i = 0; i < 15; i++) {
          inputTemplate += StringUtil.getXSSFCell(row.getCell(i));
        }

        logger.info("Input Template Head = {}", inputTemplate);
        if (CommonString.INPUTTASKTEMPLATECONF_BANKCARD_FIRST.equals(inputTemplate)) {
          logger.info("模板正确======>");
        } else {
          logger.info("导入模板不正确");
          return returnFail(RespCode.error101, "导入模板不正确");
        }
      }

//            //设置保存数据的目录
//            String path = CommonString.EXECLPATH + "/littleBeeFile/task/" + customKey;
//            //创建保存数据的目录
//            FileUtil.mkdirs(path);
//
//            String fileName = file.getOriginalFilename();
//            String fileUrl = path + "/" + fileName;
//
//            //保存源文件
//            File sourceFile = new File(fileUrl);
//            file.transferTo(sourceFile);

      String uploadPath = "/littleBeeFile/task/" + customKey + "/";
      FtpTool.uploadFile(uploadPath, file.getOriginalFilename(), file.getInputStream());

      List<ChannelTaskType> list = taskService.selectAllType();
      if (TASK_TYPE_MAP == null || TASK_TYPE_MAP.size() == 0) {
        TASK_TYPE_MAP = new HashMap<>();
        for (ChannelTaskType taskType : list) {
          TASK_TYPE_MAP.put(taskType.getTypeName(), taskType.getId());
        }
      }

      return readTaskExcelData(workbook, customKey, companyId, payType, loginUser.getUsername());

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    return returnFail(RespCode.error101, RespCode.EXPORT_FAILIURE);
  }

  public Map<String, Object> readTaskExcelData(Workbook workbook, String customKey,
      String companyId, Integer payType, String operatorName) {
    Sheet sheet = workbook.getSheetAt(0);
    int num = 0;
    for (int j = 1; j < sheet.getPhysicalNumberOfRows(); j++) {
      XSSFRow row = (XSSFRow) sheet.getRow(j);
      if (row == null) {
        continue;
      }
      ++num;
    }
    logger.info("excel导入数据 ======> {} 条", num);
    if (num > 2000 || num < 0) {
      return returnFail(RespCode.error101, ReturnCode.DATA_OVERFLOW.getMsg());
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String documentType, taskName;
    for (int j = 1; j < sheet.getPhysicalNumberOfRows(); j++) {// 获取每行
      XSSFRow row = (XSSFRow) sheet.getRow(j);
      if (row == null) {
        continue;
      }

      ChannelTask task = new ChannelTask();
      task.setTaskPartition(TaskPartitionStatus.IMPORTTASK.getCode());
      task.setStatus(TaskStatus.TOBECONFIRM.getCode());
      task.setPayType(payType);
      task.setCustomKey(customKey);
      task.setCompanyId(companyId);
      task.setOperatorName(operatorName);
      task.setUndertakerName(StringUtil.getXSSFCell(row.getCell(0)).trim());

      documentType = StringUtil.getXSSFCell(row.getCell(1));//证件类型

      if (!StringUtil.isEmpty(documentType)) {
        task.setDocumentType(CertType.descOfDefault(documentType).getCode());
      }
      task.setUndertakerCertId(StringUtil.getXSSFCell(row.getCell(2)).trim());
      task.setAccount(StringUtil.getXSSFCell(row.getCell(3)).trim());
      task.setBankName(StringUtil.getXSSFCell(row.getCell(4)).trim());
      task.setTaskAmount(StringUtil.getXSSFCell(row.getCell(5)).trim());

      Date paymenTime = row.getCell(6).getDateCellValue();
      String time = sdf.format(new Date());
      if (paymenTime != null) {
        time = sdf.format(paymenTime);
      }

      task.setPaymentTime(time);
      task.setStartTime(time);
//            task.setEndTime(time);

      task.setUnitPrice(StringUtil.getXSSFCell(row.getCell(7)).trim());
      task.setUnitTab(StringUtil.getXSSFCell(row.getCell(8)).trim());
      task.setTaskAchievement(StringUtil.getXSSFCell(row.getCell(9)).trim());
      task.setAchievementFee(StringUtil.getXSSFCell(row.getCell(10)).trim());
      task.setOtherFee(StringUtil.getXSSFCell(row.getCell(11)).trim());
      task.setTaskName(StringUtil.getXSSFCell(row.getCell(12)).trim());

      taskName = StringUtil.getXSSFCell(row.getCell(13)).trim();// 任务类型
      if (!StringUtil.isEmpty(taskName)) {
        task.setTaskType(TASK_TYPE_MAP.get(taskName));
      }
      task.setCustomOrderNo(StringUtil.getXSSFCell(row.getCell(14)).trim());

      taskService.insert(task);

      logger.info("第 {} 行读取到数据 ======> data={}", j, task);
    }

    return returnSuccess();
  }

  /**
   * 任务导入模板
   */
  @RequestMapping(value = "/downloadTaskTemplate")
  public ResponseEntity<byte[]> downloadTaskTemplate() {

    String str = conf.getTaskDataInputFilePath();
    String filePath = str.substring(0, str.lastIndexOf("/"));
    String fileName = str.substring(str.lastIndexOf("/") + 1);

    HttpHeaders headers = new HttpHeaders();
    byte[] bytes = FtpTool.downloadFtpFile(filePath, fileName);
    if (bytes != null) {
      try {
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        fileName = new String(("任务结算工作量导入模板" + ".xlsx").getBytes(),
            StandardCharsets.ISO_8859_1);// 防止中文乱码
        headers.add("Content-Disposition", "attachment;filename=" + fileName);

        headers.setContentDispositionFormData("attachment", fileName);

      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      }
    }

//        File f = new File(str);
//
//        response.reset();
//        response.setContentType("application/vnd.ms-excel;charset=utf-8");
//        try {
//            response.setHeader("Content-Disposition", "attachment;filename=" + new String(("任务结算工作量导入模板" + ".xlsx").getBytes(), "iso-8859-1"));//下载文件的名称
//        } catch (UnsupportedEncodingException e) {
//            logger.error(e.getMessage(), e);
//        }
//        ServletOutputStream out = response.getOutputStream();
//        BufferedInputStream bis = null;
//        BufferedOutputStream bos = null;
//        try {
//            bis = new BufferedInputStream(new FileInputStream(f));
//            bos = new BufferedOutputStream(out);
//            byte[] buff = new byte[2048];
//            int bytesRead;
//            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
//                bos.write(buff, 0, bytesRead);
//            }
//        } catch (final IOException e) {
//            throw e;
//        } finally {
//            if (bis != null)
//                bis.close();
//            if (bos != null)
//                bos.close();
//        }

    return new ResponseEntity<>(bytes,
        headers, HttpStatus.OK);
  }

  @PostMapping(value = "/updateTaskStatus")
  @ResponseBody
  public Map<String, Object> updateTaskStatus(Integer id, Integer status) {

    try {
      taskService.updateTaskStatus(id, status);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }

    return returnSuccess();
  }

}
