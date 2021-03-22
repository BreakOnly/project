package com.jrmf.payment.openapi.model.request.econtract;

import com.jrmf.payment.openapi.constants.CertificationType;

public class ContractSignerItem {
	
	private String extrOrderId;
    private String identity;
    private String name;
    private String identityType;
    private String personalMobile;

    public String getExtrOrderId() {
        return extrOrderId;
    }

    public void setExtrOrderId(String extrOrderId) {
        this.extrOrderId = extrOrderId;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(CertificationType identityType) {
        this.identityType = identityType.getCode();
    }

    public String getPersonalMobile() {
        return personalMobile;
    }

    public void setPersonalMobile(String personalMobile) {
        this.personalMobile = personalMobile;
    }
	
}
