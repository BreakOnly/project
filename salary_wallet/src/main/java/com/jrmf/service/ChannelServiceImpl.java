package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.Channel;
import com.jrmf.persistence.ChannelDao;

/** 
* @author zhangzehui
* @version 创建时间：2018年05月21日
* 
*/
@Service("channelService")
public class ChannelServiceImpl implements ChannelService {

	@Autowired
	private ChannelDao channelDao;

	@Override
	public int addChannel(Channel channel) {
		return channelDao.addChannel(channel);
	}

	@Override
	public Channel getChannelByOriginalId(String originalId) {
		return channelDao.getChannelByOriginalId(originalId);
	}

	@Override
	public Channel getChannelHistoryById(String id) {
		return channelDao.getChannelHistoryById(id);
	}

	@Override
	public List<Channel> getChannelList(Map<String, Object> param) {
		return channelDao.getChannelList(param);
	}

	@Override
	public int getChannelListCount(Map<String, Object> param) {
		return channelDao.getChannelListCount(param);
	}

	@Override
	public void updateChannel(Channel channel) {
		channelDao.updateChannel(channel);
	}

}
 