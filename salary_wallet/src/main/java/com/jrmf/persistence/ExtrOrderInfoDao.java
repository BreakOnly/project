package com.jrmf.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.ExtrOrderInfo;

/** 
* @author 种路路 
* @version 创建时间：2018年1月11日 上午10:30:54 
* 类说明 
*/
@Mapper
public interface ExtrOrderInfoDao {

	ExtrOrderInfo getOrderInfoByUserId(@Param("id") int id);

	void changeSignType(@Param("id") int id, @Param("status") int status);

	void addExtrOrderInfo(@Param("userId") int id, @Param("extrOrderId") String channelSerialno, @Param("status") int i, @Param("customkey") String customkey);

	void updateExtrOrderInfo(ExtrOrderInfo orderInfoByUserId);

}
 