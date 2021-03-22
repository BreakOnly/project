package com.jrmf.utils.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {

	private static final int CASH_POOL_SIZE = 40;

	/**
	 * 现金业务线程池，程序初始化时new 五条线程，省去了运行过程中创建线程的开销，高效。
	 */
	public static ExecutorService cashThreadPool = Executors.newFixedThreadPool(CASH_POOL_SIZE);
	/**
	 * pdf处理业务线程池
	 */
	public static ExecutorService pdfThreadPool = Executors.newFixedThreadPool(CASH_POOL_SIZE);
	/**
	 * websocket业务线程池，适合执行短期任务，灵活回收池内线程，高效利用。没有可回收的会新建线程。
	 */
	public static ExecutorService webSocketThreadPool = Executors.newCachedThreadPool();

	/**
	 * 子账户相关业务线程池
	 */
	public static ExecutorService subAccountThreadPool = Executors.newCachedThreadPool();

	// 获取线程池剩余可用线程数量
	public int getActiveRema() {
		return CASH_POOL_SIZE - ((ThreadPoolExecutor) cashThreadPool).getActiveCount();
	}

	// 获取往期线程的最大执行时间 单位:毫秒
	public long getKeepAliveTime() {
		return ((ThreadPoolExecutor) cashThreadPool).getKeepAliveTime(TimeUnit.MILLISECONDS);
	}
}
