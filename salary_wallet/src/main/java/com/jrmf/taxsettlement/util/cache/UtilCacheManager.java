package com.jrmf.taxsettlement.util.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

public interface UtilCacheManager {

	boolean exists(String key);
	
	Object get(String key);

	Object put(String key, Object value, int cacheLife);
	
	boolean putIfAbsent(String key, Object value, int cacheLife);

	long getCacheLife(String key);
	
	Object remove(String key);

	Long increase(String key);
	
	Long decrease(String key);
	
	Long increaseBy(String key, long addNum);
	
	Long decreaseBy(String key, long substractNum);

	Set<String> keys(String pattern);

	void setMap(String key, Map<String, String> map);
	
	void setMap(String key, Map<String, String> map, int cacheLife);
	
	Map<String, String> getMap(String key);
	
	void setMapValue(String key, String fieldName, String fieldValue);
	
	String getMapValue(String key, String fieldName);
	
	Long changeMapValueBy(String key, String fieldName, long changeNum);
	
	boolean existsMapField(String key, String fieldName);

	void setList(String key, List<String> list);
	
	void setList(String key, List<String> list, int cacheLife);


	int getListSize(String key);

	List<String> getList(String key);

	String leftGetList(String key);

	void addSet(String key, Set<String> setValues);
	
	void addSet(String key, Set<String> setValues, int cacheLife);

	boolean existsMember(String key, String member);

	boolean releaseLock(String lockKey, boolean lock);

	boolean lockWithTimeout(String locaName, long acquireTimeout, long timeout);

}
