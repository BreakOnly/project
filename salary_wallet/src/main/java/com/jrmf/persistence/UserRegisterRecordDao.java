package com.jrmf.persistence;

import com.jrmf.domain.PageVisitRecord;
import com.jrmf.domain.UserRegisterRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @Title: UserRegisterRecordDao
 * @Description: 用户登记咨询记录
 * @create 2020/2/21 14:47
 */
@Mapper
public interface UserRegisterRecordDao {

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

    void insertUserRegisterRecord(UserRegisterRecord userRegisterRecord);

    /**
     * 新增页面访问数量表
     * @param pageVisitRecord
     */
    void insertPageVisitAmount(PageVisitRecord pageVisitRecord);

    /**
     * 新增页面访问记录表
     * @param pageVisitRecord
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
