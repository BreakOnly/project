package com.jrmf.service;

import com.jrmf.domain.CustomProxy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: CustomProxyService
 * @Description: 代理商接口
 * @create 2019/10/14 14:24
 */
@Service
public interface CustomProxyService {

    /**
     * 获取代理商ID根据商户唯一标识
     * @param customkey
     * @return
     */
    CustomProxy getProxyIdByCustomkey(String customkey);

    /**
     * 通过代理商ID查询绑定的商户key
     * @param levelCode
     * @return
     */
    List<String> getCustomkeyByProxyId(String levelCode);

    /**
     * 通过customkey查询levelcode
     * @param newCustomkey
     * @return
     */
    List<String> getLevelCodeByCustomkey(String newCustomkey);

    /**
     * 查询 商户唯一标识 通过leveCode
     * @param code
     * @return
     */
    List<String> getCustomkeyByLeveCode(String code);

    /**
     * 通过唯一标识查询levelcode
     * @param newCustomkey
     * @return
     */
    List<String> getProxyLevelCodeByCustomkey(String newCustomkey);
}
