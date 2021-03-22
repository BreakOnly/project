package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.ChannelInvoice;
import org.springframework.stereotype.Repository;

/** 
* @author zhangzehui
* @time 2017-12-16 
*  
*/
@Mapper
public interface ChannelInvoiceDao {

	public void addChannelInvoice(ChannelInvoice invoice);
	
	public ChannelInvoice getChannelInvoiceById(@Param("id")String id);
	
	public String getInvoiceBalance(@Param("customkey")String customkey);
	
	public List<ChannelInvoice> getChannelInvoiceByParam(Map<String,Object> param);
	
	public void updateChannelInvoice(Map<String,Object> param);

}
 