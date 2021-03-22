package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jrmf.domain.ChannelInvoice;

/** 
* @author zhangzehui
* @version 创建时间：2017年12月16日
*/
@Service
public interface ChannelInvoiceService {

    public void addChannelInvoice(ChannelInvoice invoice);
	
	public ChannelInvoice getChannelInvoiceById(String id);
	
	public String getInvoiceBalance(String customkey);
	
	public List<ChannelInvoice> getChannelInvoiceByParam(Map<String,Object> param);
	
	public void updateChannelInvoice(Map<String,Object> param);
	
}
 