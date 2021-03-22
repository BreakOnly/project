package com.jrmf.payment.ymyf.entity;

import java.io.Serializable;

/**
 * 公共接口请求信息实体类
 *
 * @author Admin
 */
public class ResultMessage implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 响应数据
     */
    private String resData;

    /**
     * 响应码
     */
    private String resCode;

    /**
     * 响应信息
     */
    private String resMsg;

    /**
     * 签名
     */
    private String sign;


    public String getResData() {
        return resData;
    }

    public void setResData(String resData) {
        this.resData = resData;
    }

    public String getResCode() {
        return resCode;
    }

    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    public String getResMsg() {
        return resMsg;
    }

    public void setResMsg(String resMsg) {
        this.resMsg = resMsg;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "ResultMessage{" +
                "resData='" + resData + '\'' +
                ", resCode='" + resCode + '\'' +
                ", resMsg='" + resMsg + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
