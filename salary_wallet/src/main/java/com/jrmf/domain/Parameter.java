package com.jrmf.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * filename：com.jrmf.domain.Parameter.java
 * 
 * @author: zhangyong
 * @time: 2013-10-11下午2:38:34
 */

public class Parameter implements Serializable {
	private static final long serialVersionUID = 8663859221330319130L;
	/**
	 * 主键
	 */
	private Integer id;
	/**
	 * 参数名
	 */
	private String paramName;
	/**
	 * 参数值
	 */
	private String paramValue;
	/**
	 * 参数类型
	 */
	private String paramFlag;
	/**
	 * 操作时间
	 */
	private Date paramDate;

	/**
	 * 状态,1正常，-1失效
	 */
	private Integer paramStatus = 1;
	/**
	 * 来自ip
	 */
	private String fromip;

	private int vailCount = 0;

	private String firstfrom;

	private String firstad;

	private String deviceUUID;

	private String serialId;

	private int isVoice;

	private String bankcardno;

	public String getBankcardno() {
		return bankcardno;
	}

	public void setBankcardno(String bankcardno) {
		this.bankcardno = bankcardno;
	}

	public int getIsVoice() {
		return isVoice;
	}

	public void setIsVoice(int isVoice) {
		this.isVoice = isVoice;
	}

	public int getVailCount() {
		return vailCount;
	}

	public void setVailCount(int vailCount) {
		this.vailCount = vailCount;
	}

	/**
	 * @return the firstfrom
	 */
	public String getFirstfrom() {
		return firstfrom;
	}

	/**
	 * @param firstfrom
	 *            the firstfrom to set
	 */
	public void setFirstfrom(String firstfrom) {
		this.firstfrom = firstfrom;
	}

	/**
	 * @return the firstad
	 */
	public String getFirstad() {
		return firstad;
	}

	/**
	 * @param firstad
	 *            the firstad to set
	 */
	public void setFirstad(String firstad) {
		this.firstad = firstad;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	public String getParamFlag() {
		return paramFlag;
	}

	public void setParamFlag(String paramFlag) {
		this.paramFlag = paramFlag;
	}

	public Date getParamDate() {
		return paramDate;
	}

	public void setParamDate(Date paramDate) {
		this.paramDate = paramDate;
	}

	public String getFromip() {
		return fromip;
	}

	public void setFromip(String fromip) {
		this.fromip = fromip;
	}

	public Integer getParamStatus() {
		return paramStatus;
	}

	public void setParamStatus(Integer paramStatus) {
		this.paramStatus = paramStatus;
	}

	public String getDeviceUUID() {
		return deviceUUID;
	}

	public void setDeviceUUID(String deviceUUID) {
		this.deviceUUID = deviceUUID;
	}

	public String getSerialId() {
		return serialId;
	}

	public void setSerialId(String serialId) {
		this.serialId = serialId;
	}

}
