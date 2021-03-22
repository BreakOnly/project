package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jrmf.domain.ChannelRelated;

/** 
* @author zhangzehui
* @version 创建时间：2018年04月25日
*/
@Service
public interface ChannelRelatedService {

	void createChannelRelated(ChannelRelated channelRelated);
	
	void updateChannelRelated(ChannelRelated channelRelated);
	
	List<ChannelRelated> getRelatedByParam(Map<String, Object> param);
	
	int getRelatedCountByParam(Map<String, Object> param);
	
	void updateRelatedStatus(Map<String, Object> param);
	
	List<ChannelRelated> getRelatedByOriginalId(String customkey,String originalId);
	
	ChannelRelated getRelatedById(String id);
	
	ChannelRelated getRelatedByCompAndOrigAll(String originalId, String companyId);

	ChannelRelated getRelatedByCompAndOrig(String originalId, String companyId);

    List<ChannelRelated> getRelatedList(String customkey);

    List<ChannelRelated> queryRelatedList(Map<String, Object> params);

	List<String> queryCustomKeysByCompanyId(String companyId);

  List<Map<String, Object>> listCompanyByOriginalId(String companyId);
}
 