package com.jrmf.taxsettlement.api.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.domain.ChannelRelated;
import com.jrmf.persistence.ChannelRelatedDao;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.APIDockingRetCodes;

public class ChannelRelatedCache {

	@Autowired
	private ChannelRelatedDao channelRelatedDao;

	private Map<String, ChannelRelated> channelRelatedCacheTable = new ConcurrentHashMap<String, ChannelRelated>();

	public ChannelRelated getChannelRelated(String merchantId, String transferCorpId) {

		String key = new StringBuilder(merchantId).append(transferCorpId).toString();
		ChannelRelated channelWithContract = channelRelatedCacheTable.get(key);
		if (channelWithContract == null) {
			synchronized (channelRelatedCacheTable) {
				channelWithContract = channelRelatedCacheTable.get(key);
				if (channelWithContract == null) {
					channelWithContract = channelRelatedDao.getRelatedByCompAndOrig(merchantId, transferCorpId);
					if (channelWithContract == null) {
						throw new APIDockingException(APIDockingRetCodes.NO_CONTRACT_WITH_AGENT.getCode(),
								new StringBuilder(merchantId).append("-").append(transferCorpId).toString());
					}
					channelRelatedCacheTable.put(key, channelWithContract);
				}
			}
		}
		return channelWithContract;
	}
}
