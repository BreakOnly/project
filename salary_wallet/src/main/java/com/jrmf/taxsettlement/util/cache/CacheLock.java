package com.jrmf.taxsettlement.util.cache;

public interface CacheLock {

	boolean lock();
	
	void unlock();

	void giveBack();
}
