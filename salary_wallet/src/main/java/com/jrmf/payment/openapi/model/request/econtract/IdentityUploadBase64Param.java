/**
 * 
 */
package com.jrmf.payment.openapi.model.request.econtract;

import java.text.MessageFormat;
import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;

import com.jrmf.payment.openapi.model.request.IBase64Param;
import com.jrmf.payment.openapi.model.response.econtract.IdentityUploadBase64Result;

/**
 * @author Napoleon.Chen
 * @date 2018年12月14日
 */
public class IdentityUploadBase64Param implements IBase64Param<IdentityUploadBase64Result> {

	private String backfile;
	private String extrSystemId;
	private String frontfile;
	private String name;
	private String identity;
    private String identityType;

	/**
	 * @return the backfile
	 */
	public String getBackfile() {
		return backfile;
	}

	/**
	 * @param backfile the backfile to set
	 */
	public void setBackfile(String backfile) {
		this.backfile = backfile;
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
	 * @return the frontfile
	 */
	public String getFrontfile() {
		return frontfile;
	}

	/**
	 * @param frontfile the frontfile to set
	 */
	public void setFrontfile(String frontfile) {
		this.frontfile = frontfile;
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

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#requestURI()
	 */
	@Override
	public String requestURI() {
		return "/econtract/extr/identity/upload-base64";
	}

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#methodName()
	 */
	@Override
	public String methodName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#version()
	 */
	@Override
	public String version() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#respDataClass()
	 */
	@Override
	public Class<?> respDataClass() {
		return IdentityUploadBase64Result.class;
	}

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBase64Param#format()
	 */
	@Override
	public String format() {
		return MessageFormat.format("backfile={0}&extrSystemId={1}&frontfile={2}&identity={3}&identityType={4}&name={5}", 
				DigestUtils.md5Hex(Base64.getDecoder().decode(backfile)), extrSystemId, 
				DigestUtils.md5Hex(Base64.getDecoder().decode(frontfile)), identity, identityType, name);
	}
	
}
