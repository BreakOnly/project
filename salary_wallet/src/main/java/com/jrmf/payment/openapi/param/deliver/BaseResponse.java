package com.jrmf.payment.openapi.param.deliver;

import com.jrmf.payment.openapi.param.IObject;

/**
 * <p> 发放接口异步通知的客户端响应数据结构
 * <p> <code> "0000".equals(code),表示异步通知成功,代发服务不再发送异步通知 </code>
 * @author Napoleon.Chen
 * @date 2018年12月5日
 */
public class BaseResponse implements IObject {
    private String code;
    private String msg;
    private String sign;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
