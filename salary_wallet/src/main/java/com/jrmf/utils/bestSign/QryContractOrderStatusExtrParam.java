package com.jrmf.utils.bestSign;

public class QryContractOrderStatusExtrParam extends CommonExtrParam implements IObject {

    /** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = -7691819564612183246L;
	private String orderId;
	private String extrOrderId;
	
    public String getExtrOrderId() {
		return extrOrderId;
	}

	public void setExtrOrderId(String extrOrderId) {
		this.extrOrderId = extrOrderId;
	}

	public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
