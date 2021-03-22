package com.jrmf.payment.openapi.model.request;

import java.io.File;

public class FormDataItem {

	private String name;
	private String value;
	private File file;
	private String mimeType;
	
	
	public FormDataItem(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public FormDataItem(String name, File file, String mimeType) {
		this.name = name;
		this.file = file;
		this.mimeType = mimeType;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	
}
