package com.jrmf.controller.common;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.jrmf.common.CommonString;
import com.jrmf.common.ResponseCodeMapping;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CompanyType;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.Company;
import com.jrmf.domain.OrganizationNode;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.CompanyService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/11/15 9:52
 * Version:1.0
 *
 * @author guoto
 */
@Api(value = "公共查询接口", tags = {"公共查询接口"})
@Controller
@RequestMapping("/common/query")
public class CommonController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(CommonController.class);
    @Autowired
    private CompanyService companyService;
    @Autowired
    private ChannelRelatedService channelRelatedService;
    @Autowired
    private ChannelCustomService channelCustomService;
    @Autowired
    private OrganizationTreeService organizationTreeService;
    @Autowired
    private CustomProxyDao customProxyDao;

    /**
     * Author Nicholas-Ning
     * Description //TODO 服务公司列表查询
     * Date 14:46 2018/12/7
     * Param [session]
     * return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @RequestMapping(value = "/listcompany")
    @ResponseBody
    public Map<String, Object> listCompany(HttpServletRequest request, HttpSession session) {
        Map<String, Object> result;
        try {
            result = new HashMap<>(10);
            String customKey = (String) session.getAttribute(CommonString.CUSTOMKEY);
            //当前登陆账号的主体账户
            ChannelCustom loginCustom = channelCustomService.getCustomByCustomkey(customKey);
            int customType = loginCustom.getCustomType();

            List<Company> companyList = new ArrayList<>();
            //判断是否是魔方科技访问，魔方科技获取所有服务公司信息
            if (CommonString.ROOT.equals(customKey)) {
                companyList = companyService.getCompanyList(null);
            } else {
                //判断是商户(包括管理员)还是服务公司进行访问，商户获取对应服务公司，服务公司获取自己
                if (CustomType.CUSTOM.getCode() == (customType)
                        || CustomType.PROXY.getCode() == (customType)
                        || CustomType.GROUP.getCode() == (customType)) {
                    //商户
                    List<ChannelRelated> relatedList = channelRelatedService.getRelatedList(customKey);
                    for (ChannelRelated related : relatedList) {
                        Company company = companyService.getCompanyByUserId(Integer.parseInt(related.getCompanyId()));
                        companyList.add(company);
                    }
                } else if (CustomType.COMPANY.getCode() == (customType)) {
                    //服务公司
                    Company company = companyService.getCompanyByUserId(Integer.parseInt(customKey));
                    companyList.add(company);
                }
            }
            result.put("companyList", companyList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.codeMaps.get(RespCode.error107));
        }
        return returnSuccess(result);
    }

    @RequestMapping(value = "/new/listcompany")
    @ResponseBody
    public Map<String, Object> listCompanyNew(HttpServletRequest request, HttpSession session) {
        Map<String, Object> result;
        try {
            result = new HashMap<>(10);
            String customKey = (String) session.getAttribute(CommonString.CUSTOMKEY);
            ChannelCustom loginCustom = channelCustomService.getCustomByCustomkey(customKey);
            int customTypeLogin = loginCustom.getCustomType();

            List<Company> companyList = new ArrayList<>();
            if (CustomType.COMPANY.getCode() == (customTypeLogin)) {//服务公司
                Company company = companyService.getCompanyByUserId(Integer.parseInt(loginCustom.getCustomkey()));
                companyList.add(company);
            } else {//判断是商户(包括管理员)商户获取对应服务公司

                String customKeyStr = "";
                String queryCurrent = request.getParameter("queryCurrent");
                if ("1".equals(queryCurrent)) {
                    customKeyStr = request.getParameter("customkey");
                } else {

                    int nodeIdInt = 0;
                    int customTypeInt = 0;
                    String nodeId = request.getParameter("nodeId");
                    String customType = request.getParameter("customType");
                    if (!StringUtil.isEmpty(customType)) {
                        customTypeInt = Integer.parseInt(customType);
                    }
                    if (!StringUtil.isEmpty(nodeId)) {
                        nodeIdInt = Integer.parseInt(nodeId);
                    }

                    //当前点击节点的customKey
                    String currentCustomkey = request.getParameter("customKey");
                    ChannelCustom custom = channelCustomService.getCustomByCustomkey(currentCustomkey);
                    //判断当前的节点是不是关联性代理商
                    if (custom != null && custom.getCustomType() == CustomType.PROXY.getCode() && custom.getProxyType() == 1) {
                        logger.info("当前点击商户{}是关联性代理商", custom.getCompanyName());
                        customTypeInt = CustomType.PROXYCHILDEN.getCode();
                        //关联性代理的关联关系在custom_proxy_childen,防止传递过来的nodeId是其他关联关系表的
                        OrganizationNode node = customProxyDao.getProxyChildenNodeByCustomKey(currentCustomkey,null);
                        nodeIdInt = node.getId();
                    } else if (custom != null) {
                        customTypeInt = custom.getCustomType();
                    }

                    List<String> customKeyList = organizationTreeService.queryNodeCusotmKey(customTypeInt, "G", nodeIdInt);
                    for (String ckey : customKeyList) {
                        customKeyStr = customKeyStr + "," + ckey;
                    }
                    if (customKeyStr.lastIndexOf(",") >= 0) {
                        customKeyStr = customKeyStr.substring(1);
                    }
                }

                List<ChannelRelated> relatedList = channelRelatedService.getRelatedList(customKeyStr);
                Set<String> companyIdSet = new HashSet<String>();

                for (ChannelRelated related : relatedList) {
                    companyIdSet.add(related.getCompanyId());
                }
                for (String companyId : companyIdSet) {
                    Company company = companyService.getCompanyByUserId(Integer.parseInt(companyId));
                    companyList.add(company);
                }
            }
            result.put("companyList", companyList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.codeMaps.get(RespCode.error107));
        }
        return returnSuccess(result);
    }

    /**
     * @Author YJY
     * @Description 根据 key 获取其关联的公司
     * @Date  2020/11/23
     * @Param [request, session]
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    @ApiOperation("服务公司下拉框")
    @PostMapping(value = "/company/list")
    @ResponseBody
    public Map<String, Object> getCompanyListByKey(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>(1);
        List<ChannelCustom> returnList = new ArrayList<>();
        String companyId = request.getParameter("companyId");
        Integer loginCompanyId = returnSubcontractCompanyId(request);
        if (!isRootAdmin(request) && ObjectUtils.isEmpty(loginCompanyId)) {
            return returnFail(ResponseCodeMapping.ERR_529.getCode(),ResponseCodeMapping.ERR_529.getMessage());
        }
        /**
        * @Description 如果不是超管 转包服务公司直接返回自己的数据
        **/
        if (!isRootAdmin(request) && StringUtils.isBlank(companyId)) {
            ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
            if (CustomType.ROOT.getCode() == customLogin.getCustomType() && !StringUtil
                .isEmpty(customLogin.getMasterCustom())) {
                ChannelCustom masterCustom = customService
                    .getCustomByCustomkey(customLogin.getMasterCustom());
                returnList.add(masterCustom);
            }else{
                returnList.add(customLogin);
            }

            result.put("data",returnList);
            return returnSuccess(result);
        }
        /**
        * @Description 超管查询所有
        **/
        if(StringUtils.isBlank(companyId)){
            List<Company> companyList =  companyService.getAllCompanyList(1);
            if(!CollectionUtils.isEmpty(companyList)) {
                for (Company data : companyList) {

                    ChannelCustom channelCustom = new ChannelCustom();
                    channelCustom.setCompanyName(data.getCompanyName());
                    channelCustom.setCustomkey(data.getUserId()+"");
                    returnList.add(channelCustom);
                }
            }
            result.put("data",returnList);
            return returnSuccess(result);
        }

      if(StringUtils.isNotBlank(companyId)) {
            try {
               List<ChannelRelated> list = channelRelatedService.getRelatedList(companyId);
               if(!CollectionUtils.isEmpty(list)){
                   for(ChannelRelated data:list){

                       ChannelCustom channelCustom = new ChannelCustom();
                       channelCustom.setCompanyName(data.getCompanyName());
                       channelCustom.setCustomkey(data.getCompanyId());
                       returnList.add(channelCustom);
                   }
               }
               result.put("data",returnList);
              return returnSuccess(result);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return returnFail(RespCode.error107, RespCode.codeMaps.get(RespCode.error107));
            }
       }
        return returnSuccess(result);
    }



    /**
     * 查询当前登录的超管及代理商服务公司
     *
     * @param
     * @throws
     * @author linsong
     * @date 2019/4/28
     */
    @RequestMapping(value = "/companyList")
    @ResponseBody
    public Map<String, Object> companyList(HttpServletRequest request) {
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");

        Map<String, Object> result = new HashMap<>(10);

        List<Company> companyList = new ArrayList<>();

        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            ChannelCustom masterCustom = channelCustomService.getCustomByCustomkey(loginUser.getMasterCustom());
            if (CustomType.PROXY.getCode() == masterCustom.getCustomType() || CommonString.ROOT.equals(masterCustom.getCustomkey())) {
                loginUser = masterCustom;
            }
        }

        if (CommonString.ROOT.equals(loginUser.getCustomkey())) {
            companyList = companyService.getCompanyList(null);
        } else if (CustomType.PROXY.getCode() == loginUser.getCustomType()) {
            //判断是不是关联性代理商
            if (loginUser.getProxyType() == 1) {
                String customkeys = loginUser.getCustomkey();
                List<String> customKeyList = customProxyDao.queryProxyChildenCustomKeyCurrentAndChildren(customkeys);
                customkeys = StringUtils.join(customKeyList, ",");
                companyList = companyService.getCompanyListByProxy(customkeys);
            } else {
                companyList = companyService.getCompanyListByProxy(loginUser.getCustomkey());
            }
        }

        result.put("companyList", companyList);
        return returnSuccess(result);
    }

    /**
     * 查询指定服务公司下所有商户
     * @param companyId
     * @return
     */
    @RequestMapping(value = "/customList2")
    @ResponseBody
    public Map<String, Object> customList2 (String companyId) {
        if (StringUtil.isEmpty(companyId)) {
            return returnFail(RespCode.error101, "参数异常");
        }
        List<String> stringList = channelRelatedService.queryCustomKeysByCompanyId(companyId);
        String customKeys = Joiner.on(",").join(stringList);
        List<Map<String, Object>> customList = channelCustomService.queryCustom2(customKeys);
        return returnSuccess(customList);
    }

    /**
     * 根据邮箱查询商户
     */
    @RequestMapping(value = "/getCustomByEmail")
    @ResponseBody
    public Map<String, Object> getCustomByEmail(HttpServletRequest request,String email) {
        Map<String,Object> resultMap = new HashMap<>();
        ChannelCustom channelCustom = channelCustomService.getChannelCustomByEmail(email);
        resultMap.put("existCustom",channelCustom != null);
        resultMap.put(RespCode.RESP_STAT, RespCode.success);
        resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        return resultMap;
    }

    /**
     * 通过customKey查询商户信息 统一社会信用码 地址信息
     */
    @RequestMapping(value = "/getCustomByCustomKey")
    @ResponseBody
    public Map<String, Object> getCustomByCustomKey(HttpServletRequest request,String customKey) {
        Map<String,Object> resultMap = new HashMap<>();
        ChannelCustom channelCustom = channelCustomService.getChannelCustomByKey(customKey);
        resultMap.put("data",channelCustom);
        resultMap.put(RespCode.RESP_STAT, RespCode.success);
        resultMap.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        return resultMap;
    }
    /**
     * 查询当前登录账户下属的商户
     */
    @RequestMapping(value = "/customList")
    @ResponseBody
    public Map<String, Object> customList(HttpServletRequest request, String type) {
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        List<Map<String, Object>> customList = null;

        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            ChannelCustom masterCustom = channelCustomService.getCustomByCustomkey(loginUser.getMasterCustom());
            loginUser = masterCustom;
        }

        if (CommonString.ROOT.equals(loginUser.getCustomkey())) {
            customList = channelCustomService.queryCustom(null);
        } else if (CustomType.CUSTOM.getCode() == loginUser.getCustomType()) {
            customList = channelCustomService.queryCustom(loginUser.getCustomkey());
        } else if (CustomType.GROUP.getCode() == loginUser.getCustomType()) {
            int nodeId = organizationTreeService.queryNodeIdByCustomKey(loginUser.getCustomkey());
            List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
            String customKeys = Joiner.on(",").join(stringList);

            customList = channelCustomService.queryCustom(customKeys);
        } else if (CustomType.COMPANY.getCode() == loginUser.getCustomType()) {
            List<String> stringList = channelRelatedService.queryCustomKeysByCompanyId(loginUser.getCustomkey());
            String customKeys = Joiner.on(",").join(stringList);
            // 商户开篇信息管理，商户邮寄地址管理 会传type类型，查询所有的商户，原先的商户类型写死，不会带出转包服务公司
            if (!StringUtil.isEmpty(type)) {
                customList = channelCustomService.queryAllCustom(customKeys);
            } else {
                customList = channelCustomService.queryCustom(customKeys);
            }
        } else if (CustomType.PROXY.getCode() == loginUser.getCustomType()) {
            //判断是不是关联性代理商
            if (loginUser.getProxyType() == 1) {
                OrganizationNode node = customProxyDao.getProxyChildenNodeByCustomKey(loginUser.getCustomkey(),null);
                List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());

                customList = new ArrayList<>();

                if (stringList != null && stringList.size() > 0) {
                    List<String> customStringList = new ArrayList<>();
                    for (String customKey : stringList) {
                        OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey,null);
                        List<String> itemCustomStringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
                        if (itemCustomStringList != null) {
                            customStringList.addAll(itemCustomStringList);
                        }
                    }
                    String customKeys = Joiner.on(",").join(customStringList);
                    customList.addAll(channelCustomService.queryCustom(customKeys));
                }
            } else {
                OrganizationNode node = customProxyDao.getNodeByCustomKey(loginUser.getCustomkey(),null);
                List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
                String customKeys = Joiner.on(",").join(stringList);
                customList = channelCustomService.queryCustom(customKeys);
            }
        } else if (CustomType.PLATFORM.getCode() == loginUser.getCustomType()){
            //平台登陆商户
            Integer businessPlatformId = loginUser.getId();
            Map<String,Object> params = new HashMap<>();
            params.put("businessPlatformId",businessPlatformId);
            List<ChannelCustom> channelCustomList = channelCustomService.getCustomByParam(params);
            if (channelCustomList != null && channelCustomList.size() > 0){
                List<String> customKeys = new ArrayList<>();
                for (ChannelCustom channelCustom : channelCustomList) {
                    if (!StringUtil.isEmpty(channelCustom.getCustomkey())){
                        customKeys.add(channelCustom.getCustomkey());
                    }
                }
                customList = channelCustomService.queryCustom(Joiner.on(",").join(customKeys));
            }
        }
        return returnSuccess(customList);
    }


    /**
     * 查询当前登录的用户的下发公司
     *
     * @param
     * @throws
     */
    @RequestMapping(value = "/new/companyList")
    @ResponseBody
    public Map<String, Object> companyListByCustomKey(HttpServletRequest request, String customKey,
        Integer companyType) {

        Map<String, Object> result = new HashMap<>(10);

        List<Company> companyList = new ArrayList<>();

        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

       Integer platformId =  checkCustom(channelCustom);
       if(!ObjectUtils.isEmpty(platformId)){
           companyList =  companyService.selectCompanyByPlatform(platformId);
           result.put("companyList", companyList);
           return returnSuccess(result);
       }

        if (CustomType.ROOT.getCode() == channelCustom.getCustomType() && !StringUtil.isEmpty(channelCustom.getMasterCustom())) {
            ChannelCustom masterCustom = channelCustomService.getCustomByCustomkey(channelCustom.getMasterCustom());
            channelCustom = masterCustom;
        }

        if (CustomType.COMPANY.getCode() != channelCustom.getCustomType() && !StringUtil.isEmpty(customKey)) {
            channelCustom = channelCustomService.getCustomByCustomkey(customKey);
        }

        if (CommonString.ROOT.equals(channelCustom.getCustomkey())) {
            companyList = companyService.getAllCompanyList(companyType);
        } else if (CustomType.CUSTOM.getCode() == channelCustom.getCustomType()) {
            List<ChannelRelated> relatedList = channelRelatedService.getRelatedList(channelCustom.getCustomkey());
            for (ChannelRelated related : relatedList) {
                Company company = companyService.getCompanyByUserId(Integer.parseInt(related.getCompanyId()));
                companyList.add(company);
            }
        } else if (CustomType.GROUP.getCode() == channelCustom.getCustomType()) {
            int id = organizationTreeService.queryNodeIdByCustomKey(channelCustom.getCustomkey());
            List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, id);

            if (stringList != null && stringList.size() > 0) {
                String originalIds = Joiner.on(",").join(stringList);

                List<ChannelRelated> relatedList = channelRelatedService.getRelatedList(originalIds);

                if (relatedList!=null && relatedList.size()>0){
                    StringBuilder ids = new StringBuilder();
                    for (ChannelRelated related : relatedList) {
                        ids.append(related.getCompanyId()).append(",");
                    }

                    if (',' == ids.charAt(ids.length() - 1)) {
                        ids = ids.deleteCharAt(ids.length() - 1);
                    }

                    companyList = companyService.getCompanyByUserIds(ids.toString());
                }

            }
        } else if (CustomType.COMPANY.getCode() == channelCustom.getCustomType()) {

            Company company = companyService.getCompanyByUserId(Integer.parseInt(channelCustom.getCustomkey()));
            companyList.add(company);
        } else if (CustomType.PROXY.getCode() == channelCustom.getCustomType()) {
            //判断是不是关联性代理商
            if (channelCustom.getProxyType() == 1) {
                String customkeys = channelCustom.getCustomkey();
                List<String> customKeyList = customProxyDao.queryProxyChildenCustomKeyCurrentAndChildren(customkeys);
                customkeys = StringUtils.join(customKeyList, ",");
                companyList = companyService.getCompanyListByProxy(customkeys);
            } else {
                companyList = companyService.getCompanyListByProxy(channelCustom.getCustomkey());
            }
        }

        result.put("companyList", companyList);
        return returnSuccess(result);
    }

    /**
     * 查询当前登录的用户的具有个体户下发能力的下发公司
     *
     * @param
     * @throws
     */
    @RequestMapping(value = "/individual/companyList")
    @ResponseBody
    public Map<String, Object> individualCompanyList(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>(10);
        ChannelCustom channelCustom = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        String userId = null;
        if (isCompany(channelCustom)) {
            userId = channelCustom.getCustomkey();
        }
        List<Company> companyList = companyService.getIndividualCompanys(userId);
        result.put("companyList", companyList);
        return returnSuccess(result);
    }


    /**
     * 查询当前登录账户下属的商户
     */
    @RequestMapping(value = "/loginComstomType")
    @ResponseBody
    public Map<String, Object> loginComstomType(HttpServletRequest request) {
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        Map<String, Object> user = new HashMap<>();
        user.put("customKey", loginUser.getCustomkey());
        user.put("customType", loginUser.getCustomType());

        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            ChannelCustom masterCustom = channelCustomService.getCustomByCustomkey(loginUser.getMasterCustom());
            user.put("customKey", masterCustom.getCustomkey());
            user.put("customType", masterCustom.getCustomType());
        }

        return returnSuccess(user);
    }


    /**
     * 超管根据条件检索所有商户、下发公司、代理商
     */
    @RequestMapping(value = "/allList")
    @ResponseBody
    public Map<String, Object> allList(HttpServletRequest request, String content, Integer customType, Integer loginRole,
    								   Integer fundModelType,String customkey,
                                       @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                       @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        List<Map<String, Object>> customList;

        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            ChannelCustom masterCustom = channelCustomService.getCustomByCustomkey(loginUser.getMasterCustom());
            loginUser = masterCustom;
        }

        if (!CommonString.ROOT.equals(loginUser.getCustomkey())) {
            return returnFail(RespCode.error101, RespCode.PERMISSIONERROR);
        }

        PageHelper.startPage(pageNo, pageSize);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("content", content);
        params.put("customType", customType);
        params.put("loginRole", loginRole);
        params.put("customkey", customkey);
        if (fundModelType != null) {
            if(fundModelType==0){
                fundModelType=1;
            }else if(fundModelType==1){
                fundModelType=2;
            }
        }
        params.put("fundModelType", fundModelType);
        customList = channelCustomService.getAllList(params);

        Map<String, Object> result = new HashMap<>(5);
        PageInfo page = new PageInfo(customList);
        result.put("total", page.getTotal());
        result.put("list", page.getList());

        return returnSuccess(result);
    }

    /**
     * 查询当前服务公司信息
     * @return
     */
    @RequestMapping(value = "/subcontractorCompany")
    @ResponseBody
    public Map<String, Object> subcontractorCompany(HttpServletRequest request, String companyId) {
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            loginUser= channelCustomService.getCustomByCustomkey(loginUser.getMasterCustom());
        }

        if (!StringUtil.isEmpty(companyId)) {
            loginUser= channelCustomService.getCustomByCustomkey(companyId);
        }

        if (loginUser.getCustomType() == CustomType.COMPANY.getCode()) {
            return returnSuccess(loginUser);
        }
        return returnSuccess();
    }
}
