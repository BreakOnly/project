package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.Channel;
import org.springframework.stereotype.Repository;

/** 
* @author zhangzehui
* @time 2018-05-21
*  
*/
@Mapper
public interface ChannelDao {

	public int addChannel(Channel channel);
	
	public Channel getChannelByOriginalId(@Param("originalId")String originalId);
	
	public Channel getChannelHistoryById(@Param("id")String id);
	
	public List<Channel> getChannelList(Map<String,Object> param);
	
	public int getChannelListCount(Map<String,Object> param);

	public void updateChannel(Channel channel);
	

}
 