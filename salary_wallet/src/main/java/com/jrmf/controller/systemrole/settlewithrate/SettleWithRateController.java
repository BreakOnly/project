package com.jrmf.controller.systemrole.settlewithrate;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.controller.systemrole.merchant.WalletCompanyController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.OrganizationNode;
import com.jrmf.domain.SettleWithRate;
import com.jrmf.persistence.CustomGroupDao;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.service.SettleWithRateService;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @Title: SettleWithRateController
 * @Description: 变更费率
 * @create 2020/2/14 16:31
 */
@RestController
@RequestMapping("/settleWithRate")
public class SettleWithRateController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SettleWithRateController.class);

    @Autowired
    private SettleWithRateService settleWithRateService;

    @Autowired
    private ChannelCustomService customService;

    @Autowired
    private WalletCompanyController walletCompanyController;

    @Autowired
    private OrganizationTreeService organizationTreeService;

    @Autowired
    private CustomProxyDao customProxyDao;

    @Autowired
    private CustomGroupDao customGroupDao;


    /**
     * 变更费率查询
     *
     * @param companyName
     * @param customType
     * @param startTime
     * @param endTime
     * @param pageSize
     * @param pageNo
     * @return
     */
    @RequestMapping(value = "/querySettleWithRate")
    public Map<String, Object> querySettleWithRate(@RequestParam(value = "companyName", required = false) String companyName,
                                                   @RequestParam(value = "customType", required = false) String customType,
                                                   @RequestParam(value = "startTime", required = false) String startTime,
                                                   @RequestParam(value = "endTime", required = false) String endTime,
                                                   @RequestParam(value = "pageSize", required = false, defaultValue = "10") String pageSize,
                                                   @RequestParam(value = "pageNo") String pageNo) {
        Map<String, Object> result = new HashMap<>(10);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        Map<String, Object> paramMap = new HashMap<>(9);
        paramMap.put("companyName", companyName);
        paramMap.put("customType", customType);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);

        try {
            int total = settleWithRateService.querySettleWithRateCount(paramMap);
            if (!StringUtil.isEmpty(pageNo)) {
                paramMap.put("start", getFirst(pageNo, pageSize));
                paramMap.put("limit", Integer.parseInt(pageSize));
            }
            List<SettleWithRate> list = settleWithRateService.querySettleWithRate(paramMap);
            result.put("list", list);
            result.put("total", total);
        } catch (Exception e) {
            logger.error("查询变更费率失败{}", e);
            return returnFail(RespCode.error101, "查询失败，请联系管理员");
        }
        return result;
    }

    /**
     * 新增变更费率
     *
     * @param settleWithRate
     * @param request
     * @return
     */
    @RequestMapping(value = "/insertSettleWithRate")
    public Map<String, Object> insertSettleWithRate(SettleWithRate settleWithRate, HttpServletRequest request) {
        Map<String, Object> result;
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        try {

            Optional.ofNullable(settleWithRate)
                    .map(SettleWithRate::getModifyRate)
                    .filter(withRate -> ArithmeticUtil.compareTod(withRate, "0") != -1)
                    .orElseThrow(() -> new Exception("请输入正确费率"));
            settleWithRate.setModifyRate(ArithmeticUtil.divideStr(settleWithRate.getModifyRate(), "100"));

            Optional.ofNullable(settleWithRate)
                    .filter(rate -> !StringUtil.isEmpty(rate.getCustomkey()) && !StringUtil.isEmpty(rate.getCustomType() + "") && !StringUtil.isEmpty(rate.getCompanyId() + "") && !StringUtil.isEmpty(rate.getNetfileId() + ""))
                    .orElseThrow(() -> new Exception("网络错误"));

            Map<String, Object> map = new HashMap<>();
            map.put("customkey", settleWithRate.getCustomkey());
            map.put("customType", settleWithRate.getCustomType());
            map.put("netfileId", settleWithRate.getNetfileId());
            map.put("companyId", settleWithRate.getCompanyId());
            map.put("startTime", settleWithRate.getModifyEffectStartTime());
            map.put("endTime", settleWithRate.getModifyEffectEndTime());

            if (settleWithRateService.querySettleWithRateByParamCount(map) > 0) {
                return returnFail(RespCode.INSERT_FAIL, "档位生效时间不可重叠");
            }

            settleWithRate.setModifyAddUser(loginUser.getUsername());
            result = settleWithRateService.insertSettleWithRate(settleWithRate);
        } catch (Exception e) {
            logger.error("新增变更费率失败{}", e);
            return returnFail(RespCode.INSERT_FAIL, e.getMessage());
        }
        return result;
    }

    /**
     * 根据商户类型查询商户名称
     *
     * @param customType
     * @param customkey
     * @return
     */
    @RequestMapping(value = "/queryMerchantInfo")
    public Map<String, Object> queryMerchantInfo(@RequestParam(value = "customType", required = false) String customType,
                                                 @RequestParam(value = "customkey", required = false) String customkey,
                                                 @RequestParam(value = "companyId", required = false) String companyId) {
        Map<String, Object> result = new HashMap<>(10);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        Map<String, Object> param = new HashMap<>();
        List<Map<String, Object>> list;

        if (StringUtil.isEmpty(companyId)) {
            if (StringUtil.isEmpty(customkey)) {
                if (!StringUtil.isEmpty(customType)) {
                    List<Map<String, Object>> customByCustomList = customService.getCustomByCustomType(customType);
                    result.put("list", customByCustomList);
                    return result;
                } else {
                    return returnFail(RespCode.error101, "请选择商户类型");
                }
            } else {
                param.put("customkey", customkey);
                if (CustomType.PROXY.getCode() == Integer.parseInt(customType)) {
                    list = settleWithRateService.queryProxyInfo(param);
                } else {
                    list = settleWithRateService.queryMerchantInfo(param);
                }
                result.put("list", list);
            }
        } else {
            if (!StringUtil.isEmpty(customkey)) {
                param.put("customkey", customkey);
            } else {
                return returnFail(RespCode.error101, "请选择商户");
            }
            param.put("companyId", companyId);
            if (CustomType.PROXY.getCode() == Integer.parseInt(customType)) {
                list = settleWithRateService.queryProxyInfoAndCompanyId(param);
            } else {
                list = settleWithRateService.queryMerchantInfoAndCompanyId(param);
            }
            result.put("list", list);
        }

        return result;
    }

    /**
     * 商户费率查询
     *
     * @return
     */
    @RequestMapping(value = "/merchantRateQuery")
    public Map<String, Object> merchantRateQuery(HttpServletRequest request, String companyName, int pageSize, int pageNo, String customName, String startRate, String endRate) {
        Map<String, Object> result = new HashMap<>(7);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        Map<String, String> param = new HashMap<>(8);
        param.put("companyName", companyName);
        param.put("customName", customName);
        param.put("startRate", startRate);
        param.put("endRate", endRate);

        //校验是否有权限
        boolean flag = false;
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (customLogin.getCustomType() == CustomType.ROOT.getCode() && customLogin.getMasterCustom() != null) {
            customLogin = customService.getCustomByCustomkey(customLogin.getMasterCustom());
        }
        // 权限为：服务公司、代理商、超管
        Integer[] allowCustomType = new Integer[]{CustomType.COMPANY.getCode(), CustomType.PROXY.getCode(), 6};
        flag = customService.getCustomKeysByType(param, allowCustomType, customLogin);
        List<Map<String, String>> list = new ArrayList<>();
        logger.info("是否符合权限:{},商户权限：{}", flag, customLogin.getCustomType());
        if (flag) {
            if (customLogin.getCustomType() == CustomType.COMPANY.getCode()) {
                String companyId = param.get("companyId");
                logger.info("服务公司userId:{}", companyId);
                if (!StringUtil.isEmpty(companyId)) {
                    PageHelper.startPage(pageNo, pageSize);
                    list = settleWithRateService.queryMerchantRateByParam(param);
                }
            } else if (customLogin.getCustomType() == CustomType.PROXY.getCode()) {
                String customkey = param.get("customkey");
                logger.info("代理商唯一标识：{}", customkey);
                if (!StringUtil.isEmpty(customkey)) {
                    //判断是不是关联性代理商
                    if (customLogin.getProxyType() == 1) {
                        List<String> allList = new ArrayList<>();
                        OrganizationNode node = customProxyDao.getProxyChildenNodeByCustomKey(customkey,null);
                        List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());

                        if (stringList != null && stringList.size() > 0) {
                            allList.addAll(stringList);
                            String joinCustomkey = String.join(",", stringList);
                            List<OrganizationNode> customkeyList = customProxyDao.getCustomProxyByLevelCode(joinCustomkey);
                            if (customkeyList.size() > 0 && !customkeyList.isEmpty()) {
                                for (OrganizationNode o : customkeyList) {
                                    allList.add(o.getCustomKey());
                                    if (o.getCustomType() == CustomType.GROUP.getCode()) {
                                        OrganizationNode organizationNode = customGroupDao.getCustomGroupByCustomkey(o.getCustomKey());
                                        List<String> groupList = organizationTreeService.queryNodeCusotmKey(CustomType.GROUP.getCode(),QueryType.QUERY_CURRENT_AND_CHILDREN, organizationNode.getId());
                                        if (groupList.size() > 0 && !groupList.isEmpty()) {
                                            allList.addAll(groupList);
                                        }
                                    }
                                }
                                String customKeys = Joiner.on(",").join(allList);
                                logger.info("代理商旗下商户唯一标识：{}", customKeys);
                                param.put("customkey", String.join(",", customKeys));
                                PageHelper.startPage(pageNo, pageSize);
                                list =  settleWithRateService.queryMerchantRateByParam(param);
                            }

                        }
                    } else {
                        OrganizationNode node = customProxyDao.getNodeByCustomKey(customkey,null);
                        List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
                        if (stringList.size() > 0 && !stringList.isEmpty()) {
                            String customKeys = Joiner.on(",").join(stringList);
                            logger.info("代理商旗下商户唯一标识：{}", customKeys);
                            param.put("customkey", customKeys);
                            PageHelper.startPage(pageNo, pageSize);
                            list = settleWithRateService.queryMerchantRateByParam(param);
                        }
                    }
                }
            } else if (CommonString.ROOT.equals(customLogin.getCustomkey())) {
                PageHelper.startPage(pageNo, pageSize);
                list = settleWithRateService.queryMerchantRateByParam(param);
            }
            PageInfo page = new PageInfo(list);
            result.put("total", page.getTotal());
            result.put("list", page.getList());
        } else {
            result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT));
        }

        return result;
    }

    /**
     * 商户费率查询
     *
     * @return
     */
    @RequestMapping(value = "/merchantRateQueryExport")
    public void merchantRateQueryExport(HttpServletRequest request, HttpServletResponse response, String companyName, String customName, String startRate, String endRate) {
        Map<String, String> param = new HashMap<>(8);
        param.put("companyName", companyName);
        param.put("customName", customName);
        param.put("startRate", startRate);
        param.put("endRate", endRate);

        //校验是否有权限
        boolean flag = false;
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (customLogin.getCustomType() == CustomType.ROOT.getCode() && customLogin.getMasterCustom() != null) {
            customLogin = customService.getCustomByCustomkey(customLogin.getMasterCustom());
        }
        // 权限为：服务公司、代理商、超管
        Integer[] allowCustomType = new Integer[]{CustomType.COMPANY.getCode(), CustomType.PROXY.getCode(), 6};
        flag = customService.getCustomKeysByType(param, allowCustomType, customLogin);
        List<Map<String, String>> list = new ArrayList<>();
        logger.info("是否符合权限:{},商户类型：{}", flag, customLogin.getCustomType());
        if (flag) {
            if (customLogin.getCustomType() == CustomType.COMPANY.getCode()) {
                String companyId = param.get("companyId");
                logger.info("服务公司userId:{}", companyId);
                if (!StringUtil.isEmpty(companyId)) {
                    list = settleWithRateService.queryMerchantRateByParam(param);
                }
            } else if (customLogin.getCustomType() == CustomType.PROXY.getCode()) {
                String customkey = param.get("customkey");
                logger.info("代理商唯一标识：{}", customkey);
                if (!StringUtil.isEmpty(customkey)) {
                    //判断是不是关联性代理商
                    if (customLogin.getProxyType() == 1) {
                        List<String> allList = new ArrayList<>();
                        OrganizationNode node = customProxyDao.getProxyChildenNodeByCustomKey(customkey,null);
                        List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());

                        if (stringList != null && stringList.size() > 0) {
                            allList.addAll(stringList);
                            String joinCustomkey = String.join(",", stringList);
                            List<OrganizationNode> customkeyList = customProxyDao.getCustomProxyByLevelCode(joinCustomkey);
                            if (customkeyList.size() > 0 && !customkeyList.isEmpty()) {
                                for (OrganizationNode o : customkeyList) {
                                    allList.add(o.getCustomKey());
                                    if (o.getCustomType() == CustomType.GROUP.getCode()) {
                                        OrganizationNode organizationNode = customGroupDao.getCustomGroupByCustomkey(o.getCustomKey());
                                        List<String> groupList = organizationTreeService.queryNodeCusotmKey(CustomType.GROUP.getCode(),QueryType.QUERY_CURRENT_AND_CHILDREN, organizationNode.getId());
                                        if (groupList.size() > 0 && !groupList.isEmpty()) {
                                            allList.addAll(groupList);
                                        }
                                    }
                                }
                                String customKeys = Joiner.on(",").join(allList);
                                logger.info("代理商旗下商户唯一标识：{}", customKeys);
                                param.put("customkey", String.join(",", customKeys));
                                list =  settleWithRateService.queryMerchantRateByParam(param);
                            }

                        }
                    } else {
                        OrganizationNode node = customProxyDao.getNodeByCustomKey(customkey,null);
                        List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
                        if (stringList.size() > 0 && !stringList.isEmpty()) {
                            String customKeys = Joiner.on(",").join(stringList);
                            logger.info("代理商旗下商户唯一标识：{}", customKeys);
                            param.put("customkey", customKeys);
                            list = settleWithRateService.queryMerchantRateByParam(param);
                        }
                    }
                }
            } else if (CommonString.ROOT.equals(customLogin.getCustomkey())) {
                list = settleWithRateService.queryMerchantRateByParam(param);
            }
        }

        List<Map<String, Object>> data = new ArrayList<>();
        String[] colunmName = new String[]{"商户名称", "服务公司", "商户承担费率", "档位开始金额", "档位结束金额"};
        String filename = "商户费率表";
        for (Map<String, String> dataResult : list) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", dataResult.get("customName"));
            dataMap.put("2", dataResult.get("companyName"));
            dataMap.put("3", dataResult.get("rate"));
            dataMap.put("4", dataResult.get("amountStart"));
            dataMap.put("5", dataResult.get("amountEnd"));
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }
}
