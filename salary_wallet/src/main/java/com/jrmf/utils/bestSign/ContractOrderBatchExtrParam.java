package com.jrmf.utils.bestSign;



import java.util.List;

public class ContractOrderBatchExtrParam extends CommonExtrParam implements IObject {


    /** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = 6808645641184534034L;


	private String templateId;


    private String notifyMethod;

    private String notifyUrl = "";

    private List<ContractSignerExtrDTO> list;


    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getNotifyMethod() {
        return notifyMethod;
    }

    public void setNotifyMethod(String notifyMethod) {
        this.notifyMethod = notifyMethod;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public List<ContractSignerExtrDTO> getList() {
        return list;
    }

    public void setList(List<ContractSignerExtrDTO> list) {
        this.list = list;
    }


}
