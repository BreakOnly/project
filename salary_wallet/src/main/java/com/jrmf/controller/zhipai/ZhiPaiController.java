package com.jrmf.controller.zhipai;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.APIResponse;
import com.jrmf.common.Constant;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.common.ServiceResponse;
import com.jrmf.common.YuncrServiceFeignClient;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelTask;
import com.jrmf.domain.CustomerFirm;
import com.jrmf.domain.TaxCode;
import com.jrmf.domain.dto.YuncrPushProjectDTO;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelTaskService;
import com.jrmf.service.CustomerFirmService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.utils.Base64Utils;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.SalaryConfigUtil;
import com.jrmf.utils.StringUtil;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/zhipai")
@Slf4j
public class ZhiPaiController extends BaseController {

  @Autowired
  YuncrServiceFeignClient yuncrServiceFeignClient;
  @Autowired
  CustomerFirmService customerFirmService;

  @Autowired
  private ChannelTaskService taskService;
  @Autowired
  private ChannelCustomService customService;
  @Autowired
  private OrganizationTreeService organizationTreeService;
  @Autowired
  private SalaryConfigUtil conf;
  @Value("${ftppath}")
  private String basePath;


  @ApiOperation("获取经济分类信息")
  @GetMapping("/economic/category")
  public APIResponse getCategoryList(
      @RequestParam Integer level,
      @RequestParam(required = false) String levelCode) {
    ServiceResponse serviceResponse = yuncrServiceFeignClient.getCategoryList(level, levelCode);
    if (serviceResponse != null && Constant.SERVICE_RESPONSE_CODE_SUCCESS
        .equals(serviceResponse.getCode())) {
      return APIResponse.successResponse(serviceResponse.getData());
    } else {
      return APIResponse.errorResponse(ResponseCodeMapping.ERR_500);
    }
  }


  @ApiOperation("获取税收分类编码")
  @GetMapping("/tax/category")
  public APIResponse getTaxCategoryList(@RequestParam Integer level,
      @RequestParam(required = false) String levelCode) {
    List<TaxCode> taxCodeList = taskService.selectTaxCode(level, levelCode);
    return APIResponse.successResponse(taxCodeList);
  }

  @ApiOperation("查询发包方信息")
  @GetMapping("/employer/info")
  public APIResponse getEmployerInfo(HttpServletRequest request) {
    String customKey = request.getParameter("customKey");
    if (customKey == null || "".equals(customKey)) {
      ChannelCustom channelCustom = (ChannelCustom) request.getSession()
          .getAttribute("customLogin");
      if (channelCustom != null) {
        customKey = channelCustom.getCustomkey();
      }
    }
    if (customKey != null && !"".equals(customKey)) {
      CustomerFirm customerFirm = customerFirmService.getCustomerFirmByCustomKey(customKey);
      return APIResponse.successResponse(customerFirm);
    }
    return APIResponse.errorResponse(ResponseCodeMapping.ERR_5008);
  }


  @PostMapping(value = "/updateResourceTask")
  @ResponseBody
  public Map<String, Object> updateResourceTask(HttpServletRequest request, ChannelTask task) {
    //ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

    ChannelCustom loginUser = new ChannelCustom();
    loginUser.setCustomkey("mfkj");
    loginUser.setCustomType(1);
    loginUser.setCompanyName("北京西城区阜成门一路魔方科技有限公司");

    try {
      if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil
          .isEmpty(loginUser.getMasterCustom())) {
        ChannelCustom masterCustom = customService
            .getCustomByCustomkey(loginUser.getMasterCustom());
        if (masterCustom != null) {
          loginUser = masterCustom;
        }
      }
      //TODO 这是个什么逻辑
      task.setCustomKey(loginUser.getCustomkey());
      if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
        task.setCompanyId(loginUser.getCustomkey());
      } else {
        task.setCompanyId("1018");
      }
      if (task.getId() != null) {
        taskService.updateByPrimaryKeySelective(task);
      } else {
        task.setOperatorName(loginUser.getUsername());
        if (task.getBizType() != null && task.getBizType().byteValue() == 2) {
          task.setDeletedFlag(true);
        }
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
      log.error(e.getMessage(), e);
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
    return yuncrPushProjectDTO;
  }

  /**
   * 文件上传
   */
  @PostMapping(value = "/upload")
  @ResponseBody
  private APIResponse commonUpload(MultipartFile upfile,
      @RequestParam(required = false) String bizPath) {
    try {
      if (!upfile.isEmpty()) {

        long size = upfile.getSize();  // 文件大小
        String fileName = UUID.randomUUID().toString() + "." + FilenameUtils
            .getExtension(upfile.getOriginalFilename());
        String uploadPath = "/common/upload/";
        if (bizPath != null && !"".equals(bizPath)) {
          uploadPath = bizPath;
        }
        boolean state = FtpTool.uploadFile(uploadPath, fileName, upfile.getInputStream());

        if (state) {
          log.info("上传成功");
          JSONObject jsonObject = new JSONObject();
          jsonObject.put("url", basePath + uploadPath + fileName);
          jsonObject.put("size", size);
          jsonObject.put("originalFilename", upfile.getOriginalFilename());
          return APIResponse.successResponse(jsonObject);
        }
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return APIResponse.errorResponse(ResponseCodeMapping.ERR_500);
  }

  private Map<String, Object> resultMap(String state, String url, long size, String title,
      String original, String type) {
    Map<String, Object> result = new HashMap<>();
    result.put("state", state);
    result.put("original", original);
    result.put("size", size);
    result.put("title", title);
    result.put("type", type);
    result.put("url", url);
    return result;
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

    //ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
    ChannelCustom loginUser = new ChannelCustom();
    loginUser.setCustomkey("mfkj");
    loginUser.setCustomType(1);
    loginUser.setCompanyName("北京西城区阜成门一路魔方科技有限公司");

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
      log.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }

    return returnSuccess(paramMap);
  }

}
