package com.jrmf.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @Title: SignShare
 * @Description: 共享签约配置类 (t_sign_share_limit & t_sign_share_scope)
 * @create 2020/4/27 9:47
 */
@Getter
@Setter
public class SignShare {

    private int id;

    private int type;

    private String customkey;

    private int limitId;

    private String limitName;

    private int status;

    private String createTime;

    private String updateTime;

    private String limitGroupId;

    private String shareCustomkey;

    private int limitStatus;

    private String limitCreateTime;

    private String limitUpdateTime;

    private String typeName;

    private String companyName;

    private String statusName;

    private String shareCompanyName;

    private String groupCustomKeys;

    private String oldLimitGroupId;
}
