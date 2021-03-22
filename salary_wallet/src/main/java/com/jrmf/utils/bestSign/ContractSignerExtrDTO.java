package com.jrmf.utils.bestSign;

/**
 * Created by gavin on 2017/10/17.
 */
public class ContractSignerExtrDTO implements IObject {


    private String extrOrderId;

    private String identity;

    private String name;

    private String identityType;

    private String personalMobile;

    @Override
    public String toString() {
        return "ContractSignerExtrDTO{" +
                "extrOrderId='" + extrOrderId + '\'' +
                ", identity='" + identity + '\'' +
                ", name='" + name + '\'' +
                ", identityType='" + identityType + '\'' +
                ", personalMobile='" + personalMobile + '\'' +
                '}';
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
