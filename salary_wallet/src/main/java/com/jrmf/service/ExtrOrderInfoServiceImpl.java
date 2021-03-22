package com.jrmf.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jrmf.domain.ExtrOrderInfo;
import com.jrmf.persistence.ExtrOrderInfoDao;

/** 
* @author 种路路 
* @version 创建时间：2018年1月11日 上午10:21:22 
* 类说明 
*/
@Service("extrOrderInfoService")
public class ExtrOrderInfoServiceImpl implements ExtrOrderInfoService {
	@Autowired
	private ExtrOrderInfoDao extrOrderInfoDao;
	@Override
	public ExtrOrderInfo getOrderInfoByUserId(int id) {
		// TODO Auto-generated method stub
		return extrOrderInfoDao.getOrderInfoByUserId(id);
	}
	@Override
	public void changeSignType(int id, int status) {
		// TODO Auto-generated method stub
		extrOrderInfoDao.changeSignType(id,status);
	}
	@Override
	public void addExtrOrderInfo(int id, String channelSerialno, int i,String customkey) {
		// TODO Auto-generated method stub
		extrOrderInfoDao.addExtrOrderInfo( id, channelSerialno, i,customkey);
	}
	@Override
	public void updateExtrOrderInfo(ExtrOrderInfo orderInfoByUserId) {
		// TODO Auto-generated method stub
		extrOrderInfoDao.updateExtrOrderInfo( orderInfoByUserId);
	}

}
 