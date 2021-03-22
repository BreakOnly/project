package com.jrmf.utils.bestSign;

public class ContractOrderSingleExtrParam extends CommonExtrParam implements IObject {


    /** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = -1439591914461421586L;
	private String templateId;
    private String notifyMethod;
    private String extrOrderId;
    private String identity;
    private String name;
    private String identityType;
    private String personalMobile;


    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getNotifyMethod() {
        return notifyMethod;
    }

    public void setNotifyMethod(String notifyMethod) {
        this.notifyMethod = notifyMethod;
    }

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

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public String getPersonalMobile() {
        return personalMobile;
    }

    public void setPersonalMobile(String personalMobile) {
        this.personalMobile = personalMobile;
    }


}
