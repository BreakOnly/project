package com.jrmf.payment.openapi.param.econtract;

import com.jrmf.payment.openapi.param.IObject;

public class CommonExtrResult implements java.io.Serializable, IObject {
    private static final long serialVersionUID = 1L;

    private String sign;
    private String resultCode;
    private String resultMessage;

    public CommonExtrResult(String resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    public CommonExtrResult() {
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

}
