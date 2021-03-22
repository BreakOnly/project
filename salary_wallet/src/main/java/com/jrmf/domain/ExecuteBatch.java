package com.jrmf.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrmf.service.AlipayService;
import com.jrmf.service.UserCommissionService;

import net.sf.json.JSONObject;

public class ExecuteBatch implements Runnable {

	private Logger logger = LoggerFactory.getLogger(ExecuteBatch.class);

	private CyclicBarrier cb;

	private List<Map<String, Object>> param;

	private AlipayService alipayServiceImpl;

	private UserCommissionService commissionService;

	public ExecuteBatch(CyclicBarrier cb, AlipayService alipayServiceImpl, List<Map<String, Object>> param,
			UserCommissionService commissionService) {
		super();
		this.alipayServiceImpl = alipayServiceImpl;
		this.param = param;
		this.commissionService = commissionService;
		this.cb = cb;
	}

	@Override
	public void run() {
//		try {
//			logger.info("《《《《《《《支付宝资金下发开始》》》》》》》");
//			for (Map<String, Object> map2 : param) {
//				UserCommission user = commissionService.getUserCommission((String) map2.get("out_biz_no"));
//				String result = alipayServiceImpl.singleTransferAccounts(map2);
//				if (StringUtils.isEmpty(result)) {
//					// 未收到返回视为订单失败。
//					logger.info("单号："+map2.get("out_biz_no")+"接口未返回！");
//					user.setStatus(2);
//					user.setStatusDesc("接口未返回！");
//					commissionService.updateUserCommission(user);
//				}
//				JSONObject jsonObject = null;
//				JSONObject jsStr = null;
//				try {
//					jsStr = JSONObject.fromObject(result);
//					jsonObject = JSONObject.fromObject(jsStr.get("alipay_fund_trans_toaccount_transfer_response"));
//				} catch (Exception e) {
//					logger.error("", e);
//					logger.info(e.getMessage(), e);
//				}
//				if(jsonObject != null){
//					logger.info(jsonObject.toString());
//					if ("10000".equals(jsonObject.get("code"))) {
//						// 成功
//						user.setStatus(1);
//						user.setStatusDesc("发放成功");
//						commissionService.updateUserCommission(user);
//					} else {
//						String sub_msg = jsonObject.get("sub_msg").toString();
//						user.setStatus(2);
//						user.setStatusDesc(sub_msg);
//						commissionService.updateUserCommission(user);
//					}
//				}
//			}
//			cb.await();
//		} catch (InterruptedException e) {
//			logger.error(e.getMessage(),e);
//		} catch (BrokenBarrierException e) {
//			logger.error(e.getMessage(),e);
//		}
	}

}
