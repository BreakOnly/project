package com.jrmf.service;

import com.jrmf.domain.CustomProxy;
import com.jrmf.persistence.CustomGroupDao;
import com.jrmf.persistence.CustomProxyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: CustomProxyServiceImpl
 * @Description: 代理商接口
 * @create 2019/10/14 14:28
 */
@Service("customProxyService")
public class CustomProxyServiceImpl implements CustomProxyService{

    @Autowired
    CustomProxyDao customProxyDao;

    @Autowired
    CustomGroupDao customGroupDao;

    /**
     * 获取代理商ID根据商户唯一标识
     * @param customkey
     * @return
     */
    @Override
    public CustomProxy getProxyIdByCustomkey(String customkey) {
        return customProxyDao.getProxyIdByCustomkey(customkey);
    }

    /**
     * 通过代理商ID查询绑定的商户key
     * @param levelCode
     * @return
     */
    @Override
    public List<String> getCustomkeyByProxyId(String levelCode) {
        return customProxyDao.getCustomkeyByProxyId(levelCode);
    }

    /**
     * 通过customkey查询levelcode
     * @param newCustomkey
     * @return
     */
    @Override
    public List<String> getLevelCodeByCustomkey(String newCustomkey) {
        return customGroupDao.getLevelCodeByCustomkey(newCustomkey);
    }

    /**
     * 查询 商户唯一标识 通过leveCode
     * @param code
     * @return
     */
    @Override
    public List<String> getCustomkeyByLeveCode(String code) {
        return customGroupDao.getCustomkeyByLeveCode(code);
    }

    /**
     * 通过唯一标识查询levelcode
     * @param newCustomkey
     * @return
     */
    @Override
    public List<String> getProxyLevelCodeByCustomkey(String newCustomkey) {
        return customProxyDao.getProxyLevelCodeByCustomkey(newCustomkey);
    }
}
