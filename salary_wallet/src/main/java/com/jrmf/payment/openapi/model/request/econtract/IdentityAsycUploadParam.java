package com.jrmf.payment.openapi.model.request.econtract;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jrmf.payment.openapi.model.request.FormDataItem;
import com.jrmf.payment.openapi.model.request.IFormDataParam;

public class IdentityAsycUploadParam implements IFormDataParam<Void>{

	Map<String, String> queryParams = new HashMap<>();
	List<FormDataItem> formData = new ArrayList<>();

	@Override
	public String requestURI() {
		return "/econtract/extr/identity/asyn/upload";
	}
	
	@Override
	public String methodName() {
		return null;
	}

	@Override
	public String version() {
		return null;
	}

	@Override
	public Map<String, String> queryParams() {
		return queryParams;
	}

	@Override
	public List<FormDataItem> formData() {
		return formData;
	}

	public void setName(String name){
		queryParams.put("name", name);
	}
	
	public void setIdentityType(String identityType){
		queryParams.put("identityType", identityType);
	}
	
	public void setIdentity(String identity){
		queryParams.put("identity", identity);
	}
	
	public void setNotifyUrl(String notifyUrl){
		queryParams.put("notifyUrl", notifyUrl);
	}

	public void setFrontfile(File file){
		formData.add(new FormDataItem("frontfile", file, "image/png"));
	}
	
	public void setBackfile(File file){
		formData.add(new FormDataItem("backfile", file, "image/png"));
	}

	@Override
	public Class<?> respDataClass() {
		return Void.class;
	}
	
}
