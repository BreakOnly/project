package com.jrmf.utils;

import com.jrmf.taxsettlement.util.cache.UtilCacheManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2019/1/24 15:53
 * Version:1.0
 */
@Component
public class OrderNoUtil {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    @Autowired
    private UtilCacheManager utilCacheManager;
    public String getChannelSerialno(){
        String newDate = sdf.format(new Date());
        Long orderNO = utilCacheManager.increase(newDate);
        if(orderNO.equals(0)){
            utilCacheManager.put(newDate,0,87000);
            orderNO = utilCacheManager.increase(newDate);
        }
        StringBuilder builder = new StringBuilder();
        if(orderNO.toString().length() < 8){
            for (int i = 0; i < (8-orderNO.toString().length()); i++) {
                builder.append("0");
            }
        }
        String string = builder.append(orderNO.toString()).toString();
        return newDate+string;
    }
    public Object reSetOrderNo(){
        String newDate = sdf.format(new Date());
        return utilCacheManager.remove(newDate);
    }

    public String getReceiptNo() {
        String newDate = sdf.format(new Date())+"#";
        Long orderNO = utilCacheManager.increase(newDate);
        if(orderNO.equals(0)){
            utilCacheManager.put(newDate,0,87000);
            orderNO = utilCacheManager.increase(newDate);
        }
        StringBuilder builder = new StringBuilder();
        if(orderNO.toString().length() < 6){
            for (int i = 0; i < (6-orderNO.toString().length()); i++) {
                builder.append("0");
            }
        }
        return builder.append(orderNO.toString()).toString();
    }
    
	public static String getOrderNo() {
		Date date=new Date();  
		DateFormat format = new SimpleDateFormat("yyyyMMdd");  
		String time = format.format(date);  
		int hashCodeV = UUID.randomUUID().toString().hashCode();  
		if (hashCodeV < 0) {//有可能是负数  
			hashCodeV = -hashCodeV;  
		}  
		//订单号
		String order_no = time+String.format("%011d", hashCodeV);
		return order_no;
	}
}
