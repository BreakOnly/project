package com.jrmf.api.thread;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/8 13:17
 * Version:1.0
 *
 * @author guoto
 */
public class ApiThread extends Thread {

    public static final String DEFAULT_NAME = "salary_wallet_api_thread";
    private static volatile boolean DEBUGLIFECYCLE = true;
    /**创建过多少线程*/
    private static final AtomicInteger ALIVE = new AtomicInteger();
    /**正在运行的线程数线程*/
    private static final AtomicInteger CREATED = new AtomicInteger();
    private static final Logger logger = LoggerFactory.getLogger(ApiThread.class);

    public ApiThread(Runnable runnable) {
        this(runnable, DEFAULT_NAME);
    }

    public ApiThread(Runnable runnable, String name) {
        super(runnable, name + "-" + CREATED.incrementAndGet());
        setUncaughtExceptionHandler((t, e) -> logger.error("UNCAUGHT in thread " + t.getName(), e));
        logger.debug("alive={},created={}",getAlive(),getCreated());
    }

    @Override
    public void run() {
        boolean debug = DEBUGLIFECYCLE;
        if (debug) {
            logger.debug("Created " + getName());
        }
        try {
            ALIVE.incrementAndGet();
            super.run();
        } finally {
            ALIVE.decrementAndGet();
            if (debug) {
                logger.debug("Created " + getName());
            }
        }
    }

    public static AtomicInteger getAlive() {
        return ALIVE;
    }

    public static AtomicInteger getCreated() {
        return CREATED;
    }
}
