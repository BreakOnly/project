package com.jrmf.taxsettlement.api.gateway.batch.form;

import java.io.IOException;

import com.jrmf.taxsettlement.api.service.ActionParams;

public interface BatchFormDistiller {

	BatchFormDistillResult distill(byte[] fileBytes, Class<? extends ActionParams> exactalParamClass) throws IOException;

}
