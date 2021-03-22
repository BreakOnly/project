/**
 * 
 */
package com.jrmf.domain;


/**
 * 
* @author chonglulu  
*
 */

public class BankInfo {

	private int id;
	private String bankpic;
	private String bankBgpic;
	private String bankName;
	private String bankBrhName;
	private double maxInvestLimit;
	private double todayMaxInvestLimit;
	private String bankno;
	private String payType;

	private String lianlianOrderlimit;
	private String lianlianDaylimit;
	private String lianlianMonthlimit;
	private String liandongMonthlimit;
	private String liandongOrderlimit;
	private String liandongDaylimit;
	private String chinapayMonthlimit;
	private String chinapayOrderlimit;
	private String chinapayDaylimit;
	private String fuyoupayMonthlimit;
	private String fuyoupayOrderlimit;
	private String fuyoupayDaylimit;
	private String jdpayOrderlimit;
	private String jdpayDaylimit;
	private String jdpayMonthlimit;
	private String shortName;
	private String priority;
	
	public String getBankBgpic() {
		return bankBgpic;
	}

	public void setBankBgpic(String bankBgpic) {
		this.bankBgpic = bankBgpic;
	}

	public String getFuyoupayMonthlimit() {
		return fuyoupayMonthlimit;
	}

	public void setFuyoupayMonthlimit(String fuyoupayMonthlimit) {
		this.fuyoupayMonthlimit = fuyoupayMonthlimit;
	}

	public String getFuyoupayOrderlimit() {
		return fuyoupayOrderlimit;
	}

	public void setFuyoupayOrderlimit(String fuyoupayOrderlimit) {
		this.fuyoupayOrderlimit = fuyoupayOrderlimit;
	}

	public String getFuyoupayDaylimit() {
		return fuyoupayDaylimit;
	}

	public void setFuyoupayDaylimit(String fuyoupayDaylimit) {
		this.fuyoupayDaylimit = fuyoupayDaylimit;
	}

	public String getLianlianOrderlimit() {
		return lianlianOrderlimit;
	}

	public void setLianlianOrderlimit(String lianlianOrderlimit) {
		this.lianlianOrderlimit = lianlianOrderlimit;
	}

	public String getLianlianDaylimit() {
		return lianlianDaylimit;
	}

	public void setLianlianDaylimit(String lianlianDaylimit) {
		this.lianlianDaylimit = lianlianDaylimit;
	}

	public String getLianlianMonthlimit() {
		return lianlianMonthlimit;
	}

	public void setLianlianMonthlimit(String lianlianMonthlimit) {
		this.lianlianMonthlimit = lianlianMonthlimit;
	}

	public String getLiandongMonthlimit() {
		return liandongMonthlimit;
	}

	public void setLiandongMonthlimit(String liandongMonthlimit) {
		this.liandongMonthlimit = liandongMonthlimit;
	}

	public String getLiandongOrderlimit() {
		return liandongOrderlimit;
	}

	public void setLiandongOrderlimit(String liandongOrderlimit) {
		this.liandongOrderlimit = liandongOrderlimit;
	}

	public String getLiandongDaylimit() {
		return liandongDaylimit;
	}

	public void setLiandongDaylimit(String liandongDaylimit) {
		this.liandongDaylimit = liandongDaylimit;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getBankno() {
		return bankno;
	}

	public void setBankno(String bankno) {
		this.bankno = bankno;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the bankName
	 */
	public String getBankName() {
		return bankName;
	}

	/**
	 * @param bankName
	 *            the bankName to set
	 */
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	/**
	 * @return the maxInvestLimit
	 */
	public double getMaxInvestLimit() {
		return maxInvestLimit;
	}

	/**
	 * @param maxInvestLimit
	 *            the maxInvestLimit to set
	 */
	public void setMaxInvestLimit(double maxInvestLimit) {
		this.maxInvestLimit = maxInvestLimit;
	}

	/**
	 * @return the todayMaxInvestLimit
	 */
	public double getTodayMaxInvestLimit() {
		return todayMaxInvestLimit;
	}

	/**
	 * @param todayMaxInvestLimit
	 *            the todayMaxInvestLimit to set
	 */
	public void setTodayMaxInvestLimit(double todayMaxInvestLimit) {
		this.todayMaxInvestLimit = todayMaxInvestLimit;
	}

	/**
	 * @return the bankpic
	 */
	public String getBankpic() {
		return bankpic;
	}

	/**
	 * @param bankpic
	 *            the bankpic to set
	 */
	public void setBankpic(String bankpic) {
		this.bankpic = bankpic;
	}

	/**
	 * @return the chinapayMonthlimit
	 */
	public String getChinapayMonthlimit() {
		return chinapayMonthlimit;
	}

	/**
	 * @param chinapayMonthlimit
	 *            the chinapayMonthlimit to set
	 */
	public void setChinapayMonthlimit(String chinapayMonthlimit) {
		this.chinapayMonthlimit = chinapayMonthlimit;
	}

	/**
	 * @return the chinapayOrderlimit
	 */
	public String getChinapayOrderlimit() {
		return chinapayOrderlimit;
	}

	/**
	 * @param chinapayOrderlimit
	 *            the chinapayOrderlimit to set
	 */
	public void setChinapayOrderlimit(String chinapayOrderlimit) {
		this.chinapayOrderlimit = chinapayOrderlimit;
	}

	/**
	 * @return the chinapayDaylimit
	 */
	public String getChinapayDaylimit() {
		return chinapayDaylimit;
	}

	/**
	 * @param chinapayDaylimit
	 *            the chinapayDaylimit to set
	 */
	public void setChinapayDaylimit(String chinapayDaylimit) {
		this.chinapayDaylimit = chinapayDaylimit;
	}

	public String getBankname() {
		return bankName;
	}

	public String getPaytype() {
		return payType;
	}



	/**
	 * @return the jdpayOrderlimit
	 */
	public String getJdpayOrderlimit() {
		return jdpayOrderlimit;
	}

	/**
	 * @param jdpayOrderlimit the jdpayOrderlimit to set
	 */
	public void setJdpayOrderlimit(String jdpayOrderlimit) {
		this.jdpayOrderlimit = jdpayOrderlimit;
	}

	/**
	 * @return the jdpayDaylimit
	 */
	public String getJdpayDaylimit() {
		return jdpayDaylimit;
	}

	/**
	 * @param jdpayDaylimit the jdpayDaylimit to set
	 */
	public void setJdpayDaylimit(String jdpayDaylimit) {
		this.jdpayDaylimit = jdpayDaylimit;
	}

	/**
	 * @return the jdpayMonthlimit
	 */
	public String getJdpayMonthlimit() {
		return jdpayMonthlimit;
	}

	/**
	 * @param jdpayMonthlimit the jdpayMonthlimit to set
	 */
	public void setJdpayMonthlimit(String jdpayMonthlimit) {
		this.jdpayMonthlimit = jdpayMonthlimit;
	}

	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param shortName the shortName to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getBankBrhName() {
		return bankBrhName;
	}

	public void setBankBrhName(String bankBrhName) {
		this.bankBrhName = bankBrhName;
	}
		

}
