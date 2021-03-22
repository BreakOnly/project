package com.jrmf.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 签约要素规则类 (t_sign_element_rule)<br/>
 * @author: <br/>
 * @create：2020年04⽉28⽇<br/>
 */
@Data
public class SignElementRule implements Serializable {

    private int id;

    private String merchantId;

    private String merchantName;

    private int companyId;

    private String companyName;

    private String signLevel;

    private String signLevelName;

    private int signRule;

    private String signRuleName;

    private int papersRequire;

    private String papersRequireName;

    private int status;

    private String statusName;

    private String remark;

    private String createTime;

    private String updateTime;
}
