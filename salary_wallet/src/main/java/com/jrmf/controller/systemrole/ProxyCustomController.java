package com.jrmf.controller.systemrole;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.AgainCalculateType;
import com.jrmf.controller.constant.CustomCostMaintainCountType;
import com.jrmf.controller.constant.GearLaberType;
import com.jrmf.controller.constant.ProxyType;
import com.jrmf.domain.*;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.CustomProxyService;
import com.jrmf.service.ProxyCustomService;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019年10月31日14:49:55
 * @desc 代理商分佣统计
 **/
@Controller
@RequestMapping("proxycustom")
public class ProxyCustomController extends BaseController {
    private final ProxyCustomService proxyCustomService;
    private final CustomProxyService customProxyService;
    private final CustomProxyDao customProxyDao;

    @Autowired
    private ChannelCustomService channelCustomService;

    private static Logger logger = LoggerFactory.getLogger(ProxyCustomController.class);

    @Autowired
    public ProxyCustomController(ProxyCustomService proxyCustomService, CustomProxyService customProxyService, CustomProxyDao customProxyDao) {
        this.proxyCustomService = proxyCustomService;
        this.customProxyService = customProxyService;
        this.customProxyDao = customProxyDao;
    }

    /**
     * 代理商分佣统计报表-----列表查询接口
     * businessPlatformId
     */
    @RequestMapping("list")
    @ResponseBody
    public Map<String, Object> list(HttpServletRequest request) {
        int respstat = RespCode.success;
        HashMap<String, Object> result = new HashMap<>(8);
        Page page = new Page(request);
        //获取登陆信息
        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (!isRootAdmin(channelCustom) && !isPlatformAdminAccount(channelCustom)) {
            return returnFail(RespCode.error101, "非管理员无法进行此查询");
        }
        //是否是平台商户
        if (isPlatformAdminAccount(channelCustom)){
            //平台商户
            Integer platformId = checkCustom(channelCustom);
            page.getParams().put("businessPlatformId", String.valueOf(platformId));
        }
        PageHelper.startPage(page.getPageNo(),page.getPageSize());
        List<CustomProxySubCommission> relationList = proxyCustomService.listByPage(page);
        PageInfo<CustomProxySubCommission> pageInfo = new PageInfo<>(relationList);
        result.put("total", pageInfo.getTotal());
        result.put("subCommissionList", pageInfo.getList());
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        return result;
    }

    /**
     * 代理商分佣统计报表-----导出
     */
    @RequestMapping("export")
    public void export(HttpServletRequest request, HttpServletResponse response) {
        Page page = new Page(request);
        //获取登陆信息
        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute("customLogin");
        //是否是平台商户
        if (isPlatformAdminAccount(channelCustom)){
            //平台商户
            Integer platformId = checkCustom(channelCustom);
            page.getParams().put("businessPlatformId", String.valueOf(platformId));
        }
        List<CustomProxySubCommission> relationList = proxyCustomService.listByPage(page);
        List<Map<String, Object>> data = new ArrayList<>();
        String filename = "代理商分佣统计";
        String[] columnName = new String[]{"日期", "代理商名称", "商户名称", "关联商户类型", "服务公司名称", "商户下发额",
                "返佣金额", "商户承担服务费", "代理商成本费率","变更费率","变更后成本服务费","变更生效时间","变更费率操作者", "金额档位标签", "档位最小金额", "金额范围运算符", "档位最大金额", "商户直接代理名称", "统计计算方式", "下级代理差额收益", "创建时间", "最后更新时间"};
        for (CustomProxySubCommission customProxySubCommission : relationList) {

            Map<String, Object> dataMap = new HashMap<>(30);
            dataMap.put("1", customProxySubCommission.getTime());
            dataMap.put("2", customProxySubCommission.getProxyCustomName());
            dataMap.put("3", customProxySubCommission.getCustomName());
            dataMap.put("4", ProxyType.codeOfDefault(customProxySubCommission.getProxyType()).getDesc());
            dataMap.put("5", customProxySubCommission.getCompanyName());
            dataMap.put("6", customProxySubCommission.getAmount());
            dataMap.put("7", customProxySubCommission.getReturnCommissionAmount());
            dataMap.put("8", customProxySubCommission.getCustomServiceFee());
            dataMap.put("9", customProxySubCommission.getProxyFeeRate());
            dataMap.put("10", customProxySubCommission.getModifyRate());
            dataMap.put("11", customProxySubCommission.getModifyProxyFee());
            dataMap.put("12", customProxySubCommission.getModifyEffectStartTime()+"-"+customProxySubCommission.getModifyEffectEndTime());
            dataMap.put("13", customProxySubCommission.getModifyAddUser());

            dataMap.put("14", GearLaberType.codeOfDefault(customProxySubCommission.getGearLabel()).getDesc() );
            dataMap.put("15", customProxySubCommission.getAmountStart());
            dataMap.put("16", customProxySubCommission.getOperator());
            dataMap.put("17", customProxySubCommission.getAmountEnd());
            dataMap.put("18", customProxySubCommission.getDirectProxyCustomName());
            dataMap.put("19", CustomCostMaintainCountType.codeOfDefault(customProxySubCommission.getCountType()).getDesc()  );
            dataMap.put("20", customProxySubCommission.getNextLevelProxyDiffEarnRate());
            dataMap.put("21", customProxySubCommission.getCreateTime());
            dataMap.put("22", customProxySubCommission.getUpdateTime());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, columnName, filename, data);
    }

    /**
     * 代理商分佣统计报表-----重新发起结算单 
     */
    @RequestMapping("recalculate")
    @ResponseBody
    public Map<String, Object> recalculate(HttpServletRequest request, @RequestParam("againType") int againType,
                                           @RequestParam(value = "merchantId", required = false) String merchantId,
                                           @RequestParam(value = "businessPlatformId", required = false) String businessPlatformId,
                                           @RequestParam("month") String month) {
        HashMap<String, Object> result = new HashMap<>(8);
        //获取登陆信息
        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (!isRootAdmin(channelCustom) && !isPlatformAdminAccount(channelCustom)) {
            return returnFail(RespCode.error101, "非管理员无法进行此操作");
        }
        if (isPlatformAdminAccount(channelCustom)){
            //平台商户
            Integer platformId = checkCustom(channelCustom);
            businessPlatformId = String.valueOf(platformId);
        }
        //判断选择的商户是否在当前平台下
        if (!StringUtil.isEmpty(merchantId)){
            Map<String,Object> params = new HashMap<>();
            params.put("businessPlatformId",businessPlatformId);
            params.put("customkey",merchantId);
            List<ChannelCustom> channelCustomList = channelCustomService.getCustomByParam(params);
            if (channelCustomList == null || channelCustomList.size() == 0){
                return returnFail(RespCode.PLATFORM_NOT_MERCHANT, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_MERCHANT));
            }
        }
        int respstat = RespCode.success;
        String processId = MDC.get(PROCESS);
        String finalBusinessPlatformId = businessPlatformId;
        ThreadUtil.pdfThreadPool.execute(() -> {
            MDC.put(PROCESS,processId);
            AgainCalculateType againCalculateType = AgainCalculateType.codeOf(againType);
            if(againCalculateType == AgainCalculateType.CUSTOM_PROXY){
                CustomProxy customProxy = customProxyService.getProxyIdByCustomkey(merchantId);
                int parentId = customProxy.getParentId();
                if(parentId != 0){
                    OrganizationNode node = customProxyDao.getNodeById(customProxy.getParentId(),null);
                    String customKey = node.getCustomKey();
                    Map<Object, Object> map = new HashMap<>(4);
                    map.put(AgainCalculateType.CUSTOM_PROXY.getCustomType(),customKey);
                    map.put("time",month);
                    map.put("proxyType",2);
                    proxyCustomService.deleteByParam(map);
                }
            }
            proxyCustomService.calculate(againType,merchantId,month, finalBusinessPlatformId);
            MDC.remove(PROCESS);
        });
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        return result;
    }

}























