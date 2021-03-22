package com.jrmf.persistence;

import com.jrmf.domain.CustomInfo;
import com.jrmf.domain.WebCusotmInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface WebCusotmInfoDao {
    int addWebCustomInfo(WebCusotmInfo webCusotmInfo);

    List<WebCusotmInfo> listCustomInfo(Map<String, Object> params);
}
