package com.jrmf.controller.systemrole.merchant;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.CompanyType;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.FundModelType;
import com.jrmf.controller.constant.InvoiceOrderStatus;
import com.jrmf.controller.constant.LinkageType;
import com.jrmf.controller.constant.RechargeStatusType;
import com.jrmf.controller.constant.RechargeType;
import com.jrmf.controller.constant.ServiceFeeType;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.Channel;
import com.jrmf.domain.ChannelConfig;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.ChannelHistoryPic;
import com.jrmf.domain.ChannelPermission;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.Company;
import com.jrmf.domain.CustomCompanyRateConf;
import com.jrmf.domain.CustomInfo;
import com.jrmf.domain.CustomNotice;
import com.jrmf.domain.CustomOrganization;
import com.jrmf.domain.CustomReceiveConfig;
import com.jrmf.domain.LinkageBaseConfig;
import com.jrmf.domain.Notice;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.User;
import com.jrmf.domain.UserBatch;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.service.*;
import com.jrmf.splitorder.service.CustomSplitSuccessOrderService;
import com.jrmf.taxsettlement.api.APIDockingManager;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.CipherUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author guoto
 */
@Controller
@RequestMapping("/channelCustom")
public class ChannelController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(ChannelController.class);
    @Resource(name = "channelCustomService")
    ChannelCustomService channelCustomService;
    @Autowired
    CustomPermissionService customPermissionService;
    @Autowired
    WebUserService userService;
    @Autowired
    ChannelService channelService;
    @Autowired
    private OrderNoUtil orderNoUtil;
    @Autowired
    private ChannelHistoryService channelHistoryService;
    @Autowired
    private ChannelRelatedService channelRelatedService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private UserSerivce userSerivce;
    @Autowired
    private CustomBalanceService customBalanceService;
    @Autowired
    private APIDockingManager apiDockingManager;
    @Autowired
    private OrganizationTreeService organizationTreeService;
    @Autowired
    private CustomSplitSuccessOrderService customSplitSuccessOrderService;
    @Autowired
    private CustomCompanyRateConfService rateConfService;
    @Autowired
    private BestSignConfig bestSignConfig;
    @Autowired
    private CustomReceiveConfigService receiveConfigService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private CustomNoticeService customNoticeService;
    @Autowired
    private OemConfigService oemConfigService;
    @Autowired
    private LinkageTransferRecordService linkageTransferRecordService;
    @Autowired
    private LinkageCustomConfigService linkageCustomConfigService;
    @Autowired
    private CustomReceiveConfigService customReceiveConfigService;
    @Autowired
    private ChannelConfigService channelConfigService;


    /**
     * 添加商户
     *
     * @param request
     * @param custom
     * @return:
     */
    @RequestMapping(value = "/catalog/channel/saveChannelCustom")
    public @ResponseBody
    Map<String, Object> saveChannelCustom(HttpServletRequest request, ChannelCustom custom) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(10);
        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (channelCustom.getCustomType() != 3 && !"mfkj".equals(channelCustom.getCustomkey())
                && !"mfkj".equals(channelCustom.getMasterCustom())) {
            return retModelMsg(RespCode.error101, "不允许此操作", result);
        }
        ChannelCustom existUser = channelCustomService.getUserByUserNameAndOemUrl(custom.getUsername(), request.getServerName());
        if (existUser != null) {
            return retModelMsg(RespCode.error101, "用户名已占用", result);
        }
        boolean exist = this.userService.valiUserByColumn(custom.getUsername());
        if (exist) {
            return retModelMsg(RespCode.error101, "该邮箱已经被注册", result);
        }
        String partnerId = StringUtil.getStringRandom();
        String appSecret = UUID.randomUUID() + "";
        custom.setPassword(CipherUtil.generatePassword(custom.getPassword(), partnerId));
        custom.setEmail(custom.getUsername());
        custom.setCustomkey(partnerId);
        custom.setAppSecret(appSecret);
        custom.setEnabled(0);
        /**
         * 1 商户 2 服务公司 3 代理商
         */
        if (custom.getCustomType() == CustomType.CUSTOM.getCode()) {
            Channel channel = new Channel();
            channel.setAppSecret(appSecret);
            channel.setOriginalId(partnerId);
            channel.setOriginalName(custom.getCompanyName());
            channelService.addChannel(channel);
        }
        apiDockingManager.addMerchantAPIDockingMode(custom.getCustomkey());
        channelCustom.setAddAccount(channelCustom.getUsername());
        channelCustomService.saveChannelCustom(custom);
        return retModel(respstat, result);
    }

    /**
     * 公司账户信息概览
     */
    @RequestMapping(value = "/company/account", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyAccount(HttpServletRequest request, HttpServletResponse response) {
        // 商户标识
        String originalId = (String) request.getSession().getAttribute("customkey");
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(5);
        String startTime = (String) request.getParameter("startTime");
        String endTime = (String) request.getParameter("endTime");
        String companyId = (String) request.getParameter("companyId");
        logger.info("/company/account方法  传参： originalId=" + originalId);
        if (StringUtil.isEmpty(originalId)) {
            return retModel(RespCode.error101, result);
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<>(5);
                paramMap.put("customkey", originalId);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                List<ChannelHistory> list = channelHistoryService.getChannelHistoryBySubmit(paramMap);
                int total = list == null ? 0 : list.size();
                result.put("total", total);
                ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(originalId, companyId);
                ChannelRelated hsBankAccount = new ChannelRelated();
                // 徽商电子户
                String balance = channelHistoryService.getBalance(originalId, related.getCompanyId(), "1");
                hsBankAccount.setBalance(balance);
                hsBankAccount.setCompanyName("银行电子户");

                ChannelRelated aLiAccount = new ChannelRelated();
                // 支付宝账户
                String aliBalance = channelHistoryService.getBalance(originalId, related.getCompanyId(), "2");
                aLiAccount.setBalance(aliBalance);
                aLiAccount.setCompanyName("支付宝账户");
                result.put("hsBankAccount", hsBankAccount);
                result.put("aLiAccount", aLiAccount);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", result);
            }
        }
        Map<String, Object> model = retModel(respstat, result);
        logger.info("返回结果：" + model);
        return model;
    }

    /**
     * 公司账户信息列表
     *
     * @return
     */
    @RequestMapping(value = "/company/accountList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyAccountList(HttpServletRequest request,
                                           HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        String originalId = (String) request.getSession().getAttribute("customkey");// 商户标识
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();
        String startTime = (String) request.getParameter("startTime");
        String endTime = (String) request.getParameter("endTime");
        String amount = (String) request.getParameter("amount");
        String payType = (String) request.getParameter("payType");
//        String pageNo = (String) request.getParameter("pageNo");
//        String pageSize = request.getParameter("pageSize") == null ? "10" : (String) request.getParameter("pageSize");
        logger.info("/company/accountList方法  传参： originalId=" + originalId);
        if (StringUtil.isEmpty(originalId)) {
            return retModel(RespCode.error101, result);
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("customkey", originalId);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                paramMap.put("amount", amount);
                paramMap.put("payType", payType);
//                int total = channelHistoryService.getChannelHistoryBySubmit(paramMap).size();
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * Integer.parseInt(pageSize));
//                    paramMap.put("limit", Integer.parseInt(pageSize));
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<ChannelHistory> list = channelHistoryService.getChannelHistoryBySubmit(paramMap);
                PageInfo<ChannelHistory> pageInfo = new PageInfo<>(list);
                result.put("list", pageInfo.getList());
                result.put("total", pageInfo.getTotal());
                result.put("pageNo", pageNo);
                result.put("pageSize", pageSize);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", result);
            }
        }
        return retModel(respstat, result);
    }

    /**
     * 账号列表 说明:
     *
     * @param request
     * @param model
     * @return:
     */
    @RequestMapping(value = "/channelUser/getChannelUserList")
    public @ResponseBody
    Map<String, Object> getChannelUserCount(
            HttpServletRequest request, ModelMap model,
            @RequestParam(required = false, defaultValue = "0") Integer nodeId,
            @RequestParam(required = false, defaultValue = "0") Integer customType,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;

        HashMap<String, Object> result = new HashMap<>();

        // 调用根据当前节点customKey获取key集合，形成字符串
        String customKeyStr = "";
        if (customType != 2 && customType != 3) {
            List<String> customKeyList = organizationTreeService.queryNodeCusotmKey(customType, "G", nodeId);
            for (String ckey : customKeyList) {
                customKeyStr = customKeyStr + "," + ckey;
            }
            if (customKeyStr.lastIndexOf(",") >= 0) {
                customKeyStr = customKeyStr.substring(1);
            }
        } else {
            customKeyStr = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
        }

        String name = request.getParameter("name");
        String loginName = request.getParameter("loginName");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
//        String pageNo = request.getParameter("pageNo");
//        String pageSize = request.getParameter("pageSize") == null ? "10" : request.getParameter("pageSize");
        logger.info("/channelUser/getChannelUserList方法  传参： nodeId=" + nodeId
                + "--customType=" + customType + "--customKeyStr="
                + customKeyStr);

        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("name", name);
            paramMap.put("loginName", loginName);
            paramMap.put("startTime", startTime);
            paramMap.put("endTime", endTime);
            paramMap.put("masterCustom", customKeyStr);
            if (customLogin.getLoginRole() == 2) {
                paramMap.put("loginRole", customLogin.getLoginRole());
            }

//            int total = channelCustomService.getCustomList(paramMap).size();
//            if (!StringUtil.isEmpty(pageNo)) {
//                paramMap.put("start",
//                        (Integer.parseInt(pageNo) - 1) * Integer.parseInt(pageSize));
//                paramMap.put("limit", Integer.parseInt(pageSize));
//            }
            PageHelper.startPage(pageNo, pageSize);
            List<ChannelCustom> list = channelCustomService.getCustomList(paramMap);
            PageInfo<ChannelCustom> pageInfo = new PageInfo<>(list);
            result.put("list", pageInfo.getList());
            result.put("total", pageInfo.getTotal());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", result);
        }

        return retModel(respstat, result);
    }


    /**
     * 添加管理员 说明:
     *
     * @param request
     * @param custom
     * @return:
     */
    @RequestMapping(value = "/channelUser/saveChannelUser")
    public @ResponseBody
    Map<String, Object> saveChannelUser(HttpServletRequest request, ChannelCustom custom) {

        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();

        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");

        if (custom.getLoginRole() == 1 && customLogin.getLoginRole() == 2) {
            return retModelMsg(RespCode.error101, "操作员身份不能创建管理员账号", result);
        }
        String customKey = request.getParameter("customkey");

        if (custom.getId() == 0) {//id==0则为新增

            ChannelCustom existUser = channelCustomService.getUserByUserNameAndOemUrl(custom.getUsername(), request.getServerName());

            ChannelCustom channelCustom = channelCustomService.getCustomByCustomkey(customKey);

            if (existUser != null) {
                return retModelMsg(RespCode.error101, "用户账户/邮箱已被占用", result);
            }

            ChannelCustom customNew = new ChannelCustom();

            /**
            * @Description 添加所属业务平台
            **/
            customNew.setBusinessPlatform(customLogin.getBusinessPlatform());
            customNew.setBusinessPlatformId(customLogin.getBusinessPlatformId());


            customNew.setUsername(custom.getUsername());
            customNew.setCompanyName(custom.getCompanyName());
            customNew.setPhoneNo(custom.getPhoneNo());
            customNew.setPassword(CipherUtil.generatePassword(custom.getPassword(), customKey));
            customNew.setCustomType(4);
            customNew.setMasterCustom(customKey);
            customNew.setEnabled(1);
            customNew.setTranPassword(channelCustom.getTranPassword());
            customNew.setDataReview(channelCustom.getDataReview());
            customNew.setLoginRole(custom.getLoginRole());
            customNew.setIsReviewTransfer(channelCustom.getIsReviewTransfer());
            customNew.setReviewType(channelCustom.getReviewType());
            customNew.setTransferType(channelCustom.getTransferType());
            channelCustom.setAddAccount(customLogin.getUsername());

            channelCustomService.saveChannelCustom(customNew);
            // 如果为机构登陆账户创建账号，则获取它的上级账户
            ChannelCustom login = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
            if (CustomType.ROOT.getCode() == customLogin.getCustomType() && !StringUtil.isEmpty(customLogin.getMasterCustom())) {
                login = customService.getCustomByCustomkey(customLogin.getMasterCustom());
            }

            CustomOrganization customOrganization = noticeService.getCustomOrganizationByCustomType(login.getCustomType());
            String id = "";
            String customType = "," + login.getCustomType() + ",";
            List<Notice> notices = new ArrayList<>();
            if (CustomType.CUSTOM.getCode() == login.getCustomType()) {
                List<CustomOrganization> list = noticeService.getCustomOrganizationByParentId(customOrganization.getId());
                for (CustomOrganization c : list) {
                    if (c.getLoginRole() == custom.getLoginRole()) {
                        id = "," + c.getId() + ",";
                    }
                }
                notices = noticeService.getCustomOrganizationByCustomTypeAndLoginRole(customType, id);
            } else if (CustomType.PROXY.getCode() == login.getCustomType()) {
                int count = oemConfigService.getOemConfigByCustomkey(login.getCustomkey());
                List<CustomOrganization> list = noticeService.getCustomOrganizationByParentId(customOrganization.getId());

                for (CustomOrganization c : list) {
                    if (count > 0 && c.getLoginRole() == 2) {
                        id = "," + c.getId() + ",";
                    } else if (count == 0 && c.getLoginRole() == 1) {
                        id = "," + c.getId() + ",";
                    }
                }
                notices = noticeService.getCustomOrganizationByCustomTypeAndLoginRole(customType, id);
            } else {
                notices = noticeService.getOrganizationByCustomType(customType);
            }

            notices.stream()
                    .filter(n -> n != null)
                    .forEach(n -> {
                        CustomNotice customNotice = new CustomNotice();
                        customNotice.setAccountId(customNew.getId());
                        customNotice.setNoticeId(n.getId());
                        customNotice.setReadIs(2);
                        customNotice.setCreateTime(DateUtils.getNowDate());
                        customNoticeService.insertCustomNotice(customNotice);
                    });

        } else {//id != 0则为更新
            ChannelCustom customUpdate = new ChannelCustom();
            customUpdate.setId(custom.getId());
            /**
             * @Description 添加所属业务平台
             **/
            customUpdate.setBusinessPlatform(customLogin.getBusinessPlatform());
            customUpdate.setBusinessPlatformId(customLogin.getBusinessPlatformId());

            customUpdate.setUsername(custom.getUsername());
            customUpdate.setCompanyName(custom.getCompanyName());
            customUpdate.setPhoneNo(custom.getPhoneNo());
            customUpdate.setPassword(CipherUtil.generatePassword(custom.getPassword(), custom.getMasterCustom()));
            customUpdate.setLoginRole(custom.getLoginRole());
            channelCustomService.updateCustomById(customUpdate);
        }
        return retModel(respstat, result);
    }

    /**
     * 公司账户线下充值
     *
     * @return
     */
    @RequestMapping(value = "/company/rechange", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyRechange(HttpServletRequest reques,
                                        HttpSession session,
                                        String amount,
                                        String rechargeAmount,
                                        Integer payType,
                                        String payAccountNo,
                                        String payAccountBankName,
                                        String inAccountNo,
                                        String inAccountName,
                                        String inAccountBankName,
                                        String customKey,
                                        String companyId,
                                        String forwardCompanyName,
                                        Integer serviceFeeType,
                                        Integer rechargeType,
                                        Integer rateId,
                                        Integer rechargeConfirmType,
                                        Integer channelHistoryId,
                                        MultipartFile[] rechargeFile,
                                        String linkPhone) {

        if (rechargeType == null || (rechargeType == RechargeType.AMOUNT.getCode() && StringUtil.isEmpty(amount))
                || (rechargeType == RechargeType.SERVICEAMOUNT.getCode() && StringUtil.isEmpty(rechargeAmount)) || payType == null || StringUtil.isEmpty(customKey)
                || StringUtil.isEmpty(companyId)) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }
        
        if (rechargeConfirmType == null){
            return returnFail(RespCode.error101, "服务公司未配置收款账户！");
        }

        if (rechargeType == RechargeType.AMOUNT.getCode() && ArithmeticUtil.compareTod(amount, "0") <= 0) {
            return returnFail(RespCode.error124, RespCode.AMOUNT_ERROR);
        }

        if (rechargeType == RechargeType.SERVICEAMOUNT.getCode() && ArithmeticUtil.compareTod(rechargeAmount, "0") <= 0) {
            return returnFail(RespCode.error124, RespCode.AMOUNT_ERROR);
        }

        if (rechargeType == RechargeType.AMOUNT.getCode() && serviceFeeType != null && serviceFeeType == ServiceFeeType.RECHARGE.getCode() && rateId == null) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        if (!StringUtil.isEmpty(linkPhone) && !StringUtil.isMobileNO(linkPhone)) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        Map<String, Object> params = new HashMap<>(2);
        params.put("customkey", customKey);
        params.put("channelId", companyId);
        params.put("companyId", companyId);
        params.put("payType", payType);
        List<CustomReceiveConfig> receiveConfigList = receiveConfigService.queryRechargeAccountListNoPape(params);
        if (receiveConfigList != null && receiveConfigList.size() > 0) {
            CustomReceiveConfig receiveConfig = receiveConfigList.get(0);
            if (receiveConfig.getStatus() != null && receiveConfig.getStatus() == 2) {
                return returnFail(RespCode.error101, "服务公司该收款账户充值限制，请联系客户经理");
            }
        } else {
            List<ChannelConfig> channelConfigList = channelCustomService.getChannelConfigListByParam(params);
            if (channelConfigList != null && channelConfigList.size() > 0) {
                ChannelConfig channelConfig = channelConfigList.get(0);
                if (channelConfig.getStatus() != null && channelConfig.getStatus() == 2) {
                    return returnFail(RespCode.error101, "服务公司该收款账户充值限制，请联系客户经理");
                }
            } else {
                return returnFail(RespCode.error101, "收款账户未配置");
            }
        }
        ChannelCustom loginUser = (ChannelCustom) session.getAttribute(CommonString.CUSTOMLOGIN);
        ChannelRelated chargeInfo = channelRelatedService.getRelatedByCompAndOrig(customKey, companyId);
        if (chargeInfo == null || loginUser == null) {
            return returnFail(RespCode.error101, RespCode.RELATIONSHIP_DOES_NOT_EXIST);
        }

        InputStream in;
        try {
            //这里判断商户的资金联动类型，如果是联动充值就自动转账
            ChannelCustom custom = channelCustomService.getCustomByCustomkey(customKey);

            ChannelHistory chargeDetail = new ChannelHistory();
            chargeDetail.setStatus(0);
            chargeDetail.setCustomkey(customKey);
            chargeDetail.setTransfertype(1);

            LinkageBaseConfig baseConfig = linkageCustomConfigService.getConfigByCustomKey(custom.getCustomkey(), LinkageType.RECHARGENO.getCode());
            if (FundModelType.RECHARGE.getCode() == custom.getFundModelType() && baseConfig != null && !StringUtil.isEmpty(baseConfig.getPathNo())) {

                Company company = companyService.getCompanyByCompanyName(inAccountName);
                if (company == null) {
                    return returnFail(RespCode.error101, RespCode.IN_ACCOUNT_NAME_ERROR);
                }

                PaymentConfig paymentConfig = new PaymentConfig(baseConfig);
                //调用支付通道工厂模式
                Payment payment = PaymentFactory.paymentEntity(paymentConfig);
                //不使用交易方法，不传递UtilCacheManager
                PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, null);
                Payment proxy = paymentProxy.getProxy();

                String balance = String.valueOf(proxy.queryBalanceResult(String.valueOf(payType)).getAttachment());
                if (ArithmeticUtil.compareTod(rechargeAmount, balance) > 0){
                    return returnFail(RespCode.error101, RespCode.PATH_BALANCE_ERROR);
                }
                chargeDetail.setStatus(RechargeStatusType.NORECHARGE.getCode());
            }


            if (rechargeType == RechargeType.AMOUNT.getCode()) {  //余额充值
                chargeDetail.setServiceFeeType(serviceFeeType);
                if (serviceFeeType == ServiceFeeType.RECHARGE.getCode()) { //充值预扣收方式
                    logger.info("——————————————开始计算充值预扣收方式服务费————————————");
                    CustomCompanyRateConf rateConf = rateConfService.getById(rateId);
                    if (rateConf == null) {
                        logger.error("————————————未获取到充值预扣收方式服务费规则——————————");
                        return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
                    }

                    BigDecimal b1 = new BigDecimal(ArithmeticUtil.getFormatDouble(amount));
                    BigDecimal b2 = new BigDecimal(ArithmeticUtil.getFormatDouble(rateConf.getCustomRate()));
                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setGroupingUsed(false);
                    String serviceFee;

                    //服务费计算规则为1  服务费=交易金额*服务费率
                    if (rateConf.getFeeRuleType() == 1) {
                        serviceFee = nf.format(b1.multiply(b2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    } else if (rateConf.getFeeRuleType() == 2) {  //服务费计算规则为2 服务费=交易金额/(1-服务费率)*服务费率
                        serviceFee = nf.format(b1.divide(new BigDecimal(1).subtract(b2), 4, BigDecimal.ROUND_HALF_UP).multiply(b2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    } else {
                        return returnFail(RespCode.error101, RespCode.FEE_RULE_TYEP_ERROR);
                    }

                    chargeDetail.setServiceFeeRate(rateConf.getCustomRate());
                    chargeDetail.setServiceFee(serviceFee);
                    chargeDetail.setAmount(amount);
                    chargeDetail.setRechargeAmount(ArithmeticUtil.addStr(amount, serviceFee));

                    Company company = companyService.getCompanyByUserId(Integer.parseInt(companyId));
                    if (CompanyType.SUBCONTRACT.getCode() == company.getCompanyType()) {
                        chargeDetail.setForwardCommissionAmount(amount);
                        //如果是转包服务公司，从 custom_company_rate_conf 表中获取转包服务公司费率
                        List<CustomCompanyRateConf> customCompanyRateConfs = rateConfService.getConfByCustomKey(companyId);
                        if (customCompanyRateConfs == null || customCompanyRateConfs.isEmpty()){
                            return returnFail(RespCode.error101, RespCode.COMPANY_NOT_RATE_CONFIG);
                        }
                        boolean companyRateFlag = true;
                        String companyRate0 = customCompanyRateConfs.get(0).getCustomRate();
                        for (CustomCompanyRateConf companyRate : customCompanyRateConfs) {
                            if (ArithmeticUtil.compareTod(companyRate0,companyRate.getCustomRate()) != 0) {
                                companyRateFlag = false;
                                break;
                            }
                        }
                        if (companyRateFlag){
                            b2 = new BigDecimal(ArithmeticUtil.getFormatDouble(companyRate0));
                            String realCompanyAmount = nf.format(b1.multiply(b2).add(b1).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            chargeDetail.setRealCompanyAmount(realCompanyAmount);
                        }
                    }

                    logger.info("充值金额：{},服务费计算规则：{},服务费率：{},服务费：{},到账可用余额：{}", chargeDetail.getRechargeAmount(),rateConf.getFeeRuleType(), rateConf.getCustomRate(), chargeDetail.getServiceFee(), chargeDetail.getAmount());

                } else if (serviceFeeType == ServiceFeeType.ISSUE.getCode()) { //下发实时扣收
                    chargeDetail.setServiceFeeRate("0");
                    chargeDetail.setServiceFee("0");
                    chargeDetail.setAmount(amount);
                    chargeDetail.setRechargeAmount(amount);
                    Company company = companyService.getCompanyByUserId(Integer.parseInt(companyId));
                    if (CompanyType.SUBCONTRACT.getCode() == company.getCompanyType()){
                        CustomCompanyRateConf customCompanyMinRate = rateConfService.getCustomCompanyMinRate(customKey, companyId);
                        chargeDetail.setForwardCommissionAmount(ArithmeticUtil.divideStr(amount,ArithmeticUtil.addStr2("1",customCompanyMinRate.getCustomRate())));
                        //计算转包服务公司转账金额
                        calculateRealCompanyAmount(amount, customKey, companyId, chargeDetail);
                    }
                } else if (serviceFeeType == ServiceFeeType.PERSON.getCode()){ //下发实时扣收个人承担
                    chargeDetail.setServiceFeeRate("0");
                    chargeDetail.setServiceFee("0");
                    chargeDetail.setAmount(amount);
                    chargeDetail.setRechargeAmount(amount);
                    Company company = companyService.getCompanyByUserId(Integer.parseInt(companyId));
                    if (CompanyType.SUBCONTRACT.getCode() == company.getCompanyType()){
                        CustomCompanyRateConf customCompanyMinRate = rateConfService.getCustomCompanyMinRate(customKey, companyId);
                        chargeDetail.setForwardCommissionAmount(ArithmeticUtil.divideStr(amount,ArithmeticUtil.addStr2("1",customCompanyMinRate.getCustomRate())));
                        //计算转包服务公司转账金额
                        calculateRealCompanyAmount(amount, customKey, companyId, chargeDetail);
                    }
                }
            } else if (rechargeType == RechargeType.SERVICEAMOUNT.getCode()) {  //补服务费
                chargeDetail.setRechargeAmount(rechargeAmount);
                chargeDetail.setServiceFeeRate("0");
                chargeDetail.setServiceFee("0");
                chargeDetail.setAmount("0");
            }

            chargeDetail.setAccountNo(payAccountNo);
            chargeDetail.setAccountName(payAccountBankName);
            chargeDetail.setRecCustomkey(chargeInfo.getCompanyId());
            chargeDetail.setPayType(payType);
            chargeDetail.setInAccountNo(inAccountNo);
            chargeDetail.setInAccountName(inAccountName);
            chargeDetail.setInAccountBankName(inAccountBankName);
            chargeDetail.setRechargeType(rechargeType);
            chargeDetail.setRechargeConfirmType(rechargeConfirmType);
            chargeDetail.setInvoiceStatus(InvoiceOrderStatus.NO_TYPE.getCode());
            chargeDetail.setInvoiceAmount("0");
            chargeDetail.setInvoiceingAmount("0");
            chargeDetail.setUnInvoiceAmount(chargeDetail.getRechargeAmount());
            chargeDetail.setOrderno(orderNoUtil.getChannelSerialno());
            chargeDetail.setOperatorName(loginUser.getUsername());
            chargeDetail.setOrdername(CommonString.ORDERNAME);
            chargeDetail.setLinkPhone(linkPhone);
            chargeDetail.setOemUrl(reques.getServerName());
            chargeDetail.setRechargeFileNum(0);
            chargeDetail.setForwardCompanyName(forwardCompanyName);
            channelHistoryService.addChannelHistory(chargeDetail);
            if (channelHistoryId != null && channelHistoryId > 0){
                ChannelHistory channelHistory = channelHistoryService.getChannelHistoryById(String.valueOf(channelHistoryId));
                if (channelHistory != null){
                    String forwardRechargeRecordIds = channelHistory.getForwardRechargeRecordIds();
                    if (StringUtil.isEmpty(forwardRechargeRecordIds)){
                        forwardRechargeRecordIds = String.valueOf(chargeDetail.getId());
                    }else{
                        forwardRechargeRecordIds = forwardRechargeRecordIds + "," + chargeDetail.getId();
                    }
                    channelHistory.setForwardRechargeRecordIds(forwardRechargeRecordIds);
                    channelHistoryService.updateChannelHistory(channelHistory);
                }
            }

            ChannelCustom customLogin = (ChannelCustom) reques.getSession().getAttribute("customLogin");
            uploadRechargeFile(rechargeFile, chargeDetail.getOrderno(), customLogin.getUsername());


            if (FundModelType.RECHARGE.getCode() == custom.getFundModelType() && baseConfig != null && !StringUtil.isEmpty(baseConfig.getPathNo())) {
                //创建联动充值记录并调账
                ThreadUtil.subAccountThreadPool.execute(() -> linkageTransferRecordService.addRechargeLinkageTransfer(chargeDetail.getOrderno()));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }

        return returnSuccess();
    }

    private void calculateRealCompanyAmount(String amount, String customKey, String companyId, ChannelHistory chargeDetail) {
        //下发公司转账金额 = 充值金额 / (1 + 商户费率) * (1 + 转包服务公司费率)
        //商户费率
        List<CustomCompanyRateConf> customRates = rateConfService.getConfsByCustomKeyAndCompanyId(customKey,companyId);
        boolean customRateFlag = !CollectionUtils.isEmpty(customRates);
        if (customRateFlag){
            String customRate0 = customRates.get(0).getCustomRate();
            for (CustomCompanyRateConf customRate : customRates) {
                if (ArithmeticUtil.compareTod(customRate0,customRate.getCustomRate()) != 0) {
                    customRateFlag = false;
                    break;
                }
            }
        }
        //转包服务公司费率
        List<CustomCompanyRateConf> companyRates = rateConfService.getConfByCustomKey(companyId);
        boolean companyRateFlag = !CollectionUtils.isEmpty(companyRates);
        if (companyRateFlag){
            String companyRate0 = companyRates.get(0).getCustomRate();
            for (CustomCompanyRateConf companyRate : companyRates) {
                if (ArithmeticUtil.compareTod(companyRate0,companyRate.getCustomRate()) != 0) {
                    companyRateFlag = false;
                    break;
                }
            }
        }
        if (customRateFlag && companyRateFlag){
            String forwardCommissionAmount = ArithmeticUtil.divideStr(amount, ArithmeticUtil.addStr2("1", customRates.get(0).getCustomRate()));
            String realCompanyAmount = ArithmeticUtil.mulStr(forwardCommissionAmount, ArithmeticUtil.addStr2("1", companyRates.get(0).getCustomRate()),2);
            chargeDetail.setRealCompanyAmount(realCompanyAmount);
        }
    }

    private void uploadRechargeFile(MultipartFile[] rechargeFile, String orderNo, String userName)
        throws IOException {
        if (rechargeFile != null && rechargeFile.length > 0) {
            for (MultipartFile file : rechargeFile) {
                InputStream in = new ByteArrayInputStream(file.getBytes());
                //使用UUID图片重命名
                String name = UUID.randomUUID().toString().replaceAll("-", "");
                //获取文件扩展名
                String ext = FilenameUtils.getExtension(file.getOriginalFilename());
                //设置文件上传路径
                String fileName = name + "." + ext;
                String uploadFile = FtpTool
                    .uploadFile(bestSignConfig.getFtpURL(), 21, "/rechargeFile/", fileName, in,
                        bestSignConfig.getUsername(), bestSignConfig.getPassword());
                if (!"error".equals(uploadFile)) {
                    ChannelHistoryPic channelHistoryPic = new ChannelHistoryPic();
                    channelHistoryPic.setOrderNo(orderNo);
                    channelHistoryPic.setRechargeFile("/rechargeFile/" + fileName);
                    channelHistoryPic.setAddUser(userName);
                    channelHistoryService.insertChannelHistoryPic(channelHistoryPic);
                    channelHistoryService.updateChannelHistoryFileNumAddByOrderNo(orderNo);
                }
            }
        }
    }

    /**
     * 获取权限 说明:
     *
     * @param model
     * @param request
     * @param id
     * @return:
     */
    @RequestMapping(value = "/channelUser/getChannelUserPermission")
    public @ResponseBody
    Map<String, Object> getCustomPermission(Model model, HttpServletRequest request, String id) {
        int respstat = RespCode.success;
        HashMap<String, Object> result = new HashMap<String, Object>();

        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");// 渠道名称

        String customKey = (String) request.getSession().getAttribute("customkey");// 渠道

        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        List<ChannelPermission> allPermission = new ArrayList<ChannelPermission>();
        if ("mfkj".equals(customKey)) {
            allPermission = customPermissionService.getAllPermission();
        } else {
            allPermission = customPermissionService.getCustomPermission(customLogin.getId());
        }
        List<ChannelPermission> customPermission = customPermissionService.getCustomPermission(Integer.parseInt(id));
        for (int i = 0; i < allPermission.size(); i++) {
            ChannelPermission e = allPermission.get(i);
            // 除魔方科技以外的其它公司不能给别人分配添加管理员相关的功能，目前把不能分配的功能写死
            if (!"mfkj".equals(customKey)) {
                if (!"/custom/catalog/channelUser/getChannelUser.do".equals(e.getLink())
                        || !"/custom/catalog/channelUser/addChannelUser.do".equals(e.getLink())) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("id", e.getId());
                    map.put("pId", e.getParentId());
                    map.put("name", e.getContentName());
                    for (ChannelPermission permission : customPermission) {
                        if (permission.getId() == e.getId()) {
                            map.put("checked", true);
                        }
                    }
                    mapList.add(map);
                }
            } else {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", e.getId());
                map.put("pId", e.getParentId());
                map.put("name", e.getContentName());
                for (ChannelPermission permission : customPermission) {
                    if (permission.getId() == e.getId()) {
                        map.put("checked", true);
                    }
                }
                mapList.add(map);
            }
        }
        result.put("list", mapList);
        return retModel(respstat, result);
    }

    /**
     * 删除管理员 说明:
     *
     * @param request
     * @param id
     * @return:
     */
    @RequestMapping(value = "/channelUser/delChannelUser")
    public @ResponseBody
    Map<String, Object> delChannelUser(HttpServletRequest request, String id) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();
        channelCustomService.deleteById(id);
        return retModel(respstat, result);
    }

    /**
     * 启用停用管理员 说明:
     *
     * @param request
     * @param id
     * @param currStatus
     * @return:
     */
    @RequestMapping(value = "/channelUser/editChannelUserStatus")
    public @ResponseBody
    Map<String, Object> editChannelUserStatus(HttpServletRequest request, String id,
                                              String currStatus) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();
        String newStatus = ("1".equals(currStatus)) ? "0" : "1";
        channelCustomService.enabledCustom(Integer.parseInt(id), newStatus);
        return retModel(respstat, result);
    }

    /**
     * 重置管理员密码 说明:
     *
     * @param request
     * @param id
     * @param password
     * @return:
     */
    @RequestMapping(value = "/channelUser/resetChannelUserPwd")
    public @ResponseBody
    Map<String, Object> editChannelUserPwd(HttpServletRequest request, String id,
                                           String password) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();
        String customkey = (String) request.getSession().getAttribute("customkey");
        logger.info("customkey:" + customkey + "id:" + id + "password" + password);
        password = CipherUtil.generatePassword(password, customkey);
        channelCustomService.updatePassword(Integer.parseInt(id), password);
        return retModel(respstat, result);
    }

    /**
     * 薪税钱包配置——配置商户服务公司——商户服务公司列表
     */
    @RequestMapping(value = "/companyListData")
    public @ResponseBody
    Map<String, Object> companyListData(HttpServletRequest request,
                                        @RequestParam(value = "customName", required = false) String customName,
                                        @RequestParam(value = "startTime", required = false) String startTime,
                                        @RequestParam(value = "endTime", required = false) String endTime,
                                        @RequestParam(value = "customType", required = false) String customType,
                                        @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                        @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(6);

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }

        Map<String, Object> paramMap = new HashMap<>(8);
        paramMap.put("customType", customType);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("name", customName);
        PageHelper.startPage(pageNo,pageSize);
        List<ChannelCustom> list = channelCustomService.getListCustom(paramMap);
        PageInfo<ChannelCustom> pageInfo = new PageInfo<>(list);

        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        return retModel(respstat, result);
    }

    /**
     * 薪税钱包配置——配置商户服务公司——商户服务公司列表
     */
    @RequestMapping(value = "/newCompanyListData")
    public @ResponseBody
    Map<String, Object> newCompanyListData(HttpServletRequest request,
                                        @RequestParam(value = "customName", required = false) String customName,
                                        @RequestParam(value = "startTime", required = false) String startTime,
                                        @RequestParam(value = "endTime", required = false) String endTime,
                                        @RequestParam(value = "customType", required = false) String customType,
                                        @RequestParam(value = "businessPlatformId", required = false) Integer businessPlatformId,
                                        @RequestParam(value = "customKey", required = false) String customKey,
                                        @RequestParam(value = "enabled", required = false) Integer enabled,
                                        @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                        @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo) {

        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(6);
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        //查询权限：超管、平台和转包服务公司
        if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser) && !isForwardCompany(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }
        //如果是平台商户，获取 businessPlatformId
        if (isPlatformAccount(loginUser)){
            businessPlatformId = loginUser.getBusinessPlatformId();
        }
        Map<String, Object> paramMap = new HashMap<>(8);
        //如果是转包服务公司，获取该服务公司下的商户
        if (isForwardCompany(loginUser)){
            String companyId = loginUser.getCustomType() == CustomType.ROOT.getCode() ? loginUser.getMasterCustom() : loginUser.getCustomkey();
            List<String> customKeys = channelRelatedService.queryCustomKeysByCompanyId(companyId);
            if (customKeys != null && customKeys.size() > 0){
                paramMap.put("customKeys", Joiner.on(",").join(customKeys));
            }
        }
        if (!isAdmin(loginUser)) {//如果是操作员，添加查询条件
            paramMap.put("addAccount", loginUser.getUsername());
        }
        paramMap.put("customType", customType);
        paramMap.put("customkey", customKey);
        paramMap.put("enabled", enabled);
        paramMap.put("businessPlatformId", businessPlatformId);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("name", customName);
        if (pageSize == 0){//如果传递 0 查所有
            pageSize = Integer.MAX_VALUE;
        }
        PageHelper.startPage(pageNo,pageSize);
        List<ChannelCustom> list = channelCustomService.getListCustom(paramMap);
        if (list != null && list.size() > 0){
            for (ChannelCustom channelCustom : list) {
                channelCustom.setPassword("123456");
            }
        }
        PageInfo<ChannelCustom> pageInfo = new PageInfo<>(list);

        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        return retModel(respstat, result);
    }

    /**
     * 薪税钱包配置——配置商户服务公司——商户服务公司列表
     */
    @RequestMapping(value = "/customAccountInfos")
    public @ResponseBody
    Map<String,Object> customAccountInfos(HttpServletRequest request,
                                           @RequestParam(value = "customKey", required = false) String customKey) {

        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>(8);
        //查询商户下配置的服务公司收款账户
        paramMap.put("customkey", customKey);
        paramMap.put("status", 1);
        List<CustomReceiveConfig> customReceiveConfigs = customReceiveConfigService.queryRechargeAccountListNoPape(paramMap);
        List<Map<String,String>> accountInfos = new ArrayList<>();
        if (customReceiveConfigs != null &&customReceiveConfigs.size() >0){
            for (CustomReceiveConfig customReceiveConfig : customReceiveConfigs) {
                Map<String,String> accountInfo = new HashMap<>();
                accountInfo.put("accountName",customReceiveConfig.getReceiveUser());
                accountInfo.put("bankName",customReceiveConfig.getReceiveBank());
                accountInfo.put("accountNum",customReceiveConfig.getReceiveAccount());
                accountInfos.add(accountInfo);
            }
        }else{
            //获取商户配置服务公司下的默认银行账户
            //获取商户关联的服务公司
            List<ChannelRelated> channelRelateds = channelRelatedService.queryRelatedList(paramMap);
            if (channelRelateds != null && channelRelateds.size() > 0){
                for (ChannelRelated channelRelated : channelRelateds) {
                    //获取服务公司的银行账户
                    String companyId = channelRelated.getCompanyId();
                    paramMap.put("channelId",companyId);
                    List<ChannelConfig> channelConfigs = channelConfigService.queryChannelConfig(paramMap);
                    if (channelConfigs == null || channelConfigs.size() == 0){
                        continue;
                    }
                    for (ChannelConfig channelConfig : channelConfigs) {
                        //相同的银行卡不重复添加
                        boolean bankExist = false;
                        for (Map<String, String> info : accountInfos) {
                            String accountNum = info.get("accountNum");
                            if (accountNum.equals(channelConfig.getAccountNum())){
                                bankExist = true;
                                break;
                            }
                        }
                        if (!bankExist){
                            Map<String,String> accountInfo = new HashMap<>();
                            accountInfo.put("accountName",channelConfig.getAccountName());
                            accountInfo.put("bankName",channelConfig.getBankName());
                            accountInfo.put("accountNum",channelConfig.getAccountNum());
                            accountInfos.add(accountInfo);
                        }
                    }
                }
            }
        }
        result.put("accountInfos", accountInfos);
        return retModel(respstat, result);
    }

    /**
     * 商户关联的薪税服务公司列表
     */
    @RequestMapping(value = "/originalForList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> originalForList(HttpServletRequest request,
                                        HttpServletResponse response) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String originalId = (String) request.getParameter("originalId");
        logger.info("/originalForList 方法  传参： originalId=" + originalId);
        try {
            if (StringUtil.isEmpty(originalId)) {
                return retModelMsg(RespCode.error107, "启用失败，配置不存在！", model);
            }
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("originalId", originalId);
            List<ChannelRelated> list = channelRelatedService.getRelatedByParam(param);
            List<ChannelRelated> relateds = new ArrayList<ChannelRelated>();
            for (ChannelRelated channelRelated : list) {
                ChannelRelated related = muStr(channelRelated);
                relateds.add(related);
            }
            model.put("total", relateds == null ? 0 : relateds.size());
            model.put("list", relateds);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", model);
        }
        Map<String, Object> result = retModel(respstat, model);
        logger.info("返回结果：" + model);
        return result;
    }

    public ChannelRelated muStr(ChannelRelated related) {
        String serviceRate = ArithmeticUtil.mulStr(related.getServiceRates(), "100", 3);
        String upperServiceRates = ArithmeticUtil.mulStr(related.getUpperServiceRates(), "100", 3);
        String profiltUpper = ArithmeticUtil.mulStr(related.getProfiltUpper(), "100", 3);
        String profiltLower = ArithmeticUtil.mulStr(related.getProfiltLower(), "100", 3);
        related.setServiceRates(serviceRate);
        related.setProfiltLower(profiltLower);
        related.setProfiltUpper(profiltUpper);
        related.setUpperServiceRates(upperServiceRates);
        return related;
    }

    /**
     * 配置商户的薪资服务公司--薪税平台列表
     */
    @RequestMapping(value = "/merchantList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> merchantList(HttpServletRequest request,
                                     HttpServletResponse response) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            List<CustomInfo> list = customInfoService.getAllActiveCustom();
            model.put("list", list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", model);
        }
        Map<String, Object> result = retModel(respstat, model);
        logger.info("返回结果：" + result);
        return result;
    }

    /**
     * 配置商户的薪资服务公司--薪资服务公司列表
     */
    @RequestMapping(value = "/companyList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyListBychannelId(HttpServletRequest request,
                                               HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String merchantId = (String) request.getParameter("merchantId");
        String userName = (String) request.getParameter("userName");
//        String pageNo = (String) request.getParameter("pageNo");
//        String pageSize = request.getParameter("pageSize") == null ? "10" : (String) request.getParameter("pageSize");

        logger.info("/companyList 方法  传参： customkey=" + merchantId);
        if (StringUtil.isEmpty(merchantId)) {
            return retModelMsg(RespCode.error107, "参数异常，请联系管理员！", model);
        }
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("merchantId", merchantId);
            params.put("userName", userName);
//			List<User> list = userSerivce.getCompanyByMerchantId(merchantId);
//            int total = userSerivce.getUserBatchByParam(params).size();
//            if (!StringUtil.isEmpty(pageNo)) {
//                params.put("start", (Integer.parseInt(pageNo) - 1) * Integer.parseInt(pageSize));
//                params.put("limit", Integer.parseInt(pageSize));
//            }
            PageHelper.startPage(pageNo, pageSize);
            List<UserBatch> list = userSerivce.getUserBatchByParam(params);
            PageInfo<UserBatch> pageInfo = new PageInfo<>(list);
            model.put("list", pageInfo.getList());
            model.put("total", pageInfo.getTotal());
            model.put("pageNo", pageNo);
            model.put("pageSize", pageSize);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", model);
        }
        Map<String, Object> result = retModel(respstat, model);
        logger.info("返回结果：" + result);
        return result;
    }

    /**
     * 配置商户的薪资服务公司--修改费率
     */
    @RequestMapping(value = "/updateCompany", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> updateCompany(HttpServletRequest request,
                                      HttpServletResponse response, ChannelRelated related) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String customKey = (String) request.getSession().getAttribute("customkey");
        if (!"mfkj".equals(customKey)
                || StringUtil.isEmpty(related.getServiceRates())
                || StringUtil.isEmpty(related.getUpperServiceRates())
                || StringUtil.isEmpty(related.getProfiltLower())
                || StringUtil.isEmpty(related.getProfiltUpper())) {
            return retModelMsg(RespCode.error107, "参数异常，请联系管理员！", model);
        }
        try {
            String serviceRate = ArithmeticUtil.mulStr(related.getServiceRates(), "0.01", 4);
            String upperServiceRates = ArithmeticUtil.mulStr(related.getUpperServiceRates(), "0.01", 4);
            String profiltUpper = ArithmeticUtil.mulStr(related.getProfiltUpper(), "0.01", 4);
            String profiltLower = ArithmeticUtil.mulStr(related.getProfiltLower(), "0.01", 4);
            related.setServiceRates(serviceRate);
            related.setUpperServiceRates(upperServiceRates);
            related.setProfiltLower(profiltLower);
            related.setProfiltUpper(profiltUpper);
            channelRelatedService.updateChannelRelated(related);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", model);
        }
        Map<String, Object> result = retModel(respstat, model);
        logger.info("返回结果：" + result);
        return result;
    }

    /**
     * 配置商户的薪资服务公司--保存薪资服务公司关系
     */
    @RequestMapping(value = "/selectCompany", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> selectCompany(HttpServletRequest request, ChannelRelated related) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>(10);
        String customKey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
        if (StringUtil.isEmpty(related.getMerchantId())
                || StringUtil.isEmpty(related.getCompanyId())
                || !CommonString.ROOT.equals(customKey)
                || StringUtil.isEmpty(related.getServiceRates())
                || StringUtil.isEmpty(related.getUpperServiceRates())
                || StringUtil.isEmpty(related.getProfiltLower())
                || StringUtil.isEmpty(related.getProfiltUpper())
                || StringUtil.isEmpty(related.getOriginalId())) {
            return retModelMsg(RespCode.error107, "参数异常，请联系管理员！", model);
        }

        Map<String, Object> param = new HashMap<>(5);
        param.put("originalId", related.getOriginalId());
        param.put("companyId", related.getCompanyId());

        int countByappId = channelRelatedService.getRelatedCountByParam(param);
        if (countByappId > 0) {
            return retModelMsg(RespCode.error107, "关联关系已存在，请勿重复提交！", model);
        }
        try {
            String serviceRate = ArithmeticUtil.mulStr(related.getServiceRates(), "0.01", 5);
            String upperServiceRates = ArithmeticUtil.mulStr(related.getUpperServiceRates(), "0.01", 5);
            String profiltUpper = ArithmeticUtil.mulStr(related.getProfiltUpper(), "0.01", 5);
            String profiltLower = ArithmeticUtil.mulStr(related.getProfiltLower(), "0.01", 5);
            related.setServiceRates(serviceRate);
            related.setUpperServiceRates(upperServiceRates);
            related.setProfiltLower(profiltLower);
            related.setProfiltUpper(profiltUpper);
            //状态为 启用
            related.setStatus(1);
            //变更其他状态为历史状态
//			Map<String,Object> param2= new HashMap<>(5);
//			param2.put("originalId",related.getOriginalId());
//			param2.put("status","1");
//			channelRelatedService.updateRelatedStatus(param2);
            //保存启用状态的关系
            channelRelatedService.createChannelRelated(related);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return retModelMsg(RespCode.error107, "接口查询失败，请联系管理员！", model);
        }
        Map<String, Object> result = retModel(respstat, model);
        logger.info("返回结果：" + result);
        return result;
    }

    /**
     * 启用配置
     */
    @RequestMapping(value = "/originalEnable", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> originalEnable(HttpServletRequest request,
                                       HttpServletResponse response) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String id = (String) request.getParameter("id");
        logger.info("/originalEnable 方法  传参： id = " + id);
        try {
            ChannelRelated channelRelated = channelRelatedService.getRelatedById(id);
            if (channelRelated == null || StringUtil.isEmpty(id)) {
                respstat = RespCode.error107;
                model.put(RespCode.RESP_STAT, respstat);
                model.put(RespCode.RESP_MSG, "启用失败，配置不存在！");
                return model;
            }
            /**
             * 变更其他状态为历史状态
             */
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("originalId", channelRelated.getOriginalId());
            param.put("status", "1");
            channelRelatedService.updateRelatedStatus(param);
            channelRelated.setStatus(1);//启用该配置
            channelRelatedService.updateChannelRelated(channelRelated);
        } catch (Exception e) {
            respstat = RespCode.error107;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
            logger.error(e.getMessage(), e);
            return model;
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "成功");
        logger.info("返回结果：" + model);
        return model;
    }

    /**
     * 关联关系详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> detail(HttpServletRequest request,
                               HttpServletResponse response) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<String, Object>();
        String id = (String) request.getParameter("id");
        logger.info("/detail 方法  传参： id = " + id);
        try {
            ChannelRelated channelRelated = channelRelatedService.getRelatedById(id);
            if (channelRelated == null || StringUtil.isEmpty(id)) {
                respstat = RespCode.error107;
                model.put(RespCode.RESP_STAT, respstat);
                model.put(RespCode.RESP_MSG, "启用失败，配置不存在！");
                return model;
            }
            channelRelated = muStr(channelRelated);
            model.put("channelRelated", channelRelated);
        } catch (Exception e) {
            respstat = RespCode.error107;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
            logger.error(e.getMessage(), e);
            return model;
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "成功");
        logger.info("返回结果：" + model);
        return model;
    }


    /**
     * 公司用户列表
     *
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value = "/company/userList", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyUserList(HttpServletResponse response, HttpServletRequest request,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        String originalId = (String) request.getSession().getAttribute(
                "customkey");//商户标识
        Map<String, Object> model = new HashMap<String, Object>();
        String userType = (String) request.getParameter("userType");//用户类型，1普通 ， 2商户，  0禁用， -1  待激活  11  补全信息  ,-2待激活商户
        String startTime = (String) request.getParameter("startTime");
        String endTime = (String) request.getParameter("endTime");
        String userName = (String) request.getParameter("userName");
//        String pageNo = (String) request.getParameter("pageNo");
        logger.info("userList方法  传参：startTime=" + startTime + " endTime=" + endTime + "customkey=" + originalId + "pageNo=" + pageNo);
        if (StringUtil.isEmpty(originalId) ) {
            respstat = RespCode.error101;
            model.put(RespCode.RESP_STAT, respstat);
            model.put(RespCode.RESP_MSG, "请求参数不全");
            return model;
        } else {
            try {
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("userType", userType);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                paramMap.put("userName", userName);
                paramMap.put("originalId", originalId);
//                int total = userSerivce.getUserRelatedCountByParam(paramMap);
//                int pageSize = 10;
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<User> userList = userSerivce.getUserRelatedByParam(paramMap);
                PageInfo<User> pageInfo = new PageInfo<>(userList);
                model.put("total", pageInfo.getTotal());
                model.put("userList", pageInfo.getList());
            } catch (Exception e) {
                respstat = RespCode.error107;
                model.put(RespCode.RESP_STAT, respstat);
                model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
                logger.error(e.getMessage(), e);
                return model;
            }
        }
        model.put(RespCode.RESP_STAT, respstat);
        model.put(RespCode.RESP_MSG, "成功");
        logger.info("返回结果：" + model);
        return model;
    }

    /**
     * 薪税钱包配置——商户管理——列表
     * 说明:
     *
     * @param request
     * @param model
     * @param companyName
     * @param customType
     * @param startTime
     * @param endTime
     * @return:
     */
    @RequestMapping(value = "/customLevel/getCustomManageByPage")
    public @ResponseBody
    Map<String, Object> getCustomManageByPage(HttpServletRequest request,
                                              ModelMap model, String companyName, String customType, String startTime, String endTime,
                                              String pageNo) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();
        String customkey = (String) request.getSession().getAttribute("customkey");// 渠道名称
        int pageSize = 10;
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
        paramMap.put("limit", pageSize);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("name", companyName);
        paramMap.put("enabled", 1);
        paramMap.put("customType", customType);
        if (!"mfkj".equals(customkey)) {
            paramMap.put("customkey", customkey);
        }
        List<ChannelCustom> customList = channelCustomService.getCustomList(paramMap);
        result.put("customList", customList);
        return retModel(respstat, result);
    }

    /**
     * 打款凭证查看
     */
    @RequestMapping("/picList")
    @ResponseBody
    public Map<String, Object> invoicePicList(@RequestParam String orderNo) {
        //根据订单号查看打款凭证
        List<Map<String, Object>> picList = channelHistoryService
            .getPicListByOrderNo(orderNo);
        return returnSuccess(picList);
    }

}
