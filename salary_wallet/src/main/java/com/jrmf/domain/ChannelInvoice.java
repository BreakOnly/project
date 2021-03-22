package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author zhangzehui
* @time 2017-12-16 
* @description: 渠道发票记录表
*/
public class ChannelInvoice implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private int id;
	private String createtime;
	private String amount;//开票金额
	private String orderno;//发票申请编号
	private String customkey;//渠道key
	private String updatetime;//操作时间
	private String reCustomkey;//出票公司key
	private String invoiceNo;//纳税人识别号
	private String invoiceNum;//发票票号
	private int status;// 0 申请成功,待开票   1 已寄出   2 被驳回 3 发票已开，待邮寄
	private String operatorName;//操作人
	private String remark;//备注信息
	private String receiverName;//收件人姓名
	private String address;//邮寄地址以及联系方式
	private String bankMessage;//开户行及账号
	private int num;//发票数量
	private int invoiceType;//发票类型 1 ：企业管理咨询服务费
	private int taxpayerType;//纳税人类型2 一般纳税人 1小规模纳税人
	private String courierCompany;//快递公司
	private String courierNo;//快递订单号
	
	/**
	 * 冗余字段
	 * @return
	 */
	private String bankNameAndBankNo;//开户行及账号
	private String openCompany;
	private String invoiceCompanyName;
	private String mobileNo;
	
	public String getBankNameAndBankNo() {
		return bankNameAndBankNo;
	}
	public void setBankNameAndBankNo(String bankNameAndBankNo) {
		this.bankNameAndBankNo = bankNameAndBankNo;
	}
	public String getCourierNo() {
		return courierNo;
	}
	public void setCourierNo(String courierNo) {
		this.courierNo = courierNo;
	}
	public String getCourierCompany() {
		return courierCompany;
	}
	public void setCourierCompany(String courierCompany) {
		this.courierCompany = courierCompany;
	}
	public String getMobileNo() {
		return mobileNo;
	}
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	public String getOpenCompany() {
		return openCompany;
	}
	public void setOpenCompany(String openCompany) {
		this.openCompany = openCompany;
	}
	public String getInvoiceCompanyName() {
		return invoiceCompanyName;
	}
	public void setInvoiceCompanyName(String invoiceCompanyName) {
		this.invoiceCompanyName = invoiceCompanyName;
	}
	public int getTaxpayerType() {
		return taxpayerType;
	}
	public void setTaxpayerType(int taxpayerType) {
		this.taxpayerType = taxpayerType;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCreatetime() {
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getOrderno() {
		return orderno;
	}
	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}
	public String getCustomkey() {
		return customkey;
	}
	public void setCustomkey(String customkey) {
		this.customkey = customkey;
	}
	public String getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}
	public String getInvoiceNo() {
		return invoiceNo;
	}
	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getOperatorName() {
		return operatorName;
	}
	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getReceiverName() {
		return receiverName;
	}
	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getInvoiceNum() {
		return invoiceNum;
	}
	public void setInvoiceNum(String invoiceNum) {
		this.invoiceNum = invoiceNum;
	}
	public String getReCustomkey() {
		return reCustomkey;
	}
	public void setReCustomkey(String reCustomkey) {
		this.reCustomkey = reCustomkey;
	}
	public int getInvoiceType() {
		return invoiceType;
	}
	public void setInvoiceType(int invoiceType) {
		this.invoiceType = invoiceType;
	}
	public String getBankMessage() {
		return bankMessage;
	}
	public void setBankMessage(String bankMessage) {
		this.bankMessage = bankMessage;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	@Override
	public String toString() {
		return "ChannelInvoice [id=" + id + ", createtime=" + createtime + ", amount=" + amount + ", orderno=" + orderno
				+ ", customkey=" + customkey + ", updatetime=" + updatetime + ", reCustomkey=" + reCustomkey
				+ ", invoiceNo=" + invoiceNo + ", invoiceNum=" + invoiceNum + ", status=" + status + ", operatorName="
				+ operatorName + ", remark=" + remark + ", receiverName=" + receiverName + ", address=" + address
				+ ", bankMessage=" + bankMessage + ", num=" + num + ", invoiceType=" + invoiceType + ", taxpayerType="
				+ taxpayerType + ", courierCompany=" + courierCompany + ", courierNo=" + courierNo
				+ ", bankNameAndBankNo=" + bankNameAndBankNo + ", openCompany=" + openCompany + ", invoiceCompanyName="
				+ invoiceCompanyName + ", mobileNo=" + mobileNo + "]";
	}
	
}
 