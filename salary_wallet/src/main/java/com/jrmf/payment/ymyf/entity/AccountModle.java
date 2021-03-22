package com.jrmf.payment.ymyf.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.jrmf.payment.ymyf.util.Constant;

public class AccountModle implements Serializable {
	
	private Map<String, String> accountMap;//map的key存放税优通道ID，map的value存放对应的账户余额
	
	private Long levyId;//税优通道ID,当levyId=0时查询结果返回商户签约的所有税优地的余额
    //返回码
    private String resCode = Constant.SUCCESS;
    
    //返回信息
    private String resMsg  = Constant.SUCCESS_INFO;


	public Map<String, String> getAccountMap() {
		return accountMap;
	}

	public void setAccountMap(Map<String, String> accountMap) {
		this.accountMap = accountMap;
	}

	public Long getLevyId() {
		return levyId;
	}

	public void setLevyId(Long levyId) {
		this.levyId = levyId;
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
    
}
