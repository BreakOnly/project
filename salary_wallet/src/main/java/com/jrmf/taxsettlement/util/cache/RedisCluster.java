package com.jrmf.taxsettlement.util.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RedisCluster implements UtilCacheManager {

	private static final Logger logger = LoggerFactory.getLogger(RedisCluster.class);

	private static final String SUCC_RET = "OK";

	@Autowired
	private JedisCluster jedisCluster;

	private byte[] toByteArrayKey(String key) {
		return key.getBytes(StandardCharsets.UTF_8);
	}

	private byte[] toByteArrayObj(Object obj) throws IOException {

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(bytes);
		objOut.writeObject(obj);
		objOut.flush();
		return bytes.toByteArray();
	}

	private Object toObj(byte[] byteArray) throws IOException, ClassNotFoundException {

		if (byteArray == null) {
			return null;
		}

		try (ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(byteArray))) {
			return objIn.readObject();
		}
	}

	@Override
	public Object get(String key) {
		try {
			return toObj(jedisCluster.get(toByteArrayKey(key)));
		}catch (Exception e) {
			throw new UtilCacheException(e);
		} 
	}

	@Override
	public long getCacheLife(String key) {
		try {
			return jedisCluster.ttl(toByteArrayKey(key));
		}catch (Exception e) {
			throw new UtilCacheException(e);
		} 
	}

	@Override
	public Object put(String key, Object value, int cacheLife) {
		byte[] byteArrayKey = toByteArrayKey(key);
		try {
			Object previous = toObj(jedisCluster.get(byteArrayKey));
			byte[] bytesKey = toByteArrayKey(key);
			byte[] bytesValue = toByteArrayObj(value);
			String jedisRetCode = jedisCluster.set(bytesKey, bytesValue);

			if (cacheLife > 0) {
				jedisCluster.expire(bytesKey, cacheLife);
			}

			if (!SUCC_RET.equals(jedisRetCode)) {
				logger.error("fail to set value of key[{}] to jedis with retcode:[{}]", key,
						jedisRetCode);
			}
			return previous;
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public boolean putIfAbsent(String key, Object value, int cacheLife) {
		try {
			byte[] bytesKey = toByteArrayKey(key);
			byte[] bytesValue = toByteArrayObj(value);
			boolean setOk = jedisCluster.setnx(bytesKey, bytesValue) == 1;

			if (setOk && cacheLife > 0) {
				jedisCluster.expire(bytesKey, cacheLife);
			}

			if (!setOk) {
				logger.debug("fail to set value of key[{}] to jedis", key);
			}
			return setOk;
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public Object remove(String key) {
		byte[] byteArrayKey = toByteArrayKey(key);
		try {
			Object previous = toObj(jedisCluster.get(byteArrayKey));
			jedisCluster.del(byteArrayKey);
			return previous;
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public boolean exists(String key) {
		byte[] byteArrayKey = toByteArrayKey(key);
		try {
			return jedisCluster.exists(byteArrayKey);
		}catch (Exception e) {
			throw new UtilCacheException(e);
		} 
	}

	@Override
	public void setMap(String key, Map<String, String> map) {
		try {
			String jedisRetCode = jedisCluster.hmset(key, map);
			if (!SUCC_RET.equals(jedisRetCode)) {
				logger.error("fail to set map of key[{}] to jedis with retcode:[{}]", key,
						jedisRetCode);
			}
		} catch (Exception e) {
			throw new UtilCacheException(e);
		} 
	}

	@Override
	public void setMap(String key, Map<String, String> map, int cacheLife) {
		try {
			String jedisRetCode = jedisCluster.hmset(key, map);
			if (cacheLife > 0) {
				jedisCluster.expire(key, cacheLife);
			}
			if (!SUCC_RET.equals(jedisRetCode)) {
				logger.error("fail to set map of key[{}] to jedis with retcode:[{}]", key,
						jedisRetCode);
			}
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public Map<String, String> getMap(String key) {
		try {
			return jedisCluster.hgetAll(key);
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public void setMapValue(String key, String fieldName, String fieldValue) {
		try {
			jedisCluster.hset(key, fieldName, fieldValue);
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public String getMapValue(String key, String fieldName) {
		try {
			return jedisCluster.hget(key, fieldName);
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public boolean existsMapField(String key, String fieldName) {
		try {
			return jedisCluster.hexists(key, fieldName);
		}catch (Exception e) {
			throw new UtilCacheException(e);
		} 
	}

	@Override
	public void setList(String key, List<String> list) {
		try {
			jedisCluster.lpush(key, list.toArray(new String[0]));
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public void setList(String key, List<String> list, int cacheLife) {
		try {
			jedisCluster.lpush(key, list.toArray(new String[0]));
			if (cacheLife > 0) {
				jedisCluster.expire(key, cacheLife);
			}
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}


	@Override
	public int getListSize(String key) {
		try {
			return jedisCluster.llen(key).intValue();
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public List<String> getList(String key) {
		try {
			return jedisCluster.lrange(key, 0, -1);
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public String leftGetList(String key) {
		try {
			return jedisCluster.lpop(key);
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public void addSet(String key, Set<String> setValues) {
		try {
			jedisCluster.sadd(key, setValues.toArray(new String[0]));
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public void addSet(String key, Set<String> setValues, int cacheLife) {
		try {
			jedisCluster.sadd(key, setValues.toArray(new String[0]));
			if (cacheLife > 0) {
				jedisCluster.expire(key, cacheLife);
			}
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public boolean existsMember(String key, String member) {
		try {
			return jedisCluster.sismember(key, member);
		}catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public Set<String> keys(String pattern) {
		try {
			logger.debug("Start getting keys... ");
			TreeSet<String> keys = new TreeSet<>();
			Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();

			for (String key : clusterNodes.keySet()) {
				logger.debug("Getting keys from: {}", key);
				JedisPool jedisPool = clusterNodes.get(key);
                try (Jedis jedisConn = jedisPool.getResource()) {
                    keys.addAll(jedisConn.keys(pattern));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
			}
			logger.debug("Keys gotten");
			return keys;
		}catch (Exception e) {
			throw new UtilCacheException(e);
		} 
	}

	@Override
	public Long increase(String key) {
		byte[] byteArrayKey = toByteArrayKey(key);
		try {
			return jedisCluster.incr(byteArrayKey);
		} catch (Exception e) {
			throw new UtilCacheException(e);
		} 
	}

	@Override
	public Long decrease(String key) {
		byte[] byteArrayKey = toByteArrayKey(key);
		try {
			return jedisCluster.decr(byteArrayKey);
		} catch (Exception e) {
			throw new UtilCacheException(e);
		} 
	}

	@Override
	public Long increaseBy(String key, long addNum) {
		byte[] byteArrayKey = toByteArrayKey(key);
		try {
			return jedisCluster.incrBy(byteArrayKey, addNum);
		} catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public Long decreaseBy(String key, long substractNum) {
		byte[] byteArrayKey = toByteArrayKey(key);
		try {
			return jedisCluster.decrBy(byteArrayKey, substractNum);
		} catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	@Override
	public Long changeMapValueBy(String key, String fieldName, long changeNum) {
		try {
			return jedisCluster.hincrBy(key, fieldName, changeNum);
		} catch (Exception e) {
			throw new UtilCacheException(e);
		}
	}

	/**
	 * 加锁
	 * @param lockName  锁的key
	 * @param acquireTimeout  获取超时时间
	 * @param lockExpiresTimeout   锁的超时时间
	 * @return 锁标识
	 */
	@Override
	public boolean lockWithTimeout(String lockName,long acquireTimeout, long lockExpiresTimeout) {
		// 锁名，即key值
		String lockKey = "lock:" + lockName;
    	long timeout = acquireTimeout;
        while (timeout >= 0){
            Long expires = System.currentTimeMillis() + lockExpiresTimeout + 1;
            String expiresStr = String.valueOf(expires);
            if (jedisCluster.setnx(lockKey,expiresStr)==1){
                return true;
            }
            String lockTimeStr = jedisCluster.get(lockKey);
            if (lockTimeStr != null && Long.parseLong(lockTimeStr) < System.currentTimeMillis()){
                String oldLockTimeStr = jedisCluster.getSet(lockKey,expiresStr);
                if (oldLockTimeStr != null && oldLockTimeStr.equals(lockTimeStr)){
                    return true;
                }
            }
            int sleepTime=new Random().nextInt(10)*100;
            timeout -= sleepTime;
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
							logger.error(e.getMessage(),e);
            }
        }
        return false;
	}

	/**
	 * 释放锁
	 * @param lockName 锁的key
	 * @param lock    释放锁的标识
	 * @return 释放成功失败
	 */
	@Override
	public boolean releaseLock(String lockName, boolean lock) {
		// 锁名，即key值
		String lockKey = "lock:" + lockName;
        if (lock){
        	jedisCluster.del(lockKey);
        	return true;
        }
        return false;
	}
	
}
