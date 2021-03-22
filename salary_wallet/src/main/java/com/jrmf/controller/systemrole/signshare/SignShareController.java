package com.jrmf.controller.systemrole.signshare;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.SignShareType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.SignElementRule;
import com.jrmf.domain.SignShare;
import com.jrmf.domain.User;
import com.jrmf.service.SignShareService;
import com.jrmf.service.UserSerivce;
import com.jrmf.service.UsersAgreementService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: SignShareController
 * @Description: 共享签约配置接口
 * @create 2020/4/27 9:39
 */
@Slf4j
@RestController
@RequestMapping(value = "/sign/share")
@Api(tags = "共享签约接口，url前缀： /sign/share ")
public class SignShareController extends BaseController {
    private static final Logger LOGGER= LoggerFactory.getLogger(SignShareController.class);
    @Autowired
    private SignShareService signShareService;

    @Autowired
    private UserSerivce userSerivce;
    @Autowired
    private UsersAgreementService usersAgreementService;

    /**
     * 共享签约范围 - 查询
     * @param companyName 商户名称
     * @param limitName 限制组名称
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param status 状态 0：失效 1：有效
     * @param type 共享类型 1：按服务公司共享，2：按商户共享 3：商户间一对多共享
     * @param pageSize
     * @param pageNo
     * @return
     */
    @ApiIgnore
    @RequestMapping(value = "/getSignShare")
    public Map<String, Object> getSignShare(String companyName, String limitName, String startDate, String endDate,
                                            String status, String type, Integer pageSize, Integer pageNo) {
        Map<String, Object> result = new HashMap<>(7);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        Map<String, Object> param = new HashMap<>(9);
        param.put("companyName", companyName);
        param.put("limitName", limitName);
        param.put("startDate", startDate);
        param.put("endDate", endDate);
        param.put("status", status);
        param.put("type", type);

        PageHelper.startPage(pageNo, pageSize);
        List<SignShare> list = signShareService.getSignShareByParam(param);
        PageInfo page = new PageInfo(list);
        result.put("total", page.getTotal());
        result.put("list", page.getList());
        return result;
    }

    /**
     * 共享签约范围 - 新增&修改
     * @param signShare 签约共享bean
     * @param request
     * @return
     */
    @ApiIgnore
    @RequestMapping(value = "/configSignShare")
    public Map<String, Object> configSignShare(SignShare signShare, HttpServletRequest request) throws Exception {
        Map<String, Object> result = null;

        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        if (!CommonString.ROOT.equals(channelCustom.getCustomkey()) && !CommonString.ROOT.equals(channelCustom.getMasterCustom()) ) {
            return returnFail(RespCode.error101, "权限不足");
        }

        if(signShare.getType() == SignShareType.CUSTOMONETOMANY.getCode()) {
            if (StringUtil.isEmpty(signShare.getLimitName())) {
                return returnFail(RespCode.error101, "请输入限制组名称");
            }
        }

        if (!StringUtil.isEmpty(signShare.getLimitName()) && signShare.getType() == SignShareType.CUSTOMONETOMANY.getCode()) {
            SignShare share = signShareService.getSignShareByLimitName(signShare.getLimitName(), signShare.getId());
            if (share != null) {
                return returnFail(RespCode.error101, "限制组名称已存在");
            }
        }

        List<SignShare> share = signShareService.getSignShareByCustomKey2(signShare.getCustomkey(), signShare.getId());
        if(!share.isEmpty() && share.size() > 0) {
            for (SignShare s : share) {
                if (s.getType() == signShare.getType()) {
                    return returnFail(RespCode.error101, "请勿重复配置同一商户");
                }
            }
        }

        try {
            result = signShareService.configSignShare(signShare);
        } catch (Exception e) {
            log.error("配置共享签约失败{}",e);
            return returnFail(RespCode.error101, "配置失败，请联系管理员");
        }
        return result;
    }

    /**
     * 共享签约范围- 删除
     * @param id
     * @return
     */
    @ApiIgnore
    @RequestMapping(value = "/removeSignShare")
    public Map<String, Object> removeSignShare(String id) {
        Map<String, Object> result = null;
        try {
            result = signShareService.deleteSignShare(id);
        } catch (Exception e) {
            log.error("删除共享签约失败{}",e);
            return returnFail(RespCode.error101, "删除失败，请联系管理员");
        }
        return result;
    }


    /**
     * 共享签约限制组 - 查询
     * @param
     * @return
     */
    @ApiIgnore
    @RequestMapping(value = "/getSignShareLimitNameInfo")
    public Map<String, Object> getSignShareLimitNameInfo(String limitGroupId, Integer pageSize, Integer pageNo) {
        Map<String, Object> result = new HashMap<>(7);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        PageHelper.startPage(pageNo, pageSize);
        List<SignShare> list = signShareService.getSignShareLimitByLimitGroupId(limitGroupId);
        PageInfo page = new PageInfo(list);
        result.put("total", page.getTotal());
        result.put("list", page.getList());
        return result;
    }


    /**
     * 共享签约限制组 - 新增
     * @param signShare
     * @return
     */
    @ApiIgnore
    @RequestMapping(value = "/configSignShareLimitNameInfo")
    public Map<String, Object> configSignShareLimitNameInfo(SignShare signShare) {
        Map<String, Object> result = null;
        try {
            result = signShareService.insertSignShareLimitNameInfo(signShare);
        } catch (Exception e) {
            log.error("新增签约限制组失败{}",e);
            return returnFail(RespCode.error101, "新增失败,请联系管理员");
        }
        return result;
    }

    /**
     * 共享签约限制组 - 生效&失效
     * @param id
     * @return
     */
    @ApiIgnore
    @RequestMapping(value = "/updateSignShareLimitStatus")
    public Map<String, Object> updateSignShareLimitStatus(String id, String status) {
        Map<String, Object> result = null;
        try {
            result = signShareService.updateSignShareLimitStatus(id, status);
        } catch (Exception e) {
            log.error("修改签约限制组失败{}",e);
            return returnFail(RespCode.error101, "系统异常，请联系管理员");
        }
        return result;
    }


    /**
     * 共享签约限制组 - 删除
     * @param id
     * @return
     */
    @ApiIgnore
    @RequestMapping(value = "/deleteSignShareLimit")
    public Map<String, Object> deleteSignShareLimit(String id) {
        Map<String, Object> result = null;
        try {
            result = signShareService.deleteSignShareLimit(id);
        } catch (Exception e) {
            log.error("删除共享签约限制组失败{}",e);
            return returnFail(RespCode.error101, "删除失败，请联系管理员");
        }
        return result;
    }


    /**
     * 签约要素规则-查询
     * @param merchantId 平台编号
     * @param companyName 服务公司名称
     * @param startDate 起始时间
     * @param endDate 结束时间
     * @param status 状态 0：失效 1：有效
     * @param signRule 签约规则 0：无限制 1：先签约后支付
     * @param signLevel 签约要素 2：二要素签约 3：三要素签约 4：四要素签约
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiIgnore
    @RequestMapping(value = "/getSignElementRule")
    public Map<String, Object> getSignElementRule(HttpServletRequest request, String merchantId, String companyName, String startDate, String endDate,
                                                  String status, String signRule, String signLevel, Integer pageNo, Integer pageSize) {


        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        if (!CommonString.ROOT.equals(channelCustom.getCustomkey()) && !CommonString.ROOT.equals(channelCustom.getMasterCustom()) ) {
            return returnFail(RespCode.error101, "权限不足");
        }

        Map<String, Object> result = new HashMap<>(7);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        Map<String, Object> param = new HashMap<>(9);
        param.put("companyId", companyName);
        param.put("merchantId", merchantId);
        param.put("startDate", startDate);
        param.put("endDate", endDate);
        param.put("status", status);
        param.put("signRule", signRule);
        param.put("signLevel", signLevel);

        PageHelper.startPage(pageNo, pageSize);
        List<SignElementRule> list = signShareService.getSignElementRule(param);
        PageInfo page = new PageInfo(list);
        result.put("total", page.getTotal());
        result.put("list", page.getList());
        return result;
    }

    /**
     * 签约要素规则-新增&修改
     * @param
     * @return
     */
    @ApiIgnore
    @RequestMapping(value = "/configSignElementRule")
    public Map<String, Object> configSignElementRule(SignElementRule signElementRule) {
        Map<String, Object> result = null;
        try {
            result = signShareService.configSignElementRule(signElementRule);
        } catch (Exception e) {
            log.error("配置签约要素规则失败{}",e);
            return returnFail(RespCode.error101, "配置失败，请联系管理员");
        }
        return result;
    }

    /**
     * 签约要素规则-删除
     * @param id
     * @return
     */
    @ApiIgnore
    @RequestMapping(value = "/deleteSignElementRule")
    public Map<String, Object> deleteSignElementRule(String id) {
        Map<String, Object> result = null;
        try {
            result = signShareService.deleteSignElementRule(id);
        } catch (Exception e) {
            log.error("删除签约要素规则失败{}",e);
            return returnFail(RespCode.error101, "删除失败，请联系管理员");
        }
        return result;
    }


    @PostMapping("/management/platform/users")
    @ApiOperation("查询平台用户信息")
    public @ResponseBody
    Map<String, Object> queryUsers(@ApiParam("姓名") @RequestParam(value = "userName", required = false) String userName,
                              @ApiParam("身份证号") @RequestParam(value = "certId", required = false) String certId,
                              @ApiParam("状态") @RequestParam(value = "userStatus", required = false) String userStatus,
                              @ApiParam("认证等级，多个等级用,分隔") @RequestParam(value = "checkLevel", required = false) String checkLevel,
                              @ApiParam("是否证照认证") @RequestParam(value = "checkByPhoto", required = false) String checkByPhoto,
                              @ApiParam("创建时间起始值") @RequestParam(value = "minCreateTime", required = false) String minCreateTime,
                              @ApiParam("创建时间截止值") @RequestParam(value = "maxCreateTime", required = false) String maxCreateTime,
                              @ApiParam("更新时间起始值") @RequestParam(value = "minLastModifyTime", required = false) String minLastModifyTime,
                              @ApiParam("更新时间截止值") @RequestParam(value = "maxLastModifyTime", required = false) String maxLastModifyTime,
                              @RequestParam(required = false, defaultValue = "1") int pageNo,
                              @RequestParam(required = false, defaultValue = "10") int pageSize) {
        Map<String, Object> paramMap = new HashMap<>(16);
        paramMap.put("userName", userName);
        paramMap.put("certId", certId);
        paramMap.put("userStatus", userStatus);
        paramMap.put("checkLevel", checkLevel);
        paramMap.put("checkByPhoto", checkByPhoto);
        paramMap.put("minCreateTime", StringUtils.isBlank(minCreateTime)?null:minCreateTime.trim()+" 00:00:00");
        paramMap.put("maxCreateTime", StringUtils.isBlank(maxCreateTime)?null:maxCreateTime.trim()+" 23:59:59");
        paramMap.put("minLastModifyTime", StringUtils.isBlank(minLastModifyTime)?null:minLastModifyTime.trim()+" 00:00:00");
        paramMap.put("maxLastModifyTime", StringUtils.isBlank(maxLastModifyTime)?null:maxLastModifyTime.trim()+" 23:59:59");
        PageHelper.startPage(pageNo, pageSize);
        List<User> userList = userSerivce.getUserForPlatform(paramMap);
        if(userList!=null){
            for(User user:userList){
                if(StringUtils.isNotBlank(user.getMobilePhone())){
                    //手机号脱敏
                    user.setMobilePhone(StringUtil.rePhone(user.getMobilePhone()));
                }
            }

        }

        PageInfo<User> pageInfo = new PageInfo(userList);
        Map<String, Object> resultMap = new HashMap<>(4);

        resultMap.put("total", pageInfo.getTotal());
        resultMap.put("userList", pageInfo.getList());
        return returnSuccess(resultMap);
    }


    @ApiOperation("共享签约协议记录查询")
    @PostMapping("/management/platform/agreements")
    public @ResponseBody
    Map<String, Object> queryUserAgreements(@ApiParam("姓名") @RequestParam(required = false) String userName,
                                           @ApiParam("身份证号") @RequestParam(required = false) String certId,
                                           @ApiParam("签约开始时间") @RequestParam(required = false) String signDateStart,
                                           @ApiParam("签约结束时间") @RequestParam(required = false) String signDateEnd,
                                           @ApiParam("服务公司id") @RequestParam(required = false) String companyId,
                                           @ApiParam("认证等级，多个等级用,分隔") @RequestParam(required = false) String checkLevel,
                                           @ApiParam("是否证照认证，多选用,分隔") @RequestParam(required = false) String checkByPhoto,
                                           @ApiParam("签约来源") @RequestParam(required = false) String signSubmitType,
                                           @ApiParam("手机号") @RequestParam(required = false) String mobilePhone,
                                           @ApiParam("迁移状态") @RequestParam(required = false) String signStatus,
                                           @ApiParam("商户名称") @RequestParam(required = false) String customName,
                                           @RequestParam(required = false, defaultValue = "1") int pageNo,
                                           @RequestParam(required = false, defaultValue = "10") int pageSize) {
        Map<String, Object> result = new HashMap<>(4);
        Map<String, Object> params = new HashMap<>(15);

        params.put("customName", customName);
        params.put("companyId", companyId);
        params.put("userName", userName);
        params.put("checkLevel", checkLevel);
        params.put("checkByPhoto", checkByPhoto);
        params.put("signSubmitType", signSubmitType);
        params.put("mobilePhone", mobilePhone);
        params.put("certId", certId);
        params.put("signDateStart", signDateStart);
        params.put("signDateEnd", signDateEnd);
        params.put("signStatus", signStatus);

        PageHelper.startPage(pageNo, pageSize);
        LOGGER.info("共享签约协议记录管理参数 params{}", params);
        List<Map<String, Object>> agreements = usersAgreementService.getAgreementsForPlatform(params);
        PageInfo<User> pageInfo = new PageInfo(agreements);
        result.put("agreements", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        return returnSuccess(result);
    }

    @ApiOperation("获取商户需要共享签约的数量")
    @GetMapping("/management/platform/shareNum")
    public @ResponseBody Map<String, Object> getNeedShareAgreementNum(@RequestParam String originalId){
        int count= signShareService.getNeedShareAgreementNum(originalId);
        Map<String, Object> result = new HashMap<>();
        result.put("count",count);
        return returnSuccess(result);
    }


    @ApiOperation("后台发起共享签约")
    @GetMapping("/management/platform/trigger")
    public @ResponseBody Map<String, Object> triggerShareAgreement(@RequestParam String originalId,@RequestParam int shareScope){
        ThreadUtil.pdfThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                signShareService.triggerShareAgreement(originalId,shareScope);
            }
        });

        return returnSuccess();
    }

    @GetMapping("/management/platform/getMerchantListByUserId")
    @ApiOperation("查询用户所属商户列表")
    public @ResponseBody Map<String, Object> getMerchantListByUserId(@RequestParam int userId) {
        return returnSuccess(signShareService.getMerhcantListByUserId(userId));
    }

    /**
     * 共享签约协议记录 - 发起共享签约 - 通过共享类型查询
     * @param type 共享类型 1：按服务公司共享，2：按商户共享 3：商户间一对多共享
     * @return
     */
    @ApiIgnore
    @RequestMapping(value = "/getSignShareByType")
    public Map<String, Object> getSignShare(@RequestParam(required = false) String type) {
        List<SignShare> list = signShareService.getSignShareByType(type);
        return returnSuccess(list);
    }
}
