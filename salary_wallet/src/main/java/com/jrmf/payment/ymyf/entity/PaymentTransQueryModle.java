package com.jrmf.payment.ymyf.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 商户代付查询接口实体类
 * @author Admin
 *
 */
public class PaymentTransQueryModle implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private String merId;//商户编号
	 
    private String merBatchId;// 商户批次号
    
	private List<QueryItems> queryItems;//代付明细数据集合
	
	public static class QueryItems{

	    // 商户订单号
	    private String merOrderId;
	    
	    private String payItemId;//订单流水号
	    
	    private int state = 0;//交易状态 0：未处理 1：处理中 2：提现中 3：成功 4：失败
	    
	    private String resCode;
	    
	    private String resMsg;

		public String getMerOrderId() {
			return merOrderId;
		}

		public void setMerOrderId(String merOrderId) {
			this.merOrderId = merOrderId;
		}

		public String getPayItemId() {
			return payItemId;
		}

		public void setPayItemId(String payItemId) {
			this.payItemId = payItemId;
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}
		
		public String getResCode() {
			return resCode;
		}

		public void setResCode(String resCode) {
			this.resCode = resCode;
		}

		public String getResMsg() {
			return resMsg;
		}

		public void setResMsg(String resMsg) {
			this.resMsg = resMsg;
		}
		
	}

	public String getMerId() {
		return merId;
	}

	public void setMerId(String merId) {
		this.merId = merId;
	}
	
	public List<QueryItems> getQueryItems() {
		return queryItems;
	}

	public void setQueryItems(List<QueryItems> queryItems) {
		this.queryItems = queryItems;
	}

	public String getMerBatchId() {
		return merBatchId;
	}

	public void setMerBatchId(String merBatchId) {
		this.merBatchId = merBatchId;
	}
	

}
