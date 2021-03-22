package com.jrmf.taxsettlement.util.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

public class RedisCacheManager implements UtilCacheManager {

	private static final Logger logger = LoggerFactory.getLogger(RedisCacheManager.class);

	private static final String SUCC_RET = "OK";
	
	private final JedisPool jedisPool;
	
	private static String KEY_PRE = "REDIS_LOCK_";
    private static final String SET_SUCCESS  = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final Long RELEASE_SUCCESS = 1L;
    private DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");

	public RedisCacheManager(JedisPoolConfig config, String ip, int port, int timeout) {
		this.jedisPool = new JedisPool(config, ip, port, timeout);
	}
	
	public RedisCacheManager(JedisPoolConfig config, String ip, int port, int timeout, String password) {
		this.jedisPool = new JedisPool(config, ip, port, timeout, password);
	}

	private byte[] toByteArrayKey(String key) {
		return ((String) key).getBytes(Charset.forName("utf-8"));
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
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				return toObj(jedis.get(toByteArrayKey(key)));
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for getting cache data");
		}
	}

	@Override
	public long getCacheLife(String key) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				return jedis.ttl(toByteArrayKey(key));
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for query life of cache data");
		}
	}
	
	@Override
	public Object put(String key, Object value, int cacheLife) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				byte[] byteArrayKey = toByteArrayKey(key);
				try {
					@SuppressWarnings("unchecked")
					Object previous = toObj(jedis.get(byteArrayKey));
					byte[] bytesKey = toByteArrayKey(key);
					byte[] bytesValue = toByteArrayObj(value);
					String jedisRetCode = jedis.set(bytesKey, bytesValue);

					if (cacheLife > 0) {
						jedis.expire(bytesKey, cacheLife);
					}

					if (!SUCC_RET.equals(jedisRetCode)) {
						logger.error("fail to set value of key[{}] to jedis with retcode:[{}]", key.toString(),
								jedisRetCode);
					}
					return previous;
				} catch (Exception e) {
					throw new UtilCacheException(e);
				}

			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for putting cache data");
		}
	}

	@Override
	public boolean putIfAbsent(String key, Object value, int cacheLife) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				byte[] byteArrayKey = toByteArrayKey(key);
				try {
					@SuppressWarnings("unchecked")
					Object previous = toObj(jedis.get(byteArrayKey));
					byte[] bytesKey = toByteArrayKey(key);
					byte[] bytesValue = toByteArrayObj(value);
					boolean setOk = jedis.setnx(bytesKey, bytesValue) == 1;

					if (setOk && cacheLife > 0) {
						jedis.expire(bytesKey, cacheLife);
					}

					if (!setOk) {
						logger.debug("fail to set value of key[{}] to jedis", key.toString());
					}
					return setOk;
				} catch (Exception e) {
					throw new UtilCacheException(e);
				}

			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for putting cache data");
		}
	}
	
	@Override
	public Object remove(String key) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			byte[] byteArrayKey = toByteArrayKey(key);
			try {
				@SuppressWarnings("unchecked")
				Object previous = toObj(jedis.get(byteArrayKey));
				jedis.del(byteArrayKey);
				return previous;
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for removing cache data");
		}
	}

	@Override
	public boolean exists(String key) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			byte[] byteArrayKey = toByteArrayKey(key);
			try {
				return jedis.exists(byteArrayKey);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for getting cache data");
		}
	}

	@Override
	public void setMap(String key, Map<String, String> map) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				String jedisRetCode = jedis.hmset(key, map);
				
				if (!SUCC_RET.equals(jedisRetCode)) {
					logger.error("fail to set map of key[{}] to jedis with retcode:[{}]", key.toString(),
							jedisRetCode);
				}
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for setting map cache data");
		}
	}
	
	@Override
	public void setMap(String key, Map<String, String> map, int cacheLife) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				String jedisRetCode = jedis.hmset(key, map);
				
				if (cacheLife > 0) {
					jedis.expire(key, cacheLife);
				}

				if (!SUCC_RET.equals(jedisRetCode)) {
					logger.error("fail to set map of key[{}] to jedis with retcode:[{}]", key.toString(),
							jedisRetCode);
				}
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for setting map cache data");
		}		
	}

	@Override
	public Map<String, String> getMap(String key) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				return jedis.hgetAll(key);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for getting map cache data");
		}
	}

	@Override
	public void setMapValue(String key, String fieldName, String fieldValue) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				jedis.hset(key, fieldName, fieldValue);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for setting map cache field data");
		}
	}

	@Override
	public String getMapValue(String key, String fieldName) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				return jedis.hget(key, fieldName);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for getting map cache field data");
		}
	}

	@Override
	public boolean existsMapField(String key, String fieldName) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				return jedis.hexists(key, fieldName);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for exist map cache field data");
		}
	}

	@Override
	public void setList(String key, List<String> list) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				jedis.lpush(key, list.toArray(new String[list.size()]));
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for setting list cache data");
		}
	}

	@Override
	public void setList(String key, List<String> list, int cacheLife) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				jedis.lpush(key, list.toArray(new String[list.size()]));

				if (cacheLife > 0) {
					jedis.expire(key, cacheLife);
				}
				
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for setting list cache data");
		}		
	}


	@Override
	public int getListSize(String key) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				return jedis.llen(key).intValue();
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for sizing list cache data");
		}
	}

	@Override
	public List<String> getList(String key) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				return jedis.lrange(key, 0, -1);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for getting list cache data");
		}
	}

	@Override
	public String leftGetList(String key) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				return jedis.lpop(key);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for lpop list cache data");
		}
	}

	@Override
	public void addSet(String key, Set<String> setValues) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				jedis.sadd(key, setValues.toArray(new String[setValues.size()]));
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for adding set cache data");
		}
	}
	
	@Override
	public void addSet(String key, Set<String> setValues, int cacheLife) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				jedis.sadd(key, setValues.toArray(new String[setValues.size()]));

				if (cacheLife > 0) {
					jedis.expire(key, cacheLife);
				}

			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for adding set cache data");
		}		
	}

	@Override
	public boolean existsMember(String key, String member) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				return jedis.sismember(key, member);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for containing member of set cache data");
		}
	}


	@Override
	public Set<String> keys(String pattern) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				Set<String> keys = new HashSet<String>();
				for (byte[] byteKey : jedis.keys(toByteArrayKey(pattern == null || "".equals(pattern) ? "*" : pattern))) {
					keys.add(new String(byteKey, Charset.forName("utf-8")));
				}
				return keys;
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for getting cache data");
		}
	}


	@Override
	public Long increase(String key) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			byte[] byteArrayKey = toByteArrayKey(key);
			try {
				return jedis.incr(byteArrayKey);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for removing cache data");
		}
	}

	@Override
	public Long decrease(String key) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			byte[] byteArrayKey = toByteArrayKey(key);
			try {
				return jedis.decr(byteArrayKey);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for removing cache data");
		}
	}

	@Override
	public Long increaseBy(String key, long addNum) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			byte[] byteArrayKey = toByteArrayKey(key);
			try {
				return jedis.incrBy(byteArrayKey, addNum);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for removing cache data");
		}
	}

	@Override
	public Long decreaseBy(String key, long substractNum) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			byte[] byteArrayKey = toByteArrayKey(key);
			try {
				return jedis.decrBy(byteArrayKey, substractNum);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for removing cache data");
		}
	}

	@Override
	public Long changeMapValueBy(String key, String fieldName, long changeNum) {
		Jedis jedis = jedisPool.getResource();
		if (jedis != null) {
			try {
				return jedis.hincrBy(key, fieldName, changeNum);
			} catch (Exception e) {
				throw new UtilCacheException(e);
			} finally {
				jedis.close();
			}
		} else {
			throw new UtilCacheException("cannot get jedis connection for getting map cache field data");
		}
	}
	
    @Override
    /**
     * 加锁
     * @param locaName  锁的key
     * @param acquireTimeout  获取超时时间
     * @param timeout   锁的超时时间
     * @return 锁标识
     */
    public boolean lockWithTimeout(String locaName,long acquireTimeout, long timeout) {
        Jedis conn = null;
        try {
            // 获取连接
            conn = jedisPool.getResource();
            // 随机生成一个value
            String identifier = UUID.randomUUID().toString();
            // 锁名，即key值
            String lockKey = "lock:" + locaName;
            // 超时时间，上锁后超过此时间则自动释放锁
            int lockExpire = (int)(timeout / 1000);

            // 获取锁的超时时间，超过这个时间则放弃获取锁
            long end = System.currentTimeMillis() + acquireTimeout;
            while (System.currentTimeMillis() < end) {
                if (conn.setnx(lockKey, identifier) == 1) {
                    conn.expire(lockKey, lockExpire);
                    // 返回value值，用于释放锁时间确认
                    return true;
                }
                // 返回-1代表key没有设置超时时间，为key设置一个超时时间
                if (conn.ttl(lockKey) == -1) {
                    conn.expire(lockKey, lockExpire);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (JedisException e) {
					logger.error(e.getMessage(),e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return false;
    }
    
    /**
     * 释放锁
     * @param lockName 锁的key
     * @param lock    释放锁的标识
     * @return
     */
    public boolean releaseLock(String lockName, boolean lock) {
        Jedis conn = null;
        String lockKey = "lock:" + lockName;
        boolean retFlag = false;
        try {
            conn = jedisPool.getResource();
            while (true) {
                // 监视lock，准备开始事务
                conn.watch(lockKey);
                // 通过前面返回的value值判断是不是该锁，若是该锁，则删除，释放锁
                if (lock) {
                    Transaction transaction = conn.multi();
                    transaction.del(lockKey);
                    List<Object> results = transaction.exec();
                    if (results == null) {
                        continue;
                    }
                    retFlag = true;
                }
                conn.unwatch();
                break;
            }
        } catch (JedisException e) {
					logger.error(e.getMessage(),e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return retFlag;
    }
}
