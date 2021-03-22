package com.jrmf.controller.systemrole.channelRoute;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.CustomThirdPaymentConfig;
import com.jrmf.domain.PaymentChannel;
import com.jrmf.domain.PaymentChannelRoute;
import com.jrmf.service.ChannelRouteService;
import com.jrmf.service.CustomThirdPaymentConfigService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.exception.ChannelRouteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: ChannelRouteController
 * @Description: 通道路由基础配置
 * @create 2020/3/23 14:20
 */
@RestController
@RequestMapping(value = "/channelRoute")
public class ChannelRouteController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ChannelRouteController.class);

    @Autowired
    private ChannelRouteService channelRouteService;
    @Autowired
    private CustomThirdPaymentConfigService customThirdPaymentConfigService;

    /**
     * 查询基础信息
     * @return
     */
    @RequestMapping(value = "/baseQuery")
    public Map<String, Object> baseQuery(HttpServletRequest request, String pathNo, String pathName, String startTime, String endTime, Integer pageSize, Integer pageNo) {
        Map<String, Object> result = new HashMap<>();
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (!CommonString.ROOT.equals(customLogin.getCustomkey()) && !CommonString.ROOT.equals(customLogin.getMasterCustom())) {
            return returnFail(RespCode.error101,"权限不足！");
        }

        Map<String, Object> map = new HashMap<>(7);
        map.put("pathNo", pathNo);
        map.put("pathName", pathName);
        map.put("startDate", startTime);
        map.put("endDate", endTime);

        if (pageNo != null && pageSize != null) {
            PageHelper.startPage(pageNo, pageSize);
        }
        List<PaymentChannel> list = channelRouteService.getChannelRouteBaseQuery(map);
        PageInfo page = new PageInfo(list);
        result.put("list", page.getList());
        result.put("total", page.getTotal());
        return result;
    }

    /**
     * 新增/修改 基础通道路由
     * @param channelRoute
     * @return
     */
    @RequestMapping(value = "/insertChannel")
    public Map<String, Object> insertChannel(PaymentChannel channelRoute) {
        try {
            if (channelRoute.getId() == null) {
                if (StringUtil.isEmpty(channelRoute.getPathNo())) {
                    return returnFail(RespCode.error101, "请输入通道编号");
                }
                String channelRouteByPathNo = channelRouteService.getChannelRouteByPathNo(channelRoute.getPathNo(), null);
                if (!StringUtil.isEmpty(channelRouteByPathNo)) {
                    return returnFail(RespCode.error101, "通道编号已存在，请重新输入");
                }

                if (StringUtil.isEmpty(channelRoute.getPathName())) {
                    return returnFail(RespCode.error101, "请输入通道名称");
                }
                String channelRouteByPathName = channelRouteService.getChannelRouteByPathName(channelRoute.getPathName(), null);
                if (!StringUtil.isEmpty(channelRouteByPathName)) {
                    return returnFail(RespCode.error101, "通道名称已存在，请重新输入");
                }
                channelRouteService.insertChannel(channelRoute);
            } else {
                String channelRouteByPathNo = channelRouteService.getChannelRouteByPathNo(channelRoute.getPathNo(), channelRoute.getId());
                if (!StringUtil.isEmpty(channelRouteByPathNo)) {
                    return returnFail(RespCode.error101, "通道编号已存在，请重新输入");
                }

                String channelRouteByPathName = channelRouteService.getChannelRouteByPathName(channelRoute.getPathName(), channelRoute.getId());
                if (!StringUtil.isEmpty(channelRouteByPathName)) {
                    return returnFail(RespCode.error101, "通道名称已存在，请重新输入");
                }

                // 资金联动关联记录
                PaymentChannel route = channelRouteService.getChannelRouteById(channelRoute.getId());
                if (route == null) {
                    return returnFail(RespCode.error101, "通道不存在，请刷新页面");
                }
                List<Map<String, Object>> linkageBaseList = this.getChannelRouteAndLinkageBaseByPathNo(route.getPathNo());
                if (linkageBaseList != null && linkageBaseList.size() > 0) {
                    return returnFail(RespCode.error101, "资金联动已与本通道绑定，无法修改");
                }

                // 通道路由关联记录
                List<Map<String, Object>> channelRouteRelationList = this.getChannelRouteRelationByPathNo(route.getPathNo());
                if (channelRouteRelationList != null && channelRouteRelationList.size() > 0) {
                    return returnFail(RespCode.error101, "服务公司已与本通道绑定，无法修改");
                }

                channelRouteService.updateChannel(channelRoute);
            }
        } catch (Exception e) {
            logger.error("配置通道路由失败：{}", e);
            return returnFail(RespCode.error101, e.getMessage());
        }
        return returnSuccess();
    }

    public List<Map<String, Object>> getChannelRouteAndLinkageBaseByPathNo(String pathNo) {
        List<Map<String, Object>> linkageList = channelRouteService.getChannelRouteAndLinkageBaseByPathNo(pathNo);
        return linkageList;
    }

    public List<Map<String, Object>> getChannelRouteRelationByPathNo(String pathNo) {
        List<Map<String, Object>> relationList = channelRouteService.getChannelRouteRelationByPathNo(pathNo);
        return relationList;
    }

    /**
     * 删除通道
     * @param id
     * @param pathNo
     * @return
     */
    @RequestMapping(value = "/deleteChannel")
    public Map<String, Object> deleteChannel(String id, String pathNo) {
        if (StringUtil.isEmpty(id)) {
            return returnFail(RespCode.error101, "参数异常");
        }

        // 资金联动关联记录
        List<Map<String, Object>> linkageList = this.getChannelRouteAndLinkageBaseByPathNo(pathNo);
        if (linkageList != null && linkageList.size() > 0) {
            return returnFail(RespCode.error101, "资金联动已与本通道绑定，无法删除");
        }

        // 通道路由关联记录
        List<Map<String, Object>> channelRouteRelationList = this.getChannelRouteRelationByPathNo(pathNo);
        if (channelRouteRelationList != null && channelRouteRelationList.size() > 0) {
            return returnFail(RespCode.error101, "服务公司已与本通道绑定，无法删除");
        }

        try {
            channelRouteService.deleteChannel(id);
        } catch (Exception e) {
            logger.error("删除通道路由失败：{}", e);
            return returnFail(RespCode.error101, "删除通道失败，请联系管理员");
        }
        return returnSuccess();
    }


    /**
     * 查看下发公司配置通道路由
     * @return
     */
    @RequestMapping(value = "/serviceCompanyChannelRoute")
    public Map<String, Object> serviceCompanyChannelRoute(HttpServletRequest request, String pathNo, String pathName, String startTime, String endTime, String companyName, String paymentType, String isSubAccount, Integer pageSize, Integer pageNo) {
        Map<String, Object> result = new HashMap<>();
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (!CommonString.ROOT.equals(customLogin.getCustomkey()) && !CommonString.ROOT.equals(customLogin.getMasterCustom())) {
            return returnFail(RespCode.error101,"权限不足！");
        }

        Map<String, Object> map = new HashMap<>(10);
        map.put("pathNo", pathNo);
        map.put("pathName", pathName);
        map.put("startDate", startTime);
        map.put("endDate", endTime);
        map.put("companyName", companyName);
        map.put("paymentType", paymentType);
        map.put("isSubAccount", isSubAccount);

        PageHelper.startPage(pageNo, pageSize);
        List<PaymentChannelRoute> list = channelRouteService.getServiceCompanyChannelRoute(map);
        PageInfo page = new PageInfo(list);
        result.put("list", page.getList());
        result.put("total", page.getTotal());
        return result;
    }

    /**
     * 新增/修改下发公司通道路由信息
     * @param paymentChannelRoute
     * @return
     */
    @RequestMapping(value = "/serviceCompanyChannelRouteConfig")
    @Transactional
    public Map<String, Object> serviceCompanyChannelRouteConfig(PaymentChannelRoute paymentChannelRoute) {
        Map<String, Object> result;
        try {
            result = channelRouteService.serviceCompanyChannelRouteConfig(paymentChannelRoute);
        } catch (ChannelRouteException e) {
            logger.error("配置失败：{}", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return returnFail(RespCode.error101, e.getMessage());
        } catch (Exception e) {
            logger.error("配置失败：{}", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return returnFail(RespCode.error101,"配置失败，请联系管理员");
        }
        return result;
    }

    /**
     * 删除下发公司通道路由信息
     */
    @RequestMapping(value = "/deleteServiceCompanyChannelRoute")
    @Transactional
    public Map<String, Object> deleteServiceCompanyChannelRoute(String companyId, String paymentType, String pathNo, String customKey, String isDefault) {
        Map<String, Object> result;
        try {
            // 啊啊啊
            result = channelRouteService.deleteServiceCompanyChannelRoute(companyId, paymentType, pathNo, customKey, isDefault);
        } catch (Exception e) {
            logger.error("删除失败：{}", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return returnFail(RespCode.error101,"删除失败，请联系管理员");
        }
        return result;
    }

    @PostMapping(value = "/customThirdPaymentConfigList")
    public Map<String, Object> customThirdPaymentConfigList(HttpServletRequest request,
        String customName, String startTime, String endTime,
        String pathNo, String configType, String thirdMerchid,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {

        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isRootAdmin(channelCustom)) {
            return returnFail(RespCode.error101, RespCode.PERMISSIONERROR);
        }

        try {

            Map<String, Object> result = new HashMap<>(5);

            PageHelper.startPage(pageNo, pageSize);
            List<CustomThirdPaymentConfig> list = customThirdPaymentConfigService
                .listAllByParam(customName, startTime, endTime, pathNo, configType, thirdMerchid);
            PageInfo<CustomThirdPaymentConfig> pageInfo = new PageInfo<>(list);

            result.put("list", pageInfo.getList());
            result.put("total", pageInfo.getTotal());
            return returnSuccess(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }

    }

    @PostMapping(value = "/saveOrUpdateThirdPaymentConfig")
    public Map<String, Object> saveOrUpdateThirdPaymentConfig(HttpServletRequest request,
        CustomThirdPaymentConfig customThirdPaymentConfig) {

        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isRootAdmin(channelCustom)) {
            return returnFail(RespCode.error101, RespCode.PERMISSIONERROR);
        }

        try {
            CustomThirdPaymentConfig thirdPaymentConfig = customThirdPaymentConfigService
                .getByCustomKeyAndPathNo(customThirdPaymentConfig.getCustomKey(),
                    customThirdPaymentConfig.getPathNo(), customThirdPaymentConfig.getId());
            if (thirdPaymentConfig == null) {
                int count = customThirdPaymentConfigService
                    .saveOrUpdateConfig(customThirdPaymentConfig);
                if (count > 0) {
                    return returnSuccess();
                }
                return returnFail(RespCode.error107, RespCode.OPERATING_FAILED);

            } else {
                return returnFail(RespCode.error107, RespCode.CUSTOM_THIRD_CONFIG_EXIST);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }

    }

    @PostMapping(value = "/deleteThirdPaymentConfig")
    public Map<String, Object> deleteThirdPaymentConfig(HttpServletRequest request,
        CustomThirdPaymentConfig customThirdPaymentConfig) {

        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isRootAdmin(channelCustom)) {
            return returnFail(RespCode.error101, RespCode.PERMISSIONERROR);
        }

        try {
            int count = customThirdPaymentConfigService
                .deleteByPrimaryKey(customThirdPaymentConfig.getId());
            if (count > 0) {
                return returnSuccess();
            }
            return returnFail(RespCode.error107, RespCode.OPERATING_FAILED);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }

    }
}
