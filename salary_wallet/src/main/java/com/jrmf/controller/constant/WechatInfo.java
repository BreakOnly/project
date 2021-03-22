package com.jrmf.controller.constant;

/**
 * 1221
 * @author Administrator zh
 *
 */
public class WechatInfo {
    private String zhishuitongAppid;
    private String zhishuitongAppSeckey;
    private String snsAccessTokenUrl;
    private String authorizeUrl;
    private String baseUrl;
    private String aygPrivateKey;
    private String basePath;
    
    public WechatInfo(String zhishuitongAppid, 
    		String zhishuitongAppSeckey,
			String snsAccessTokenUrl,
			String authorizeUrl,
			String baseUrl,
			String aygPrivateKey,
			String basePath) {
		super();
		this.zhishuitongAppid = zhishuitongAppid;
		this.zhishuitongAppSeckey = zhishuitongAppSeckey;
		this.snsAccessTokenUrl = snsAccessTokenUrl;
		this.authorizeUrl = authorizeUrl;
		this.baseUrl = baseUrl;
		this.aygPrivateKey = aygPrivateKey;
		this.basePath = basePath;
	}
    
	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getZhishuitongAppid() {
		return zhishuitongAppid;
	}
	public void setZhishuitongAppid(String zhishuitongAppid) {
		this.zhishuitongAppid = zhishuitongAppid;
	}
	public String getZhishuitongAppSeckey() {
		return zhishuitongAppSeckey;
	}
	public void setZhishuitongAppSeckey(String zhishuitongAppSeckey) {
		this.zhishuitongAppSeckey = zhishuitongAppSeckey;
	}
	public String getSnsAccessTokenUrl() {
		return snsAccessTokenUrl;
	}
	public void setSnsAccessTokenUrl(String snsAccessTokenUrl) {
		this.snsAccessTokenUrl = snsAccessTokenUrl;
	}
	public String getAuthorizeUrl() {
		return authorizeUrl;
	}
	public void setAuthorizeUrl(String authorizeUrl) {
		this.authorizeUrl = authorizeUrl;
	}
	public String getBaseUrl() {
		return baseUrl;
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	public String getAygPrivateKey() {
		return aygPrivateKey;
	}
	public void setAygPrivateKey(String aygPrivateKey) {
		this.aygPrivateKey = aygPrivateKey;
	}
    
}
