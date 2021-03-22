package com.jrmf.utils;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: YJY
 * @date: 2020/8/28 11:00
 * @description: 公共线程池
 */
public class ThreadPoolUtils {


  /**
  * @Description 核心线程
  **/
  private final static int THREAD_POOL_CORE_SIZE = 10;
  /**
   * @Description 最大线程
   **/
  private final static int THREAD_POOL_MAX_SIZE = 20;
  /**
   * @Description 存活时间
   **/
  private final static int KEEP_ALIVE_TIME_SECOND = 3;
  /**
   * @Description 等待队列
   **/
  private final static int ARRAY_BLOCK_QUEUE = 6;
  /**
   * @Description 线程池
   **/
  private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
      THREAD_POOL_CORE_SIZE, THREAD_POOL_MAX_SIZE, KEEP_ALIVE_TIME_SECOND,
      TimeUnit.SECONDS, new ArrayBlockingQueue<>(ARRAY_BLOCK_QUEUE));

  /**
   * @Author YJY
   * @Description 返回线程
   * @Date  2020/8/14
   * @Param []
   * @return java.util.concurrent.ThreadPoolExecutor
   **/
  public static ThreadPoolExecutor getThread() {

    return threadPoolExecutor;

  }

}
