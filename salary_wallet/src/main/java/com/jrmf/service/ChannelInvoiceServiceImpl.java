package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.ChannelInvoice;
import com.jrmf.persistence.ChannelInvoiceDao;

/** 
* @author zhangzehui
* @version 创建时间：2017年12月16日
* 
*/
@Service("channelInvoiceService")
public class ChannelInvoiceServiceImpl implements ChannelInvoiceService {

	@Autowired
	private ChannelInvoiceDao channelInvoiceDao;
	
	@Override
	public void addChannelInvoice(ChannelInvoice invoice) {
		channelInvoiceDao.addChannelInvoice(invoice);
	}

	@Override
	public ChannelInvoice getChannelInvoiceById(String id) {
		return channelInvoiceDao.getChannelInvoiceById(id);
	}

	@Override
	public List<ChannelInvoice> getChannelInvoiceByParam(
			Map<String, Object> param) {
		return channelInvoiceDao.getChannelInvoiceByParam(param);
	}

	@Override
	public String getInvoiceBalance(String customkey) {
		return channelInvoiceDao.getInvoiceBalance(customkey);
	}

	@Override
	public void updateChannelInvoice(Map<String, Object> param) {
		channelInvoiceDao.updateChannelInvoice(param);
	}
}
 