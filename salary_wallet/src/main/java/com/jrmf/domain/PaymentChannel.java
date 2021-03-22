package com.jrmf.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @Title: ChannelRoute
 * @Description: 通道路由基础配置
 * @create 2020/3/23 15:00
 */
@Getter
@Setter
public class PaymentChannel {

    private Integer id;

    private String pathNo;

    private String pathName;

    private String pathDesc;

    private String createTime;

    private String updateTime;

    private Integer pathType;

    private Integer pathKeyType;
}
