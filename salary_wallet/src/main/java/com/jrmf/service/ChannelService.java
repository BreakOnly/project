package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jrmf.domain.Channel;

/** 
* @author zhangzehui
* @version 创建时间：2018年05月21日
*/
@Service
public interface ChannelService {

	public int addChannel(Channel channel);
	
	public Channel getChannelByOriginalId( String originalId);
	
	public Channel getChannelHistoryById( String id);
	
	public List<Channel> getChannelList(Map<String,Object> param);
	
	public int getChannelListCount(Map<String,Object> param);

	public void updateChannel(Channel channel);
	
}
 