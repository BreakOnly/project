package com.jrmf.payment.openapi.param.deliver;

import com.jrmf.payment.openapi.param.BaseParam;
import com.jrmf.payment.openapi.param.IObject;

/**
 * <p> 发放接口异步传输消息体
 * <p> 如果是java spring,建议使用 <code> @RequestBody PayNotifyParam </code>
 * @author Napoleon.Chen
 * @date 2018年11月30日
 */
public class PayNotifyParam extends BaseParam<PayNotifyParam.Data> {
    public static class Data implements IObject {
        private String code;
        private String msg;
        private String exceptionCode;
        private String reqNo;
        private String orderNo;
        private String outOrderNo;
        private String attach;


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

        public String getExceptionCode() {
            return exceptionCode;
        }

        public void setExceptionCode(String exceptionCode) {
            this.exceptionCode = exceptionCode;
        }

        public String getReqNo() {
            return reqNo;
        }

        public void setReqNo(String reqNo) {
            this.reqNo = reqNo;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public String getOutOrderNo() {
            return outOrderNo;
        }

        public void setOutOrderNo(String outOrderNo) {
            this.outOrderNo = outOrderNo;
        }

        public String getAttach() {
            return attach;
        }

        public void setAttach(String attach) {
            this.attach = attach;
        }
    }
}