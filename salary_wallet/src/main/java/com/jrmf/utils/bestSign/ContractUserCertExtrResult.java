package com.jrmf.utils.bestSign;

public class ContractUserCertExtrResult extends CommonExtrResult {
    /** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = 1L;
	//身份证异步上传
    private String extrSystemId;
    private String identity;
    private String identityType;

    public ContractUserCertExtrResult (String resultCode, String resultMessage, String extrSystemId, String identity, String identityType) {
        super(resultCode, resultMessage);
        this.extrSystemId = extrSystemId;
        this.identity = identity;
        this.identityType = identityType;
    }

    public ContractUserCertExtrResult() {
    }

    public String getExtrSystemId() {
        return extrSystemId;
    }

    public void setExtrSystemId(String extrSystemId) {
        this.extrSystemId = extrSystemId;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

	@Override
	public String toString() {
		return "ContractUserCertExtrResult [extrSystemId=" + extrSystemId + ", identity=" + identity + ", identityType="
				+ identityType + "]"+super.toString();
	}
    
}
