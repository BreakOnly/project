package com.jrmf.controller.systemrole.merchant;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.LoginRole;
import com.jrmf.controller.constant.sms.SmsTemplateCodeEnum;
import com.jrmf.domain.*;
import com.jrmf.service.*;
import com.jrmf.utils.CipherUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.SMSSendUtils;
import com.jrmf.utils.StringUtil;
import javax.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("/user")
public class LoginVueController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(LoginVueController.class);

    @Autowired
    private ChannelCustomService customService;

    @Autowired
    private CustomPermissionService customPermissionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OemConfigService oemConfigService;

    @Autowired
    private AccountChangeRelationService accountChangeRelationService;

    final String flag = SMSSendUtils.VALI_MOBILE;

    @Autowired
    private CustomPermissionTemplateService customPermissionTemplateService;

    /**
     * 登陆信息验证
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> login(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(5);
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        String code = request.getParameter("code");
        logger.info(userName + "请求登陆，验证码：" + code);
        String userNameWhiteList = "jt003@jrmf.com,lililiwfh@jrmf.com,ptsh@163.com,shuiguo@163.com,beijing@163.com,ptsh@163.com";
        HttpSession session = request.getSession();
        if (userNameWhiteList.contains(userName.trim())) {
            code = (String) session.getAttribute("code");
            logger.error("李丽莉自动化测试跳过验证码");
        }

        session.setMaxInactiveInterval(10 * 60 * 60);

        String sessionCode = (String) session.getAttribute("code");
        logger.info("sessionCode:" + sessionCode);

        if (sessionCode == null || !sessionCode.equalsIgnoreCase(code)) {
            return retModel(RespCode.error301, result);
        }

        ChannelCustom custom = customService.getUserByUserNameAndOemUrl(userName, request.getServerName());

        String customkey;
        if (custom == null
                || (StringUtil.isEmpty(custom.getCustomkey()) && StringUtil.isEmpty(custom.getMasterCustom()))) {
            respstat = RespCode.error302;
        } else {
            if (1 != custom.getEnabled()) {
                return retModel(RespCode.error303, result);
            }
            if (StringUtil.isEmpty(custom.getCustomkey())) {
                customkey = custom.getMasterCustom();
                int masterCustomType = customService.getCustomByCustomkey(custom.getMasterCustom())
                    .getCustomType();
                custom.setMasterCustomType(masterCustomType);
            } else {
                customkey = custom.getCustomkey();
            }
            if (!custom.getPassword().equals(CipherUtil.generatePassword(password, customkey))) {
                return retModel(RespCode.error304, result);
            }
            session.setAttribute("customLogin", custom);
            result.put("mobilePhone", custom.getPhoneNo());
            session.setAttribute("customkey", customkey);
        }
        return retModel(respstat, result);
    }

    /**
     * 发送手机验证码
     *
     * @return
     */
    @RequestMapping(value = "/sendCode", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> accountDetail(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>(5);
        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute("customLogin");
        final String mobilePhone = channelCustom.getPhoneNo();
        // 这个list里面的手机号直接随便填手机验证码。
        List<String> list = new ArrayList<>();
        list.add("15311371251");
        list.add("13601394905");
        list.add("15010269013");
        logger.info("sendCode方法  传参： mobilePhone=" + mobilePhone);
        if (StringUtil.isEmpty(mobilePhone)) {
            respstat = RespCode.error101;
        } else {
            try {
                /**
                 * 发送验证码
                 */
                Map<String, Object> map = new HashMap<>(4);
                map.put("portalDomain", request.getServerName());
                OemConfig oemConfig = oemConfigService.getOemByParam(map);
                if (oemConfig.getSmsStatus() != 1) {
                    retModel(respstat, model);
                    logger.info("返回结果：" + model);
                    return model;
                }
                if (list.contains(mobilePhone)) {
                    retModel(respstat, model);
                    logger.info("返回结果：" + model);
                    return model;
                }
                String smsSignature = oemConfig.getSmsSignature();
                String code = StringUtil.GetRandomNumberStr6();
                String content = "【" + smsSignature + "】验证码 " + code + "，为了您的帐号安全，请勿泄漏。感谢您使用我司为自由职业从业者提供的云结算服务。";
                String[] mobiletelno = {mobilePhone};

                final String templateParam = "{\"code\":\"" + code + "\"}";
                sendCode(request, flag, code, mobiletelno, content, smsSignature,
                    SmsTemplateCodeEnum.LOGIN.getCode(),
                    templateParam);
                logger.info("本次发送的code：" + code);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return retModelMsg(RespCode.error301, "验证码发送失败!", model);
            }
        }
        retModel(respstat, model);
        logger.info("返回结果：" + model);
        return model;
    }

    /**
     * 验证手机动态码
     *
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/validMobileCode")
    public @ResponseBody
    Map<String, Object> validMobileCode(HttpServletRequest request)
            throws Exception {

        String code = request.getParameter("code");
        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute("customLogin");
        List<String> list = new ArrayList<>();
        list.add("15311371251");
        list.add("13601394905");
        list.add("15010269013");

        final String mobilePhone = channelCustom.getPhoneNo();
        logger.info("validMobileCode 方法： code=" + code + ", mobilePhone=" + mobilePhone);
        Map<String, Object> response = new HashMap<String, Object>();

        int respstat = RespCode.success;
        if (StringUtil.isEmpty(code) || StringUtil.isEmpty(mobilePhone)) {
            respstat = RespCode.error101;
        }

        if (RespCode.success == respstat) {
            try {
                code = code.trim();
                Parameter param;
                Map<String, Object> map = new HashMap<>(4);
                map.put("portalDomain", request.getServerName());
                OemConfig oemConfig = oemConfigService.getOemByParam(map);
                if (oemConfig.getSmsStatus() != 1) {
                    param = new Parameter();
                } else if (list.contains(mobilePhone)) {
                    param = new Parameter();
                } else {
                    param = parameterService.valiMobiletelno(mobilePhone, code, flag);
                }
                if (param == null) {
                    respstat = RespCode.error141;
                }
            } catch (Exception e) {
                respstat = RespCode.error000;
                logger.error(e.getMessage(), e);
            }
        }
        if (channelCustom.getCustomType() == CustomType.ROOT.getCode()) {
            ChannelCustom masterCustom = customService.getCustomByCustomkey(channelCustom.getMasterCustom());
            response.put("companyName", masterCustom.getCompanyName() + "-" + channelCustom.getCompanyName());
            response.put("customType", masterCustom.getCustomType());
        } else {
            response.put("companyName", channelCustom.getCompanyName());
            response.put("customType", channelCustom.getCustomType());
        }
        response.put("isRoot", StringUtil.isEmpty(channelCustom.getMasterCustom()));
        response.put("isReview", channelCustom.getDataReview() == 1);
        response.put("isSetTransPassword", !StringUtil.isEmpty(channelCustom.getTranPassword()));
        boolean flag = (CommonString.ROOT.equals(channelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == channelCustom.getCustomType() && CommonString.ROOT.equals(channelCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == channelCustom.getLoginRole()));
        response.put("isSuperRoot", flag);
        response.put("loginRole", channelCustom.getLoginRole());
        response.put("phoneNo", channelCustom.getPhoneNo());
        logger.info(response.toString());

        request.getSession().setAttribute(CommonString.CUSTOMLOGINACCOUNTID, channelCustom.getId());

        return retModel(respstat, response);
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @RequestMapping("/userLoginOut")
    public @ResponseBody
    Map<String, Object> userLogOut(HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>(5);
        request.getSession().removeAttribute("customLogin");
        request.getSession().removeAttribute("customkey");
        request.getSession().removeAttribute(CommonString.CUSTOMLOGINACCOUNTID);
        return retModel(respstat, model);
    }

    /**
     * 是否设置了交易密码
     *
     * @return
     */
    @RequestMapping("/isSetTransPassword")
    public @ResponseBody
    Map<String, Object> isSetTransPassword(HttpSession session) {
        ChannelCustom custom = (ChannelCustom) session.getAttribute(CommonString.CUSTOMLOGIN);
        if (!StringUtil.isEmpty(custom.getTranPassword())) {
            return returnSuccess("true");
        }
        return returnSuccess("false");
    }


    /**
     * 设置登陆密码 说明:
     *
     * @param request
     * @return:
     */
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> resetPassword(HttpServletRequest request) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute("customLogin");
        String password = request.getParameter("password");
        String oldPassword = request.getParameter("oldPassword");
        String customkey = (String) request.getSession().getAttribute("customkey");

        if (!channelCustom.getPassword().equals(CipherUtil.generatePassword(oldPassword, customkey))) {
            return retModelMsg(RespCode.error101, "旧密码错误!", result);
        }


        password = CipherUtil.generatePassword(password, customkey);
        customService.updatePassword(channelCustom.getId(), password);
        ChannelCustom customByCustomkey = customService.getCustomByCustomkey(customkey);
        request.getSession().removeAttribute("customLogin");
        request.getSession().setAttribute("customLogin", customByCustomkey);
        return retModelMsg(RespCode.success, "设置成功!请重新登陆", result);
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 修改交易密码
     * Date 21:54 2019/1/14
     * Param [session, password, oldPassword]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @RequestMapping(value = "/updateTranPassword", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> resetTranPassword(HttpServletRequest request, HttpSession session, String password, String oldPassword) {
        String customKey = (String) session.getAttribute(CommonString.CUSTOMKEY);
        ChannelCustom custom = (ChannelCustom) session.getAttribute(CommonString.CUSTOMLOGIN);
        if (custom.getCustomType() == 4) {
            return returnFail(RespCode.error101, "仅主体账号可以修改交易密码！");
        }
        if (StringUtil.isEmpty(oldPassword)) {
            return returnFail(RespCode.error101, "旧密码不能为空！");
        }
        String tranPassword = custom.getTranPassword();
        if (StringUtil.isEmpty(tranPassword)) {
            return returnFail(RespCode.error101, "未设置交易密码，不允许修改！");
        }
        if (CipherUtil.generatePassword(oldPassword, customKey).equals(tranPassword)) {
            password = CipherUtil.generatePassword(password, customKey);
            customService.updateCustomTransFerPassword(customKey, password);
        } else {
            return returnFail(RespCode.error101, "旧密码错误");
        }
        ChannelCustom newLoginUser = customService.getCustomById(custom.getId());
        session.removeAttribute(CommonString.CUSTOMLOGIN);
        session.setAttribute(CommonString.CUSTOMLOGIN, newLoginUser);
        HashMap<String, Object> model = new HashMap<>(3);
        return retModelMsg(RespCode.success, "设置成功!", model);
    }

    /**
     * 设置 交易密码 说明:
     *
     * @param request
     * @return:
     */
    @RequestMapping(value = "/setTranPassword", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> setTranPassword(HttpServletRequest request) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        ChannelCustom cc = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (cc.getCustomType() == 4) {
            return returnFail(RespCode.error101, "仅主体账号可以设置交易密码！");
        }
        ChannelCustom custom = customService.getCustomById(cc.getId());
        String customkey = (String) request.getSession().getAttribute("customkey");// 渠道名称
        if (!StringUtil.hasNullStr(custom.getTranPassword())) {
            return retModelMsg(RespCode.error101, "设置失败。交易密码已设置，不可重复设置!", result);
        }
        String password = request.getParameter("password");
        String userName = cc.getUsername();

        password = CipherUtil.generatePassword(password, customkey);
        customService.updateCustomTransFerPassword(customkey, password);
        result.put("userName", userName);
        ChannelCustom customByCustomkey = customService.getCustomById(cc.getId());
        request.getSession().removeAttribute("customLogin");
        request.getSession().setAttribute("customLogin", customByCustomkey);
        return retModelMsg(RespCode.success, "设置成功!请重新登陆！", result);
    }

    /**
     * 重置 交易密码 说明:
     *
     * @param request
     * @return:
     */
    @RequestMapping(value = "/resetTranPassword", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> resetTranPassword(HttpServletRequest request) {
        HashMap<String, Object> result = new HashMap<>();
        ChannelCustom cc = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (cc.getCustomType() == 4) {
            return returnFail(RespCode.error101, "仅主体账号可以重置交易密码！");
        }
        if (StringUtil.isEmpty(cc.getTranPassword())) {
            return returnFail(RespCode.error101, "未设置交易密码不可以重置！");
        }
        String customkey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
        String password = request.getParameter("password");

        password = CipherUtil.generatePassword(password, customkey);
        customService.updateCustomTransFerPassword(customkey, password);
        ChannelCustom customByCustomkey = customService.getCustomById(cc.getId());
        request.getSession().removeAttribute("customLogin");
        request.getSession().setAttribute("customLogin", customByCustomkey);
        return retModelMsg(RespCode.success, "重置成功!请重新登陆！", result);
    }

    /**
     * 获取当前一级权限 说明:
     *
     * @param request
     * @return:
     */
    @RequestMapping(value = "/login/firstLevel")
    public @ResponseBody
    HashMap<String, Object> getCustomPermission(HttpServletRequest request) {
        int respstat = RespCode.success;
        HashMap<String, Object> result = new HashMap<String, Object>();
        List<Map<String, Object>> firstLevelMenuIdsMap = new ArrayList<Map<String, Object>>();
        //获取当前登陆商户信息
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        //判断商户使用的权限类型为原有/模板类型
        Map<String, Object> typeMap = customPermissionService.getTempTypeByCustomId(customLogin.getId());
        Integer type = Integer.parseInt(String.valueOf(typeMap.get("type")));
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("contentLevel", 1);
        paramsMap.put("customId", customLogin.getId());
        if (typeMap != null) {
            if (type == 1) {
                //原有模式
                paramsMap.put("type", "1");
                firstLevelMenuIdsMap = customPermissionService.getCustomMenuIdsByLevel(paramsMap);
            } else {
                //模板模式
                paramsMap.put("type", "2");
                List<String> customTempList = customPermissionService.getCustomPerissionMapping(paramsMap);
                StringBuffer sb = new StringBuffer();
                Set<String> menuIdSet = new HashSet<String>();
                for (String tempId : customTempList) {
                    //获取模板详情
                    CustomPermissionTemplate loginPermissionTemplate = customPermissionTemplateService.getPermissionTempDetail(Integer.parseInt(tempId));
                    if (loginPermissionTemplate.getStatus() == 1) {
                        String menuIds = loginPermissionTemplate.getMenuIds();
                        String[] menuIdArray = menuIds == null ? new String[]{} : menuIds.split(",");
                        for (String menuId : menuIdArray) {
                            menuIdSet.add(menuId);
                        }
                    }
                }
                Iterator<String> itr = menuIdSet.iterator();
                while (itr.hasNext()) {
                    sb.append(itr.next()).append(",");
                }
                String menuIds = sb.length() < 1 ? "" : sb.substring(0, sb.length() - 1);
                paramsMap.put("menuIds", menuIds);
                firstLevelMenuIdsMap = customPermissionService.getCustomMenuIdsTempByLevel(paramsMap);
            }
        }
        //id,pId,name
        result.put("list", firstLevelMenuIdsMap);
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        return result;
    }

    /**
     * 获取当前一级权限 说明:
     *
     * @param request
     * @param id
     * @return:
     */
    @RequestMapping(value = "/login/otherLevel")
    public @ResponseBody
    HashMap<String, Object> otherLevel(HttpServletRequest request, String id) {
        int respstat = RespCode.success;
        HashMap<String, Object> result = new HashMap<>(5);
        // 渠道名称
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (customLogin == null) {
            respstat = RespCode.error306;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
            return result;
        }
        List<Map<String, Object>> firstLevelMenuIdsMap = new ArrayList<Map<String, Object>>();
        //判断商户使用的权限类型为原有/模板类型
        Map<String, Object> typeMap = customPermissionService.getTempTypeByCustomId(customLogin.getId());
        Integer type = 2;
        if (typeMap == null) {
            type = 2;
        } else {
            type = Integer.parseInt(String.valueOf(typeMap.get("type")));
        }
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("contentLevel", 2);
        paramsMap.put("customId", customLogin.getId());
        paramsMap.put("parentId", id);
        if (type == 1) {
            //原有模式
            paramsMap.put("type", "1");
            firstLevelMenuIdsMap = customPermissionService.getCustomMenuIdsByLevel(paramsMap);
            for (Map<String, Object> menuSecondLevel : firstLevelMenuIdsMap) {
                paramsMap.put("parentId", menuSecondLevel.get("id"));
                paramsMap.put("contentLevel", 3);
                List<Map<String, Object>> childThreadLevelList = customPermissionService.getCustomMenuIdsByLevel(paramsMap);
                if (childThreadLevelList != null && childThreadLevelList.size() > 0) {
                    menuSecondLevel.put("list", childThreadLevelList);
                }
            }
        } else {
            //模板模式
            paramsMap.put("type", "2");
            List<String> customTempList = customPermissionService.getCustomPerissionMapping(paramsMap);
            StringBuffer sb = new StringBuffer();
            Set<String> menuIdSet = new HashSet<String>();
            for (String tempId : customTempList) {
                //获取模板详情
                CustomPermissionTemplate loginPermissionTemplate = customPermissionTemplateService.getPermissionTempDetail(Integer.parseInt(tempId));
                if (loginPermissionTemplate.getStatus() == 1) {
                    String menuIds = loginPermissionTemplate.getMenuIds();
                    String[] menuIdArray = menuIds == null ? new String[]{} : menuIds.split(",");
                    for (String menuId : menuIdArray) {
                        menuIdSet.add(menuId);
                    }
                }
            }
            Iterator<String> itr = menuIdSet.iterator();
            while (itr.hasNext()) {
                sb.append(itr.next()).append(",");
            }
            String menuIds = sb.length() < 1 ? "" : sb.substring(0, sb.length() - 1);
            paramsMap.put("menuIds", menuIds);
            firstLevelMenuIdsMap = customPermissionService.getCustomMenuIdsTempByLevel(paramsMap);
            for (Map<String, Object> menuSecondLevel : firstLevelMenuIdsMap) {
                paramsMap.put("contentLevel", 3);
                paramsMap.put("parentId", menuSecondLevel.get("id"));
                List<Map<String, Object>> childThreadLevelList = customPermissionService.getCustomMenuIdsTempByLevel(paramsMap);
                if (childThreadLevelList != null && childThreadLevelList.size() > 0) {
                    menuSecondLevel.put("list", childThreadLevelList);
                }
            }
        }
        result.put("list", firstLevelMenuIdsMap);
        result.put(RespCode.RESP_STAT, respstat);
//        result.put("sessionId", request.getSession().getId());
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        return result;
    }

    /**
     * 获取当前一级权限 说明:
     *
     * @param request
     * @return:
     *//*
     * @RequestMapping(value = "/test") public @ResponseBody Model
     * test(Model model, HttpServletRequest request, String id) {
     * List<BankCard> list = bankCardBinService.getbankcardAll(); // for
     * (BankCard bankCard : list) { // String bankCardNo =
     * bankCard.getStart(); // bankCardNo = bankCardNo+"888888888888"; //
     * String bankName = bankCardBinService.getBankName(bankCardNo); //
     * if(StringUtil.isEmpty(bankCardBinService.getBankName(bankCardNo)) //
     * || bankCard.getBankName().equals(bankName)){ //
     * model.addAttribute(bankCard.getBankName(),bankCard.getId()); // }; //
     * } return model; }
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 管理员重置商户交易密码
     *
     * @author linsong
     * @date 2019/4/3
     */
    @PostMapping(value = "/resetTranPassword2")
    @ResponseBody
    public Map<String, Object> resetTranPassword2(HttpServletRequest request, @RequestParam("customkey") String customkey) {
        //        HashMap<String, Object> result = new HashMap<>();
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(customLogin) && !isPlatformAccount(customLogin)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }

        //传递的customkey为父账号customkey,判断父账号交易密码不为空即代表所有子账号交易密码都不为空
        ChannelCustom custom = customService.getCustomByCustomkey(customkey);
        if (custom != null && StringUtil.isEmpty(custom.getTranPassword())) {
            return returnFail(RespCode.error101, "未设置交易密码不可以重置！");
        }

        String tranPassword = CipherUtil.generatePassword("123456", customkey);
        customService.updateCustomTransFerPassword(custom.getCustomkey(), tranPassword);

        return returnSuccess();
    }

    /**
     * 商户列表重置登陆密码
     * @param request
     * @param customkey
     * @return
     */
    @RequestMapping(value = "/resetPassword2")
    @ResponseBody
    public Map<String, Object> resetPassword2(HttpServletRequest request, @RequestParam("customkey") String customkey) {

        logger.info("customkey:" + customkey);
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(customLogin) && !isPlatformAccount(customLogin)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }

        ChannelCustom custom = customService.getCustomByCustomkey(customkey);
        if (custom == null) {
            return returnFail(RespCode.error101, "商户不存在，请刷新页面重试!");
        }
        if (StringUtil.isEmpty(custom.getPassword())) {
            return returnFail(RespCode.error101, "未设置登陆密码不可以重置！");
        }

        String password = CipherUtil.generatePassword("123456", customkey);
        customService.updateCustomPassword(custom.getId(), password);

        return returnSuccess();
    }


    /**
     * 切换账号列表
     *
     * @author linsong
     * @date 2019/8/2
     */
    @RequestMapping(value = "/changeAccountList")
    @ResponseBody
    public Map<String, Object> changeAccountList(HttpServletRequest request, @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                                 @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        ChannelCustom loginCustom = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        Integer accountId = (Integer) request.getSession().getAttribute(CommonString.CUSTOMLOGINACCOUNTID);

        if (accountId == null) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        Map<String, Object> result = new HashMap<>(5);

        List<Map<String, Object>> list = new ArrayList<>();

        boolean canChange = false;
        if (accountId != loginCustom.getId()) {
            list = accountChangeRelationService.changeAccountList(accountId, loginCustom.getId());
            //如果当前登录用户在根账户切换组中
            if (list != null && list.size() > 0) {
                canChange = true;
            }
        } else {
            canChange = true;
        }

        if (canChange) {
            PageHelper.startPage(pageNo, pageSize);
            list = accountChangeRelationService.changeAccountList(accountId, null);
        }

        PageInfo page = new PageInfo(list);
        result.put("total", page.getTotal());
        result.put("list", page.getList());

        return returnSuccess(result);
    }


    /**
     * 切换账号
     *
     * @author linsong
     * @date 2019/8/2
     */
    @RequestMapping(value = "/changeAccount")
    @ResponseBody
    public Map<String, Object> changeAccount(HttpServletRequest request, Integer changeAccountId) {

        if (changeAccountId == null) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        ChannelCustom changeCustom = customService.getCustomById(changeAccountId);

        if (changeCustom == null) {
            return returnFail(RespCode.error101, "账号不存在");
        }

        HttpSession session = request.getSession();
        Integer accountId = (Integer) session.getAttribute(CommonString.CUSTOMLOGINACCOUNTID);

        if (accountId == null) {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        List<Map<String, Object>> list = accountChangeRelationService.changeAccountList(accountId, changeAccountId);

        Map<String, Object> response = new HashMap<>();


        if (list != null && list.size() > 0) {

            String customKey = changeCustom.getCustomkey();
            String customName = changeCustom.getCompanyName();
            int customType = changeCustom.getCustomType();
            if (changeCustom.getCustomType() == CustomType.ROOT.getCode()) {
                ChannelCustom masterCustom = customService.getCustomByCustomkey(changeCustom.getMasterCustom());
                customType = masterCustom.getCustomType();
                customKey = masterCustom.getCustomkey();
                customName = masterCustom.getCompanyName() + "-" + changeCustom.getCompanyName();
                changeCustom.setMasterCustomType(customType);
            }

            session.setAttribute(CommonString.CUSTOMLOGIN, changeCustom);
            session.setAttribute(CommonString.CUSTOMKEY, customKey);

            response.put("customType", customType);
            response.put("companyName", customName);
            response.put("isRoot", StringUtil.isEmpty(changeCustom.getMasterCustom()));
            response.put("isReview", changeCustom.getDataReview() == 1);
            response.put("isSetTransPassword", !StringUtil.isEmpty(changeCustom.getTranPassword()));
            boolean flag = (CommonString.ROOT.equals(changeCustom.getCustomkey()) || (CustomType.ROOT.getCode() == changeCustom.getCustomType() && CommonString.ROOT.equals(changeCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == changeCustom.getLoginRole()));
            response.put("isSuperRoot", flag);
            response.put("loginRole", changeCustom.getLoginRole());
            response.put("username", changeCustom.getUsername());
            response.put("phoneNo", changeCustom.getPhoneNo());
            logger.info(response.toString());

        } else {
            return returnFail(RespCode.error101, RespCode.PARAMS_ERROR);
        }

        return returnSuccess(response);
    }

    /**
     * 忘记密码->验证
     * @param request
     * @param userName
     * @param password
     * @param againPassword
     * @param code
     * @param phoneNo
     * @return
     */
    @RequestMapping(value = "/forgetPassword")
    @ResponseBody
    public Map<String, Object> forgetPassword(HttpServletRequest request,
                                              @RequestParam(value = "userName") String userName,
                                              @RequestParam(value = "password") String password,
                                              @RequestParam(value = "againPassword") String againPassword,
                                              @RequestParam(value = "code") String code,
                                              @RequestParam(value = "phoneNo") String phoneNo) {

        Map<String, Object> result = new HashMap<>();
        try {
            result = customService.forgetPassword(request, userName, password, againPassword, code, phoneNo);
        } catch (Exception e) {
            logger.error("忘记密码验证异常:", e);
            result.put(RespCode.RESP_STAT, RespCode.error107);
            result.put(RespCode.RESP_MSG, "重置密码失败，请联系管理员！");
            return result;
        }
        return result;
    }

    /**
     * 发送手机验证码
     *
     * @return
     */
    @RequestMapping(value = "/sendCode2", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> accountDetail2(String phoneNo, HttpServletRequest request) {
        int respstat = RespCode.success;
        Map<String, Object> model = new HashMap<>(5);
        logger.info("sendCode2方法  传参： phoneNo=" + phoneNo);
        if (StringUtil.isEmpty(phoneNo)) {
            respstat = RespCode.error101;
        } else {
            try {
                /**
                 * 发送验证码
                 */
                Map<String, Object> map = new HashMap<>(4);
                map.put("portalDomain", request.getServerName());
                OemConfig oemConfig = oemConfigService.getOemByParam(map);
                if (oemConfig.getSmsStatus() != 1) {
                    retModel(respstat, model);
                    logger.info("返回结果：" + model);
                    return model;
                }
                String smsSignature = oemConfig.getSmsSignature();
                String code = StringUtil.GetRandomNumberStr6();
                String content = "【" + smsSignature + "】验证码 " + code + "，为了您的帐号安全，请勿泄漏。感谢您使用我司为自由职业从业者提供的云结算服务。";
                String[] mobiletelno = {phoneNo};
                final String templateParam = "{\"code\":\"" + code + "\"}";
                sendCode(request, flag, code, mobiletelno, content, smsSignature,
                    SmsTemplateCodeEnum.LOGIN.getCode(),
                    templateParam);
                logger.info("本次发送的code：" + code);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return retModelMsg(RespCode.error301, "验证码发送失败!", model);
            }
        }
        retModel(respstat, model);
        logger.info("返回结果：" + model);
        return model;
    }


    /**
     * 忘记密码->修改密码
     * @param request
     * @return
     */
    @RequestMapping(value = "/validMobileCodeAndForgetPassword")
    @ResponseBody
    public Map<String, Object> validMobileCodeAndForgetPassword(HttpServletRequest request, @RequestParam(value = "code") String code) {

        Map<String, Object> result = new HashMap<>();
        try {
            result = customService.validMobileCodeAndForgetPassword(request, code);
        } catch (Exception e) {
            logger.error("忘记密码修改异常:", e);
            result.put(RespCode.RESP_STAT, RespCode.error107);
            result.put(RespCode.RESP_MSG, "重置密码失败，请联系管理员！");
            return result;
        }
        return result;
    }

    /**
     * 切换账号配置管理-> 查询
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryAccount")
    @ResponseBody
    public Map<String, Object> queryAccount(HttpServletRequest request, @RequestParam(value = "companyName", required = false) String companyName,
                                            @RequestParam(value = "companyType", required = false) String companyType,
                                            @RequestParam(value = "userName", required = false) String userName,
                                            @RequestParam(value = "configCompanyName", required = false) String configCompanyName,
                                            @RequestParam(value = "configCompanyType", required = false) String configCompanyType,
                                            @RequestParam(value = "configUserName", required = false) String configUserName,
                                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession()
            .getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }

        Map<String, Object> result = new HashMap<>();
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        Map<String, Object> param = new HashMap<>();
        param.put("companyName", companyName);
        param.put("companyType", companyType);
        param.put("userName", userName);
        param.put("configCompanyName", configCompanyName);
        param.put("configCompanyType", configCompanyType);
        param.put("configUserName", configUserName);
        try {
            PageHelper.startPage(pageNo, pageSize);
            List<Map<String, Object>> list = customService.queryAccount(param);
            PageInfo page = new PageInfo(list);
            result.put("total", page.getTotal());
            result.put("list", page.getList());
        } catch (Exception e) {
            logger.error("切换账号查询异常:", e);
            result.put(RespCode.RESP_STAT, RespCode.error107);
            result.put(RespCode.RESP_MSG, "网络错误！");
            return result;
        }
        return result;
    }

    /**
     * 切换账号配置管理-> 新增/修改
     * @param request
     * @return
     */
    @RequestMapping(value = "/configAccount")
    @ResponseBody
    public Map<String, Object> configAccount(HttpServletRequest request, @RequestParam(value = "customId", required = false) String customId,
                                             @RequestParam(value = "configCustomId", required = false) String[] configCustomId,
                                             @RequestParam(value = "id", required = false) String id) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(loginUser) && !isPlatformAccount(loginUser)) {
            return returnFail(RespCode.error101, RespCode.PERMISSION_ERROR);
        }

        Map<String, Object> result = accountChangeRelationService.configAccount(loginUser, customId, configCustomId, id);
        return result;
    }


    /**
     * 切换账号配置管理-> 删除
     * @param request
     * @return
     */
    @RequestMapping(value = "/deleteAccount")
    @ResponseBody
    public Map<String, Object> deleteAccount(HttpServletRequest request, @RequestParam(value = "id") String id) {

        logger.info("切换账号配置管理->删除id：", id);
        Optional.ofNullable(id)
                .orElseThrow(() -> new NullPointerException("网络错误"));

        try {
            AccountChangeRelation accountChangeRelation = accountChangeRelationService.getAccountChangeRelationById(id);
            Optional.ofNullable(accountChangeRelation)
                    .orElseThrow(() -> new NullPointerException("该信息不存在，请刷新页面后重试！"));
            accountChangeRelationService.deleteAccountChangeRelationById(id);
        } catch (Exception e) {
            logger.error("切换账号删除异常：", e);
            return returnFail(RespCode.error101, e.getMessage());
        }
        return returnSuccess();
    }

    /**
     * 切换账号配置管理-> 根据商户角色类型查询商户
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryCustomByType")
    @ResponseBody
    public Map<String, Object> queryCustomByType(HttpServletRequest request, @RequestParam(value = "companyType") String companyType) {
        Map<String, Object> result = new HashMap<>();
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        try {
            List<Map<String,Object>> list = customService.getCustomByCustomType(companyType);
            result.put("list", list);
        } catch (Exception e) {
            logger.error("根据商户角色类型查询商户异常：", e);
            result.put(RespCode.RESP_STAT, RespCode.error107);
            result.put(RespCode.RESP_MSG, "查询商户失败，请联系管理员！");
            return result;
        }
        return result;
    }


    @RequestMapping(value = "/queryCurrentLoginStatus")
    @ResponseBody
    public Map<String, Object> queryCurrentLoginStatus(HttpServletRequest request) {

        logger.info("request cookies:{}", request.getHeader("Cookie"));

        ChannelCustom loginUser = (ChannelCustom) request.getSession()
            .getAttribute(CommonString.CUSTOMLOGIN);

        if (loginUser != null) {
            Map<String, Object> response = new HashMap<String, Object>();
            if (CustomType.ROOT.getCode() == loginUser.getCustomType()) {
                ChannelCustom masterCustom = customService
                    .getCustomByCustomkey(loginUser.getMasterCustom());
                response.put("companyName",
                    masterCustom.getCompanyName() + "-" + loginUser.getCompanyName());
            } else {
                response.put("companyName", loginUser.getCompanyName());
            }
            response.put("isRoot", StringUtil.isEmpty(loginUser.getMasterCustom()));
            response.put("isReview", loginUser.getDataReview() == 1);
            response.put("isSetTransPassword", !StringUtil.isEmpty(loginUser.getTranPassword()));
            boolean flag = (CommonString.ROOT.equals(loginUser.getCustomkey()) || (
                CustomType.ROOT.getCode() == loginUser.getCustomType() && CommonString.ROOT
                    .equals(loginUser.getMasterCustom())
                    && LoginRole.ADMIN_ACCOUNT.getCode() == loginUser.getLoginRole()));
            response.put("isSuperRoot", flag);
            response.put("loginRole", loginUser.getLoginRole());
            response.put("phoneNo", loginUser.getPhoneNo());
            response.put("userName",loginUser.getUsername());
            response.put("customKey", !StringUtil.isEmpty(loginUser.getMasterCustom())?loginUser.getMasterCustom():loginUser.getCustomkey());
            response.put("customType",loginUser.getCustomType());
            return returnSuccess(response);
        }

        return returnFail(RespCode.error306, "用户未登录");

    }

}
