package com.jrmf.service;

import com.jrmf.domain.PageVisitRecord;
import com.jrmf.domain.UserRegisterRecord;
import com.jrmf.persistence.UserRegisterRecordDao;
import com.jrmf.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.Map;

/**
 * @Title: UserRegisterRecordServiceImpl
 * @Description: 用户登记咨询记录
 * @create 2020/2/21 14:44
 */
@Service("UserRegisterRecordService")
public class UserRegisterRecordServiceImpl implements UserRegisterRecordService{

    @Autowired
    private UserRegisterRecordDao userRegisterRecordDao;

    private static final Logger logger = LoggerFactory.getLogger(UserRegisterRecordServiceImpl.class);

    /**
     * 用户登记咨询记录查询
     * @param paramMap
     * @return
     */
    @Override
    public List<UserRegisterRecord> queryRecordList(Map<String, Object> paramMap) {
        return userRegisterRecordDao.queryRecordList(paramMap);
    }

    /**
     * 计数用户登记咨询记录查询
     * @param paramMap
     * @return
     */
    @Override
    public int queryRecordListCount(Map<String, Object> paramMap) {
        return userRegisterRecordDao.queryRecordListCount(paramMap);
    }


    /**
     * 添加用户信息
     * @param userRegisterRecord
     * @return
     */
    @Override
    public void insertUserRegisterRecord(UserRegisterRecord userRegisterRecord) {
        userRegisterRecordDao.insertUserRegisterRecord(userRegisterRecord);
    }

    /**
     * H5页面 增加访问记录和数量
     * @param pageVisitRecord
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertPageVisitRecord(PageVisitRecord pageVisitRecord) {
        try {
            // 新增页面访问量表
            pageVisitRecord.setPageDate(DateUtils.getNowDay());
            userRegisterRecordDao.insertPageVisitAmount(pageVisitRecord);

            //新增页面访问记录表
            pageVisitRecord.setAmountId(pageVisitRecord.getId());
            userRegisterRecordDao.insertPageVisitRecord(pageVisitRecord);
        } catch (Exception e) {
            logger.error("添加页面访问记录失败 pageVisitRecord:{}", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    /**
     * 根据username 和phoneNo 查询
     * @param userName
     * @param phoneNo
     * @return
     */
    @Override
    public int selectUserRegisterRecord(String userName, String phoneNo) {
        return userRegisterRecordDao.selectUserRegisterRecord(userName, phoneNo);
    }
}
