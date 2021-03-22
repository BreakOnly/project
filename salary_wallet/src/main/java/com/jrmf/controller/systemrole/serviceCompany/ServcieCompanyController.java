package com.jrmf.controller.systemrole.serviceCompany;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.controller.systemrole.SystemRoleController;
import com.jrmf.domain.*;
import com.jrmf.service.*;
import com.jrmf.utils.*;
import com.jrmf.utils.exception.ParamErrorException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 描 述: 下发公司接口<br/>
 * 创 建：2019年09⽉19⽇<br/>
 */
@RestController
@RequestMapping("/service/company")
public class ServcieCompanyController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ServcieCompanyController.class);

    @Autowired
    private UserSerivce userSerivce;
    @Autowired
    private ChannelCustomService customService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private CompanyRateConfService companyRateConfService;
    @Autowired
    private ChannelConfigService channelConfigService;
    @Autowired
    private CompanyNetfileRateConfService companyNetfileRateConfService;
    @Autowired
    private ProxyCostMaintainService proxyCostMaintainService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private CustomNoticeService customNoticeService;
    @Autowired
    private SystemRoleController systemRoleController;
    @Autowired
    ChannelHistoryService channelHistoryService;
    @Autowired
    ChannelInterimBatchService2 channelInterimBatchService2;
    @Autowired
    private AgreementTemplateService agreementTemplateService;
    @Autowired
    private ChannelRelatedService channelRelatedService;
    @Autowired
    private BestSignConfig bestSignConfig;
    @Autowired
    private ChannelCustomService channelCustomService;


    /**
     * 查询下发公司信息
     * @return
     */
    @RequestMapping(value = "/queryCompanyList", method = RequestMethod.POST)
    public Map<String, Object> queryCompanyList(HttpServletRequest request,
                                                @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                @RequestParam(value = "pageNo") Integer pageNo,
                                                @RequestParam(value = "companyName", required = false) String companyName,
                                                @RequestParam(value = "status", required = false) String status,
                                                @RequestParam(value = "submitTimeStart", required = false) String submitTimeStart,
                                                @RequestParam(value = "submitTimeEnd", required = false) String submitTimeEnd,
                                                @RequestParam(value = "realCompanyName", required = false) String realCompanyName,
                                                @RequestParam(value = "customname", required = false) String customname,
                                                @RequestParam(value = "companyType", required = false) String companyType,
                                                @RequestParam(value = "email", required = false) String email) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>();
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));


        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("companyName",companyName);
        paramMap.put("status",status);
        paramMap.put("submitTimeStart",submitTimeStart);
        paramMap.put("submitTimeEnd",submitTimeEnd);
        paramMap.put("realCompanyName",realCompanyName);
        paramMap.put("customname",customname);
        paramMap.put("companyType",companyType);
        paramMap.put("email",email);

//        int total = companyService.getCompanyListByParamCount(paramMap);
//        if (!StringUtil.isEmpty(pageNo)) {
//            paramMap.put("start", getFirst(pageNo, pageSize));
//            paramMap.put("limit", Integer.parseInt(pageSize));
//        }
        PageHelper.startPage(pageNo,pageSize);
        List<Company> list = companyService.getCompanyListByParam(paramMap);
        PageInfo<Company> pageInfo = new PageInfo<>(list);

        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        return result;
    }

    /**
     * @return
     * @description: 添加下发公司 (添加qb_user表获取主键，添加qb_company，最后添加channel_custom)
     */
    @RequestMapping(value = "/addServiceCompany", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addServiceCompany(HttpServletRequest request) {

        ChannelCustom loginUser = (ChannelCustom)request.getSession().getAttribute("customLogin");
        if (!CommonString.ROOT.equals(loginUser.getCustomkey()) && !CommonString.ROOT.equals(loginUser.getMasterCustom())) {
            return returnFail(RespCode.error101, "不允许此操作");
        }

        String merchantId = request.getParameter("merchantId");
        String companyName = request.getParameter("companyName");
        String companyType = request.getParameter("companyType");
        String email = request.getParameter("email");
        String contactorMobile = request.getParameter("contactorMobile");
        String tranPassword = request.getParameter("tranPassword");
        String status = request.getParameter("status");
        String minAge = request.getParameter("minAge");
        String maxAge = request.getParameter("maxAge");
        String singleMonthLimit = request.getParameter("singleMonthLimit");
        String singleQuarterLimit = request.getParameter("singleQuarterLimit");
        String realCompanyId = request.getParameter("realCompanyId");
        String serviceCompanyId = request.getParameter("serviceCompanyId");
        String businessLicenseNo = request.getParameter("businessLicenseNo");
        String expiresEnd = request.getParameter("expiresEnd");
        String realCompanyRate = request.getParameter("realCompanyRate");
        String businessPlatform = request.getParameter("businessPlatform");
        String invoiceCategory = request.getParameter("invoiceCategory");

        boolean name = companyService.getCompanyName(companyName);
        boolean channelcustomByCompanyName = customService.getChannelCustomCompanyNameByCustomType(companyName);
        if (name || channelcustomByCompanyName) {
            return returnFail(RespCode.error101, "该服务公司已存在");
        }

        Company companyEmail = companyService.getCompanyEmail(email);
        ChannelCustom customEmail = customService.getUserByUserNameAndOemUrl(email, request.getServerName());
        if (companyEmail != null || customEmail != null) {
            return returnFail(RespCode.error101, "该邮箱已注册");
        }

        User user = new User();
        user.setUserNo(contactorMobile); // 是否用手机号
        user.setUserName(companyName);
        // 防止username和certId 唯一性索引异常
        user.setCertId(OrderNoUtil.getOrderNo());
        user.setMobilePhone(contactorMobile);
        user.setMerchantId(merchantId);
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        user.setCreateTime(dateFormat.format(now));
        user.setUserType(2);
//        user.setCompanyUserNo("");
        user.setCompanyName(companyName);
        try {
            userSerivce.insertUserInfo(user);
            Company company = new Company();
            company.setUserId(user.getId());
            company.setCompanyName(companyName);
            company.setContactorMobile(contactorMobile);
            company.setBusinessPlatform(businessPlatform);
            company.setEmail(email);
            boolean forwardCompany = "1".equals(companyType);
            if (forwardCompany) {
                if (StringUtil.isEmpty(realCompanyRate)) {
                    company.setRealCompanyRate("");
                } else {
                    company.setRealCompanyRate( ArithmeticUtil.mulStr(realCompanyRate, "0.01", 4));
                }
            }
            company.setSocialCreditCode(businessLicenseNo);
            company.setTaxRegisterNo(businessLicenseNo);
            company.setBusinessLicenseNo(businessLicenseNo);
            if(Integer.parseInt(companyType)==0){
            	company.setRealCompanyId(String.valueOf(company.getUserId()));
            }else{
            	company.setRealCompanyId(realCompanyId);
            }
            if (!StringUtil.isEmpty(serviceCompanyId)) {
                company.setServiceCompanyId(Long.parseLong(serviceCompanyId));
            }
            company.setSingleMonthLimit(singleMonthLimit);
            company.setSingleQuarterLimit(singleQuarterLimit);
            company.setMinAge(Integer.parseInt(minAge));
            company.setMaxAge(Integer.parseInt(maxAge));
            company.setStatus(Integer.parseInt(status));
            company.setCompanyType(Integer.parseInt(companyType));
            company.setExpiresEnd(expiresEnd);
            company.setCreateTime(DateUtils.getNowDate());
            company.setUpdateTime(DateUtils.getNowDate());
            if(StringUtils.isNotBlank(invoiceCategory)) {
                company.setInvoiceCategory(Integer.parseInt(invoiceCategory));
            }
            companyService.addCompany(company);

            ChannelCustom channelCustom = new ChannelCustom();
            String appSecret = UUID.randomUUID() + "";
            channelCustom.setCompanyName(companyName);
            channelCustom.setContractCompanyName(companyName);
            channelCustom.setCustomkey(String.valueOf(user.getId()));
            channelCustom.setAppSecret(appSecret);
            channelCustom.setLoginRole(1);
            channelCustom.setMerchantName(channelCustom.getCompanyName());
            channelCustom.setPassword(CipherUtil.generatePassword(tranPassword, channelCustom.getCustomkey()));
            channelCustom.setTranPassword(CipherUtil.generatePassword(tranPassword, channelCustom.getCustomkey()));
            channelCustom.setEmail(email);
            //添加的时候设置为不可用，待服务公司审核之后将custom信息设置为可用
//            channelCustom.setEnabled(0);
            channelCustom.setEnabled(1);
            channelCustom.setPhoneNo(contactorMobile);
            channelCustom.setUsername(email);
            channelCustom.setCustomType(2);
            channelCustom.setInvoiceType(1);
            channelCustom.setTaxpayerType(1);
            channelCustom.setLoginRole(1);
            if (forwardCompany){
                channelCustom.setBusinessPlatform("金融魔方");
                channelCustom.setBusinessPlatformId(1);
            }
            customService.saveChannelCustom(channelCustom);
            systemRoleController.insertCustomNotice(channelCustom.getCustomType(), channelCustom.getCustomkey());
        } catch (Exception e) {
            logger.error("新增服务公司基础配置信息异常：", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return returnFail(RespCode.error107, "新增服务公司基础配置失败，请联系管理员!");
        }
        return returnSuccess();
    }

    /**
     * @description：修改下发公司
     * @return
     */
    @RequestMapping(value = "/updateServiceCompany", method = RequestMethod.POST)
    public Map<String, Object> updateChannelCustom(HttpServletRequest request) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (!CommonString.ROOT.equals(loginUser.getCustomkey()) && !CommonString.ROOT.equals(loginUser.getMasterCustom())) {
            return returnFail(RespCode.error101, "不允许此操作");
        }

        String id = request.getParameter("id");
        String userId = request.getParameter("userId");
        String merchantId = request.getParameter("merchantId");
        String companyName = request.getParameter("companyName");
        String companyType = request.getParameter("companyType");
        String email = request.getParameter("email");
        String contactorMobile = request.getParameter("contactorMobile");
        String tranPassword = request.getParameter("tranPassword");
        String status = request.getParameter("status");
        String minAge = request.getParameter("minAge");
        String maxAge = request.getParameter("maxAge");
        String singleMonthLimit = request.getParameter("singleMonthLimit");
        String singleQuarterLimit = request.getParameter("singleQuarterLimit");
        String realCompanyId = request.getParameter("realCompanyId");
        String serviceCompanyId = request.getParameter("serviceCompanyId");
        String businessLicenseNo = request.getParameter("businessLicenseNo");
        String expiresEnd = request.getParameter("expiresEnd");
        String realCompanyRate = request.getParameter("realCompanyRate");
        String businessPlatform = request.getParameter("businessPlatform");
        String invoiceCategory = request.getParameter("invoiceCategory");
        if (StringUtil.isEmpty(id)) {
            throw new ParamErrorException(RespCode.codeMaps.get(RespCode.ParamNotFound));
        }

        Company c = companyService.getCompanyById(id);
        if (c == null) {
            return returnFail(RespCode.error101, "该服务公司不存在，请刷新页面重试");
        }

        Company companyByCompanyName = companyService.getCompanyByCompanyName(companyName);
        if (companyByCompanyName != null) {
            if (companyName.equals(companyByCompanyName.getCompanyName()) && !id.equals(companyByCompanyName.getId()+"")) {
                return returnFail(RespCode.error101, "该服务公司已存在");
            }
        }
        ChannelCustom custom = customService.getChannelCustomByCompanyName(companyName);
        if (custom != null) {
            if (companyName.equals(custom.getCompanyName()) && !custom.getCustomkey().equals(c.getUserId()+"")) {
                return returnFail(RespCode.error101, "该服务公司已存在");
            }
        }

        if (StringUtil.isEmpty(email)) {
            return returnFail(RespCode.error101, "请输入邮箱");
        } else {
            Company companyEmail = companyService.getCompanyEmail(email);
            if (companyEmail != null ) {
                if (email.equals(companyEmail.getEmail()) && !id.equals(companyEmail.getId()+"")) {
                    return returnFail(RespCode.error101, "此邮箱已被占用");
                }
            }
            ChannelCustom customEmail = customService.getUserByUserNameAndOemUrl(email, request.getServerName());
            if (customEmail != null ) {
                if (email.equals(customEmail.getUsername()) && !customEmail.getCustomkey().equals(c.getUserId()+"")) {
                    return returnFail(RespCode.error101, "此邮箱已被占用");
                }
            }
        }
        //判断类型-实际下发，转包下发、别名下发
        int oldCompanyType = c.getCompanyType();
        //判断服务公司下发类型是否有修改
        boolean flag = companyType.equals(String.valueOf(oldCompanyType));
        //如果是转包下发和别名下发，判断实际服务公司是否有修改
        if (flag && (oldCompanyType == 1 || oldCompanyType == 2)){
            String oldRealCompanyId = c.getRealCompanyId();
            if (!realCompanyId.equals(oldRealCompanyId)) {
                flag = false;
            }
        }
        if (!flag){//检查该服务公司是否有未下发完的数据
            //先查询下发临时批次表是否有未完成的下发记录
            Map<String,Object> params = new HashMap<>();
            String nowDay = DateUtils.getNowDay();
            String startDay = DateUtils.getBeforeDayString(14);
            params.put("recCustomkey",c.getUserId());
            params.put("startTime",startDay);
            params.put("endTime",nowDay);
            List<ChannelInterimBatch> channelInterimBatchList = channelInterimBatchService2
                .getChannelInterimBatchByParam(params);
            if (channelInterimBatchList != null && channelInterimBatchList.size() > 0){
                for (ChannelInterimBatch channelInterimBatch : channelInterimBatchList) {
                    int existInterimBatchStatus = channelInterimBatch.getStatus();
                    if (existInterimBatchStatus != 2){
                        return returnFail(RespCode.UPDATE_FAIL, "该服务公司近14天内存在未下发完成的临时批次，不能修改。");
                    }
                }
            }
            //查询正式下发批次表是否有未完成的下发记录
            params.put("transfertype","2");
            params.put("status","0,3");
            List<ChannelHistory> channelHistoryList = channelHistoryService.getChannelHistoryList(params);
            if (channelHistoryList != null && channelHistoryList.size() > 0){
                return returnFail(RespCode.UPDATE_FAIL, "该服务公司近14天内存在未下发完成的批次，不能修改。");
            }
        }

        User user = new User();
        user.setId(Integer.parseInt(userId));
        user.setMerchantId(merchantId);
        user.setUserNo(contactorMobile);
        user.setMobilePhone(contactorMobile);
        user.setUserType(2);
        user.setCompanyName(companyName);
        user.setUserName(companyName);
        try {

            userSerivce.updateUserInfo(user);
            Company company = new Company();
            company.setExpiresEnd(expiresEnd);
            company.setId(Integer.parseInt(id));
            company.setUserId(Integer.parseInt(userId));
            company.setCompanyName(companyName);
            company.setContactorMobile(contactorMobile);
            company.setEmail(email);
            company.setSocialCreditCode(businessLicenseNo);
            company.setTaxRegisterNo(businessLicenseNo);
            company.setBusinessLicenseNo(businessLicenseNo);
            company.setBusinessPlatform(businessPlatform);

            if(Integer.parseInt(companyType)==0){
                company.setRealCompanyId(String.valueOf(company.getUserId()));
            }else{
                company.setRealCompanyId(realCompanyId);
            }
            if ("1".equals(companyType)) {
                if (StringUtil.isEmpty(realCompanyRate)) {
                    company.setRealCompanyRate("");
                } else {
                    company.setRealCompanyRate( ArithmeticUtil.mulStr(realCompanyRate, "0.01", 4));
                }
            }
            if (!StringUtil.isEmpty(serviceCompanyId)) {
                company.setServiceCompanyId(Long.parseLong(serviceCompanyId));
            }
            company.setSingleMonthLimit(singleMonthLimit);
            company.setSingleQuarterLimit(singleQuarterLimit);
            company.setMinAge(Integer.parseInt(minAge));
            company.setMaxAge(Integer.parseInt(maxAge));
            company.setStatus(Integer.parseInt(status));
            company.setCompanyType(Integer.parseInt(companyType));
            company.setUpdateTime(DateUtils.getNowDate());
            if(StringUtils.isNotBlank(invoiceCategory)) {
                company.setInvoiceCategory(Integer.parseInt(invoiceCategory));
            }
            companyService.updateCompanyInfo(company);

            List<CompanyRateConf> companyRateConfByCompanyId = companyRateConfService.getCompanyRateConfByCompanyId(c.getUserId());
            if (companyRateConfByCompanyId != null && companyRateConfByCompanyId.size() > 0) {
                companyRateConfService.updateCompanyRateConfByCompanyId(merchantId, c.getUserId());
            }

            List<CompanyNetfileRateConf> companyNetfileRateConfList = companyNetfileRateConfService.getCompanyNetfileRateConfByCompanyId(c.getUserId());
            if (companyNetfileRateConfList != null && companyNetfileRateConfList.size() > 0) {
                companyNetfileRateConfService.updateCompanyNetfileRateConfByCompanyId(merchantId, c.getUserId());
            }

            ChannelCustom channelCustom = new ChannelCustom();
            String appSecret = UUID.randomUUID() + "";
            channelCustom.setCompanyName(companyName);
            channelCustom.setContractCompanyName(companyName);
            channelCustom.setCustomkey(userId);
            channelCustom.setAppSecret(appSecret);
            channelCustom.setLoginRole(1);
            channelCustom.setMerchantName(channelCustom.getCompanyName());
            if (!StringUtil.isEmpty(tranPassword)) {
                channelCustom.setPassword(CipherUtil.generatePassword(tranPassword, userId));
                channelCustom.setTranPassword(CipherUtil.generatePassword(tranPassword, userId));
            }
            channelCustom.setEmail(email);
            //添加的时候设置为不可用，待服务公司审核之后将custom信息设置为可用
//            channelCustom.setEnabled(0);
            channelCustom.setPhoneNo(contactorMobile);
            channelCustom.setUsername(email);
            channelCustom.setCustomType(2);
            channelCustom.setInvoiceType(1);
            channelCustom.setTaxpayerType(1);
            channelCustom.setLoginRole(1);
            channelCustom.setEndTime(DateUtils.getNowDate());
            customService.updateChannelCustomInfo(channelCustom);
       } catch (Exception e) {
           logger.error("修改服务公司基础配置信息失败:", e);
           return returnFail(RespCode.UPDATE_FAIL,"系统异常，请联系管理员！");
       }
        return returnSuccess();
    }

    /**
     * @return
     * @description: 查询档位基础信息
     */
    @RequestMapping(value = "/queryGearPosition", method = RequestMethod.POST)
    public Map<String, Object> queryServiceCompany( @RequestParam(value = "pageSize", required = false, defaultValue = "10") String pageSize,
                                                    @RequestParam(value = "pageNo", required = false) String pageNo,
                                                    @RequestParam(value = "companyId") int companyId,
                                                    @RequestParam(value = "amountStart", required = false) String amountStart,
                                                    @RequestParam(value = "amountEnd", required = false) String amountEnd) {

        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<>();
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        try {
            if (!StringUtil.isEmpty(amountStart) && !StringUtil.isEmpty(amountEnd)) {

                CompanyNetfileRateConf minAmount = companyNetfileRateConfService.getMinAmountStartByCompanyId(companyId);
                CompanyNetfileRateConf maxAmount = companyNetfileRateConfService.getMaxAmountStartByCompanyId(companyId);

                if (minAmount != null && ArithmeticUtil.compareTod(amountEnd,minAmount.getAmountEnd()) != 1) {
                    result.put("gearLabel", 1);
                    return result;
                } else if (maxAmount != null && (ArithmeticUtil.compareTod(amountEnd,maxAmount.getAmountEnd()) != 1) && (ArithmeticUtil.compareTod(amountStart,maxAmount.getAmountStart())  != -1)) {
                    result.put("gearLabel", 2);
                    return result;
                } else {
                    return returnFail(RespCode.error107, "当前配置档位金额范围跨越报税档位！");
                }

            }
            paramMap.put("companyId", companyId);
            int total = companyRateConfService.queryCompanyRateConfCount(paramMap);
            if (!StringUtil.isEmpty(pageNo)) {
                paramMap.put("start", getFirst(pageNo, pageSize));
                paramMap.put("limit", Integer.parseInt(pageSize));
            }
            List<CompanyRateConf> list = companyRateConfService.queryCompanyRateConf(paramMap);
            result.put("list", list);
            result.put("total", total);
            logger.info("查询服务公司下发金额档位信息返回结果：" + result);
        } catch (Exception e) {
            logger.error("查询服务公司下发金额档位信息异常：", e);
            return returnFail(RespCode.error107, "查询服务公司下发金额档位信息失败，请联系管理员！");
        }
        return result;
    }

    /**
     * @return
     * @description: 配置档位
     */
    @RequestMapping(value = "/configGearPosition", method = RequestMethod.POST)
    public Map<String, Object> configGearPosition(HttpServletRequest request) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (!CommonString.ROOT.equals(loginUser.getCustomkey()) && !CommonString.ROOT.equals(loginUser.getMasterCustom())) {
            return returnFail(RespCode.error101, "不允许此操作");
        }

        String companyId = request.getParameter("companyId");
        String id = request.getParameter("id");
        String companyName = request.getParameter("companyName");
        String gearPosition = request.getParameter("gearPosition");
        String amountStart = request.getParameter("amountStart");
        String operator = request.getParameter("operator");
        String amountEnd = request.getParameter("amountEnd");
        String gearPositionShorthand = request.getParameter("gearPositionShorthand");
        String gearPositionDesc = request.getParameter("gearPositionDesc");
        String costRate = request.getParameter("costRate");
        String mfkjCostRate = request.getParameter("mfkjCostRate");
        String gearLabel = request.getParameter("gearLabel");

        if (!StringUtil.isNumber(amountStart) && StringUtil.isNumber(amountEnd)) {
            return returnFail(RespCode.error124, "请输入正确的金额");
        }

        if (ArithmeticUtil.compareTod(amountEnd, amountStart) == -1 || ArithmeticUtil.compareTod(amountEnd, amountStart) == 0) {
            return returnFail(RespCode.error124, "结束金额不能小于等于起始金额");
        }

        CompanyRateConf companyRateConf = new CompanyRateConf();
        companyRateConf.setBusinessType(1);
        companyRateConf.setGearPosition(Integer.parseInt(gearPosition));
        companyRateConf.setAmountStart(amountStart);
        companyRateConf.setAmountEnd(amountEnd);
        companyRateConf.setOperator(operator);
        companyRateConf.setGearPositionShorthand(gearPositionShorthand);
        companyRateConf.setGearPositionDesc(gearPositionDesc);
        if (StringUtil.isEmpty(costRate)) {
            companyRateConf.setCostRate("0");
        }
        if (StringUtil.isEmpty(mfkjCostRate)) {
            companyRateConf.setMfkjCostRate("0");
        }
        companyRateConf.setCostRate(costRate);
        companyRateConf.setMfkjCostRate(mfkjCostRate);
        companyRateConf.setGearLabel(Integer.parseInt(gearLabel));
        try {
            companyRateConf.setCompanyId(Integer.parseInt(companyId));
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("companyId", companyId);
            CompanyNetfileRateConf minAmount = companyNetfileRateConfService.getMinAmountStartByCompanyId(companyRateConf.getCompanyId());
            CompanyNetfileRateConf maxAmount = companyNetfileRateConfService.getMaxAmountStartByCompanyId(companyRateConf.getCompanyId());
            List<CompanyNetfileRateConf> list = companyNetfileRateConfService.queryNetfileGearPosition(paramMap);
            if (list != null && list.size() > 0) {
                for (CompanyNetfileRateConf c : list) {
                    if (minAmount != null && (ArithmeticUtil.compareTod(amountEnd, minAmount.getAmountEnd()) != 1)) {
                        if (c.getGearLabel() == 1 && ArithmeticUtil.compareTod(amountEnd, c.getAmountEnd()) != 1) {
                            companyRateConf.setNetfileId(c.getId());
                            break;
                        }
                    } else if (maxAmount != null && (ArithmeticUtil.compareTod(amountEnd, maxAmount.getAmountEnd()) != 1) && (ArithmeticUtil.compareTod(amountStart, maxAmount.getAmountStart()) != -1)) {
                        if (c.getGearLabel() == 2 && ArithmeticUtil.compareTod(amountEnd, c.getAmountEnd()) != 1) {
                            companyRateConf.setNetfileId(c.getId());
                            break;
                        }
                    } else {
                        return returnFail(RespCode.error107, "当前配置档位金额范围跨越报税档位！");
                    }
                }
            } else {
                return returnFail(RespCode.INSERT_FAIL, "报税档位信息查询为空，不能配置！");
            }

            List<CompanyRateConf> rateConf = companyRateConfService.getCompanyRateConfByCompanyId(Integer.parseInt(companyId));
            if (rateConf != null && rateConf.size() > 0) {
                for (CompanyRateConf rc : rateConf) {
                    if (!StringUtil.isEmpty(id)) {
                        List<CompanyRateConf> conf = companyRateConfService.getCompanyRateConfByNoIdAndCompanyId(Integer.parseInt(id), companyId);
                        if (conf != null && conf.size() > 0) {
                            for (CompanyRateConf c : conf) {
                                if (ArithmeticUtil.compareTod(companyRateConf.getAmountStart(), c.getAmountStart()) == 0 && ArithmeticUtil.compareTod(companyRateConf.getAmountEnd(), c.getAmountEnd()) == 0) {
                                    return returnFail(RespCode.error107, "档位金额已存在，请重新输入金额！");
                                }
                            }
                        }
                    } else {
                        if (ArithmeticUtil.compareTod(companyRateConf.getAmountStart(), rc.getAmountStart()) == 0 && ArithmeticUtil.compareTod(companyRateConf.getAmountEnd(), rc.getAmountEnd()) == 0) {
                            return returnFail(RespCode.error107, "档位金额已存在，请重新输入金额！");
                        }
                    }
                }
            }

            if (!StringUtil.isEmpty(id)) {
                if ("1".equals(gearLabel)) {
                    CompanyRateConf crc = companyRateConfService.getCompanyRateConfById(Integer.parseInt(id));
                    if (!StringUtil.isEmpty(crc.getCostRate())) {
                        if (ArithmeticUtil.compareTod(companyRateConf.getCostRate(), crc.getCostRate()) != 0) {
                            return returnFail(RespCode.UPDATE_FAIL, "报税标签为“小金额”则成本费率必须一致!");
                        }
                    }
                }
                companyRateConf.setId(Integer.parseInt(id));
                Date now = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                companyRateConf.setUpdateTime(dateFormat.format(now));
                companyRateConfService.updateGear(companyRateConf);
            } else {
                String merchantId = companyService.getMerchantIdByUserId(companyRateConf.getCompanyId());
                companyRateConf.setMerchantId(merchantId);
                companyRateConfService.insertCompanyRateConf(companyRateConf);
            }
        } catch (Exception e) {
            logger.error("新增/修改服务公司下发金额档位配置失败：", e);
            return returnFail(RespCode.INSERT_FAIL, "配置服务公司下发金额档位配置失败，请联系管理员！");
        }
        return returnSuccess();
    }

    /**
     * 判断金额是否重叠
     * @param start1
     * @param end1
     * @param start2
     * @param end2
     * @return true 交叉 、 false 不交叉
     */
    private static boolean judgeTwoAreaOverlap(double start1,double end1,double start2,double end2) {
        return start2 < end1 && end2 > start1;
    }


    /**
     * @return
     * @description: 删除档位
     *
     */
    @RequestMapping(value = "/deleteGearPosition", method = RequestMethod.POST)
    public Map<String, Object> deleteGearPosition(@RequestParam(value = "id") String id) {
        logger.info("删除档位 id = " + id);
        boolean companyRateConf = companyRateConfService.queryGearInfoByGearId(id);
        if (!companyRateConf) {
            return returnFail(RespCode.error101,"该档位ID不存在");
        }

        boolean config =  companyRateConfService.queryCustomCompanyRateConfById(id);
        if (config) {
            return returnFail(RespCode.error101,"档位配置已绑定商户，无法删除！");
        }

        try {
            companyRateConfService.removeGeraByGearId(id);
        } catch (Exception e) {
            logger.error("删除服务公司下发金额档位异常：", e);
            return returnFail(RespCode.DELETE_FAIL,"删除服务公司下发金额档位异常");
        }
        return returnSuccess();
    }

    /**
     * @return
     * @description: 查询服务公司收款账户
     */
    @RequestMapping(value = "/queryPaymentAccount", method = RequestMethod.POST)
    public Map<String, Object> queryPaymentAccount( @RequestParam(value = "pageSize", required = false, defaultValue = "10") String pageSize,
                                                    @RequestParam(value = "pageNo", required = false) String pageNo,
                                                    @RequestParam(value = "channelId") int channelId) {

        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<>();
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        try {
            paramMap.put("channelId", channelId);
            int total = channelConfigService.queryChannelConfigCount(paramMap);
            if (!StringUtil.isEmpty(pageNo)) {
                paramMap.put("start", getFirst(pageNo, pageSize));
                paramMap.put("limit", Integer.parseInt(pageSize));
            }
            List<ChannelConfig> list =  channelConfigService.queryChannelConfig(paramMap);
            result.put("list", list);
            result.put("total", total);
            logger.info("查询服务公司收款账户返回结果：" + result);
        } catch (Exception e) {
            logger.error("查询服务公司收款账户异常：", e);
            return returnFail(RespCode.error107, "查询服务公司收款账户失败，请联系管理员！");
        }
        return result;
    }

    /**
     * @return
     * @description: 新增/修改服务公司收款账户
     */
    @RequestMapping(value = "/operationPaymentAccount", method = RequestMethod.POST)
    public Map<String, Object> operationPaymentAccount(HttpServletRequest request) {

        ChannelConfig channelConfig = new ChannelConfig();
        String id = request.getParameter("id");
        String companyName = request.getParameter("companyName");
        String payType = request.getParameter("payType");
        String accountName = request.getParameter("accountName");
        String accountNum = request.getParameter("accountNum");
        String bankName = request.getParameter("bankName");
        String status = request.getParameter("status");
        String rechargeConfirmType = request.getParameter("rechargeConfirmType");

        if (StringUtil.isEmpty(rechargeConfirmType)) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        logger.info("服务公司收款账户id：" + id);
        try {
            Company company = companyService.getCompanyByCompanyName(companyName);
            if (company != null) {
               channelConfig.setChannelId(company.getUserId());
                channelConfig.setPayType(Integer.parseInt(payType));
                channelConfig.setAccountName(accountName);
                channelConfig.setAccountNum(accountNum);
                channelConfig.setBankName(bankName);
                channelConfig.setStatus(Integer.parseInt(status));
                channelConfig.setRechargeConfirmType(Integer.valueOf(rechargeConfirmType));
                if (!StringUtil.isEmpty(id)){
                	ChannelConfig channelConfigOld = channelConfigService.queryChannelConfigById(Integer.parseInt(id));
                	if (channelConfigOld.getStatus() == 2 && Integer.parseInt(status) == 1) {
                        int count = companyService.checkIsExist(channelConfig);
                        if(count>0){
                            return returnFail(RespCode.error101,"已有正常使用账户，无法修改！");
                        }
                    }
                	if(channelConfigOld.getPayType()!=Integer.parseInt(payType) ){
                    	int count = companyService.checkIsExist(channelConfig);
                    	if(count>0){
                            return returnFail(RespCode.error101,"下发公司对应的下发通道只允许配置一个！");
                    	}
                	}
                	channelConfig.setUpdateTime(DateUtils.getNowDate());
                    channelConfig.setId(Integer.parseInt(id));
                    channelConfigService.updatePaymentAccount(channelConfig);
                } else {
                    int count = companyService.checkIsExist(channelConfig);
                    if(count>0){
                        return returnFail(RespCode.error101,"下发公司对应的下发通道只允许配置一个！");
                    }
                    channelConfig.setUpdateTime(DateUtils.getNowDate());
                	channelConfig.setCreateTime(DateUtils.getNowDate());
                    channelConfigService.insertPaymentAccount(channelConfig);
                }
            } else {
                return returnFail(RespCode.error101,"服务公司不存在！");
            }
        } catch (Exception e) {
            logger.error(" 新增/修改服务公司收款账户失败 ", e);
            return returnFail(RespCode.UPDATE_FAIL," 新增/修改服务公司收款账户失败，请联系管理员！");
        }
        return returnSuccess();
    }

    /**
     * @return
     * @description: 删除服务公司收款账户
     */
    @RequestMapping(value = "/deletePaymentAccount", method = RequestMethod.POST)
    public Map<String, Object> deletePaymentAccount(@RequestParam(value = "id") int id) {
        logger.info("删除服务公司收款账户 Id = " + id);
        ChannelConfig channelConfig = channelConfigService.queryChannelConfigById(id);
        if (channelConfig.getId() == 0) {
            return returnFail(RespCode.error101,"该服务公司收款账户不存在");
        }
        try {
            channelConfigService.deleteChannelConfigById(id);
        } catch (Exception e) {
            logger.error("删除服务公司收款账户异常：", e);
            return returnFail(RespCode.DELETE_FAIL,"删除服务公司收款账户失败，请联系管理员！");
        }
        return returnSuccess();
    }


    /**
     * @return
     * @description: 查询报税金额档位信息
     */
    @RequestMapping(value = "/queryNetfileGearPosition", method = RequestMethod.POST)
    public Map<String, Object> queryNetfileGearPosition( @RequestParam(value = "pageSize", required = false, defaultValue = "10") String pageSize,
                                                    @RequestParam(value = "pageNo", required = false) String pageNo,
                                                    @RequestParam(value = "companyId") int companyId) {

        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<>();
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        try {
            paramMap.put("companyId", companyId);
            int total = companyNetfileRateConfService.queryNetfileGearPositionCount(paramMap);
            if (!StringUtil.isEmpty(pageNo)) {
                paramMap.put("start", getFirst(pageNo, pageSize));
                paramMap.put("limit", Integer.parseInt(pageSize));
            }
            List<CompanyNetfileRateConf> list = companyNetfileRateConfService.queryNetfileGearPosition(paramMap);
            result.put("list", list);
            result.put("total", total);
            logger.info("查询报税金额档位信息返回结果：" + result);
        } catch (Exception e) {
            logger.error("查询报税金额档位信息异常：", e);
            return returnFail(RespCode.error107, "查询报税金额档位信息失败，请联系管理员！");
        }
        return result;
    }

    /**
     * @return
     * @description: 配置报税金额档位信息
     */
    @RequestMapping(value = "/configNetfileGearPosition", method = RequestMethod.POST)
    public Map<String, Object> configNetfileGearPosition(HttpServletRequest request, CompanyNetfileRateConf companyNetfileRateConf) {

        String amountStart = companyNetfileRateConf.getAmountStart();
        String amountEnd = companyNetfileRateConf.getAmountEnd();

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (!CommonString.ROOT.equals(loginUser.getCustomkey()) && !CommonString.ROOT.equals(loginUser.getMasterCustom())) {
            return returnFail(RespCode.error101, "不允许此操作");
        }

        if (!StringUtil.isNumber(amountStart) && StringUtil.isNumber(amountEnd)) {
            return returnFail(RespCode.error124, "请输入正确的金额");
        }

        if (ArithmeticUtil.compareTod(amountEnd, amountStart) == -1 || ArithmeticUtil.compareTod(amountEnd, amountStart) == 0) {
            return returnFail(RespCode.error124, "结束金额不能小于等于起始金额");
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("companyId", companyNetfileRateConf.getCompanyId());
        List<CompanyNetfileRateConf> rateConf = companyNetfileRateConfService.queryNetfileGearPosition(paramMap);

        if (rateConf != null && rateConf.size() > 0) {
            for (CompanyNetfileRateConf rc : rateConf) {
                if (companyNetfileRateConf.getId() != null) {
                    if (rateConf.size() != 1) {
                        List<CompanyNetfileRateConf> cnrc = companyNetfileRateConfService.getCompanyNetfileRateConfByNoIdAndCompanyId(companyNetfileRateConf.getId(), companyNetfileRateConf.getCompanyId());
                        for (CompanyNetfileRateConf c : cnrc) {
                            boolean b = judgeTwoAreaOverlap(Double.parseDouble(c.getAmountStart()), Double.parseDouble(c.getAmountEnd()), Double.parseDouble(amountStart), Double.parseDouble(amountEnd));
                            if (b) {
                                return returnFail(RespCode.error124, "档位金额不能存在有重叠交替的部分！");
                            }
                        }
                    }
                } else {
                    boolean b = judgeTwoAreaOverlap(Double.parseDouble(rc.getAmountStart()), Double.parseDouble(rc.getAmountEnd()), Double.parseDouble(amountStart), Double.parseDouble(amountEnd));
                    if (b) {
                        return returnFail(RespCode.error124, "档位金额不能存在有重叠交替的部分！");
                    }
                }
            }
        }

        CompanyNetfileRateConf netfileRateConf = companyNetfileRateConfService.getCompanyNetfileRateConfByCompanyIdAndGearPosition(companyNetfileRateConf.getCompanyId(), companyNetfileRateConf.getGearPosition());

        if (companyNetfileRateConf.getId() == null && netfileRateConf != null) {
            return returnFail(RespCode.error124, "档位编号重复！");
        } else if (companyNetfileRateConf.getId() != null && netfileRateConf != null){
            CompanyNetfileRateConf companyNetfileRateConfById = companyNetfileRateConfService.getCompanyNetfileRateConfById(companyNetfileRateConf.getId());
            if (companyNetfileRateConf.getGearPosition() != companyNetfileRateConfById.getGearPosition()) {
                List<CompanyNetfileRateConf> list = companyNetfileRateConfService.getNoGearPositionByCompanyIdAndGearPosition(companyNetfileRateConf.getCompanyId(), companyNetfileRateConfById.getGearPosition());
                for (CompanyNetfileRateConf c : list) {
                    if (companyNetfileRateConf.getGearPosition() == c.getGearPosition()) {
                        return returnFail(RespCode.error124, "档位编号重复！");
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        try {
            result = companyNetfileRateConfService.configNetfileGearPosition(companyNetfileRateConf);
        } catch (Exception e) {
            logger.error("配置报税金额档位配置失败：", e);
            return returnFail(RespCode.INSERT_FAIL, "配置报税金额档位配置失败，请联系管理员！");
        }

        return result;
    }


    /**
     * @return
     * @description: 删除报税金额档位信息
     */
    @RequestMapping(value = "/deleteNetfileGearPosition", method = RequestMethod.POST)
    public Map<String, Object> deleteNetfileGearPosition(@RequestParam(value = "id") String id) {
        logger.info("删除档位 id = " + id);
        CompanyNetfileRateConf companyNetfileRateConfById = companyNetfileRateConfService.getCompanyNetfileRateConfById(Integer.parseInt(id));
        if (companyNetfileRateConfById == null) {
            return returnFail(RespCode.error101,"该档位ID不存在");
        }

        boolean companyRateConf =  companyRateConfService.queryCompanyRateConfByNetfileId(id);
        if (companyRateConf) {
            return returnFail(RespCode.error101,"该报税档位已关联商户下发金额档位，无法删除！");
        }

        boolean proxyCostMaintain =  proxyCostMaintainService.getProxyCostMaintainByNetfileId(id);
        if (proxyCostMaintain) {
            return returnFail(RespCode.error101,"该报税档位已关联代理商成本维护信息，无法删除！");
        }

        try {
            companyNetfileRateConfService.removeGeraByGearId(id);
        } catch (Exception e) {
            logger.error("删除报税金额档位异常：", e);
            return returnFail(RespCode.DELETE_FAIL,"删除报税金额档位失败，请联系管理员！");
        }
        return returnSuccess();
    }


    /**
     * 签约模板 - 查看
     *
     * @return
     */
    @RequestMapping(value = "/agreement/template/select", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> selectTemplate(@RequestParam String userId,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
        @RequestParam(value = "pageNo") Integer pageNo) {
        Map<String, Object> result = new HashMap<>(2);
        Map<String, Object> params = new HashMap<>(15);
        params.put("originalId", userId);
        int total = agreementTemplateService.listUserAgreementTemplatesCount(params);
        params.put("start", (pageNo - 1) * pageSize);
        params.put("limit", pageSize);
        List<AgreementTemplate> templates = agreementTemplateService
            .listUserAgreementTemplates(params);
        result.put("total", total);
        result.put("templates", templates);
        return returnSuccess(result);
    }




    /**
     * 签约配置 - 添加模版
     */
    @RequestMapping(value = "/agreement/template/insert", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> insertTemplate(
        @RequestParam("templateName") String templateName,
        @RequestParam("agreementName") String agreementName,
        @RequestParam("userId") Integer userId,
        @RequestParam("realCompanyId") String realCompanyId,
        @RequestParam("thirdMerchId") String thirdMerchId,
        @RequestParam("thirdTemplateId") String thirdTemplateId,
        @RequestParam("thirdTemplateDes") String thirdTemplateDes,
        @RequestParam(value = "regType", required = false, defaultValue = "1") int regType,
        @RequestParam("agreementPayment") String agreementPayment,
        @RequestParam(value = "agreementType") Integer agreementType,
        @RequestParam(value = "channelType") Integer channelType,
        @RequestParam(value = "uploadIdCard") Integer uploadIdCard) throws Exception {
        String merchantId = companyService.getMerchantIdByUserId(userId);
        if(StringUtil.isEmpty(merchantId)){
            int code = RespCode.COMPANY_NOT_FOUND;
            return returnFail(code, RespCode.codeMaps.get(code));
        }
        String implementor = merchantId;
        String agreementTemplateURL = "https://jrmf360.com?qzjcagreement.html";
        String aiyuangongCompany = "aiyuangong";
        String hygCompany = "huiyonggong";
        String xiaohuangfengCompany = "xiaohuangfeng";
        String htmlTemplate = "";
        AgreementTemplate agreementTemplate = new AgreementTemplate();
        agreementTemplate.setChannelType(channelType);
        if (aiyuangongCompany.equals(implementor)) {
            String fileName = thirdMerchId + "_" + thirdTemplateId + ".pdf";
            agreementTemplateURL = bestSignConfig.getClientServerNameUrl() + "/pdf/" + fileName;
            String url = "/econtract/extr/template/download?extrSystemId=" + thirdMerchId + "&templateId=" + thirdTemplateId;
            String domain = bestSignConfig.getBestSignURL();
            HttpURLConnection conn = (HttpURLConnection) new URL(domain + url).openConnection();
            conn.setRequestMethod("GET");
            InputStream in = conn.getInputStream();

            String uploadFile = FtpTool.uploadFile(bestSignConfig.getFtpURL(), 21, "/pdf/", fileName, in, bestSignConfig.getClientUsername(), bestSignConfig.getClientPassword());
            String uploadSuccess = "success";
            if (!uploadSuccess.equals(uploadFile)) {
                return returnFail(0, "上传协议失败");
            }
        }
        if (hygCompany.equals(implementor)) {
            agreementTemplateURL = "https://jrmf360.com?hyggreement.html";
            htmlTemplate = "hyg";
        }
        if (xiaohuangfengCompany.equals(implementor)) {
            agreementTemplateURL = "https://jrmf360.com?bmxhfgreement.html";
            htmlTemplate = "xiaohuangfeng";
        }
        if (agreementTemplate.getChannelType()!=null&&agreementTemplate.getChannelType()==2) {
            agreementTemplateURL = "https://jrmf360.com?ymagreement.html";
            htmlTemplate = "ym";
        }
        agreementTemplate.setAgreementName(agreementName);
        agreementTemplate.setAgreementPayment(agreementPayment);
        agreementTemplate.setAgreementTemplateURL(agreementTemplateURL);
        agreementTemplate.setAgreementType(String.valueOf(agreementType));
        agreementTemplate.setCompanyId(realCompanyId);
        agreementTemplate.setOriginalId(String.valueOf(userId));
        agreementTemplate.setMerchantId(implementor);
        agreementTemplate.setThirdMerchId(thirdMerchId);
        agreementTemplate.setThirdTemplateId(thirdTemplateId);
        agreementTemplate.setThirdTemplateDes(thirdTemplateDes);
        agreementTemplate.setRegType(regType);
        agreementTemplate.setTemplateName(templateName);
        agreementTemplate.setHtmlTemplate(htmlTemplate);
        agreementTemplate.setUploadIdCard(uploadIdCard);
        agreementTemplateService.addAgreementTemplate(agreementTemplate);
        return returnSuccess(null);
    }

    /**
     * 修改
     */
    @RequestMapping(value = "/agreement/template/update", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> updateTemplate(
        @RequestParam("id") String id,
        @RequestParam("realCompanyId") String realCompanyId,
        @RequestParam("thirdMerchId") String thirdMerchId,
        @RequestParam("thirdTemplateId") String thirdTemplateId,
        @RequestParam("templateName") String templateName,
        @RequestParam("thirdTemplateDes") String thirdTemplateDes,
        @RequestParam("agreementName") String agreementName,
        @RequestParam("agreementPayment") String agreementPayment,
        @RequestParam(value = "agreementType") Integer agreementType,
        @RequestParam(value = "channelType") Integer channelType,
        @RequestParam(value = "uploadIdCard") Integer uploadIdCard) throws Exception {
        Map<String, Object> params = new HashMap<>(4);
        params.put("id", id);
        List<AgreementTemplate> templates = agreementTemplateService.getAgreementTemplateByParam(params);
        if (templates.isEmpty()) {
            int code = RespCode.AGREEMENT_TEMPLATE_NOT_FOUND;
            return returnFail(code, RespCode.codeMaps.get(code));
        }
        AgreementTemplate agreementTemplate = templates.get(0);
        String merchantId = companyService.getMerchantIdByUserId(Integer.parseInt(agreementTemplate.getOriginalId()));
        String implementor = merchantId;
        String agreementTemplateURL = "https://jrmf360.com?qzjcagreement.html";
        String fileName = thirdMerchId + "_" + thirdTemplateId + ".pdf";
        String aiyuangongCompany = "aiyuangong";
        String hygCompany = "huiyonggong";
        String xiaohuangfengCompany = "xiaohuangfeng";
        String htmlTemplate = "";
        if (aiyuangongCompany.equals(implementor)) {
            agreementTemplateURL = bestSignConfig.getClientServerNameUrl() + "/pdf/" + fileName;
            String url = "/econtract/extr/template/download?extrSystemId=" + thirdMerchId + "&templateId=" + thirdTemplateId;
            String domain = "https://openapitest.aiyuangong.com";
            HttpURLConnection conn = (HttpURLConnection) new URL(domain + url).openConnection();
            conn.setRequestMethod("GET");
            InputStream in = conn.getInputStream();

            String uploadFile = FtpTool.uploadFile(bestSignConfig.getFtpURL(), 21, "/pdf/", fileName, in, bestSignConfig.getClientUsername(), bestSignConfig.getClientPassword());
            String uploadSuccess = "success";
            if (!uploadSuccess.equals(uploadFile)) {
                return returnFail(0, "上传协议失败");
            }
        }
        if (hygCompany.equals(implementor)) {
            agreementTemplateURL = "https://jrmf360.com?hyggreement.html";
            htmlTemplate = "hyg";
        }
        if (xiaohuangfengCompany.equals(implementor)) {
            agreementTemplateURL = "https://jrmf360.com?bmxhfgreement.html";
            htmlTemplate = "xiaohuangfeng";
        }
        if (agreementTemplate.getChannelType()==2) {
            agreementTemplateURL = "https://jrmf360.com?ymagreement.html";
            htmlTemplate = "ym";
        }
        agreementTemplate.setAgreementType(String.valueOf(agreementType));
        agreementTemplate.setChannelType(channelType);
        agreementTemplate.setMerchantId(implementor);
        agreementTemplate.setHtmlTemplate(htmlTemplate);
        agreementTemplate.setAgreementName(agreementName);
        agreementTemplate.setAgreementPayment(agreementPayment);
        agreementTemplate.setAgreementTemplateURL(agreementTemplateURL);
        agreementTemplate.setCompanyId(realCompanyId);
        agreementTemplate.setThirdMerchId(thirdMerchId);
        agreementTemplate.setThirdTemplateId(thirdTemplateId);
        agreementTemplate.setThirdTemplateDes(thirdTemplateDes);
        agreementTemplate.setTemplateName(templateName);
        agreementTemplate.setUploadIdCard(uploadIdCard);
        agreementTemplateService.updateAgreementTemplate(agreementTemplate);
        return returnSuccess(null);
    }

    /**
     * 删除
     */
    @RequestMapping(value = "/agreement/template/delete", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> deleteTemplate(
        @RequestParam("id") String id) {
        Map<String, Object> params = new HashMap<>(4);
        params.put("id", id);
        List<AgreementTemplate> templates = agreementTemplateService.getAgreementTemplateByParam(params);
        if (templates.isEmpty()) {
            int code = RespCode.AGREEMENT_TEMPLATE_NOT_FOUND;
            return returnFail(code, RespCode.codeMaps.get(code));
        }
        agreementTemplateService.deleteAgreementTemplate(id);
        return returnSuccess(null);
    }

    /**
     * 查询所有实际下发公司
     * @return
     */
    @RequestMapping(value = "/agreement/template/getRealityCompany", method = RequestMethod.POST)
    public Map<String, Object> getRealityCompany() {
        List<Company> company = companyService.listRealityCompany();
        return returnSuccess(company);
    }
}
