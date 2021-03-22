package com.jrmf.api.thread;

import java.util.concurrent.ThreadFactory;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/8 13:43
 * Version:1.0
 * @author guoto
 */
public class ApiThreadFactory implements ThreadFactory {

    private final String poolName;

    public ApiThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new ApiThread(r,poolName);
    }

}
