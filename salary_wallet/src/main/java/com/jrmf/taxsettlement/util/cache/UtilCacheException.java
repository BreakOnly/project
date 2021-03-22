package com.jrmf.taxsettlement.util.cache;

public class UtilCacheException extends RuntimeException {

	public UtilCacheException(Exception e) {
		super(e);
	}

	public UtilCacheException(String msg) {
		super(msg);
	}

}
