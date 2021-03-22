package com.jrmf.taxsettlement.util.cache;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

class RedisLock implements CacheLock {

	private static Logger logger = LoggerFactory.getLogger(RedisLock.class);

	private Jedis jedis;

	/**
	 * Lock key path.
	 */
	private String lockKey;

	/**
	 * 锁超时时间，防止线程在入锁以后，无限的执行等待
	 */
	private int expireMsecs = 60 * 1000;

	/**
	 * 锁等待时间，防止线程饥饿
	 */
	private int timeoutMsecs = 10 * 1000;

	private volatile boolean locked = false;

	/**
	 * Detailed constructor with default acquire timeout 10000 msecs and lock
	 * expiration of 60000 msecs.
	 *
	 * @param lockKey
	 *            lock key (ex. account:1, ...)
	 */
	public RedisLock(Jedis jedis, String lockKey) {
		this.jedis = jedis;
		this.lockKey = lockKey + "_lock";
	}

	/**
	 * Detailed constructor with default lock expiration of 60000 msecs.
	 *
	 */
	public RedisLock(Jedis jedis, String lockKey, int timeoutMsecs) {
		this(jedis, lockKey);
		this.timeoutMsecs = timeoutMsecs;
	}

	/**
	 * Detailed constructor.
	 *
	 */
	public RedisLock(Jedis jedis, String lockKey, int timeoutMsecs, int expireMsecs) {
		this(jedis, lockKey, timeoutMsecs);
		this.expireMsecs = expireMsecs;
	}

	/**
	 * @return lock key
	 */
	public String getLockKey() {
		return lockKey;
	}

	/**
	 * 获得 lock. 实现思路: 主要是使用了redis 的setnx命令,缓存了锁. reids缓存的key是锁的key,所有的共享,
	 * value是锁的到期时间(注意:这里把过期时间放在value了,没有时间上设置其超时时间) 执行过程:
	 * 1.通过setnx尝试设置某个key的值,成功(当前没有这个锁)则返回,成功获得锁
	 * 2.锁已经存在则获取锁的到期时间,和当前时间比较,超时的话,则设置新的值
	 *
	 * @return true if lock is acquired, false acquire timeouted
	 * @throws InterruptedException
	 *             in case of thread interruption
	 */
	public synchronized boolean lock() {
		
		Random random = new Random(System.currentTimeMillis());
		
		int timeout = timeoutMsecs;
		while (timeout >= 0) {
			long expires = System.currentTimeMillis() + expireMsecs + 1;
			String expiresStr = String.valueOf(expires); // 锁到期时间
			if (jedis.setnx(lockKey, expiresStr) == 1L) {
				// lock acquired
				locked = true;
				return true;
			}

			String currentValueStr = jedis.get(lockKey);
			if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {
				String oldValueStr = jedis.getSet(lockKey, expiresStr);
				if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
					locked = true;
					return true;
				}
			}
			
			int randomSleepTime = random.nextInt(100);
			timeout -= randomSleepTime;
			try {
				Thread.sleep(randomSleepTime);
			} catch (InterruptedException e) {
				logger.warn("thread awake up for interrupted", e);
			}

		}
		return false;
	}

	/**
	 * Acqurired lock release.
	 */
	public synchronized void unlock() {
		if (locked) {
			jedis.del(lockKey);
			locked = false;
		}
	}

	@Override
	public void giveBack() {
		jedis.close();
	}
}
