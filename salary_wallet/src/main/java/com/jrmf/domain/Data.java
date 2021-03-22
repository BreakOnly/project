package com.jrmf.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Data {

	// 总数
	public long totalNum;
	// 设置全局缓存区存放每次导入请求的数据
//	public static final ConcurrentHashMap<String, Data> cacahmap = new ConcurrentHashMap<>();
	// 设置全局缓存区存放每次导入请求的数据
//	public static final ConcurrentHashMap<String, Boolean> cacahflag = new ConcurrentHashMap<>();
	// 实例变量，储存本次请求的导入数据
//	public CopyOnWriteArrayList<Map<String, Object>> data_map = new CopyOnWriteArrayList<>();

	// 获取处理进度和数据
//	public static void initDataMap(String batchId, int totalNum) {
//		Data data = new Data();
//		data.setTotalNum(totalNum);
//		Data.setData(batchId, data);
//		Data.setFlag(batchId);
//	}

	// 获取当前结果
//	public static Map<String, Object> returnResult(String batchId) {
//		Map<String, Object> result = new HashMap<>();
//		Data data = Data.cacahmap.containsKey(batchId) == true ? Data.getData(batchId) : null;
//		result.put("total", data != null ? data.getTotalNum() : 0);// 总条数
//		result.put("current", data != null ? data.data_map.size() : 0);// 当前执行条数
//		result.put("list", data != null ? data.data_map : null);// 结果集
//		result.put("flag", Data.cacahflag.containsKey(batchId) ? Data.cacahflag.get(batchId) : false);// 是否执行完毕,如果key不存在可能时没有经过导入或者已被删除。
//		return result;
//	}

//	public long getTotalNum() {
//		return totalNum;
//	}
//
//	public void setTotalNum(long totalNum) {
//		this.totalNum = totalNum;
//	}

//	public static void setData(String key, Data value) {
//		cacahmap.put(key, value);
//	}

//	public static Data getData(String key) {
//		return cacahmap.get(key);
//	}
//
//	public static void deleteData(String key) {
//		System.out.println("key:" + key + ":: 删除之。");
//		cacahmap.remove(key);
//		String result = cacahmap.containsKey(key) == false ? "YES" : "NO";
//		System.out.println("删除是否成功：" + result);
//	}
//
//	public static void setFlag(String key) {
//		cacahflag.put(key, true);
//	}
//
//	public static void complete(String key) {
//		cacahflag.put(key, false);
//	}
//
//	public static Boolean getFlag(String key) {
//		return cacahflag.get(key);
//	}
//
//	public static void deleteFlag(String key) {
//		cacahflag.remove(key);
//	}

}
