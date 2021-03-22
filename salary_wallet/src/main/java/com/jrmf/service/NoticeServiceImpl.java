package com.jrmf.service;

import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.CustomOrganization;
import com.jrmf.domain.Notice;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.persistence.NoticeDao;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Title: NoticeServiceImpl
 * @Description:
 * @create 2020/1/15 16:26
 */
@Service("NoticeService")
public class NoticeServiceImpl implements NoticeService {

    private static final Logger logger = LoggerFactory.getLogger(NoticeServiceImpl.class);

    @Autowired
    private NoticeDao noticeDao;

    @Autowired
    private ChannelCustomDao customDao;

    @Autowired
    private BestSignConfig bestSignConfig;

    @Value("${ftppath}")
    private String domainName;

    /**
     * 查询总条数
     *
     * @param paramMap
     * @return
     */
    @Override
    public int getNoticeCount(Map<String, Object> paramMap) {
        return noticeDao.getNoticeCount(paramMap);
    }

    /**
     * 查询公告信息 及 统计阅读数 & 查看率
     *
     * @param paramMap
     * @return
     */
    @Override
    public List<Notice> getNoticeList(Map<String, Object> paramMap) {
        return noticeDao.getNoticeList(paramMap);
    }

    @Override
    public List<CustomOrganization> queryCustomOrganizationId(String id) {
        return noticeDao.queryCustomOrganizationId(id);
    }

    /**
     * 发布公告
     *
     * @param notice
     * @param accessoryNameFile
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> insertNotice(Notice notice, MultipartFile accessoryNameFile) {
        Map<String, Object> result = new HashMap<String, Object>(4);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        String fileName = UUID.randomUUID().toString().replace("-", "");
        // 上传附件
        if (accessoryNameFile != null) {
            try {
                logger.info("-----上传附件开始-----");
                String accessoryName = accessoryNameFile.getOriginalFilename();
                String accessoryNameType = accessoryName.substring(accessoryName.lastIndexOf(".") + 1);
                notice.setAccessoryName(accessoryName);
                notice.setAccessoryUrl(domainName + "/noticeAccessory/" + fileName + "." + accessoryNameType);
                byte[] bytes = accessoryNameFile.getBytes();
                InputStream fileInputStream = new ByteArrayInputStream(bytes);
                FtpTool.uploadFile(bestSignConfig.getFtpURL(), 21, "/noticeAccessory/", fileName + "." + accessoryNameType, fileInputStream, bestSignConfig.getUsername(), bestSignConfig.getPassword());
            } catch (IOException e) {
                logger.error("上传附件失败{}", e);
                result.put(RespCode.RESP_STAT, RespCode.error101);
                result.put(RespCode.RESP_MSG, "上传附件失败，请联系管理员！");
                return result;
            }
            logger.info("-----上传附件完成-----");
        }

        // 开始发布公告
        Set<String> noticeAccountId = new HashSet<>();
        String customType = "";
        String organizationCustomType = "";
        String organizationType = "";
        String organizationId = "";
        boolean flag = false;
        try {
            Optional.ofNullable(notice.getOrganizationId())
                    .orElseThrow(() -> new Exception("参数异常"));
            logger.info("-----发布公告开始-----");
            String[] organizationIdsplit = notice.getOrganizationId().split(",");
            // 过滤非数字的id
            List<String> organizationIdList = this.filterLetter(notice.getOrganizationId(), 1);

            List<CustomOrganization> customOrganization = new ArrayList<>();
            if (!organizationIdList.isEmpty() && organizationIdList.size() > 0) {
                customOrganization = noticeDao.queryCustomOrganizationId(organizationIdList.stream().collect(Collectors.joining(",")));
            }

            // 过滤数字的id
            List<String> companyIdList = this.filterLetter(notice.getOrganizationId(), 0);
            if (!companyIdList.isEmpty() && companyIdList.size() > 0) {
                CustomOrganization c = new CustomOrganization();
                c.setType(1);
                c.setCustomType(7);
                customOrganization.add(c);
                c = new CustomOrganization();
            }
            notice.setOrganizationId("");
            List<CustomOrganization> systemType = noticeDao.queryCustomOrganizationIdByTypeIs1();
            // id为0表示全部选择
            if (Stream.of(organizationIdsplit).collect(Collectors.toList()).contains("0")) {
                flag = true;
            }
            if (flag) {
                logger.info("type------{}", systemType.stream()
                        .filter(s -> s.getCustomType() != 7)
                        .map(s -> s.getCustomType() + "")
                        .collect(Collectors.joining(",")));
                String type = systemType.stream()
                        .filter(s -> s.getCustomType() != 7)
                        .map(s -> s.getCustomType() + "")
                        .collect(Collectors.joining(","));
                notice.setOrganizationType("," + type);
            } else {
                Optional.ofNullable(customOrganization)
                        .filter(x -> x.size() > 0)
                        .orElseThrow(() -> new Exception("发布商户不存在，请刷新页面重试"));

                for (CustomOrganization c : customOrganization) {
                    if (c.getType() == 1) {
                        if (c.getCustomType() != null && c.getCustomType() != 7) {
                            organizationType = organizationType + c.getCustomType() + ",";
                        } else if (c.getCustomType() != null && c.getCustomType() == 7) {
                            customType = "7,";
                        } else {
                            organizationId = organizationId + c.getId() + ",";
                        }
                    } else {
                        organizationCustomType = "6,";
                        organizationId = organizationId + c.getId() + ",";
                    }
                }

                if (!StringUtil.isEmpty(customType)) {
                    organizationType = organizationType + customType;
                }

                if (!StringUtil.isEmpty(organizationCustomType)) {
                    organizationType = organizationType + organizationCustomType;
                }

                if (!StringUtil.isEmpty(organizationType)) {
                    notice.setOrganizationType("," + organizationType);
                }

                if (!StringUtil.isEmpty(organizationId)) {
                    notice.setOrganizationId("," + organizationId);
                }
            }

            logger.info("-------新增到公告表-------");
            noticeDao.insertNotice(notice);

            // 开始新增到公告中间表
            if (flag) {
                // 包含0表示：发布公告到所有商户
                List<String> channelCustomId = customDao.getChannelCustomId();
                noticeDao.insertCustomNotice(channelCustomId, notice.getId());
            } else {
                for (CustomOrganization c : customOrganization) {
                    // 类型1为:系统组织
                    if (c.getType() == 1) {
                        if (c.getCustomType() == null) {
                            if (Stream.of(organizationIdsplit).collect(Collectors.toList()).contains("1")) {
                                break;
                            } else if (Stream.of(organizationIdsplit).collect(Collectors.toList()).contains("3")) {
                                break;
                            } else {
                                List<CustomOrganization> noticeList = noticeDao.getCustomTypeByParentId(c.getParentId());
                                noticeList.forEach(n -> {
                                    if (n.getCustomType() == 1) {
                                        // 商户
                                        List<String> ids = customDao.getCustomByCustomTypeAndLoginRole(n.getCustomType(), c.getLoginRole());
                                        noticeAccountId.addAll(ids);
                                    } else if (n.getCustomType() == 3) {
                                        if (c.getLoginRole() == 1) {
                                            // 普通代理商
                                            List<String> ids = customDao.getAgentCustomByCustomTypeAndLoginRole();
                                            noticeAccountId.addAll(ids);
                                        } else if (c.getLoginRole() == 2) {
                                            // OEM代理商
                                            List<String> ids = customDao.getOemAgentCustomByCustomTypeAndLoginRole();
                                            noticeAccountId.addAll(ids);
                                        }
                                    }
                                });
                            }
                        } else if (c.getCustomType() != 7) {
                            List<String> customId = customDao.getChannelCustomByCustomType(c.getCustomType());
                            noticeAccountId.addAll(customId);
                        } else {
                            String companyId = "";
                            for (String l : companyIdList) {
                                String[] a = l.split("A");
                                companyId = companyId + a[1] + ",";
                            }
                            String companyIds = companyId.substring(0, companyId.length() - 1);
                            logger.info("---------服务公司companyId---------{}", companyIds);
                            // 服务公司对应商户
                            List<String> customIds = customDao.getCustomByCompanyId(companyIds);
                            List<String> ids = customDao.getIdByCompanyId(companyIds);
                            noticeAccountId.addAll(customIds);
                            noticeAccountId.addAll(ids);
                        }
                    } else {
                        noticeAccountId.addAll(Arrays.stream(c.getAccountIds().split(",")).collect(Collectors.toList()));
                    }
                }
                // 新增到公告中间表 结束
                List<String> list = new ArrayList<>(noticeAccountId);
                logger.info("-----发布公告结束，接收公告的商户有-----：{}", list);
                noticeDao.insertCustomNotice(list, notice.getId());
            }
        } catch (Exception e) {
            logger.error("发布公告失败{}", e);
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return result;
        }
        return result;
    }

    /**
     * 过滤非数字
     *
     * @param organizationId
     * @param type
     * @return
     */
    public List<String> filterLetter(String organizationId, int type) {
        String[] organizationIdsplit = organizationId.split(",");
        List<String> organizationIdList = new ArrayList<>(Arrays.asList(organizationIdsplit));
        Iterator<String> iterator = organizationIdList.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (type == 1) {
                if (!StringUtil.isNumber(next)) {
                    iterator.remove();
                }
            } else {
                if (StringUtil.isNumber(next)) {
                    iterator.remove();
                }
            }
        }
        return organizationIdList;
    }

    /**
     * 新增自定义组织
     *
     * @param customOrganization
     * @return
     */
    @Override
    public Map<String, Object> insertCustomOrganization(CustomOrganization customOrganization) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
        try {
            if (!StringUtil.isEmpty(customOrganization.getOrganizationName())) {
                int count = noticeDao.getOrganizationNamByOrganizationNam(customOrganization.getOrganizationName());
                if (count > 0) {
                    result.put(RespCode.RESP_STAT, RespCode.error101);
                    result.put(RespCode.RESP_MSG, "名称已存在！");
                    return result;
                }
            }

            noticeDao.insertCustomOrganization(customOrganization);
        } catch (Exception e) {
            logger.error("自定义组织失败{}", e);
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, "自定义组织失败，请联系管理员！");
            return result;
        }
        return result;
    }

    @Override
    public Notice getNoticeById(String id) {
        return noticeDao.getNoticeById(id);
    }

    /**
     * 根据商户主键查询 商户公告中间表
     *
     * @param paramMap
     * @return
     */
    @Override
    public List<Map<String, Object>> getCustomNoticeByCustomNoticeId(Map<String, Object> paramMap) {
        return noticeDao.getCustomNoticeByCustomNoticeId(paramMap);
    }

    /**
     * 根据主键id修改为已读
     *
     * @param param
     */
    @Override
    public void updateCustomNoticeReadIsByIds(Map<String, Object> param) {
        noticeDao.updateCustomNoticeReadIsByIds(param);
    }

    /**
     * 查询该商户下所有公告
     *
     * @param param
     * @return
     */
    @Override
    public List<Map<String, Object>> getCustomAllNoticeByCustomNoticeId(Map<String, Object> param) {
        return noticeDao.getCustomAllNoticeByCustomNoticeId(param);
    }

    /**
     * 统计查询该商户下所有公告数量
     *
     * @param param
     * @return
     */
    @Override
    public int getCustomAllNoticeByCustomNoticeIdCount(Map<String, Object> param) {
        return noticeDao.getCustomAllNoticeByCustomNoticeIdCount(param);
    }

    /**
     * 根据id修改已读未读
     *
     * @param id
     */
    @Override
    public void updateCustomNoticeReadIsById(Integer accountId, String id) {
        noticeDao.updateCustomNoticeReadIsById(accountId, id);
    }

    /**
     * 通过id查询公告详情
     *
     * @param id
     * @return
     */
    @Override
    public List<Map<String, Object>> getCustomANoticeByNoticeId(String id) {
        return noticeDao.getCustomANoticeByNoticeId(id);
    }

    @Override
    public List<Notice> getNoticeByCustomType(String customType) {
        return noticeDao.getNoticeByCustomType(customType);
    }

    /**
     * 修改状态为失效
     *
     * @param id
     */
    @Override
    public void updateNoticeEnabled(String id) {
        noticeDao.updateNoticeEnabled(id);
    }

    /**
     * 修改状态为删除
     *
     * @param id
     */
    @Override
    public void deleteNoticeEnabled(String id) {
        noticeDao.deleteNoticeEnabled(id);
    }

    /**
     * 查询系统组织结构
     *
     * @return
     */
    @Override
    public List<CustomOrganization> queryCustomOrganizationIdByTypeIs1() {
        return noticeDao.queryCustomOrganizationIdByTypeIs1();
    }

    /**
     * 查询自定义组织结构
     *
     * @return
     */
    @Override
    public List<CustomOrganization> queryCustomOrganizationIdByTypeIs2() {
        return noticeDao.queryCustomOrganizationIdByTypeIs2();
    }

    /**
     * 通过id查找parentId
     *
     * @param id
     * @return
     */
    @Override
    public List<Map<String, Object>> getOrganizationById(Integer id) {
        return noticeDao.getOrganizationById(id);
    }

    /**
     * 删除自定义组织
     *
     * @param id
     * @return
     */
    @Override
    public void deleteCustomOrganization(String id) {
        noticeDao.deleteCustomOrganization(id);
    }

    /**
     * 根据主键id查询对应类型
     *
     * @param id
     * @return
     */
    @Override
    public List<String> getCustomOrganization(String id) {
        return noticeDao.getCustomOrganization(id);
    }

    /**
     * 查询所有自定义组织信息（字段值有限，可新增）
     *
     * @return
     */
    @Override
    public List<CustomOrganization> selectAllCustomOrganization() {
        return noticeDao.selectAllCustomOrganization();
    }

    /**
     * 查询所有服务公司
     *
     * @return
     */
    @Override
    public List<Map<String, Object>> selectAllCompany() {
        return noticeDao.selectAllCompany();
    }

    /**
     * 根据商户类型查询自定义组织信息
     * @param customType
     * @return
     */
    @Override
    public CustomOrganization getCustomOrganizationByCustomType(int customType) {
        return noticeDao.getCustomOrganizationByCustomType(customType);
    }

    /**
     * 根据主键id查询子节点
     * @param id
     * @return
     */
    @Override
    public List<CustomOrganization> getCustomOrganizationByParentId(Integer id) {
        return noticeDao.getCustomOrganizationByParentId(id);
    }

    /**
     * 通过商户类型和系统身份类型获取公告信息
     * @param customType
     * @param id
     * @return
     */
    @Override
    public List<Notice> getCustomOrganizationByCustomTypeAndLoginRole(String customType, String id) {
        return noticeDao.getCustomOrganizationByCustomTypeAndLoginRole(customType,id);
    }

    /**
     * 通过商户类型查询公告
     * @param customType
     * @return
     */
    @Override
    public List<Notice> getOrganizationByCustomType(String customType) {
        return noticeDao.getOrganizationByCustomType(customType);
    }

    /**
     * 获取商户管理员的id
     * @param customType
     * @return
     */
    @Override
    public CustomOrganization getCustomAdminByCustomType(int customType) {
        return noticeDao.getCustomAdminByCustomType(customType);
    }
}
