package com.jrmf.splitorder.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplitOrderThreadPool {
    private static final int CASH_POOL_SIZE = 40;

    public static ExecutorService cashThreadPool = Executors.newFixedThreadPool(CASH_POOL_SIZE);

}
