package com.jrmf.payment.openapi.model.request.econtract;

import java.text.MessageFormat;
import java.util.UUID;

public class ContractTemplateDownloadParam {

	private String templateId;
	private String extrSystemId;
	private String localPath;
	
	public String requestURI() {
		return MessageFormat.format("/econtract/extr/template/download?extrSystemId={0}&templateId={1}&nonce={2}", extrSystemId, templateId, UUID.randomUUID().toString().replaceAll("-", ""));
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
	 * @return the extrSystemId
	 */
	public String getExtrSystemId() {
		return extrSystemId;
	}

	/**
	 * @param extrSystemId the extrSystemId to set
	 */
	public void setExtrSystemId(String extrSystemId) {
		this.extrSystemId = extrSystemId;
	}

	/**
	 * @return the localPath
	 */
	public String getLocalPath() {
		return localPath;
	}

	/**
	 * @param localPath the localPath to set
	 */
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	
}
