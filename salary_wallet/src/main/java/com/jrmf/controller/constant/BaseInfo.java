package com.jrmf.controller.constant;

/**
 * 20190122
 * @author Administrator zh
 *
 */
public class BaseInfo {
 
	private String transferBaffleSwitch;//下发挡板开关
    private String calculationLimit;//计费额度值
    private String domainName;//域名
    
    public BaseInfo(String transferBaffleSwitch,
    		String calculationLimit,
    		String domainName) {
		super();
		this.transferBaffleSwitch = transferBaffleSwitch;
		this.calculationLimit = calculationLimit;
		this.domainName = domainName;
	}
    
	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}


	public String getTransferBaffleSwitch() {
		return transferBaffleSwitch;
	}
	public void setTransferBaffleSwitch(String transferBaffleSwitch) {
		this.transferBaffleSwitch = transferBaffleSwitch;
	}
	public String getCalculationLimit() {
		return calculationLimit;
	}
	public void setCalculationLimit(String calculationLimit) {
		this.calculationLimit = calculationLimit;
	}
    


}
