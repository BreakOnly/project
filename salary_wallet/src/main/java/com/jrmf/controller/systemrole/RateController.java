package com.jrmf.controller.systemrole;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.ServiceFeeType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.CompanyNetfileRateConf;
import com.jrmf.domain.CompanyRateConf;
import com.jrmf.domain.CustomCompanyRateConf;
import com.jrmf.service.*;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/rate")
public class RateController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(RateController.class);
    private final CustomCompanyRateConfService customCompanyRateConfServiceImpl;
    private final CompanyRateConfService companyRateConfServiceImpl;
    //    private CustomBalanceDao customBalanceService;
    private CustomBalanceService customBalanceService;
    private ChannelRelatedService channelRelatedService;
    @Autowired
    private CompanyNetfileRateConfService companyNetfileRateConfService;

    @Autowired
    public RateController(CustomCompanyRateConfService customCompanyRateConfServiceImpl, CompanyRateConfService companyRateConfServiceImpl, CustomBalanceService customBalanceService, ChannelRelatedService channelRelatedService) {
        this.customCompanyRateConfServiceImpl = customCompanyRateConfServiceImpl;
        this.companyRateConfServiceImpl = companyRateConfServiceImpl;
        this.customBalanceService = customBalanceService;
        this.channelRelatedService = channelRelatedService;
    }

    @RequestMapping("/list")
    @ResponseBody
    public Map<String, Object> listCustomCompanyRateConf(HttpServletRequest request,
                                                         String originalId,
                                                         @RequestParam(defaultValue = "1") Integer pageNo,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        //请求权限判断
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }
        //请求参数判断
        if (StringUtil.isEmpty(originalId)) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }
        Map<String, Object> params = new HashMap<>(5);
        params.put("customKey", originalId);
        PageHelper.startPage(pageNo, pageSize);
        List<CustomCompanyRateConf> list = customCompanyRateConfServiceImpl
            .listCustomCompanyRateConf(params);
        PageInfo<CustomCompanyRateConf> pageInfo = new PageInfo<>(list);

        return returnSuccess(pageInfo.getList(), pageInfo.getTotal());
    }

    @RequestMapping(value = "/realCompanyList",method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> realCompanyList(HttpServletRequest request,String companyId) {
        //请求参数判断
        if (StringUtil.isEmpty(companyId)) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }
        Map<String, Object> params = new HashMap<>(5);
        params.put("customKey", companyId);
        List<Map<String,Object>> realCompanyList = customCompanyRateConfServiceImpl.realCompanyList(params);
        return returnSuccess(realCompanyList);
    }



    @RequestMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCustomCompanyRateConf(HttpServletRequest request,
        CustomCompanyRateConf customCompanyRateConf) {
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }

        Map<String, Object> result = this.checkConf(customCompanyRateConf);
        logger.info("商户配置档位校验返回信息：{}", result);
        if (!"1".equals(result.get("state").toString())) {
            return result;
        }
//        if (!checkConf(customCompanyRateConf)) return returnFail(RespCode.error101, RespCode.OPERATING_FAILED);

        boolean isOverlap = customCompanyRateConfServiceImpl.checkOverlap(customCompanyRateConf.getCustomkey(), customCompanyRateConf.getCompanyId(), customCompanyRateConf.getId(), customCompanyRateConf.getRateConfId());
        if (isOverlap) {
            return returnFail(RespCode.error101, RespCode.RATE_OVERLAP);
        }

        if (customCompanyRateConf.getServiceFeeType() == ServiceFeeType.RECHARGE.getCode()) {
            List<CustomCompanyRateConf> rateConfsList = customCompanyRateConfServiceImpl.getCustomRateConfByKeyAndId(customCompanyRateConf.getCustomkey(), customCompanyRateConf.getCompanyId(), customCompanyRateConf.getId());
            if (rateConfsList.size() > 0 && !rateConfsList.isEmpty()) {
                for (CustomCompanyRateConf c : rateConfsList) {
                    logger.info("输入费率：{}，查出来的费率:{}", customCompanyRateConf.getCustomRate(), ArithmeticUtil.mulStr(c.getCustomRate(), "100"));
                    if (ArithmeticUtil.compareTod(customCompanyRateConf.getCustomRate(), ArithmeticUtil.mulStr(c.getCustomRate(), "100")) != 0) {
                        return returnFail(RespCode.error101, RespCode.SERVICE_FEE_TYPE_ERROR);
                    }
                }
            }
        }

        boolean isSuccess = false;
        String errorMsg = "";
        String customRate = ArithmeticUtil.mulStr(customCompanyRateConf.getCustomRate(), "0.01", 4);
        String mfIncomeRate = ArithmeticUtil.mulStr(customCompanyRateConf.getMfIncomeRate(), "0.01", 4);
        logger.info("---------配置费率：" + customRate + "---------魔方收益率:" + mfIncomeRate);
        customCompanyRateConf.setCustomRate(customRate);
        customCompanyRateConf.setMfIncomeRate(mfIncomeRate);
        if (customCompanyRateConf.getId() == null || customCompanyRateConf.getId().equals(0)) {
            // add
            List<CustomCompanyRateConf> Confs = customCompanyRateConfServiceImpl.getConfByCustomKey(customCompanyRateConf.getCustomkey());
            //校验商户在指定下发公司下的服务费收取类型是否出现多种模式
            boolean checkRateServiceTypeFlag = checkRateServiceType(customCompanyRateConf);
            if (checkRateServiceTypeFlag) {
                isSuccess = customCompanyRateConfServiceImpl.addCustomCompanyRateConf(customCompanyRateConf);
                CompanyRateConf byId = companyRateConfServiceImpl.getById(customCompanyRateConf.getRateConfId());
                boolean flag = true;
                if (Confs != null && Confs.size() != 0) {
                    for (CustomCompanyRateConf conf : Confs) {
                        if (conf.getCompanyId().equals(byId.getCompanyId())) {
                            flag = false;
                        }
                    }
                }
                if (isSuccess && flag) {
                    int[] payTypeArr = {1, 2, 4};
                    for (int i = 0; i < payTypeArr.length; i++) {
                        Map<String, Object> params = new HashMap<>(10);
                        params.put(CommonString.CUSTOMKEY, customCompanyRateConf.getCustomkey());
                        params.put(CommonString.COMPANYID, byId.getCompanyId());
                        params.put(CommonString.PAYTYPE, payTypeArr[i]);
                        customBalanceService.initCustomBalance(params);
                    }

                    ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(customCompanyRateConf.getCustomkey(), byId.getCompanyId() + "");
                    if (channelRelated == null) {
                        channelRelated = new ChannelRelated();
                        channelRelated.setOriginalId(customCompanyRateConf.getCustomkey());
                        channelRelated.setCompanyId(byId.getCompanyId() + "");
                        channelRelated.setMerchantId(byId.getMerchantId());
                        channelRelated.setStatus(1);
                        channelRelated.setProfiltLower("0");
                        channelRelated.setProfiltUpper("0");
                        channelRelated.setServiceRates("0");
                        channelRelated.setUpperServiceRates("0");
                        channelRelated.setAppIdAyg(customCompanyRateConf.getAppId());
                        channelRelatedService.createChannelRelated(channelRelated);
                    }else{
                        channelRelated.setAppIdAyg(customCompanyRateConf.getAppId());
                        channelRelatedService.updateChannelRelated(channelRelated);
                    }
                }
            } else {
                errorMsg = "商户在指定下发公司服务费收取类型只能为同一种";
            }
        } else {
            boolean checkRateServiceTypeFlag = checkRateServiceType(customCompanyRateConf);
            if (checkRateServiceTypeFlag) {
                // update
                ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrigAll(customCompanyRateConf.getCustomkey(), customCompanyRateConf.getCompanyId() + "");
                if (channelRelated != null) {
                    channelRelated.setAppIdAyg(customCompanyRateConf.getAppId());
                    channelRelatedService.updateChannelRelated(channelRelated);
                }
                isSuccess = customCompanyRateConfServiceImpl.updateCustomCompanyRateConf(customCompanyRateConf);
            } else {
                errorMsg = "商户在指定下发公司服务费收取类型只能为同一种";
            }
        }
        if (StringUtil.isEmpty(errorMsg)) {
            return isSuccess ? returnSuccess((Object) null) : returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
        } else {
            return isSuccess ? returnSuccess((Object) null) : returnFail(RespCode.error101, errorMsg);
        }
    }

    /**
     * 校验商户在下发公司下的服务费收取方式是是否出现多种模式
     * serviceFeeType 如果只有一个  可以修改
     *
     * @return 是否可以修改
     */
    private boolean checkRateServiceType(CustomCompanyRateConf customCompanyRateConf) {
        boolean checkServiceType;
        List<Integer> serviceTypeGroup = customCompanyRateConfServiceImpl.getServiceTypeGroup(customCompanyRateConf);
        if (serviceTypeGroup != null && serviceTypeGroup.size() > 1) {
            checkServiceType = false;
        } else {
            if (customCompanyRateConf.getId() == null || customCompanyRateConf.getId().equals(0)) {
                //add
                if (serviceTypeGroup == null || serviceTypeGroup.size() == 0) {
                    checkServiceType = true;
                } else {
                    Integer serviceType = serviceTypeGroup.get(0);
                    checkServiceType = Objects.equals(customCompanyRateConf.getServiceFeeType(), serviceType);
                }
            } else {
                List<Map<String, Object>> confList = customCompanyRateConfServiceImpl.getCustomRateConfList(customCompanyRateConf.getCustomkey(), customCompanyRateConf.getCompanyId() + "");
                if (confList.size() > 1) {
                    String serviceFeeType = confList.get(0).get("serviceFeeType").toString();
                    checkServiceType = serviceFeeType.equals(customCompanyRateConf.getServiceFeeType() + "");
                } else {
                    checkServiceType = true;
                }
            }
        }
        return checkServiceType;
    }

//    private boolean checkConf(CustomCompanyRateConf customCompanyRateConf) {
//        CompanyRateConf byId = companyRateConfServiceImpl.getById(customCompanyRateConf.getRateConfId());
//        List<CustomCompanyRateConf> list = customCompanyRateConfServiceImpl.getConfByCustomKey(customCompanyRateConf.getCustomkey());
//        logger.info("已存在配置项：{}", list);
//        if (customCompanyRateConf.getId() == null || customCompanyRateConf.getId().equals(0)) {
//            for (CustomCompanyRateConf companyRateConf : list) {
//                if (customCompanyRateConf.getRateConfId().equals(companyRateConf.getRateConfId())) return false;
//                if (companyRateConf.getCompanyId().equals(byId.getCompanyId()))
//                    if (!companyRateConf.getFeeRuleType().equals(customCompanyRateConf.getFeeRuleType())) return false;
//            }
//            return true;
//        } else {
//            CustomCompanyRateConf db = customCompanyRateConfServiceImpl.getById(customCompanyRateConf.getId());
//            logger.info("当前修改配置项：{}", db);
//            if (db.getFeeRuleType().equals(customCompanyRateConf.getFeeRuleType()) && db.getRateConfId().equals(customCompanyRateConf.getRateConfId()))
//                return true;
//            int count = 0;
//            for (CustomCompanyRateConf companyRateConf : list)
//                if (companyRateConf.getCompanyId().equals(db.getCompanyId())) ++count;
//            if (count == 1) return true;
//            if (!db.getFeeRuleType().equals(customCompanyRateConf.getFeeRuleType())) return false;
//            for (CustomCompanyRateConf companyRateConf : list)
//                if (companyRateConf.getRateConfId().equals(customCompanyRateConf.getRateConfId())) return false;
//            return true;
//        }
//    }


    private Map<String, Object> checkConf(CustomCompanyRateConf customCompanyRateConf) {
        CompanyRateConf byId = companyRateConfServiceImpl.getById(customCompanyRateConf.getRateConfId());
        List<CustomCompanyRateConf> list = customCompanyRateConfServiceImpl.getConfByCustomKey(customCompanyRateConf.getCustomkey());
        logger.info("已存在配置项：{}", list);

        if (customCompanyRateConf.getId() == null || customCompanyRateConf.getId().equals(0)) {
            for (CustomCompanyRateConf companyRateConf : list) {
                if (customCompanyRateConf.getRateConfId().equals(companyRateConf.getRateConfId())) {
                    return returnFail(RespCode.error101, "请勿重复配置档位");
                }
                if (companyRateConf.getCompanyId().equals(byId.getCompanyId())) {
                    if (!companyRateConf.getFeeRuleType().equals(customCompanyRateConf.getFeeRuleType())) {
                        return returnFail(RespCode.error101, "服务费计算规则请保持一致");
                    }
                }
            }
            return returnSuccess();
        }

        CustomCompanyRateConf db = customCompanyRateConfServiceImpl.getById(customCompanyRateConf.getId());
        logger.info("当前修改配置项：{}", db);
        if (db.getFeeRuleType().equals(customCompanyRateConf.getFeeRuleType()) && db.getRateConfId().equals(customCompanyRateConf.getRateConfId())) {
            return returnSuccess();
        }
        int count = 0;
        for (CustomCompanyRateConf companyRateConf : list) {
            if (companyRateConf.getCompanyId().equals(db.getCompanyId())) {
                ++count;
            }
        }
        if (count == 1) {
            return returnSuccess();
        }

        if (!db.getFeeRuleType().equals(customCompanyRateConf.getFeeRuleType())) {
            return returnFail(RespCode.error101, "服务费计算规则请保持一致");
        }
        for (CustomCompanyRateConf companyRateConf : list) {
            if (companyRateConf.getRateConfId().equals(customCompanyRateConf.getRateConfId())) {
                return returnFail(RespCode.error101, "请勿重复配置档位");
            }
        }
        return returnSuccess();
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Map<String, Object> removeCustomCompanyRateConf(HttpServletRequest request, Integer id) {
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }
        return customCompanyRateConfServiceImpl.deleteCustomCompanyRateConf(id) ? returnSuccess((Object) null) : returnFail(RespCode.error101, RespCode.DELETE_FAILED);
    }

    @RequestMapping("/gearPosition")
    @ResponseBody
    public Map<String, Object> listGearPosition(HttpServletRequest request,  Integer companyId) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }

        if (companyId == null || companyId == 0) return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        List<CompanyRateConf> list = companyRateConfServiceImpl.listGearPosition(companyId);
        return returnSuccess(list);
    }


    @RequestMapping("/netfileGearPosition")
    @ResponseBody
    public Map<String, Object> netfileGearPosition(HttpServletRequest request,  Integer companyId) {
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }
        if (companyId == null || companyId == 0) return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("companyId", companyId);
        List<CompanyNetfileRateConf> list = companyNetfileRateConfService.queryNetfileGearPosition(paramMap);
        return returnSuccess(list);
    }
}
