package com.jrmf.payment.openapi.model.response.invoice;

import java.util.List;

import com.jrmf.payment.openapi.model.response.IBizResult;

public class QueryInvoiceBindResult implements IBizResult{
	
	private List<ServiceCompanyResult> serviceCompanies;
	private List<CustomCompanyResult> customCompanies;
	private List<InvoiceSubjectResult> subjects;

	public List<ServiceCompanyResult> getServiceCompanies() {
		return serviceCompanies;
	}

	public void setServiceCompanies(List<ServiceCompanyResult> serviceCompanies) {
		this.serviceCompanies = serviceCompanies;
	}

	public List<CustomCompanyResult> getCustomCompanies() {
		return customCompanies;
	}

	public void setCustomCompanies(List<CustomCompanyResult> customCompanies) {
		this.customCompanies = customCompanies;
	}

	public List<InvoiceSubjectResult> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<InvoiceSubjectResult> subjects) {
		this.subjects = subjects;
	}

}
