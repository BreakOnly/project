package com.jrmf.controller.systemrole.notice;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.CustomOrganization;
import com.jrmf.domain.Notice;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.NoticeService;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: noticeController
 * @Description: 通知公告
 * @create 2020/1/15 16:08
 */
@RestController
@RequestMapping("/notice")
public class NoticeController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(NoticeController.class);

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private ChannelCustomService customService;

    private static String id = "";

    /**
     * 通知公告查询
     *
     * @param title
     * @param noticeType
     * @param organizationType
     * @param startTime
     * @param endTime
     * @param pageSize
     * @param pageNo
     * @return
     */
    @RequestMapping(value = "/queryNotice")
    public Map<String, Object> queryNotice(@RequestParam(value = "title", required = false) String title,
                                           @RequestParam(value = "noticeType", required = false) String noticeType,
                                           @RequestParam(value = "organizationType", required = false) String organizationType,
                                           @RequestParam(value = "startTime", required = false) String startTime,
                                           @RequestParam(value = "endTime", required = false) String endTime,
                                           @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                           @RequestParam(value = "pageNo", required = false) Integer pageNo) {
//      initialCapacity = (需要存储的元素个数 / 负载因子) + 1
        Map<String, Object> result = new HashMap<>(7);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        Map<String, Object> paramMap = new HashMap<>(8);
        paramMap.put("title", title);
        paramMap.put("noticeType", noticeType);
        paramMap.put("endTime", endTime);
        paramMap.put("startTime", startTime);
        // 通过发布范围id 查询商户
        id = "";
        Optional.of(organizationType)
                .ifPresent(type -> {
                    noticeService.queryCustomOrganizationId(type)
                            .stream()
                            .filter(c -> c != null)
                            .forEach(c -> {
                                // 1: 系统组织
                                if (CustomType.CUSTOM.getCode() == c.getType()) {
                                    if (c.getCustomType() == null) {
                                        id = id + c.getId() + ",|,";
                                    } else {
                                        id = id + c.getCustomType() + ",|,";
                                    }
                                } else {
                                    id = id + c.getId() + ",|,";
                                }
                            });
                    if (!StringUtil.isEmpty(id)) {
                        paramMap.put("organizationType", "," + id.substring(0, id.length() - 2));
                    }
                });

        PageHelper.startPage(pageNo, pageSize);
        List<Notice> list = noticeService.getNoticeList(paramMap);
        PageInfo page = new PageInfo(list);
        result.put("total", page.getTotal());
        result.put("list", page.getList());
        return result;
    }

    /**
     * 查询通知范围
     *
     * @return
     */
    @RequestMapping(value = "/queryOrganizationType")
    public Map<String, Object> queryOrganizationType() {
        Map<String, Object> result = new HashMap<>(5);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        List<Map<String, Object>> list = new ArrayList<>();

        // 自定义组织
        Map<String, Object> customMap = new HashMap<>(4);
        customMap.put("label", "自定义组织");
        List<CustomOrganization> customOrganizationList = noticeService.selectAllCustomOrganization();
        logger.info("customOrganizationList{}", customOrganizationList);
        customMap.put("options", customOrganizationList
                .stream()
                .filter(c -> c.getType() == 2)
                .collect(Collectors.toList()));
        list.add(customMap);

        // 系统组织
        Map<String, Object> systemMap = new HashMap<>(4);
        systemMap.put("label", "系统组织");
        systemMap.put("options", customOrganizationList
                .stream()
                .filter(c -> c.getType() == 1)
                .collect(Collectors.toList()));
        list.add(systemMap);

        result.put("list", list);
        return result;
    }

    /**
     * 发布公告
     *
     * @param notice
     * @param accessoryNameFile
     * @return
     */
    @RequestMapping(value = "/insertNotice")
    public Map<String, Object> insertNotice(Notice notice,
                                            @RequestParam(value = "accessoryNameFile", required = false) MultipartFile accessoryNameFile,
                                            HttpServletRequest request) {
        Map<String, Object> result;
        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        if (CustomType.ROOT.getCode() == loginUser.getCustomType() && !StringUtil.isEmpty(loginUser.getMasterCustom())) {
            loginUser = customService.getCustomByCustomkey(loginUser.getMasterCustom());
        }

        if (!CommonString.ROOT.equals(loginUser.getCustomkey())) {
            return returnFail(RespCode.error106, "权限不足！");
        }

        try {
            notice.setAddUser(loginUser.getUsername());
            result = noticeService.insertNotice(notice, accessoryNameFile);
        } catch (Exception e) {
            logger.error("发布公告失败：", e);
            return returnFail(RespCode.INSERT_FAIL, "发布公告失败，请联系管理员！");
        }
        return result;
    }

    /**
     * 新增自定义组织
     *
     * @param customOrganization
     * @return
     */
    @RequestMapping(value = "/insertCustomOrganization")
    public Map<String, Object> insertCustomOrganization(CustomOrganization customOrganization) {
        Map<String, Object> result;
        try {
            result = noticeService.insertCustomOrganization(customOrganization);
        } catch (Exception e) {
            logger.error("自定义组织失败：", e);
            return returnFail(RespCode.INSERT_FAIL, "自定义组织失败，请联系管理员！");
        }
        return result;
    }

    /**
     * 附件导出
     *
     * @throws IOException
     **/
    @RequestMapping(value = "/export")
    public ResponseEntity<byte[]> downloadWhiteListTmp(HttpServletRequest request) throws IOException {

        String id = request.getParameter("id");
        Notice notice = noticeService.getNoticeById(id);
        String filePath = "/noticeAccessory/" + notice.getAccessoryUrl().substring(notice.getAccessoryUrl().lastIndexOf("/") + 1);
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        byte[] bytes = FtpTool.downloadFtpFile(filePath.substring(0, filePath.lastIndexOf("/")), fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        // 防止中文乱码
        fileName = new String(notice.getAccessoryName().getBytes("gbk"), "iso8859-1");
        headers.add("Content-Disposition", "attachment;filename=" + fileName);
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<byte[]>(bytes,
                headers, HttpStatus.OK);
    }


    /**
     * 商户查询公告信息
     *
     * @return
     */
    @RequestMapping(value = "/customQueryNotice")
    public Map<String, Object> customQueryNotice(HttpServletRequest request, @RequestParam(value = "type", required = false) String type) {
        Map<String, Object> result = new HashMap<>(10);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        Map<String, Object> paramMap = new HashMap<>();
        try {
            ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
            Integer id = loginUser.getId();
            if (id == null) {
                result.put(RespCode.RESP_STAT, RespCode.error105);
                result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error105));
                return result;
            }
            paramMap.put("id", id);
            List<Map<String, Object>> list;
            // 轮播展示所有未读消息
            if ("1".equals(type)) {
                list = noticeService.getCustomNoticeByCustomNoticeId(paramMap);
            } else {
                // 展示前三条未读消息
                paramMap.put("start", 0);
                paramMap.put("limit", 3);
                list = noticeService.getCustomNoticeByCustomNoticeId(paramMap);
            }
            result.put("list", list);
        } catch (Exception e) {
            logger.error("商户查询公告信息失败：", e);
            return returnFail(RespCode.INSERT_FAIL, "商户查询公告信息失败，请联系管理员！");
        }

        return result;
    }

    /**
     * 清空 & 一键已读
     *
     * @return
     */
    @RequestMapping(value = "/clearCustomNotice")
    public Map<String, Object> clearCustomNotice(HttpServletRequest request) {

        Map<String, Object> param = new HashMap<>();

        try {
            ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
            Integer id = loginUser.getId();
            if (id == null) {
                return returnFail(RespCode.INSERT_FAIL, "用户不存在");
            }
            param.put("accountId", id);
            noticeService.updateCustomNoticeReadIsByIds(param);
        } catch (Exception e) {
            logger.error("清空失败：", e);
            return returnFail(RespCode.INSERT_FAIL, "一键阅读失败，请联系管理员！");
        }
        return returnSuccess();
    }

    /**
     * 查询全部公告 & 查看公告详情
     *
     * @return
     */
    @RequestMapping(value = "/customQueryAllNotice")
    public Map<String, Object> customQueryAllNotice(HttpServletRequest request,
                                                    @RequestParam(value = "id", required = false) String id,
                                                    @RequestParam(value = "startTime", required = false) String startTime,
                                                    @RequestParam(value = "endTime", required = false) String endTime,
                                                    @RequestParam(value = "noticeType", required = false) String noticeType,
                                                    @RequestParam(value = "pageSize", required = false, defaultValue = "10") String pageSize,
                                                    @RequestParam(value = "pageNo", required = false) String pageNo) {
        Map<String, Object> result = new HashMap<>(10);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        List<Map<String, Object>> list;
        int total = 0;
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("startTime", startTime);
            param.put("endTime", endTime);
            if ("0".equals(noticeType)) {
                noticeType = null;
            }
            param.put("noticeType", noticeType);
            ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
            Integer accountId = loginUser.getId();
            if (accountId == null) {
                return returnFail(RespCode.INSERT_FAIL, "参数异常");
            }
            if (!StringUtil.isEmpty(id)) {
                list = noticeService.getCustomANoticeByNoticeId(id);
                noticeService.updateCustomNoticeReadIsById(accountId, id);
            } else {
                param.put("accountId", accountId);
                total = noticeService.getCustomAllNoticeByCustomNoticeIdCount(param);
                if (!StringUtil.isEmpty(pageNo)) {
                    param.put("start", getFirst(pageNo, pageSize));
                    param.put("limit", Integer.parseInt(pageSize));
                }
                list = noticeService.getCustomAllNoticeByCustomNoticeId(param);
            }
            result.put("list", list);
            result.put("total", total);
        } catch (Exception e) {
            logger.error("查询全部公告失败：", e);
            return returnFail(RespCode.INSERT_FAIL, "查询全部公告失败，请联系管理员！");
        }
        return result;
    }


    /**
     * 删除 & 失效接口
     *
     * @param type
     * @param id
     * @return
     */
    @RequestMapping("/deleteNoticeEnabled")
    public Map<String, Object> deleteNoticeEnabled(@RequestParam(value = "type", required = false) String type,
                                                   @RequestParam(value = "id", required = false) String id) {
        Map<String, Object> result = new HashMap<>(10);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        // 失效
        try {
            if (!StringUtil.isEmpty(type)) {
                if ("1".equals(type)) {
                    noticeService.updateNoticeEnabled(id);
                } else {
                    noticeService.deleteNoticeEnabled(id);
                }
            } else {
                return returnFail(RespCode.DELETE_FAIL, "参数异常");
            }
        } catch (Exception e) {
            logger.error("操作失败", e);
            return returnFail(RespCode.DELETE_FAIL, "操作失败，请联系管理员！");
        }
        return result;
    }

    /**
     * 查询组织结构
     * @return
     */
    @RequestMapping(value = "/organizationStructure")
    public Map<String, Object> organizationStructure() {
        Map<String, Object> result = new HashMap<>(5);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        List<Map<String, Object>> list = new ArrayList<>();

        // 自定义组织信息
        JSONObject customJsonObject = new JSONObject();
        customJsonObject.put("organizationName", "自定义组织");
        customJsonObject.put("id", "custom");
        customJsonObject.put("children", noticeService.queryCustomOrganizationIdByTypeIs2());
        list.add(customJsonObject);

        // 系统组织信息
        List<CustomOrganization> systemList = noticeService.queryCustomOrganizationIdByTypeIs1();
        JSONObject systemObject = new JSONObject();
        systemObject.put("organizationName", "系统组织");
        systemObject.put("id", 0);

        List<Map<String, Object>> systemChildrenList = new ArrayList<>();
        JSONObject childrenJsonObject = new JSONObject();
        JSONObject systemChildrenJsonObject = new JSONObject();

        for (CustomOrganization c : systemList) {
            if (c.getCustomType() == 7) {
                JSONObject childrenObject = new JSONObject();
                childrenJsonObject.put("id", c.getId());
                childrenJsonObject.put("organizationName", c.getOrganizationName());
                // 服务公司customkey 有可能和系统组织表重复，在前面加A 来区分
                List<Map<String, Object>> channelCustoms = noticeService.selectAllCompany();
                childrenJsonObject.put("children", channelCustoms);
            } else {
                childrenJsonObject.put("id", c.getId());
                childrenJsonObject.put("organizationName", c.getOrganizationName());
            }
            List<Map<String, Object>> mapId = noticeService.getOrganizationById(c.getId());
            List<Map<String, Object>> childrenList = new ArrayList<>();
            if (mapId != null && mapId.size() > 0) {
                for (Map<String, Object> m : mapId) {
                    systemChildrenJsonObject.put("id", m.get("id"));
                    systemChildrenJsonObject.put("organizationName", m.get("organizationName"));
                    childrenList.add(systemChildrenJsonObject);
                    systemChildrenJsonObject = new JSONObject();
                }
                childrenJsonObject.put("children", childrenList);
            }
            systemChildrenList.add(childrenJsonObject);
            childrenJsonObject = new JSONObject();
        }
        systemObject.put("children", systemChildrenList);
        list.add(systemObject);

        result.put("list", list);
        return result;
    }

    /**
     * 删除自定义组织
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteCustomOrganization")
    public Map<String, Object> deleteCustomOrganization(@RequestParam(value = "id") String id) {

        Map<String, Object> result = new HashMap<>();
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        try {
            noticeService.deleteCustomOrganization(id);
        } catch (Exception e) {
            logger.error("删除自定义组织失败{}", e);
            return returnFail(RespCode.error101, "删除自定义组织失败,请联系管理员！");
        }
        return result;
    }

    /**
     * 根据自定义ID查询
     *
     * @param ids
     * @param content
     * @param customType
     * @param loginRole
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/queryOrganizationInfo")
    public Map<String, Object> queryOrganizationInfo(@RequestParam(value = "ids", required = false) String ids,
                                                     @RequestParam(value = "content", required = false) String content,
                                                     @RequestParam(value = "customType", required = false) Integer customType,
                                                     @RequestParam(value = "loginRole", required = false) Integer loginRole,
                                                     @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                                     @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        Set<String> accountId = new HashSet<>();
        if (!StringUtil.isEmpty(ids)) {
            List<String> accountIds = noticeService.getCustomOrganization(ids);
            // 去重
            accountId.addAll(accountIds);
            paramMap.put("content", content);
            paramMap.put("customType", customType);
            paramMap.put("loginRole", loginRole);
            paramMap.put("ids", String.join("", accountId));
            PageHelper.startPage(pageNo, pageSize);
            List<Map<String, Object>> list = customService.getCustomByIds(paramMap);
            PageInfo page = new PageInfo(list);
            result.put("total", page.getTotal());
            result.put("list", page.getList());
        } else {
            return returnFail(RespCode.error101, "请选择自定义组织！");
        }
        return result;
    }
}
