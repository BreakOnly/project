package com.jrmf.utils.dto;

public class SignaturePair {

	private String origin;
	private String signature;
	
	
	public SignaturePair(String origin, String signature) {
		super();
		this.origin = origin;
		this.signature = signature;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	
}
