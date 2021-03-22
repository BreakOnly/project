package com.jrmf.payment.openapi.param.deliver;

import com.jrmf.payment.openapi.param.BaseParam;
import com.jrmf.payment.openapi.param.IObject;

/**
 * 
 * @author Napoleon.Chen
 * @date 2019年1月16日
 */
public class ReceiptAsyncNotifyParam extends BaseParam<ReceiptAsyncNotifyParam.Data> {
	
	public static class Data implements IObject {
	    private String code;
	    private String msg;
	    private String reqNo;
	    private String outOrderNo;
	    private String attach;
	    private String downloadUrl;
	
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
	
	    public String getReqNo() {
	        return reqNo;
	    }
	
	    public void setReqNo(String reqNo) {
	        this.reqNo = reqNo;
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
	
	    public String getDownloadUrl() {
	        return downloadUrl;
	    }
	
	    public void setDownloadUrl(String downloadUrl) {
	        this.downloadUrl = downloadUrl;
	    }
	}
}
