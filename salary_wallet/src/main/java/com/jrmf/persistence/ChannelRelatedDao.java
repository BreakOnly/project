package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.ChannelRelated;

/** 
* @author zhangzehui
* @version 创建时间：2018年4月25日  
* 类说明 
*/
@Mapper
public interface ChannelRelatedDao {

	List<ChannelRelated> getRelatedList(String customkey);

	void createChannelRelated(ChannelRelated channelRelated);
	
	void updateChannelRelated(ChannelRelated channelRelated);
	
	List<ChannelRelated> getRelatedByParam(Map<String, Object> param);
	
	int getRelatedCountByParam(Map<String, Object> param);
	
	void updateRelatedStatus(Map<String, Object> param);
	
	List<ChannelRelated> getRelatedByOriginalId(@Param("merchantId")String merchantId,@Param("originalId")String originalId);
	
	ChannelRelated getRelatedById(@Param("id")String id);
	
	ChannelRelated getRelatedByCompAndOrigAll(@Param("originalId")String originalId,@Param("companyId")String companyId);

	ChannelRelated getRelatedByCompAndOrig(@Param("originalId")String originalId,@Param("companyId")String companyId);

    List<ChannelRelated> queryRelatedList(Map<String, Object> params);

    List<String> queryCustomKeysByCompanyId(String companyId);

	ChannelRelated getRelatedByCustomKeyAndCompanyId(@Param("customKey") String customKey, @Param("companyId") String companyId);

	String getNameByCustomKey(String customKey);

  List<Map<String, Object>> listCompanyByOriginalId(String companyId);
}
 