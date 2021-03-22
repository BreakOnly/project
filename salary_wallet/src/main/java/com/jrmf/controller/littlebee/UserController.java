package com.jrmf.controller.littlebee;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.*;
import com.jrmf.domain.*;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.ChannelUserRealNameService;
import com.jrmf.service.ChannelUserService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.service.UserSerivce;
import com.jrmf.service.UsersAgreementService;
import com.jrmf.splitorder.domain.ReturnCode;
import com.jrmf.utils.*;
import com.jrmf.utils.exception.CheckUserNameCertIdCountException;
import com.jrmf.utils.threadpool.ThreadUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

@RestController
@RequestMapping("/littleBee/user")
public class UserController extends BaseController {

  private static Logger logger = LoggerFactory.getLogger(UserController.class);


  @Autowired
  private ChannelUserService channelUserService;
  @Autowired
  private ChannelUserRealNameService channelUserRealNameService;
  @Autowired
  private SalaryConfigUtil conf;
  @Autowired
  private UsersAgreementService usersAgreementService;
  @Autowired
  private UserSerivce userSerivce;
  @Autowired
  private OrganizationTreeService organizationTreeService;
  @Autowired
  private ChannelRelatedService channelRelatedService;

  @PostMapping(value = "/list")
  public Map<String, Object> list(HttpServletRequest request, String userName, String certId,
      String startTime, String endTime, String phoneNo, Integer state,
      Integer level, Integer type, Integer pageNo, Integer pageSize) {

    ChannelCustom loginUser = (ChannelCustom) request.getSession()
        .getAttribute(CommonString.CUSTOMLOGIN);

    if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil
        .isEmpty(loginUser.getMasterCustom())) {
      ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
      if (masterCustom != null) {
        loginUser = masterCustom;
      }
    }

    Map<String, Object> paramMap = new HashMap<>();

    paramMap.put("userName", userName);
    paramMap.put("certId", certId);
    paramMap.put("startTime", startTime);
    paramMap.put("endTime", endTime);
    paramMap.put("phoneNo", phoneNo);

    paramMap.put("state", state);
    paramMap.put("level", level);
    paramMap.put("type", type);

    if (!isRootAdmin(loginUser)) {
      if (loginUser.getCustomType() == CustomType.CUSTOM.getCode()) {
        paramMap.put("customKey", loginUser.getCustomkey());
      } else if (loginUser.getCustomType() == CustomType.GROUP.getCode()) {
        int nodeId = organizationTreeService
            .queryNodeIdByCustomKey(loginUser.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(CustomType.GROUP.getCode(),
                QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
        if (customKeys == null || customKeys.size() == 0) {
          return returnFail(RespCode.error101, RespCode.RELATIONSHIP_DOES_NOT_EXIST);
        }
        paramMap.put("customKey", String.join(",", customKeys));
      } else if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
        List<String> customKeys = channelRelatedService
            .queryCustomKeysByCompanyId(loginUser.getCustomkey());
        paramMap.put("customKey", String.join(",", customKeys));
      } else {
        return returnFail(RespCode.error101, "权限错误");
      }
    }

    try {

      Map<String, Object> result = new HashMap<>(5);
      PageHelper.startPage(pageNo, pageSize);
      PageHelper.orderBy("createTime desc");
      List<Map<String, Object>> userList = channelUserService.getAllList(paramMap);
      PageInfo<ChannelUser> page = new PageInfo(userList);

      result.put("total", page.getTotal());
      result.put("list", page.getList());
      return returnSuccess(result);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }
  }


  /**
   * 小黄蜂用户导入模板下载
   *
   * @throws Exception
   */
  @RequestMapping(value = "/downloadUserTemplate")
  public ResponseEntity<byte[]> downloadUserTemplate() {

    String str = conf.getLittleBeeUserDataInputFilePath();
    String filePath = str.substring(0, str.lastIndexOf("/"));
    String fileName = str.substring(str.lastIndexOf("/") + 1);

    HttpHeaders headers = new HttpHeaders();
    byte[] bytes = FtpTool.downloadFtpFile(filePath, fileName);
    if (bytes != null) {
      try {
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        fileName = new String(("任务平台用户推荐导入模板" + ".xlsx").getBytes(),
            StandardCharsets.ISO_8859_1);// 防止中文乱码
        headers.add("Content-Disposition", "attachment;filename=" + fileName);

        headers.setContentDispositionFormData("attachment", fileName);

      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      }
    }

    return new ResponseEntity<>(bytes,
        headers, HttpStatus.OK);
  }


  @PostMapping(value = "/inputUser")
  public Map<String, Object> inputUser(HttpServletRequest request, MultipartFile file,
      String customKey) {

    logger.info("小黄蜂inputUser请求参数======> customKey={}", customKey);

    ChannelCustom loginUser = (ChannelCustom) request.getSession()
        .getAttribute(CommonString.CUSTOMLOGIN);

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
        StringBuilder userTemplate = new StringBuilder();
        for (int i = 0; i < 5; i++) {
          userTemplate.append(StringUtil.getXSSFCell(row1.getCell(i)));
        }

        logger.info("Input Template Head = {}", userTemplate.toString());
        if (CommonString.INPUT_USERTEMPLATE_FIRST.equals(userTemplate.toString())) {
          logger.info("模板正确======>");
        } else {
          logger.info("导入模板不正确");
          return returnFail(RespCode.error101, "导入模板不正确");
        }
      }
//
//            //设置保存数据的目录
//            String path = CommonString.EXECLPATH + "/littleBee/" + customKey;
//            //创建保存数据的目录
//            FileUtil.mkdirs(path);

      String date = DateUtils.formartDate(new Date(), "yyyyMMddHHmmss");
//            String fileName = date + file.getOriginalFilename();
//            String fileUrl = path + "/" + fileName;
//
//            //保存源文件
//            File sourceFile = new File(fileUrl);
//            file.transferTo(sourceFile);

      String uploadPath = "/littleBeeFile/user/" + customKey + "/";
      FtpTool.uploadFile(uploadPath, date + file.getOriginalFilename(), file.getInputStream());

      return readUserExcelData(workbook, customKey, loginUser.getUsername());

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error101, RespCode.EXPORT_FAILIURE);
    }

  }


  public Map<String, Object> readUserExcelData(Workbook workbook, String customKey,
      String operatorName) throws ParseException {
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
    if (num > 2002 || num < 0) {
      return returnFail(RespCode.error101, ReturnCode.DATA_OVERFLOW.getMsg());
    }

    int success = 0;
    ArrayList<Map<String, Object>> errorList = new ArrayList<>();

    List<User> users = new ArrayList<>();

    String userName, certType, certId, phoneNo = null, remark;
    for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {// 获取每行

      int currentRow = j + 1;
      HashMap<String, Object> map = new HashMap<>(2);

      XSSFRow row = (XSSFRow) sheet.getRow(j);
      if (row == null) {
        map.put("errorInfo", "第" + currentRow + "行记录，数据行不能为空");
        errorList.add(map);
        continue;
      }

      userName = StringUtil.getXSSFCell(row.getCell(0));
      certType = StringUtil.getXSSFCell(row.getCell(1));//证件类型
      certId = StringUtil.getXSSFCell(row.getCell(2));
      phoneNo = StringUtil.getXSSFCell(row.getCell(3));

      if (StringUtil.isEmpty(userName) || StringUtil.isEmpty(certType) || StringUtil
          .isEmpty(certId)) {
        map.put("errorInfo", "第" + currentRow + "行记录，必填字段存在空值");
        errorList.add(map);
        continue;
      }

      if (!StringUtil.iDCardValidate(certId)) {
        map.put("errorInfo", "第" + currentRow + "行记录，身份证号格式错误");
        errorList.add(map);
        continue;
      }

      if (!StringUtil.isEmpty(phoneNo) && !StringUtil.isMobileNO(phoneNo)) {
        map.put("errorInfo", "第" + currentRow + "行记录，手机号格式错误");
        errorList.add(map);
        continue;
      }

      remark = StringUtil.getXSSFCell(row.getCell(4));

      ChannelUser channelUser = new ChannelUser();
      channelUser.setPhoneNo(phoneNo);
      if (StringUtil.isEmpty(phoneNo)) {
        channelUser.setPhoneNo(certId);
      }

      ChannelUser currentUser = channelUserService.selectByPhoneNo(channelUser.getPhoneNo());
      if (currentUser != null) {
        logger.error("导入手机号或证件号：{}已注册,请勿重复导入", channelUser.getPhoneNo());
        map.put("errorInfo", "第" + currentRow + "行记录，导入手机号或证件号已注册，请勿重复导入");
        errorList.add(map);
        continue;
      }

      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("state", "0");
      paramMap.put("certId", certId);
      List<ChannelUserRealName> list = channelUserRealNameService.selectAll(paramMap);
      if (list != null && list.size() > 0) {
        logger.error("导入证件号：{}已实名,请勿重复导入", certId);
        map.put("errorInfo", "第" + currentRow + "行记录，导入证件号已实名,请勿重复导入，请勿重复导入");
        errorList.add(map);
        continue;
      }

      channelUser.setCustomKey(customKey);
      channelUser.setRemark(remark);
      channelUser.setPassword(CipherUtil.generatePassword("123456", channelUser.getPhoneNo()));
      channelUser.setType(LittleBeeUserType.INPUT.getCode());
      channelUser.setOperatorName(operatorName);
      channelUserService.insert(channelUser);

      Integer userId = channelUser.getId();

      if (userId > 0) {
        ChannelUserRealName userRealName = new ChannelUserRealName();
        userRealName.setUserId(userId);
        userRealName.setName(userName);
        userRealName.setCertType(CertType.descOfDefault(certType).getCode());
        userRealName.setCertId(certId);
        userRealName.setUserPhoneNo(channelUser.getPhoneNo());
        userRealName.setState(ChannelUserRealNameType.SUCCESS.getCode());

        Map resultState = usersAgreementService.checkTwoElements(userName, certId);
        if (!"success".equals(resultState.get("status"))) {
          userRealName.setState(ChannelUserRealNameType.FAIL.getCode());
        } else {
          User user = new User(certId, userName, certId, channelUser.getPhoneNo(),
              CertType.ID_CARD.getDesc());
          users.add(user);
        }

        channelUserRealNameService.insert(userRealName);
      }

      success++;
    }

    ThreadUtil.pdfThreadPool.execute(new Thread(() -> {
      HashMap<String, Object> paramMap = new HashMap<>(3);
      paramMap.put("users", users);
      paramMap.put("customkey", customKey);
      try {
        userSerivce.addUserBatchByExcel(paramMap);
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      }
    }));

    HashMap<String, Object> resultMap = new HashMap<>(5);

    resultMap.put("success", success);
    resultMap.put("errorList", errorList);
    resultMap.put("hasError", !errorList.isEmpty());

    return returnSuccess(resultMap);
  }


  @PostMapping(value = "/updateUser")
  public Map<String, Object> updateUser(HttpServletRequest request, Integer userId,
      String customKey, Integer certType, String certId, String userName,String phoneNo) {

    ChannelCustom loginUser = (ChannelCustom) request.getSession()
        .getAttribute(CommonString.CUSTOMLOGIN);

    if (StringUtil.isEmpty(customKey) || certId == null || StringUtil.isEmpty(certId) || StringUtil
        .isEmpty(userName) || StringUtil.isEmpty(phoneNo)) {
      return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
    }

    try {

      ChannelUser channelUser = new ChannelUser();
      ChannelUser existUser = channelUserService.selectByPhoneNo(phoneNo);
      ChannelUserRealName existUserRealName = channelUserRealNameService.selectByCertId(certId);

      if (userId != null) {
        channelUser = channelUserService.selectByUserId(userId);

        if (existUser != null && userId != existUser.getId()) {
          return returnFail(RespCode.error101, "手机号已存在");
        }

        if (existUserRealName != null && channelUser.getId() != existUserRealName.getUserId()) {
          return returnFail(RespCode.error101, "证件号已实名");
        }
      } else {
        if (existUser != null) {
          return returnFail(RespCode.error101, "手机号已存在");
        }

        if (existUserRealName != null) {
          return returnFail(RespCode.error101, "证件号已实名");
        }
      }

      channelUser.setCertId(certId);
      channelUser.setPhoneNo(phoneNo);
      channelUser.setCustomKey(customKey);
      channelUser.setPassword(CipherUtil.generatePassword("123456", channelUser.getPhoneNo()));
      channelUser.setType(LittleBeeUserType.INPUT.getCode());
      channelUser.setOperatorName(loginUser.getUsername());

      channelUserService.addOrUpdateUser(channelUser);

      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("notState", "1");
      paramMap.put("userId", channelUser.getId());

      List<ChannelUserRealName> list = channelUserRealNameService.selectAll(paramMap);
      ChannelUserRealName userRealName = new ChannelUserRealName();
      if (list != null && list.size() > 0) {
        userRealName = list.get(0);
      }

      userRealName.setUserId(channelUser.getId());
      userRealName.setName(userName);
      userRealName.setCertType(certType);
      userRealName.setCertId(certId);
      userRealName.setUserPhoneNo(channelUser.getPhoneNo());

      userRealName.setState(ChannelUserRealNameType.SUCCESS.getCode());

      List<User> users = new ArrayList<>();
      Map resultState = usersAgreementService.checkTwoElements(userName, certId);
      if (!"success".equals(resultState.get("status"))) {
        userRealName.setState(ChannelUserRealNameType.FAIL.getCode());
      } else {
        User user = new User(certId, userName, certId, channelUser.getPhoneNo(),
            CertType.ID_CARD.getDesc());
        users.add(user);
      }

      channelUserRealNameService.addOrUpdateUserRealName(userRealName);

      ThreadUtil.pdfThreadPool.execute(new Thread(() -> {
        HashMap<String, Object> param = new HashMap<>(3);
        param.put("users", users);
        param.put("customkey", customKey);
        try {
          userSerivce.addUserBatchByExcel(param);
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        }
      }));

      return returnSuccess();

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }

  }


  @RequestMapping(value = "/deleteUser")
  public Map<String, Object> deleteUser(Integer userId) {

    if (userId == null) {
      return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
    }

    try {

      channelUserService.deleteByUserId(userId);
      channelUserRealNameService.deleteByUserId(userId);

      return returnSuccess();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }

  }


  /**
   * 二要素校验接口
   *
   * @param userName  姓名
   * @param certId    身份证号
   * @param customKey customKey  传 xiaohuangfeng
   * @return check  true  校验成功   fasle  失败 message    返回原因
   */
  @RequestMapping(value = "/two-elements-check", method = RequestMethod.POST)
  public Map<String, Object> twoElementsCheck(String userName, String certId, String customKey) {

    String message = "success";
    boolean check= false;
    try {
      Map<String,Object> checkResult = usersAgreementService.checkUserNameAndCertId(userName, certId, null, customKey);
      int code =(int)checkResult.get("code");
      if (code==-1||code==1){
        check=true;
      }
    } catch (CheckUserNameCertIdCountException e) {
      logger.error(e.getMessage(), e);
      check = false;
      message = e.getRespmsg();
    }
    HashMap<String, Object> resultMap = new HashMap<>(5);
    resultMap.put("check", check);
    resultMap.put("message", message);
    return returnSuccess(resultMap);


  }

  @RequestMapping(value = "/resetPassword")
  public Map<String, Object> resetPassword(Integer userId) {

    if (userId == null) {
      return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
    }

    try {

      ChannelUser user = channelUserService.selectByUserId(userId);
      if (user == null) {
        return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
      }

      channelUserService
          .updatePassword(userId, CipherUtil.generatePassword("123456", user.getPhoneNo()));

      return returnSuccess();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
    }

  }


  @RequestMapping(value = "/export")
  public void export(HttpServletRequest request, HttpServletResponse response, String userName,
      String certId, String startTime, String endTime,
      String phoneNo, Integer state, Integer level, Integer type) {

    ChannelCustom loginUser = (ChannelCustom) request.getSession()
        .getAttribute(CommonString.CUSTOMLOGIN);

    if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil
        .isEmpty(loginUser.getMasterCustom())) {
      ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
      if (masterCustom != null) {
        loginUser = masterCustom;
      }
    }

    Map<String, Object> paramMap = new HashMap<>(16);

    paramMap.put("userName", userName);
    paramMap.put("certId", certId);
    paramMap.put("startTime", startTime);
    paramMap.put("endTime", endTime);
    paramMap.put("phoneNo", phoneNo);

    paramMap.put("state", state);
    paramMap.put("level", level);
    paramMap.put("type", type);

    if (!isRootAdmin(loginUser)) {
      if (loginUser.getCustomType() == CustomType.CUSTOM.getCode()) {
        paramMap.put("customKey", loginUser.getCustomkey());
      } else if (loginUser.getCustomType() == CustomType.GROUP.getCode()) {
        int nodeId = organizationTreeService
            .queryNodeIdByCustomKey(loginUser.getCustomkey());
        List<String> customKeys = organizationTreeService
            .queryNodeCusotmKey(CustomType.GROUP.getCode(),
                QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
        if (customKeys == null || customKeys.size() == 0) {
          return;
        }
        paramMap.put("customKey", String.join(",", customKeys));
      } else if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
        List<String> customKeys = channelRelatedService
            .queryCustomKeysByCompanyId(loginUser.getCustomkey());
        paramMap.put("customKey", String.join(",", customKeys));
      } else {
        return;
      }
    }

    List<Map<String, Object>> userList = channelUserService.getAllList(paramMap);

    String[] colunmName = new String[]{"用户姓名", "证件类型", "证件号", "状态", "绑定手机号", "类型", "商户名称", "备注描述",
        "用户创建时间", "认证级别", "最后更新时间", "操作账号"};
    String filename = "任务平台用户信息表";
    List<Map<String, Object>> data = new ArrayList<>();
    for (Map<String, Object> user : userList) {
      Map<String, Object> dataMap = new HashMap<>(20);
      dataMap.put("1", user.get("userName"));

      Integer certType = (Integer) user.get("certType");
      if (certType != null) {
        dataMap.put("2", CertType.codeOf(certType).getDesc());
      } else {
        dataMap.put("2", "无");
      }

      dataMap.put("3", user.get("certId"));

      int userState =
          user.get("state") == null ? 0 : Integer.parseInt(user.get("state").toString());

      dataMap.put("4", LittleBeeUserState.codeOf(userState).getDesc());
      dataMap.put("5", user.get("phoneNo"));

      Integer userType = (Integer) user.get("type");
      if (userType != null) {
        dataMap.put("6", LittleBeeUserType.codeOf(userType).getDesc());
      } else {
        dataMap.put("6", "未知类型");
      }
      dataMap.put("7", user.get("customName"));
      dataMap.put("8", user.get("remark"));
      dataMap.put("9", user.get("createTime"));

      Integer userLevel = (Integer) user.get("level");
      if (userType != null) {
        dataMap.put("10", LittleBeeUserLevel.codeOf(userLevel).getDesc());
      } else {
        dataMap.put("10", "level-0");
      }
      dataMap.put("11", user.get("lastUpdateTime"));
      dataMap.put("12", user.get("operatorName"));

      data.add(sortMapByKey(dataMap));
    }

    ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
  }
}
