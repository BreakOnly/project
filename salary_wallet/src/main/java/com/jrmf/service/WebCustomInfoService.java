package com.jrmf.service;

import com.jrmf.domain.CustomInfo;
import com.jrmf.domain.WebCusotmInfo;
import com.jrmf.persistence.WebCusotmInfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WebCustomInfoService {
    private static Logger logger = LoggerFactory.getLogger(WebCustomInfoService.class);
    private final WebCusotmInfoDao webCusotmInfoDao;

    @Autowired
    public WebCustomInfoService(WebCusotmInfoDao webCusotmInfoDao) {
        this.webCusotmInfoDao = webCusotmInfoDao;
    }

    public boolean addWebCustomInfo(WebCusotmInfo webCusotmInfo) {
        try {
            webCusotmInfoDao.addWebCustomInfo(webCusotmInfo);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return true;
    }

    public List<WebCusotmInfo> listCustomInfo(Map<String, Object> params) {
        return webCusotmInfoDao.listCustomInfo(params);
    }
}
