package com.jrmf.service;

import com.alibaba.fastjson.JSON;
import com.jrmf.domain.OperateRecord;
import com.jrmf.domain.OperateURL;
import com.jrmf.persistence.OperateRecordDao;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 种路路
 * @create 2020-02-26 13:41
 * @desc 操作记录
 **/
@Service("operateRecordService")
public class OperateRecordServiceImpl implements OperateRecordService {

    @Autowired
    private OperateRecordDao operateRecordDao;

    @Autowired
    private UtilCacheManager cacheManager;
    /**
     * 保存记录
     * @param operateRecord 记录
     */
    @Override
    public void addOperateRecord(OperateRecord operateRecord) {
        operateRecordDao.addOperateRecord(operateRecord);
    }

    /**
     * 获取OperateURL
     * @param url
     */
    @Override
    public OperateURL getOperateURL(String url) {
        Object object = cacheManager.get(url);
        if(object != null){
            return JSON.parseObject(object.toString(), OperateURL.class);
        }
        return null;
    }

    @PostConstruct
    public void loadAndInit() {
        Map<String, Object> params = new HashMap<>(4);
        for (OperateURL operateURL : operateRecordDao.getURLList(params)) {
            cacheManager.put(operateURL.getUrl(), JSON.toJSONString(operateURL), -1);
        }
    }


}
