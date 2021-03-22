package com.jrmf.utils.bestSign;
public abstract class CommonExtrParam  implements java.io.Serializable,IObject {
	private static final long serialVersionUID = 1L;

	public CommonExtrParam() {
		// TODO Auto-generated constructor stub
	}

	private String extrSystemId;
	private String sign;
	private String notifyUrl="";


	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getExtrSystemId() {
		return extrSystemId;
	}

	public void setExtrSystemId(String extrSystemId) {
		this.extrSystemId = extrSystemId;
	}


	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}
}
