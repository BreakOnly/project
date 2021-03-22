package com.jrmf.domain;

import java.io.Serializable;

/**
 * @author 种路路
 * @create 2018-11-12 14:49
 * @desc 商户签约合同模版
 **/
public class AgreementTemplate implements Serializable {
    /**
     * 主键
     */
    private int id;
    /**
     * 签约模板名称
     */
    private String templateName;
    /**
     * 协议名称
     */
    private String agreementName;
    /**
     * 登记业务类型
     */
    private int regType;
    /**
     * 魔方商户号
     */
    private String originalId;
    /**
     * 平台号
     */
    private String merchantId;
    /**
     * 服务公司编号
     */
    private String companyId;
    /**
     * 签约第三方商户ID
     */
    private String thirdMerchId;
    /**
     * 签名模板ID
     */
    private String thirdTemplateId;
    /**
     * 签约模板描述
     */
    private String thirdTemplateDes;
    /**
     *签约方式
     1-本地人工审核签约
     2-调用第三方接口静默签约
     */
    private String agreementType;
    /**
     *模板协议限制
     1-先签约后支付
     2-先支付后签约
     3-不限制
     */
    private String agreementPayment;
    /**
     *签名模板URL地址
     */
    private String agreementTemplateURL;
    /**
     *签名模板参数1
     */
    private String agreementTemplateParamsA;
    /**
     *签名模板参数2
     */
    private String agreementTemplateParamsB;
    /**
     *签名模板参数3
     */
    private String agreementTemplateParamsC;
    /**
     *创建时间
     */
    private String createTime;
    /**
     *最后一次更新时间
     */
    private String lastUpdateTime;

    private Integer channelType;

    private String privateKey;

    private String publicKey;

    private String apiKey;

    private String reqUrl;
  /**
   * 是否上传身份证图片 1：上传，2：不上传
   */
  private Integer uploadIdCard;

  public Integer getUploadIdCard() {
    return uploadIdCard;
  }

  public void setUploadIdCard(Integer uploadIdCard) {
    this.uploadIdCard = uploadIdCard;
  }


	@Override
	public String toString() {
		return "AgreementTemplate [id=" + id + ", templateName=" + templateName
				+ ", agreementName=" + agreementName + ", regType=" + regType
				+ ", originalId=" + originalId + ", merchantId=" + merchantId
				+ ", companyId=" + companyId + ", thirdMerchId=" + thirdMerchId
				+ ", thirdTemplateId=" + thirdTemplateId
				+ ", thirdTemplateDes=" + thirdTemplateDes + ", agreementType="
				+ agreementType + ", agreementPayment=" + agreementPayment
				+ ", agreementTemplateURL=" + agreementTemplateURL
				+ ", agreementTemplateParamsA=" + agreementTemplateParamsA
				+ ", agreementTemplateParamsB=" + agreementTemplateParamsB
				+ ", agreementTemplateParamsC=" + agreementTemplateParamsC
				+ ", createTime=" + createTime + ", lastUpdateTime="
				+ lastUpdateTime + ", channelType=" + channelType
				+ ", privateKey=" + privateKey + ", publicKey=" + publicKey
				+ ", apiKey=" + apiKey + ", reqUrl=" + reqUrl
				+ ", htmlTemplate=" + htmlTemplate + ", preparedA=" + preparedA
				+ ", preparedB=" + preparedB + ", customName=" + customName
				+ ", companyName=" + companyName + "]";
	}

	public String getHtmlTemplate() {
        return htmlTemplate;
    }

    public void setHtmlTemplate(String htmlTemplate) {
        this.htmlTemplate = htmlTemplate;
    }

    /**
     *最后一次更新时间
     */
    private String htmlTemplate;
    /**
     *预留
     */
    private String preparedA;
    /**
     *预留
     */
    private String preparedB;
    /**
     *冗余字段-商户名称
     */
    private String customName;
    /**
     *冗余字段-服务公司名称
     */
    private String companyName;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRegType() {
        return regType;
    }

    public void setRegType(int regType) {
        this.regType = regType;
    }

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getThirdMerchId() {
        return thirdMerchId;
    }

    public void setThirdMerchId(String thirdMerchId) {
        this.thirdMerchId = thirdMerchId;
    }

    public String getThirdTemplateId() {
        return thirdTemplateId;
    }

    public void setThirdTemplateId(String thirdTemplateId) {
        this.thirdTemplateId = thirdTemplateId;
    }

    public String getThirdTemplateDes() {
        return thirdTemplateDes;
    }

    public void setThirdTemplateDes(String thirdTemplateDes) {
        this.thirdTemplateDes = thirdTemplateDes;
    }

    public String getAgreementType() {
        return agreementType;
    }

    public void setAgreementType(String agreementType) {
        this.agreementType = agreementType;
    }

    public String getAgreementPayment() {
        return agreementPayment;
    }

    public void setAgreementPayment(String agreementPayment) {
        this.agreementPayment = agreementPayment;
    }

    public String getAgreementTemplateURL() {
        return agreementTemplateURL;
    }

    public void setAgreementTemplateURL(String agreementTemplateURL) {
        this.agreementTemplateURL = agreementTemplateURL;
    }

    public String getAgreementTemplateParamsA() {
        return agreementTemplateParamsA;
    }

    public void setAgreementTemplateParamsA(String agreementTemplateParamsA) {
        this.agreementTemplateParamsA = agreementTemplateParamsA;
    }

    public String getAgreementTemplateParamsB() {
        return agreementTemplateParamsB;
    }

    public void setAgreementTemplateParamsB(String agreementTemplateParamsB) {
        this.agreementTemplateParamsB = agreementTemplateParamsB;
    }

    public String getAgreementTemplateParamsC() {
        return agreementTemplateParamsC;
    }

    public void setAgreementTemplateParamsC(String agreementTemplateParamsC) {
        this.agreementTemplateParamsC = agreementTemplateParamsC;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getPreparedA() {
        return preparedA;
    }

    public void setPreparedA(String preparedA) {
        this.preparedA = preparedA;
    }

    public String getPreparedB() {
        return preparedB;
    }

    public void setPreparedB(String preparedB) {
        this.preparedB = preparedB;
    }

    public String getAgreementName() {
        return agreementName;
    }
    public void setAgreementName(String agreementName) {
        this.agreementName = agreementName;
    }

	public Integer getChannelType() {
		return channelType;
	}

	public void setChannelType(Integer channelType) {
		this.channelType = channelType;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getReqUrl() {
		return reqUrl;
	}

	public void setReqUrl(String reqUrl) {
		this.reqUrl = reqUrl;
	}

  public void build( String templateName, String agreementName,
       String merchantId, String companyId, String thirdMerchId,
      String thirdTemplateId, String thirdTemplateDes, String agreementType,
      String agreementPayment, String agreementTemplateURL,
       Integer channelType, Integer uploadIdCard, String htmlTemplate) {
      if (templateName!=null &&!"".endsWith(templateName)){
        this.templateName = templateName;
      }
      if (agreementName!=null && !"".equals(agreementName)){
        this.agreementName = agreementName;
      }
      if (merchantId!=null && !"".equals(merchantId)){
        this.merchantId = merchantId;
      }
    if (companyId!=null && !"".equals(companyId)){
      this.companyId = companyId;
    }
    if (thirdMerchId!=null && !"".equals(thirdMerchId)){
      this.thirdMerchId = thirdMerchId;
    }
    if (thirdTemplateId!=null && !"".equals(thirdTemplateId)){
      this.thirdTemplateId = thirdTemplateId;
    }
    if (thirdTemplateDes!=null && !"".equals(thirdTemplateDes)){
      this.thirdTemplateDes = thirdTemplateDes;
    }
    if (agreementType!=null && !"".equals(agreementType)){
      this.agreementType = agreementType;
    }
    if (agreementPayment!=null && !"".equals(agreementPayment)){
      this.agreementPayment = agreementPayment;
    }
    if (agreementTemplateURL!=null && !"".equals(agreementTemplateURL)){
      this.agreementTemplateURL = agreementTemplateURL;
    }
    if (channelType!=null && !"".equals(channelType)){
      this.channelType = channelType;
    }
    if (uploadIdCard!=null && !"".equals(uploadIdCard)){
      this.uploadIdCard = uploadIdCard;
    }
    if (htmlTemplate!=null && !"".equals(htmlTemplate)){
      this.htmlTemplate = htmlTemplate;
    }
  }
}
