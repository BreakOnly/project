package com.jrmf.controller.policy;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelAreas;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.TPolicy;
import com.jrmf.service.TPolicyService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/policy/manage")
public class PolicyManageController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(PolicyManageController.class);

    @Autowired
    private TPolicyService tPolicyService;
    @Autowired
    private HttpServletRequest request;

    @PostMapping(value = "/policyList")
    public Map<String, Object> policyList(TPolicy tPolicy,
                                          @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                          @RequestParam(required = false, defaultValue = "10") Integer pageSize) {


        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(channelCustom)) {
            return returnFail(RespCode.error101, RespCode.PERMISSIONERROR);
        }

        try {
            Map<String, Object> result = new HashMap<>(5);
            PageHelper.startPage(pageNo, pageSize);
            PageHelper.orderBy("tp.createTime desc");
            List<Map<String, Object>> userList = tPolicyService.selectByExample(tPolicy);
            PageInfo page = new PageInfo(userList);

            result.put("total", page.getTotal());
            result.put("list", page.getList());
            return returnSuccess(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }
    }


    @PostMapping(value = "/updatePolicy")
    public Map<String, Object> list(TPolicy tPolicy) {

        try {

            if (tPolicy.getId() != null) {
                tPolicyService.updateByPrimaryKeySelective(tPolicy);
            } else {
                tPolicyService.insertSelective(tPolicy);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }

        return returnSuccess();
    }

    @PostMapping(value = "/deletePolicy")
    @ResponseBody
    public Map<String, Object> deletePolicy(Integer id) {

        try {
            tPolicyService.deleteByPrimaryKey(id);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }

        return returnSuccess();
    }

    @RequestMapping(value = "/policyTypeList")
    @ResponseBody
    public Map<String, Object> policyTypeList(String typeId) {

        if (StringUtil.isEmpty(typeId)) {
            typeId = "0";
        }

        try {
            List<Map<String, Object>> policyTypeList = tPolicyService.selectPolicyTypeByCode(typeId);
            return returnSuccess(policyTypeList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }

    }

}
