package com.jrmf.splitorder.thread;

import com.jrmf.splitorder.domain.BaseOrderInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheOriginalData {
    private static final Logger logger = LoggerFactory.getLogger(CacheOriginalData.class);
    public static Map<String, List<BaseOrderInfo>> cacheOriginalData = new ConcurrentHashMap<>();

    public static void clear(String key) {
        cacheOriginalData.remove(key);
        logger.info("cacheOriginalData 清理成功！ key={}", key);
    }

    public static List<BaseOrderInfo> get(String key) {
        List<BaseOrderInfo> baseOrderInfos = cacheOriginalData.get(key);
        return baseOrderInfos;
    }

    /**
     * 拆单数据写入缓存
     *
     * @param data     线程拆单后的数据
     * @param serialNo 缓存的key值
     * @return
     */
    public static boolean put(String serialNo, List<BaseOrderInfo> data) {
        List<BaseOrderInfo> baseOrderInfos = get(serialNo);
        if (baseOrderInfos == null) {
            baseOrderInfos = new ArrayList<>();
            cacheOriginalData.put(serialNo, baseOrderInfos);
        }
        for (BaseOrderInfo datum : data) {
            baseOrderInfos.add(datum);
        }
        cacheOriginalData.put(serialNo, baseOrderInfos);
        return true;
    }
}
