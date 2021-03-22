package com.jrmf.controller.systemrole;

import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.SplitOrderConf;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.service.SplitOrderConfService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/splitconf")
public class SplitOrderConfController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SplitOrderConfController.class);
    @Autowired
    private ChannelCustomService customService;
    @Autowired
    private ChannelRelatedService channelRelatedService;
    @Autowired
    private SplitOrderConfService splitOrderConfService;
    @Autowired
    private OrganizationTreeService organizationTreeService;

    @RequestMapping("/custom/info")
    public Map<String, Object> getCustomInfo(HttpServletRequest request) {
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
            if (masterCustom != null) {
                loginUser = masterCustom;
            }
        }

        List<ChannelCustom> customList = new ArrayList<>();
        if (CommonString.ROOT.equals(loginUser.getCustomkey())) {
            customList = customService.getAllCustom();
        } else if (loginUser.getCustomType() == CustomType.CUSTOM.getCode()) {
            customList = customService.queryGroupCustom(loginUser.getCustomkey());
        } else if (loginUser.getCustomType() == CustomType.GROUP.getCode()) {
            int nodeId = organizationTreeService.queryNodeIdByCustomKey(loginUser.getCustomkey());
            List<String> customKeys = organizationTreeService.queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
            if (customKeys == null || customKeys.size() == 0) {
                return returnFail(RespCode.error101, RespCode.RELATIONSHIP_DOES_NOT_EXIST);
            }
            customList = customService.queryGroupCustom(String.join(",", customKeys));
        } else if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
            List<String> customKeys = channelRelatedService.queryCustomKeysByCompanyId(loginUser.getCustomkey());
            if (customKeys == null || customKeys.size() == 0) {
                return returnFail(RespCode.error101, RespCode.RELATIONSHIP_DOES_NOT_EXIST);
            }
            customList = customService.queryGroupCustom(String.join(",", customKeys));
        }

        return returnSuccess(customList);
    }

    @RequestMapping("/company/info")
    public Map<String, Object> getCustomInfo(HttpServletRequest request, String customKey) {
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

        List<ChannelRelated> relatedList = new ArrayList<>();
        if (CustomType.COMPANY.getCode() == loginUser.getCustomType()) {
            ChannelRelated related = new ChannelRelated();
            related.setCompanyName(loginUser.getCompanyName());
            related.setCompanyId(loginUser.getCustomkey());
            relatedList.add(related);
        } else {
            relatedList = channelRelatedService.getRelatedList(customKey);
        }

        return returnSuccess(relatedList);
    }


    @RequestMapping("/list")
    public Map<String, Object> getSplitOrderConfList(HttpServletRequest request, String customKey, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize, String timeStart, String timeEnd, String customName) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
        Map<String, Object> params = new HashMap<>();

        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            ChannelCustom masterCustom = customService.getCustomByCustomkey(loginUser.getMasterCustom());
            if (masterCustom != null) {
                loginUser = masterCustom;
            }
        }

        if (CommonString.ROOT.equals(loginUser.getCustomkey())) {
            params.put("customKey", customKey);
        } else if (loginUser.getCustomType() == CustomType.CUSTOM.getCode()) {
            params.put("customKey", loginUser.getCustomkey());
        } else if (loginUser.getCustomType() == CustomType.GROUP.getCode()) {
            int nodeId = organizationTreeService.queryNodeIdByCustomKey(loginUser.getCustomkey());
            List<String> customKeys = organizationTreeService.queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
            if (customKeys == null || customKeys.size() == 0) {
                return returnFail(RespCode.error101, RespCode.RELATIONSHIP_DOES_NOT_EXIST);
            }
            params.put("customKeys", String.join(",", customKeys));
        } else {
            return returnFail(RespCode.error101, "权限错误");
        }

        params.put("customName", customName);
        params.put("timeStart", timeStart);
        params.put("timeEnd", timeEnd);
        int total = splitOrderConfService.getConfByCustomKey(params).size();
        params.put("start", (pageNo - 1) * pageSize);
        params.put("limit", pageSize);
        List<SplitOrderConf> result = splitOrderConfService.getConfByCustomKey(params);
        return returnSuccess(result, total);
    }

    @RequestMapping("/delete")
    public Map<String, Object> deleteSplitOrderConf(String customKey, String companyId) {
        logger.info("拆单配置删除，customKey = {},companyId = {}", customKey, companyId);
        boolean delSuccess = splitOrderConfService.deleteSplitOrderConf(customKey, companyId);
        return delSuccess ? returnSuccess(null) : returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
    }

    @RequestMapping("/update")
    public Map<String, Object> addAndUpdateSplitOrderConf(SplitOrderConf splitOrderConf) {
        SplitOrderConf splitOrderConfDB = splitOrderConfService.getConfByCustomKeyAndCompanyId(splitOrderConf.getCustomKey(), splitOrderConf.getCompanyId());
        boolean flag;
        if (splitOrderConfDB == null) {
            logger.info("拆单配置添加，customKey = {},companyId = {}", splitOrderConf.getCustomKey(), splitOrderConf.getCompanyId());
            boolean addSuccess = splitOrderConfService.addSplitOrderConf(splitOrderConf);
            flag = addSuccess;
        } else {
            logger.info("拆单配置修改，customKey = {},companyId = {}", splitOrderConf.getCustomKey(), splitOrderConf.getCompanyId());
            boolean updateSuccess = splitOrderConfService.updateSplitOrderConf(splitOrderConf);
            flag = updateSuccess;
        }
        return flag ? returnSuccess(null) : returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
    }
}
