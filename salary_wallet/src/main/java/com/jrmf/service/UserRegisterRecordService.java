package com.jrmf.service;

import com.jrmf.domain.PageVisitRecord;
import com.jrmf.domain.UserRegisterRecord;

import java.util.List;
import java.util.Map;

/**
 * @Title: UserRegisterRecordService
 * @Description: 用户登记咨询记录
 * @create 2020/2/21 14:44
 */
public interface UserRegisterRecordService {
    /**
     * 用户登记咨询记录查询
     * @param paramMap
     * @return
     */
    List<UserRegisterRecord> queryRecordList(Map<String, Object> paramMap);

    /**
     * 计数用户登记咨询记录查询
     * @param paramMap
     * @return
     */
    int queryRecordListCount(Map<String, Object> paramMap);

    /**
     * 添加用户信息
     * @param userRegisterRecord
     * @return
     */
    void insertUserRegisterRecord(UserRegisterRecord userRegisterRecord);

    /**
     * H5页面 增加访问记录和数量
     * @param pageVisitRecord
     * @return
     */
    void insertPageVisitRecord(PageVisitRecord pageVisitRecord);

    /**
     * 根据username 和phoneNo 查询
     * @param userName
     * @param phoneNo
     * @return
     */
    int selectUserRegisterRecord(String userName, String phoneNo);
}
