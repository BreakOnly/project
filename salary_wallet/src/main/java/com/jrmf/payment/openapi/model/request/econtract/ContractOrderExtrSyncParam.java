/**
 * 
 */
package com.jrmf.payment.openapi.model.request.econtract;

import java.util.Date;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.param.econtract.CommonExtrResult;

/**
 * @author Napoleon.Chen
 * @date 2019年1月3日
 */
public class ContractOrderExtrSyncParam implements IBaseParam<CommonExtrResult> {

	private String contractId;
	private String templateId;
	private Date createTime; 
	private String extrOrderId;
	private String outerDownloadUrl;
	private Date partyaSignTime;
	private String partyaUserId;
	private String partyaUserName;
	private Date partybSignTime;
	private String partybUserId;
	private String partybUserName;
	private Date partycSignTime;
	private String partycUserId;
	private String partycUserName;
	private String personalIdentity;
	private String personalIdentityType;
	private String personalMobile;
	private String personalName;
	private String personalCertId;
	private String personalCertType;
	private String manufacturer;
	private String contractContentBase64;
	private boolean check;

	/**
	 * @return the contractId
	 */
	public String getContractId() {
		return contractId;
	}

	/**
	 * @param contractId the contractId to set
	 */
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}

	/**
	 * @return the templateId
	 */
	public String getTemplateId() {
		return templateId;
	}

	/**
	 * @param templateId the templateId to set
	 */
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * @param createTime the createTime to set
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * @return the extrOrderId
	 */
	public String getExtrOrderId() {
		return extrOrderId;
	}

	/**
	 * @param extrOrderId the extrOrderId to set
	 */
	public void setExtrOrderId(String extrOrderId) {
		this.extrOrderId = extrOrderId;
	}

	/**
	 * @return the outerDownloadUrl
	 */
	public String getOuterDownloadUrl() {
		return outerDownloadUrl;
	}

	/**
	 * @param outerDownloadUrl the outerDownloadUrl to set
	 */
	public void setOuterDownloadUrl(String outerDownloadUrl) {
		this.outerDownloadUrl = outerDownloadUrl;
	}

	/**
	 * @return the partyaSignTime
	 */
	public Date getPartyaSignTime() {
		return partyaSignTime;
	}

	/**
	 * @param partyaSignTime the partyaSignTime to set
	 */
	public void setPartyaSignTime(Date partyaSignTime) {
		this.partyaSignTime = partyaSignTime;
	}

	/**
	 * @return the partyaUserId
	 */
	public String getPartyaUserId() {
		return partyaUserId;
	}

	/**
	 * @param partyaUserId the partyaUserId to set
	 */
	public void setPartyaUserId(String partyaUserId) {
		this.partyaUserId = partyaUserId;
	}

	/**
	 * @return the partyaUserName
	 */
	public String getPartyaUserName() {
		return partyaUserName;
	}

	/**
	 * @param partyaUserName the partyaUserName to set
	 */
	public void setPartyaUserName(String partyaUserName) {
		this.partyaUserName = partyaUserName;
	}

	/**
	 * @return the partybSignTime
	 */
	public Date getPartybSignTime() {
		return partybSignTime;
	}

	/**
	 * @param partybSignTime the partybSignTime to set
	 */
	public void setPartybSignTime(Date partybSignTime) {
		this.partybSignTime = partybSignTime;
	}

	/**
	 * @return the partybUserId
	 */
	public String getPartybUserId() {
		return partybUserId;
	}

	/**
	 * @param partybUserId the partybUserId to set
	 */
	public void setPartybUserId(String partybUserId) {
		this.partybUserId = partybUserId;
	}

	/**
	 * @return the partybUserName
	 */
	public String getPartybUserName() {
		return partybUserName;
	}

	/**
	 * @param partybUserName the partybUserName to set
	 */
	public void setPartybUserName(String partybUserName) {
		this.partybUserName = partybUserName;
	}

	/**
	 * @return the partycSignTime
	 */
	public Date getPartycSignTime() {
		return partycSignTime;
	}

	/**
	 * @param partycSignTime the partycSignTime to set
	 */
	public void setPartycSignTime(Date partycSignTime) {
		this.partycSignTime = partycSignTime;
	}

	/**
	 * @return the partycUserId
	 */
	public String getPartycUserId() {
		return partycUserId;
	}

	/**
	 * @param partycUserId the partycUserId to set
	 */
	public void setPartycUserId(String partycUserId) {
		this.partycUserId = partycUserId;
	}

	/**
	 * @return the partycUserName
	 */
	public String getPartycUserName() {
		return partycUserName;
	}

	/**
	 * @param partycUserName the partycUserName to set
	 */
	public void setPartycUserName(String partycUserName) {
		this.partycUserName = partycUserName;
	}

	/**
	 * @return the personalIdentity
	 */
	public String getPersonalIdentity() {
		return personalIdentity;
	}

	/**
	 * @param personalIdentity the personalIdentity to set
	 */
	public void setPersonalIdentity(String personalIdentity) {
		this.personalIdentity = personalIdentity;
	}

	/**
	 * @return the personalIdentityType
	 */
	public String getPersonalIdentityType() {
		return personalIdentityType;
	}

	/**
	 * @param personalIdentityType the personalIdentityType to set
	 */
	public void setPersonalIdentityType(String personalIdentityType) {
		this.personalIdentityType = personalIdentityType;
	}

	/**
	 * @return the personalMobile
	 */
	public String getPersonalMobile() {
		return personalMobile;
	}

	/**
	 * @param personalMobile the personalMobile to set
	 */
	public void setPersonalMobile(String personalMobile) {
		this.personalMobile = personalMobile;
	}

	/**
	 * @return the personalName
	 */
	public String getPersonalName() {
		return personalName;
	}

	/**
	 * @param personalName the personalName to set
	 */
	public void setPersonalName(String personalName) {
		this.personalName = personalName;
	}

	/**
	 * @return the personalCertId
	 */
	public String getPersonalCertId() {
		return personalCertId;
	}

	/**
	 * @param personalCertId the personalCertId to set
	 */
	public void setPersonalCertId(String personalCertId) {
		this.personalCertId = personalCertId;
	}

	/**
	 * @return the personalCertType
	 */
	public String getPersonalCertType() {
		return personalCertType;
	}

	/**
	 * @param personalCertType the personalCertType to set
	 */
	public void setPersonalCertType(String personalCertType) {
		this.personalCertType = personalCertType;
	}

	/**
	 * @return the manufacturer
	 */
	public String getManufacturer() {
		return manufacturer;
	}

	/**
	 * @param manufacturer the manufacturer to set
	 */
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	
	/**
	 * @return the contractContentBase64
	 */
	public String getContractContentBase64() {
		return contractContentBase64;
	}

	/**
	 * @param contractContentBase64 the contractContentBase64 to set
	 */
	public void setContractContentBase64(String contractContentBase64) {
		this.contractContentBase64 = contractContentBase64;
	}

	/**
	 * @return the check
	 */
	public boolean isCheck() {
		return check;
	}

	/**
	 * @param check the check to set
	 */
	public void setCheck(boolean check) {
		this.check = check;
	}

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#requestURI()
	 */
	@Override
	public String requestURI() {
		return "/econtract/extr/order/outer-sync";
	}

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#methodName()
	 */
	@Override
	public String methodName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#version()
	 */
	@Override
	public String version() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#respDataClass()
	 */
	@Override
	public Class<?> respDataClass() {
		return CommonExtrResult.class;
	}
	
}
