package com.jrmf.payment.entity;

import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;

public interface Payment<T, R, O> {

	/**
	 * 获取请求参数模板
	 *
	 * @return
	 */
	public T getTransferTemple(UserCommission userCommission) throws Exception;

	/**
	 * 支付
	 *
	 */
	public abstract PaymentReturn<O> paymentTransfer(UserCommission userCommission);


	/**
	 * 获取请求结果
	 * @return
	 */
	public abstract PaymentReturn<O> getTransferResult(R result);

	/**
	 * 获取请求结果
	 * @return
	 */
	public abstract PaymentReturn<TransStatus> queryTransferResult(String orderNo);


	/**
	 * 查询余额
	 * @return
	 */
	public abstract PaymentReturn<O> queryBalanceResult(String type);


	/**
	 * 联动交易
	 * @exception
	 */
	PaymentReturn<String> linkageTransfer(LinkageTransferRecord transferRecord);

	/**
	 * 查询交易明细
	 * @param queryParams
	 * @return
	 */
	PaymentReturn<LinkageTransHistoryPage> queryTransHistoryPage(LinkageQueryTranHistory queryParams);

}
