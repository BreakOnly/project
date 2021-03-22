package com.jrmf.controller.systemrole.merchant.payment;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.controller.constant.LinkageSignType;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Joiner;
import com.jrmf.controller.constant.BatchLockStatus;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.domain.*;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.persistence.ChannelInterimBatch2Dao;
import com.jrmf.service.*;

import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.code.yanf4j.util.ConcurrentHashSet;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.controller.constant.TempStatus;
import com.jrmf.payment.service.ConfirmGrantService2;
import com.jrmf.persistence.CommissionTemporary2Dao;

/**
 * @author 张泽辉
 * @version V 1.0 : 平安银行下发模块
 */
@Controller
@RequestMapping("/merchant/paPay/new")
public class MerchantPAPaymentControllerNew extends BaseController {

  private static Logger logger = LoggerFactory.getLogger(MerchantPAPaymentControllerNew.class);
  @Autowired
  private UserCommissionService commissionService;
  @Autowired
  private ChannelCustomService customService;
  @Autowired
  private ChannelHistoryService channelHistoryService;
  @Autowired
  private ChannelRelatedService channelRelatedService;
  @Autowired
  private ChannelInterimBatchService2 interimBatchService2;
  @Autowired
  private ConfirmGrantService2 confirmGrantService;

  @Autowired
  private OrganizationTreeService organizationTreeService;
  @Autowired
  private CalculationFeeService calculationFeeService;
  @Autowired
  private CustomLimitConfService customLimitConfService;
  @Autowired
  private CustomCompanyRateConfService customCompanyRateConfService;
  @Autowired
  private CompanyService companyService;
  @Autowired
  private ChannelInterimBatch2Dao interimBatchDao2;
  @Autowired
  private YmyfCommonService ymyfCommonService;
  @Autowired
  private OrderNoUtil orderNoUtil;
  @Autowired
  private UtilCacheManager utilCacheManager;
  @Autowired
  private ChannelInterimBatchService2 channelInterimBatchService2;
  @Autowired
  private UsersAgreementService usersAgreementService;

  @Value("${payLockTime}")
  private Integer payLockTime;

  /**
   * 批次数据准备--临时批次查询
   */

  @RequestMapping(value = "/interimBatch", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> interimBatch(HttpServletRequest request,
      @RequestParam(required = false, defaultValue = "0") Integer nodeId,
      @RequestParam(required = false, defaultValue = "0") Integer customType,
      @RequestParam(defaultValue = "1", required = false) Integer pageNo,
      @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>(5);
    String customKeyStr = "";
    String customkey = request.getParameter("customkey");// 商户key
    List<String> customKeyList = organizationTreeService
        .queryNodeCusotmKey(customkey, customType, "G", nodeId);
    for (String ckey : customKeyList) {
      customKeyStr = customKeyStr + "," + ckey;
    }
    if (customKeyStr.lastIndexOf(",") >= 0) {
      customKeyStr = customKeyStr.substring(1);
    }

    String operatorName = null;
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if (customLogin.getLoginRole() == 2) {//如果为操作员，只能查看自己数据
      operatorName = customLogin.getUsername();
    }

    String menuId = request.getParameter("menuId");// 项目id
    String batchName = request.getParameter("batchName");// 批次名称
    String menuName = request.getParameter("menuName");// 项目名称
    String batchDesc = request.getParameter("batchDesc");// 批次说明
    String status = request.getParameter("status");// 批次状态
    String fileName = request.getParameter("fileName");// 批次文件名称
    String payType = request.getParameter("payType");// 下发通道
    String endTime = request.getParameter("endTime");// 结束时间
    String startTime = request.getParameter("startTime");// 开始时间
//        String pageNo = request.getParameter("pageNo");// 页码

    try {
      String menuIds = "";
      menuIds = getMenuIds(menuId, menuIds);
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("customkey", customKeyStr);
      paramMap.put("menuIds", menuIds);
      paramMap.put("batchName", batchName);
      paramMap.put("menuName", menuName);
      paramMap.put("batchDesc", batchDesc);
      paramMap.put("status", status);
      paramMap.put("fileName", fileName);
      paramMap.put("payType", payType);
      paramMap.put("startTime", startTime);
      paramMap.put("endTime", endTime);
      paramMap.put("operatorName", operatorName);

//            int total = interimBatchService2.getChannelInterimBatchByParam(paramMap).size();
//            int pageSize = 10;
//            if (!StringUtil.isEmpty(pageNo)) {
//                paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                paramMap.put("limit", pageSize);
//            }
      PageHelper.startPage(pageNo, pageSize);
      List<ChannelInterimBatch> list = interimBatchService2
          .getChannelInterimBatchByParam(paramMap);
      PageInfo<ChannelInterimBatch> pageInfo = new PageInfo<>(list);
      result.put("total", pageInfo.getTotal());
      result.put("list", pageInfo.getList());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      respstat = RespCode.error101;
      logger.error(e.getMessage());
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "查询失败");
      return result;
    }
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, "成功");
    logger.info("返回结果：" + result);
    return result;
  }

  private String getMenuIds(String menuId, String menuIds) {
    if (!StringUtil.isEmpty(menuId)) {
      Map<String, Object> param = new HashMap<String, Object>();
      CustomMenu customMenuById = customService.getCustomMenuById(Integer.parseInt(menuId));
      if (customMenuById == null) {
        menuIds = menuId;
      } else {
        param.put("levelCode", customMenuById.getLevelCode());
        List<CustomMenu> nodeTree = customService.getNodeTree(param);
        for (CustomMenu customMenu : nodeTree) {
          menuIds += customMenu.getId() + ",";
        }
      }
    }
    return menuIds;
  }

  /**
   * 批次提交后，查询最近一次提交后的批次信息，返回该批次名称和批次说明
   *
   * @param customKey 商户key
   * @param menuId 项目id
   * @param payType 支付类型
   * @param companyId 服务公司id
   */
  @RequestMapping(value = "/getBatchInfo", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> getBatchInfo(@RequestParam String customKey,
      @RequestParam String menuId, @RequestParam String payType,
      @RequestParam String companyId) {
    Map<String, Object> param = new HashMap<>(6);
    param.put("customKey", customKey);
    param.put("menuId", menuId);
    param.put("payType", payType);
    param.put("companyId", companyId);
    Map<String, Object> result = interimBatchService2.getBatchInfo(param);
    return returnSuccess(result);
  }

  /**
   * 批次准备--新增批次信息
   *
   * @return:
   */
  @RequestMapping(value = "/inputBatchInfo", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> inputBatchInfo(HttpServletRequest request, MultipartFile file) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>();

    String nodeCustomKey = request.getParameter("customkey");//根据组织树获取用户key
    String menuId = request.getParameter("menuId");// 项目id
    String payType = request.getParameter("payType");//下发通道
    String companyId = request.getParameter("companyId");//服务公司
    String realCompanyId = request.getParameter("realCompanyId");//服务公司
    String batchName = request.getParameter("batchName");// 批次名称
    String batchDesc = request.getParameter("batchDesc");// 批次说明
    String taskAttachmentFile = request.getParameter("taskAttachmentFile");// 批次任务附件  20190711
    if (StringUtil.isEmpty(companyId)) {
      return returnFail(RespCode.error101, RespCode.codeMaps.get(RespCode.error101));
    }
    ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
    String operatorName = loginUser.getUsername();// 操作人
    try {
      if (file != null) {
        result = customService
            .inputBatchInfoNew(respstat, operatorName, nodeCustomKey, menuId, payType, companyId,
                batchName, batchDesc, file.getInputStream(), file.getOriginalFilename(),
                taskAttachmentFile, result, realCompanyId);
      } else {
        respstat = RespCode.error101;
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "上传文件不能为空！");
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return result;
  }

  /**
   * 批次数据准备--删除临时批次
   */

  @RequestMapping(value = "/deleteBatchs", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> deleteBatchs(HttpServletRequest request,
      HttpServletResponse response) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>();

    String nodeCustomKey = "";
    String ids = request.getParameter("ids");// 批次ID集合
    if (StringUtil.isEmpty(ids)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "参数异常");
      return result;
    }

    ChannelInterimBatch batch = interimBatchService2.getChannelInterimBatchByOrderno(ids, "");
    if (batch == null || batch.getStatus() == 9) {
      return returnFail(RespCode.error101, RespCode.UNLOCK_ERROR);
    }

    try {
      // 删除批次信息 （业务删除）
      interimBatchService2.deleteByIds(ids, nodeCustomKey);
      // 删除批次对应的明细数据 （业务删除）
      interimBatchService2.deleteByBatchId(ids, nodeCustomKey);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "删除失败");
      return result;
    }
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, "成功");
    logger.info("返回结果：" + result);
    return result;
  }

  /**
   * 批次数据准备--批次明细列表
   */
  @RequestMapping(value = "/batchDetail", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> batchDetail(HttpServletRequest request, HttpServletResponse response,
      @RequestParam(defaultValue = "1", required = false) Integer pageNo,
      @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>();

    String batchId = request.getParameter("batchId");// 批次ID
    String userName = request.getParameter("userName");// 姓名
    String idCard = request.getParameter("idCard");// 身份证号
    String status = request.getParameter("status");// 状态
    String account = request.getParameter("account");// 账号
    String amount = request.getParameter("amount");
    amount = ArithmeticUtil.formatDecimals(amount);// 金额
    String payType = request.getParameter("payType");// 支付方式
    String menuId = request.getParameter("menuId");// 项目ID
//        String pageNo = request.getParameter("pageNo");// 页码
    if (StringUtil.isEmpty(batchId)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "参数异常！");
      return result;
    }
    try {
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("batchId", batchId);
      paramMap.put("userName", userName);
      paramMap.put("idCard", idCard);
      paramMap.put("status", status);
      paramMap.put("menuId", menuId);
      paramMap.put("account", account);
      paramMap.put("amount", amount);
      paramMap.put("payType", payType);

      PageHelper.startPage(pageNo, pageSize);
      List<CommissionTemporary> list = interimBatchService2.getCommissionedByParam(paramMap);
      PageInfo<CommissionTemporary> page = new PageInfo<>(list);
      result.put("total", page.getTotal());
      result.put("list", page.getList());
    } catch (Exception e) {
      logger.error("", e);
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "查询失败");
      return result;
    }
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, "成功");
    logger.info("返回结果：" + result);
    return result;
  }

  /**
   * 批次提交复核 说明:
   *
   * @return:
   */
  @RequestMapping(value = "/batchSubmitReview", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> batchSubmitReview(HttpServletRequest request, HttpServletResponse response) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>();
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    String orderNo = request.getParameter("orderNo");
    if (StringUtil.isEmpty(orderNo)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      return result;
    }
    try {
      interimBatchService2.submitReview(orderNo);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      respstat = RespCode.error107;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      return result;
    }
    return result;
  }

  @RequestMapping(value = "/batchReviewDialog", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> batchReviewDialog(HttpServletRequest request, HttpServletResponse response) {

    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>();
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));

    //orderNo
    String batchId = request.getParameter("batchId");
    ChannelCustom custom = (ChannelCustom) request.getSession().getAttribute("customLogin");

    ChannelInterimBatch batch = interimBatchService2
        .getChannelInterimBatchByOrderno(batchId, custom.getCustomkey());
    if (batch != null) {
      return result;
    } else {
      int reviewType = custom.getReviewType();
      if (reviewType == 3) {//判断当前登录用户是否是只读权限（ 查看本机构及以下机构）
        respstat = RespCode.error101;
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.REVIEW_SHOW_ONLY);
        return result;
      }
    }

    return result;
  }

  /**
   * 多批次审核
   *
   * @param batchIdStr 批次号list
   * @return 全部待审核  1
   */
  @RequestMapping(value = "/batchesReviewDialog", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> batchesReviewDialog(HttpServletRequest request,
      @RequestParam("batchIds") String batchIdStr) {

    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>(4);

    //orderNo
    ChannelCustom custom = (ChannelCustom) request.getSession().getAttribute("customLogin");
    String customKey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);

    int reviewType = custom.getReviewType();
//        查询nodeId
    int id = organizationTreeService.queryNodeIdByCustomKey(customKey);
//      3 复核本机构及查看以下机构
    if (reviewType == 3) {
      List<String> stringList = organizationTreeService
          .queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, id);

      stringList.removeIf(next -> next.equals(custom.getCustomkey()));

      String originalIds = Joiner.on(",").join(stringList);
      List<ChannelInterimBatch> channelInterimBatches = interimBatchService2
          .getChannelInterimBatchByOrdernos(batchIdStr, originalIds);
      if (channelInterimBatches.size() > 0) {
        //查询到下级机构的数据。权限不够。无法审批
        result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
        result.put(RespCode.RESP_MSG, RespCode.REVIEW_SHOW_ONLY);
        return result;
      }
      //当做只能 复核本机构
      reviewType = 1;
    }
//        1复核本机构
    if (reviewType == 1) {
      List<ChannelInterimBatch> channelInterimBatches = interimBatchService2
          .getChannelInterimBatchByOrdernos(batchIdStr, custom.getCustomkey());
      if (channelInterimBatches.size() != batchIdStr.split(",").length) {
        logger.error("请求条数：" + batchIdStr.split(",").length);
        logger.error("查询条数：" + channelInterimBatches.size());
        respstat = RespCode.NOT_ALL_BATCHES_ARE_PENDING_APPROVAL;
      }
    } else if (reviewType == 2) {
//        2 复核本机构及以下机构
      List<String> stringList = organizationTreeService
          .queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, id);
      String originalIds = Joiner.on(",").join(stringList);
      List<ChannelInterimBatch> channelInterimBatches = interimBatchService2
          .getChannelInterimBatchByOrdernos(batchIdStr, originalIds);
      if (channelInterimBatches.size() != batchIdStr.split(",").length) {
        logger.error("请求条数：" + batchIdStr.split(",").length);
        logger.error("查询条数：" + channelInterimBatches.size());
        respstat = RespCode.NOT_ALL_BATCHES_ARE_PENDING_APPROVAL;
      }
    } else {
      respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
    }

    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    return result;
  }


  /**
   * 批次复核 说明:
   *
   * @return:
   */
  @RequestMapping(value = "/batchReview", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> batchReview(HttpServletRequest request, HttpServletResponse response) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>();
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    String orderNo = request.getParameter("orderNo");
    String status = request.getParameter("status");//状态
    String reviewDesc = request.getParameter("reviewDesc");//复核意见

    ChannelInterimBatch tempBatch = interimBatchService2
        .getChannelInterimBatchByOrderno(orderNo, "");
    if (tempBatch == null) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "此批次已被执行打款或删除");
      return result;
    } else if (7 == tempBatch.getStatus() || 8 == tempBatch.getStatus()) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "此批次已被复核");
      return result;
    }

    if (StringUtil.isEmpty(orderNo) || StringUtil.isEmpty(status)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      return result;
    }

    try {
      ChannelCustom custom = (ChannelCustom) request.getSession().getAttribute("customLogin");
      Map<String, Object> param = new HashMap<>();
      param.put("orderNo", orderNo);
      param.put("status", status);
      param.put("reviewor", custom.getUsername());
      param.put("reviewDesc", reviewDesc);
      interimBatchService2.batchReview(param);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      respstat = RespCode.error107;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      return result;
    }
    return result;
  }

  /**
   * 批次复核 说明:
   */
  @RequestMapping(value = "/review/batchReview", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> batchReviewByBatchIds(HttpServletRequest request,
      @RequestParam("batchIds") String batchIdStr,
      @RequestParam("status") String status,
      @RequestParam(value = "reviewDesc", required = false, defaultValue = "") String reviewDesc
  ) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>(4);

    ChannelCustom custom = (ChannelCustom) request.getSession().getAttribute("customLogin");

    String[] split = batchIdStr.split(",");
    for (String orderNo : split) {
      Map<String, Object> param = new HashMap<>(8);
      param.put("orderNo", orderNo);
      param.put("status", status);
      param.put("reviewor", custom.getUsername());
      param.put("reviewDesc", reviewDesc);
      interimBatchService2.batchReview(param);
    }
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    return result;
  }

  /**
   * 批次状态查询 说明:
   *
   * @return:
   */
  @RequestMapping(value = "/batchStatus", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> batchStatus(HttpServletRequest request, HttpServletResponse response) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>();
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));

    String orderno = request.getParameter("orderNo");
    if (StringUtil.isEmpty(orderno)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      return result;
    }
    try {
      ChannelInterimBatch channelInterimBatch = interimBatchService2
          .getChannelInterimBatchByOrderno(orderno, "");
      result.put("channelInterimBatch", channelInterimBatch);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      respstat = RespCode.error107;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      return result;
    }
    return result;
  }

  /**
   * 批次数据复核----批次列表 说明:
   *
   * @return: 批次列表
   */
  @RequestMapping(value = "/queryReviewedBatch", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> queryReviewedBatch(HttpServletRequest request,
      @RequestParam(required = false, defaultValue = "0") Integer nodeId,
      @RequestParam(required = false, defaultValue = "0") Integer customType,
      @RequestParam(defaultValue = "1", required = false) Integer pageNo,
      @RequestParam(defaultValue = "10", required = false) Integer pageSize) {

    String queryMode = QueryType.QUERY_CURRENT_AND_CHILDREN;
    String customKey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
    ChannelCustom custom = customService.getCustomByCustomkey(customKey);
    if (custom.getReviewType() == 1) {
      queryMode = QueryType.QUERY_CURRENT;
    }

    List<String> customKeyList = organizationTreeService
        .queryNodeCusotmKey(customType, queryMode, nodeId);
    String customKeyStr = StringUtils.join(customKeyList, ",");

    String operatorName = null;
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    //如果为操作员，只能查看自己数据
    if (customLogin.getLoginRole() == 2) {
      operatorName = customLogin.getUsername();
    }

    String batchName = request.getParameter("batchName");//批次名称
    String batchDesc = request.getParameter("batchDesc");//批次描述
    String status = request.getParameter("status");//批次状态
    String startTime = request.getParameter("startTime");//创建时间起始
    String endTime = request.getParameter("endTime");//创建时间结束
    String payType = request.getParameter("payType");//下发通道
    String fileName = request.getParameter("fileName");//提交批次文件名称
    String menuId = request.getParameter("menuId");//项目id
//        String pageNo = request.getParameter("pageNo");//当前页码
//        String pageSize = request.getParameter("pageSize");//每页条数、
//        if (StringUtil.isEmpty(pageSize)) {
//            pageSize = "10";
//        }
    String menuIds = "";
    menuIds = getMenuIds(menuId, menuIds);
    Map<String, Object> param = new HashMap<>(20);
    param.put("customkey", customKeyStr);
    param.put("batchName", batchName);
    param.put("batchDesc", batchDesc);
    param.put("status", status);
    param.put("startTime", startTime);
    param.put("endTime", endTime);
    param.put("payType", payType);
    param.put("menuIds", menuIds);
    param.put("fileName", fileName);
    param.put("operatorName", operatorName);

//        int total = interimBatchService2.queryReviewedBatch(param).size();
//
//        param.put("start", (Integer.parseInt(pageNo) - 1) * Integer.parseInt(pageSize));
//        param.put("limit", Integer.parseInt(pageSize));
    PageHelper.startPage(pageNo, pageSize);
    List<ChannelInterimBatch> listChannelInterimBatch = interimBatchService2
        .queryReviewedBatch(param);
    PageInfo<ChannelInterimBatch> pageInfo = new PageInfo<>(listChannelInterimBatch);

    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>(8);
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    result.put("total", pageInfo.getTotal());
    result.put("listChannelInterimBatch", pageInfo.getList());
    return result;
  }

  /**
   * 批次数据复核----批次明细列表 说明:
   *
   * @return:
   */
  @RequestMapping(value = "/queryReviewedCommission", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> queryReviewedCommission(HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(defaultValue = "1", required = false) Integer pageNo,
      @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>();
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));

    String orderNo = request.getParameter("orderNo");//批次号
    String userName = request.getParameter("userName");//姓名
    String certId = request.getParameter("certId");//证件号
    String amount = request.getParameter("amount");//金额
    String status = request.getParameter("status");//订单状态
    String account = request.getParameter("account");//订单状态
//        String pageNo = request.getParameter("pageNo");//当前页码
    if (StringUtil.isEmpty(orderNo)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      return result;
    }
    try {
      Map<String, Object> param = new HashMap<>();
      param.put("customkey", "");
      param.put("batchId", orderNo);
      param.put("userName", userName);
      param.put("idCard", certId);
      param.put("amount", amount);
      param.put("account", account);
      param.put("status", status);
      PageHelper.startPage(pageNo, pageSize);
      List<CommissionTemporary> listCommissionTemporary = interimBatchService2
          .getCommissionedByParam(param);
      PageInfo<CommissionTemporary> pageInfo = new PageInfo<>(listCommissionTemporary);
      result.put("listCommissionTemporary", pageInfo.getList());
      result.put("total", pageInfo.getTotal());

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      respstat = RespCode.error107;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      return result;
    }
    return result;
  }

  /**
   * 批次数据复核----批次明细列表（多批次） 说明:
   *
   * @return:
   */
  @RequestMapping(value = "/batch/queryReviewedCommission", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> batchQueryReviewedCommission(HttpServletRequest request,
      @RequestParam("batchIds") String batchIds,
      @RequestParam(defaultValue = "1", required = false) Integer pageNo,
      @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>(16);
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    String customkey = (String) request.getSession().getAttribute("customkey");
    String userName = request.getParameter("userName");//姓名
    String certId = request.getParameter("certId");//证件号
    String amount = request.getParameter("amount");//金额
    String status = request.getParameter("status");//订单状态
    String account = request.getParameter("account");//订单状态
//        String pageNo = request.getParameter("pageNo");//当前页码
//        String pageSize = request.getParameter("pageSize");//每页条数
    //商户名称
    String customName = request.getParameter("customName");
    //批次名称
    String batchName = request.getParameter("batchName");
//        if (StringUtil.isEmpty(pageSize)) {
//            pageSize = "10";
//        }
    Map<String, Object> param = new HashMap<>(12);
    param.put("customkey", customkey);
    param.put("batchIds", batchIds);
    param.put("userName", userName);
    param.put("idCard", certId);
    param.put("amount", amount);
    param.put("account", account);
    param.put("status", status);
    param.put("customName", customName);
    param.put("batchName", batchName);
    List<CommissionTemporary> commissionedByBatchIdsAndParam = interimBatchService2
        .getCommissionedByBatchIdsAndParam(param);
//        int total = commissionedByBatchIdsAndParam.size();

//        param.put("start", (Integer.parseInt(pageNo) - 1) * Integer.parseInt(pageSize));
//        param.put("limit", Integer.parseInt(pageSize));
    PageHelper.startPage(pageNo, pageSize);
    List<CommissionTemporary> listCommissionTemporary = interimBatchService2
        .getCommissionedByBatchIdsAndParam(param);
    PageInfo<CommissionTemporary> pageInfo = new PageInfo<>(listCommissionTemporary);
    result.put("listCommissionTemporary", pageInfo.getList());
    result.put("total", pageInfo.getTotal());

    int successCount = 0;
    int failCount = 0;
    String successFee = "0.00";
    String successAmount = "0.00";
    String successSupplementFee = "0.00";
    for (CommissionTemporary commissionTemporary : commissionedByBatchIdsAndParam) {
      int status1 = commissionTemporary.getStatus();
      if (status1 == 1) {
        successCount++;
        //总金额
        String amount1 = commissionTemporary.getAmount();
        successAmount = ArithmeticUtil
            .formatDecimals(ArithmeticUtil.addStr(successAmount, amount1));
        //总服务费
        String fee = commissionTemporary.getSumFee();
        successFee = ArithmeticUtil.formatDecimals(ArithmeticUtil.addStr(successFee, fee));
        //补充服务费
        String getSupplementFee = commissionTemporary.getSupplementFee();
        successSupplementFee = ArithmeticUtil
            .formatDecimals(ArithmeticUtil.addStr(successSupplementFee, getSupplementFee));
      }
      if (status1 == 2) {
        failCount++;
      }
    }
    //成功条数
    result.put("successCount", successCount);
    //失败条数
    result.put("failCount", failCount);
    //下发金额
    result.put("successAmount", successAmount);
    //服务费
    result.put("successFee", successFee);
    //补差价
    result.put("successSupplementAmount", successSupplementFee);
    //应付金额
    result.put("payAmount",
        ArithmeticUtil.formatDecimals(ArithmeticUtil.addStr(successAmount, successFee)));
    return result;
  }

  /**
   * 批次数据提交--批次信息
   */

  @RequestMapping(value = "/batchData", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> batchData(HttpServletRequest request, HttpServletResponse response) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>();

    String batchId = request.getParameter("batchId");// 批次Id
    if (StringUtil.isEmpty(batchId)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "参数异常！");
      return result;
    }
    try {
      ChannelInterimBatch batch = interimBatchService2.getChannelInterimBatchByOrderno(batchId, "");

      String customKey = batch.getCustomkey();
      ChannelCustom custom = customService.getCustomByCustomkey(customKey);
      result.put("dataReview", custom.getDataReview());
      result.put("batchData", batch);
    } catch (Exception e) {
      logger.error("", e);
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, "操作失败");
      return result;
    }
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, "成功");
    return result;
  }


  /**
   * 打款前校验信息重复接口
   */
  @RequestMapping(value = "/commission/repeat", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> repeatCommission(HttpServletRequest request, String orderNo) {
    ChannelCustom loginUser = (ChannelCustom) request.getSession()
        .getAttribute(CommonString.CUSTOMLOGIN);
    if (StringUtil.isEmpty(loginUser.getTranPassword())) {
      return returnFail(RespCode.error101, RespCode.PASSWORD_DOES_NOT_SET);
    }
    Set<String> repeat = new HashSet<>();//用于获得交集
    Set<String> repeatCommission2 = new HashSet<>(); //存放交集
    List<CommissionTemporary> repeatCommission1 = new ArrayList<>();//返回前台的重复数据的list
    List<CommissionTemporary> commissions = interimBatchService2
        .getCommissionsByBatchId(orderNo, "");//查询的结果集
    //遍历结果集，如果放入set不成功就将重复数据放入交集中
    for (CommissionTemporary commission : commissions) {
      String payInfo =
          commission.getUserName() + commission.getBankCardNo() + commission.getAmount()
              + commission.getOriginalId();
      if (!repeat.add(payInfo)) {
        repeatCommission2.add(payInfo);
      }
    }
    //遍历交集从结果集中取所有重复对象
    for (String info : repeatCommission2) {
      for (CommissionTemporary commission : commissions) {
        String payInfo =
            commission.getUserName() + commission.getBankCardNo() + commission.getAmount()
                + commission.getOriginalId();
        if (info.equals(payInfo) && TempStatus.SUCCESS.getCode() == commission.getStatus()) {
          repeatCommission1.add(commission);
        }
      }
    }
    logger.info("导入的重复数据:list{}", repeatCommission1);
    return returnSuccess(repeatCommission1);
  }

  /**
   * 打款前设置重复校验状态的接口
   */
  @RequestMapping(value = "/commission/uncheck", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> notCheck(String ids) {
    String[] split = ids.split(",");
    Integer[] commIds = new Integer[split.length];
    for (int i = 0; i < split.length; i++) {
      commIds[i] = Integer.parseInt(split[i]);
    }
    int countCommission = interimBatchService2.updateCommToNotCheck(commIds);
    if (countCommission > 0) {
      return returnSuccess(null);
    }
    return returnFail(RespCode.error000, RespCode.codeMaps.get(RespCode.error000));
  }

  /**
   * 批次数据提交--打款
   */
  @RequestMapping(value = "/commonOption", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> bankPayCommonOption(HttpServletRequest request) {
    Map<String, Object> result;
    String batchId = request.getParameter("batchId");
    String companyId = request.getParameter("companyId");
    String tranPassword = request.getParameter("tranPassword");
    String remark = request.getParameter("remark");

    ChannelInterimBatch batch = interimBatchService2.getChannelInterimBatchByOrderno(batchId, "");
    if (batch == null || batch.getStatus() == 4) {
      return returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
    }

    if (BatchLockStatus.CONFIRMING.getCode() == batch.getLockStatus()
        || BatchLockStatus.CONFIRMSUCCESS.getCode() == batch.getLockStatus()
        || BatchLockStatus.CONFIRMUNKNOWNERROR.getCode() == batch.getLockStatus()) {
      return returnFail(RespCode.error101, "该批次验证码正在确认中，请勿重复操作！");
    }

    String key =
        "commonOption," + batchId + "," + batch.getCustomkey() + "," + batch.getRecCustomkey();

    String nodeCustomKey = batch.getCustomkey();
    //被操作人
    ChannelCustom channelCustom = customService.getCustomByCustomkey(nodeCustomKey);
    // 操作人
    ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

    if (!nodeCustomKey.equals(loginUser.getCustomkey())) {
      if (loginUser.getCustomType() == 5 && channelCustom.getTransferType() == 1) {
        return returnFail(RespCode.error101, "不允许为下级机构操作下发");
      }
    }

    String operatorName = loginUser.getUsername();
    ChannelRelated related = channelRelatedService
        .getRelatedByCompAndOrig(nodeCustomKey, companyId);
    if (related == null) {
      return returnFail(RespCode.error101, "服务公司被禁用，请联系管理员！");
    }

    //判断批次状态校验
    if (batch.getStatus() == 0) {
      return returnFail(RespCode.error101, "系统数据计算中，请稍后重试！");
    }

    String realCompanyId = channelInterimBatchService2.selectRealCompanyIdByBatchId(batchId);
    Company company = companyService.getCompanyByUserId(Integer.parseInt(companyId));
    if (StringUtil.isEmpty(realCompanyId)) {
      realCompanyId = company.getRealCompanyId();
    }

    Map processingMap = new HashMap();
    //非转包判断是否支付前签约，转包不管是否配置否是支付前签约
    if (companyId.equals(realCompanyId) && LinkageSignType.YES.getCode() == company
        .getLinkageSign()) {
      processingMap = usersAgreementService
          .getlinkageSignProcessingCount(batch.getOrderno(), batch.getCustomkey(),
              companyId);
    } else if (!companyId.equals(realCompanyId)) {

      CustomCompanyRateConf customCompanyMinRate = customCompanyRateConfService
          .getCustomCompanyMinRate(companyId, realCompanyId);

      if (customCompanyMinRate == null) {
        return returnFail(RespCode.error101, RespCode.COMPANY_NOT_RATE_CONFIG);
      }
      processingMap = usersAgreementService
          .getlinkageSignProcessingCount(batch.getOrderno(), companyId,
              realCompanyId);
    }
    if (processingMap != null && processingMap.size() > 0) {
      long processingCount = (long) processingMap.get("processCount");
      if (processingCount > 0) {
        return returnFail(RespCode.error101,
            "您好，系统该批次下发信息认证处理未完成，请稍等" + processingMap.get("leftTime") + "分钟后再确认支付");
      }
    }

    PaymentConfig paymentConfig = companyService
        .getPaymentConfigInfo(String.valueOf(batch.getPayType()), batch.getCustomkey(), companyId);
    PaymentConfig realPaymentConfig = companyService
        .getPaymentConfigInfo(String.valueOf(batch.getPayType()), batch.getCustomkey(),
            realCompanyId);
    if (paymentConfig == null || realPaymentConfig == null || StringUtil
        .isEmpty(paymentConfig.getPathNo()) || StringUtil.isEmpty(realPaymentConfig.getPathNo())) {
      return returnFail(RespCode.error101, "未配置下发通道路由,请联系管理员");
    }

    if (StringUtil.isEmpty(tranPassword) || StringUtil.isEmpty(loginUser.getTranPassword())) {
      return returnFail(RespCode.error101, RespCode.PASSWORD_DOES_NOT_SET);
    }
    if (StringUtil.isEmpty(batchId) || StringUtil.isEmpty(operatorName)) {
      return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
    } else {

      try {
        /**
         * 验证批次的状态是不是可以打款--该商户需要进行审核的情况下，批次状态等于7才可以打款
         */
        if (channelCustom.getDataReview() == 1) {
          ChannelInterimBatch channelInterimBatch = interimBatchService2
              .getChannelInterimBatchByOrderno(batchId, nodeCustomKey);
          if (channelInterimBatch.getStatus() != 7 && channelInterimBatch.getStatus() != 9) {
            return returnFail(RespCode.error101, "请审核通过后，再进行打款。");
          }
        }
        /**
         * 验证交易密码
         */
        if (channelCustom.getTranPassword() != null
            && !loginUser.getTranPassword().equals(CipherUtil.generatePassword(tranPassword,
            StringUtil.isEmpty(loginUser.getCustomkey()) ? loginUser.getMasterCustom()
                : loginUser.getCustomkey()))) {
          return returnFail(RespCode.error101, RespCode.PASSWORD_ERROR);
        }

        if (!utilCacheManager.lockWithTimeout(key, 1000, payLockTime)) {
          return returnFail(RespCode.error101, "请勿频繁操作，十分钟后再试!");
        }
        String userName = confirmGrantService.lockCommissionUsers(batchId);
        if (userName != null) {
          return returnFail(RespCode.error101, "该批次明细有在另外批次正在下发中的用户:" + userName);
        }
        result = confirmGrantService
            .grantTransfer(nodeCustomKey, companyId, batchId, remark, operatorName, realCompanyId);
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        return returnFail(RespCode.error101, RespCode.CONNECTION_ERROR);
      }
    }

    if (!String.valueOf(RespCode.success).equals(result.get(RespCode.RESP_STAT))) {
      utilCacheManager.remove(key);
    }

    confirmGrantService.unLockCommissionUsers(batchId);

    return result;
  }

  /**
   * 批次交易结果查询-批次
   */
  @RequestMapping(value = "/batchResultQuery", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> batchResultQuery(HttpServletRequest request,
      @RequestParam("pageNo") int pageNo,
      @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
      @RequestParam(defaultValue = "0") Integer nodeId,
      @RequestParam(defaultValue = "0") Integer customType) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>(5);
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));

    String customKeyStr = "";
    List<String> customKeyList = organizationTreeService
        .queryNodeCusotmKey(customType, "G", nodeId);
    for (String ckey : customKeyList) {
      customKeyStr = customKeyStr + "," + ckey;
    }
    if (customKeyStr.lastIndexOf(",") >= 0) {
      customKeyStr = customKeyStr.substring(1);
    }

    String operatorName = null;
    ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
    if (customLogin.getLoginRole() == 2) {//如果为操作员，只能查看自己数据
      operatorName = customLogin.getUsername();
    }

    String batchName = request.getParameter("batchName");
    String batchDesc = request.getParameter("batchDesc");
    String status = request.getParameter("status");
    String batchAmount = request.getParameter("amount");
    batchAmount = ArithmeticUtil.formatDecimals(batchAmount);
    String payType = request.getParameter("payType");
    String contentName = request.getParameter("contentName");
    String fileName = request.getParameter("fileName");
    String recCustomkey = request.getParameter("recCustomkey");
    String submitTimeStart = request.getParameter("submitTimeStart");
    String submitTimeEnd = request.getParameter("submitTimeEnd");
    String completeTimeStart = request.getParameter("completeTimeStart");
    String completeTimeEnd = request.getParameter("completeTimeEnd");
    String menuId = request.getParameter("menuId");
    String menuIds = "";
    menuIds = getMenuIds(menuId, menuIds);

    Map<String, Object> param = new HashMap<>(24);
    param.put("customkey", customKeyStr);
    param.put("batchName", !StringUtil.isEmpty(batchName) ? batchName : "");
    param.put("batchDesc", !StringUtil.isEmpty(batchDesc) ? batchDesc : "");
    param.put("status", !StringUtil.isEmpty(status) ? Integer.parseInt(status) : null);
    param.put("batchAmount", !StringUtil.isEmpty(batchAmount) ? batchAmount : "");
    param.put("payType", !StringUtil.isEmpty(payType) ? Integer.parseInt(payType) : null);
    param.put("contentName", !StringUtil.isEmpty(contentName) ? contentName : "");
    param.put("fileName", !StringUtil.isEmpty(fileName) ? fileName : "");
    param.put("recCustomkey", !StringUtil.isEmpty(recCustomkey) ? recCustomkey : "");
    param.put("submitTimeStart", !StringUtil.isEmpty(submitTimeStart) ? submitTimeStart : "");
    param.put("submitTimeEnd", !StringUtil.isEmpty(submitTimeEnd) ? submitTimeEnd : "");
    param.put("completeTimeStart", !StringUtil.isEmpty(completeTimeStart) ? completeTimeStart : "");
    param.put("completeTimeEnd", !StringUtil.isEmpty(completeTimeEnd) ? completeTimeEnd : "");
    param.put("menuIds", menuIds);
    param.put("operatorName", operatorName);

    PageHelper.startPage(pageNo, pageSize);
    List<Map<String, Object>> channelHistoryList = channelHistoryService
        .batchResultQuery(param);
    PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(channelHistoryList);
    result.put("channelHistoryList", pageInfo.getList());
    result.put("total", pageInfo.getTotal());
    return result;
  }

  /**
   * 批次交易结果查询-批次详情
   */
  @RequestMapping(value = "/commissionResultQuery", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> commissionResultQuery(HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(defaultValue = "1", required = false) Integer pageNo,
      @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
    int respstat = RespCode.success;
    Map<String, Object> result = new HashMap<>();
    result.put(RespCode.RESP_STAT, respstat);
    result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
    String batchId = request.getParameter("batchId");
    if (StringUtil.isEmpty(batchId)) {
      respstat = RespCode.error101;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      return result;
    }
    String userName = request.getParameter("userName");// 用户名
    String certId = request.getParameter("certId");// 身份证号
    String account = request.getParameter("account");// 收款账号
    String amount = request.getParameter("amount");
    amount = ArithmeticUtil.formatDecimals(amount);// 金额
    String status = request.getParameter("status");// 订单状态
    try {
      Map<String, Object> param = new HashMap<>();
      param.put("batchId", batchId);
      param.put("userName", StringUtil.isEmpty(userName) == false ? userName : "");
      param.put("certId", StringUtil.isEmpty(certId) == false ? certId : "");
      param.put("account", StringUtil.isEmpty(account) == false ? account : "");
      param.put("amount", StringUtil.isEmpty(amount) == false ? amount : "");
      param.put("status", StringUtil.isEmpty(status) == false ? Integer.parseInt(status) : null);
      PageHelper.startPage(pageNo, pageSize);
      List<UserCommission> commissionList = commissionService.commissionResultQuery(param);
      PageInfo<UserCommission> pageInfo = new PageInfo<>(commissionList);
      ChannelHistory batchData = channelHistoryService.getChannelHistoryById(batchId);
      result.put("batchData", batchData);
      result.put("commissionList", pageInfo.getList());
      result.put("total", pageInfo.getTotal());

    } catch (NumberFormatException e) {
      logger.error(e.getMessage(), e);
      respstat = RespCode.error107;
      result.put(RespCode.RESP_STAT, respstat);
      result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
      return result;
    }
    return result;
  }


  /**
   * 批次数据提交--校验验证码
   */
  @RequestMapping(value = "/checkCode", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> checkCode(HttpServletRequest request, String batchId, String reqId,
      String code) {

    ChannelInterimBatch batch = interimBatchService2.getChannelInterimBatchByOrderno(batchId, "");
    if (null == batch || !batch.getYmReqId().equals(reqId) || batch.getStatus() != 9) {
      return returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
    }

    if (BatchLockStatus.CONFIRMING.getCode() == batch.getLockStatus()
        || BatchLockStatus.CONFIRMSUCCESS.getCode() == batch.getLockStatus()
        || BatchLockStatus.CONFIRMUNKNOWNERROR.getCode() == batch.getLockStatus()) {
      return returnFail(RespCode.error101, "该批次验证码正在确认中，请勿重复操作！");
    }

    String customKey = batch.getCustomkey();
    String companyId = batch.getRecCustomkey();
    //被操作人
    ChannelCustom channelCustom = customService.getCustomByCustomkey(customKey);
    // 操作人
    ChannelCustom loginUser = (ChannelCustom) request.getSession()
        .getAttribute(CommonString.CUSTOMLOGIN);

    if (!customKey.equals(loginUser.getCustomkey())) {
      if (loginUser.getCustomType() == 5 && channelCustom.getTransferType() == 1) {
        return returnFail(RespCode.error101, "不允许为下级机构操作下发");
      }
    }

    String operatorName = loginUser.getUsername();
    ChannelRelated related = channelRelatedService
        .getRelatedByCompAndOrig(customKey, batch.getRecCustomkey());
    if (related == null) {
      return returnFail(RespCode.error101, "服务公司被禁用，请联系管理员！");
    }

    Company company = companyService.getCompanyByUserId(Integer.parseInt(companyId));
    //真实下发公司id
    String realCompanyId = company.getRealCompanyId();
    if (StringUtil.isEmpty(realCompanyId)) {
      realCompanyId = companyId;
    }

    PaymentConfig paymentConfig = companyService
        .getPaymentConfigInfo(String.valueOf(batch.getPayType()), batch.getCustomkey(),
            realCompanyId);
    if (paymentConfig == null) {
      return returnFail(RespCode.error101, "未配置下发通道路由,请联系管理员");
    }

    try {
      /**
       * 验证批次的状态是不是可以打款--该商户需要进行审核的情况下，批次状态等于7才可以打款
       * 删除复核校验，用户点击打款输入密码
       */
//            if (channelCustom.getDataReview() == 1) {
//                ChannelInterimBatch channelInterimBatch = interimBatchService2.getChannelInterimBatchByOrderno(batchId, customKey);
//                if (channelInterimBatch.getStatus() != 7) {
//                    return returnFail(RespCode.error101, "请审核通过后，再进行打款。");
//                }
//            }

      if (PaymentFactory.YMFWSPAY.equals(paymentConfig.getPathNo())) {

        String key = "ymCheckCode," + batchId + "," + reqId + "," + code;
        if (!utilCacheManager.lockWithTimeout(key, 1000, 86400000)) {
          return returnFail(RespCode.error101, "请勿重复填写验证码!");
        }

        batch.setRealCompanyId(realCompanyId);
        Map<String, Object> result = confirmGrantService
            .ymGrantTransferAfter(batch, code, operatorName, channelCustom.getCompanyName(),
                company.getCompanyName());
        return result;
      } else {
        return returnFail(RespCode.error101, "未配置正确的下发通道路由,请联系管理员");
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error101, RespCode.CONNECTION_ERROR);
    }
  }


  /**
   * 批次数据提交--重新获取验证码
   */
  @RequestMapping(value = "/reGetCode", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, Object> bankPayCommonOption(HttpServletRequest request, String batchId,
      String reqId) {
    Map<String, Object> result = new HashMap<>();

    ChannelInterimBatch batch = interimBatchService2.getChannelInterimBatchByOrderno(batchId, "");
    if (batch == null || !batch.getYmReqId().equals(reqId) || batch.getStatus() == 4) {
      return returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
    }

    if (BatchLockStatus.CONFIRMING.getCode() == batch.getLockStatus()
        || BatchLockStatus.CONFIRMSUCCESS.getCode() == batch.getLockStatus()
        || BatchLockStatus.CONFIRMUNKNOWNERROR.getCode() == batch.getLockStatus()) {
      return returnFail(RespCode.error101, "该批次验证码正在确认中，请勿重复操作！");
    }

    // 操作人
    ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

    Company company = companyService.getCompanyByUserId(Integer.parseInt(batch.getRecCustomkey()));
    //真实下发公司id
    String realCompanyId = company.getRealCompanyId();
    if (StringUtil.isEmpty(realCompanyId)) {
      realCompanyId = batch.getRecCustomkey();
    }

    PaymentConfig paymentConfig = companyService
        .getPaymentConfigInfo(String.valueOf(batch.getPayType()), batch.getCustomkey(),
            realCompanyId);
    if (paymentConfig == null) {
      return returnFail(RespCode.error101, "未配置下发通道路由,请联系管理员");
    }

    try {

      if (PaymentFactory.YMFWSPAY.equals(paymentConfig.getPathNo())) {

        batch.setRealCompanyId(realCompanyId);
        //溢美预下单
        Map<String, String> prePayResult = ymyfCommonService.prePay(batch, loginUser.getPhoneNo());
        logger.info("--------重新执行溢美服务商模式预下单返回结果:{}---------------", prePayResult);

        if (prePayResult.get("code").equals("0000")) {
          interimBatchDao2
              .updateBatchLockState(batch.getOrderno(), BatchLockStatus.GETSUCCESS.getCode());

          //预下单成功
          result.put("reqId", prePayResult.get("reqId"));
          return returnSuccess(result);
        } else {
          interimBatchDao2
              .updateBatchLockState(batch.getOrderno(), BatchLockStatus.GETFAILURE.getCode());
          return returnFail(RespCode.PRE_PAY_FAIL,
              RespCode.codeMaps.get(RespCode.PRE_PAY_FAIL) + prePayResult.get("msg"));
        }

      } else {
        return returnFail(RespCode.error101, "未配置正确的下发通道路由,请联系管理员");
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      interimBatchDao2.updateBatchLockState(batch.getOrderno(), BatchLockStatus.GETERROR.getCode());
      return returnFail(RespCode.error101, RespCode.CONNECTION_ERROR);
    }
  }

  /**
   * 批次数据准备--解锁临时批次
   */
  @RequestMapping(value = "/unLockBatch", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> unLockBatch(HttpServletRequest request, String batchId) {

    ChannelInterimBatch batch = interimBatchService2.getChannelInterimBatchByOrderno(batchId, "");
    if (batch == null || batch.getStatus() != 9) {
      return returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
    }

    if (BatchLockStatus.CONFIRMING.getCode() == batch.getLockStatus()
        || BatchLockStatus.CONFIRMSUCCESS.getCode() == batch.getLockStatus()
        || BatchLockStatus.CONFIRMUNKNOWNERROR.getCode() == batch.getLockStatus()) {
      return returnFail(RespCode.error101, "该批次验证码正在确认中，无法解锁！");
    }

    String key =
        "unLockBatch," + batchId + "," + batch.getCustomkey() + "," + batch.getRecCustomkey();

    if (!utilCacheManager.lockWithTimeout(key, 1000, 600000)) {
      return returnFail(RespCode.error101, "请勿频繁操作，十分钟后再试!");
    }

    // 操作人
    ChannelCustom loginUser = (ChannelCustom) request.getSession()
        .getAttribute(CommonString.CUSTOMLOGIN);

    try {
      Company company = companyService
          .getCompanyByUserId(Integer.parseInt(batch.getRecCustomkey()));
      //真实下发公司id
      String realCompanyId = company.getRealCompanyId();
      if (StringUtil.isEmpty(realCompanyId)) {
        realCompanyId = batch.getRecCustomkey();
      }

      batch.setRealCompanyId(realCompanyId);
      Map<String, Object> result = ymyfCommonService.smsPayResultQuery(batch, null);

      if ("1010".equals(result.get("code"))) {
        confirmGrantService.unlockBatch(batch);

      } else if ("0000".equals(result.get("code"))) {

        ChannelHistory history = channelHistoryService.getByOriginalBeachNo(batch.getOrderno());
        //溢美存在记录 正式表中没有记录，执行同步数据
        if (history == null) {

          String processId = (String) MDC.get(PROCESS);

          //被操作人
          ChannelCustom channelCustom = customService.getCustomByCustomkey(batch.getCustomkey());
//                    Company company = companyService.getCompanyByUserId(Integer.parseInt(batch.getRecCustomkey()));

          // 生成交易订单号（申请单号）
          String orderNo = orderNoUtil.getChannelSerialno();

          confirmGrantService
              .syncBatch(batch, orderNo, channelCustom.getCompanyName(), company.getCompanyName(),
                  loginUser.getUsername(), processId);

          result.clear();
          result.put("batchNo", orderNo);
          return returnSuccess(result);

        }
      } else {
        return returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
      }

      return returnSuccess();

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return returnFail(RespCode.error101, RespCode.CONNECTION_ERROR);
    }

  }

}
