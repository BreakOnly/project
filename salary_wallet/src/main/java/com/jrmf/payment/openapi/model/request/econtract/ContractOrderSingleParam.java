package com.jrmf.payment.openapi.model.request.econtract;

import com.jrmf.payment.openapi.model.request.IBaseParam;
import com.jrmf.payment.openapi.model.response.econtract.ContractOrderResult;

public class ContractOrderSingleParam implements IBaseParam<ContractOrderResult> {

    private String templateId;
    private String notifyUrl;
    private String extrOrderId;
    private String identity;
    private String name;
    private String identityType;
    private String personalMobile;
	
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
	 * @return the notifyUrl
	 */
	public String getNotifyUrl() {
		return notifyUrl;
	}

	/**
	 * @param notifyUrl the notifyUrl to set
	 */
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
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
	 * @return the identity
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * @param identity the identity to set
	 */
	public void setIdentity(String identity) {
		this.identity = identity;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the identityType
	 */
	public String getIdentityType() {
		return identityType;
	}

	/**
	 * @param identityType the identityType to set
	 */
	public void setIdentityType(String identityType) {
		this.identityType = identityType;
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

	@Override
	public String requestURI() {
		return "/econtract/extr/order/submit";
	}

	@Override
	public String methodName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String version() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> respDataClass() {
		return ContractOrderResult.class;
	}

}
