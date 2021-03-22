package com.jrmf.payment.openapi.model.request;

import java.util.List;
import java.util.Map;

public interface IFormDataParam<RES> extends IBaseParam<RES> {

	Map<String, String> queryParams();
	List<FormDataItem> formData();
}
