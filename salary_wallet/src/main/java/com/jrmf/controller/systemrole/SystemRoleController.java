package com.jrmf.controller.systemrole;

import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.domain.*;
import com.jrmf.persistence.CustomGroupDao;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.persistence.NoticeDao;
import com.jrmf.service.*;
import com.jrmf.taxsettlement.api.APIDockingManager;
import com.jrmf.taxsettlement.api.TaxSettlementInertnessDataCache;
import com.jrmf.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * 用途： 作者：郭桐宁 时间：2018/12/28 14:31 Version:1.0
 *
 * @author guoto
 */
@Controller
@RequestMapping("/system/role")
public class SystemRoleController extends BaseController {

  private static final Logger logger = LoggerFactory.getLogger(SystemRoleController.class);
  private final OrganizationTreeService organizationTreeService;
  private final ChannelCustomService customService;
  private final CustomGroupDao customGroupDao;
  private final CustomProxyDao customProxyDao;
  private final APIDockingManager apiDockingManager;
  private final WebUserService userService;
  private final TaxSettlementInertnessDataCache taxSettlementInertnessDataCache;
  private final NoticeService noticeService;
  private final CustomNoticeService customNoticeService;

  @Autowired
  public SystemRoleController(OrganizationTreeService organizationTreeService,
      ChannelCustomService customService, CustomGroupDao customGroupDao,
      ChannelService channelService, CustomProxyDao customProxyDao,
      APIDockingManager apiDockingManager, UserSerivce userSerivce, WebUserService userService,
      TaxSettlementInertnessDataCache taxSettlementInertnessDataCache, NoticeService noticeService,
      CustomNoticeService customNoticeService) {
    this.organizationTreeService = organizationTreeService;
    this.customService = customService;
    this.customGroupDao = customGroupDao;
    this.customProxyDao = customProxyDao;
    this.apiDockingManager = apiDockingManager;
    this.userService = userService;
    this.taxSettlementInertnessDataCache = taxSettlementInertnessDataCache;
    this.noticeService = noticeService;
    this.customNoticeService = customNoticeService;
  }

  /**
   * Author Nicholas-Ning Description // TODO 加载系统登陆账号的组织机构树 Date 9:24 2019/1/3 Param [session,
   * queryMode, customType, nodeId] return java.util.Map<java.lang.String,java.lang.Object>
   **/
  @RequestMapping("/organization/tree")
  @ResponseBody
  public Map<String, Object> getSystemRoleTree(HttpSession session,
      @RequestParam(required = false, defaultValue = "NULL") String queryMode,
      @RequestParam(required = false) Integer customType,
      @RequestParam(required = false, defaultValue = "0") Integer nodeId,
      @RequestParam(required = false) String customKey) {
    if (customKey == null) {
      customKey = (String) session.getAttribute(CommonString.CUSTOMKEY);
    }
    ChannelCustom custom = customService.getCustomByCustomkey(customKey);
    //判断当前的节点是不是关联性代理商
    if (custom.getCustomType() == CustomType.PROXY.getCode() && custom.getProxyType() == 1
        && !"NULL".equals(queryMode)) {
      logger.info("当前点击商户{}是关联性代理商", custom.getCompanyName());
      customType = CustomType.PROXYCHILDEN.getCode();
    }
    if (customType == null) {
      customType = custom.getCustomType();
    }
    Integer platformId = checkCustom(custom);
    List<OrganizationNode> organizationNodes = organizationTreeService
        .queryOrganizationTree(customKey, customType, queryMode, nodeId, platformId);
    return returnSuccess(organizationNodes);
  }

  /**
   * 商户添加页面弹窗。 说明:
   *
   * @return:
   */
  @RequestMapping(value = "/organization/listNode", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> listNode(HttpSession session, Integer customType, Integer nodeId,
      @RequestParam(required = false) String customName,
      @RequestParam(required = false, defaultValue = "1") Integer pageNo,
      @RequestParam(required = false, defaultValue = "10") Integer pageSize,
      @RequestParam(required = false) String customKey) {
    List<OrganizationNode> nodes = new ArrayList<>();
    int total = 0;
    ChannelCustom custom = customService.getCustomByCustomkey(customKey);
    Integer platformId = checkCustom(custom);
    Map<String, Object> params = new HashMap<>(5);
    if (customType == CustomType.GROUP.getCode()) {
      OrganizationNode nodeById = customGroupDao.getNodeById(nodeId, platformId);
      params.put("customName", customName);
      params.put("nodeId", nodeId);
      params.put("customKey", customKey);
      params.put("levelCode", nodeById.getLevelCode());
      params.put("platformId", platformId);
      //  total = customGroupDao.queryNodeByParam(params).size();
//            params.put("start", (pageNo - 1) * pageSize);
//            params.put("limit", pageSize);
      nodes = customGroupDao.queryNodeByParam(params);
      total = nodes.size();
    } else if (customType == CustomType.PROXY.getCode()) {
      OrganizationNode nodeById = customProxyDao.getNodeById(nodeId, platformId);
      params.put("customName", customName);
      params.put("nodeId", nodeId);
      params.put("customKey", customKey);
      params.put("levelCode", nodeById.getLevelCode());
      params.put("customType", CustomType.PROXY.getCode());
      params.put("platformId", platformId);
      // total = customProxyDao.queryNodeByParam(params).size();
//            params.put("start", (pageNo - 1) * pageSize);
//            params.put("limit", pageSize);
      nodes = customProxyDao.queryNodeByParam(params);
      total = nodes.size();
    }
    return returnSuccess(nodes, total);
  }

  public int queryTotal(CustomGroupDao customGroupDao, String customName,
      Integer nodeId, String customKey, OrganizationNode nodeById) {
    Map<String, Object> params = new HashMap<>(5);
    params.put("customName", customName);
    params.put("nodeId", nodeId);
    params.put("customKey", customKey);
    params.put("levelCode", nodeById.getLevelCode());
    int total = customGroupDao.queryNodeByParam(params).size();
    return total;
  }


  /**
   * 批次数据复核----复核组织树 说明:
   *
   * @return:
   */
  @RequestMapping(value = "/organization/queryReviewedNode", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> queryReviewedNode(HttpSession session) {
    String queryMode;
    String customKey = (String) session.getAttribute(CommonString.CUSTOMKEY);
    ChannelCustom custom = customService.getCustomByCustomkey(customKey);
    int customType = custom.getCustomType();

    int reviewType = custom.getReviewType();
    if (custom.getCustomType() == CustomType.CUSTOM.getCode()) {
      queryMode = QueryType.NULL;
    } else {
      queryMode = QueryType.QUERY_CURRENT;
    }

    int nodeId = organizationTreeService.queryNodeIdByCustomKey(customKey);
    List<OrganizationNode> organizationNodes = organizationTreeService
        .queryOrganizationTree(customKey, customType, queryMode, nodeId, null);

    if (reviewType == 1) {
      organizationNodes.get(0).setHasChilden(-1);
    }

    return returnSuccess(organizationNodes);
  }


  /**
   * Author Nicholas-Ning Description //TODO 添加系统角色,普通商户,集团型商户,代理商. Date 10:42 2019/1/4 Param
   * [session, channelCustom, parentId] return java.util.Map<java.lang.String,java.lang.Object>
   **/
  @RequestMapping("/add")
  @ResponseBody
  public Map<String, Object> addSystemRole(HttpServletRequest request, HttpSession session,
      ChannelCustom channelCustom,
      @RequestParam(required = false) String parentCustomKey,
      @RequestParam(required = false) String proxyCustomKey) {
    ChannelCustom loginUser = (ChannelCustom) session.getAttribute("customLogin");
    if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser)) {
      return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
    }
    ChannelCustom existUser = customService
        .getUserByUserNameAndOemUrl(channelCustom.getUsername(), request.getServerName());
    if (existUser != null) {
      return returnFail(RespCode.error101, "用户名已占用");
    }
    boolean exist = userService.valiUserByColumn(channelCustom.getUsername());
    if (exist) {
      return returnFail(RespCode.error101, "该邮箱已经被注册");
    }
    logger.info("添加系统角色接口参数：channelCustom={} parentCustomKey={} proxyCustomKey={}", channelCustom,
        parentCustomKey, proxyCustomKey);
    //维护角色信息到channel_custom表中。
    String customKey = StringUtil.getStringRandom();
    String appSecret = UUID.randomUUID() + "";
    channelCustom.setCustomkey(customKey);
    channelCustom.setAddAccount(loginUser.getUsername());
    channelCustom.setAppSecret(appSecret);
    channelCustom.setLoginRole(1);
    channelCustom.setAgentId(proxyCustomKey);
    channelCustom.setMerchantName(channelCustom.getCompanyName());
    channelCustom.setPassword(CipherUtil.generatePassword(channelCustom.getPassword(), customKey));
    channelCustom.setEmail(channelCustom.getUsername());
    //添加的时候设置为不可用，待服务公司审核之后将custom信息设置为可用
    channelCustom.setEnabled(0);

    if (channelCustom.getCustomType() == CustomType.PROXY.getCode() && !StringUtil
        .isEmpty(proxyCustomKey)) {
      channelCustom.setProxyType(1);
      ChannelCustom custom = customService.getCustomByCustomkey(proxyCustomKey);
      custom.setProxyType(1);
      customService.updateCustomById(custom);
    }

    if (!StringUtil.isEmpty(proxyCustomKey)) {
      channelCustom.setBusinessChannel(proxyCustomKey);
    }

    customService.saveChannelCustom(channelCustom);
    //根据角色类型维护系统角色的树状关联关系
    //添加的是集团型商户
    if (channelCustom.getCustomType() == CustomType.GROUP.getCode()) {
      if (!StringUtil.isEmpty(parentCustomKey)) {
        organizationTreeService.addParentGroup(parentCustomKey, channelCustom.getCustomkey());
      } else {
        organizationTreeService.addGroup(channelCustom.getCustomkey());
      }
      if (!StringUtil.isEmpty(proxyCustomKey)) {
        organizationTreeService.addParentProxy(proxyCustomKey, channelCustom.getCustomkey());
      }
    } else if (channelCustom.getCustomType() == CustomType.PROXY.getCode()) {
      if (!StringUtil.isEmpty(proxyCustomKey)) {
        organizationTreeService.addParentProxyChilden(proxyCustomKey, channelCustom.getCustomkey());
      } else {
        organizationTreeService.addProxyChilden(channelCustom.getCustomkey());
      }
      organizationTreeService.addProxy(channelCustom.getCustomkey());
    } else if (channelCustom.getCustomType() == CustomType.CUSTOM.getCode()) {
      if (!StringUtil.isEmpty(proxyCustomKey)) {
        organizationTreeService.addParentProxy(proxyCustomKey, channelCustom.getCustomkey());
      }
    }
    //通知API端，系统新增了一个角色。
    apiDockingManager.addMerchantAPIDockingMode(customKey);
    this.insertCustomNotice(channelCustom.getCustomType(), customKey);
    return returnSuccess(null);
  }

  public void insertCustomNotice(int customType, String customKey) {
    //新增商户时将有关涉及公告同步
    //根据商户类型查询公告表是否有关自己的信息有则添加
    String type = "," + customType + ",";
    String id = "";
    List<Notice> notices = new ArrayList<>();
    if (CustomType.CUSTOM.getCode() == customType) {
      CustomOrganization customOrganization = noticeService.getCustomAdminByCustomType(customType);
      id = "," + customOrganization.getId() + ",";
      notices = noticeService.getCustomOrganizationByCustomTypeAndLoginRole(type, id);
    } else {
      notices = noticeService.getOrganizationByCustomType(type);
    }

    ChannelCustom custom = customService.getCustomByCustomkey(customKey);
    if (notices != null && notices.size() > 0) {
      for (Notice notice : notices) {
        CustomNotice customNotice = new CustomNotice();
        customNotice.setAccountId(custom.getId());
        customNotice.setNoticeId(notice.getId());
        customNotice.setReadIs(2);
        customNotice.setEnabled(1);
        customNotice.setCreateTime(DateUtils.getNowDate());
        customNoticeService.insertCustomNotice(customNotice);
      }
    }
  }

  @RequestMapping("/custom/get")
  public @ResponseBody
  Map<String, Object> getChannelCustom(String customKey) {
    Map<String, Object> custom = customService.getCustomByCustomkeyMap(customKey);
    OrganizationNode parent = customGroupDao.getNodeById(
        Integer.parseInt(custom.get("parentId") == null ? "0" : (String) custom.get("parentId")),
        null);
    OrganizationNode proxy = customProxyDao
        .getNodeById(custom.get("proxyId") == null ? 0 : (Integer) custom.get("proxyId"), null);
    String agentId = (String) custom.get("agentId");
    if (proxy == null && !StringUtil.isEmpty(agentId)) {
      proxy = customProxyDao.getNodeByCustomKey(agentId, null);
    }
    return returnSuccess(custom, parent, proxy);
  }

  @RequestMapping("/custom/update")
  public @ResponseBody
  Map<String, Object> updateChannelCustom(HttpServletRequest request, ChannelCustom channelCustom,
      @RequestParam(required = false) String parentCustomKey,
      @RequestParam(required = false) String proxyCustomKey) {

    if (StringUtil.isEmpty(channelCustom.getEmail())) {
      return returnFail(RespCode.error101, "请输入账号");
    } else {
      ChannelCustom custom = customService
          .getUserByUserNameAndOemUrl(channelCustom.getEmail(), request.getServerName());
      channelCustom.setUsername(channelCustom.getEmail());
      if (custom != null) {
        if (!channelCustom.getCustomkey().equals(custom.getCustomkey())) {
          return returnFail(RespCode.error101, "此账号已被占用");
        }
      }
    }

    if (!StringUtil.isEmpty(parentCustomKey)) {
      if (!organizationTreeService.addParentGroup(parentCustomKey, channelCustom.getCustomkey())) {
        return returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
      }
    } else {
      if (channelCustom.getCustomType() == 5) {
        organizationTreeService.removeParentGroup(channelCustom.getCustomkey());
      }
    }
    if (!StringUtil.isEmpty(proxyCustomKey)) {
      channelCustom.setBusinessChannel(proxyCustomKey);
      if (channelCustom.getCustomType() == CustomType.PROXY.getCode()) {
        if (!organizationTreeService
            .addParentProxyChilden(proxyCustomKey, channelCustom.getCustomkey())) {
          return returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
        }
      } else {
        if (!organizationTreeService.addParentProxy(proxyCustomKey, channelCustom.getCustomkey())) {
          return returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
        }
      }
      channelCustom.setAgentId(proxyCustomKey);
      channelCustom.setProxyType(1);

      ChannelCustom custom = customService.getCustomByCustomkey(proxyCustomKey);
      custom.setProxyType(1);
      customService.updateCustomById(custom);
    } else {
      channelCustom.setBusinessChannel("");
      organizationTreeService.removeParentProxyChilden(channelCustom.getCustomkey());
      organizationTreeService.removeParentProxy(channelCustom.getCustomkey());
    }
    customService.updateCustomById(channelCustom);
    customService.updateSubAccountPlatformId(channelCustom.getBusinessPlatformId(),
        channelCustom.getCustomkey());
    try {
      taxSettlementInertnessDataCache.init();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return returnSuccess(null);
  }

  @RequestMapping("/group/list")
  public @ResponseBody
  Map<String, Object> listGroupCustom(@RequestParam(required = false) String companyName,
      @RequestParam(defaultValue = "1") Integer pageNo,
      @RequestParam(defaultValue = "10") Integer pageSize,
      @RequestParam(defaultValue = "0") Integer nodeId,
      @RequestParam(defaultValue = "0") Integer customType,
      @RequestParam(required = false) String userName) {
    List<String> customKeyList = organizationTreeService
        .queryNodeCusotmKey(customType, QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
    StringBuilder stringBuffer = new StringBuilder();
    for (String customKey : customKeyList) {
      stringBuffer.append(customKey).append(",");
    }
    Map<String, Object> params = new HashMap<>(5);
    String customKeys = stringBuffer.toString();
    params.put("customKeys", customKeys);
    params.put("companyName", companyName);
    params.put("userName", userName);
    int total = customService.queryGroupInfoByParams(params).size();
    params.put("start", (pageNo - 1) * pageSize);
    params.put("limit", pageSize);
    List<Map<String, Object>> result = customService.queryGroupInfoByParams(params);
    return returnSuccess(result, total);
  }

  @RequestMapping("/proxy/list")
  public @ResponseBody
  Map<String, Object> listProxyCustom(@RequestParam(required = false) String companyName,
      @RequestParam(required = false, defaultValue = "1") Integer pageNo,
      @RequestParam(required = false, defaultValue = "10") Integer pageSize,
      @RequestParam(required = false, defaultValue = "0") Integer nodeId,
      @RequestParam(required = false, defaultValue = "0") Integer customType,
      @RequestParam(required = false) String userName) {
    List<String> customKeyList = organizationTreeService
        .queryNodeCusotmKey(customType, QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
    StringBuffer stringBuffer = new StringBuffer();
    for (String customKey : customKeyList) {
      stringBuffer.append(customKey).append(",");
    }

    Map<String, Object> params = new HashMap<>(5);
    String customKeys = stringBuffer.toString();
    params.put("customKeys", customKeys);
    params.put("companyName", companyName);
    params.put("userName", userName);
    int total = customService.queryProxyInfoByParams(params).size();
    params.put("start", (pageNo - 1) * pageSize);
    params.put("limit", pageSize);
    List<Map<String, Object>> result = customService.queryProxyInfoByParams(params);
    return returnSuccess(result, total);
  }

  @RequestMapping("/proxy/excel")
  public void listProxyCustom(HttpServletResponse response,
      @RequestParam(required = false) String companyName,
      @RequestParam(required = false, defaultValue = "0") Integer nodeId,
      @RequestParam(required = false, defaultValue = "0") Integer customType,
      @RequestParam(required = false) String userName) {
    List<String> customKeyList = organizationTreeService
        .queryNodeCusotmKey(customType, QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
    StringBuffer stringBuffer = new StringBuffer();
    for (String customKey : customKeyList) {
      stringBuffer.append(customKey).append(",");
    }

    Map<String, Object> params = new HashMap<>(5);
    String customKeys = stringBuffer.toString();
    params.put("customKeys", customKeys);
    params.put("companyName", companyName);
    params.put("userName", userName);
    List<Map<String, Object>> result = customService.queryProxyInfoByParams(params);
    String filename = "代理商列表统计";
    String[] colunmName = new String[]{"商户名称", "注册账号", "验证手机号", "商户类型", "创建时间"};
    List<Map<String, Object>> data = new ArrayList<>();
    for (Map<String, Object> stringObjectMap : result) {
      Map<String, Object> dataMap = new HashMap<>(15);
      dataMap.put("1", stringObjectMap.get("companyName"));
      dataMap.put("2", stringObjectMap.get("userName"));
      dataMap.put("3", stringObjectMap.get("phoneNo"));
      dataMap.put("4", stringObjectMap.get("customType"));
      dataMap.put("5", stringObjectMap.get("createTime"));
      data.add(sortMapByKey(dataMap));
    }
    ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
  }

  @RequestMapping("/group/excel")
  public void listGroupCustom(HttpServletResponse response,
      @RequestParam(required = false) String companyName,
      @RequestParam(required = false, defaultValue = "0") Integer nodeId,
      @RequestParam(required = false, defaultValue = "0") Integer customType,
      @RequestParam(required = false) String userName) {
    List<String> customKeyList = organizationTreeService
        .queryNodeCusotmKey(customType, QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
    StringBuffer stringBuffer = new StringBuffer();
    for (String customKey : customKeyList) {
      stringBuffer.append(customKey).append(",");
    }

    Map<String, Object> params = new HashMap<>(5);
    String customKeys = stringBuffer.toString();
    params.put("customKeys", customKeys);
    params.put("companyName", companyName);
    params.put("userName", userName);
    List<Map<String, Object>> result = customService.queryGroupInfoByParams(params);
    String filename = "集团型商户列表统计";
    String[] colunmName = new String[]{"商户名称", "注册账号", "验证手机号", "下发数据复核类型", "上级节点编号",
        "集团复核限制", "商户支付限制", "是否存在子节点", "创建时间"};
    List<Map<String, Object>> data = new ArrayList<>();
    for (Map<String, Object> stringObjectMap : result) {
      Map<String, Object> dataMap = new HashMap<>(15);
      dataMap.put("1", stringObjectMap.get("companyName"));
      dataMap.put("2", stringObjectMap.get("userName"));
      dataMap.put("3", stringObjectMap.get("phoneNo"));
      dataMap.put("4", stringObjectMap.get("dataReview"));
      dataMap.put("5", stringObjectMap.get("customKey"));
      dataMap.put("6", stringObjectMap.get("reviewType"));
      dataMap.put("7", stringObjectMap.get("transferType"));
      dataMap.put("8", stringObjectMap.get("hasChilden"));
      dataMap.put("9", stringObjectMap.get("createTime"));
      data.add(sortMapByKey(dataMap));
    }
    ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
  }


  @RequestMapping("/platform/list")
  @ResponseBody
  public Map<String, Object> selectPlatform(HttpServletRequest request) {

    ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute("customLogin");

    /**
    * @Description 机构账户
    **/
    if (channelCustom.getCustomType() == CustomType.ROOT.getCode()) {

      /**
       * @Description 获取主商户信息
       **/
      channelCustom = customService.getCustomByCustomkey(channelCustom.getMasterCustom());

    }
    /**
     * @Description 平台账户 只查自己
     **/
    if (channelCustom.getCustomType() == CustomType.PLATFORM.getCode()) {
      List<HashMap> list = new ArrayList<>();
      HashMap hashMap = customService.selectPlatformByCostomId(channelCustom.getId());
      list.add(hashMap);
      return returnSuccess(list);
    }

    /**
    * @Description 超管获取所有
    **/
    if(channelCustom.getCustomkey().equals(CommonString.ROOT)){

      List<HashMap> list = customService.selectPlatformList();
      return returnSuccess(list);
    }

    return returnFail(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT,RespCode.PERMISSION_ERROR);
  }

}
