package com.jrmf.taxsettlement.api.gateway.batch.form;

import com.jrmf.taxsettlement.api.service.ActionParams;

public interface DataFormTemplate<D extends DataFormUnit> {

	ActionParams parse(D dataFormUnit, Class<? extends ActionParams> exactalParamClass) throws Exception;
}
