package com.jrmf.service;

import com.jrmf.domain.CustomOrganization;
import com.jrmf.domain.Notice;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @Title: NoticeService
 * @Description:
 * @create 2020/1/15 16:21
 */
@Service
public interface NoticeService {
    /**
     * 查询总条数
     * @param paramMap
     * @return
     */
    int getNoticeCount(Map<String, Object> paramMap);

    /**
     * 查询公告信息 及 统计阅读数 & 查看率
     * @param paramMap
     * @return
     */
    List<Notice> getNoticeList(Map<String, Object> paramMap);

    List<CustomOrganization> queryCustomOrganizationId(String id);

    Map<String, Object> insertNotice(Notice notice, MultipartFile accessoryNameFile);

    /**
     * 新增自定义组织
     * @param customOrganization
     * @return
     */
    Map<String, Object> insertCustomOrganization(CustomOrganization customOrganization);

    Notice getNoticeById(String id);

    /**
     * 根据商户主键查询 商户公告中间表
     * @param paramMap
     * @return
     */
    List<Map<String, Object>> getCustomNoticeByCustomNoticeId(Map<String, Object> paramMap);

    /**
     * 根据主键id修改为已读
     * @param param
     */
    void updateCustomNoticeReadIsByIds(Map<String, Object> param);

    /**
     * 查询该商户下所有公告
     * @param param
     * @return
     */
    List<Map<String, Object>> getCustomAllNoticeByCustomNoticeId(Map<String, Object> param);

    /**
     * 统计查询该商户下所有公告数量
     * @param param
     * @return
     */
    int getCustomAllNoticeByCustomNoticeIdCount(Map<String, Object> param);

    /**
     * 根据id修改已读未读
     * @param accountId
     */
    void updateCustomNoticeReadIsById(Integer accountId, String id);

    /**
     * 通过id查询公告详情
     * @param id
     * @return
     */
    List<Map<String, Object>> getCustomANoticeByNoticeId(String id);

	List<Notice> getNoticeByCustomType(String customType);

    /**
     * 修改状态为失效
     * @param id
     */
    void updateNoticeEnabled(String id);

    /**
     * 修改状态为删除
     * @param id
     */
    void deleteNoticeEnabled(String id);

    /**
     * 查询系统组织结构
     * @return
     */
    List<CustomOrganization> queryCustomOrganizationIdByTypeIs1();

    /**
     * 查询自定义组织结构
     * @return
     */
    List<CustomOrganization> queryCustomOrganizationIdByTypeIs2();

    /**
     * 通过id查找parentId
     * @param id
     * @return
     */
    List<Map<String, Object>> getOrganizationById(Integer id);

    /**
     * 删除自定义组织
     * @param id
     * @return
     */
    void deleteCustomOrganization(String id);

    /**
     * 根据主键id查询对应类型
     * @param id
     * @return
     */
    List<String> getCustomOrganization(String id);

    /**
     * 查询所有自定义组织信息（字段值有限，可新增）
     * @return
     */
    List<CustomOrganization> selectAllCustomOrganization();

    /**
     * 查询所有服务公司
     * @return
     */
    List<Map<String,Object>> selectAllCompany();

    /**
     * 根据商户类型查询自定义组织信息
     * @param customType
     * @return
     */
    CustomOrganization getCustomOrganizationByCustomType(int customType);

    /**
     * 根据主键id查询子节点
     * @param id
     * @return
     */
    List<CustomOrganization> getCustomOrganizationByParentId(Integer id);

    /**
     * 通过商户类型和系统身份类型获取公告信息
     * @param customType
     * @param id
     * @return
     */
    List<Notice> getCustomOrganizationByCustomTypeAndLoginRole(String customType, String id);

    /**
     * 通过商户类型查询公告
     * @param customType
     * @return
     */
    List<Notice> getOrganizationByCustomType(String customType);

    /**
     * 获取商户管理员的id
     * @param customType
     * @return
     */
    CustomOrganization getCustomAdminByCustomType(int customType);
}
