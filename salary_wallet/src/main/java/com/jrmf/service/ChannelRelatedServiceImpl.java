package com.jrmf.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.ChannelRelated;
import com.jrmf.persistence.ChannelRelatedDao;

/** 
* @author zhangzehui
* @version 创建时间：2017年12月16日
* 
*/
@Service("channelRelatedService")
public class ChannelRelatedServiceImpl implements ChannelRelatedService {

	@Autowired
	private ChannelRelatedDao channelRelatedDao;
	
	@Override
	public List<ChannelRelated> getRelatedList(String customkey) {
		return channelRelatedDao.getRelatedList(customkey);
	}

	@Override
	public List<ChannelRelated> queryRelatedList(Map<String, Object> params) {
		return channelRelatedDao.queryRelatedList(params);
	}

	@Override
	public List<String> queryCustomKeysByCompanyId(String companyId) {
		return channelRelatedDao.queryCustomKeysByCompanyId(companyId);
	}

	@Override
	public List<Map<String, Object>> listCompanyByOriginalId(String companyId) {
		return channelRelatedDao.listCompanyByOriginalId(companyId);
	}

	@Override
	public void createChannelRelated(ChannelRelated channelRelated) {
		channelRelatedDao.createChannelRelated(channelRelated);
	}

	@Override
	public void updateChannelRelated(ChannelRelated channelRelated) {
		channelRelatedDao.updateChannelRelated(channelRelated);
	}

	@Override
	public List<ChannelRelated> getRelatedByParam(Map<String, Object> param) {
		return channelRelatedDao.getRelatedByParam(param);
	}

	@Override
	public int getRelatedCountByParam(Map<String, Object> param) {
		return channelRelatedDao.getRelatedCountByParam(param);
	}

	@Override
	public void updateRelatedStatus(Map<String, Object> param) {
		channelRelatedDao.updateRelatedStatus(param);
	}

	@Override
	public List<ChannelRelated> getRelatedByOriginalId(String customkey,
			String originalId) {
		return channelRelatedDao.getRelatedByOriginalId(customkey, originalId);
	}

	@Override
	public ChannelRelated getRelatedById(String id) {
		return channelRelatedDao.getRelatedById(id);
	}

	@Override
	public ChannelRelated getRelatedByCompAndOrigAll(String originalId,
			String companyId) {
		return channelRelatedDao.getRelatedByCompAndOrigAll(originalId, companyId);
	}

	@Override
	public ChannelRelated getRelatedByCompAndOrig(String originalId, String companyId) {
		return channelRelatedDao.getRelatedByCompAndOrig(originalId,companyId);
	}

}
 