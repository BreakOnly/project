package com.jrmf.api.thread;

import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * 用途：动态的为每个批次任务提供一个适配的线程池，在任务结束后会将线程池也关闭。
 * 作者：郭桐宁
 * 时间：2018/12/8 11:46
 * Version:1.0
 *
 * @author guoto
 */
@Component
public class TaskThreadPoolFactory {
    private static final long RATIO_OF_BATCH_TIME = 75;
    private static final double TARGET_CPU_UTILIZATION = 0.5;
    private static final double RATIO_OF_WAIT_TIME_AND_RUNTIME = 1.2;


}
