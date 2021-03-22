package com.jrmf.controller;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class test {

    static class CallableDemo implements Callable<Map<String, Object>> {
        private String batchId;

        public CallableDemo(String batchId) {
            this.batchId = batchId;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            System.out.println(batchId);
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("1", 1);
            hashMap.put("2", 2);
            hashMap.put("3", 3);
            TimeUnit.SECONDS.sleep(3);
            return hashMap;
        }
    }

    public static void main(String[] args) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
		System.out.println("当前时间：" + simpleDateFormat.format(new Date()));
		try {
			FutureTask<Map<String, Object>> getData = new FutureTask<>(new CallableDemo("1"));
			Executor executor = Executors.newSingleThreadExecutor();
			executor.execute(getData);
			Map<String, Object> map = getData.get();
			String data = String.format("data=%s", JSONObject.toJSONString(map));
			System.out.println(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("当前时间：" + simpleDateFormat.format(new Date()));
    }
}