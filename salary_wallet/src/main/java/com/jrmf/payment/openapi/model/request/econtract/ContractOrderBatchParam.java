package com.jrmf.payment.openapi.model.request.econtract;

import java.util.List;

import com.jrmf.payment.openapi.model.request.IBaseParam;

public class ContractOrderBatchParam implements IBaseParam<Void> {

    private String templateId;
    private String notifyUrl = "";
    private List<ContractSignerItem> list;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public List<ContractSignerItem> getList() {
        return list;
    }

    public void setList(List<ContractSignerItem> list) {
        this.list = list;
    }

	/* (non-Javadoc)
	 * @see com.jrmf.payment.openapi.model.request.IBaseParam#requestURI()
	 */
	@Override
	public String requestURI() {
		return "/econtract/extr/order/batchsubmit";
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

	@Override
	public Class<?> respDataClass() {
		return Void.class;
	}
    
}
