package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author zh
* @version 创建时间：2019年2月28日 
*/
public class CustomPaymentTotalAmount implements Serializable{

	private static final long serialVersionUID = 8028470539645251919L;
	
	private int id;
	private String companyId;
	private String originalId;
	private String identityNo;
	private Integer lastDayTotal;
	private Integer todayTotal;
	private Integer lastQuarterTotal;
	private Integer currentQuarterTotal;
	private Integer lastMonthTotal;
	private Integer currentMonthTotal;
	private String updateTime;
	private String createTime;
	
	private String customName;
	private String companyName;
	
	private String lastDayTotalStr;
	private String todayTotalStr;
	private String lastMonthTotalStr;
	private String currentMonthTotalStr;
	private String lastQuarterTotalStr;
	private String currentQuarterTotalStr;

	public String getLastDayTotalStr() {
		return lastDayTotalStr;
	}
	public void setLastDayTotalStr(String lastDayTotalStr) {
		this.lastDayTotalStr = lastDayTotalStr;
	}
	public String getTodayTotalStr() {
		return todayTotalStr;
	}
	public void setTodayTotalStr(String todayTotalStr) {
		this.todayTotalStr = todayTotalStr;
	}
	public String getLastMonthTotalStr() {
		return lastMonthTotalStr;
	}
	public void setLastMonthTotalStr(String lastMonthTotalStr) {
		this.lastMonthTotalStr = lastMonthTotalStr;
	}
	public String getCurrentMonthTotalStr() {
		return currentMonthTotalStr;
	}
	public void setCurrentMonthTotalStr(String currentMonthTotalStr) {
		this.currentMonthTotalStr = currentMonthTotalStr;
	}
	public String getCustomName() {
		return customName;
	}
	public void setCustomName(String customName) {
		this.customName = customName;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCompanyId() {
		return companyId;
	}
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	public String getOriginalId() {
		return originalId;
	}
	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}
	public String getIdentityNo() {
		return identityNo;
	}
	public void setIdentityNo(String identityNo) {
		this.identityNo = identityNo;
	}
	public Integer getLastDayTotal() {
		return lastDayTotal;
	}
	public void setLastDayTotal(Integer lastDayTotal) {
		this.lastDayTotal = lastDayTotal;
	}
	public Integer getTodayTotal() {
		return todayTotal;
	}
	public void setTodayTotal(Integer todayTotal) {
		this.todayTotal = todayTotal;
	}
	public Integer getLastMonthTotal() {
		return lastMonthTotal;
	}
	public void setLastMonthTotal(Integer lastMonthTotal) {
		this.lastMonthTotal = lastMonthTotal;
	}
	public Integer getCurrentMonthTotal() {
		return currentMonthTotal;
	}
	public void setCurrentMonthTotal(Integer currentMonthTotal) {
		this.currentMonthTotal = currentMonthTotal;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

    public Integer getLastQuarterTotal() {
        return lastQuarterTotal;
    }

    public void setLastQuarterTotal(Integer lastQuarterTotal) {
        this.lastQuarterTotal = lastQuarterTotal;
    }

    public Integer getCurrentQuarterTotal() {
        return currentQuarterTotal;
    }

    public void setCurrentQuarterTotal(Integer currentQuarterTotal) {
        this.currentQuarterTotal = currentQuarterTotal;
    }

    public String getLastQuarterTotalStr() {
        return lastQuarterTotalStr;
    }

    public void setLastQuarterTotalStr(String lastQuarterTotalStr) {
        this.lastQuarterTotalStr = lastQuarterTotalStr;
    }

    public String getCurrentQuarterTotalStr() {
        return currentQuarterTotalStr;
    }

    public void setCurrentQuarterTotalStr(String currentQuarterTotalStr) {
        this.currentQuarterTotalStr = currentQuarterTotalStr;
    }
}
 