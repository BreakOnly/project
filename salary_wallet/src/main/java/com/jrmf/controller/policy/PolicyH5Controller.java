package com.jrmf.controller.policy;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelAreas;
import com.jrmf.domain.TPolicyGroup;
import com.jrmf.service.TPolicyService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.weixin.WeiXinUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest/policy")
public class PolicyH5Controller extends BaseController {

    private Logger logger = LoggerFactory.getLogger(PolicyH5Controller.class);

    @Autowired
    private TPolicyService tPolicyService;

    @PostMapping(value = "/policyList")
    public Map<String, Object> policyList(Integer type,
                                          String keyword,
                                          @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                          @RequestParam(required = false, defaultValue = "2000") Integer pageSize) {

        try {
            Map<String, Object> result = new HashMap<>(5);
            PageHelper.startPage(pageNo, pageSize);
            PageHelper.orderBy("contentOrder asc");
            List<Map<String, Object>> userList = tPolicyService.selectH5ListByType(type, keyword);
            PageInfo page = new PageInfo(userList);

            result.put("total", page.getTotal());
            result.put("list", page.getList());
            return returnSuccess(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }
    }

    @PostMapping(value = "/epidemicPolicyList")
    public Map<String, Object> epidemicPolicyList(String keyword, String areaCode,
                                                  @RequestParam(required = false, defaultValue = "2") Integer parentId) {

        try {
            List<TPolicyGroup> tPolicyGroupList = tPolicyService.selectH5ListByArea(keyword, areaCode, parentId);
//            List<String> policyTypeList = tPolicyService.selectPolicyTypeStr(parentId);
//
//
//            List<String> groupTypeList = new ArrayList<>();
//            tPolicyGroupList.forEach(item -> groupTypeList.add(item.getTypeName()));
//
//            policyTypeList.forEach(typeName -> {
//                if (!groupTypeList.contains(typeName)) {
//                    tPolicyGroupList.add(new TPolicyGroup(typeName));
//                }
//            });

            return returnSuccess(tPolicyGroupList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }
    }

    @RequestMapping(value = "/areaList")
    @ResponseBody
    public Map<String, Object> areaList() {

        try {
            List<ChannelAreas> parentList = tPolicyService.selectAreaByRootCode("1");
            return returnSuccess(parentList);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }
    }

    @RequestMapping(value = "/visitsPolicy")
    @ResponseBody
    public Map<String, Object> visitsPolicy(Integer policyId) {

        try {
            tPolicyService.updateVisitsCount(policyId);
            return returnSuccess();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }
    }

    @RequestMapping("/getWXConfig")
    @ResponseBody
    public Object WeixShare(String requestUrl, String backTitle, String backDesc, String backImgUrl) {

        try {
            Object share_obj = WeiXinUtil.getAppWxConfig(requestUrl, backTitle, backDesc, backImgUrl);

            return returnSuccess(share_obj);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }
    }
}
