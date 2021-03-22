package com.jrmf.payment.ymyf.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 公共接口请求信息实体类
 *
 * @author Admin
 */
@Data
public class PayNotifyModel implements Serializable {

    private String funCode;
    private String merchantId;
    private String resData;
    private String resCode;
    private String resMsg;
    private String sign;
    private String version;
}
