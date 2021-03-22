package com.jrmf.payment.openapi.model.request.deliver;

import java.math.BigDecimal;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.deliver.PayUnifiedOrderResult;


/**
 * 单笔代发请求
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2018年8月6日
 */
public class PayUnifiedOrderParam  implements IBaseParam<PayUnifiedOrderResult>{

	/**
	 * 落地服务商id
	 */
	private Long serviceCompanyId;
	/**
	 * 批次号
	 */
	private String batchNo;
	/**
	 * 调用方订单号
	 */
	private String outOrderNo;
	/**
	 * 订单总金额(单位元)（发放的金额）
	 */
	private BigDecimal amount;
	/**
	 * 收款方账号(如果是第三方支付，填写第三方支付账号)
	 */
	private String accountNo;
	/**
	 * 收款方账号名称
	 */
	private String accountName;
	/**
	 * 收款方账号开户行名称(包含第三方支付)
	 */
	private String bank;
	/**
	 * 收款方账号开户支行
	 */
	private String depositBank;
	/**
	 * 身份证
	 */
	private String idCard;
	/**
	 * 手机
	 */
	private String phone;
	/**
	 * 应发金额
	 */
	private BigDecimal shouldAmount;
	/**
	 * 个税
	 */
	private BigDecimal personalIncomeTax;
	/**
	 * 增值税
	 */
	private BigDecimal addedValueTax;
	/**
	 * 附加税
	 */
	private BigDecimal additionalTax;
	
	/**
	 * 服务费
	 */
	private BigDecimal serviceFee;
	/**
	 * 总费用
	 */
	private BigDecimal totalFee;
	
	/**
	 * 附加数据，在查询和异步通知接口中原样返回，可作为自定义参数使用(长度64)
	 */
	private String attach;
	
	private String memo;

	public Long getServiceCompanyId() {
		return serviceCompanyId;
	}

	public void setServiceCompanyId(Long serviceCompanyId) {
		this.serviceCompanyId = serviceCompanyId;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getOutOrderNo() {
		return outOrderNo;
	}

	public void setOutOrderNo(String outOrderNo) {
		this.outOrderNo = outOrderNo;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getDepositBank() {
		return depositBank;
	}

	public void setDepositBank(String depositBank) {
		this.depositBank = depositBank;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public BigDecimal getShouldAmount() {
		return shouldAmount;
	}

	public void setShouldAmount(BigDecimal shouldAmount) {
		this.shouldAmount = shouldAmount;
	}

	public BigDecimal getPersonalIncomeTax() {
		return personalIncomeTax;
	}

	public void setPersonalIncomeTax(BigDecimal personalIncomeTax) {
		this.personalIncomeTax = personalIncomeTax;
	}

	public BigDecimal getAddedValueTax() {
		return addedValueTax;
	}

	public void setAddedValueTax(BigDecimal addedValueTax) {
		this.addedValueTax = addedValueTax;
	}

	public BigDecimal getAdditionalTax() {
		return additionalTax;
	}

	public void setAdditionalTax(BigDecimal additionalTax) {
		this.additionalTax = additionalTax;
	}

	public BigDecimal getServiceFee() {
		return serviceFee;
	}

	public void setServiceFee(BigDecimal serviceFee) {
		this.serviceFee = serviceFee;
	}

	public BigDecimal getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(BigDecimal totalFee) {
		this.totalFee = totalFee;
	}

	public String getAttach() {
		return attach;
	}

	public void setAttach(String attach) {
		this.attach = attach;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	@Override
	public String requestURI() {
		return "/deliver/dlvopenapi/api/app/pay/unified-order";
	}

	@Override
	public String methodName() {
		return "ayg.salary.pay";
	}

	@Override
	public String version() {
		return "1.0";
	}

	@Override
	public Class<?> respDataClass() {
		return PayUnifiedOrderResult.class;
	}

}
